/* $Id: MainTask3.java 2020/12/01 8:44:11 davvids $ */
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

public class MainTask3 extends AbstractQuest {

	//this quest slot
	private static final String QUEST_SLOT = "main_task_3";
	
	private static final String FIRST_QUEST_SLOT = "help_sedro";
	private static final String SECOND_QUEST_SLOT = "introduce_players";
	
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
		res.add("Zanim Mistrz Faumonii III przyzna mi kolejne zadanie znowu musze pomóc innym mieszkancom Semos. Powinienem porozmawiać ze starcem Sedro i chorym chłopcem Tadem.");
		if (!isCompleted(player)) {
			res.add("Żeby ukończyć główne zadanie musze przynieść " + Grammar.a_noun(player.getRequiredItemName(QUEST_SLOT,0)) + " aby udowodnić, że umiem wytwarzać eliksiry ziołowe.	Mistrz Faumonii III poinformował mnie, że zielarze w Semos chętne powiedzą gdzie powinienem szukać danego zioła.");
		}
		if (isCompleted(player)) {
			res.add("Ukończyłem trzecie zadanie od Mistrza Faumonii i otrzymałem odznakę wojownika Faumonii trzeciego poziomu.	Otrzymałem nóż z kości. Podobno dzięki niemu moge wzmocnić swoje umiejętności ataku i obrony. W tym celu powinienem wybrać się na pole treningowe pod bankiem Semos i znaleźć innego gracza do treningu. Mistrz Faumonii powiedział mi, że podczas walki z potworami i graczami przez cały czas zdobywam punkty ataku i obrony, tym więcej ich mam, tym silniejszy jestem.	Następne główne zadanie otrzymam po przekroczeniu 15 poziomu doświadczenia.");
		}
		return res;
	}
	
	private void checkCollectingQuests() {
		final SpeakerNPC npc = npcs.get("Mistrz Faumonii III");
		
		npc.add(
		ConversationStates.IDLE,
		ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new QuestCompletedCondition(MAIN1),
			new QuestCompletedCondition(MAIN2),
			new LevelGreaterThanCondition(9),
			new GreetingMatchesNameCondition(npc.getName()),
			new QuestNotStartedCondition(QUEST_SLOT)),
		ConversationStates.ATTENDING,
		"Witaj, mam dla ciebie główne #zadanie trzeciego poziomu.",
		null);
		
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
			new AndCondition(new QuestCompletedCondition(MAIN1),
			new QuestCompletedCondition(MAIN2),
			new LevelGreaterThanCondition(9),
			new QuestNotCompletedCondition(FIRST_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Zanim przyznam ci kolejne główne zadanie musisz wybrać się do Sedro w mieście Semos i pomóc mu z potworami. Zazwyczaj spędza czas w pobliżu domku startowego.",
			null);
			
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
			new AndCondition(new QuestCompletedCondition(MAIN1),
			new QuestCompletedCondition(MAIN2),
			new LevelGreaterThanCondition(9),
			new QuestNotCompletedCondition(SECOND_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Zanim przyznam ci kolejne główne zadanie musisz wybrać się chorego chłopca Tada w mieście Semos i dostarczyć mu lekarstwo. Znajdziesz go w domku nad bankiem Semos.",
			null);	
			
	}

	private void requestItem() {
		
		final SpeakerNPC npc = npcs.get("Mistrz Faumonii III");
		final Map<String,Integer> items = new HashMap<String, Integer>();
		
		items.put("napar z aranduli",30);
		items.put("napar z pokrzywy",10);
		
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
				new AndCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestCompletedCondition(MAIN1),
						new QuestCompletedCondition(MAIN2),
						new LevelGreaterThanCondition(9),
						new QuestCompletedCondition(SECOND_QUEST_SLOT),
						new QuestCompletedCondition(FIRST_QUEST_SLOT)),
					ConversationStates.ATTENDING,
					null,
					new StartRecordingRandomItemCollectionAction(QUEST_SLOT, items, "Wykonałeś już zadania o które cie prosiłem. Czas abyś nauczył się produkować eliksiry ziołowe które mogą być dobrą alternatywną dla zwykłych eliksirów leczniczych. W całej krainie Faumonii rośnie kilka leczniczych roślin, zielarze w Semos za drobną opłatą wykonają dla ciebie eliksiry ziołowe, jeśli ich dopytasz to dadzą ci wskazówki gdzie powinieneś szukać danych zioł. Zielarnia semos znajduje się 3 budynki na lewo od banku."
						+ ". Na początek powinieneś zainteresować się pokrzywą i arandulą. Jak już to zrobisz to w ramach głównego zadania przynieś mi: [item]."));
	}
						
	private void collectItem() {
			final SpeakerNPC npc = npcs.get("Mistrz Faumonii III");
			
					npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.QUEST_ITEM_QUESTION, 
				"Powinieneś nauczyć się wytwarzać eliksiry ziołowe z aranduli i pokrzywy. W tym celu miałeś porozmawiać z zielarzami w mieście Semos i wykonać eliksiry. Jesli nie wiesz gdzie szukać danych roslin, porozmawiaj z zielarzami. Znajdziesz ich w 3 budynku na lewo od banku Semos. Jeśli masz problem z pozyskaniem roślin to wybierz się do banku lub skorzystaj z globalnego czatu '/k wiadomość'. Przyniosłeś eliksiry ziołowe, o które cię prosiłem?",
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
				"Nauczyłeś się zbierać rośliny i produkować eliksiry ziołowe. Te które dzisiaj zrobiłeś powoli i delikatnie odnawiają twoje zdrowie, o mocniejszych porozmawiamy innym razem... Zasługujesz na odznakę wojownika Faumonii trzeciego poziomu. Następne główne zadanie otrzymasz po przekroczeniu 15 poziomu doświadczenia. Weź ten nóż z kości. Dzięki niemu możesz zacząć w łatwy sposób ćwiczyć swoje umiejętności ataku i obrony. Po prostu wybierz się na pole treningowe pod bankiem Semos i znajdź innego gracza do treningu. Pamiętaj, że jeżeli nie ma chętnych to możesz skorzystać z globalnego chatu wpisując '/k wiadomość' Podczas walki z potworami i graczami przez cały czas zdobywasz punkty ataku i obrony, tym więcej ich masz, tym silniejszy jesteś.",
				new MultipleActions(new DropRecordedItemAction(QUEST_SLOT), 
									new SetQuestAction(QUEST_SLOT, "done"),
									new DropItemAction("odznaka wojownika II",1),
									new EquipItemAction("nóż z kości", 1, true),
									new EquipItemAction("odznaka wojownika III", 1, true)));
																
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
				"Wojownik Faumonii 3",
				"Mistrz Faumonii III ma dla ciebie trzecie główne zadanie które pomoże ci zostać wielkim wojownikiem Faumonii.",
				true);
		
		checkCollectingQuests();
		requestItem();
		collectItem();

	}	
	
		@Override
	public String getName() {
		return "MainTask3";
	}
	
	@Override
	public int getMinLevel() {
		return 10;
	}

	@Override
	public String getNPCName() {
		return "Mistrz Faumonii III";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}
}
		
	        	