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
import java.util.Vector;

import sicsim.config.SicsimConfig;
import sicsim.types.LinkRate;
import sicsim.types.NodeId;
import sicsim.utils.FileIO;
import sicsim.utils.PatternMatching;

/**
 * This class maintains the structure to store the information of upload/download bandwidth of peers and provides
 * the methods to work with this structure.  
 */
public class Bandwidth {

	private HashMap<String, HashMap<String, LinkRate>> bandwidth = new HashMap<String, HashMap<String, LinkRate>>();
	
//----------------------------------------------------------------------------------
	/**
	 * Consumes the upload bandwidth of a peer with a specific rate.
	 * @param srcId Specifies the node id of the source peer.
	 * @param destId Specifies the node id of the destination peer.
	 * @param rate Specifies the rate.
 	 */
	public void useUploadBandwidth(NodeId srcId, NodeId destId, int rate) {
		HashMap<String, LinkRate> nodeList;
		LinkRate newRate;

		if (this.bandwidth.containsKey(srcId.toString())) {
			nodeList = this.bandwidth.get(srcId.toString());
			if (nodeList.containsKey(destId.toString()))
				newRate = nodeList.get(destId.toString());
			else
				newRate = new LinkRate();
		}
		else {
			nodeList = new HashMap<String, LinkRate>();
			newRate = new LinkRate();
		}

		newRate.uploadBandwidth += rate;
		nodeList.put(destId.toString(), newRate);
		this.bandwidth.put(srcId.toString(), nodeList);
	}

//----------------------------------------------------------------------------------
	/**
	 * Consumes the download bandwidth of a peer with a specific rate.
	 * @param srcId Specifies the node id of the source peer.
	 * @param destId Specifies the node id of the destination peer.
	 * @param rate Specifies the rate.
 	 */
	public void useDownloadBandwidth(NodeId srcId, NodeId destId, int rate) {
		HashMap<String, LinkRate> nodeList;
		LinkRate newRate;

		if (this.bandwidth.containsKey(destId.toString())) {
			nodeList = this.bandwidth.get(destId.toString());
			if (nodeList.containsKey(srcId.toString()))
				newRate = nodeList.get(srcId.toString());
			else
				newRate = new LinkRate();
		}
		else {
			nodeList = new HashMap<String, LinkRate>();
			newRate = new LinkRate();
		}
		
		newRate.downloadBandwidth += rate;
		nodeList.put(srcId.toString(), newRate);
		this.bandwidth.put(destId.toString(), nodeList);
	}

//----------------------------------------------------------------------------------
	/**
	 * Releases the upload bandwidth of a peer with a specific rate.
	 * @param srcId Specifies the node id of the source peer.
	 * @param destId Specifies the node id of the destination peer.
	 * @param rate Specifies the rate.
 	 */
	public boolean releaseUploadBandwidth(NodeId srcId, NodeId destId, int rate) {
		HashMap<String, LinkRate> nodeList;
		LinkRate currentRate;

		if (this.bandwidth.containsKey(srcId.toString())) {
			nodeList = this.bandwidth.get(srcId.toString());
			if (nodeList.containsKey(destId.toString()))
				currentRate = nodeList.get(destId.toString());
			else
				return false;
		}
		else 
			return false;

		currentRate.uploadBandwidth -= rate;

		if (currentRate.downloadBandwidth == 0 && currentRate.uploadBandwidth == 0)
			nodeList.remove(destId.toString());
		else
			nodeList.put(destId.toString(), currentRate);

		this.bandwidth.put(srcId.toString(), nodeList);
		
		return true;
	}

//----------------------------------------------------------------------------------
	/**
	 * Releases the download bandwidth of a peer with a specific rate.
	 * @param srcId Specifies the node id of the source peer.
	 * @param destId Specifies the node id of the destination peer.
	 * @param rate Specifies the rate.
 	 */
	public boolean releaseDownloadBandwidth(NodeId srcId, NodeId destId, int rate) {
		HashMap<String, LinkRate> nodeList;
		LinkRate currentRate;

		// Set the destination node rate		
		if (this.bandwidth.containsKey(destId.toString())) {
			nodeList = this.bandwidth.get(destId.toString());
			if (nodeList.containsKey(srcId.toString()))
				currentRate = nodeList.get(srcId.toString());
			else
				return false;
		}
		else 
			return false;

		currentRate.downloadBandwidth -= rate;

		if (currentRate.uploadBandwidth == 0 && currentRate.downloadBandwidth == 0) 
			nodeList.remove(srcId.toString());
		else
			nodeList.put(srcId.toString(), currentRate);

			this.bandwidth.put(destId.toString(), nodeList);
		
		return true;
	}

//----------------------------------------------------------------------------------
	/**
	 * Returns the total download rate of one peer.
	 * @param nodeId Specifies the node id of the peer.
	 * @return The total download rate of peer with node id 'nodeId'.
 	 */
	public int getTotalDownloadBandwidth(NodeId nodeId) {
		HashMap<String, LinkRate> nodeList;
		int downloadRate = 0;
		
		if ((nodeList = this.bandwidth.get(nodeId.toString())) == null)
			return 0;
		
		Iterator<String> nodeListIter = nodeList.keySet().iterator();
		while (nodeListIter.hasNext())
			downloadRate += nodeList.get(nodeListIter.next()).downloadBandwidth;

		return downloadRate;
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Returns the total upload rate of one peer.
	 * @param nodeId Specifies the node id of the peer.
	 * @return The total upload rate of peer with node id 'nodeId'.
 	 */
	public int getTotalUploadBandwidth(NodeId nodeId) {
		HashMap<String, LinkRate> nodeList;
		int uploadRate = 0;
		
		if ((nodeList = this.bandwidth.get(nodeId.toString())) == null)
			return 0;

		Iterator<String> nodeListIter = nodeList.keySet().iterator();
		while (nodeListIter.hasNext())
			uploadRate += nodeList.get(nodeListIter.next()).uploadBandwidth;

		return uploadRate;
	}


//----------------------------------------------------------------------------------
	/**
	 * Returns the upload rate between two peers.
	 * @param srcId Specifies the node id of the source peer.
	 * @param destId Specifies the node id of the destination peer.
 	 */
	public int getCurrentUploadRate(NodeId srcId, NodeId destId) {
		try {
			return this.bandwidth.get(srcId.toString()).get(destId.toString()).uploadBandwidth;
		}
		catch(Exception e) {
			return 0;
		}
	}

//----------------------------------------------------------------------------------
	/**
	 * Removes the information of on peer from bandwidth structure.
	 * @param nodeId Specifies the node id of the peer.
 	 */

	public void remove(NodeId nodeId) {
		String node;
		HashMap<String, LinkRate> nodeList = new HashMap<String, LinkRate>();
		
		if (this.bandwidth.containsKey(nodeId.toString()))
			this.bandwidth.remove(nodeId.toString());

		Iterator<String> bandwidthIter = this.bandwidth.keySet().iterator();
		while (bandwidthIter.hasNext()) {
			node = bandwidthIter.next();
			nodeList = this.bandwidth.get(node);
			if (nodeList.containsKey(nodeId.toString())) {
				nodeList.remove(nodeId.toString());
				this.bandwidth.put(node, nodeList);
			}
		}
					
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Returns list of peers that a peer is uploading to them.
	 * @param nodeId Specifies the node id of the peer.
	 * @return List of peers that the peer with node id 'nodeId' is uploading to them.		
 	 */
	public Vector<String> getListOfUploads(NodeId nodeId) {
		if (!this.bandwidth.containsKey(nodeId.toString()) || this.bandwidth.get(nodeId.toString()).size() == 0)
			return null;
		
		String node;
		Vector<String> listOfUploads = new Vector<String>();
		HashMap<String, LinkRate> nodeList = this.bandwidth.get(nodeId.toString());
		Iterator<String> nodeListIter = nodeList.keySet().iterator();
		while (nodeListIter.hasNext()) {
			node = nodeListIter.next();
			if (nodeList.get(node).uploadBandwidth > 0)
				listOfUploads.add(node);
		}
		
		return ((listOfUploads.size() != 0) ? listOfUploads : null);
	}

//----------------------------------------------------------------------------------
	/**
	 * Returns list of peers that a peer are downloading from them.
	 * @param nodeId Specifies the node id of the peer.
	 * @return List of peers that the peer with node id 'nodeId' are downloading from them.		
 	 */
	public Vector<String> getListOfDownloads(NodeId nodeId) {
		if (!this.bandwidth.containsKey(nodeId.toString()) || this.bandwidth.get(nodeId.toString()).size() == 0)
			return null;
		
		String node;
		Vector<String> listOfDownloads = new Vector<String>();
		HashMap<String, LinkRate> nodeList = this.bandwidth.get(nodeId.toString());
		Iterator<String> nodeListIter = nodeList.keySet().iterator();
		while (nodeListIter.hasNext()) {
			node = nodeListIter.next();
			if (nodeList.get(node).downloadBandwidth > 0)
				listOfDownloads.add(node);
		}
		
		return ((listOfDownloads.size() != 0) ? listOfDownloads : null);
	}
	
//----------------------------------------------------------------------------------
	public String toString() {
		String baseNode, node;
		String str = new String();
		HashMap<String, LinkRate> nodeList = new HashMap<String, LinkRate>();
		
		Iterator<String> bandwidthIter = this.bandwidth.keySet().iterator();
		while (bandwidthIter.hasNext()) {
			baseNode = bandwidthIter.next();
			str += (PatternMatching.subSplitter + "base_node: " + baseNode + "\n");			
			nodeList = this.bandwidth.get(baseNode);
			Iterator<String> nodeListIter = nodeList.keySet().iterator();
			while (nodeListIter.hasNext()) {
				node = nodeListIter.next();
				str += (PatternMatching.subSplitter + "node: " + node + " => BW: (" + nodeList.get(node).toString() + ")\n");
			}
			
			str += (PatternMatching.splitter + "\n");
		}
	
		return str;
	}

//----------------------------------------------------------------------------------
	/**
	 * Save the state of the bandwidth of peers in a file. The file name is specified in SicsSimConfig.BW_FILE.
	 * @see SicsimConfig
 	 */
	public void saveState(long currentTime) {
		FileIO.write(this.toString(), SicsimConfig.BW_FILE);
	}

//----------------------------------------------------------------------------------
	/**
	 * Load the state of the bandwidth of peers from a file. The file name is specified in SicsSimConfig.BW_FILE.
	 * @see SicsimConfig
 	 */
	public void loadState() {
		String node;
		String baseNode = null;
		LinkRate rate = new LinkRate();
			
		String str = FileIO.read(SicsimConfig.BW_FILE);
		String[] parts = str.split(PatternMatching.splitter);

		for (int i = 0; i < parts.length - 1; i++) {
			HashMap<String , LinkRate> linkRate = new HashMap<String, LinkRate>();
			String[] subParts = parts[i].split(PatternMatching.subSplitter);
			
			baseNode = PatternMatching.getNodeValue(parts[i], "base_node:").toString();

			for (int j = 2; j < subParts.length; j++) {
				node = PatternMatching.getNodeValue(subParts[j], "node:").toString();
				rate = LinkRate.restore(PatternMatching.getStrValue(subParts[j], "BW:"));
				linkRate.put(node, rate);
			   }
			    
			this.bandwidth.put(baseNode, linkRate);
		}

		if (SicsimConfig.LOG_SIM && SicsimConfig.LOG_LEVEL >= SicsimConfig.LOG_DEBUG)
			System.out.println("BANDWIDTH => " + this.bandwidth);

	}
}
