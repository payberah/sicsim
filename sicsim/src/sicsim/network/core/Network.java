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

package sicsim.network.core;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import sicsim.config.SicsimConfig;
import sicsim.core.scheduler.SicSim;
import sicsim.network.links.AbstractLink;
import sicsim.network.peers.AbstractPeer;
import sicsim.network.peers.BandwidthPeer;
import sicsim.types.NodeId;
import sicsim.utils.*;

/**
 * This class provides a platform that contains all the peers in the system, regardless if they have joined the overlay or not.
 */
public class Network {
	private SicSim sim;
	private CoreNet coreNet;
	private Bandwidth bandwidth;
	private Hashtable<Integer, Integer> idIpList = new Hashtable<Integer, Integer>();
	private Hashtable<String, AbstractPeer> network = new Hashtable<String, AbstractPeer>();
	
	// Random number generator
	private RandomSet randomSet = new RandomSet(0, SicsimConfig.MAX_NODE, SicsimConfig.NETWORK_SEED);
	private RandomSet[] skewedRandomSet;
	
	// Random IP generator
	private Random randomIp = new Random(7 * SicsimConfig.NETWORK_SEED);	
	private Random networkRand = new Random(SicsimConfig.NETWORK_SEED);
	
//----------------------------------------------------------------------------------
	public Network(SicSim sim, Bandwidth bandwidth, CoreNet coreNet) {
		this.sim = sim;
		this.bandwidth = bandwidth;
		this.coreNet = coreNet;
		
		if (SicsimConfig.SKEWED) {
			this.skewedRandomSet = new RandomSet[2 * SicsimConfig.NUM_OF_CLUSTER];
			for (int i = 0; i < 2 * SicsimConfig.NUM_OF_CLUSTER; i++) {
				this.skewedRandomSet[i] = new RandomSet(i * (SicsimConfig.MAX_NODE / (2 * SicsimConfig.NUM_OF_CLUSTER)), (i + 1) * (SicsimConfig.MAX_NODE / (2 * SicsimConfig.NUM_OF_CLUSTER)), SicsimConfig.NETWORK_SEED);
			}
		}
	}

//----------------------------------------------------------------------------------
	/**
	 * Adds the peer with specified node id into the network.
	 * @param nodeId Specifies the node id of the peer.
	 * @param node An instance of AbstractPeer that specifies the peer's properties.
 	 */
	public void add(NodeId nodeId, AbstractPeer node) {
		this.network.put(nodeId.toString(), node);
		this.idIpList.put(new Integer(nodeId.id), new Integer(nodeId.ip));
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Removes the peer with specified node id from the network.
	 * @param nodeId Specifies the node id of the peer.
 	 */
	public void remove(NodeId nodeId) {
		this.network.remove(nodeId.toString());
		this.idIpList.remove(new Integer(nodeId.id));
		this.bandwidth.remove(nodeId);
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Returns the peer whose node id id 'nodeId'.
	 * @param nodeId Specifies the node id of the peer.
	 * @return The peer, or if the peer is not exist returns null.		
 	 */
	public AbstractPeer getNode(NodeId nodeId) {
		if (nodeId == null)
			return null;
		
		return this.network.get(nodeId.toString());
	}

//----------------------------------------------------------------------------------
	/**
	 * Returns the number of peers in the network.
	 * @return Number of peers in the network.		
 	 */
	public int size() {
		return this.network.size();
	}

//----------------------------------------------------------------------------------
	/**
	 * Returns the availability of the peer in the network.
	 * @param nodeId Specifies the node id of the peer.
	 * @return 'true' if the peer with node id of 'nodeId' is available in the network or 'false' if it is not.		
 	 */
	public boolean contains(NodeId nodeId) {
		return this.network.containsKey(nodeId.toString());
	}

//----------------------------------------------------------------------------------
	/**
	 * Returns the availability of the peer in the network.
	 * @param id Specifies the id of the peer.
	 * @return 'true' if the peer with id of 'id' is available in the network or 'false' if it is not.		
 	 */
	private boolean contains(int id) {
		return this.idIpList.containsKey(new Integer(id));
	}

//----------------------------------------------------------------------------------
	/**
	 * Generates a random node id which is not exist in network.
	 * @return A unique random nodeId.		
 	 */
	public NodeId generateUniqeNodeId() {
		int id;
		int ip = this.randomIp.nextInt(SicsimConfig.MAX_NODE);
		
		if (!SicsimConfig.SKEWED) {
			id = this.randomSet.addInt();
		
			while (this.contains(id)) {
				this.randomSet.undoLastAdd();
				id = this.randomSet.addInt();
			}
		} else {
			int cluster = Distribution.cluster(SicsimConfig.NUM_OF_CLUSTER, SicsimConfig.PROB_OF_CLUSTER);
			id = this.skewedRandomSet[cluster].addInt();
			
			while (this.contains(id)) {
				this.skewedRandomSet[cluster].undoLastAdd();
				id = this.skewedRandomSet[cluster].addInt();
			}
		}
		
		
		return new NodeId(id, ip);		
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Returns a random node id from the existing peers in the network.
	 * @return A random nodeId from the network.		
 	 */
    public NodeId getRandomNodeIdFromNetwork() {
		int count = 0;
		int randomIndex = this.networkRand.nextInt(this.network.size());
		Enumeration<String> netEnum = this.network.keys();

		while (netEnum.hasMoreElements() && count < randomIndex) {
			netEnum.nextElement();
			count++;
		}
		
		return this.network.get(netEnum.nextElement()).getId();
    }

//----------------------------------------------------------------------------------
	/**
	 * Returns the list of node ids of pees in the network.
	 * @return List of peers' node id in the network.		
 	 */
	public Vector<NodeId> getNodes() {

		Vector<NodeId> nodeList = new Vector<NodeId>();
		Integer id;
		Integer ip;
		
		Enumeration<Integer> idipListEnum = this.idIpList.keys();
		while(idipListEnum.hasMoreElements()) {
			id = idipListEnum.nextElement();
			ip = idIpList.get(id);
			nodeList.add(new NodeId(id, ip));
		}

		return nodeList;
	}
	
//----------------------------------------------------------------------------------
	/**
	 * This method is periodically called by simulator in each time unit if SicsSimConfig.SYNC_UPDATE is 'true'.
	 * @param currentTime Current time of simulator.
 	 */
	public void updateNetwork(long currentTime) {
		Enumeration<String> netEnum = this.network.keys();
		while (netEnum.hasMoreElements())
			this.network.get(netEnum.nextElement()).syncMethod(currentTime);
	}

//----------------------------------------------------------------------------------
	public String toString() {
		String str = new String("Network: ");
		
		Enumeration<String> netEnum = this.network.keys();
		while (netEnum.hasMoreElements())
			str += (netEnum.nextElement() + " ");
		
		return str;
	}

//----------------------------------------------------------------------------------
	/**
	 * Save the state of the network in a file. The file name is specified in SicsSimConfig.NETWORK_FILE.
	 * @see SicsimConfig
 	 */
	public void saveState(long currentTime) {
		String str = new String();
		
		Enumeration<String> netEnum = this.network.keys();
		while (netEnum.hasMoreElements()) {
			str += this.network.get(netEnum.nextElement()).toString();
			str += PatternMatching.peerSplitter + "\n";
		}
		
		FileIO.append(str, SicsimConfig.NETWORK_FILE);
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Load the state of the network from a file. The file name is specified in SicsSimConfig.NETWORK_FILE.
	 * @see SicsimConfig
 	 */
	public void loadState(FailureDetector failureDetector, OverlayNetwork overlay, Monitor monitor) {
		String nodeType;
		String linkType;
		NodeId nodeId;
		int linkLatency;
		int uploadBw;
		int downloadBw;
		String peerLocalInfo = new String();
		
		String str = FileIO.read(SicsimConfig.NETWORK_FILE);
		String[] parts = str.split(PatternMatching.peerSplitter);
		try {
			for (int i = 0; i < parts.length - 1; i++) {
				nodeType = PatternMatching.getStrValue(parts[i], "nodeType:");
				linkType = PatternMatching.getStrValue(parts[i], "linkType:");
				nodeId = PatternMatching.getNodeValue(parts[i], "nodeId:");				
				linkLatency = PatternMatching.getIntValue(parts[i], "linkLatency:");
				peerLocalInfo = parts[i].substring(parts[i].indexOf(PatternMatching.localSplitter));

				AbstractPeer node = (AbstractPeer)Class.forName(nodeType).newInstance();
				AbstractLink link = (AbstractLink)Class.forName(linkType).newInstance();
	
				if (node instanceof BandwidthPeer) {
					uploadBw = PatternMatching.getIntValue(parts[i], "upload:");
					downloadBw = PatternMatching.getIntValue(parts[i], "download:");
					((BandwidthPeer)node).init(nodeId, link, this.bandwidth, failureDetector, overlay, monitor, uploadBw, downloadBw);
					
				} else
					node.init(nodeId, link, this.bandwidth, failureDetector, overlay, monitor);
				
				link.init(nodeId, linkLatency, this.sim, this, this.coreNet);
	
				node.restore(peerLocalInfo);
				
				this.add(nodeId, node);
			}
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}
