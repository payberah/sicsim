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

package sicsim.network.links;

import sicsim.config.SicsimConfig;
import sicsim.network.core.CoreNet;
import sicsim.types.Message;
import sicsim.types.NodeId;
/**
 * A reliable link that each message that transfers through this link, eventually will be
 * received by the destination peer.
 */
public class ReliableLink extends AbstractLink {
	
//----------------------------------------------------------------------------------
	/**
	 * Sends a message to a peer.
	 * @param destId The destination peer.
	 * @param msg The message to be sent to the destination peer.
	 */
	public void send(NodeId destId, Message msg) {
		this.sim.addEvent(this.totalLatency(destId), this.nodeId, destId, new String("MSG"), msg);				
	}
	
//----------------------------------------------------------------------------------	
	private int totalLatency(NodeId destId) {
		int srcLinkLatency = this.linkLatency;

		if (this.network.getNode(destId) != null) {
			int destLinkLatency = this.network.getNode(destId).getLinkLatency();
			int netLatency = CoreNet.getNetLatency(nodeId, destId);
			
			return (srcLinkLatency + destLinkLatency + netLatency);
		}
		else 
			return SicsimConfig.FAILURE_DETECTOR_MAX_TIME;
	}
}
