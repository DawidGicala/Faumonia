/* $Id: SetWelcomeText.java,v 1.3 2010/11/25 21:40:29 martinfuchs Exp $ */
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
import games.stendhal.server.core.engine.StendhalRPRuleProcessor;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;

import java.util.List;

/**
 * sets the welcome text players see on login.
 *
 * @author hendrik
 */
public class SetWelcomeText extends ScriptImpl {

	@Override
	public void execute(Player admin, List<String> args) {
		if (args.isEmpty()) {
			admin.sendPrivateText(NotificationType.ERROR, "Brakuje argumentu.");
			return;
		}

		if (args.size() > 1) {
			admin.sendPrivateText(NotificationType.ERROR, "Zbyt dużo argumentów. Użyj cudzysłowów.");
			return;
		}

		StendhalRPRuleProcessor.setWelcomeMessage(args.get(0));
		admin.sendPrivateText("Ustaw tekst powitania na: " + args.get(0));
	}

}
