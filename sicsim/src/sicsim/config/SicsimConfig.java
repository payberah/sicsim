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

package sicsim.config;

import sicsim.utils.FileIO;
import sicsim.utils.PatternMatching;

/**
 * The global configuration setting of simulator.
 */
public class SicsimConfig {
	//----------------------------------------------------------------------------------
	/**
	 * The simulator configuration file.
	 */
	private static String CONF_FILE = new String("sicsim.conf");
	
	//----------------------------------------------------------------------------------
	// sicsim setting
	/**
	 * Defines the maximum time of simulation.
	 */
	public static int SIM_TIME;
	/**
	 * Defines the ID space or maximum number of peers in the system.
	 */
	public static int MAX_NODE;

	//----------------------------------------------------------------------------------
	// scenario setting
	/**
	 * Defines the name of scenario file.
	 */
	public static String SCENARIO_FILE;
	
	//----------------------------------------------------------------------------------
	// enable/disable layers
	/**
	 * Enables/disables the synchronous update feature of simulator. 
	 * If this variable is true the simulator calls the syncMethod
	 * of all peers in system at each time unit.
	 */
	public static boolean SYNC_UPDATE;
	/**
	 * Enabling/Disabling the Monitor is controlled by this variable.
	 */
	public static boolean MONITOR;
	
	//----------------------------------------------------------------------------------
	// distributions used in simulator
	/**
  	 * The peer IDs in ID space can be distributed uniformly or it can be skewed. 
  	 * If this variable is true then the distribution of IDs are skewed.
	 */
	public static boolean SKEWED;
	/**
	 * In case of having skewed ID distribution, this variable shows the number of 
	 * clusters in ID space.
	 */
	public static int NUM_OF_CLUSTER;
	/**
	 * Defines the percentage of peers that are places in clusters. 
	 * For example if NUM OF CLUSTER is 4 and PROB OF CLUSTER is 0.7, means there 
	 * are 4 clusters in ID space and 70% of whole peers are placed in these clusters.
	 */
	public static double PROB_OF_CLUSTER;

	//----------------------------------------------------------------------------------
	// network latency setting
	/**
	 * Each message from one peer to other peers passes through the core network.
	 * This variable defines the mean latency in the core network.
	 */
	public static int NETWORK_LATENCY;
	/**
	 * To have a realistic model of latency, SICSIM assumes the latency between each
	 * two peers in not fixed and changes in time. This variable shows the maximum 
	 * latency drift that each connection has.
	 */
	public static int NETWORK_LATENCY_DRIFT;
	/**
	 * Defines the maximum link latency of links in the system.
	 */
	public static int LINK_LATENCY;

	//----------------------------------------------------------------------------------
	// sicssim timing setting
	/**
	 * In case of failing a peer, the peers who registered for this peer, will receive 
	 * the failure notification of failed peer in maximum FAILURE DETECTOR LATENCY time unit.
	 */
	public static int FAILURE_DETECTOR_LATENCY;
	public static int FAILURE_DETECTOR_MAX_TIME = 1000;
	/**
	 * Defines the period of getting snapshot from peers, by calling the snapshot method of Monitor.
	 */
	public static int SNAPSHOT_PERIOD;

	//----------------------------------------------------------------------------------
	// seed setting
	/**
	 * This is the seed used in simulator.
	 */
	public static long SEED;
	/**
	 * This is the seed used for failure detector.
	 */
	public static long FAILURE_DETECTION_SEED;
	/**
	 * This is the seed used for networking.
	 */
	public static long NETWORK_SEED;
	/**
	 * This is the seed used for bandwidth settings.
	 */
	public static long BANDWIDTH_SEED;
	/**
	 * This is the seed used for link latencies.
	 */
	public static long LATENCY_SEED;

	//----------------------------------------------------------------------------------
	// overlay log setting
	/**
	 * Defines the disable level of debugging.
	 */
	public static int LOG_DISABLE = 0;
	/**
	 * Defines the error level of debugging.
	 */
	public static int LOG_ERR = 1;
	/**
	 * Defines the warning level of debugging.
	 */
	public static int LOG_WARNING = 2;
	/**
	 * Defines the notice level of debugging.
	 */
	public static int LOG_NOTICE = 3;
	/**
	 * Defines the information level of debugging.
	 */
	public static int LOG_INFO = 4;
	/**
	 * Defines the debug level of debugging.
	 */
	public static int LOG_DEBUG = 5;
	/**
	 * Printing out the debugging information of simulator core is enabled/disabled by this variable.
	 */
	public static boolean LOG_SIM;
	/**
	 * Defines the level of printing debugging information. 0 means disable, 1 means error, 
	 * 2 means warning, 3 means notice, 4 means info and 5 means debug. 
	 * If it sets to 5, every debugging messages of SICSIM core is printed, but if it 
	 * sets to 3, only notice messages are printed, and setting it to 0, disable the 
	 * printing debug information.
	 */
	public static int LOG_LEVEL;

	//----------------------------------------------------------------------------------
	// log file setting
	/**
	 * Defines the name of file used to restore the size of network in time units.
	 */
	public static String NET_SIZE_FILE;
	/**
	 * Defines the file to save the bandwidth status of peers.
	 */
	public static String BW_FILE;
	/**
	 * Defines the name of file that stores the information of peers in overlay network.
	 */
	public static String NETWORK_FILE;
	/**
	 * Defines the name file to store future event list status.
	 */
	public static String FEL_FILE;
	/**
	 * Defines the name file to store the time system.
	 */
	public static String TIME_FILE;
	/**
	 * Defines the name file to store the overlay information.
	 */
	public static String OVERLAY_FILE;
	/**
	 * Defines the file to store the status of failure detector.
	*/
	public static String FAILURE_DETECTOR_FILE;
	
