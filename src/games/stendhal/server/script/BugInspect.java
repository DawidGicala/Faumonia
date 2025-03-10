/* $Id: BugInspect.java,v 1.30 2012/05/30 18:50:04 kiheru Exp $ */
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
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.Task;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;

import java.util.HashSet;
import java.util.List;

import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

import org.apache.log4j.Logger;

/**
 * Deep inspects a player and all his/her items.
 * 
 * @author hendrik
 */
public class BugInspect extends ScriptImpl implements TurnListener {
	private static Logger logger = Logger.getLogger(BugInspect.class);
	private final HashSet<String> seen = new HashSet<String>();
	private boolean keepRunning = true;

	@Override
	public void execute(final Player admin, final List<String> args) {
		super.execute(admin, args);
		SingletonRepository.getTurnNotifier().notifyInTurns(6, this);
		keepRunning = true;
		seen.clear();
	}

	public void onTurnReached(final int currentTurn) {
		SingletonRepository.getRuleProcessor().getOnlinePlayers().forAllPlayersExecute(
				
			new Task<Player>() {

			public void execute(final Player player) {
				if (!seen.contains(player.getName())) {

					seen.add(player.getName());

					final StringBuilder sb = new StringBuilder();
					sb.append("Sprawdzam " + player.getName() + "\n");
					boolean caught = false;
					boolean warn = false;

					// inspect slots
					for (final RPSlot slot : player.slots()) {
						if ("!buddy".equals(slot.getName()) 
							|| "!ignore".equals(slot.getName())
							|| "!kills".equals(slot.getName()) 
							|| "!quests".equals(slot.getName())) {
							continue;
						}
						sb.append("\nSlot " + slot.getName() + ": \n");

						// list objects
						for (final RPObject object : slot) {
							if (object instanceof StackableItem) {
								final StackableItem item = (StackableItem) object;
								if (!"money".equals(item.getName()) && (item.getQuantity() > 10000)) {
									caught = true;
								}
								if ("money".equals(item.getName()) && (item.getQuantity() > 100000000)) {
									caught = true;
								}
								if (!"money".equals(item.getName()) && (item.getQuantity() > 7000)) {
									warn = true;
								}
								if ("money".equals(item.getName()) && (item.getQuantity() > 1000000)) {
									warn = true;
								}
							}
							sb.append("   " + object + "\n");
						}
					}

					String message = player.getName() + " posiada sporą ilość przedmiotów";
					if (caught) {

						new GameEvent("bug inspect", "jail", player.getName(), Integer.toString(-1), "possible bug abuse").raise();
						SingletonRepository.getJail().imprison(player.getName(), player, -1, "prawdopodobnie luka w grze");
						player.sendPrivateText(NotificationType.SUPPORT, "Użyj /support aby porozmawiać z najemnikiem o ilości twoich przedmiotów, które mogą być wynikiem luki w grze.");
						player.notifyWorldAboutChanges();

						message = "aresztowano " + player.getName() + " z powodu dużej ilości przedmiotów";
					}

					if (warn || caught) {

						new GameEvent("bug inspect", "support", message).raise();
						SingletonRepository.getRuleProcessor().sendMessageToSupporters("bug inspect", message);
						logger.warn("User with large amout of items: " + message + "\r\n" + sb.toString());
					}
				}
			}
		});

		if (keepRunning) {
			SingletonRepository.getTurnNotifier().notifyInTurns(6, this);
		}
	}

	@Override
	public void unload(final Player admin, final List<String> args) {
		super.unload(admin, args);
		keepRunning = false;
	}

}
