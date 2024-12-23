/* $Id: SentenceAction.java,v 1.10 2013/01/06 16:24:06 nhnb Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.actions;
import static games.stendhal.common.constants.Actions.SENTENCE;
import static games.stendhal.common.constants.Actions.VALUE;
import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.entity.player.Player;

import java.io.UnsupportedEncodingException;

import marauroa.common.game.RPAction;

import org.apache.log4j.Logger;

/**
 * sets a sentences that is displayed on the character page of the website.
 */
public class SentenceAction implements ActionListener {
	private static Logger logger = Logger.getLogger(SentenceAction.class);

	public static void register() {
		CommandCenter.register(SENTENCE, new SentenceAction());
	}

	@Override
	public void onAction(final Player player, final RPAction action) {
		if (!action.has(VALUE)) {
			player.sendPrivateText(NotificationType.ERROR, "Proszę użyj /sentence <sentencja>");
			return;
		}

		String sentence = action.get(VALUE);
		try {
			if (sentence.getBytes("UTF-8").length > 250) {
				player.sendPrivateText(NotificationType.ERROR, "Twoja sentencja była zbyt długa");
				return;
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e, e);
		}

		new GameEvent(player.getName(), "sentence", Integer.toString(sentence.length()), sentence).raise();
		player.setSentence(sentence);
		player.sendPrivateText("Twoje sentencja została zmieniona na: " + action.get(VALUE));
	}
}
