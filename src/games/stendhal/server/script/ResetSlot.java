/* $Id: ResetSlot.java,v 1.4 2012/05/30 18:50:04 kiheru Exp $ */
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
package games.stendhal.server.script;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;

import java.util.List;

import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * Resets an RPSlot.
 * 
 * @author kymara
 */
public class ResetSlot extends ScriptImpl {

	@Override
	public void execute(final Player admin, final List<String> args) {
		super.execute(admin, args);

		// admin help
		if (args.size() < 2) {
			admin.sendPrivateText("Potrzebna jest nazwa wojownika oraz nazwa slotu jako parametr.");
			return;
		}

		// find the player and slot
		final Player player = SingletonRepository.getRuleProcessor().getPlayer(args.get(0));
		if (player == null) {
			admin.sendPrivateText("Nie ma takiego wojownika: " + args.get(0));
			return;
		}
		final RPSlot slot = player.getSlot(args.get(1));
		if (slot == null) {
			admin.sendPrivateText("Nie ma takiego slotu: " + args.get(1));
			return;
		}

		// remove old store object
		final RPObject rpObject = slot.iterator().next();
		slot.remove(rpObject.getID());

		// create new store object
		slot.add(new RPObject());

		// notify the player
		player.sendPrivateText(NotificationType.SUPPORT, 
				"Stan twojego " + args.get(1) + " został zresetowany przez "
				+ admin.getTitle());
	}
}
