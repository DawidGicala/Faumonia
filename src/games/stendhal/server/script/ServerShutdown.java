/* $Id: ServerShutdown.java,v 1.7 2012/03/26 19:42:39 nhnb Exp $ */
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

/**
 * Shuts down the server in a regular fashion. You should warn connected players
 * to logout if that is still possible.
 * 
 * If the server is started in a loop, it will come up again: while sleep 60; do
 * java -jar marauroa -c marauroa.ini -l; done
 * 
 * @author M. Fuchs
 */
public class ServerShutdown extends ScriptImpl {

	@Override
	public void execute(final Player admin, final List<String> args) {
		final String text = admin.getTitle()
				+ " rozpoczął zamykanie serwera.";

		SingletonRepository.getRuleProcessor().tellAllPlayers(NotificationType.SUPPORT, text);

		new Thread(
			new Runnable() {
    			public void run() {
    				//marauroad.finish() is already called using a JRE shutdown hook, so we don't need
    				// to call it here directly: 
    				//marauroad.getMarauroa().finish();

    				System.exit(0);
    			}
			}, "Server Shutdown").start();
	}

}
