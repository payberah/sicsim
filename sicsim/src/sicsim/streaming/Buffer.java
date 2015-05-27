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

package sicsim.streaming;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import sicsim.config.SicsimConfig;
import sicsim.types.NodeId;
import sicsim.utils.PatternMatching;

public class Buffer {

	private long playback = SicsimConfig.STOP;
	private long playbackLastUpdate = 0;
	private long pauseDuration = 0;
	private long pauseStartTime = 0;
	private boolean hasStratedPlayback = false;
	
	private HashMap<String, Stripe> buffer = new HashMap<String, Stripe>(); // <strip_name, stripe_information>

	
//----------------------------------------------------------------------------------
	public long getPlayback() {
		return this.playback;
	}
	
//----------------------------------------------------------------------------------
	public void addBuffer(NodeId provider, String stripe, long segment, long currentTime) {
		Stripe stripeInfo = new Stripe(provider, currentTime, segment);
		
		if (this.buffer.get(stripe) == null)
			this.buffer.put(stripe, stripeInfo);
		else {  // if this stripe already exists in the data buffer, we only need to add this last segment to the buffer
			this.buffer.get(stripe).putSingleSegment(provider, segment, currentTime);
		}
	}
	
//----------------------------------------------------------------------------------
	public boolean containsStripe(String stripe) {
		return this.buffer.containsKey(stripe);
	}
	
//----------------------------------------------------------------------------------
	public boolean containsSegment(long segment) {
		String stripe;
		Iterator<String> stripeIter = this.buffer.keySet().iterator();
		
		while (stripeIter.hasNext()) {
			stripe = stripeIter.next();
			if (this.buffer.get(stripe).containsSegment(segment))
				return true;
		}
		
		return false;
	}
	
//----------------------------------------------------------------------------------
	public boolean containsSegment(long segment, long time) {
		String stripe;
		Iterator<String> stripeIter = this.buffer.keySet().iterator();
		
		while (stripeIter.hasNext()) {
			stripe = stripeIter.next();
			if (this.buffer.get(stripe).containsSegment(segment, time))
				return true;
		}
		
		return false;
	}
	
//----------------------------------------------------------------------------------
	public boolean containsSegment(String stripe, long segment) {
		return (this.buffer.containsKey(stripe) && this.buffer.get(stripe).containsSegment(segment));
	}
	
//----------------------------------------------------------------------------------
	public boolean containsSegment(String stripe, long segment, long time) {
		return (this.buffer.containsKey(stripe) && this.buffer.get(stripe).containsSegment(segment, time));
	}
	
//----------------------------------------------------------------------------------
	public NodeId getStripeProvider(String stripe) {
		if (this.buffer.containsKey(stripe))
			return this.buffer.get(stripe).getProvider();
		
		return null;
	}
	
//----------------------------------------------------------------------------------
	public Vector<NodeId> getProviders() {
		String stripe;
		Vector<NodeId> providers = new Vector<NodeId>();
		Iterator<String> stripeIter = this.buffer.keySet().iterator();
		
		while (stripeIter.hasNext()) {
			stripe = stripeIter.next();
			providers.addElement(this.getStripeProvider(stripe));
		}
		
		return providers;
	}
	
//----------------------------------------------------------------------------------
	public void removeProvider(NodeId provider, String stripe) {
		if (this.buffer.containsKey(stripe))
			this.buffer.get(stripe).removeProvider(provider);
	}

//----------------------------------------------------------------------------------
	public void updateProvider(NodeId provider, String stripe) {
		if (this.buffer.containsKey(stripe))
			this.buffer.get(stripe).updateProvider(provider);
	}

//----------------------------------------------------------------------------------
	public void updateBuffer(String stripe, Vector<Long> segmentList, long updateTime) {
		Stripe stripeInfo = this.buffer.get(stripe);
		
		stripeInfo.putStream(segmentList, updateTime);
		
		this.buffer.put(stripe, stripeInfo);
		
	}
	
//----------------------------------------------------------------------------------
	public void updateBuffer(String stripe, HashMap<Long, Long> stream, long updateTime) {
		Stripe stripeInfo = this.buffer.get(stripe);
		
		stripeInfo.putStream(stream, updateTime);
		
		this.buffer.put(stripe, stripeInfo);
		
	}
	
//----------------------------------------------------------------------------------
	public long findEarliestSegment() {
		String stripe;
		long segment = Long.MAX_VALUE;
		Iterator<String> stripeIter = this.buffer.keySet().iterator();
		
		while (stripeIter.hasNext()) {
			stripe = stripeIter.next();
			if (segment > this.buffer.get(stripe).getFirstSegment())
				segment = this.buffer.get(stripe).getFirstSegment();
		}
		
		return segment;		
	}
	
//----------------------------------------------------------------------------------
	public long getLargestSegment(long time) {
		String stripe;
		long segment = Long.MIN_VALUE;
		long largeSegment;
		Iterator<String> stripeIter = this.buffer.keySet().iterator();
		
		while (stripeIter.hasNext()) {
			stripe = stripeIter.next();
			largeSegment = this.buffer.get(stripe).getLargestSegmentAt(time);
			if (segment < largeSegment)
				segment = largeSegment;
		}		
		
		return segment;
	}
	
//----------------------------------------------------------------------------------
	public long getSmallestHead(long time) {
		String stripe;
		long segment = Long.MAX_VALUE;
		long largeSegment;
		Iterator<String> stripeIter = this.buffer.keySet().iterator();
		
		while (stripeIter.hasNext()) {
			stripe = stripeIter.next();
			largeSegment = this.buffer.get(stripe).getLargestSegmentAt(time);
			if (segment > largeSegment)
				segment = largeSegment;
		}		
		
		return segment;
	}
	
//----------------------------------------------------------------------------------
	public long getLastSegment(String stripe) {
		if (this.buffer.containsKey(stripe))
			return this.buffer.get(stripe).getLastSegment();

		return -1;		
	}
	
//----------------------------------------------------------------------------------
	public long getLastRecvSegment(String stripe) {
		if (this.buffer.containsKey(stripe))
			return this.buffer.get(stripe).getLastRecvSegFromPovider();

		return -1;		
	}
	
//----------------------------------------------------------------------------------
	public long getLastUpdateTime(String stripe) {
		if (this.buffer.containsKey(stripe))
			return this.buffer.get(stripe).getLastUpdate();
		
		return -1;		
	}

//----------------------------------------------------------------------------------
	public long getUpdateTime(String stripe, long segment) {
		if (this.buffer.containsKey(stripe))
			return this.buffer.get(stripe).getSegmentUpdateTime(segment);
		
		return -1;		
	}

//----------------------------------------------------------------------------------
	public long getNextSegment(String stripe, long segment) {
		return this.buffer.get(stripe).getNextSegment(segment);
	}
	
//----------------------------------------------------------------------------------
	public boolean isPlaybackTimeSet() {
		return (this.playback != SicsimConfig.STOP);
	}
	
//----------------------------------------------------------------------------------
	public void setPlaybackTime(long time) {
		if (!isPlaybackTimeSet()) {
			this.playback = this.findEarliestSegment();
			this.playbackLastUpdate = time;
		}
		else
			updatePlaybackTime(time);
	}
	
//----------------------------------------------------------------------------------	
	public HashMap<Long, Long> getStripeStream(String stripe, long lastSegment, long fromTime, long toTime) {
		if (this.containsStripe(stripe))
			return this.buffer.get(stripe).getStream(lastSegment, fromTime, toTime);
		
		return null;
	}
	
//----------------------------------------------------------------------------------
	public void updatePlaybackTime(long time) {
		long expectedPlayback = 0; // this shows how much we expect to proceed

		if (this.playbackLastUpdate < time) { // if an update is required
			
			if (!isPlaybackTimeSet())
				return;
			
			if (this.pauseDuration <= 0)
				expectedPlayback = this.playback + (time - this.playbackLastUpdate);
			else {
				if (time - this.pauseStartTime >= this.pauseDuration) { // pause duration is over
					expectedPlayback = this.playback + (time - this.playbackLastUpdate - this.pauseDuration);
					this.pauseDuration = 0;
				}
				else { // pause needs to be continued
					expectedPlayback = this.playback;
					this.pauseDuration -= (time - this.pauseStartTime);
					this.pauseStartTime = time;
				}
			}
			
			for (; expectedPlayback > this.playback; expectedPlayback--)
				if (this.containsSegment(expectedPlayback, time)) {
					this.playback = expectedPlayback;
					break;
				}	 			
				
			this.playbackLastUpdate = time;
		}
		else if (this.playbackLastUpdate < time)
			System.out.println("\n Error in Buffer.java: wrong parameter to update playback time! Time cannot move backward.");
	}

//----------------------------------------------------------------------------------
	public double getQuality(long time, int numOfDemandedStripes) {
		double quality = 0;
		String stripe;
		this.updatePlaybackTime(time);
		
		if (this.playback == SicsimConfig.STOP)
			return 0;
		
		Iterator<String> stripeIter = this.buffer.keySet().iterator();
		while (stripeIter.hasNext()) {
			stripe = stripeIter.next();
			if (this.buffer.get(stripe).containsSegment(this.playback, time))
				quality++;
		}

		return (quality * 100 / numOfDemandedStripes);
	}

//----------------------------------------------------------------------------------
	public long getPauseDuration() {
		return this.pauseDuration;
	}

//----------------------------------------------------------------------------------
	public void setPauseDuration(long pauseDuration) {
		this.pauseDuration = pauseDuration;
	}

//----------------------------------------------------------------------------------
	public long getPauseStartTime() {
		return this.pauseStartTime;
	}

//----------------------------------------------------------------------------------
	public void setPauseStartTime(long pauseStartTime) {
		this.pauseStartTime = pauseStartTime;
	}

//----------------------------------------------------------------------------------
	public boolean hasStratedPlayback() {
		return this.hasStratedPlayback;
	}

//----------------------------------------------------------------------------------
	public void setHasStartPlayback() {
		this.hasStratedPlayback = true;
	}

//----------------------------------------------------------------------------------
	public void showBuffer(NodeId nodeId, String stripe) {
		System.out.println(nodeId + " " + stripe + " --> " + this.buffer.get(stripe).toString());
	}
	
//----------------------------------------------------------------------------------
	public void showAllBuffer(NodeId nodeId) {
		String stripe;
		for (int i = 0; i < SicsimConfig.NUM_OF_STRIPES; i++) {
			stripe = new String("stripe" + i);
			if (this.containsStripe(stripe))
				System.out.println(nodeId + " " + stripe + " --> " + this.buffer.get(stripe).toString());
		}
	}

//----------------------------------------------------------------------------------
	public String toString() {
		String stripe;
		String str = new String();
		Iterator<String> stripeIter = this.buffer.keySet().iterator();
		
		str += ("playback: " + this.playback + "\n");
		str += ("lastUpdate: " + this.playbackLastUpdate + "\n");
		str += ("pauseDuration: " + this.pauseDuration + "\n");
		str += ("pauseStartTime: " + this.pauseStartTime + "\n");
		str += ("hasStratedPlayback: " + this.hasStratedPlayback + "\n");
		
		while (stripeIter.hasNext()) {
			stripe = stripeIter.next();
			str += (PatternMatching.splitter + "\n");
			str += ("stripeName: " + stripe + " \n");
			str += ("stripe: \n" + this.buffer.get(stripe).toString());
		}
		
		return str;
	}
	
//----------------------------------------------------------------------------------
	public void restore(String str) {
		String stripeName = new String();
		Stripe stripe = new Stripe(); 
		
		this.playback = PatternMatching.getLongValue(str, "playback:");
		this.playbackLastUpdate = PatternMatching.getLongValue(str, "lastUpdate:");
		this.pauseDuration = PatternMatching.getLongValue(str, "pauseDuration:");
		this.pauseStartTime = PatternMatching.getLongValue(str, "pauseStartTime:");
		this.hasStratedPlayback = Boolean.parseBoolean(PatternMatching.getStrValue(str, "hasStartedPlayback:"));
		
		String[] parts = str.split(PatternMatching.splitter);

		for (int i = 1; i < parts.length; i++) {
			stripeName = PatternMatching.getStrValue(parts[i], "stripeName:");
			String ooo = parts[i].substring(parts[i].indexOf("stripe:"));
			stripe.restore(ooo);
			this.buffer.put(stripeName, stripe);
		}
	}
}
