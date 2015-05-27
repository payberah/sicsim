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

package sicsim.types;

import sicsim.utils.PatternMatching;

public class LinkRate {
	public int downloadBandwidth = 0;
	public int uploadBandwidth = 0;
	
//----------------------------------------------------------------------------------
	public LinkRate() {
	}
	
//----------------------------------------------------------------------------------
	public LinkRate(int downlaodBw, int uploadBw) {
		this.downloadBandwidth = downlaodBw;
		this.uploadBandwidth = uploadBw;
	}
	
//----------------------------------------------------------------------------------
	public String toString() {
		return new String("downloadBw: " + this.downloadBandwidth + ", uploadBw: " + this.uploadBandwidth);
	}
	
//----------------------------------------------------------------------------------
	public static LinkRate restore(String str) {
		LinkRate linkRate = new LinkRate();
		linkRate.downloadBandwidth = PatternMatching.getIntValue(str, "downloadBw:");
		linkRate.uploadBandwidth = PatternMatching.getIntValue(str, "uploadBw:");
		
		return linkRate;
	}
}
