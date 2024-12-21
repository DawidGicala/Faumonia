/***************************************************************************
 *                   (C) Copyright 2003-2008 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game;

/**
 * This exception is thrown when an action lacks of an important attribute like:
 * <ul>
 * <li>sourceid
 * <li>zoneid
 * </ul>
 *
 * @author miguel
 *
 */
public class ActionInvalidException extends Exception {

	private static final long serialVersionUID = -2287105367089095987L;

	/**
	 * Constructor
	 *
	 * @param attribute missing attribute
	 */
	public ActionInvalidException(String attribute) {
		super("Nieprawid\u0142owa akcja: Brak obowi\u0105zuj\u0105cego atrybutu [" + attribute + "]");
	}
}
