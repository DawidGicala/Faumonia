/* $Id: OdlamekScroll.java,v 1.11 2010/11/25 20:58:54 kiheru Exp $ */
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
package games.stendhal.server.entity.item.scroll;

import games.stendhal.common.MathHelper;
import games.stendhal.server.core.events.DelayedPlayerTextSender;
import games.stendhal.server.entity.player.Player;

import java.util.Map;

/**
 * Represents the odlamek1 that takes the player to 7 kikareukin clouds,
 * after which it will teleport player to a random location in 6 kikareukin islands.
 */
public class OdlamekScroll extends TimedTeleportScroll {

	private static final long DELAY = 1 * MathHelper.MILLISECONDS_IN_ONE_HOUR;
	private static final int NEWTIME = 60;
	
	/**
	 * Creates a new timed marked OdlamekScroll scroll.
	 * 
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public OdlamekScroll(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param item
	 *            item to copy
	 */
	public OdlamekScroll(final OdlamekScroll item) {
		super(item);
	}

	@Override
	protected String getBeforeReturnMessage() {
		return "Czujesz jakby chmury już nie mdasdasdasdaogły wytrzymać pod Twoim ciężarem ... ";
	}

	@Override
	protected String getAfterReturnMessage() {
		return "Spadłeś przez dziurę w chmddddaurach na twardą ziemię.";
	}

	// Only let player use odlamek1 from 6 kika clouds
	// odlamek1s used more frequently than every 6 hours only last 5 minutes
	@Override
	protected boolean useTeleportScroll(final Player player) {
		if (!"0_semos_city".equals(player.getZone().getName())) {
			if ("-1_semos_crazy_dungeon_1".equals(player.getZone().getName())) {
				player.sendPrivateText("Inny balon ndasdadaie mógł wynieść cię wyżej.");
			} else {
				player.sendPrivateText("Balon próbował unieśćdadada cię wyżej, ale wysokość była zbyt niska, aby podnieść Ciebie. " 
									  + "Spróbuj przejść gdzdasdaieś, gdzie jest wyżej.");
			}
			return false; 
		} 
		long lastuse = -1;
		if (player.hasQuest("odlamek1")) {
			lastuse = Long.parseLong(player.getQuest("odlamek1"));		
		}
		
		player.setQuest("odlamek1", Long.toString(System.currentTimeMillis()));
		
		final long timeRemaining = (lastuse + DELAY) - System.currentTimeMillis();
		if (timeRemaining > 0) {
			// player used the odlamek1 within the last DELAY hours
			// so this use of odlamek1 is going to be shortened 
			// (the clouds can't take so much weight on them)
			// delay message for 1 turn for technical reasons
			new DelayedPlayerTextSender(player, "Chmury osłabły od ostatniego razu idasdadada nie utrzymają Ciebie zbyt długo.", 1);

			return super.useTeleportScroll(player, "-1_semos_crazy_dungeon_1", 31, 21, NEWTIME);
		}

		return super.useTeleportScroll(player);
	}
}
