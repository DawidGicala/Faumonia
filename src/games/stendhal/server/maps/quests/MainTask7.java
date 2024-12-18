/* $Id: MainTask7.java 2020/12/02 16:32:11 davvids $ */
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

public class MainTask7 extends AbstractQuest {

	//this quest slot
	private static final String QUEST_SLOT = "main_task_7";
	
	private static final String FIRST_QUEST_SLOT = "kirdneh_badge";
	
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
		res.add("Zanim Mistrz Faumonii VII przyzna mi kolejne zadanie musze uzyskać status zasłużonego mieszkańca miasta Kirdneh. Miasto Kirdneh znajdę przemierząjąc karzełkowy las na wschód od miasta Fado.");
		if (!isCompleted(player)) {
			res.add("Żeby ukończyć główne zadanie musze zdobyć 20 sztuk nietoperzego truchła a następne przetworzyć je na eliksiry i przynieść: " + Grammar.a_noun(player.getRequiredItemName(QUEST_SLOT,0)) + "	Truchło nietoperze moge przetworzyć na duże eliksiry u grabarza Same Taker w mieście Semos.");
		}
		if (isCompleted(player)) {
			res.add("Ukończyłem siódme zadanie od Mistrza Faumonii i otrzymałem odznakę wojownika Faumonii siódmego poziomu.	Następne główne zadanie otrzymam po przekroczeniu 40 poziomu doświadczenia.");
		}
		return res;
	}
	
	private void checkCollectingQuests() {
		final SpeakerNPC npc = npcs.get("Mistrz Faumonii VII");
		
		npc.add(
		ConversationStates.IDLE,
		ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new QuestCompletedCondition(MAIN1),
			new QuestCompletedCondition(MAIN2),
			new LevelGreaterThanCondition(34),
			new GreetingMatchesNameCondition(npc.getName()),
			new QuestNotStartedCondition(QUEST_SLOT)),
		ConversationStates.ATTENDING,
		"Witaj, mam dla ciebie główne #zadanie siódmego poziomu.",
		null);
		
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
			new AndCondition(new QuestCompletedCondition(MAIN1),
			new QuestCompletedCondition(MAIN2),
			new LevelGreaterThanCondition(34),
			new QuestNotCompletedCondition(FIRST_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Ostatnim razem poznałeś odległe miasto Fado, tym razem znowu musisz wybrać się w tamten region. Na wschód od miasta Fado, za lasem pełnym karzełków znajdziesz miasto Kirdneh. Odnajdź ratusz i wykonaj zadania u Urzędnika Kirdneh aby uzyskać status zasłużonego mieszkańca.",
			null);
						
	}

	private void requestItem() {
		
		final SpeakerNPC npc = npcs.get("Mistrz Faumonii VII");
		final Map<String,Integer> items = new HashMap<String, Integer>();
		
		items.put("duży eliksir",20);
		
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
				new AndCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestCompletedCondition(MAIN1),
						new QuestCompletedCondition(MAIN2),
						new LevelGreaterThanCondition(34),
						new QuestCompletedCondition(FIRST_QUEST_SLOT)),
					ConversationStates.ATTENDING,
					null,
					new StartRecordingRandomItemCollectionAction(QUEST_SLOT, items, "Wykonałeś już zadanie o które cie prosiłem i poznałeś nowe miasto - Kirdneh"
						+ ". Czas abyś nauczył się wytwarzać eliksiry lecznicze. Musisz trochę powalczyć i zdobyć 20 truchła nietoperzego. Nietoperze znajdziesz wszędzie w podziemiach. Nastepnie wybierz się do grabarza Same Taker w mieście Semos i wykonaj eliksiry. Znajdziesz go w pobliżu. Przynieś mi: [item]."));
	}
						
	private void collectItem() {
			final SpeakerNPC npc = npcs.get("Mistrz Faumonii VII");
			
					npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.QUEST_ITEM_QUESTION, 
				"Powinieneś wybrać się do podziemi i zdobyć 20 truchła nietoperzego. Nastepnie odwiedzić grabarza Same Take z Semos i wykonać 20 dużych eliksirów. Przyniosłeś to o co cie prosiłem?",
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
				"Poznałeś kolejne miasto - Kirdneh oraz nauczyłeś się wykonywać duże eliksiry z nietoperzego truchła. Zasługujesz na odznakę wojownika Faumonii siódmego poziomu. Następne główne zadanie otrzymasz po przekroczeniu 40 poziomu doświadczenia.",
				new MultipleActions(new DropRecordedItemAction(QUEST_SLOT), 
									new SetQuestAction(QUEST_SLOT, "done"),
									new DropItemAction("odznaka wojownika VI",1),
									new EquipItemAction("odznaka wojownika VII", 1, true)));
																
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
				"Wojownik Faumonii 7",
				"Mistrz Faumonii VII ma dla ciebie siódme główne zadanie które pomoże ci zostać wielkim wojownikiem Faumonii.",
				true);
		
		checkCollectingQuests();
		requestItem();
		collectItem();

	}	
	
		@Override
	public String getName() {
		return "MainTask7";
	}
	
	@Override
	public int getMinLevel() {
		return 35;
	}

	@Override
	public String getNPCName() {
		return "Mistrz Faumonii VII";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}
}
		
	        	