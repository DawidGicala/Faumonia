/* $Id: HouseSellerGreetingAction.java,v 1.4 2011/05/01 19:50:06 martinfuchs Exp $ */
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
package games.stendhal.server.maps.quests.houses;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/** House owners are offered the chance to buy a spare key when the seller greets them. Others are just greeted with their name. */
final class HouseSellerGreetingAction extends HouseChatAction implements ChatAction {
	protected HouseSellerGreetingAction(final String questslot) {
		super(questslot);
	}

	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
		String reply = "";
		
		if (HouseUtilities.playerOwnsHouse(player)) {
			reply = " Za "
				+ HouseChatAction.COST_OF_SPARE_KEY
				+ " money możesz dorobić zapasowy klucz do swojego domu. Czy chcesz dorobić?";
			raiser.setCurrentState(ConversationStates.QUESTION_1);
		} else if (player.hasQuest(questslot)) {
			// the player has lost the house. clear the slot so that he can buy a new one if he wants
			player.removeQuest(questslot);
		}
		
		raiser.say("Witaj " + player.getTitle() + "." + reply);
	}
	
}
