/* $Id: MainTask23.java 2025/06/22 13:32:13 davvids $ */
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
import games.stendhal.server.core.rp.quest.MasterStage;
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

public class MainTask23 extends AbstractQuest {

	//this quest slot
	private static final String QUEST_SLOT = "main_task_23";
	
	private static final String FIRST_QUEST_SLOT = "main_task_3";
	private static final String SAUSID = "kill_sausid";
	private static final String WALT_SPIDER = "kill_ados_spiders";
	
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
		res.add("Zanim Mistrz Faumonii przyzna mi kolejne główne zadanie musze wykonać kilka zadań.	Powinienem wybrać się do stolicy Ados odszukać rodzinę Walta i pomóc z pająkami..	Powinienem wybrac się również na południe, do miasta Fado i wykonać tajmnicze zadanie dla Sausida.");
		if (!isCompleted(player)) {
			res.add("Żeby ukończyć główne zadanie musze przynieść " + Grammar.a_noun(player.getRequiredItemName(QUEST_SLOT,0)) + ".");
		}
		if (isCompleted(player)) {
			res.add("Ukończyłem dwudzieste-trzecie zadanie od Mistrza Faumonii i otrzymałem odznakę wojownika Faumonii dwudziestego-trzeciego poziomu.	Następne główne zadanie otrzymam po przekroczeniu 280 poziomu doświadczenia.");
		}
		return res;
	}
	
	private void checkCollectingQuests() {
		final MasterStage npc = MasterStage.register(23, this);
		
		npc.add(
		ConversationStates.IDLE,
		ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new QuestCompletedCondition(MAIN1),
			new QuestCompletedCondition(MAIN2),
			new LevelGreaterThanCondition(259),
			new GreetingMatchesNameCondition(npc.getName()),
			new QuestNotStartedCondition(QUEST_SLOT)),
		ConversationStates.ATTENDING,
		"Witaj, mam dla ciebie główne #zadanie dwudziestego-trzeciego poziomu.",
		null);
		
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
			new AndCondition(new QuestCompletedCondition(MAIN1),
			new QuestCompletedCondition(MAIN2),
			new LevelGreaterThanCondition(259),
			new QuestNotCompletedCondition(SAUSID),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Zanim przyznam ci kolejne główne zadanie, powinieneś pomóc Sausidowi. W tym celu wybierz się karczmy w Fado i wykonaj tajne zadanie.",
			null);
			
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
			new AndCondition(new QuestCompletedCondition(MAIN1),
			new QuestCompletedCondition(MAIN2),
			new LevelGreaterThanCondition(259),
			new QuestNotCompletedCondition(FIRST_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Zanim przyznam ci kolejne główne zadanie, czas abyś trochę powalczył. W tym celu wybierz się do stolicy Ados, odszukaj Verdana w ratuszu i pokonaj dla niego armie Superczłowieków.",
			null);	
			
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
			new AndCondition(new QuestCompletedCondition(MAIN1),
			new QuestCompletedCondition(MAIN2),
			new LevelGreaterThanCondition(259),
			new QuestNotCompletedCondition(WALT_SPIDER),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Zanim przyznam ci kolejne główne zadanie, musisz pomóc rodzinie Walta z pajęczakami. W tym celu wybierz się stolicy Ados i pomóż Waltowi. Jego posiadłości powinieneś szukać na południu.",
			null);	
			
	}

	private void requestItem() {
		
		final MasterStage npc = MasterStage.register(23, this);
		final Map<String,Integer> items = new HashMap<String, Integer>();
		
		items.put("gruczoł przędzy",42);
		items.put("gruczoł przędzy",34);
		items.put("gruczoł przędzy",39);
		items.put("gruczoł przędzy",63);
		items.put("gruczoł przędzy",49);
		items.put("gruczoł przędzy",56);
		
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
				new AndCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestCompletedCondition(MAIN1),
						new QuestCompletedCondition(MAIN2),
						new LevelGreaterThanCondition(259),
						new QuestCompletedCondition(SAUSID),
						new QuestCompletedCondition(FIRST_QUEST_SLOT),
						new QuestCompletedCondition(WALT_SPIDER)),
					ConversationStates.ATTENDING,
					null,
					new StartRecordingRandomItemCollectionAction(QUEST_SLOT, items, "Wykonałeś już zadania o które cie prosiłem, pomogłeś rodzinie Walta i wykonałeś tajmnicze zadanie dla Sausida."
						+ ". Potrzebuje jeszcze czegoś... Powinieneś nazbierać tego sporo podczas walki z pajęczakami. Przynieś mi: [item]."));
	}
						
	private void collectItem() {
			final MasterStage npc = MasterStage.register(23, this);
			
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
				"Pomogłeś rodzinie Walta, wykonałeś tajne zadanie Sausida oraz dostarczyłeś gruczoły przędzy. Zasługujesz na odznakę wojownika Faumonii dwudziestego-trzeciego poziomu. Następne główne zadanie otrzymasz po przekroczeniu 280 poziomu doświadczenia. ",
				new MultipleActions(new DropRecordedItemAction(QUEST_SLOT), 
									new SetQuestAction(QUEST_SLOT, "done"),
									new DropItemAction("odznaka wojownika XXII",1),
									new EquipItemAction("klucz do tawerny23", 1, true),
									new EquipItemAction("odznaka wojownika XXIII", 1, true)));
																
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
				"Wojownik Faumonii 23",
				"Mistrz Faumonii ma dla ciebie dwudzieste-trzecie główne zadanie które pomoże ci zostać wielkim wojownikiem Faumonii.",
				true);
		
		checkCollectingQuests();
		requestItem();
		collectItem();

	}	
	
		@Override
	public String getName() {
		return "MainTask23";
	}
	
	@Override
	public int getMinLevel() {
		return 260;
	}

	@Override
	public String getNPCName() {
		return MasterStage.NPC_NAME;
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}
}
		
	        	