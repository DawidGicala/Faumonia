/* $Id: TPPShowQuestState.java,v 1.3 2011/12/11 23:06:34 martinfuchs Exp $ */
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
import games.stendhal.server.maps.quests.ThePiedPiper;
import games.stendhal.server.util.TimeUtil;

import java.util.List;

/**
 * Showing what is current ThePiedPiper quest state, and when it will switch to next.
 * 
 * @author yoriy
 */
public class TPPShowQuestState extends ScriptImpl {

	@Override
	public void execute(final Player admin, final List<String> args) {
		
		StringBuilder sb = new StringBuilder();
		ThePiedPiper TPP = (ThePiedPiper) StendhalQuestSystem.get().getQuest("ThePiedPiper");
		sb.append("Stan zadania The Pied Piper:\n");
        sb.append("Fraza zadania: "+ThePiedPiper.getPhase().toString()+"\n");
		sb.append("Następna fraza : "+ThePiedPiper.getNextPhase(ThePiedPiper.getPhase()).toString()+"\n");
		int turns=TPP.getRemainingTurns();
		int seconds=TPP.getRemainingSeconds();
		sb.append("Pozostało rund: ");
		sb.append(turns);
		sb.append("\n");
		sb.append("Pozostało sekund: ");
        sb.append(seconds);
        sb.append(" ("+TimeUtil.timeUntil(seconds, true)+")");
		//sb.append("\n");
        admin.sendPrivateText(sb.toString());
	}
}
