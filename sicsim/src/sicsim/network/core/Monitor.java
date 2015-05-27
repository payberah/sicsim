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

package sicsim.network.core;

import sicsim.config.SicsimConfig;

/**
 * This is an abstract class that provides a global view to the whole peers in system.
 */
public abstract class Monitor {
	/**
	 * Monitor access to all peers in the system through 'network'. The peers in 'network' may not be a member of overlay network.
	 */
	protected Network network;

	/**
	 * Monitor access to all peers in the overlay network through 'overlay'.
	 */
	protected OverlayNetwork overlay;

	/**
	 * Monitor access to the bandwidth information of all peers through 'bandwidth'.
	 */
	protected Bandwidth bandwidth;
	
//----------------------------------------------------------------------------------
	/**
	 * Initialize the monitor
	 */
	public void init(Network network, OverlayNetwork overlay, Bandwidth bandwidth) {
		this.network = network;
		this.overlay = overlay;
		this.bandwidth = bandwidth;
	}	
	
//----------------------------------------------------------------------------------
	/**
	 * This method is called in each time unit by simulator.
	 * @param currentTime Specifies the currentTime
 	 */
	public abstract void update(long currentTime);

//----------------------------------------------------------------------------------
	/**
	 * This method is called at the end of simulation.
	 * @param currentTime Specifies the currentTime
 	 */
	public abstract void verify(long currentTime);
	
//----------------------------------------------------------------------------------
	/**
	 * This method is called at periodically by interval SicsSimConfig.SNAPSHOT_PERIOD. 
	 * @param currentTime Specifies the currentTime
	 * @see SicsimConfig
 	 */
	public abstract void snapshot(long currentTime);

}
