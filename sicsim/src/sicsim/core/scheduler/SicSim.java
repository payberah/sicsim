/*
 * Copyright (c) 2008
 *  
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package sicsim.core.scheduler;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

import sicsim.config.SicsimConfig;
import sicsim.core.scenario.Scenario;
import sicsim.network.core.*;
import sicsim.network.links.AbstractLink;
import sicsim.network.peers.AbstractPeer;
import sicsim.types.Message;
import sicsim.types.NodeId;
import sicsim.utils.Distribution;
import sicsim.utils.FileIO;

/**
 * Class to implement the core module of simulator. It creates the main objects of the system.
 * @see Scheduler
 * @see Scenario
 * @see FEL   
 */
public class SicSim {
	
	private Bandwidth bandwidth = new Bandwidth();
	private CoreNet coreNet = new CoreNet();
	private Network network = new Network(this, this.bandwidth, this.coreNet);
	private OverlayNetwork overlay = new OverlayNetwork(this.network);
	private FailureDetector failureDetector = new FailureDetector(this, this.network);
	private Scenario scenario = new Scenario();
	private FEL futureEventList = new FEL();
	private Scheduler scheduler = new Scheduler(this.scenario, this.futureEventList);
	private Monitor monitor;
	
	private long overlayLastUpdate = -1;
	private long localTime = 0;
	private long currentTime = 0;
	private long lastNetSize = 0;
	
