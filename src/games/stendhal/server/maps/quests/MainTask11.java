/* $Id: MainTask11.java,v 1.18 2020/12/03 11:09:17 davvids Exp $ */
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

public class MainTask11 extends AbstractQuest {

	private static final String QUEST_SLOT = "main_task_11";
	
	private static final String DWARF = "cloaks_for_bario";
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Mistrz Faumonii XI");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new LevelGreaterThanCondition(74)), 
				ConversationStates.QUEST_OFFERED,
				"Witaj, mam dla ciebie główne zadanie jedenastego poziomu."
				+ " Jesteś zainteresowany?",
				null);

		final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("rycerz śmierci", new Pair<Integer, Integer>(0,20));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Przed murami stolicy zlokalizowane jest zoo, w jego pobliżu znajduje się jaskinia lecz wejście do niej znajduje się z innej strony. Wybierz się w góry i wejdź do niej, znajdziesz tam krasnala Bario, wykonaj dla niego zadanie. Następnie idź i zabij 20 rycerzy śmierci, najwięcej z nich znajdziesz na mokradłach na wschód od stolicy Ados. Na co czekasz? Do roboty!",
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

		final SpeakerNPC npc = npcs.get("Mistrz Faumonii XI");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new DropItemAction("odznaka wojownika X",1));
	    actions.add(new EquipItemAction("odznaka wojownika XI", 1));
		actions.add(new SetQuestAction(QUEST_SLOT, "done;1"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		
		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);		
		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new QuestCompletedCondition(DWARF),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.ATTENDING, 
				"Widze, że wykonałeś swoje jedenaste zadanie. Pomogłeś krasnalowi Bario i zdobyłeś cenne rękawice oraz zabiłeś 20 rycerzy śmierci. "
				+ "Zasługujesz na odznakę wojownika Faumonii jedenastego poziomu. Po nastepne główne zadanie powinieneś przyjść do Mistrza Faumonii XII po przekroczeniu 80 poziomu doświadczenia. W międzyczasie możesz wykonać zadanie na ulepszenie swojego sztyletu żywiołów. W tym celu powinieneś porozmawiać z Żywiołakiem Wody stacjonującym na molo w Ados.",
				new MultipleActions(actions));

				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new QuestCompletedCondition(DWARF))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Powinieneś odnaleźć jaskinie w pobliżu stołecznego zoo i pomóc znajdującemu się tam krasnalowi Bario.",
				null);
				
				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Miałeś wybrać się na mokradła, zlokalizowane na wschód od Ados i zabić 20 rycerzy śmierci!",
				null);
		
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Wojownik Faumonii 11",
				"Mistrz Faumonii XI ma dla ciebie jedenaste główne zadanie które pomoże ci zostać wielkim wojownikiem Faumonii.",
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
				res.add("Muszę odnaleźć jaskinie zlokalizowaną w pobliżu stołecznego zoo i pomóc znajdującemu się tam krasnalowi Bario.	Następnie powinienem wybrać się na mokradła znajdujące się na wschód od Ados i pokonać 20 rycerzy śmierci.");
			} else if(isRepeatable(player)){
				res.add("Błąd gry. Zgłoś go administracji.");
			} else {
				res.add("Ukończyłem jedenaste zadanie od Mistrza Faumonii i otrzymałem odznakę wojownika Faumonii jedenastego poziomu.	Następne główne zadanie otrzymam po przekroczeniu 80 poziomu doświadczenia.	W międzyczasie mogę ulepszyć swój sztylet żywiołów, w tym celu powinienem porozmawiać z Żywiołem Wody stacjonującym na molo w Ados.");
			}
			return res;
	}


	@Override
	public String getName() {
		return "MainTask11";
	}
	
	@Override
	public int getMinLevel() {
		return 75;
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"done").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Mistrz Faumonii XI";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	
}
