/* $Id: HouseBuying.java,v 1.63 2011/11/13 17:13:16 kymara Exp $ */
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
package games.stendhal.server.maps.quests;

import java.util.LinkedList;

import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.houses.HouseBuyingMain;

public class HouseBuying extends AbstractQuest {
	private static final String QUEST_SLOT = "house";
	private HouseBuyingMain quest;

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public void addToWorld() {
		super.addToWorld();

		quest = new HouseBuyingMain();
		quest.addToWorld();
		
		fillQuestInfo(
				"Kupno Domu",
				"Mieszkania można kupić w całej Faiumoni i ziemiach Polan.",
				false);
	}
	
	public LinkedList<String> getHistory(final Player player) {
		return quest.getHistory(player);
	}
	
	@Override
	public String getName() {
		return "HouseBuying";
	}
	
	@Override
	public int getMinLevel() {
		return 50;
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return quest.isCompleted(player);
	}

	@Override
	public String getNPCName() {
		return "Barrett Holmes";
	}
}
