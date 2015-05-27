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

package sicsim.core.scheduler;

import sicsim.types.Message;
import sicsim.types.NodeId;

public class FutureEvent implements Comparable<FutureEvent> {

	public NodeId srcId;
	public NodeId destId;
	public long time; 
	public String type;
	public Message data;
	public int signal;
	
	public Class<?> nodeType;
	public Class<?> linkType;
	public Class<?> monitorOverlay;	

//----------------------------------------------------------------------------------
	FutureEvent() {
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Create an Event
	 * @param time time of event to trigger
	 * @param fromId source
	 * @param Id if event type is PERIODIC, then Id is the identifier for this event
	 * 			 else, Id is the id of the destination node
	 * @param type the event type
	 * @param data the event data
	 */
	FutureEvent(long time, NodeId srcId, NodeId destId, String type, Message data) {
		this.time = time;
		this.srcId = srcId;
		this.destId = destId;
		this.type = type;
		this.data = data;
	}

//----------------------------------------------------------------------------------
	/**
	 * Create an Event
	 * @param time time of event to trigger
	 * @param fromId source
	 * @param Id if event type is PERIODIC, then Id is the identifier for this event
	 * 			 else, Id is the id of the destination node
	 * @param type the event type
	 * @param data the event data
	 */
	FutureEvent(long time, NodeId srcId, NodeId destId, String type, Message data, Class<?> nodeType, Class<?> linkType) {
		this.time = time;
		this.srcId = srcId;
		this.destId = destId;
		this.type = type;
		this.data = data;
		this.nodeType = nodeType;
		this.linkType = linkType;
	}
	
//----------------------------------------------------------------------------------
	FutureEvent(String type, Class<?> auxOverlay) {
		this.type = type;
		this.monitorOverlay = auxOverlay;
	}
	
//----------------------------------------------------------------------------------
	FutureEvent(String type, long time) {
		this.type = type;
		this.time = time;
	}

//----------------------------------------------------------------------------------
	FutureEvent(String type, long time, int signal) {
		this.type = type;
		this.time = time;
		this.signal = signal;
	}
	
//----------------------------------------------------------------------------------
	public int compareTo(FutureEvent event) {
		if (this.time < event.time)
			return -1;
		else if (this.time > event.time)
			return 1;
		else 
			return 0;
	}
	
//----------------------------------------------------------------------------------
	public String toString() {
		String str = new String();

		str += "type: " + ((this.type != null) ? this.type : "") + "\n";
		str += "time: " + this.time + "\n";
		str += "src: " + ((this.srcId != null) ? this.srcId : "") + "\n";
		str += "dest: " + ((this.destId != null) ? this.destId : "") + "\n";
		str += "data: " + ((this.data != null) ? this.data.toString() : "") + "\n";
		
		return str;
	}
}
