/* $Id: DailyItemQuestSummer2024.java,v 1.76 2024/06/19 15:13:16 davvids Exp $ */
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
package games.stendhal.server.maps.quests;

import games.stendhal.common.MathHelper;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropRecordedItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.IncreaseXPDependentOnLevelAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartRecordingRandomItemCollectionAction;
import games.stendhal.server.entity.npc.action.SayRequiredItemAction;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasRecordedItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * QUEST: Daily Item Fetch Quest.
 * <p>
 * PARTICIPANTS:
 * <li> Mayor of Ados
 * <li> some items
 * <p>
 * STEPS:
 * <li> talk to Mayor of Ados to get a quest to fetch an item
 * <li> bring the item to the mayor
 * <li> if you cannot bring it in one week he offers you the chance to fetch
 * another instead
 * <p>
 * REWARD:
 * <li> xp 
 * <li> 10 Karma
 * <p>
 * REPETITIONS:
 * <li> once a day
 */
public class DailyItemQuestSummer2024 extends AbstractQuest {

	private static final String QUEST_SLOT = "daily_item_summer_2024";
	
	/** How long until the player can give up and start another quest */
	private static final int expireDelay = MathHelper.MINUTES_IN_ONE_WEEK; 
	
	/** How often the quest may be repeated */
	private static final int delay = MathHelper.MINUTES_IN_ONE_DAY; 
	
