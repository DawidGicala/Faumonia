/* $Id: FishInspect.java,v 1.6 2012/05/30 18:50:04 kiheru Exp $ */
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
 * to look for fish
 * @author hendrik/kymara
 */
public class FishInspect extends ScriptImpl implements TurnListener {
	private static Logger logger = Logger.getLogger(FishInspect.class);
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
					sb.append("Badam " + player.getName() + "\n");
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
								if ("dorsz".equals(item.getName()) && (item.getQuantity() > 300)) {
									caught = true;
								} else if ("pstrąg".equals(item.getName()) && (item.getQuantity() > 300)) {
									caught = true;
								} else if ("makrela".equals(item.getName()) && (item.getQuantity() > 300)) {
									caught = true;
								} else if ("okoń".equals(item.getName()) && (item.getQuantity() > 300)) {
									caught = true;
								} else if ("płotka".equals(item.getName()) && (item.getQuantity() > 300)) {
									caught = true;
								} else if ("pokolec".equals(item.getName()) && (item.getQuantity() > 300)) {
									caught = true;
								} else if ("błazenek".equals(item.getName()) && (item.getQuantity() > 300)) {
									caught = true;
								} else if ("palia alpejska".equals(item.getName()) && (item.getQuantity() > 300)) {
									caught = true;
								}
							}
							sb.append("   " + object + "\n");
						}
					}

					String message = player.getName() + " has a large amount of items";
					if (caught) {

						new GameEvent("fish inspect", "jail", player.getName(), Integer.toString(-1), "possible macro use to get fish").raise();
						SingletonRepository.getJail().imprison(player.getName(), player, -1, "possible macro use to get fish");
						player.sendPrivateText(NotificationType.SUPPORT, "Użyj /support, aby porozmawiać z administratorem o zbyt dużej ilości ryb, które mogą pochodzić z nielegalnego zródła.");
						player.notifyWorldAboutChanges();

						message = "aresztowano " + player.getName() + " ze względu na zbyt dużą ilość ryb";
					}

					if (warn || caught) {

						new GameEvent("fish inspect", "support", message).raise();
						SingletonRepository.getRuleProcessor().sendMessageToSupporters("fish inspect", message);
						logger.warn("User with large amount of fish: " + message + "\r\n" + sb.toString());
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
