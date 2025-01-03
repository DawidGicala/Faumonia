/* $Id: KalavanHouseseller.java,v 1.8 2011/05/01 19:50:06 martinfuchs Exp $ */
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

import games.stendhal.common.parser.ExpressionType;
import games.stendhal.common.parser.JokerExprMatcher;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.condition.AgeGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TextHasNumberCondition;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

final class KalavanHouseseller extends HouseSellerNPCBase {
	/** Cost to buy house in kalavan. */
	private static final int COST_KALAVAN = 100000;
	private static final String PRINCESS_QUEST_SLOT = "imperial_princess";

	KalavanHouseseller(final String name, final String location, final HouseTax houseTax) {
		super(name, location, houseTax);
		init();
	}

	private void init() {
		// Other than the condition that you must not already own a house, there are a number of conditions a player must satisfy. 
		// For definiteness we will check these conditions in a set order. 
		// So then the NPC doesn't have to choose which reason to reject the player for (appears as a WARN from engine if he has to choose)

		// player has not done required quest, hasn't got a house at all
add(ConversationStates.ATTENDING, 
		Arrays.asList("cost", "house", "buy", "purchase", "koszt", "dom", "kupić", "cenę", "cena"),
		new AndCondition(new QuestNotStartedCondition(HouseSellerNPCBase.QUEST_SLOT), new QuestNotCompletedCondition(KalavanHouseseller.PRINCESS_QUEST_SLOT)),
		ConversationStates.ATTENDING, 
			"Jak już wiesz koszt nowego domu wynosi "
		+ getCost()
		+ " money. Ale obawiam się, że nie mogę Ci sprzedać domu dopóki twoje obywatelstwo nie zostanie nadane przez Króla, którego znajdziesz "
		+ " w zamku Kalavan na północ stąd. Spróbuj najpierw porozmawiać z jego córką, ona jest ... przyjazna.",
			null);

// player is not old enough but they have doen princess quest 
// (don't need to check if they have a house, they can't as they're not old enough)
add(ConversationStates.ATTENDING, 
		Arrays.asList("cost", "house", "buy", "purchase", "koszt", "dom", "kupić", "cenę", "cena"),
		new AndCondition(
							 new QuestCompletedCondition(KalavanHouseseller.PRINCESS_QUEST_SLOT),
							 new NotCondition(new AgeGreaterThanCondition(HouseSellerNPCBase.REQUIRED_AGE))),
		ConversationStates.ATTENDING, 
		"Koszt nowego domu wynosi "
		+ getCost()
		+ "money. Ale obawiam się, że nie mogę Ci jeszcze zaufać w kwestii kupna domu. Wróć gdy spędzisz tutaj ponad " 
		+ Integer.toString((HouseSellerNPCBase.REQUIRED_AGE / 60)) + " hours on Faiumoni.",
		null);

// player is eligible to buy a house
		add(ConversationStates.ATTENDING, 
		Arrays.asList("cost", "house", "buy", "purchase", "koszt", "dom", "kupić", "cenę", "cena"),
		new AndCondition(new QuestNotStartedCondition(HouseSellerNPCBase.QUEST_SLOT), 
						 new AgeGreaterThanCondition(HouseSellerNPCBase.REQUIRED_AGE), 
							 new QuestCompletedCondition(KalavanHouseseller.PRINCESS_QUEST_SLOT)),
		ConversationStates.QUEST_OFFERED, 
		"Jak już wiesz koszt nowego domu wynosi "
		+ getCost()
		+ " money. Prócz tego musisz zapłacić podatek od nieruchomości w wysokości " + HouseTax.BASE_TAX
		+ " money, miesięcznie. Możesz mnie się zapytać, który dom jest #dostępny, a jeżeli masz już jakiś na oku to podaj mi jego numer.",
		null);

// handle house numbers 1 to 25
addMatching(ConversationStates.QUEST_OFFERED,
		// match for all numbers as trigger expression
		ExpressionType.NUMERAL, new JokerExprMatcher(),
		new TextHasNumberCondition(getLowestHouseNumber(), getHighestHouseNumber()),
		ConversationStates.ATTENDING,
		null,
		new BuyHouseChatAction(getCost(), QUEST_SLOT));

addJob("Jestem agentem nieruchomości z prostymi zasadami. Sprzedaję domy tym, którzy otrzymali #obywatelstwo. Domy sporo kosztują. Tymczasowo nasza broszura jest na #http://www.faumonia.pl/galerie-zdj%C4%99%C4%87/domki.");
addReply(Arrays.asList("citizenship", "obywatelstwo"),
			 "O tym decyduje rodzina królewska w zamku Kalavan.");


setDescription("Oto mężczyzna wyglądający na bystrą osobę.");
setEntityClass("estateagentnpc");
setPosition(55, 94);
initHP(100);
		
	}

	protected int getCost() {
		return KalavanHouseseller.COST_KALAVAN;
	}

	protected int getHighestHouseNumber() {
		return 25;
	}

	protected int getLowestHouseNumber() {
		return 1;
	}

	@Override
	protected void createPath() {
		final List<Node> nodes = new LinkedList<Node>();
		nodes.add(new Node(55, 94));
		nodes.add(new Node(93, 94));
		nodes.add(new Node(93, 73));
		nodes.add(new Node(107, 73));
		nodes.add(new Node(107, 35));
		nodes.add(new Node(84, 35));
		nodes.add(new Node(84, 20));
		nodes.add(new Node(17, 20));
		nodes.add(new Node(17, 82));
		nodes.add(new Node(43, 82));
		nodes.add(new Node(43, 94));
		setPath(new FixedPath(nodes, true));
	}
}
