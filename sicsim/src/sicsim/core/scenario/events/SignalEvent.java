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

import sicsim.utils.Distribution;

public class SignalEvent extends ScenarioEvent {
	private long count;
	private int deltaTime;
	private int signal;
	private int eventCount = 0;
	
//----------------------------------------------------------------------------------
	public SignalEvent(long count, int deltaTime, int signal) {
		this.count = count;
		this.deltaTime = deltaTime;
		this.signal = signal;
	}
	
//----------------------------------------------------------------------------------
	public boolean hasNext() { 
		return (this.eventCount < this.count);
	}

//----------------------------------------------------------------------------------
	public Event nextEvent() {
		this.eventCount++;
		int time = Distribution.exp(this.deltaTime);
		return new Event(new String("SIGNAL"), time, this.signal);
	}
	
//----------------------------------------------------------------------------------
	public void undo() {
		this.eventCount--;
	}
}

