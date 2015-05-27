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
import sicsim.core.scheduler.SicSim;
import sicsim.network.core.CoreNet;
import sicsim.network.core.Network;
import sicsim.types.Message;
import sicsim.types.NodeId;

/**
 * The communication interface tied to the simulator for sending messages between peers.
 */
public abstract class AbstractLink {
	protected NodeId nodeId;
	protected SicSim sim;
	protected Network network;
	protected CoreNet coreNet;
	protected int linkLatency;
	
//----------------------------------------------------------------------------------
	/**
	 * Initialize the link.
	 */
	public void init(NodeId nodeId, int linkLatency, SicSim sim, Network network, CoreNet coreNet) {
		this.nodeId = nodeId;
		this.sim = sim;
		this.network = network;
		this.coreNet = coreNet;
		this.linkLatency = linkLatency;
	}

//----------------------------------------------------------------------------------	
	/**
	 * An abstract method that sends a message to a peer.
	 * @param destId The destination peer.
	 * @param msg The message to be sent to the destination peer.
	 */
	public abstract void send(NodeId destId, Message msg);

//----------------------------------------------------------------------------------
	/**
	 * Send a message to itself.
	 * @param msg The message to be sent to the destination peer.
	 * @param time The delay that this message will be received by the peer.
	 */
	public void loopback(Message msg, long time) {
		this.sim.addEvent(time, this.nodeId, this.nodeId, new String("MSG"), msg);
	}

//----------------------------------------------------------------------------------
	/**
	 * Send a message to the simulator.
	 * @param msg The message to be sent to the destination peer.
	 */
	public void sendSim(Message msg) {
		this.sim.addEvent(0, this.nodeId, new NodeId(SicsimConfig.SICSSIM_NODE), new String("SIM_MSG"), msg);
	}

//----------------------------------------------------------------------------------	
	/**
	 * Gets the latency of this link.
	 * @return The latency of this link.
	 */
	public int getLinkLatency() {
		return this.linkLatency;
	}
}
