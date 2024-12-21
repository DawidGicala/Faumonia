/* $Id: MainTask27.java,v 1.18 2020/12/08 13:17:17 davvids Exp $ */
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

import games.stendhal.common.MathHelper;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import marauroa.common.Pair;

public class MainTask27 extends AbstractQuest {

	private static final String QUEST_SLOT = "main_task_27";
	
	private static final String CLOAK = "mithril_cloak";
	private static final String SHIELD = "mithrilshield_quest";
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Mistrz Faumonii XXVII");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new LevelGreaterThanCondition(399)),
				ConversationStates.QUEST_OFFERED,
				"Witaj, mam dla ciebie główne zadanie dwudziestego-siódmego poziomu."
				+ " Jesteś zainteresowany?",
				null);

		final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("złota śmierć", new Pair<Integer, Integer>(0,50));
		toKill.put("srebrna śmierć", new Pair<Integer, Integer>(0,50));
		toKill.put("czarna śmierć", new Pair<Integer, Integer>(0,200));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Czas na prawdziwe wyzwania. Wybierz się do magicznego miasta, odszukaj Baldemara i wykonaj zadanie na tarczę z mithrilu. Następnie udaj się do Idy w mieście Ados i wykonaj zadanie na płaszcz z mithrilu. Na sam koniec udowodnij swoje męstwo zabijająć śmierci, zabij: 50 złotych śmierci, 50 srebrnych śmierci, 200 czarnych śmierci.",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED, 
				ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING,
				"Nie ma problemu. "
				+ "Wróć gdy zdecydujesz się zostać wojownikiem Faumonii. ",
				new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Mistrz Faumonii XXVII");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new DropItemAction("odznaka wojownika XXVI",1));
	    actions.add(new EquipItemAction("odznaka wojownika XXVII", 1));
		actions.add(new EquipItemAction("klucz do tawerny", 1));
		actions.add(new SetQuestAction(QUEST_SLOT, "done;1"));	
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		
		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);		
		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new QuestCompletedCondition(CLOAK),
						new QuestCompletedCondition(SHIELD),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.ATTENDING, 
				"Widze, że wykonałeś swoje dwudzieste-siódme zadanie. Zdobyłeś tarczę i płaszcz z mithrilu oraz udowodniłeś swoje męstwo zabijając śmierci. "
				+ "Zasługujesz na odznakę wojownika Faumonii dwudziestego-siódmego poziomu. Weź ten klucz, dzięki niemu dostaniesz się do sekretnego pokoju w tawernie Semos. Po nastepne główne zadanie powinieneś przyjść do Mistrza Faumonii XXVIII po przekroczeniu 450 poziomu doświadczenia. W międzyczasie możesz wykonać zadanie dla kolekcjonera w hotelu Fado, które zwiększy twoje maksymalne życie oraz wykuć pierścień nieśmiertelnych u kowala w Fado.",
				new MultipleActions(actions));

				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new QuestCompletedCondition(CLOAK))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Powinieneś wybrać się do idy w mieście Ados i zdobyć płaszcz z mithrilu.",
				null);
				
				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new QuestCompletedCondition(SHIELD))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Powinieneś wybrać się do magicznego miasta, odnaleźć Baldemara i zdobyć tarczę z mithrilu.",
				null);
				
				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Miałeś udowodnić swoje męstwo zabijająć 50 złotych śmierci, 50 srebrnych śmierci i 200 czarnych śmierci.",
				null);
		
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Wojownik Faumonii 27",
				"Mistrz Faumonii XXVII ma dla ciebie dwudzieste-siódme główne zadanie które pomoże ci zostać wielkim wojownikiem Faumonii.",
				false);
		step_1();
		step_2();
		step_3();
	}
	
	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			if (!isCompleted(player)) {
				res.add("Muszę wybrać się do Idy w mieście Ados i zdobyć płaszcz z mithrilu.	Następnie powinienem odnaleźć Baldemara w magicznym mieście i posiąść tarczę z mithrilu.	Na sam koniec muszę udowodnić swoje męstwo zabijając 50 złotych śmierci, 50 srebrnych śmierci i 200 czarnych śmierci.");
			} else if(isRepeatable(player)){
				res.add("Błąd gry. Zgłoś go administracji.");
			} else {
				res.add("Ukończyłem dwudzieste-siódme zadanie od Mistrza Faumonii i otrzymałem odznakę wojownika Faumonii dwudziestego-siódmego poziomu oraz sekretny klucz do tawerny w Semos.	Następne główne zadanie otrzymam po przekroczeniu 450 poziomu doświadczenia.	W międzyczasie mogę wykonać zadanie u kolekcjonra w hotelu Fado oraz zdobyć pierścień nieśmiertelnych od kowala w Fado.");
			}
			return res;
	}


	@Override
	public String getName() {
		return "MainTask27";
	}
	
	@Override
	public int getMinLevel() {
		return 400; 
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"done").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Mistrz Faumonii XXVII";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	
}
