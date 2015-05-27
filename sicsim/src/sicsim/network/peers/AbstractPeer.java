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

package sicsim.network.peers;

import java.util.HashMap;
import java.util.Iterator;

import sicsim.config.SicsimConfig;
import sicsim.network.core.Bandwidth;
import sicsim.network.core.Monitor;
import sicsim.network.core.OverlayNetwork;
import sicsim.network.core.FailureDetector;
import sicsim.network.links.AbstractLink;
import sicsim.types.Message;
import sicsim.types.NodeId;
import sicsim.utils.PatternMatching;

/**
 * Peers simulated in the system have to implement this abstract class
 */
public abstract class AbstractPeer {
	/**
	 * The node id of the peer.
	 */
	protected NodeId nodeId;	

	/**
	 * An instance of the link that connects the peer to the core network.
	 */
	protected AbstractLink link;

	/**
	 * An instance of bandwidth structure that is used by 'BandwidthPeer'.
	 */
	protected Bandwidth bandwidth;

	/**
	 * An instance of failure detector. The peer can subscribes in failure detector for peers of interest.
	 */
	protected FailureDetector failureDetector;
	
	/**
	 * The overlay network. The peer can get information about other peers in the overlay network by using 'overlay'.
	 */
	protected OverlayNetwork overlay;

	/**
	 * The Monitor. The peer can use monitor to verify its behaviour.
	 */
	protected Monitor monitor;
	
	/**
	 * 	An array for listening to all event types that peer can listen to
	 */
	protected HashMap<String, PeerEventListener> listeners = new HashMap<String, PeerEventListener>();

//----------------------------------------------------------------------------------
	/**
	 * Initialize the peer.
	 */
	public void init(NodeId nodeId, AbstractLink link, Bandwidth bandwidth, FailureDetector failureDetector, OverlayNetwork overlay, Monitor monitor) {
		this.nodeId = nodeId;
		this.link = link;
		this.bandwidth = bandwidth;
		this.failureDetector = failureDetector;
		this.overlay = overlay;
		this.monitor = monitor;
	
		this.registerEvents();
	}

//----------------------------------------------------------------------------------
	/**
	 * An abstract method that called by simulator when the first peer comes to the system.
	 * @param currentTime The current time of the system.
	 */
	public abstract void create(long currentTime);

//----------------------------------------------------------------------------------
	/**
	 * An abstract method that called by simulator whenever a peer joins the system, except the first peer.
	 * @param currentTime The current time of the system.
	 */
	public abstract void join(long currentTime);

//----------------------------------------------------------------------------------
	/**
	 * An abstract method that called by simulator whenever a peer decides to leave the system.
	 * @param currentTime The current time of the system.
	 */
	public abstract void leave(long currentTime);
	
//----------------------------------------------------------------------------------
	/**
	 * Notification that a peer has failed. The peer must have already 
	 * subscribed for the failure of this peer. This is an abstract method.
	 * @param failedId The node that has failed.
	 * @param currentTime The current time of the system.
	 */
	public abstract void failure(NodeId failedId, long currentTime);
	
//----------------------------------------------------------------------------------
	/**
	 * Receive a message from another peer. Called by the simulator when a message is received
	 * for this peer. This is an abstract method.
	 * @param srcId Node id of message source.
	 * @param msg The message that source has sent.
	 * @param currentTime The current time of the system.
	 */
	public abstract void receive(NodeId srcId, Message msg, long currentTime);

//----------------------------------------------------------------------------------
	/**
	 * An abstract method that receives a signal from simulator. Called by the simulator
	 *  when a the simulator sends a signal to the peer.
	 * @param signal The signal number.
	 * @param currentTime The current time of the system.
	 */
    public abstract void signal(int signal, long currentTime);	

//----------------------------------------------------------------------------------
	/**
	 * A Synchronized method that is called in each time unit. This feature is disabled 
	 * by default, and can be enabled by setting 'true' the SicsSimConfig.SYNC_UPDATE.
	 * This is an abstract method.
	 * @param currentTime The current time of the system.
	 * @see SicsimConfig
	 */
    public abstract void syncMethod(long currentTime);	

//----------------------------------------------------------------------------------
    public abstract String toString();
    
//----------------------------------------------------------------------------------
	/**
	 * An abstract method that restores the saved status of the peer.
	 * @param status The saved status of the peer. This status should be parsed and restores
	 * the local value of the peer.
	 */
    public abstract void restore(String status);	

//----------------------------------------------------------------------------------
	/**
	 * Registers the handler for receiving messages. This is an abstract method.
	 */
    protected abstract void registerEvents();	
	
//----------------------------------------------------------------------------------
	/**
	 * Add an event listener. This method will override the previous listener
     * @param eventType Event to subscribe from.
     * @param listener The listener for event.
	 */
    protected void addEventListener(String eventType, PeerEventListener listener) {     
    	this.listeners.put(eventType, listener);
    }

//----------------------------------------------------------------------------------
    /**
     * Remove an event listener.
     * @param eventType event to unsubscribe from.
     */
    protected void removeEventListener(String eventType) {     
    	this.listeners.remove(eventType);
    }
    
//----------------------------------------------------------------------------------
	/**
	 * Sends a message to another peer.
	 * @param destId The node id of destination peer.
	 * @param msg The message that peer sends to the destination peer.
	 */
    protected void sendMsg(NodeId destId, Message msg) {
		this.link.send(destId, msg);
	}

//----------------------------------------------------------------------------------
	/**
	 * A peer can send a message to itself by calling this method.
	 * @param msg The message that peer sends to itself.
	 * @param time The delay that this message will be received by the peer.
	 */
    protected void loopback(Message msg, long time) {
		this.link.loopback(msg, time);
	}

//----------------------------------------------------------------------------------
	/**
	 * Broadcasts a message to all the peers in the overlay network, not all the peer
	 * in the system.
	 * @param msg The message that peer broadcasts.
	 */
    protected void broadcast(Message msg) {
    	NodeId node;
		Iterator<String> nodeIter = this.overlay.getNodes().iterator();
		while (nodeIter.hasNext()) {
			node = new NodeId(nodeIter.next());
			if (!node.equals(this.nodeId))
				this.link.send(node, msg);			
		}	
	}

//----------------------------------------------------------------------------------
	/**
	 * Sends a message to the simulator.
	 * @param msg The message that peer sends to simulator.
	 */
    protected void sendSim(Message msg) {
		this.link.sendSim(msg);
	}    
    
//----------------------------------------------------------------------------------
	/**
	 * Get the identifier of this peer.
	 * @return The node id of the peer.
	 */
	public NodeId getId() {
		return this.nodeId;
	}	

//----------------------------------------------------------------------------------
	/**
	 * Get the link latency of this peer.
	 * @return The link latency of this peer.
	 */
    public int getLinkLatency() {
    	return this.link.getLinkLatency();
    }
    
//----------------------------------------------------------------------------------
	/**
	 * Returns the status of the peer that should be saved. This method should be called
	 * inside the 'toString' of a peer who inheritance from this peer. The local information
	 * of child class should be added at the end of the returning string of this method.
	 * @param peer The class of peer.
	 * @param link The class of link.
	 * @return A string that contains the information of status of this peer.
	 */
    public String getStateString(Class<?> peer, Class<?> link) {
    	String str = new String();
    	
    	str += ("nodeType: " + peer.getName() + "\n");
    	str += ("linkType: " + link.getName() + "\n");
    	str += ("nodeId: " + this.nodeId + "\n");
    	str += ("linkLatency: " + this.link.getLinkLatency() + "\n");
    	str += PatternMatching.localSplitter;

    	return str;
    }
}
