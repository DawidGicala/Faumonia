/* $Id: TradeCenterMarketSlot.java,v 1.4 2011/01/12 21:58:24 nhnb Exp $ */
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

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * the slot of the trade center in which all the offered items are stored
 * 
 * @author hendrik
 */
public class TradeCenterMarketSlot extends EntitySlot {

	/**
	 * Creates a new TradeCenterMarketSlot.
	 * 
	 */
	public TradeCenterMarketSlot() {
		super();
	}

	/**
	 * Creates a new TradeCenterMarketSlot.
	 * 
	 * @param name
	 *            name of slot
	 */
	public TradeCenterMarketSlot(final String name) {
		super(name, name);
	}

	@Override
	public boolean isReachableForTakingThingsOutOfBy(final Entity entity) {
		if (!(entity instanceof SpeakerNPC)) {
			setErrorMessage("Tylko menedżerowie handlu mają dostęp do " + getName());
			return false;
		}
		return true;
	}

	@Override
	public boolean isReachableForThrowingThingsIntoBy(final Entity entity) {
		return isReachableForTakingThingsOutOfBy(entity);
	}

	@Override
	public boolean isItemSlot() {
		return true;
	}

	@Override
	public boolean isTargetBoundCheckRequired() {
		return true;
	}


	/**
	 * gets the type of the slot ("slot", "ground", "market")
	 *
	 * @return slot type
	 */
	@Override
	public String getSlotType() {
		return "marget";
	}
}