	/**
	 * All items which are possible/easy enough to find. If you want to do
	 * it better, go ahead. *
	 * not to use yet, just getting it ready.
	 */
	private static Map<String,Integer> items;

private static void buildItemsMap() {
    items = new HashMap<String, Integer>();

    items.put("marchew", 45);
    items.put("marchew", 67);
    items.put("marchew", 72);
    items.put("sałata", 34);
    items.put("sałata", 56);
    items.put("sałata", 78);
    items.put("ser", 31);
    items.put("ser", 49);
    items.put("ser", 68);
    items.put("szynka", 39);
    items.put("szynka", 50);
    items.put("szynka", 73);
    items.put("mięso", 41);
    items.put("mięso", 55);
    items.put("mięso", 79);

    items.put("jabłko", 7);
    items.put("jabłko", 15);
    items.put("oliwka", 6);
    items.put("oliwka", 18);
    items.put("gruszka", 5);
    items.put("gruszka", 13);
    items.put("granat", 8);
    items.put("granat", 12);
    items.put("arbuz", 9);
    items.put("arbuz", 14);
    items.put("karczoch", 4);
    items.put("karczoch", 19);
    items.put("brokuł", 6);
    items.put("brokuł", 17);
    items.put("kalafior", 10);
    items.put("kalafior", 20);
    items.put("cukinia", 5);
    items.put("cukinia", 16);
    items.put("cebula", 8);
    items.put("cebula", 12);
    items.put("kapusta", 7);
    items.put("kapusta", 14);
    items.put("szpinak", 9);
    items.put("szpinak", 18);
    items.put("pieczarka", 11);
    items.put("pieczarka", 19);
    items.put("borowik", 4);
    items.put("borowik", 20);
    items.put("kanapka", 7);
    items.put("kanapka", 16);
    items.put("mięso z kraba", 5);
    items.put("mięso z kraba", 12);
    items.put("lody", 6);
    items.put("lody", 17);
    items.put("wyśmienity ser", 4);
    items.put("wyśmienity ser", 13);
    items.put("udko", 7);
    items.put("udko", 15);
    items.put("kiełbasa wiejska", 9);
    items.put("kiełbasa wiejska", 18);
    items.put("por", 10);
    items.put("por", 19);

    items.put("banan", 2);
    items.put("wisienka", 1);
    items.put("kokos", 3);
    items.put("winogrona", 2);
    items.put("ananas", 1);
    items.put("jajo", 3);
    items.put("chleb", 1);
    items.put("miód", 2);
    items.put("truskawka", 3);
}

	
	private ChatAction startQuestAction() {
		// common place to get the start quest actions as we can both starts it and abort and start again
		
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new StartRecordingRandomItemCollectionAction(QUEST_SLOT,0,items,"Jestem na wakacjach i trochę zgłodniałem. Przynieś mi [item]"
				+ " i powiedz #załatwione, gdy przyniesiesz. Dobrze Ci to wynagrodzę!"));	
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		
		return new MultipleActions(actions);
	}
	
	private void getQuest() {
		final SpeakerNPC npc = npcs.get("Diakes");
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,expireDelay))), 
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"Już prosiłem Cie o przyniesienie [item]"
						+ ". Powiedz #załatwione jeżeli przyniesiesz!"));
		
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new TimePassedCondition(QUEST_SLOT,1,expireDelay)), 
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"Zapomniałeś? Miałeś przynieść mi [item]"
						+ " powiedz #załatwione, jak przyniesiesz. Być może nie ma już tego jedzenia na rynku! Możesz przynieść #inny rodzaj jedzenia jeżeli chcesz lub wróć z tym, o który cię na początku prosiłem."));
	
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,delay))), 
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT,1, delay, "Na tą chwile jestem najedzony. Proszę wróć za"));

		
		
		
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new OrCondition(new QuestNotStartedCondition(QUEST_SLOT),
								new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
												 new TimePassedCondition(QUEST_SLOT,1,delay))), 
				ConversationStates.ATTENDING,
				null,
				startQuestAction());
	}
	
	private void completeQuest() {
		final SpeakerNPC npc = npcs.get("Diakes");
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Zgłodniałem, ale nie chce mi się stąd ruszać. Co powiesz na małe #zadanie?",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES, 
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Na tą chwile nie jestem już głodny. Dziękuje.",
				null);
		
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new DropRecordedItemAction(QUEST_SLOT,0));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		actions.add(new IncrementQuestAction(QUEST_SLOT, 2, 1));
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "done"));
		actions.add(new IncreaseXPDependentOnLevelAction(8, 90.0));
		actions.add(new EquipItemAction("czerwony kamyczek szlachetny", 1));
		actions.add(new EquipItemAction("czerwony kamień szlachetny", 1));
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,0)),
				ConversationStates.ATTENDING, 
				"Bardzo dziękuje! Dzięki Tobie nie musze się stąd ruszać i mogę dalej odpoczywać... weź w nagrodę te 2 kamienie szlachetne, w najbliższym czasie nie będą mi potrzebne.",
				new MultipleActions(actions));
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new NotCondition(new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,0))),
				ConversationStates.ATTENDING, 
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"Jeszcze nie przyniosłeś [item]"
						+ ". Idź i zdobądź, następnie wróć i powiedz #załatwione."));

	}
	
	private void abortQuest() {
		final SpeakerNPC npc = npcs.get("Diakes");
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
						 		 new TimePassedCondition(QUEST_SLOT,1,expireDelay)), 
				ConversationStates.ATTENDING, 
				null, 
				// start quest again immediately
				startQuestAction());
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
						 		 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,expireDelay))), 
				ConversationStates.ATTENDING, 
				"Bardzo zależy mi konkretnie na tym jedzeniu. Idź i zdobądź dla mnie jedzenie lub kup je od innego wojownika. Nie pozwolę Tobie poddać się tak szybko.", 
				null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				new QuestNotActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Wydaje mi się, że jeszcze nie rozmawialiśmy o #zadaniu", 
				null);
		
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem plażowicza Diakes w Semos");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę pomóc Diakesowi.");
			return res;
		}

		res.add("Chcę pomóc Diakesowi.");
		if (player.hasQuest(QUEST_SLOT) && !player.isQuestCompleted(QUEST_SLOT)) {
			String questItem = player.getRequiredItemName(QUEST_SLOT,0);
			int amount = player.getRequiredItemQuantity(QUEST_SLOT,0);
			if (!player.isEquipped(questItem, amount)) {
				res.add(("Zostałem poproszony o przyniesienie "
						+ Grammar.quantityplnoun(amount, questItem, "") + ", aby pomóc Diakesowi. Nie mam tego jeszcze."));
			} else {
				res.add(("Znalazłem "
						+ Grammar.quantityplnoun(amount, questItem, "") + " do pomocy Diakesowi i muszę je teraz dostarczyć."));
			}
		}
		int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
		if (repetitions > 0) {
			res.add("Pomogłem Diakesowi z jedzeniem "
					+ Grammar.quantityplnoun(repetitions, "razy") + " do tej pory.");
		}
		if (isRepeatable(player)) {
            res.add("Wydaje mi się, że Diakes znów zgłodniał.");
		} else if (isCompleted(player)){
			res.add("Na tą chwile Diakes powinien być dalej najedzony.");
		}
		return res;
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Jedzenie dla plażowicza",
				"Diakes - leniwy plażowicz w Semos, potrzebuje kogoś kto przyniesie mu troche jedzenia.",
				true);
		
		buildItemsMap();
		
		getQuest();
		completeQuest();
		abortQuest();
	}

	@Override
	public String getName() {
		return "DailyItemQuestSummer2024";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}
	
	@Override
	public boolean isRepeatable(final Player player) {
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,delay)).fire(player, null, null);
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Diakes";
	}
}
