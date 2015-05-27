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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import sicsim.config.SicsimConfig;
import sicsim.core.scheduler.SicSim;
import sicsim.types.Message;
import sicsim.types.NodeId;
import sicsim.utils.FileIO;
import sicsim.utils.PatternMatching;

/**
 * This class prepare a failure detector that helps peer to detect the failure of other peers in the overlay.
 */
public class FailureDetector {
	private Network network;
	private SicSim sim;
	private HashMap<String, Vector<String>> registerdList = new HashMap<String, Vector<String>>();
	private Random failureRandomTime = new Random(SicsimConfig.FAILURE_DETECTION_SEED);

//----------------------------------------------------------------------------------
	public FailureDetector(SicSim sim, Network network) {
		this.sim = sim;
		this.network = network;
	}
	
//----------------------------------------------------------------------------------
	/**
	 * The peers register the peers of interest in failure detector by calling this method.
	 * @param nodeId The node id of peer of interest.
	 * @param requesterId The node id of peer who wants to register for the peer of interest.
 	 */
	public void register(NodeId nodeId, NodeId requesterId) {
		Vector<String> currentList;
		
		if (!this.network.contains(nodeId)) {
			this.sim.addEvent(SicsimConfig.FAILURE_DETECTOR_LATENCY + this.failureRandomTime.nextInt(SicsimConfig.FAILURE_DETECTOR_LATENCY), new NodeId(SicsimConfig.SICSSIM_NODE), requesterId, new String("FAILURE_DETECTION"), new Message(new String("NULL"), nodeId.toString()));
		} else {
			if (!this.registerdList.containsKey(nodeId.toString())) {
				currentList = new Vector<String>();
				currentList.addElement(requesterId.toString());
			} else {
				currentList = this.registerdList.get(nodeId.toString());
				if (!currentList.contains(requesterId.toString()))
					currentList.addElement(requesterId.toString());			
			}
		
			this.registerdList.put(nodeId.toString(), currentList);
		}
	}
	
//----------------------------------------------------------------------------------
	/**
	 * The peers unregister current registered peers in failure detector by calling this method.
	 * @param nodeId The node id of peer of interest.
	 * @param requesterId The node id of peer who wants to register for the peer of interest.
 	 */
	public void unregister(NodeId nodeId, NodeId requesterId) {
		Vector<String> currentList;
		
		if (this.registerdList.containsKey(nodeId.toString())) {
			currentList = this.registerdList.get(nodeId.toString());
			currentList.removeElement(requesterId.toString());
			
			if (currentList.size() > 0)			
				this.registerdList.put(nodeId.toString(), currentList);
			else
				this.registerdList.remove(nodeId.toString());
		}
	}

//----------------------------------------------------------------------------------
	/**
	 * Returns the list of peers who registerd for one peer.
	 * @param nodeId The node id of registered peer.
	 * @return List of peers who are registered for 'nodeId'.
 	 */
	public Vector<String> gerRegisterdNodes(NodeId nodeId) {			
		if (this.registerdList.containsKey(nodeId.toString()))
			return this.registerdList.get(nodeId.toString());
			
		return null;
	}
	
//----------------------------------------------------------------------------------
	public String toString() {
		String baseNode, node;
		String str = new String();
		Vector<String> registeredNodeList;
		
		Iterator<String> nodeIter = this.registerdList.keySet().iterator();
		while (nodeIter.hasNext()) {
			baseNode = nodeIter.next();
			str += (PatternMatching.subSplitter + "base_node: " + baseNode + "\n");			
			registeredNodeList = this.registerdList.get(baseNode);
			Iterator<String> nodeListIter = registeredNodeList.iterator();
			while (nodeListIter.hasNext()) {
				node = nodeListIter.next();
				str += (PatternMatching.subSplitter + "node: " + node + "\n");
			}
			
			str += (PatternMatching.splitter + "\n");
		}
	
		return str;
	}

//----------------------------------------------------------------------------------
	/**
	 * Save the state of the failure detector in a file. The file name is specified in SicsSimConfig.FAILURE_DETECTOR_FILE.
	 * @see SicsimConfig
 	 */
	public void saveState(long currentTime) {
		FileIO.write(this.toString(), SicsimConfig.FAILURE_DETECTOR_FILE);
	}

//----------------------------------------------------------------------------------
	/**
	 * Load the state of the failure detector from file. The file name is specified in SicsSimConfig.FAILURE_DETECTOR_FILE.
	 * @see SicsimConfig
 	 */
	public void loadState() {
		String node;
		String baseNode = null;
			
		String str = FileIO.read(SicsimConfig.FAILURE_DETECTOR_FILE);
		String[] parts = str.split(PatternMatching.splitter);

		for (int i = 0; i < parts.length - 1; i++) {
			Vector<String> registeredNodeList = new Vector<String>();
			String[] subParts = parts[i].split(PatternMatching.subSplitter);
			
			baseNode = PatternMatching.getNodeValue(parts[i], "base_node:").toString();

			for (int j = 2; j < subParts.length; j++) {
				node = PatternMatching.getNodeValue(subParts[j], "node:").toString();
				registeredNodeList.addElement(node);
			   }
			    
			this.registerdList.put(baseNode, registeredNodeList);
		}

		if (SicsimConfig.LOG_SIM && SicsimConfig.LOG_LEVEL >= SicsimConfig.LOG_DEBUG)
			System.out.println("BANDWIDTH => " + this.registerdList);

	}

	
}
