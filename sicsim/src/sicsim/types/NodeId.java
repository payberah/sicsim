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

package sicsim.types;

/**
 * This class represents a node in the system. It essentially contains
 * the node identifier and the ip address of the node. 
 */
public class NodeId {
	public int id;
	public int ip;

//----------------------------------------------------------------------------------
	public NodeId(int id, int ip) {
		this.id = id;
		this.ip = ip;
	}
	
//----------------------------------------------------------------------------------
	public NodeId(NodeId nodeId) {
		this.id = nodeId.id;
		this.ip = nodeId.ip;
	}

//----------------------------------------------------------------------------------
	public NodeId(String strNodeId) {
		this.id = Integer.parseInt(strNodeId.substring(0, strNodeId.indexOf("@")));
		this.ip = Integer.parseInt(strNodeId.substring(strNodeId.indexOf("@") + 1, strNodeId.length()));
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Compares two NodeIDs. 
	 * @param nodeId input node ID in NodeId format.
	 * @return 'true' if two node IDs are equal, otherwise return 'false'.
 	 */
	public boolean equals(NodeId nodeId) {
		if (this.id == nodeId.id && this.ip == nodeId.ip)
			return true;
		
		return false;
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Compares two NodeIDs. 
	 * @param strNodeId node ID in string format.
	 * @return 'true' if two node IDs are equal, otherwise return 'false'.
 	 */
	public boolean equals(String strNodeId) {
		int id = Integer.parseInt(strNodeId.substring(0, strNodeId.indexOf("@")));
		int ip = Integer.parseInt(strNodeId.substring(strNodeId.indexOf("@") + 1, strNodeId.length()));

		if (this.id == id && this.ip == ip)
			return true;
		
		return false;
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Generates the node if a peer in "id@ip" format. 
	 * @return "id@ip".
 	 */
	public String toString() {
		return id + "@" + ip;
	}
}
