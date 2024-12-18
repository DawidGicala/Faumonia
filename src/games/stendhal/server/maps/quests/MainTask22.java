/* $Id: MainTask22.java 2020/12/07 14:46:13 davvids $ */
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
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainTask22 extends AbstractQuest {

	//this quest slot
	private static final String QUEST_SLOT = "main_task_22";
	
	private static final String FIRST_QUEST_SLOT = "kill_mithril";
	private static final String SECOND_QUEST_SLOT = "resource_for_diehelm";
	
	private static final String MAIN1 = "main_task_1";
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
		res.add("Podjąłem się głownego zadania na wojownika Faumonii");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chce podjąć się głównego zadania na wojownika Faumonii");
			return res;
		}
		res.add("Zanim Mistrz Faumonii XXII przyzna mi kolejne główne zadanie musze poznać zamek żołnierzy mithrilu.	W tym celu powinienem wybrać się do stolicy, odnaleźć mieszkańca Banio i wykonać dla niego zadanie.	Następnie powinienem wybrać się w podziemne tunele, odnaleźć przejście do zamku, odszukać tamtejszego kowala i wykonać dla niego zadanie.");
		if (!isCompleted(player)) {
			res.add("Żeby ukończyć główne zadanie musze przynieść " + Grammar.a_noun(player.getRequiredItemName(QUEST_SLOT,0)) + ".");
		}
		if (isCompleted(player)) {
			res.add("Ukończyłem dwudzieste-drugie zadanie od Mistrza Faumonii i otrzymałem odznakę wojownika Faumonii dwudziestego-drugiego poziomu.	Następne główne zadanie otrzymam po przekroczeniu 300 poziomu doświadczenia.	W międzyczasie mogę znowu odwiedzić kolekcjonerkę płaszczy Josephine i wykonać dla niej zadanie na buty zabójcy.");
		}
		return res;
	}
	
	private void checkCollectingQuests() {
		final SpeakerNPC npc = npcs.get("Mistrz Faumonii XXII");
		
		npc.add(
		ConversationStates.IDLE,
		ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new QuestCompletedCondition(MAIN1),
			new QuestCompletedCondition(MAIN2),
			new LevelGreaterThanCondition(299),
			new GreetingMatchesNameCondition(npc.getName()),
			new QuestNotStartedCondition(QUEST_SLOT)),
		ConversationStates.ATTENDING,
		"Witaj, mam dla ciebie główne #zadanie dwudziestego-drugiego poziomu.",
		null);
		
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
			new AndCondition(new QuestCompletedCondition(MAIN1),
			new QuestCompletedCondition(MAIN2),
			new LevelGreaterThanCondition(299),
			new QuestNotCompletedCondition(SECOND_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Zanim przyznam ci kolejne główne zadanie, czas abyś poznał zamek żołnierzy mithrilu. W tym celu wybierz się do tuneli pod Semos i odnajdź drogę do zamku. Następnie odnajdź tamtejszego kowala i zdobądź klucz do więzienia.",
			null);
			
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
			new AndCondition(new QuestCompletedCondition(MAIN1),
			new QuestCompletedCondition(MAIN2),
			new LevelGreaterThanCondition(299),
			new QuestNotCompletedCondition(FIRST_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Zanim przyznam ci kolejne główne zadanie, czas abyś poznał zamek żołnierzy mithrilu. W tym celu wybierz się do stolicy Faumonii, odnajdź Banio i wykonaj dla niego zadanie.",
			null);	
			
	}

	private void requestItem() {
		
		final SpeakerNPC npc = npcs.get("Mistrz Faumonii XXII");
		final Map<String,Integer> items = new HashMap<String, Integer>();
		
		items.put("sztabka mithrilu",222);
		items.put("sztabka mithrilu",204);
		items.put("sztabka mithrilu",216);
		items.put("sztabka mithrilu",236);
		items.put("sztabka mithrilu",201);
		items.put("sztabka mithrilu",192);
		items.put("sztabka mithrilu",173);
		
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
				new AndCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestCompletedCondition(MAIN1),
						new QuestCompletedCondition(MAIN2),
						new LevelGreaterThanCondition(299),
						new QuestCompletedCondition(SECOND_QUEST_SLOT),
						new QuestCompletedCondition(FIRST_QUEST_SLOT)),
					ConversationStates.ATTENDING,
					null,
					new StartRecordingRandomItemCollectionAction(QUEST_SLOT, items, "Poznałeś już zamek żołnierzy mithrilu, wykonałeś zadanie dla Banio oraz posiadasz klucz do więzienia mithrilu."
						+ ". Podczas walki z mithrilami, na pewno zdobyłeś masę grudek mithrilu. Wybierz się teraz do kowala w Ados, przepal je na sztabki i przynieś mi: [item]."));
	}
						
	private void collectItem() {
			final SpeakerNPC npc = npcs.get("Mistrz Faumonii XXII");
			
					npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.QUEST_ITEM_QUESTION, 
				"Przyniosłeś mi sztabki mithrilu o które cię prosiłem?",
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
				"Poznałeś zamek żołnierzy mithrilu, wykonałeś zadanie dla Banio, zdobyłeś klucz do więzienia oraz przyniosłeś mi sztabki mithrilu. Zasługujesz na odznakę wojownika Faumonii dwudziestego-drugiego poziomu. Następne główne zadanie otrzymasz po przekroczeniu 300 poziomu doświadczenia. W międzyczasie możesz znowu odwiedzić kolekcjonerkę płaszczy Josephine w Fado i wykonać dla niej zadanie na buty zabójcy.",
				new MultipleActions(new DropRecordedItemAction(QUEST_SLOT), 
									new SetQuestAction(QUEST_SLOT, "done"),
									new DropItemAction("odznaka wojownika XXI",1),
									new EquipItemAction("odznaka wojownika XXII", 1, true)));
																
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
				"Wojownik Faumonii 22",
				"Mistrz Faumonii XXII ma dla ciebie dwudzieste-drugie główne zadanie które pomoże ci zostać wielkim wojownikiem Faumonii.",
				true);
		
		checkCollectingQuests();
		requestItem();
		collectItem();

	}	
	
		@Override
	public String getName() {
		return "MainTask22";
	}
	
	@Override
	public int getMinLevel() {
		return 300;
	}

	@Override
	public String getNPCName() {
		return "Mistrz Faumonii XXII";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}
}
		
	        	