/* $Id: ProgressStatusQueryAction.java,v 1.6 2013/01/06 16:24:01 nhnb Exp $ */
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
package games.stendhal.server.actions.query;

import static games.stendhal.common.constants.Actions.PROGRESS_STATUS;
import games.stendhal.server.actions.ActionListener;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.ProgressStatusEvent;

import java.util.Arrays;
import java.util.List;

import marauroa.common.game.RPAction;

/**
 * queries the server about the progress.
 *
 * @author hendrik
 */
public class ProgressStatusQueryAction implements ActionListener {
	

	/**
	 * registers this action.
	 */
	public static void register() {
		CommandCenter.register(PROGRESS_STATUS, new ProgressStatusQueryAction());
	}

	/**
	 * processes the requested action.
	 * 
	 * @param player the caller of the action
	 * @param action the action to be performed
	 */
	@Override
	public void onAction(final Player player, final RPAction action) {
		if (!action.has("progress_type")) {
			sendProgressTypes(player);
		} else if (!action.has("item")) {
			sendItemList(player, action.get("progress_type"));
		} else {
			sendDetails(player, action.get("progress_type"), action.get("item"));
		}

		player.notifyWorldAboutChanges();
	}

	/**
	 * sends a list of supported progress types
	 *
	 * @param player Player to sent the event to
	 */
	private void sendProgressTypes(Player player) {
		List<String> list = Arrays.asList("Otwarte zadania", "Ukończone zadania", "Produkcja");
		player.addEvent(new ProgressStatusEvent(list));
	}

	/**
	 * sends a list of items in the specified progress types
	 *
	 * @param player Player to sent the event to
	 * @param progressType the type of progress the player is interested in
	 */
	private void sendItemList(Player player, String progressType) {
		if (progressType.equals("Otwarte zadania")) {
			player.addEvent(new ProgressStatusEvent(progressType, 
				SingletonRepository.getStendhalQuestSystem().getOpenQuests(player)));
		} else if (progressType.equals("Ukończone zadania")) {
			player.addEvent(new ProgressStatusEvent(progressType, 
				SingletonRepository.getStendhalQuestSystem().getCompletedQuests(player)));
		} else if (progressType.equals("Produkcja")) {
			player.addEvent(new ProgressStatusEvent(progressType, 
					SingletonRepository.getProducerRegister().getWorkingProducerNames(player)));
		}
	}

	/**
	 * sends details about a selected item
	 *
	 * @param player Player to sent the event to
	 * @param progressType the type of progress the player is interested in
	 * @param item name of item to get details about
	 */
	private void sendDetails(Player player, String progressType, String item) {
		StendhalQuestSystem questSystem = SingletonRepository.getStendhalQuestSystem();
		if (progressType.equals("Otwarte zadania") || progressType.equals("Ukończone zadania")) {
			player.addEvent(new ProgressStatusEvent(progressType, item, 
				questSystem.getQuestDescription(player, item),
				questSystem.getQuestLevelWarning(player, item),
				questSystem.getQuestProgressDetails(player, item)));
		} else if (progressType.equals("Produkcja")) {
			player.addEvent(new ProgressStatusEvent(progressType, item, 
					SingletonRepository.getProducerRegister().getProductionDescription(player, item),
					SingletonRepository.getProducerRegister().getProductionDetails(player, item)));
		}
	}

}
