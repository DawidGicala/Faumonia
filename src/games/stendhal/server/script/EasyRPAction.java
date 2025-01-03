/* $Id: EasyRPAction.java,v 1.10 2010/09/19 02:36:26 nhnb Exp $ */
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

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;

import java.util.List;

import marauroa.common.game.RPAction;

/**
 * For testing purposes.
 * 
 * /script EasyRPAction.class type moveto x 1 y 1
 * 
 */

public class EasyRPAction extends ScriptImpl {

	@Override
	public void execute(final Player admin, final List<String> args) {
		super.execute(admin, args);

		if ((args.size() == 0) || (args.size() % 2 != 0)) {
			admin.sendPrivateText("/script EasyRPAction.class <klucz1> <wartość1> [<klucz2> <wartość2>] ...");
			return;
		}

		final RPAction action = new RPAction();

		for (int i = 0; i < (args.size() / 2); i++) {
			action.put(args.get(i * 2), args.get(i * 2 + 1));
		}

		SingletonRepository.getRuleProcessor().execute(admin, action);
	}

}
