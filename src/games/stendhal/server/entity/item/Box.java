/* $Id: Box.java,v 1.25 2010/12/05 12:41:06 martinfuchs Exp $ */
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

import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;

import java.util.Map;

import marauroa.common.game.RPObject;

import org.apache.log4j.Logger;

/**
 * a box which can be unwrapped.
 * 
 * @author hendrik
 */
public class Box extends Item implements UseListener {

	private final static Logger logger = Logger.getLogger(Box.class);

	/**
	 * Creates a new box.
	 * 
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public Box(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * copy constructor.
	 * 
	 * @param item
	 *            item to copy
	 */
	public Box(final Box item) {
		super(item);
	}

	public boolean onUsed(final RPEntity user) {
		if (this.isContained()) {
			// We modify the base container if the object change.
			RPObject base = this.getContainer();

			while (base.isContained()) {
				base = base.getContainer();
			}

			if (!user.nextTo((Entity) base)) {
				logger.debug("Consumable item is too far");
				user.sendPrivateText("Przedmiot jest zbyt daleko.");
				return false;
			}
		} else {
			if (!user.nextTo(this)) {
				logger.debug("Consumable item is too far");
				user.sendPrivateText("Przedmiot jest zbyt daleko.");
				return false;
			}
		}

		if (user instanceof Player) {
			return useMe((Player)user);
		} else {
			logger.error("user is not a instance of Player but: " + user, new Throwable());
			return false;
		}
	}

	// this would be overridden in the subclass
	protected boolean useMe(final Player player) {
		logger.warn("A box that didn't have a use method failed to open.");
		player.sendPrivateText("Co za dziwne pudełko! Nie masz pojęcia jak je otworzyć.");
		return false;
	}

}
