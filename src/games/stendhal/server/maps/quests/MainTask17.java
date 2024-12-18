/* $Id: MainTask17.java 2020/12/04 12:29:13 davvids $ */
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

public class MainTask17 extends AbstractQuest {

	//this quest slot
	private static final String QUEST_SLOT = "main_task_17";
	
	private static final String FIRST_QUEST_SLOT = "vs_quest";
	private static final String SECOND_QUEST_SLOT = "kill_mag";
	
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
		res.add("Zanim Mistrz Faumonii XVII przyzna mi kolejne główne zadanie musze wykonać kilka zadań.	Powinienem nauczyć się posługiwać magią. W tym celu muszę odnaleźć Staro i wykonać dla niego zadanie, podobno lubi przesiadywać tawernie Fado.	Muszę poznać katakumby Semos i powalczyć z wampirami. W tym celu powinienem wybrać się do krasnala Hogarta, znajdującego się w kopalni Orril i wykonać dla niego zadanie. Mistrz poinformował mnie, żebym uważał gdyż nie będe w stanie wykonać tego zadania, jak przekrocze 200 poziom doświadczenia.");
		if (!isCompleted(player)) {
			res.add("Żeby ukończyć główne zadanie musze wybrac się do grabarza w semos Dane Takera, sporządzić eliksiry z wampirzego truchła i przynieść: " + Grammar.a_noun(player.getRequiredItemName(QUEST_SLOT,0)) + ".");
		}
		if (isCompleted(player)) {
			res.add("Ukończyłem siedemnaste zadanie od Mistrza Faumonii i otrzymałem odznakę wojownika Faumonii siedemnastego poziomu.	Następne główne zadanie otrzymam po przekroczeniu 200 poziomu doświadczenia.");
		}
		return res;
	}
	
	private void checkCollectingQuests() {
		final SpeakerNPC npc = npcs.get("Mistrz Faumonii XVII");
		
		npc.add(
		ConversationStates.IDLE,
		ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new QuestCompletedCondition(MAIN1),
			new QuestCompletedCondition(MAIN2),
			new LevelGreaterThanCondition(179),
			new GreetingMatchesNameCondition(npc.getName()),
			new QuestNotStartedCondition(QUEST_SLOT)),
		ConversationStates.ATTENDING,
		"Witaj, mam dla ciebie główne #zadanie siedemnastego poziomu.",
		null);
		
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
			new AndCondition(new QuestCompletedCondition(MAIN1),
			new QuestCompletedCondition(MAIN2),
			new LevelGreaterThanCondition(179),
			new QuestNotCompletedCondition(SECOND_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Zanim przyznam ci kolejne główne zadanie, powinieneś nauczyć się posługiwać magią. W tym celu wybierz się do Staro, znajdującego się w tawernie Fado i wykonaj dla niego zadanie.",
			null);
			
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
			new AndCondition(new QuestCompletedCondition(MAIN1),
			new QuestCompletedCondition(MAIN2),
			new LevelGreaterThanCondition(179),
			new QuestNotCompletedCondition(FIRST_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Zanim przyznam ci kolejne główne zadanie, powinieneś poznać katakumby Semos i powalczyć z wampirami. W tym celu wybierz się do kopalnii w Orril, odnajdź kowala Hogarta i wykonaj dla niego zadanie na miecz wysysający życie. Spiesz się! Wstęp do katakumb ograniczony jest poziomem, nie dostaną się tam wojownicy po przekroczeniu 200 poziomu doświadczenia!",
			null);	
			
	}

	private void requestItem() {
		
		final SpeakerNPC npc = npcs.get("Mistrz Faumonii XVII");
		final Map<String,Integer> items = new HashMap<String, Integer>();
		
		items.put("wielki eliksir",63);
		items.put("wielki eliksir",74);
		items.put("wielki eliksir",66);
		
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
				new AndCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestCompletedCondition(MAIN1),
						new QuestCompletedCondition(MAIN2),
						new LevelGreaterThanCondition(179),
						new QuestCompletedCondition(SECOND_QUEST_SLOT),
						new QuestCompletedCondition(FIRST_QUEST_SLOT)),
					ConversationStates.ATTENDING,
					null,
					new StartRecordingRandomItemCollectionAction(QUEST_SLOT, items, "Wykonałeś już zadania o które cie prosiłem, nauczyłeś się czym jest magia oraz posiadłeś miecz wysysający życie."
						+ " Musiałeś pokonać sporo wampirów... mam nadzieje, że zebrałeś ich truchło. Grabarz Dane Taker z miasta Semos, wytwarza wielkie eliksiry z wampirzego truchła. Idź do niego, wytwórz eliksiry i przynieś mi: [item]."));
	}
						
	private void collectItem() {
			final SpeakerNPC npc = npcs.get("Mistrz Faumonii XVII");
			
					npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.QUEST_ITEM_QUESTION, 
				"Kazałem ci odwiedzić grabarza Dane Taker z miasta Semos i wytworzyć eliksiry z wampirzego truchła. Przyniosłeś eliksiry o które cię prosiłem?",
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
				"Nauczyłeś się posługiwać magią, zdobyłeś miecz wysysający życie i wytworzyłeś wielkie eliksiry z wampirzego truchła. Zasługujesz na odznakę wojownika Faumonii siedemnastego poziomu. Następne główne zadanie otrzymasz po przekroczeniu 200 poziomu doświadczenia.",
				new MultipleActions(new DropRecordedItemAction(QUEST_SLOT), 
									new SetQuestAction(QUEST_SLOT, "done"),
									new DropItemAction("odznaka wojownika XVI",1),
									new EquipItemAction("odznaka wojownika XVII", 1, true)));
																
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
				"Wojownik Faumonii 17",
				"Mistrz Faumonii XVII ma dla ciebie siedemnaste główne zadanie które pomoże ci zostać wielkim wojownikiem Faumonii.",
				true);
		
		checkCollectingQuests();
		requestItem();
		collectItem();

	}	
	
		@Override
	public String getName() {
		return "MainTask17";
	}
	
	@Override
	public int getMinLevel() {
		return 180;
	}

	@Override
	public String getNPCName() {
		return "Mistrz Faumonii XVII";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}
}
		
	        	