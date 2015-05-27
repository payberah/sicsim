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

import java.util.*;

import sicsim.config.SicsimConfig;
import sicsim.utils.Distribution;

public class LotteryEvent extends ScenarioEvent {
	
	private long count;
	private int deltaTime;
	private int numJoins;
	private int numLeaves;
	private int numFailures;
	
	private Class<?> nodeType;
	private Class<?> linkType;
	
	private Random rand = new Random(SicsimConfig.NETWORK_SEED);
	private long eventCount = 0;
	
//----------------------------------------------------------------------------------
	/**
	 * Generate a new lottery event
	 * @param count total number of events u want to generate
	 * @param time time between two events
	 * @param joins ratio of # of joins u want out of the total lottery events(given as 'count')
	 * @param leaves ratio of # of leaves u want out of the total lottery events(given as 'count')
	 * @param failures ratio of # of leaves u want out of the total lottery events(given as 'count')
	 */
	public LotteryEvent(long count, int time, int joins, int leaves, int failures, Class<?> nodeType, Class<?> linkType) {
		this.count = count;
		this.deltaTime = time;
		this.numJoins = joins;
		this.numLeaves = leaves;
		this.numFailures = failures;
		this.nodeType = nodeType;
		this.linkType = linkType;
	}
	
//----------------------------------------------------------------------------------
	public boolean hasNext() {
		return (this.eventCount < this.count);
	}
 
//----------------------------------------------------------------------------------
	public Event nextEvent() {
		String eventType;
		int time = Distribution.exp(this.deltaTime);
		int randResult = this.rand.nextInt(this.numJoins + this.numLeaves + this.numFailures);
		
		if (randResult < this.numJoins)
			eventType = new String("JOIN");
		else if (randResult < this.numJoins + this.numLeaves)
			eventType = new String("LEAVE");
		else
			eventType = new String("FAILURE");
		
		this.eventCount++;
		
		return new Event(eventType, time, this.nodeType, this.linkType);
	}	
	
//----------------------------------------------------------------------------------
	public void undo() {
		this.eventCount--;
	}
}
