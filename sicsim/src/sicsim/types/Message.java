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

public class Message {
	public String data;
	public String type;
	
//----------------------------------------------------------------------------------
	public Message() {
	}
	
//----------------------------------------------------------------------------------
	public Message(String type, String data) {
		this.data = data;
		this.type = type;
	}
	
//----------------------------------------------------------------------------------
	public String toString() {
		return new String(">msg_type: " + this.type + " >msg_data: " + ((this.data != null) ? this.data : ""));

	}

//----------------------------------------------------------------------------------
	public static Message restore(String str) {
		Message data = new Message();
		
		String[] parts = str.split(">");
		data.type = PatternMatching.getStrValue(parts[1], "msg_type:");
		data.data = PatternMatching.getStrValue(parts[2], "msg_data:");
		return data;
	}
}
