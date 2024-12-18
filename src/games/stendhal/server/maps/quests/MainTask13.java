/* $Id: MainTask13.java,v 1.18 2020/12/03 13:33:22 davvids Exp $ */
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
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import marauroa.common.Pair;

public class MainTask13 extends AbstractQuest {

	private static final String QUEST_SLOT = "main_task_13";
	
	private static final String MEDIUMMONSTERS = "kill_medium_monsters";
	private static final String WEAPON = "weapons_collector";
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Mistrz Faumonii XIII");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new LevelGreaterThanCondition(99)),
				ConversationStates.QUEST_OFFERED,
				"Witaj, mam dla ciebie główne zadanie trzynastego poziomu."
				+ " Jesteś zainteresowany?",
				null);

		final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("gigantyczny krasnal", new Pair<Integer, Integer>(0,1));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Czasz abyś pokazał, że umiesz walczyć. Zanim przyznam ci główne zadanie wybierz się do tawerny Ados i wykonaj zadanie u Bate. Nastepnie wybierz się do kolekcjonera Balduina który zamieszkuje na szczycie wielkiej góry. Znajduje się ona pomiędzy miastem Semos i Ados, w pobliżu jeziora. Na koniec wybierz się do podziemi Semos i zabij gigantycznego krasnala. Na co czekasz? Do roboty!",
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

		final SpeakerNPC npc = npcs.get("Mistrz Faumonii XIII");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new DropItemAction("odznaka wojownika XII",1));
	    actions.add(new EquipItemAction("odznaka wojownika XIII", 1));
		actions.add(new SetQuestAction(QUEST_SLOT, "done;1"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		
		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);		
		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new QuestCompletedCondition(MEDIUMMONSTERS),
						new QuestCompletedCondition(WEAPON),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.ATTENDING, 
				"Widze, że wykonałeś swoje trzynaste zadanie. Zatrudniłeś się jako płatny zabójca u Bate, przyniosłeś bronie Balduinowi oraz zabiłeś gigantycznego krasnala. "
				+ "Zasługujesz na odznakę wojownika Faumonii trzynastego poziomu. Po nastepne główne zadanie powinieneś przyjść do Mistrza Faumonii XIV po przekroczeniu 110 poziomu doświadczenia. W międzyczasie możesz wykonać drugie zadanie u kolekcjonera Balduina, oferuje bardzo cenną nagrode.",
				new MultipleActions(actions));

				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new QuestCompletedCondition(MEDIUMMONSTERS))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Powinieneś odwiedzić Bate w tawernie Ados i wykonać dla niej zadanie.",
				null);
				
				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new QuestCompletedCondition(WEAPON))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Powinieneś odwiedzić Balduina mieszkającego na szczycie wielkiej góry. Znajduje się ona pomiędzy miastem Semos a Ados, w pobliżu jeziora.",
				null);
				
				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Miałeś wybrać się w podziemia Semos i zabić gigantycznego krasnala!",
				null);
		
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Wojownik Faumonii 13",
				"Mistrz Faumonii XIII ma dla ciebie trzynaste główne zadanie które pomoże ci zostać wielkim wojownikiem Faumonii.",
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
				res.add("Muszę wybrać się do tawerny w mieście Ados i wykonać zadanie u Bate.	Następnie muszę odnaleźć Balduina który zamieszkuje szczyt wielkiej góry. Podobno znajduje się ona pomiędzy miastem Semos i Ados w pobliżu jeziora.	Na koniec powinienem wybrać się do podziemi Semos i zabić gigantycznego krasnala.");
			} else if(isRepeatable(player)){
				res.add("Błąd gry. Zgłoś go administracji.");
			} else {
				res.add("Ukończyłem trzynaste zadanie od Mistrza Faumonii i otrzymałem odznakę wojownika Faumonii trzynastego poziomu.	Następne główne zadanie otrzymam po przekroczeniu 110 poziomu doświadczenia.	W międzyczasie mogę wykonać drugie zadanie u Balduina, podobno oferuje cenną nagrodę.");
			}
			return res;
	}


	@Override
	public String getName() {
		return "MainTask13";
	}
	
	@Override
	public int getMinLevel() {
		return 100;
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"done").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Mistrz Faumonii XIII";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	
}
