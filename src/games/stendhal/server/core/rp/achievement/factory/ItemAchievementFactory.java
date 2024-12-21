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

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.PlayerLootedNumberOfItemsCondition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Factory for item related achievements.
 *
 * @author madmetzger
 */
public class ItemAchievementFactory extends AbstractAchievementFactory {

	@Override
	protected Category getCategory() {
		return Category.ITEM;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> itemAchievements = new LinkedList<Achievement>();
	//	itemAchievements.add(createAchievement("item.money.100", "Pierwsze kieszonkowe", "Zdobył 100 money na potworach", 
								//				Achievement.EASY_BASE_SCORE, true,
								//				new PlayerLootedNumberOfItemsCondition(100, "money")));
	//	itemAchievements.add(createAchievement("item.money.1000000", "Już nie potrzebujesz więcej", "Zdobył 1000000 money na potworach", 
		//		Achievement.HARD_BASE_SCORE, true,
		//		new PlayerLootedNumberOfItemsCondition(1000000, "money")));
	//	itemAchievements.add(createAchievement("item.set.red", "Niebezpieczna Amazonia", "Zdobył karmazynowy zestaw",
	//			Achievement.MEDIUM_BASE_SCORE, true,
		//		new PlayerLootedNumberOfItemsCondition(1, "zbroja karmazynowa", "karmazynowy hełm", "płaszcz karmazynowy", "spodnie karmazynowe", "buty karmazynowe", "karmazynowa tarcza")));
	//	itemAchievements.add(createAchievement("item.set.blue", "Czuję błękit", "Zdobył lazurowy zestaw",
		//		Achievement.MEDIUM_BASE_SCORE, false,
		//		new PlayerLootedNumberOfItemsCondition(1, "zbroja lazurowa", "lazurowy hełm", "prążkowany płaszcz lazurowy", "spodnie lazurowe", "buty lazurowe", "lazurowa tarcza")));
	//	itemAchievements.add(createAchievement("item.set.shadow", "Mieszkaniec cienia", "Zdobył cały zestaw cieni", 
		//		Achievement.MEDIUM_BASE_SCORE, true,
	//			new PlayerLootedNumberOfItemsCondition(1, "zbroja cieni", "hełm cieni", "płaszcz cieni", "spodnie cieni", "buty cieni", "tarcza cieni")));
	//	itemAchievements.add(createAchievement("item.set.chaos", "Zdobywca chaosu", "Zdobył cały zestaw chaosów", 
	//			Achievement.HARD_BASE_SCORE, true,
	//			new PlayerLootedNumberOfItemsCondition(1, "zbroja chaosu", "hełm chaosu", "płaszcz chaosu", "spodnie chaosu", "buty chaosu", "tarcza chaosu")));
	//	itemAchievements.add(createAchievement("item.set.golden", "Złote dziecko", "Zdobył cały złoty zestaw", 
	//			Achievement.MEDIUM_BASE_SCORE, true,
	//			new PlayerLootedNumberOfItemsCondition(1, "złota zbroja", "złoty hełm", "złoty płaszcz", "złote spodnie", "buty złote", "złota tarcza")));
		//itemAchievements.add(createAchievement("item.set.black", "Przejdź na ciemną strone", "Zdobył cały czarny zestaw", 
		//		Achievement.HARD_BASE_SCORE, true,
	//			new PlayerLootedNumberOfItemsCondition(1, "czarna zbroja", "czarny hełm", "czarny płaszcz", "czarne spodnie", "buty czarne", "czarna tarcza")));
	//	itemAchievements.add(createAchievement("item.set.mainio", "Wspaniałe Rzeczy", "Zdobył cały zestaw mainiocyjski",
		//		Achievement.HARD_BASE_SCORE, true,
		//		new PlayerLootedNumberOfItemsCondition(1, "zbroja mainiocyjska", "hełm mainiocyjski", "płaszcz mainiocyjski", "spodnie mainiocyjskie", "buty mainiocyjskie", "tarcza mainiocyjska")));
		// commented as it is not really a complete equipment set, because the helmet is missing
//		itemAchievements.add(createAchievement("item.set.xeno", "Trochę xenofobiczny?", "Zdobył cały zestaw xenocyjski",
//				Achievement.HARD_BASE_SCORE, true,
//				new PlayerLootedNumberOfItemsCondition(1, "zbroja xenocyjska", "płaszcz xenocyjski", "spodnie xenocyjskie", "buty xenocyjskie", "tarcza xenocyjska")));

		return itemAchievements;
	}

}
