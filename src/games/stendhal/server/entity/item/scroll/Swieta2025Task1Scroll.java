/*
 * $Id: Swieta2025Task1Scroll.java,v 1.10 2025/11/23 11:34:33 davvids Exp $
 */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
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

import games.stendhal.common.MathHelper;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

import java.util.Map;

/**
 * Represents the last minute that takes the player to the desert world zone,
 * after which it will teleport player to a random location in 0_semos_plains_s
 */
public class Swieta2025Task1Scroll extends DragonTimedTeleportScroll {

	/**
	 * Creates a new timed marked LastMinuteScroll scroll.
	 * 
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public Swieta2025Task1Scroll(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param item
	 *            item to copy
	 */
	public Swieta2025Task1Scroll(final Swieta2025Task1Scroll item) {
		super(item);
	}
	
	@Override
	protected boolean useTeleportScroll(final Player player) {
		return super.useTeleportScroll(player);
	}
	
	@Override
	protected String getBeforeReturnMessage() {
		return "Wkrótce musisz odpocząć...";
	}

	@Override
	protected String getAfterReturnMessage() {
		return "Znalazłeś się z powrotem w normalnym świecie";
	}
}
