/*
 * $Id: SkillerTask1Scroll,v 1.10 2020/11/28 15:27:33 davvids Exp $
 */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.item.scroll;

import games.stendhal.common.MathHelper;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

import java.util.Map;

/**
 * Represents the rainbow beans that takes the player to the dream 
 
 world zone,
 * after which it will teleport player to a random location in 0_semos_plains_s.
 */
public class SkillerTask1Scroll extends TimedTeleportScroll {
	
	private static final long DELAY = 5 * MathHelper.MILLISECONDS_IN_ONE_MINUTE;
	
	/**
	 * Creates a new timed marked SkillerTask1Scroll scroll.
	 * 
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public SkillerTask1Scroll(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param item
	 *            item to copy
	 */
	public SkillerTask1Scroll(final SkillerTask1Scroll item) {
		super(item);
	}
	
	@Override
	protected boolean useTeleportScroll(final Player player) {
		final String QUEST_SLOT = "skiller_nr_1";
		long lastuse = -1;
		if (player.hasQuest(QUEST_SLOT)) {
			final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
			if (tokens.length == 4) {
				// we stored a last time (or -1)
				lastuse = Long.parseLong(tokens[3]);
			}
			final long timeRemaining = (lastuse + DELAY) - System.currentTimeMillis();
			if (timeRemaining > 0) {
				// player used the beans within the last DELAY hours
				// so are not allowed to go yet. but don't reset the last time taken.
				// the private text doesn't get sent because events are lost on zone change. (marauroa bug)
				player.sendPrivateText("Pochorowałeś się od nadmiernego wysiłku.");
				final Item sick = SingletonRepository.getEntityManager().getItem("wymioty");
				player.getZone().add(sick);
				sick.setPosition(player.getX(), player.getY() + 1);
				// Success, so that the beans still gets used up, even though
				// the player was not teleported.
				return true;
			} else {
				// don't overwrite the last bought time from Pdiddi, this is in tokens[1]
				player.setQuest(QUEST_SLOT, "bought;" + tokens[1] + ";taken;" + System.currentTimeMillis());
				return super.useTeleportScroll(player);
			}
		} else {
			// players can only buy rainbow beans from Pdiddi who stores the time bought in quest slot
			// so if they didn't have the quest slot they got the beans ''illegally''
			player.sendPrivateText("Te kryształy są obrzydliwe. Robi ci sie niedobrze na sam widok.");
			this.removeOne();
			final Item sick = SingletonRepository.getEntityManager().getItem("wymioty");
			player.getZone().add(sick);
			sick.setPosition(player.getX(), player.getY() + 1);
			return false;
		}
	}
	
	@Override
	protected String getBeforeReturnMessage() {
		return "Czas chwilę odpocząć...";
	}

	@Override
	protected String getAfterReturnMessage() {
		return "Czas odpocząć."
				+ " Jeszcze tu wróce.";
	}
}
