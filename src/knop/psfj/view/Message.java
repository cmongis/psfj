/*
    This file is part of PSFj.

    PSFj is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PSFj is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PSFj.  If not, see <http://www.gnu.org/licenses/>. 
    
	Copyright 2013,2014 Cyril MONGIS, Patrick Theer, Michael Knop
	
 */
package knop.psfj.view;


// TODO: Auto-generated Javadoc
/**
 * The Class Message.
 */
public class Message {
	
	/** The data. */
	Object data;
	
	/** The origin. */
	Object origin;
	
	/** The name. */
	String name;
	
	/** The int data. */
	Integer intData;
	
	/** The string data. */
	String stringData;
	
	
	
	
	/**
	 * Instantiates a new message.
	 *
	 * @param origin the origin
	 * @param name the name
	 */
	public Message(Object origin, String name) {
		setName(name);
		setOrigin(origin);
	}
	
	/**
	 * Instantiates a new message.
	 *
	 * @param origin the origin
	 * @param name the name
	 * @param intData the int data
	 */
	public Message(Object origin, String name, Integer intData) {
		this(origin,name);
		setIntData(intData);
	}
	
	
	
	/**
	 * Instantiates a new message.
	 *
	 * @param origin the origin
	 * @param name the name
	 * @param stringData the string data
	 * @param intData the int data
	 * @param obj the obj
	 */
	public Message(Object origin, String name, String stringData, Integer intData, Object obj) {
		this(origin,name);
		setStringData(stringData);
		setIntData(intData);
		setData(obj);
	}
	
	/**
	 * Instantiates a new message.
	 *
	 * @param origin the origin
	 * @param name the name
	 * @param stringData the string data
	 * @param intData the int data
	 */
	public Message(Object origin, String name, String stringData, Integer intData) {
		this(origin,name,stringData,intData,null);
	}
	
	
	/**
	 * Instantiates a new message.
	 *
	 * @param origin the origin
	 * @param name the name
	 * @param stringData the string data
	 */
	public Message(Object origin, String name, String stringData) {
		this(origin,name);
		setStringData(stringData);
	}
	
	/**
	 * Instantiates a new message.
	 *
	 * @param origin the origin
	 * @param name the name
	 * @param obj the obj
	 */
	public Message(Object origin, String name, Object obj) {
		this(origin,name);
		setData(obj);
	}
	
	
	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	public Object getData() {
		return data;
	}
	
	/**
	 * Sets the data.
	 *
	 * @param data the new data
	 */
	public void setData(Object data) {
		this.data = data;
	}
	
	/**
	 * Gets the origin.
	 *
	 * @return the origin
	 */
	public Object getOrigin() {
		return origin;
	}
	
	/**
	 * Sets the origin.
	 *
	 * @param origin the new origin
	 */
	public void setOrigin(Object origin) {
		this.origin = origin;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the int data.
	 *
	 * @return the int data
	 */
	public Integer getIntData() {
		return intData;
	}
	
	/**
	 * Sets the int data.
	 *
	 * @param intData the new int data
	 */
	public void setIntData(Integer intData) {
		this.intData = intData;
	}
	
	/**
	 * Gets the string data.
	 *
	 * @return the string data
	 */
	public String getStringData() {
		return stringData;
	}
	
	/**
	 * Sets the string data.
	 *
	 * @param stringData the new string data
	 */
	public void setStringData(String stringData) {
		this.stringData = stringData;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		
		String result = "";
		result += "[Message]";
		result += "\nOrigin : "+getOrigin().getClass();
		result += "\nName : " +getName();
		result += "\nInt : " + getIntData();
		result += "\nString : " + getStringData();
		if(getData() != null) {
			result += "\nData : " + getData().getClass();
		}
		result += "\n";
		
		return result;
		
	}
	
	
	
	
}
