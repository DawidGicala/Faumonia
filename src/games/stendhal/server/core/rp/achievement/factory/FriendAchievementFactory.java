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
package games.stendhal.server.core.rp.achievement.factory;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.core.rp.achievement.condition.QuestWithPrefixCompletedCondition;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.player.Player;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
/**
 * Factory for quest achievements
 *  
 * @author kymara
 */
public class FriendAchievementFactory extends AbstractAchievementFactory {
	
	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> achievements = new LinkedList<Achievement>();
		
	    // TODO: add Pacifist achievement for not participating in pvp for 6 months or more (last_pvp_action_time)
		
		// Befriend Susi and complete quests for all children
	//	achievements.add(createAchievement("friend.quests.children", "Przyjaciel dzieci", "Ukończył zadania wszystkich dzieci",
											//	Achievement.MEDIUM_BASE_SCORE, true,
											//	new AndCondition(
														// Help Tad, Semos Town Hall (Medicine for Tad)
												//		new QuestCompletedCondition("introduce_players"),
														// Plink, Semos Plains North
												//		new QuestCompletedCondition("plinks_toy"),
														// Anna, in Ados
													//	new QuestCompletedCondition("toys_collector"),
														// Sally, Orril River
														// 'completed' doesn't work for Sally - return player.hasQuest(QUEST_SLOT) && !"start".equals(player.getQuest(QUEST_SLOT)) && !"rejected".equals(player.getQuest(QUEST_SLOT));
													//	new AndCondition(new QuestActiveCondition("campfire"), new QuestNotInStateCondition("campfire", "start")),
														// Annie, Kalavan city gardens
												//		new QuestStateStartsWithCondition("icecream_for_annie","eating;"),
														// Elisabeth, Kirdneh
												//		new QuestStateStartsWithCondition("chocolate_for_elisabeth","eating;"),
														// Jef, Kirdneh
													//	new QuestCompletedCondition("find_jefs_mom"),
														// Hughie, Ados farmhouse
												//		new AndCondition(new QuestActiveCondition("fishsoup_for_hughie"), new QuestNotInStateCondition("fishsoup_for_hughie", "start")))));
		
		// quests about finding people
	//	achievements.add(createAchievement("friend.quests.find", "Prywatny detektyw", "Znalazł wszystkie zagubione i ukrywające się aniołki",
											//	Achievement.HARD_BASE_SCORE, true,
											//	new AndCondition(
														// Rat Children (Agnus)
													//	new QuestCompletedCondition("find_rat_kids"),
														// Find Ghosts (Carena)
													//	new QuestCompletedCondition("find_ghosts"),
														// Meet Angels (any of the cherubs)
													//	new ChatCondition() {
														//	public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
														//		if (!player.hasQuest("seven_cherubs")) {
														//			return false;
														//		}
														//		final String npcDoneText = player.getQuest("seven_cherubs");
														//		final String[] done = npcDoneText.split(";");
														//		final int left = 7 - done.length;
														//		return left < 0;
														//	}
													//	})));
		
		// earn over 250 karma
		achievements.add(createAchievement("friend.karma.250", "Dobry samarytanin", "Zdobył 250 karmy",
				Achievement.MEDIUM_BASE_SCORE, true,
				new ChatCondition() {
			public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
				return player.getKarma()>250;
			}
		}));

		// meet Santa Claus, Easter Bunny and Guslarz
		achievements.add(createAchievement("friend.meet.seasonal", "Wciąż wierzy", "Spotkał Świętego Mikołaja, zajączka Wielkanocnego i Guślarza",
												Achievement.EASY_BASE_SCORE, true, new AndCondition(new QuestWithPrefixCompletedCondition("meet_santa_"), new QuestWithPrefixCompletedCondition("meet_bunny_"), new QuestWithPrefixCompletedCondition("meet_guslarz_"))));

		return achievements;
	}

	@Override
	protected Category getCategory() {
		return Category.FRIEND;
	}

}
