/* $Id: BringBlack.java 2020/12/09 14:05:13 davvids $ */
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

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropRecordedItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.StartRecordingRandomItemCollectionAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasRecordedItemWithHimCondition;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;

import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BringBlack extends AbstractQuest {

	//this quest slot
	private static final String QUEST_SLOT = "bring_black";
	
	private static final String MAIN1 = "main_task_28";
	private static final String MAIN2 = "main_task_2";
	
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
		res.add("Podjąłem się przyniesienia przedmiotów Latowi.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chce pomóc Latowi");
			return res;
		}
		if (!isCompleted(player)) {
			res.add("Żeby ukończyć główne zadanie musze przynieść " + Grammar.a_noun(player.getRequiredItemName(QUEST_SLOT,0)) + ".");
		}
		if (isCompleted(player)) {
			res.add("Pomogłem Latowi.	W nagrodę za ukończenie zadania otrzymałem: 2.000.000 pkt doświadczenia, 300.000 pkt obrony, 200.000 pkt ataku i 100 pkt karmy.");
		}
		return res;
	}
	
	private void checkCollectingQuests() {
		final SpeakerNPC npc = npcs.get("Lat");
		
		npc.add(
		ConversationStates.IDLE,
		ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new QuestCompletedCondition(MAIN1),
			new QuestCompletedCondition(MAIN2),
			new LevelGreaterThanCondition(499),
			new GreetingMatchesNameCondition(npc.getName()),
			new QuestNotStartedCondition(QUEST_SLOT)),
		ConversationStates.ATTENDING,
		"Witaj, poszukuje kogoś kto wykona dla mnie pewne #zadanie.",
		null);
			
	}

	private void requestItem() {
		
		final SpeakerNPC npc = npcs.get("Lat");
		final Map<String,Integer> items = new HashMap<String, Integer>();
		
		items.put("czarna zbroja",5);
		items.put("buty czarne",12);
		items.put("czarny płaszcz",12);
		items.put("czarny hełm",3);
		items.put("czarne spodnie",11);
		items.put("czarna tarcza",6);
		items.put("kosa czarna",4);
		items.put("czarne rękawice",2);
		items.put("halabarda czarna",1);
		items.put("czarny sztylet",3);
		items.put("czarny miecz",5);
		
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
				new AndCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestCompletedCondition(MAIN1),
						new QuestCompletedCondition(MAIN2),
						new LevelGreaterThanCondition(499)),
					ConversationStates.ATTENDING,
					null,
					new StartRecordingRandomItemCollectionAction(QUEST_SLOT, items, "Potrzebujęmy kilku przedmiotów do zbrojowni."
						+ " Przynieś mi: [item]."));
	}
						
	private void collectItem() {
			final SpeakerNPC npc = npcs.get("Lat");
			
					npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.QUEST_ITEM_QUESTION, 
				"Przyniosłeś mi przedmioty o które cię prosiłem?",
				null);
				
			npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES, 
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								new NotCondition(new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT))),
				ConversationStates.ATTENDING, 
				null,
				new SayRequiredItemAction(QUEST_SLOT, "Nie masz przy sobie [item]."));

					npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES, 
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING, 
				"Przyniosłeś mi przedmioty o które cie prosiłem. Stolica Ados jest wdzięczna za twoją pomoc!",
				new MultipleActions(new DropRecordedItemAction(QUEST_SLOT), 
									new SetQuestAction(QUEST_SLOT, "done"),
									new IncreaseDefXPAction(300000),
									new IncreaseAtkXPAction(200000),
									new IncreaseXPAction(2000000),
									new IncreaseKarmaAction(100)));

																
				npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING, 
				null,
				new SayRequiredItemAction(QUEST_SLOT, "Wróć kiedy będziesz mieć [item] ze sobą."));
	}			

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Zapasy do zbrojowni w Ados",
				"Jeden z pracowników ratusza w Ados - Lat, poszukuje kogoś kto pomoże zaopatrzyć zbrojownie.",
				true);
		
		checkCollectingQuests();
		requestItem();
		collectItem();

	}	
	
		@Override
	public String getName() {
		return "BringBlack";
	}
	
	@Override
	public int getMinLevel() {
		return 500;
	}

	@Override
	public String getNPCName() {
		return "Lat";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}
}
		
	        	