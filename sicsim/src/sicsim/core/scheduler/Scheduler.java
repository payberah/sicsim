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

import sicsim.config.SicsimConfig;
import sicsim.core.scenario.Scenario;
import sicsim.core.scenario.events.ScenarioEvent.Event;
import sicsim.types.NodeId;
import sicsim.utils.FileIO;
import sicsim.utils.PatternMatching;

/**
 * Class to implement the scheduler of simulator. This class implements the methods to reads the events from scenario
 * and adds them into the future event list. It also fetches the events from the future event list and send them 
 * to SicSim for execution.
 * @see SicSim 
 */
public class Scheduler {
	private Scenario scenario;
	private FEL futureEventList;
	private long clock = 0;
	private boolean initialized = false;
	
//----------------------------------------------------------------------------------
	public Scheduler(Scenario scenario, FEL futureEventList) {
		this.scenario = scenario;
		this.futureEventList = futureEventList;
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Generates the next event according to the scenario.
	 * @return 'false' if there is no other events in scenario, otherwise it returns 'true'. 
	 */
	private boolean generateScenarioEvent() {
		if (this.scenario.hasNextEvent()) {
			Event event = this.scenario.nextEvent();
			if (event.type.equalsIgnoreCase("MONITOR_OVERLAY"))
				this.futureEventList.addEvent(new FutureEvent(event.type, event.monitor));
			else if (event.type.equalsIgnoreCase("SIGNAL"))
				this.futureEventList.addEvent(new FutureEvent(event.type, this.clock + event.time, event.signal));
			else if (event.type.equalsIgnoreCase("DELAY") || event.type.equals("LOAD_OVERLAY") || event.type.equalsIgnoreCase("SAVE_OVERLAY"))
				this.futureEventList.addEvent(new FutureEvent(event.type, this.clock + event.time));
			else
				this.futureEventList.addEvent(new FutureEvent(this.clock + event.time, new NodeId(SicsimConfig.SICSSIM_NODE), new NodeId(SicsimConfig.SICSSIM_NODE), event.type, null, event.nodeType, event.linkType));
			return true;
		}
		
		return false;
	}

//----------------------------------------------------------------------------------
	/**
	 * Fetches the last event from the future event list and returns it.
	 * @return The last event. 
	 */
	public FutureEvent nextStep() {
		if (!this.initialized) {
			this.generateScenarioEvent();
			this.initialized = true;
		}				

		if (this.futureEventList.hasEvent()) {
			FutureEvent currentEvent = this.futureEventList.getLastEvent();
			long eventTime = currentEvent.time;

			if (this.clock != eventTime) 
				this.clock = eventTime;

			if (currentEvent.type.equalsIgnoreCase("JOIN") || 
				currentEvent.type.equalsIgnoreCase("LEAVE") || 
				currentEvent.type.equalsIgnoreCase("FAILURE") || 
				currentEvent.type.equalsIgnoreCase("DELAY") || 
				currentEvent.type.equalsIgnoreCase("SAVE_OVERLAY") || 
				currentEvent.type.equalsIgnoreCase("LOAD_OVERLAY") || 
				currentEvent.type.equalsIgnoreCase("MONITOR_OVERLAY") ||
				currentEvent.type.equalsIgnoreCase("SIGNAL"))
				this.generateScenarioEvent();
		
			if (eventTime > SicsimConfig.SIM_TIME) {
				if (SicsimConfig.LOG_SIM && SicsimConfig.LOG_LEVEL >= SicsimConfig.LOG_WARNING)
					System.out.println("SICSSIM => Simulation maximum time achieved!");
				return null;
			}
			
			return currentEvent;
		}
		else
			return null;
	}

//----------------------------------------------------------------------------------
	public long getCurrentClock() {
		return this.clock;
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Gets the current clock of simulator.
	 * @return The current clock of simulator. 
	 */
	public String toString() {
		return new String("time: " + this.clock);
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Saves the status of the scheduler.
	 */
	public void saveState(long currentTime) {
		FileIO.write(this.toString(), SicsimConfig.TIME_FILE);
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Load the status of the scheduler.
	 */
	public void loadState() {
		String str = FileIO.read(SicsimConfig.FEL_FILE);
		this.clock = PatternMatching.getLongValue(str, "time:");
		
		if (SicsimConfig.LOG_SIM && SicsimConfig.LOG_LEVEL >= SicsimConfig.LOG_DEBUG)
			System.out.println("SCHEDULER => time: " + this.clock);
	}
}
