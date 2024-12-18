/* $Id: EasterBunny.java,v 1.2 2012/04/28 06:38:34 nhnb Exp $ */
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

import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.MeetBunny;

import java.util.List;

/**
 * Starts or stops EasterBunny.
 * 
 * @author kymara
 */
public class EasterBunny extends ScriptImpl {

	@Override
	public void execute(final Player admin, final List<String> args) {
		if (args.size() != 1) {
			admin.sendPrivateText("/script EasterBunny.class {true|false}");
			return;
		}

		boolean enable = Boolean.parseBoolean(args.get(0));
		if (enable) {
			startEaster(admin);
		} else {
			stopEaster(admin);
		}
	}

	/**
	 * starts Easter
	 */
	private void startEaster(Player admin) {
		if (System.getProperty("stendhal.easterbunny") != null) {
			admin.sendPrivateText("Króliczek Wielkanocny jest aktywny.");
			return;
		}
		System.setProperty("stendhal.easterbunny", "true");
		StendhalQuestSystem.get().loadQuest(new MeetBunny());
	}

	/**
	 * ends Easter
	 */
	private void stopEaster(Player admin) {
		System.getProperties().remove("stendhal.easterbunny");
		StendhalQuestSystem.get().unloadQuest(MeetBunny.QUEST_NAME);
	}

}
