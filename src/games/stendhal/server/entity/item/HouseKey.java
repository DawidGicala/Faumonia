/* $Id: HouseKey.java,v 1.7 2011/04/02 15:44:18 kymara Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.item;

import games.stendhal.common.grammar.Grammar;

import java.util.Map;

/**
 * A key that matches if the identifier and lock number are the right. 
 */
public class HouseKey extends Item {
	private static String[] imageNames = {
			"purple",
			"turquoise",
			"yellow",
			"lime",
			"pink",
			"red",
			"orange",
			"navy",
			"brown",
			"green",
			"grey",
			"lilac"
	};
	
	public HouseKey(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
		
		setInfoString("nowhere;0;");
		setPersistent(true);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param key the key to be copied.
	 */
	public HouseKey(final HouseKey key) {
		super(key);
	}
	
	@Override
	public String describe() {
		final String[] info = getInfoString().split(";", -1);
		if (info[2].length() > 0) {
			return "Oto klucz dla " + Grammar.suffix_s(info[2]) + " do zakupionego w " + info[0] + ".";
		} else {
			return "Oto klucz do " + info[0] + ".";
		}
	}
	/* useful so that the list of thems lost on death is more specific than just 'house key' */
	@Override
	public String getName() {
		final String[] info = getInfoString().split(";", -1);
		if (info[2].length() > 0) {
			return Grammar.suffix_s(info[2]) + " klucz do drzwi";
		} else {
			return  info[0] + " klucz";
		}
	}
	
	/**
	 * Set the parameters of the key.
	 * 
	 * @param id the portal identifier
	 * @param lockNumber the number of the lock
	 * @param owner the owner of the house
	 */
	public void setup(final String id, final int lockNumber, String owner) {
		if (owner == null) {
			owner = "";
		}
		setInfoString(id + ";" + lockNumber + ";" + owner);
		chooseImage();
	}
	
	/**
	 * Check if the key matches a portal.
	 * 
	 * @param houseId identifier of the portal
	 * @param number number of the lock
	 * @return true if the key fits the lock, false otherwise
	 */
	public boolean matches(final String houseId, final int number) {
		final String[] info = getInfoString().split(";");
		
		int keyNumber = Integer.parseInt(info[1]);
		return (info[0].equals(houseId) && keyNumber == number);
	}
	
	/**
	 * Choose an image for the key, depending on door identifier and lock number.
	 * Ignores the owner, even if it's set.
	 */
	private void chooseImage() {
		final String[] info = getInfoString().split(";");
		
		put("subclass", imageNames[Math.abs((info[0].hashCode() + info[1].hashCode()) % imageNames.length)]);
	}
}
