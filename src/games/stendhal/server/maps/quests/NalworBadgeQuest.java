/* $Id: UltimateCollector.java 2020/11/27 16:14:11 davvids $ */
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
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NalworBadgeQuest extends AbstractQuest {

	//this quest slot
	private static final String QUEST_SLOT = "nalwor_badge";

	private static final String FIRST_QUEST_SLOT = "grafindle_gold";
	private static final String SECOND_QUEST_SLOT = "swords_for_nnyddion";
	private static final String THIRD_QUEST_SLOT = "kill_elfes";
	
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
		res.add("Zdecydowałem się pomóc mieszkańcom Nalwor.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chcę pomóc mieszkańcom Nalwor");
			return res;
		}
		res.add("Podobno Nnyddion, Lorithien i Grafindle potrzebują pomocy. Powinienem z nimi porozmawiać.");
		if (!isCompleted(player)) {
			res.add("Żeby ukończyć zadanie musze przynieść " + Grammar.a_noun(player.getRequiredItemName(QUEST_SLOT,0)) + ".");
		}
		if (isCompleted(player)) {
			res.add("Zostałem zasłużonym mieszkańcem Nalwor i od teraz moge kupować zwoje.");
		}
		return res;
	}
	
	private void checkCollectingQuests() {
		final SpeakerNPC npc = npcs.get("Urzędnik Nalwor");
		
		
		npc.add(
		ConversationStates.IDLE,
		ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
			new QuestNotStartedCondition(QUEST_SLOT)),
		ConversationStates.ATTENDING,
		"Witaj. Jestem urzędnikiem Nalwor. Zajmuje się sprawami urzędowymi i sprzedaje zwoje do miasta zasłużonym mieszkańcom.",
		null);
		
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("mieszkańcom", "zadanie", "zasłużonu", "task"),
			new AndCondition(new QuestNotCompletedCondition(FIRST_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Jeśli chcesz zostać zasłużonym mieszkańcem Nalwor to najpierw musisz zdobyć klucz do banku.",
			null);
			
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("mieszkańcom", "zadanie", "zasłużonu", "task"),
			new AndCondition(new QuestNotCompletedCondition(SECOND_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Jeśli chcesz zostać zasłużonym mieszkańcem Nalwor to musisz pomóc Nnyddion zapełnić depozyt.",
			null);	
					
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("mieszkańcom", "zadanie", "zasłużonu", "task"),
			new AndCondition(new QuestNotCompletedCondition(THIRD_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Jeśli chcesz zostać zasłużonym mieszkańcem Nalwor to musisz pomóc poczcie Nalwor.",
			null);
			
	}

	private void requestItem() {
		
		final SpeakerNPC npc = npcs.get("Urzędnik Nalwor");
		final Map<String,Integer> items = new HashMap<String, Integer>();
		
		
		
		items.put("zbroja elficka",1);
		items.put("spodnie elfickie",1);
		items.put("buty elfickie",1);
		items.put("tarcza elficka",1);
		items.put("hełm elficki",1);
		items.put("płaszcz elficki",1);
		
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("mieszkańcom", "zadanie", "zasłużonu", "task"),
				new AndCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestCompletedCondition(SECOND_QUEST_SLOT),
						new QuestCompletedCondition(FIRST_QUEST_SLOT),
						new QuestCompletedCondition(THIRD_QUEST_SLOT)),
					ConversationStates.ATTENDING,
					null,
					new StartRecordingRandomItemCollectionAction(QUEST_SLOT, items, "Pomogłeś już wielu mieszkańcom Nalwor. Aby ostatecznie udowodnić mi, że jesteś oddany Nalwor,"
						+ " proszę przynieś [item]."));
	}
						
	private void collectItem() {
			final SpeakerNPC npc = npcs.get("Urzędnik Nalwor");
			
					npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.QUEST_ITEM_QUESTION, 
				"Przyniosłeś przedmioty, o które cię prosiłem?",
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
				"Dziękuje Ci w imieniu mieszkańców Nalwor. Oficjalnie honoruje cię zasłużonym mieszkańcem Nalwor. Od teraz możesz kupić u mnie zwoje do miasta.",
				new MultipleActions(new DropRecordedItemAction(QUEST_SLOT), 
									new SetQuestAction(QUEST_SLOT, "done"),
									new IncreaseXPAction(80000),
									new IncreaseAtkXPAction(3500),
									new IncreaseDefXPAction(4000),
									new EquipItemAction("zwój nalwor", 10, true),
									new IncreaseKarmaAction(10)));
									
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
				"Mieszkaniec Nalwor",
				"Urzędnik Nalwor uhonoruje cie jako zasłużonego mieszkańca jeśli pomożesz kilku tutejszym mieszkańcom.",
				true);
		
		checkCollectingQuests();
		requestItem();
		collectItem();

	}	
	
		@Override
	public String getName() {
		return "NalworBadgeQuest";
	}
	
	@Override
	public int getMinLevel() {
		return 60;
	}

	@Override
	public String getNPCName() {
		return "Urzędnik Nalwor";
	}
	
	@Override
	public String getRegion() {
		return Region.NALWOR_CITY;
	}
}
		
	        	