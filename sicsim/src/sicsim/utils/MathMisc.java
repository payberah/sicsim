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

package sicsim.utils;

/**
 * Class to provide basic method to work with IDs distributed over a ring.
 */
public class MathMisc {

//----------------------------------------------------------------------------------
	/**
	 * Checks if 'id' is placed in (start, end].
	 * @param id Specifies the id to check its availability in interval.
	 * @param start Specifies the beginning of interval.
	 * @param end Specifies the end of interval.
	 * @param n Specifies the maximum number of IDs in ID space.
	 * @return 'true' if 'id' placed in (start, end], otherwise returns 'false'.
	 */
    public static boolean belongsTo(long id, long start, long end, long n) { // t E ]beg,end]
    	long nx = 0;
    	long ny = MathMisc.modMinus(end, start, n);
    	long nid = MathMisc.modMinus(id, start, n);
    	return start == end || (nid > nx && nid <= ny);
    }

//----------------------------------------------------------------------------------
	/**
	 * Checks if 'id' is placed in (start, end).
	 * @param id Specifies the id to check its availability in interval.
	 * @param start Specifies the beginning of interval.
	 * @param end Specifies the end of interval.
	 * @param n Specifies the maximum number of IDs in ID space.
	 * @return 'true' if 'id' placed in (start, end), otherwise returns 'false'.
	 */
    public static boolean belongsTonn(long id, long start, long end, long n) { // t E ]beg,end[
    	long nx = 0;
    	long ny = MathMisc.modMinus(end, start, n);
    	long nid = MathMisc.modMinus(id, start, n);
    	return (start ==  end && id != start) || (nid > nx && nid < ny);
    }

//----------------------------------------------------------------------------------
	/**
	 * Checks if 'id' is placed in (start, end].
	 * @param id Specifies the id to check its availability in interval.
	 * @param start Specifies the beginning of interval.
	 * @param end Specifies the end of interval.
	 * @param n Specifies the maximum number of IDs in ID space.
	 * @return 'true' if 'id' placed in (start, end], otherwise returns 'false'.
	 */
    public static boolean belongsToI(long id, long start, long end, long n) { // t E [beg,end[
    	long nx = 0;
    	long ny = MathMisc.modMinus(end, start, n);
    	long nid = MathMisc.modMinus(id, start, n);
    	return start == end || (nid >= nx && nid < ny);
    }

//----------------------------------------------------------------------------------
	/**
	 * Checks if 'id' is placed in [start, end].
	 * @param id Specifies the id to check its availability in interval.
	 * @param start Specifies the beginning of interval.
	 * @param end Specifies the end of interval.
	 * @param n Specifies the maximum number of IDs in ID space.
	 * @return 'true' if 'id' placed in [start, end], otherwise returns 'false'.
	 */
    public static boolean belongsToII(long id, long start, long end, long n) { // t E [beg,end]
    	long nx = 0;
    	long ny = MathMisc.modMinus(end, start, n);
    	long nid = MathMisc.modMinus(id, start, n);
    	return ((start == end) && (end == id)) || (nid >= nx && nid <= ny);
    }

//----------------------------------------------------------------------------------
	/**
	 * Return the clockwise distance between two IDs.
	 * @param from Specifies the first ID.
	 * @param to Specifies the second ID.
	 * @param n Specifies the maximum number of IDs in ID space.
	 * @return The clockwise distance between 'from' to 'to'.
	 */
    public static long distanceClockWise(long from, long to, long n) {
    	return modMinus(to, from, n);
    }

//----------------------------------------------------------------------------------
	/**
	 * Return the anti clockwise distance between two IDs.
	 * @param from Specifies the first ID.
	 * @param to Specifies the second ID.
	 * @param n Specifies the maximum number of IDs in ID space.
	 * @return The anti clockwise distance between 'from' to 'to'.
	 */
    public static long distanceAntiClockWise(long from, long to, long n) {
    	return modMinus(from, to, n);
    }

//----------------------------------------------------------------------------------
    private static long modMinus(long x, long y, long n) {
    	return (n + x - y ) % n;
    }
}
