/* $Id: AddBuddyAction.java,v 1.23 2013/01/06 16:24:02 nhnb Exp $ */
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
package games.stendhal.server.actions.buddy;

import static games.stendhal.common.constants.Actions.TARGET;
import games.stendhal.common.NotificationType;
import games.stendhal.server.actions.ActionListener;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.dbcommand.QueryCanonicalCharacterNamesCommand;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnListenerDecorator;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.Collection;

import marauroa.common.game.RPAction;
import marauroa.server.db.command.DBCommand;
import marauroa.server.db.command.DBCommandQueue;
import marauroa.server.db.command.ResultHandle;


/**
 * Adds someone to your buddy list.
 */
class AddBuddyAction implements ActionListener, TurnListener {
	
	private ResultHandle handle = new ResultHandle();
	
	/**
	 * Starts to handle a buddy action.
	 * 
	 * @param player
	 *            The player.
	 * @param action
	 *            The action.
	 */
	@Override
	public void onAction(final Player player, final RPAction action) {
		if (countBuddies(player) > 500) {
			player.sendPrivateText(NotificationType.ERROR, "Masz już zbyt dużo przyjaciół");
			return;
		}
		
		final String who = action.get(TARGET);
		
		DBCommand command = new QueryCanonicalCharacterNamesCommand(player, Arrays.asList(who));
		DBCommandQueue.get().enqueueAndAwaitResult(command, handle);
		TurnNotifier.get().notifyInTurns(0, new TurnListenerDecorator(this));
	}
	
	/**
	 * Completes handling the buddy action.
	 * 
	 * @param currentTurn ignored
	 */
	@Override
	public void onTurnReached(int currentTurn) {
		QueryCanonicalCharacterNamesCommand checkcommand = DBCommandQueue.get().getOneResult(QueryCanonicalCharacterNamesCommand.class, handle);
		
		if (checkcommand == null) {
			TurnNotifier.get().notifyInTurns(0, new TurnListenerDecorator(this));
			return;
		}

		Player player = checkcommand.getPlayer();

		Collection<String> queriedNames = checkcommand.getQueriedNames();
		String who = queriedNames.iterator().next(); // We know, we queried exactly one character.
		
		Collection<String> validNames = checkcommand.getValidNames();
		if (validNames.isEmpty()) {
				player.sendPrivateText(NotificationType.ERROR, "Nie znaleziono " + who + ".");
				return;
		} 
		
		// get the canonical name
		who = validNames.iterator().next();
		final Player buddy = SingletonRepository.getRuleProcessor().getPlayer(who);

		if (player.addBuddy(who, (buddy != null) && !buddy.isGhost())) {
			new GameEvent(player.getName(), "buddy", "add", who).raise();
			player.sendPrivateText(who + " został dodany do twojej listy przyjaciół.");
		} else {
			player.sendPrivateText(who + " już został dodany do twojej listy przyjaciół.");
		}

		new BuddyCleanup(player).cleanup();
	}

	/**
	 * counts the number of buddies this player has.
	 *
	 * @param player Player
	 * @return number of buddies
	 */
	private int countBuddies(Player player) {
		return player.countBuddies();
	}

}
