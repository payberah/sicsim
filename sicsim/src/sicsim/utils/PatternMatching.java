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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sicsim.types.NodeId;

/**
 * Class to parse the key, value files and retrieve the values for specified keys.
 */
public class PatternMatching {
	public static String splitter = new String("-------------------------"); 
	public static String peerSplitter = new String("========================="); 
	public static String subSplitter = new String(">>> ");
	public static String localSplitter = new String("[LOCAL]\n");
	public static String scenarioSplitter = new String("---");
	
	private static String numValue = new String ("([\\s]*[\\-]?[\\d]+)");
	private static String floatValue = new String ("([\\s]*[\\-]?[\\d]+[.][\\d]+)");
	private static String nodeValue = new String("([\\s]*([\\-]?[\\d]+[@][\\-]?[\\d]+))");
	private static String strValue = new String("([\\s]+.*)");
	
//----------------------------------------------------------------------------------
	private static String generateNumPattern(String key) {
		return new String ("((" + key + ")" + PatternMatching.numValue + ")");
	}

//----------------------------------------------------------------------------------
	private static String generateFloatPattern(String key) {
		return new String ("((" + key + ")" + PatternMatching.floatValue + ")");
	}

//----------------------------------------------------------------------------------
	private static String generateNodePattern(String key) {
		return new String ("((" + key + ")" + PatternMatching.nodeValue + ")");
	}
	
//----------------------------------------------------------------------------------
	private static String generateStrValuePattern(String key) {
		return new String ("((" + key + ")" + PatternMatching.strValue + ")");
	}

	
//----------------------------------------------------------------------------------
	/**
	 * Returns the result of searching key in input string as an integer number.
	 * @param searchIn Specifies the string, which is being searched.
	 * @param key Specifies the key.
	 * @return the value of 'key' in 'searchIn'. If it can not find 'key' returns -1.
	 */
	public static int getIntValue(String searchIn, String key) {
		int num = -1;
		Pattern keyPattern = Pattern.compile(PatternMatching.generateNumPattern(key));
		Pattern valuePattern = Pattern.compile(PatternMatching.numValue);
		Matcher keyMatcher = keyPattern.matcher(searchIn);
		
		if (keyMatcher.find()) {
			Matcher valueMatcher = valuePattern.matcher(keyMatcher.group());
			if (valueMatcher.find())
				num = Integer.parseInt(valueMatcher.group().trim());
		}

		return num;
	}

//----------------------------------------------------------------------------------
	/**
	 * Returns the result of searching key in input string as a long number.
	 * @param searchIn Specifies the string, which is being searched.
	 * @param key Specifies the key.
	 * @return the value of 'key' in 'searchIn'. If it can not find 'key' returns -1.
	 */
	public static long getLongValue(String searchIn, String key) {
		long num = -1;
		Pattern keyPattern = Pattern.compile(PatternMatching.generateNumPattern(key));
		Pattern valuePattern = Pattern.compile(PatternMatching.numValue);
		Matcher keyMatcher = keyPattern.matcher(searchIn);
		
		if (keyMatcher.find()) {
			Matcher valueMatcher = valuePattern.matcher(keyMatcher.group());
			if (valueMatcher.find())
				num = Long.parseLong(valueMatcher.group().trim());
		}

		return num;
	}

//----------------------------------------------------------------------------------
	/**
	 * Returns the result of searching key in input string as a double number.
	 * @param searchIn Specifies the string, which is being searched.
	 * @param key Specifies the key.
	 * @return the value of 'key' in 'searchIn'. If it can not find 'key' returns -1.
	 */
	public static double getDoubleValue(String searchIn, String key) {
		double num = -1;
		Pattern keyPattern = Pattern.compile(PatternMatching.generateFloatPattern(key));
		Pattern valuePattern = Pattern.compile(PatternMatching.floatValue);
		Matcher keyMatcher = keyPattern.matcher(searchIn);
		
		if (keyMatcher.find()) {
			Matcher valueMatcher = valuePattern.matcher(keyMatcher.group());
			if (valueMatcher.find())
				num = Double.parseDouble(valueMatcher.group().trim());
		}

		return num;
	}

//----------------------------------------------------------------------------------
	/**
	 * Returns the result of searching key in input string as a NodeId format "id@ip".
	 * @param searchIn Specifies the string, which is being searched.
	 * @param key Specifies the key.
	 * @return the value of 'key' in 'searchIn' in format "id@ip". If it can not find 'key' returns null.
	 */
	public static NodeId getNodeValue(String searchIn, String key) {
		NodeId nodeId = null;
		Pattern keyPattern = Pattern.compile(PatternMatching.generateNodePattern(key));
		Pattern valuePattern = Pattern.compile(PatternMatching.nodeValue);
		Matcher keyMatcher = keyPattern.matcher(searchIn);
		
		if (keyMatcher.find()) {
			Matcher valueMatcher = valuePattern.matcher(keyMatcher.group());
			if (valueMatcher.find())
				nodeId = new NodeId(valueMatcher.group().trim());
		}

		return nodeId;
	}

//----------------------------------------------------------------------------------
	/**
	 * Returns the result of searching key in input string as a string.
	 * @param searchIn Specifies the string, which is being searched.
	 * @param key Specifies the key.
	 * @return the value of 'key' in 'searchIn' in string format. If it can not find 'key' returns null.
	 */
	public static String getStrValue(String searchIn, String key) {
		String str = null;
		Pattern keyPattern = Pattern.compile(PatternMatching.generateStrValuePattern(key));
		Pattern valuePattern = Pattern.compile(PatternMatching.strValue);
		Matcher keyMatcher = keyPattern.matcher(searchIn);
		
		if (keyMatcher.find()) {
			Matcher valueMatcher = valuePattern.matcher(keyMatcher.group());
			if (valueMatcher.find())
				str = new String(valueMatcher.group().trim());
		}

		return str;
	}

//----------------------------------------------------------------------------------
	/**
	 * Removes the comments in input, and returns the result. Comments are shown by # in strings.
	 * @param input Specifies the input string that may have some comments on it.
	 * @return the string that its comments have been removed.
	 */
	public static String removeComments(String input) {
		String str = new String();
		String[] parts = input.split("\n");
		for (int i = 0; i < parts.length; i++) {
			if (!parts[i].startsWith("#")) {
				if (parts[i].indexOf("#") == -1)
					str += parts[i] + "\n";
				else
					str += parts[i].substring(0, parts[i].indexOf("#")) + "\n";
			}
		}
		
		return str;
	}

}
