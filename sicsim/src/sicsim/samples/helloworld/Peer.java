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
import java.util.Vector;

import sicsim.network.core.Bandwidth;
import sicsim.network.core.Monitor;
import sicsim.network.core.OverlayNetwork;
import sicsim.network.core.FailureDetector;
import sicsim.network.links.AbstractLink;
import sicsim.network.links.ReliableLink;
import sicsim.network.peers.AbstractPeer;
import sicsim.network.peers.PeerEventListener;
import sicsim.types.Message;
import sicsim.types.NodeId;
import sicsim.utils.PatternMatching;

public class Peer extends AbstractPeer {
	private static int PERIOD_INTERVAL = 100;
	Vector<String> friends = new Vector<String>();
	Vector<String> failedFriends = new Vector<String>();

//----------------------------------------------------------------------------------
	public void init(NodeId nodeId, AbstractLink link, Bandwidth bandwidth, FailureDetector failureDetector, OverlayNetwork overlay, Monitor monitor) {
		super.init(nodeId, link, bandwidth, failureDetector, overlay, monitor);
	}

//----------------------------------------------------------------------------------
	public void create(long currentTime) {
		System.out.println(this.nodeId + " is created!");
		this.overlay.add(this.nodeId);
		this.loopback(new Message("PERIODIC", null), Peer.PERIOD_INTERVAL);
	}
	
//----------------------------------------------------------------------------------
	public void join(long currentTime) {
		System.out.println(this.nodeId + " joins the system!");
		NodeId randomNode = this.overlay.getRandomNodeIdFromNetwork();

		if (randomNode != null)
			this.sendMsg(randomNode, new Message("HELLO", "Hello from " + this.nodeId.toString()));
		
		this.loopback(new Message("PERIODIC", null), Peer.PERIOD_INTERVAL);
		this.overlay.add(this.nodeId);
	}	
	
//----------------------------------------------------------------------------------
	public void leave(long currentTime) {
		System.out.println(this.nodeId + " wants to leave the system.");
		this.broadcast(new Message("LEAVE", null));
		this.sendSim(new Message("LEAVE_GRANTED", null));
	}

//----------------------------------------------------------------------------------
	public void failure(NodeId failedId, long currentTime) {
		System.out.println(this.nodeId + " detects failure of node " + failedId + " at time " + currentTime);
		if (!this.failedFriends.contains(failedId.toString()))
			this.failedFriends.addElement(failedId.toString());
	}
	
//----------------------------------------------------------------------------------
	public void receive(NodeId srcId, Message data, long currentTime) {
		if (this.listeners.containsKey(data.type))
			this.listeners.get(data.type).receivedEvent(srcId, data);
		else
			System.out.println("PEER: I'm node " + this.nodeId + " and event " + data.type + " is not registered!");
	}
	
//----------------------------------------------------------------------------------
	public void signal(int signal, long currentTime) {
		String data1 = new String("Signal 1 from " + this.nodeId);
		String data2 = new String("Signal 2 from " + this.nodeId);
		NodeId randomNode = this.overlay.getRandomNodeIdFromNetwork(this.nodeId);
		
		if (randomNode == null)
			return;
		
		switch (signal) {
		case 1:
			this.sendMsg(randomNode, new Message("SIGNAL", data1));
			break;
		case 2:
			this.sendMsg(randomNode, new Message("SIGNAL", data2));
			break;
		default:
			System.out.println("unknown signal number");				
		}
	}

//----------------------------------------------------------------------------------
	private void handlePeriodicEvent() {
		NodeId randomNode = this.overlay.getRandomNodeIdFromNetwork(this.nodeId);	
		if (randomNode != null)
			this.sendMsg(randomNode, new Message("HELLO", "Hello from " + this.nodeId.toString()));
		this.loopback(new Message("PERIODIC", null), Peer.PERIOD_INTERVAL);
	}

//----------------------------------------------------------------------------------
	private void handleHelloEvent(NodeId srcId, Message msg) {
		System.out.println(this.nodeId + " receives message: " + msg.data);
		if (!this.friends.contains(srcId.toString())) {
			this.friends.addElement(srcId.toString());
			this.failureDetector.register(srcId, this.nodeId);
		}
	}

//----------------------------------------------------------------------------------
	private void handleSignalEvent(NodeId srcId, Message msg) {
		System.out.println(this.nodeId + " receives signal: " + msg.data);
	}

//----------------------------------------------------------------------------------
	private void handleLeaveEvent(NodeId srcId) {
		if (this.friends.contains(srcId.toString())) {
			this.friends.removeElement(srcId.toString());
			this.failureDetector.unregister(srcId, this.nodeId);
			System.out.println(this.nodeId + " removes " + srcId + " from its friends list.");
		}
	}
	

//----------------------------------------------------------------------------------
	public void registerEvents() {
		this.addEventListener("PERIODIC", new PeerEventListener() {
			public void receivedEvent(NodeId srcId, Message msg) {
				handlePeriodicEvent();
			}
		});
		
		this.addEventListener("HELLO", new PeerEventListener() {
			public void receivedEvent(NodeId srcId, Message msg) {
				handleHelloEvent(srcId, msg);
			}
		});

		this.addEventListener("SIGNAL", new PeerEventListener() {
			public void receivedEvent(NodeId srcId, Message msg) {
				handleSignalEvent(srcId, msg);
			}
		});

		this.addEventListener("LEAVE", new PeerEventListener() {
			public void receivedEvent(NodeId srcId, Message msg) {
				handleLeaveEvent(srcId);
			}
		});

	}

//----------------------------------------------------------------------------------
	public void syncMethod(long currentTime) {
	}
	
//----------------------------------------------------------------------------------
	public String toString() {
		String str = this.getStateString(Peer.class, ReliableLink.class);
		Iterator<String> friendsIter = this.friends.iterator();
		
		str += "friends: ";
		while (friendsIter.hasNext())
			str += friendsIter.next() + ", ";
		
		str += "\nfailed: ";

		Iterator<String> failedIter = this.failedFriends.iterator();
		while (failedIter.hasNext())
			str += failedIter.next() + ", ";

		str += "\n";
    	return str;
    }

//----------------------------------------------------------------------------------
	public void restore(String str) {
		String friendsList = PatternMatching.getStrValue(str, "friends:");
		String friendParts[] = friendsList.split(",");
		for (int i = 0; i < friendParts.length; i++)
			this.friends.addElement(friendParts[i]);
		
		String failedList = PatternMatching.getStrValue(str, "failed:");
		String failedParts[] = failedList.split(",");
		for (int i = 0; i < failedParts.length; i++)
			this.failedFriends.addElement(failedParts[i]);
	}

//----------------------------------------------------------------------------------
	public Vector<String> getFriends() {
		return this.friends;
	}

//----------------------------------------------------------------------------------
	public Vector<String> getFailedFriends() {
		return this.failedFriends;
	}

}
