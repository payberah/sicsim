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

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

import sicsim.config.SicsimConfig;
import sicsim.types.Message;
import sicsim.types.NodeId;
import sicsim.utils.FileIO;
import sicsim.utils.PatternMatching;

/**
 * Class to implement future event list. 
 */
public class FEL {
	private Queue<FutureEvent> futureEventList = new PriorityQueue<FutureEvent>();	

//----------------------------------------------------------------------------------
	/**
	 * Adds an event to the future event list.
	 * @param event The event to be added to the future event list.
	 */
	public void addEvent(FutureEvent event) {
		this.futureEventList.add(event);
	}

//----------------------------------------------------------------------------------
	/**
	 * Returns the size of the future event list.
	 * @return The size of the future event list.
	 */
	public int size() {
		return this.futureEventList.size();
	}

//----------------------------------------------------------------------------------
	/**
	 * Does the future event list has more events.
	 * @return 'true' if it has more events, otherwise returns 'false'.
	 */
	public boolean hasEvent() {
		return !(this.futureEventList.isEmpty());
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Returns the last event from the future event list.
	 * @return The last event in the future event list.
	 */
	public FutureEvent getLastEvent() {
		return this.futureEventList.poll();
	}
		
//----------------------------------------------------------------------------------
	public String toString() {
		String str = new String();
		
		Iterator<FutureEvent> felIter = this.futureEventList.iterator();
		while(felIter.hasNext()) {
			str += felIter.next().toString();
			str += PatternMatching.splitter + "\n";
		}
		
		str += "\n";
		
		return str;
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Saves the status of the future event list.
	 */
	public void saveState(long currentTime) {
		FileIO.write(this.toString(), SicsimConfig.FEL_FILE);
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Loads the status of the future event list.
	 */
	public void loadState() {
		String str = FileIO.read(SicsimConfig.FEL_FILE);
		String[] parts = str.split(PatternMatching.splitter);

		for (int i = 0; i < parts.length - 1; i++) {
			FutureEvent event = new FutureEvent();

			event.type = PatternMatching.getStrValue(parts[i], "type:");
			event.time = PatternMatching.getIntValue(parts[i], "time:");
			if (PatternMatching.getNodeValue(parts[i], "src:") != null)
				event.srcId = new NodeId(PatternMatching.getNodeValue(parts[i], "src:"));
			if (PatternMatching.getNodeValue(parts[i], "dest:") != null)
				event.destId = new NodeId(PatternMatching.getNodeValue(parts[i], "dest:"));
			event.data = Message.restore(PatternMatching.getStrValue(parts[i], "data:"));
			this.futureEventList.add(event);
		}

		if (SicsimConfig.LOG_SIM && SicsimConfig.LOG_LEVEL >= SicsimConfig.LOG_DEBUG)
			System.out.println("FEL => " + this.futureEventList);
	}
}
