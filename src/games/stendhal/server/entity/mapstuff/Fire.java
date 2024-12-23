/* $Id: Fire.java,v 1.7 2010/09/01 07:26:46 nhnb Exp $ */
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
package games.stendhal.server.entity.mapstuff;

import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import marauroa.common.game.RPClass;

/**
 * Burning fire.
 */
public class Fire extends Entity implements UseListener {
	public static void generateRPClass() {
		final RPClass fire = new RPClass("fire");
		fire.isA("entity");
	}

	/**
	 * creates a new fire.
	 * 
	 * @param width
	 *            width in grid units
	 * @param height
	 *            height in grid units
	 */
	public Fire(final int width, final int height) {
		setDescription("Widzisz błyskające światło. Kusi Cię, aby je dotknąć.");
		setRPClass("fire");
		put("type", "fire");

		setSize(width, height);
		setResistance(100);
	}

	public boolean onUsed(final RPEntity user) {
		user.sendPrivateText("To nie zbyt dobry pomysł!");
		return true;
	}
}
