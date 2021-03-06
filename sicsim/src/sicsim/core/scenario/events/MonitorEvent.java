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

public class MonitorEvent extends ScenarioEvent {
	private int eventCount = 1;
	private Class<?> overlay;
	
//----------------------------------------------------------------------------------
	public MonitorEvent(Class<?> overlay) {
		this.overlay = overlay;
	}
	
//----------------------------------------------------------------------------------
	public boolean hasNext() {
		return (this.eventCount-- == 1);
	}
 
//----------------------------------------------------------------------------------
	public Event nextEvent() {
		return new Event(new String("MONITOR_OVERLAY"), this.overlay);
	}	
	
//----------------------------------------------------------------------------------
	public void undo() {
		this.eventCount--;
	}
}
