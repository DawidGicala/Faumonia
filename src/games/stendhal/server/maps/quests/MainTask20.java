/* $Id: MainTask20.java,v 1.18 2024/05/19 01:04:17 davvids Exp $ */
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

public class MainTask20 extends AbstractQuest {

	private static final String QUEST_SLOT = "main_task_20";
	
	private static final String CHAOSTUNNELS = "chaos_tunnels";
	private static final String KILLCHAOSMONSTERS = "kill_chaos_monsters";
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Mistrz Faumonii XX");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new LevelGreaterThanCondition(239)),
				ConversationStates.QUEST_OFFERED,
				"Witaj, mam dla ciebie główne zadanie dwudziestego poziomu."
				+ " Jesteś zainteresowany?",
				null);

		final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("lodowy olbrzym", new Pair<Integer, Integer>(0, 20));
		toKill.put("czarny olbrzym", new Pair<Integer, Integer>(0, 20));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Czas abyś wybrał się na dokładną eksplorację podziemi chaosów pod Semos. W tym celu, wybierz się do Ivara w ratuszu Ados i wykonaj dla niego zadanie na pokonanie potworów nawiedzających tunele. Następnie odszukaj Thorgala, wędrowce spacerującego w tunelach chaosów pod Semos i dostarcz mu przedmioty chaosu. Na sam koniec aby udowodnić swoję męstwo idź i zabij 20 czarnych olbrzymów i 20 lodowych olbrzymów! Na co czekasz? Do roboty!",
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

		final SpeakerNPC npc = npcs.get("Mistrz Faumonii XX");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new DropItemAction("odznaka wojownika XIX",1));
	    actions.add(new EquipItemAction("odznaka wojownika XX", 1));
		actions.add(new EquipItemAction("klucz do tawerny6", 1));
		actions.add(new EquipItemAction("klucz czarnoksiężników chaosu", 1));
		actions.add(new SetQuestAction(QUEST_SLOT, "done;1"));	
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		
		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);		
		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new QuestCompletedCondition(CHAOSTUNNELS),
						new QuestCompletedCondition(KILLCHAOSMONSTERS),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.ATTENDING, 
				"Widze, że wykonałeś swoje dwudzieste zadanie. Mocno zwiedziłeś podziemia chaosów pod Semos, pomogłeś Thorgalowi i Ivarowi oraz zyskałeś możliwość zakupu zwoi chaosu. Ponadto udowodniłeś swojeg męstwo zabijajać 20 lodowych olbrzymów i 20 czarnych olbrzymów. "
				+ "Zasługujesz na odznakę wojownika Faumonii dwudziestego poziomu. W nagrodę za ukończenie tego zadania weź ten klucz czarnoksiężników chaosu. Pozwoli Ci on dostać się do tajemniczej krypty czarnoksiężników chaosu pełnej bogactw. Po nastepne główne zadanie powinieneś przyjść do Mistrza Faumonii XXI po przekroczeniu 250 poziomu doświadczenia.",
				new MultipleActions(actions));

				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new QuestCompletedCondition(CHAOSTUNNELS))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Powinieneś wybrać się do podziemi chaosów pod Semos, odszukać Thorgala i wykonać dla niego zadanie na przyniesienie przedmiotów chaosu.",
				null);
				
				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new QuestCompletedCondition(KILLCHAOSMONSTERS))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Powinieneś wybrać się do stolicy Ados, odszukać Ivara w ratuszu Semos i wykonać dla niego zadanie na pokonanie silnych potworów w tunelach chaosu pod miastem.",
				null);
				
				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Miałeś udowodnić swoje męstwo zabijając 20 czarnych olbrzymów i 20 lodowych olbrzymów!",
				null);
		
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Wojownik Faumonii 20",
				"Mistrz Faumonii XX ma dla ciebie dwudzieste główne zadanie które pomoże ci zostać wielkim wojownikiem Faumonii.",
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
				res.add("Muszę wybrać się do stolicy Ados, odszukać Ivara i pokonać potwory w tunelach chaosów pod miastem.	Następnie powinienem wrócić do tuneli chaosów pod Semos, odszukać Thorgala i wykonać dla niego zadanie polegające na przyniesieniu przedmiotó chaosu.	Na sam koniec powinienem udowodnić swoje męstwo zabijając 20 czarnych olbrzymów i 20 lodowych olbrzymów.");
			} else if(isRepeatable(player)){
				res.add("Błąd gry. Zgłoś go administracji.");
			} else {
				res.add("Ukończyłem dwudzieste zadanie od Mistrza Faumonii i otrzymałem odznakę wojownika Faumonii dwudziestego poziomu.	Następne główne zadanie otrzymam po przekroczeniu 250 poziomu doświadczenia.");
			}
			return res;
	}


	@Override
	public String getName() {
		return "MainTask20";
	}
	
	@Override
	public int getMinLevel() {
		return 240;
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"done").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Mistrz Faumonii XX";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	
}
