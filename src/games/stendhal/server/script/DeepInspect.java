/* $Id: DeepInspect.java,v 1.17 2011/08/21 07:33:02 yoriy Exp $ */
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

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.server.game.db.CharacterDAO;
import marauroa.server.game.db.DAORegister;

/**
 * Deep inspects a player and all his/her items.
 * 
 * @author hendrik
 */
public class DeepInspect extends ScriptImpl {

	@Override
	public void execute(final Player admin, final List<String> args) {
		super.execute(admin, args);
		if ((args.size() != 2) || (!(args.get(0).equals("character") || args.get(0).equals("username")))) {
			admin.sendPrivateText("użyj: {\"postać\" | \"nazwaużytkownika\"} <nazwa>.");
			admin.sendPrivateText("postać zrobi inspekcję dostępnej w grze postaci.");
			admin.sendPrivateText("nazwa użytkownika zrobi inspekcję wszystkim postaciom należącym do tego konta czyli tym, które są powiązane w bazie danych.");
			return;
		}
		if (args.get(0).equals("character")) {
			inspectOnline(admin, args.get(1));
		} else {
			inspectOffline(admin, args.get(1));
		}
	}

	/**
	 * inspect an online character
	 *
	 * @param admin  Inspector
	 * @param character name of online character to inspect
	 */
	private void inspectOnline(final Player admin, final String charname) {
		Player player = SingletonRepository.getRuleProcessor().getPlayer(charname);
		if (player == null) {
			admin.sendPrivateText(NotificationType.ERROR, "Nie ma w grze postaci zwanej " + charname + ".");
			return;
		}
		inspect(admin, player);
	}

	/**
	 * inspects offline players
	 *
	 * @param admin  Inspector
	 * @param username username who's characters are being inspected
	 */
	private void inspectOffline(final Player admin, final String username) {
		try {
			Map<String, RPObject> characters = DAORegister.get().get(CharacterDAO.class).loadAllActiveCharacters(username);
			for (RPObject object : characters.values()) {
				inspect(admin, object);
			}
		} catch (SQLException e) {
			admin.sendPrivateText(NotificationType.ERROR, e.toString());
		} catch (IOException e) {
			admin.sendPrivateText(NotificationType.ERROR, e.toString());
		}

	}

	/**
	 * Inspects a player
	 * 
	 * @param admin  Inspector
	 * @param player player being inspected
	 */
	private void inspect(final Player admin, final RPObject player) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Badam " + player.get("name") + "\n");

		for (final String value : player) {
			sb.append(value + ": " + player.get(value) + "\n");
		}

		admin.sendPrivateText(sb.toString());
		sb.setLength(0);

		// inspect slots
		for (final RPSlot slot : player.slots()) {
			// don't return buddy-list for privacy reasons
			if (slot.getName().equals("!buddy")
					|| slot.getName().equals("!ignore")) {
				continue;
			}
			sb.append("\nSlot " + slot.getName() + ": \n");

			// list objects
			for (final RPObject object : slot) {
				sb.append("   " + object + "\n");
			}

			admin.sendPrivateText(sb.toString());
			sb.setLength(0);
		}
	}
}
