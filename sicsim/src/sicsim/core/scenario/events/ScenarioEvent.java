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

package sicsim.core.scenario.events;

/**
 * An event in a scenario. This is internal to the simulator.
 */
public abstract class ScenarioEvent {

//----------------------------------------------------------------------------------
	public static class Event {
		public String type;
		public int time;
		public int signal;
		public Class<?> nodeType;
		public Class<?> linkType;
		public Class<?> monitor;

		//----------------------------------------------------------------------------------
		/**
		 * @param type Event type
		 * @param time Time of the event
		 */
		public Event(String type, int time) {
			this.type = type;
			this.time = time;
		}
		
		//----------------------------------------------------------------------------------
		/**
		 * @param type Event type
		 * @param time Time of the event
		 * @param signal The signal number
		 */
		public Event(String type, int time, int signal) {
			this.type = type;
			this.time = time;
			this.signal = signal;
		}
		
		//----------------------------------------------------------------------------------
		/**
		 * @param type Event type
		 * @param monitor Time monitor class
		 */
		public Event(String type, Class<?> monitor) {
			this.type = type;
			this.monitor = monitor;
		}
		
		//----------------------------------------------------------------------------------
		/**
		 * @param type Event type
		 * @param time Time of the event
		 * @param nodeType Type of the peer
		 * @param linkType Type of the link
		 */
		public Event(String type, int time, Class<?> nodeType, Class<?> linkType) {
			this.type = type;
			this.time = time;
			this.nodeType = nodeType;
			this.linkType = linkType;
		}
	}

//----------------------------------------------------------------------------------
	/**
	 * Does the scenario have more events?
	 * @return true if events exist, false otherwise
	 */
	public abstract boolean hasNext();
 
//----------------------------------------------------------------------------------
	/**
	 * Get the next event in the scenario
	 * @return next event
	 */
	public abstract Event nextEvent();

//----------------------------------------------------------------------------------	
	/**
	 * Push back the event.
	 */
	public abstract void undo();
}
