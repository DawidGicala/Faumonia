/* $Id: Constants.java,v 1.9 2010/09/19 02:20:44 nhnb Exp $ */
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
package games.stendhal.common;

/**
 * Constants about slots
 */
public final class Constants {
	/**
	 * All the slots considered to be "with" the entity. Listed in priority
	 * order (i.e. bag first).
	 */
	// TODO: let the slots decide that themselves
	public static final String[] CARRYING_SLOTS = { "bag", "neck", "head", "cloak",
			"lhand", "armor", "rhand", "finger", "legs", "keyring", "medal", 
			"fingerb", "feet", "glove", "money" };

	public static final String[] CARRYING_SLOTS_BANKS = { "bag", "neck", "head", "cloak",
			"lhand", "armor", "rhand", "finger", "legs", "keyring",
			"fingerb", "feet", "glove", "money", "bank", "bank_ados", "medal",
			"bank_fado", "bank_nalwor", "bank_tsoh", "bank_zakopane",
			"zaras_chest_ados" };
}
