/* $Id: QuestAchievementFactory.java,v 1.14 2011/08/02 22:19:43 nhnb Exp $ */
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
package games.stendhal.server.core.rp.achievement.factory;

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.core.rp.achievement.condition.QuestCountCompletedCondition;
import games.stendhal.server.core.rp.achievement.condition.QuestsInRegionCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateGreaterThanCondition;
import games.stendhal.server.maps.Region;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
/**
 * Factory for quest achievements
 *  
 * @author madmetzger
 */
public class QuestAchievementFactory extends AbstractAchievementFactory {
	
	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> questAchievements = new LinkedList<Achievement>();
		
		//elf princess quest achievement
		//questAchievements.add(createAchievement("quest.special.elf_princess.0025", "Kasanowa Faiumoni", "Ukończył zadanie u księżniczki elfów 25 razy", 
											//	Achievement.MEDIUM_BASE_SCORE, true, new QuestStateGreaterThanCondition("elf_princess", 2, 24)));

		//Maze
		//questAchievements.add(createAchievement("quest.special.maze", "Kierunkowskaz", "Ukończył labirynt", 
			//	Achievement.EASY_BASE_SCORE, true, new QuestStateGreaterThanCondition("maze", 2, 0)));
		//questAchievements.add(createAchievement("quest.deathmatch", "Bohater Deathmatcha", "Zdobył 100,000 punktów na deathmatchu",
			//	Achievement.MEDIUM_BASE_SCORE, true, new QuestStateGreaterThanCondition("deathmatch_score", 0, 100000)));

		// Ados Deathmatch
		// disabled. Currently the wrong index is being checked (it would be index 6) 
		// and as per bug report https://sourceforge.net/tracker/?func=detail&aid=3148365&group_id=1111&atid=101111 the count is not saved anyway
		//questAchievements.add(createAchievement("quest.special.dm.025", "Gladiator", "Walczył na 25 Deathmatchach",
		//		Achievement.HARD_BASE_SCORE, true, new QuestStateGreaterThanCondition("deathmatch", 1, 24)));
		
		// have completed all quests in Semos City?
		//questAchievements.add(createAchievement("quest.special.semos", "Przyjaciel Semos", "Ukończył wszystkie zadania w mieście Semos",
				//Achievement.MEDIUM_BASE_SCORE, true, new QuestsInRegionCompletedCondition(Region.SEMOS_CITY)));
		
		// have completed all quests in Ados City?
		//questAchievements.add(createAchievement("quest.special.ados", "Przyjaciel Ados", "Ukończył wszystkie zadania w mieście Ados",
			//	Achievement.MEDIUM_BASE_SCORE, true, new QuestsInRegionCompletedCondition(Region.ADOS_CITY)));
		
		// complete nearly all the quests in the game?
		//questAchievements.add(createAchievement("quest.count.80", "Pogromca zadań","Ukończyl conajmniej 80 zadań",
			//	Achievement.MEDIUM_BASE_SCORE, true, new QuestCountCompletedCondition(80)));

		return questAchievements;
	}

	@Override
	protected Category getCategory() {
		return Category.QUEST;
	}

}