	private Random failureRandomTime = new Random(SicsimConfig.FAILURE_DETECTION_SEED);
	
//----------------------------------------------------------------------------------
	public SicSim() {
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Specifies the duration of running the simulation.
	 * @param till The maximum time of simulation.   
	 */
	public void runTill(long till) {
		while (this.scheduler.getCurrentClock() < till) {
			if (!this.singleStep())
				break;
			this.snapshot();
		}
	
		if (SicsimConfig.LOG_SIM && SicsimConfig.LOG_LEVEL >= SicsimConfig.LOG_INFO)
			System.out.println("SICSSIM => finish ---> time: " + this.currentTime);
		
		if (this.monitor != null)
			this.monitor.verify(this.scheduler.getCurrentClock());
	}

//----------------------------------------------------------------------------------
	private boolean singleStep() {
		if (SicsimConfig.SYNC_UPDATE)
			this.network.updateNetwork(this.scheduler.getCurrentClock());
		
		if (SicsimConfig.MONITOR) {
			if (this.monitor != null && this.overlayLastUpdate < this.scheduler.getCurrentClock()) {
				this.monitor.update(this.scheduler.getCurrentClock());
				this.overlayLastUpdate = this.scheduler.getCurrentClock();
			}
		}
		
		FutureEvent currentEvent = this.scheduler.nextStep();
		if (currentEvent != null) {
			this.doStep(currentEvent);
			return true;
		}
		
		return false;
	}

//----------------------------------------------------------------------------------
	private void doStep(FutureEvent currentEvent) {
		if (SicsimConfig.LOG_SIM && SicsimConfig.LOG_LEVEL >= SicsimConfig.LOG_DEBUG)
			System.out.println("SICSSIM => from " + currentEvent.srcId + " to " + currentEvent.destId + " event " + currentEvent.type + " data (" + currentEvent.data + ")");
			
		if (currentEvent.type.equalsIgnoreCase("MSG"))
			this.handleMsgEvent(currentEvent);
		else if (currentEvent.type.equalsIgnoreCase("SIM_MSG"))
			this.handleSimMsgEvent(currentEvent);
		else if (currentEvent.type.equalsIgnoreCase("PERIODIC"))
			this.handlePeriodicEvent(currentEvent);
		else if (currentEvent.type.equalsIgnoreCase("JOIN"))
			this.handleJoinEvent(currentEvent.nodeType, currentEvent.linkType);
		else if (currentEvent.type.equalsIgnoreCase("LEAVE"))
			this.handleLeaveEvent();				
		else if (currentEvent.type.equalsIgnoreCase("FAILURE"))
			this.handleFailureEvent();				
		else if (currentEvent.type.equalsIgnoreCase("DELAY"))
			this.handleDelayEvent();
		else if (currentEvent.type.equalsIgnoreCase("FAILURE_DETECTION"))
			this.handleFailureDetectionEvent(currentEvent);
		else if (currentEvent.type.equalsIgnoreCase("SAVE_OVERLAY"))
			this.handleSaveOverlayEvent();
		else if (currentEvent.type.equalsIgnoreCase("LOAD_OVERLAY"))
			this.handleLoadOverlayEvent();
		else if (currentEvent.type.equalsIgnoreCase("MONITOR_OVERLAY"))
			this.handleLoadMonitorEvent(currentEvent.monitorOverlay);
		else if (currentEvent.type.equalsIgnoreCase("SIGNAL"))
			this.handleSignalEvent(currentEvent.signal);
	}

//----------------------------------------------------------------------------------
	private boolean handleMsgEvent(FutureEvent event) {
		AbstractPeer srcNode = this.network.getNode(event.srcId);
		AbstractPeer destNode = this.network.getNode(event.destId);

		if (destNode == null)			
			return false;

		if (event.data.type.equalsIgnoreCase("START_RECV_DATA")) {
			if (srcNode != null)
				this.bandwidth.useDownloadBandwidth(event.srcId, event.destId, SicsimConfig.STRIPE_RATE);
		} else if (event.data.type.equalsIgnoreCase("STOP_RECV_DATA"))
			if (srcNode != null)
				this.bandwidth.releaseDownloadBandwidth(event.srcId, event.destId, SicsimConfig.STRIPE_RATE);

		destNode.receive(event.srcId, event.data, this.scheduler.getCurrentClock());
		
		return true;
	}
	
//----------------------------------------------------------------------------------
	private boolean handleSimMsgEvent(FutureEvent event) {
		AbstractPeer srcNode = this.network.getNode(event.srcId);
		
		if (srcNode == null)
			return false;
			
		if (event.data.type.equalsIgnoreCase("LEAVE_GRANTED")) {
			this.network.remove(srcNode.getId());
			this.overlay.remove(srcNode.getId());
		}

		return true;
	}
	
//----------------------------------------------------------------------------------
	private boolean handlePeriodicEvent(FutureEvent event) {
		return true;
	}

//----------------------------------------------------------------------------------
	private boolean handleJoinEvent(Class<?> nodeType, Class<?> linkType) {
		try {
			AbstractPeer node = (AbstractPeer)nodeType.newInstance();
			AbstractLink link = (AbstractLink)linkType.newInstance();

			int linkLatency = Distribution.uniform(SicsimConfig.LINK_LATENCY);
			NodeId nodeId = this.network.generateUniqeNodeId();

			node.init(nodeId, link, this.bandwidth, this.failureDetector, this.overlay, this.monitor);
			link.init(node.getId(), linkLatency, this, this.network, this.coreNet);

			if (this.network.size() == 0)
				node.create(this.scheduler.getCurrentClock());
			else
				node.join(this.scheduler.getCurrentClock());			

			this.network.add(node.getId(), node);
			
			if (SicsimConfig.LOG_SIM && SicsimConfig.LOG_LEVEL >= SicsimConfig.LOG_NOTICE)
				System.out.println("SICSSIM => " + node.getId() + " has joined ---> time: " + this.scheduler.getCurrentClock());
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return false;
		}
		
		return true;
	}
	
//----------------------------------------------------------------------------------
	private boolean handleLeaveEvent() {
		if (this.network.size() == 0) {
			if (SicsimConfig.LOG_SIM && SicsimConfig.LOG_LEVEL >= SicsimConfig.LOG_WARNING)
				System.out.println("SICSSIM => Trying to leave a node from an empty network ---> time: " + this.scheduler.getCurrentClock());
			return false;
		}

		NodeId nodeId = this.network.getRandomNodeIdFromNetwork();
		if (nodeId.id > 0 || nodeId.ip > 0) {		
			AbstractPeer node = this.network.getNode(nodeId);
			node.leave(this.scheduler.getCurrentClock());
			if (SicsimConfig.LOG_SIM && SicsimConfig.LOG_LEVEL >= SicsimConfig.LOG_NOTICE)
				System.out.println("SICSSIM => " + nodeId + " wants to leave ---> time: " + this.scheduler.getCurrentClock());
		}

		return true;
	}
	
//----------------------------------------------------------------------------------
	private boolean handleFailureEvent() {
		if (this.network.size() == 0) {
			if (SicsimConfig.LOG_SIM && SicsimConfig.LOG_LEVEL >= SicsimConfig.LOG_WARNING)
				System.out.println("SICSSIM => Trying to fail a node from an empty network ---> time: " + this.scheduler.getCurrentClock());
			return false;
		}
			
		NodeId failedId = this.network.getRandomNodeIdFromNetwork();
		
		if (failedId.id > 0 || failedId.ip > 0) {		
			Vector<String> registerdNodes = this.failureDetector.gerRegisterdNodes(failedId);
			if (registerdNodes != null) {
				Enumeration<String> nodeList = registerdNodes.elements();
				while (nodeList.hasMoreElements()) {
					NodeId registerNode = new NodeId(nodeList.nextElement());
					this.addEvent(SicsimConfig.FAILURE_DETECTOR_LATENCY + this.failureRandomTime.nextInt(SicsimConfig.FAILURE_DETECTOR_LATENCY), new NodeId(SicsimConfig.SICSSIM_NODE), registerNode, new String("FAILURE_DETECTION"), new Message(new String("NULL"), failedId.toString()));
				}
			}
			
			this.network.remove(failedId);
			this.overlay.remove(failedId);
			
			if (SicsimConfig.LOG_SIM && SicsimConfig.LOG_LEVEL >= SicsimConfig.LOG_NOTICE)
				System.out.println("SICSSIM => " + failedId + " is failed ---> time: " + this.scheduler.getCurrentClock());
		}
		
		return true;
	}
	
//----------------------------------------------------------------------------------
	private void handleDelayEvent() {
	}

//----------------------------------------------------------------------------------
	private void handleFailureDetectionEvent(FutureEvent currentEvent) {
		NodeId destId = currentEvent.destId;
		NodeId failedId = new NodeId(currentEvent.data.data);
		
		if (this.network.getNode(destId) != null)
			this.network.getNode(destId).failure(failedId, this.scheduler.getCurrentClock());
	}

//----------------------------------------------------------------------------------
	private void handleSaveOverlayEvent() {
		this.scheduler.saveState(currentTime);
		this.futureEventList.saveState(this.currentTime);
		this.bandwidth.saveState(this.currentTime);
		this.failureDetector.saveState(this.currentTime);
		this.overlay.saveState(this.currentTime);
		this.network.saveState(this.currentTime);
	}

//----------------------------------------------------------------------------------
	private void handleLoadOverlayEvent() {
		this.scheduler.loadState();
		this.futureEventList.loadState();
		this.bandwidth.loadState();
		this.failureDetector.loadState();
		this.overlay.loadState();
		this.network.loadState(this.failureDetector, this.overlay, this.monitor);
	}

//----------------------------------------------------------------------------------
	private void handleLoadMonitorEvent(Class<?> monitor) {
		try {
			this.monitor = (Monitor)monitor.newInstance();
			this.monitor.init(this.network, this.overlay, this.bandwidth);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

//----------------------------------------------------------------------------------
	private boolean handleSignalEvent(int signal) {
		if (this.network.size() == 0) {
			if (SicsimConfig.LOG_SIM && SicsimConfig.LOG_LEVEL >= SicsimConfig.LOG_WARNING)
				System.out.println("SICSSIM => Trying to send a signal to a node from an empty network ---> time: " + this.scheduler.getCurrentClock());
			return false;
		}
			
		NodeId nodeId = this.network.getRandomNodeIdFromNetwork();
		
		if (nodeId.id > 0 || nodeId.ip > 0) {		
			AbstractPeer node = this.network.getNode(nodeId);
			node.signal(signal, this.scheduler.getCurrentClock());
		}
		
			
		if (SicsimConfig.LOG_SIM && SicsimConfig.LOG_LEVEL >= SicsimConfig.LOG_NOTICE)
			System.out.println("SICSSIM => send signal " + signal + " to " + nodeId + " ---> time: " + this.scheduler.getCurrentClock());
		
		return true;
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Add an event
	 * @param time Time at which the event should occur.
	 * @param srcId Source of the event.
	 * @param destId Destination of the event.
	 * @param type Type of the event.
	 * @param msg Message attached with the event.
	 */
	public void addEvent(long time, NodeId srcId, NodeId destId, String type, Message msg) {
		
		if (!srcId.equals(SicsimConfig.SICSSIM_NODE) && !destId.equals(SicsimConfig.SICSSIM_NODE)) {
			if (msg.type.equalsIgnoreCase("START_RECV_DATA"))
				this.bandwidth.useUploadBandwidth(srcId, destId, SicsimConfig.STRIPE_RATE);
			else if (msg.type.equalsIgnoreCase("STOP_RECV_DATA"))
				this.bandwidth.releaseUploadBandwidth(srcId, destId, SicsimConfig.STRIPE_RATE);
		}
		
		this.futureEventList.addEvent(new FutureEvent(this.scheduler.getCurrentClock() + time, srcId, destId, type, msg));
	}
	
//----------------------------------------------------------------------------------
	private void snapshot() {
		if (this.scheduler.getCurrentClock() != this.currentTime) {
			this.localTime++;
			this.currentTime = this.scheduler.getCurrentClock();
			if (this.localTime == SicsimConfig.SNAPSHOT_PERIOD) {
				this.localTime = 0;					
				if (SicsimConfig.LOG_SIM && SicsimConfig.LOG_LEVEL >= SicsimConfig.LOG_NOTICE)
					System.out.println("SICSSIM => snapshot ---> time: " + this.scheduler.getCurrentClock());
				if (this.monitor != null)
					this.monitor.snapshot(this.scheduler.getCurrentClock());
			}
			
			if (this.network.size() != this.lastNetSize) {
				this.lastNetSize = this.network.size();
				FileIO.append("(" + (this.currentTime / 100) + ") " + this.network.size() + "\n", SicsimConfig.NET_SIZE_FILE);
			}
		}
	}

//----------------------------------------------------------------------------------
	/**
	 * Gets the current clock of simulator.
	 * @return The current clock of simulator. 
	 */
	public long getCurrentClock() {
		return this.scheduler.getCurrentClock();
	}

}

