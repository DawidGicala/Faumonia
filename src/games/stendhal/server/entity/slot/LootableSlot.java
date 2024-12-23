/* $Id: LootableSlot.java,v 1.12 2012/02/26 12:17:09 kiheru Exp $ */
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
package games.stendhal.server.entity.slot;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.Entity;

/**
 * A lootable slot of some creature.
 * 
 * @author hendrik
 */
public class LootableSlot extends EntitySlot {
	private final Entity owner;

	/**
	 * creates a new lootable slot.
	 * 
	 * @param owner
	 *            owner of this Slot
	 */
	public LootableSlot(final Entity owner) {
		super("content", "content");
		this.owner = owner;
	}

	@Override
	public boolean isReachableForTakingThingsOutOfBy(final Entity entity) {
		setErrorMessage(Grammar.makeUpperCaseWord(((Entity)getOwner()).getDescriptionName(true)) + " jest zbyt daleko.");
		return entity.nextTo(owner);
	}

}
