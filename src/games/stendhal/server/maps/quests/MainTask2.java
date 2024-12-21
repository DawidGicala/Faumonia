/* $Id: MainTask2.java 2020/11/30 18:24:11 davvids $ */
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
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainTask2 extends AbstractQuest {

	//this quest slot
	private static final String QUEST_SLOT = "main_task_2";

	private static final String FIRST_QUEST_SLOT = "cheese_for_soro";
	private static final String SECOND_QUEST_SLOT = "kill_gnomes";
	private static final String THIRD_QUEST_SLOT = "sheep_growing";
	
	private static final String MAIN1 = "main_task_1";
	
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
		res.add("Zanim Mistrz Faumonii II przyzna mi kolejne zadanie musze pomóc innym mieszkancom Semos. Powinienem porozmawiać z pasterzem Nishiya, młynarką Jenny oraz smakoszem sera Soro.");
		if (!isCompleted(player)) {
			res.add("Żeby ukończyć główne zadanie musze przynieść " + Grammar.a_noun(player.getRequiredItemName(QUEST_SLOT,0)) + ".");
		}
		if (isCompleted(player)) {
			res.add("Ukończyłem drugie zadanie od Mistrza Faumonii i otrzymałem odznakę wojownika Faumonii drugiego poziomu.	Następne główne zadanie otrzymam po przekroczeniu 10 poziomu doświadczenia.	W międzyczasie mogę wykonać zadania u Hackima Easso w kuźni Semos, u Eonny w magazynie Semos oraz u Hayunna Naratha w domku startowym Semos. ");
		}
		return res;
	}
	
		private void checkCollectingQuests() {
			final SpeakerNPC npc = npcs.get("Mistrz Faumonii II");
			
			npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestCompletedCondition(MAIN1),
				new LevelGreaterThanCondition(4),
				new GreetingMatchesNameCondition(npc.getName()),
				new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Witaj, mam dla ciebie główne #zadanie drugiego poziomu.",
			null);
			
			npc.add(ConversationStates.ATTENDING,
				Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
				new AndCondition(new QuestCompletedCondition(MAIN1),
				new LevelGreaterThanCondition(4),
				new QuestNotCompletedCondition(FIRST_QUEST_SLOT),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Zanim przyznam ci kolejne główne zadanie musisz wybrać się do Soro w mieście Semos i dostarczyć mu solidną porcję sera. Zazwyczaj przesiaduje w pobliżu tego budynku",
				null);
				
			npc.add(ConversationStates.ATTENDING,
				Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
				new AndCondition(new QuestCompletedCondition(MAIN1),
				new LevelGreaterThanCondition(4),
				new QuestNotCompletedCondition(SECOND_QUEST_SLOT),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Zanim przyznam ci kolejne główne zadanie musisz wybrać się do młynarki Jenny i pomóc jej z gnomami. Znajdziesz ją mapę w prawo i do góry, w pobliżu ogromnej farmy.",
				null);	
						
			npc.add(ConversationStates.ATTENDING,
				Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
				new AndCondition(new QuestCompletedCondition(MAIN1),
				new LevelGreaterThanCondition(4),
				new QuestNotCompletedCondition(THIRD_QUEST_SLOT),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Zanim przyznam ci kolejne główne zadanie powinieneś nauczyć się opiekować zwierzętami. Idź do pasterza Nishiya który krąży przed tym budynkiem i wykonaj dla niego zadanie.",
				null);
				
		}
		

	private void requestItem() {
		
		final SpeakerNPC npc = npcs.get("Mistrz Faumonii II");
		final Map<String,Integer> items = new HashMap<String, Integer>();
		
		
		
		items.put("hełm wikingów",1);
		items.put("misiurka",1);
		items.put("zbroja płytowa",1);
		items.put("tarcza z czaszką",1);
		items.put("zbroja łuskowa",1);
		items.put("lwia tarcza",1);
		
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
				new AndCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestCompletedCondition(MAIN1),
						new LevelGreaterThanCondition(4),
						new QuestCompletedCondition(SECOND_QUEST_SLOT),
						new QuestCompletedCondition(FIRST_QUEST_SLOT),
						new QuestCompletedCondition(THIRD_QUEST_SLOT)),
					ConversationStates.ATTENDING,
					null,
					new StartRecordingRandomItemCollectionAction(QUEST_SLOT, items, "Wykonałeś już zadania o które cie prosiłem. Czas abyś nauczył się sprzedawać i kupować przedmioty. Idź do Xin Blanca w tawernie Semos i"
						+ " kup mi [item]. Po prostu zapytaj o #ofertę. Jeżeli nie masz pieniędzy to sprzedaj statuetkę aniołka u Furgo w banku Semos, pytając o #ofertę.Pamiętaj, że możesz sprawdzić liste dostępnych sklepów w grze klikając w #'Menu' w prawym górnym rogu a następnie #Lista #Sklepów"));
	}
						
	private void collectItem() {
			final SpeakerNPC npc = npcs.get("Mistrz Faumonii II");
			
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
				"Nauczyłeś się sprzedawać i kupować przedmioty. Zasługujesz na odznakę wojownika Faumonii drugiego poziomu. Następne główne zadanie otrzymasz po przekroczeniu 10 poziomu doświadczenia. W międzyczasie możesz wykonać zadania u Hackima Easso w kuźni Semos, u Eonny w magazynie Semos oraz u Hayunna Naratha w domku startowym Semos. ",
				new MultipleActions(new DropRecordedItemAction(QUEST_SLOT), 
									new SetQuestAction(QUEST_SLOT, "done"),
									new DropItemAction("odznaka wojownika I",1),
									new EquipItemAction("odznaka wojownika II", 1, true)));
																
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
				"Wojownik Faumonii 2",
				"Mistrz Faumonii II ma dla ciebie drugie główne zadanie które pomoże ci zostać wielkim wojownikiem Faumonii.",
				true);
		
		checkCollectingQuests();
		requestItem();
		collectItem();

	}	
	
		@Override
	public String getName() {
		return "MainTask2";
	}
	
	@Override
	public int getMinLevel() {
		return 5;
	}

	@Override
	public String getNPCName() {
		return "Mistrz Faumonii II";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}
}
		
	        	