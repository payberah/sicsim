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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import sicsim.config.SicsimConfig;
import sicsim.types.NodeId;
import sicsim.utils.PatternMatching;

public class Stripe {
	private NodeId provider;
	private long lastRecvSegment;
	private SortedMap<Long, Long> buffer = new TreeMap<Long, Long>(); // <segmentNumber, time>

//----------------------------------------------------------------------------------
	public Stripe() {
	}
	
//----------------------------------------------------------------------------------
	public Stripe(NodeId provider, long lastUpdateTime, long segmentNumber) {
		this.provider = new NodeId(provider);
		this.lastRecvSegment = segmentNumber;
		this.buffer.put(segmentNumber, lastUpdateTime);		
	}
	
//----------------------------------------------------------------------------------
	public long getFirstSegment() {
		return this.buffer.firstKey();
	}
	
//----------------------------------------------------------------------------------
	public long getLastSegment() {
		return this.buffer.lastKey();
	}
	

//----------------------------------------------------------------------------------
	public long getLastUpdate() {
		return this.buffer.get(this.buffer.lastKey());
	}

//----------------------------------------------------------------------------------
	public long getSegmentUpdateTime(long segment) {
		return this.buffer.get(segment);
	}

//----------------------------------------------------------------------------------
	public long getLargestSegmentAt(long time) {
		Long key = this.buffer.lastKey();
		
		while (this.buffer.get(key) > time)
			key = this.buffer.headMap(key).lastKey();
		
		return key;
	}
	
//----------------------------------------------------------------------------------	
	public long getNextSegment(long segment) {
		Iterator<Long> segmentIter = this.buffer.tailMap(segment + 1).keySet().iterator();
		
		if (segmentIter.hasNext())
			return (Long)segmentIter.next();
		else
			return -1;
	}
	
//----------------------------------------------------------------------------------
	public NodeId getProvider() {
		return this.provider;
	}

//----------------------------------------------------------------------------------
	public long getLastRecvSegFromPovider() {
		return this.lastRecvSegment;
	}
	
//----------------------------------------------------------------------------------
	public boolean containsSegment(long segment, long time) {
		if (this.containsSegment(segment) && this.buffer.get(segment) <= time)
			return true;
		else
			return false;
	}

//----------------------------------------------------------------------------------
	public boolean containsSegment(long segment) {
		return this.buffer.containsKey(segment);
	}
	
//----------------------------------------------------------------------------------	
	public HashMap<Long, Long> getStream(long lastSegment, long fromTime, long toTime) {
		int hopeCount = 0;
		HashMap<Long, Long> stream = new HashMap<Long, Long>();
		long maxNumberOfSegments = toTime - fromTime;
		
		long drift = fromTime - this.buffer.get(lastSegment).longValue(); // probably needs a -1 too, if fromTime is the time unit after the last download
		
		long segment = this.getNextSegment(lastSegment);
		long segmentDownloadTime;
		
		while (stream.size() < maxNumberOfSegments && segment != -1) { 
			if (this.getSegmentUpdateTime(segment) <= toTime - drift) { // here maybe we could break the loop when there is no hope for more segments, e.g. after a number of unsuccesful timing comparisons
				segmentDownloadTime = this.buffer.get(segment).longValue() + drift;
				stream.put(segment, segmentDownloadTime);
			}
			else
				hopeCount++;
			
			if (hopeCount == 50)
				break;
			
			lastSegment = segment;
			segment = this.getNextSegment(lastSegment);
		}
		
		return stream;
	}

//----------------------------------------------------------------------------------
	public void putSingleSegment(NodeId provider, long segment, long updateTime) {
		// add segment to the buffer if it does not exist, or update its associated time to an earlier time if neccessary
		if (!this.containsSegment(segment) || this.getSegmentUpdateTime(segment) > updateTime ) { 
			this.buffer.put(segment, new Long(updateTime));
			this.provider = new NodeId(provider);
			this.lastRecvSegment = segment;
		}
		
	}
	
//----------------------------------------------------------------------------------
	public void putStream(Map<Long, Long> stream, long updateTime) {
		int newSize = this.buffer.size() + stream.size();
		
		if (newSize > SicsimConfig.BUFFER_SIZE) {
			Long key = new Long(0);
			SortedMap<Long, Long> temp = new TreeMap<Long, Long>();
			temp.putAll(this.buffer);
			temp.putAll(stream);
			Iterator<Long> iter = temp.keySet().iterator();
			for (int i = 0; i < newSize - SicsimConfig.BUFFER_SIZE; i++)
				key = iter.next();
			this.buffer.clear();
			this.buffer.putAll(temp.tailMap(key));
		}
		else
			this.buffer.putAll(stream);
		
		if (stream != null && !stream.isEmpty()) {
			SortedMap<Long, Long> temp = new TreeMap<Long, Long>();
			temp.putAll(stream);
			this.lastRecvSegment = temp.lastKey();
		}
	}
	
//----------------------------------------------------------------------------------
	public void putStream(Vector<Long> segmentList, long updateTime) {
		Map<Long, Long> stream = new HashMap<Long, Long>();
		
		long time = updateTime - segmentList.size(); // update time for the first segment in the vector
		for (int i= 0; i < segmentList.size(); i++, time++)
			stream.put(segmentList.elementAt(i), time);
		
		int newSize = this.buffer.size() + stream.size();
		
		if (newSize > SicsimConfig.BUFFER_SIZE) {
			Long key = new Long(0);
			SortedMap<Long, Long> temp = new TreeMap<Long, Long>();
			temp.putAll(this.buffer);
			temp.putAll(stream);
			Iterator<Long> iter = temp.keySet().iterator();
			for (int i = 0; i < newSize - SicsimConfig.BUFFER_SIZE; i++)
				key = iter.next();
			this.buffer.clear();
			this.buffer.putAll(temp.tailMap(key));
		}
		else
			this.buffer.putAll(stream);

		if (segmentList != null && !segmentList.isEmpty())
			this.lastRecvSegment = segmentList.lastElement();
	}

//----------------------------------------------------------------------------------
	public void removeProvider(NodeId provider) {
		this.provider = null;
	}

//----------------------------------------------------------------------------------
	public void updateProvider(NodeId provider) {
		this.provider = new NodeId(provider);
	}

//----------------------------------------------------------------------------------
	public String toString() {
		Long segment;
		String str = new String();
		Iterator<Long> segmentsIter = this.buffer.keySet().iterator();
		
		str += "provider: " + this.provider.toString() + "\n";
		str += "lastRecvSeg: " + this.lastRecvSegment + "\n";
 		while (segmentsIter.hasNext()) {
			segment = segmentsIter.next();
			str += (PatternMatching.subSplitter + "(segment: " + segment + ", download time: " + this.buffer.get(segment) + ")\n");
		}

		return str;
	}
	
//----------------------------------------------------------------------------------
	public void restore(String str) {
		long segment;
		long time;
		
		this.provider = PatternMatching.getNodeValue(str, "provider:");
		this.lastRecvSegment = PatternMatching.getLongValue(str, "lastRecvSeg:"); 
		
		String[] parts = str.split(PatternMatching.subSplitter);

		for (int i = 1; i < parts.length; i++) {
			segment = PatternMatching.getLongValue(parts[i], "segment:");
			time = PatternMatching.getLongValue(parts[i], "time:");
			this.buffer.put(segment, time);
		}
	}
}
