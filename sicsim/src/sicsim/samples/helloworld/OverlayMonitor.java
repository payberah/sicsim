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

package sicsim.samples.helloworld;

import java.util.Iterator;

import sicsim.samples.helloworld.Peer;
import sicsim.network.core.Monitor;
import sicsim.types.NodeId;
import sicsim.utils.FileIO;

public class OverlayMonitor extends Monitor {
	
//----------------------------------------------------------------------------------
	public void update(long currentTime) {
		System.out.println("----------->" + currentTime);
	}

//----------------------------------------------------------------------------------
	public void verify(long currentTime) {
		NodeId node;
		Peer peer;

		System.out.println("-------------------------------");
		System.out.println(this.network.size() + " nodes are in overlay!");

		Iterator<NodeId> nodeIter = this.network.getNodes().iterator();
		while (nodeIter.hasNext()) {
			node = new NodeId(nodeIter.next());
			peer = (Peer)this.network.getNode(node);
			System.out.println(peer.getId() + ", friends: " + peer.getFriends() + ", failed_friends: " + peer.getFailedFriends());
		}
	}

//----------------------------------------------------------------------------------
	public void snapshot(long currentTime) {
		NodeId node;
		Peer peer;
		String str = new String();
		String fileName = new String("snapshot-" + currentTime);
		
		str += "time: " + currentTime + "\n\n";
		Iterator<NodeId> nodeIter = this.network.getNodes().iterator();
		while (nodeIter.hasNext()) {
			node = new NodeId(nodeIter.next());
			peer = (Peer)this.network.getNode(node);
			str += (peer.getId() + ", friends: " + peer.getFriends() + ", failed_friends: " + peer.getFailedFriends() + "\n");
		}
		
		FileIO.write(str, fileName);
	}
}
