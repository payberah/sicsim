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

import java.util.Random;

/**
 * Class to get random numbers within an interval. 
 * The class is specifically designed to generate unique random numbers within a small interval.
 * For simulating p2p systems, this property is required to generate identifiers for nodes. 
 */
public class RandomSet {

	private final int size;
	private int pos = 0;
	private final int[] arr;
	private int rndPosAdd;
	private int rndPosRem;
	private Random random;
	
//----------------------------------------------------------------------------------
	public RandomSet(int s) {
		this.size = s;
		arr = new int[size];
		for (int i = 0; i < size; i++) 
			arr[i] = i;
		random = new Random();
	}
	
//----------------------------------------------------------------------------------
	public RandomSet(int begin, int end, long seed) {
		this.size = end - begin;
		arr = new int[size];
		for (int i = 0; i < size; i++) 
			arr[i] = i + begin;
		random = new Random(seed);
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Check if a number has already been generated
	 * @param v number to check
	 * @return true if already generated, false otherwise
	 */
	public boolean contains(int v) {
		int x;
		for (x = 0; x < this.pos && arr[x] != v; x++);
		return x == this.pos ? false : true;
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Get a uniformly distributed, unique random generated number
	 * @return the generated number
	 * @throws IndexOutOfBoundsException Thrown if the maximum number of numbers have already been generated.
	 */
	public int addInt() throws IndexOutOfBoundsException {
		if (pos == size) {
			throw new IndexOutOfBoundsException("Already generated " + size + " integers");
		}
		
		rndPosAdd = pos + random.nextInt(size - pos);
		final int tmp = arr[rndPosAdd];
		arr[rndPosAdd] = arr[pos];
		arr[pos] = tmp;
		pos++;
		
		return tmp;
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Adds an integer to the list of already generated numbers
	 * @param v	integer to add
	 * @throws Exception Either if maximum numbers of numbers has already been generated, 
	 * 			or the number specified has already been generated
	 * 			or the number is larger than the size
	 */
	public void addInt(int v) throws Exception {
		if(contains(v))
			throw new Exception("Already generated the number " + v);
		if(pos == size)
			throw new Exception("Already generated " + size + " integers");
		if(v > size)
			throw new Exception("Specified number "+ v + " is larger than the size " + size + ".");
		
		// so, 'v' exists on the right side of 'pos'
		// this implementation is not efficient, can be implemented more efficiently
		int x;
		for(x = pos; x < size && arr[x] != v; x++);
		
		if(x == size) // this is an extra check cauz this is not possible
			throw new Exception("Search for " + v + " exhausted in the available list of numbers!");
		
		// swap the found number/index
		final int tmp = arr[x];
		if(v != tmp) // this is an extra check cauz this is not possible
			throw new Exception("Specified number " + v + " doesn't match the index value " + tmp);
		arr[x] = arr[pos];
		arr[pos] = tmp;
		pos++;		
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Undo the generation of last random number. The previous operation called should be 'addInt()'
	 *
	 */
	public void undoLastAdd() { // INVARIANT: last operation must have been an addInt()
		pos--;
		final int tmp = arr[rndPosAdd];
		arr[rndPosAdd] = arr[pos];
		arr[pos] = tmp;
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Gets a uniformly distributed, random number from the already generated numbers, and makes this number available for future
	 * generation i.e. it is put back into the pool of random numbers that can be generated 
	 * @throws IndexOutOfBoundsException Thrown if no number has yet been generated
	 */
	public int removeInt() throws IndexOutOfBoundsException {
		if (pos == 0) {
			throw new IndexOutOfBoundsException("No random numbers generated yet");
		}
	
		rndPosRem = random.nextInt(pos);
		pos--;
		final int tmp = arr[rndPosRem];
		arr[rndPosRem] = arr[pos];
		arr[pos] = tmp;
		return tmp;
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Removes an integer to the list of already generated numbers, hence making it available for future
	 * generation i.e. it is put back into the pool of random numbers that can be generated 
	 * @param v	integer to remove
	 * @throws Exception Either if the number specified has not already been generated (same if no number has been generated yet)
	 * 			or the number is larger than the size
	 */
	public void removeInt(int v) throws Exception {
		if(!contains(v))
			throw new Exception("Haven't yet generated the number " + v);
		if(v > size)
			throw new Exception("Specified number "+ v + " is larger than the size " + size + ".");
		
		// so, 'v' exists on the left side of 'pos'
		// this implementation is not efficient, can be implemented more efficiently
		int x;
		for(x = pos - 1; x > -1 && arr[x] != v; x++);
		
		if(x < 0) // this is an extra check cauz this is not possible
			throw new Exception("Search for " + v + " exhausted in the list of already generated numbers!");
		
		// swap the found number/index
		pos--;
		final int tmp = arr[x];
		if(v != tmp) // this is an extra check cauz this is not possible
			throw new Exception("Specified number "+v+" doesn't match the index value " + tmp);
		
		arr[x] = arr[pos];
		arr[pos] = tmp;	
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Undo the putting back of the random number into the pool of available numbers. The previous operation called should be 'removeInt()'
	 */
	public void undoLastRemove() { // INVARIANT: last operation must have been an removeInt()
		final int tmp = arr[rndPosRem];
		arr[rndPosRem] = arr[pos];
		arr[pos] = tmp;
		pos++;
	}
	
//----------------------------------------------------------------------------------
	/**
	 * See a random number out of the numbers that have already been generated.
	 * @throws IndexOutOfBoundsException If no number has yet been generated
	 */
	public int peek() throws IndexOutOfBoundsException {
		if (pos == 0)
			throw new IndexOutOfBoundsException("Set empty, cannot peek");
		
		final int rndPos = random.nextInt(pos);
		return arr[rndPos];
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Get the number of 'numbers already generated'
	 */
	public int size() {
		return pos;
	}
}
