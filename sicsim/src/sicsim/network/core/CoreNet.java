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

import sicsim.config.SicsimConfig;
import sicsim.types.NodeId;
import sicsim.utils.Distribution;

/**
 * This class represents the core network in the system. This class creates latency in transferring messages between peers.
 */
public class CoreNet {
	
//----------------------------------------------------------------------------------
	/**
	 * Return the latency between two peers in network.
	 * @param srcId The node id of first peer.
	 * @param destId The node id of second peer.
	 * @return The latency between 'srcId' and 'destId' in network.
 	 */
	public static int getNetLatency(NodeId srcId, NodeId destId) {
		return (Distribution.normal(SicsimConfig.NETWORK_LATENCY, 0.5, srcId.id + destId.id) + Distribution.uniform(SicsimConfig.NETWORK_LATENCY_DRIFT) + 1);
	}
}
