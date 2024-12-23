/* $Id: BailAction.java,v 1.18 2011/05/01 19:50:08 martinfuchs Exp $ */
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
package games.stendhal.server.maps.deathmatch;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.player.Player;

/**
 * handles "bail" trigger to free the player from deathmatch with a penalty.
 */
public class BailAction implements ChatAction {

	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
		final String questInfo = player.getQuest("deathmatch");
		if (questInfo == null) {
			raiser.say("Tchórz! Nawet nie #zacząłeś!");
			return;
		}

		final DeathmatchState deathmatchState = DeathmatchState.createFromQuestString(player.getQuest("deathmatch"));

		if (deathmatchState.getLifecycleState() != DeathmatchLifecycle.START) {
			raiser.say("Tchórz! Nawet nie #zaczęliśmy!");
			return;
		}

		deathmatchState.setLifecycleState(DeathmatchLifecycle.BAIL);
		new SetQuestAction("deathmatch", 0, deathmatchState.toQuestString()).fire(player, sentence, raiser);
		// Track the number of bails.
		new IncrementQuestAction("deathmatch", 7, 1).fire(player, sentence, raiser);

		// TODO: fix race condition until bail is processed in DeathmatchEngine
		final Item helmet = player.getFirstEquipped("zdobyczny hełm");
		if ((helmet != null) && helmet.has("def") && (helmet.getInt("def") > 1)) {
			raiser.say("Tchórz! Z przykrością informuje, że twój hełm został magicznie osłabiony.");
		} else {
			raiser.say("Tchórz! Nie jesteś tak doświadczony jak powinieneś.");
		}
		return;
	}
}
