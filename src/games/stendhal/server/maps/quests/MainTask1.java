/* $Id: MainTask1.java,v 1.18 2020/11/30 11:43:42 davvids Exp $ */
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
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import marauroa.common.Pair;

public class MainTask1 extends AbstractQuest {

	private static final String QUEST_SLOT = "main_task_1";
	private static final int WEEK_IN_MINUTES = MathHelper.MINUTES_IN_ONE_HOUR * 999 * 999;
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Mistrz Faumonii I");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Jesteś tutaj nowy. Jeśli tylko chcesz to wyszkolę cie na najwiekszego wojownika Faumonii i naucze podstaw gry. "
				+ "Jesteś zainteresowany?",
				null);

		final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("szczur", new Pair<Integer, Integer>(0,10));
		toKill.put("szczur jaskiniowy", new Pair<Integer, Integer>(0,5));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Doskonale. Rozejrzyj się po okolicy i poznaj startowe miasto Semos. Jest to miasto w którym spotkasz wielu innych graczy. Pamiętaj, że zawsze możesz skorzystać z chatu globalnego i zapytać o to co cie trapi. W tym celu wpisz komende #/k #wiadomość. Twoje pierwsze zadanie polega na pokonaniu kilku potworów, wszystkie znajdziesz w okolicy. #Zabij #10 #szczurów #i #5 #szczurów #jaskiniowych. Jeśli nie wiesz gdzie znaleźć jakiegoś potwora to skorzystaj z chatu globalnego lub porozmawiaj ze Starkadem w banku Semos. Pamiętaj, że zawsze możesz sprawdzić status swojego zadania klikając w #Menu w prawym górnym rogu, a następnie #Dziennik #Zadań. W podobny sposób możesz sprawdzić mape całej krainy klikając w #Atlas #Map",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED, 
				ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING,
				"Nie ma problemu. Moge nauczyć cię podstaw gry, ale skoro nie chcesz... "
				+ "Wróć gdy zdecydujesz się zostać wojownikiem Faumonii. ",
				new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Mistrz Faumonii I");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
	    actions.add(new EquipItemAction("odznaka wojownika I", 1));
		actions.add(new SetQuestAction(QUEST_SLOT, "done;1"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		
		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);		
		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.ATTENDING, 
				"Widze, że wykonałeś swoje pierwsze zadanie i zabiłeś potwory. "
				+ "Zasługujesz na odznakę wojownika Faumonii pierwszego poziomu, zwiększy ona twoją obronę. Teraz czas opuścić wioskę startową, w tym celu spełnij ostatnie wymagania, ukończ samouczek i wybierz się na zwiedzanie Faumonii!. Po nastepne główne zadanie powinieneś przyjść do Mistrza Faumonii II po przekroczeniu 5 poziomu doświadczenia.",
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Powinieneś rozglądnać się po okolicy i poznać miasto Semos. Jeśli będziesz miał w czymś problem koniecznie odwiedź bank i porozmawiaj z tamtejszymi graczami lub skorzystaj z globalnego chatu wpisująć #/k #wiadomość. "
				+ "Twoje pierwsze zadanie polega na pokonaniu kilku potworów, wszystkie znajdziesz w okolicy. Zabij: 10 szczurów i 5 szczurów jaskiniowych a następnie wróć do mnie.",
				null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Wojownik Faumonii 1",
				"Mistrz Faumonii I ma dla ciebie pierwsze główne zadanie które pomoże ci zostać wielkim wojownikiem Faumonii.",
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
				res.add("Powinienem rozglądnąć się po Semos i zabić: 10 szczurów i 5 szczurów jaskiniowych.	Podobno Starkad w banku Semos pomoże mi znaleźć wszystkie potwory.	Zostałem poinformowany, że korzystając z globalnego chatu otrzymam pomoc od innych graczy, moge to zrobić przy pomocy komendy '/k wiadomość'");
			} else if(isRepeatable(player)){
				res.add("Błąd gry. Zgłoś go administracji.");
			} else {
				res.add("Ukończyłem pierwsze zadanie od Mistrza Faumonii i otrzymałem odznakę wojownika Faumonii pierwszego poziomu.	Następne główne zadanie otrzymam po przekroczeniu 5 poziomu doświadczenia.");
			}
			return res;
	}


	@Override
	public String getName() {
		return "MainTask1";
	}
	
	@Override
	public int getMinLevel() {
		return 0;
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"done").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Mistrz Faumonii I";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	
}
