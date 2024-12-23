/* $Id: EmptyScroll.java,v 1.22 2010/04/11 12:50:42 kiheru Exp $
 /***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.item.scroll;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

import java.util.Map;

/**
 * Represents an empty/unmarked teleport scroll.
 */
public class EmptyScroll extends Scroll {

	// private static final Logger logger = Logger.getLogger(EmptyScroll.class);

	/**
	 * Creates a new empty scroll.
	 * 
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public EmptyScroll(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param item
	 *            item to copy
	 */
	public EmptyScroll(final EmptyScroll item) {
		super(item);
	}

	/**
	 * Use a [empty] scroll.
	 * 
	 * @param player
	 * @return always true
	 */
	@Override
	protected boolean useScroll(final Player player) {
		final StendhalRPZone zone = player.getZone();

		if (zone.isTeleportInAllowed(player.getX(), player.getY())) {
			final Item markedScroll = SingletonRepository.getEntityManager().getItem(
					"zwój zapisany");
			markedScroll.setInfoString(player.getID().getZoneID() + " "
					+ player.getX() + " " + player.getY());
			player.equipOrPutOnGround(markedScroll);
			return true;
		} else {
			player.sendPrivateText("Silna antymagiczna aura w tym obszarze blokuje działanie zwoju!");
			return false;
		}
	}
}
