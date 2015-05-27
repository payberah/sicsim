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

import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import sicsim.config.SicsimConfig;
import sicsim.types.NodeId;
import sicsim.utils.FileIO;
import sicsim.utils.PatternMatching;

/**
 * This class provides a platform that contains all the peers have joined the overlay network.
 */
public class OverlayNetwork {
	private Network network;
	private Vector<String> overlay = new Vector<String>();
	private Random networkRand = new Random(SicsimConfig.NETWORK_SEED);
	
//----------------------------------------------------------------------------------
	public OverlayNetwork(Network network) {
		this.network = network;
	}	

//----------------------------------------------------------------------------------
	/**
	 * Adds the peer with specified node id into the overlay network.
	 * @param nodeId Specifies the node id of the peer.
 	 */
	public void add(NodeId nodeId) {
		if (!this.overlay.contains(nodeId.toString()))
			this.overlay.addElement(nodeId.toString());
	}

//----------------------------------------------------------------------------------
	/**
	 * Removes the peer with specified node id from the overlay network.
	 * @param nodeId Specifies the node id of the peer.
 	 */
	public void remove(NodeId nodeId) {
		if (this.overlay.contains(nodeId.toString()))
			this.overlay.removeElement(nodeId.toString());
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Returns the availability of the peer in the overlay network.
	 * @param nodeId Specifies the node id of the peer.
	 * @return 'true' if the peer with node id of 'nodeId' is available in the overlay network or 'false' if it is not.		
 	 */
	public boolean contains(NodeId nodeId) {
		return this.overlay.contains(nodeId.toString());
	}

//----------------------------------------------------------------------------------
	/**
	 * Returns the list of node ids of pees in the overlay network.
	 * @return List of peers' node id in the network.		
 	 */
	public Vector<String> getNodes() {
		return this.overlay;
	}

//----------------------------------------------------------------------------------
	/**
	 * Returns the number of peers in the overlay network.
	 * @return Number of peers in the network.		
 	 */
	public int size() {
		return this.overlay.size();
	}

//----------------------------------------------------------------------------------
	/**
	 * Returns a random node id from the existing peers in the overlay network whose node id is different from 'nodeId'.
	 * @return A random nodeId from the overlay network whose node id is different frm 'nodeId'.
 	 */
    public NodeId getRandomNodeIdFromNetwork(NodeId nodeId) {
		int count = 0;
		String randomNode = new String();
		Iterator<String> netIter;
		
		if (this.overlay.size() <= 1)
			return null;

		netIter = this.overlay.iterator();
		while (netIter.hasNext()) {
			randomNode = netIter.next();
			if (!this.network.contains(new NodeId(randomNode)))
				this.overlay.removeElement(randomNode);
		}

		while (true) {
			count = 0;
			netIter = this.overlay.iterator();
			int randomIndex = this.networkRand.nextInt(this.overlay.size());
			while (netIter.hasNext() && count <= randomIndex) {
				randomNode = netIter.next();
				count++;
			}
			
			if (!nodeId.equals(randomNode))
				break;
		}
		
		return new NodeId(randomNode);

    }

//----------------------------------------------------------------------------------
	/**
	 * Returns a random node id from the existing peers in the overlay network.
	 * @return A random nodeId from the overlay network.
 	 */
    public NodeId getRandomNodeIdFromNetwork() {
		int count = 0;
		String randomNode = new String();
		Iterator<String> netIter;
		
		if (this.overlay.size() < 1)
			return null;

		netIter = this.overlay.iterator();
		while (netIter.hasNext()) {
			randomNode = netIter.next();
			if (!this.network.contains(new NodeId(randomNode)))
				this.overlay.removeElement(randomNode);
		}

		netIter = this.overlay.iterator();
		int randomIndex = this.networkRand.nextInt(this.overlay.size());
		while (netIter.hasNext() && count <= randomIndex) {
			randomNode = netIter.next();
			count++;
		}
			
		return new NodeId(randomNode);

    }
    
  //----------------------------------------------------------------------------------
	public String toString() {
		String node;
		String str = new String();
		
		Iterator<String> nodeIter = this.overlay.iterator();
		while (nodeIter.hasNext()) {
			node = nodeIter.next();
			str += (PatternMatching.subSplitter + "node: " + node + "\n");
		}

		return str;
		
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Save the state of the overlay network in a file. The file name is specified in SicsSimConfig.OVERLAY_FILE.
	 * @see SicsimConfig
 	 */
	public void saveState(long currentTime) {
		FileIO.write(this.toString(), SicsimConfig.OVERLAY_FILE);		
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Load the state of the overlay network from a file. The file name is specified in SicsSimConfig.OVERLAY_FILE.
	 * @see SicsimConfig
 	 */
	public void loadState() {
		String node;
			
		String str = FileIO.read(SicsimConfig.OVERLAY_FILE);
		String[] parts = str.split(PatternMatching.subSplitter);
			
		for (int i = 1; i < parts.length; i++) {
			node = PatternMatching.getNodeValue(parts[i], "node:").toString();
			this.overlay.add(node);
		}

		if (SicsimConfig.LOG_SIM && SicsimConfig.LOG_LEVEL >= SicsimConfig.LOG_DEBUG)
			System.out.println("OVERLAY => " + this.overlay);

	}
}