	//----------------------------------------------------------------------------------
	// streaming setting
	public static int STOP = -100;
	/**
	 * In case of using BandwidthPeer, the peers can send and receive real data to each other. 
	 * This variable defines the size of buffer in each peer that used to buffer received data.
	 */
	public static int BUFFER_SIZE;

	//----------------------------------------------------------------------------------
	// predefined node id
	/**
	 * Defines the node id that is used internally for the simualtor.
	 */
	public static String SICSSIM_NODE = new String("-7@-7");

	//----------------------------------------------------------------------------------
	// bw setting
	/**
	 * In case of using SICSIM for simulating media streaming systems, and in case of 
	 * splitting the media stream into a number of stripes, this variable defines the number of stripes.
	 */
	public static int NUM_OF_STRIPES;
	/**
	 * Defines the rate of each stripe.
	 */
	public static int STRIPE_RATE;
	
//----------------------------------------------------------------------------------
	public static void loadConfig() {
		try {
			String rawStr = FileIO.read(SicsimConfig.CONF_FILE);
			String str = PatternMatching.removeComments(rawStr);
			
			SicsimConfig.SIM_TIME = PatternMatching.getIntValue(str, "SIM_TIME:");
			SicsimConfig.MAX_NODE = PatternMatching.getIntValue(str, "MAX_NODE:");
			if (PatternMatching.getStrValue(str, "SYNC_UPDATE:").equalsIgnoreCase("true"))
				SicsimConfig.SYNC_UPDATE = true;
			else
				SicsimConfig.SYNC_UPDATE = false;

			if (PatternMatching.getStrValue(str, "MONITOR:").equalsIgnoreCase("true"))
				SicsimConfig.MONITOR = true;
			else
				SicsimConfig.MONITOR = false;
			
			SicsimConfig.SCENARIO_FILE = PatternMatching.getStrValue(str, "SCENARIO_FILE:");
			SicsimConfig.NETWORK_LATENCY = PatternMatching.getIntValue(str, "NETWORK_LATENCY:");
			SicsimConfig.NETWORK_LATENCY_DRIFT = PatternMatching.getIntValue(str, "NETWORK_LATENCY_DRIFT:");
			SicsimConfig.LINK_LATENCY = PatternMatching.getIntValue(str, "LINK_LATENCY:");
			SicsimConfig.FAILURE_DETECTOR_LATENCY = PatternMatching.getIntValue(str, "FAILURE_DETECTOR_LATENCY:");
			SicsimConfig.SNAPSHOT_PERIOD = PatternMatching.getIntValue(str, "SNAPSHOT_PERIOD:");

			if (PatternMatching.getStrValue(str, "SKEWED:").equalsIgnoreCase("true"))
				SicsimConfig.SKEWED = true;
			else
				SicsimConfig.SKEWED = false;
			SicsimConfig.NUM_OF_CLUSTER = PatternMatching.getIntValue(str, "NUM_OF_CLUSTER:");
			SicsimConfig.PROB_OF_CLUSTER = PatternMatching.getDoubleValue(str, "PROB_OF_CLUSTER:");			

			SicsimConfig.SEED = PatternMatching.getIntValue(str, "SEED:");
			SicsimConfig.FAILURE_DETECTION_SEED = SicsimConfig.SEED * 5;
			SicsimConfig.NETWORK_SEED = SicsimConfig.SEED * 7;
			SicsimConfig.BANDWIDTH_SEED = SicsimConfig.SEED * 2;
			SicsimConfig.LATENCY_SEED = SicsimConfig.SEED * 8;

			if (PatternMatching.getStrValue(str, "LOG_SIM:").equalsIgnoreCase("true"))
				SicsimConfig.LOG_SIM = true;
			else
				SicsimConfig.LOG_SIM = false;

			SicsimConfig.LOG_LEVEL = PatternMatching.getIntValue(str, "LOG_LEVEL:");
			SicsimConfig.NET_SIZE_FILE = PatternMatching.getStrValue(str, "NET_SIZE_FILE:");
			SicsimConfig.BW_FILE = PatternMatching.getStrValue(str, "BW_FILE:");
			SicsimConfig.NETWORK_FILE = PatternMatching.getStrValue(str, "NETWORK_FILE:");
			SicsimConfig.FEL_FILE = PatternMatching.getStrValue(str, "FEL_FILE:");
			SicsimConfig.TIME_FILE = PatternMatching.getStrValue(str, "TIME_FILE:");
			SicsimConfig.OVERLAY_FILE = PatternMatching.getStrValue(str, "OVERLAY_FILE:");
			SicsimConfig.FAILURE_DETECTOR_FILE = PatternMatching.getStrValue(str, "FAILURE_DETECTOR_FILE:");

			SicsimConfig.BUFFER_SIZE = PatternMatching.getIntValue(str, "BUFFER_SIZE:");
			SicsimConfig.NUM_OF_STRIPES = PatternMatching.getIntValue(str, "NUM_OF_STRIPES:");
			SicsimConfig.STRIPE_RATE = PatternMatching.getIntValue(str, "STRIPE_RATE:");
		} catch (Exception e) {
			System.err.println("error on opening/parsing sicsim.conf file.");
			System.exit(1);			
		}
	}

}
