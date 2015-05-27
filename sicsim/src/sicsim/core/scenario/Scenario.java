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

package sicsim.core.scenario;

import java.util.*;

import sicsim.config.SicsimConfig;
import sicsim.core.scenario.events.MonitorEvent;
import sicsim.core.scenario.events.DelayEvent;
import sicsim.core.scenario.events.LoadOverlayEvent;
import sicsim.core.scenario.events.LotteryEvent;
import sicsim.core.scenario.events.SaveOverlayEvent;
import sicsim.core.scenario.events.ScenarioEvent;
import sicsim.core.scenario.events.SignalEvent;
import sicsim.core.scenario.events.ScenarioEvent.Event;
import sicsim.utils.FileIO;
import sicsim.utils.PatternMatching;

/**
 * Class to implement the scenario of behaving the simulator. 
 */
public class Scenario {

	private Vector<ScenarioEvent> scenarioList = new Vector<ScenarioEvent>();
	private Iterator<ScenarioEvent> scenariosIter;
	private ScenarioEvent currentScenario = null;
	
//----------------------------------------------------------------------------------
	public Scenario() {
		this.loadScenario();
		scenariosIter = scenarioList.iterator();
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Tells if there are more events to be happen or not.
	 * @return 'true' if there  are more events to be done, otherwise returns 'false'.
	 */
	public boolean hasNextEvent() {
		if (this.currentScenario == null && this.scenariosIter.hasNext()) 
			this.currentScenario = this.scenariosIter.next();
		else if (this.currentScenario == null)
			return false;
		
		while(true) {
			if (this.currentScenario.hasNext())
				return true;
			else if (!this.scenariosIter.hasNext())
				return false;
			else
				this.currentScenario = this.scenariosIter.next();
		} 
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Returns the next event from scenario.
	 * @return The next event.
	 */
	public Event nextEvent() {
		return this.currentScenario.nextEvent();
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Push back the event.
	 */
	public void undo() {
		this.currentScenario.undo();
	}
	
//----------------------------------------------------------------------------------
	private void loadScenario() {
		String eventType;
		String overlayType;
		String nodeType;
		String linkType;
		long count;
		int deltaTime;
		int numJoins;
		int numLeaves;
		int numFailures;
		int delay;
		int signal;
		
		try {
			String rawStr = FileIO.read(SicsimConfig.SCENARIO_FILE);
			String str = PatternMatching.removeComments(rawStr);
			String[] parts = str.split(PatternMatching.scenarioSplitter);

			for (int i = 0; i < parts.length; i++) {
				eventType = PatternMatching.getStrValue(parts[i], "type:");
				if (eventType.equalsIgnoreCase("lottery")) {
					nodeType = PatternMatching.getStrValue(parts[i], "peer:");
					linkType = PatternMatching.getStrValue(parts[i], "link:");
					count = PatternMatching.getIntValue(parts[i], "count:");				
					deltaTime = PatternMatching.getIntValue(parts[i], "interval:");
					numJoins = PatternMatching.getIntValue(parts[i], "join:");
					numLeaves = PatternMatching.getIntValue(parts[i], "leave:");
					numFailures = PatternMatching.getIntValue(parts[i], "failure:");
					this.scenarioList.add(i, new LotteryEvent(count, deltaTime, numJoins, numLeaves, numFailures, Class.forName(nodeType), Class.forName(linkType)));
				} else if (eventType.equalsIgnoreCase("delay")) {
					delay = PatternMatching.getIntValue(parts[i], "delay:");
					this.scenarioList.add(i, new DelayEvent(delay));					
				} else if (eventType.equalsIgnoreCase("monitor")) {
					overlayType = PatternMatching.getStrValue(parts[i], "monitor:");
					this.scenarioList.add(i, new MonitorEvent(Class.forName(overlayType)));					
				} else if (eventType.equalsIgnoreCase("signal")) {
					count = PatternMatching.getIntValue(parts[i], "count:");				
					deltaTime = PatternMatching.getIntValue(parts[i], "interval:");
					signal = PatternMatching.getIntValue(parts[i], "signal:");
					this.scenarioList.add(i, new SignalEvent(count, deltaTime, signal));					
				} else if (eventType.equalsIgnoreCase("save")) {
					this.scenarioList.add(i, new SaveOverlayEvent());					
				} else if (eventType.equalsIgnoreCase("load")) {
					this.scenarioList.add(i, new LoadOverlayEvent());					
				}
			}			
		} catch (Exception e) {
			System.err.println("error on opening/parsing scenario file.");
			System.exit(1);
		}
	}
}
