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

import sicsim.config.SicsimConfig;
import sicsim.network.core.Monitor;
import sicsim.network.core.OverlayNetwork;
import sicsim.network.core.Bandwidth;
import sicsim.network.core.FailureDetector;
import sicsim.network.links.AbstractLink;
import sicsim.types.Message;
import sicsim.types.NodeId;
import sicsim.utils.Distribution;
import sicsim.utils.PatternMatching;

/**
 * Peers simulated in the system have to implement this abstract class, if they want
 * to have upload/download bandwidth in their links.
 */
public abstract class BandwidthPeer extends AbstractPeer {
	/**
	 * Download bandwidth.
	 */
	protected int downloadBandwidth;

	/**
	 * Upload bandwidth.
	 */
	protected int uploadBandwidth;
	
//----------------------------------------------------------------------------------
	/**
	 * Initialize the peer.
	 */
	public void init(NodeId nodeId, AbstractLink link, Bandwidth bandwidth, FailureDetector failureDetector, OverlayNetwork overlay, Monitor monitor) {
		super.init(nodeId, link, bandwidth, failureDetector, overlay, monitor);
		this.uploadBandwidth = Distribution.sripanidkulachi();
		this.downloadBandwidth = SicsimConfig.NUM_OF_STRIPES * SicsimConfig.STRIPE_RATE;
	}

//----------------------------------------------------------------------------------
	/**
	 * Initialize the peer with specific bandwidth
	 */
	public void init(NodeId nodeId, AbstractLink link, Bandwidth bandwidth, FailureDetector failureDetector, OverlayNetwork overlay, Monitor monitor, int uploadBandwidth, int downloadBandwidth) {
		super.init(nodeId, link, bandwidth, failureDetector, overlay, monitor);
		this.uploadBandwidth = uploadBandwidth;
		this.downloadBandwidth = downloadBandwidth;
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Sets the upload bandwidth of this peer.
	 * @param uploadBandwidth Upload bandwidth.
	 */
	public void setUploadBandwidth(int uploadBandwidth) {
		this.uploadBandwidth = uploadBandwidth;
	}

//----------------------------------------------------------------------------------
	/**
	 * Sets the download bandwidth of this peer.
	 * @param downloadBandwidth Download bandwidth.
	 */
	public void setDownloadBandwidth(int downloadBandwidth) {
		this.downloadBandwidth = downloadBandwidth;
	}

//----------------------------------------------------------------------------------
	/**
	 * Gets the upload bandwidth of this peer.
	 * @return The upload bandwidth of this peer.
	 */
	public int getUploadBandwidth() {
		return this.uploadBandwidth;
	}

//----------------------------------------------------------------------------------
	/**
	 * Gets the download bandwidth of this peer.
	 * @return The download bandwidth of this peer.
	 */
	public int getDownloadBandwidth() {
		return this.downloadBandwidth;
	}

//----------------------------------------------------------------------------------
	/**
	 * Gets the available upload bandwidth of this peer.
	 * @return The available upload bandwidth of this peer.
	 */
	public int getAvailableUploadBandwidth() {
		return this.uploadBandwidth - this.bandwidth.getTotalUploadBandwidth(this.nodeId);
	}

//----------------------------------------------------------------------------------
	/**
	 * Gets the available download bandwidth of this peer.
	 * @return The available download bandwidth of this peer.
	 */
	public int getAvailableDownloadBandwidth() {
		return this.downloadBandwidth - this.bandwidth.getTotalDownloadBandwidth(this.nodeId);
	}

//----------------------------------------------------------------------------------
	/**
	 * Gets the upload rate from one peer to another peer.
	 * @param destId The destination peer.
	 * @return The upload rate from this peer to the 'destId'.
	 */
	public int getUploadBandwidthTo(NodeId destId) {
		return this.bandwidth.getCurrentUploadRate(this.nodeId, destId);
	}

//----------------------------------------------------------------------------------
	/**
	 * Starts to send data from this peer to the destination. 
	 * @param destId The destination peer.
	 * @param msg The message to the destination peer.
	 * @return 'true' if there is enough available upload bandwidth on this peer to 
	 * sends data, otherwise returns 'false'.
	 */
    protected boolean startSendData(NodeId destId, String msg) {
		Message controlData = new Message();
		controlData.data = msg;
		controlData.type = new String("START_RECV_DATA");
				
		if (this.bandwidth.getTotalUploadBandwidth(this.nodeId) + SicsimConfig.STRIPE_RATE <= this.uploadBandwidth) {
			this.link.send(destId, controlData);
			return true;
		}
		else {
			if (SicsimConfig.LOG_SIM && SicsimConfig.LOG_LEVEL >= SicsimConfig.LOG_NOTICE)
				System.out.println("BW_PEER => " + this.nodeId + " does not have upload bw for " + destId);			
			return false;			
		}
	}

//----------------------------------------------------------------------------------
	/**
	 * Stops to send data from this peer to the destination. 
	 * @param destId The destination peer.
	 * @param msg The message to the destination peer.
	 */
    protected void stopSendData(NodeId destId, String msg) {
		Message controlData = new Message();
		controlData.data = new String(msg);
		controlData.type = new String("STOP_RECV_DATA");

		this.link.send(destId, controlData);
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
    	str += ("upload: " + this.uploadBandwidth + "\n");
    	str += ("download: " + this.downloadBandwidth + "\n");
    	str += PatternMatching.localSplitter;

    	return str;
    }
}
