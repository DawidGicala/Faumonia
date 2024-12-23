/* $Id: TellAllAction.java,v 1.14 2013/01/06 16:23:55 nhnb Exp $ */
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
package games.stendhal.server.actions.admin;

import static games.stendhal.common.constants.Actions.TELLALL;
import static games.stendhal.common.constants.Actions.TEXT;
import games.stendhal.common.NotificationType;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;

/**
 * shouts a message to all players as "administrator".
 */
public class TellAllAction extends AdministrationAction {

	public static void register() {
		CommandCenter.register(TELLALL, new TellAllAction(), 2);

	}

	@Override
	public void perform(final Player player, final RPAction action) {
		String sender = player.getName();
		if (action.has("sender") && (player.getName().equals("postman"))) {
			sender = action.get("sender");
		}

		if (action.has(TEXT)) {
			final String message = "Administrator #" + player.getTitle() + " OGŁASZA: " + action.get(TEXT);

			new GameEvent(sender, TELLALL, action.get(TEXT)).raise();

			SingletonRepository.getRuleProcessor().tellAllPlayers(NotificationType.SUPPORT, message);
		}
	}

}
