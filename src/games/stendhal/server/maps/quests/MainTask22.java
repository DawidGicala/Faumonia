/* $Id: MainTask22.java,v 1.18 2024/12/18 20:22:42 davvids Exp $ */
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

public class MainTask22 extends AbstractQuest {

	private static final String QUEST_SLOT = "main_task_22";
	
	private static final String IMMORTAL_SWORD = "immortalsword_quest";
	private static final String KILL_STONE = "kill_stone";
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Mistrz Faumonii XXII");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new LevelGreaterThanCondition(249)),
				ConversationStates.QUEST_OFFERED,
				"Witaj, mam dla ciebie główne zadanie dziewiątego poziomu."
				+ " Jesteś zainteresowany?",
				null);

		final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("balourgh wojownik", new Pair<Integer, Integer>(0,60));
		toKill.put("balourgh żołnierz", new Pair<Integer, Integer>(0,60));
		toKill.put("balourgh lider", new Pair<Integer, Integer>(0,40));
		toKill.put("balourgh komandor", new Pair<Integer, Integer>(0,40));
		
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Czasz abyś pokazał, że umiesz walczyć. Zanim przyznam ci główne zadanie, idź do podziemnego kowala Vulcanusa i wykuj miecz nieśmiertelnych. Znajdziesz go w jaskini, na zachód od Semos. Następnie wybierz się na polanę na północ od miasta Semos, odszukaj Lucka, przyjmij zadanie i wyczyść pobliską krypte z potworów. Jak już to zrobisz to zabij 60 balorughtów wojowników, 60 balorughtów żołnierzy, 40 balorughtów liderów oraz 20 balorughtów komandorów aby udowodnić swoje męstwo. Powinieneś szukać ich w podziemiach rezydencji na południe od miasta Kirdneh. Na co czekasz? Do roboty!",
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

		final SpeakerNPC npc = npcs.get("Mistrz Faumonii XXII");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new DropItemAction("odznaka wojownika XXI",1));
	    actions.add(new EquipItemAction("odznaka wojownika XXII", 1));
		actions.add(new SetQuestAction(QUEST_SLOT, "done;1"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		
		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);		
		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new QuestCompletedCondition(IMMORTAL_SWORD),
						new QuestCompletedCondition(KILL_STONE),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.ATTENDING, 
				"Widze, że wykonałeś swoje dwudziestę drugie, poznałeś Vulcanusa i wykułeś miecz nieśmiertelnych, pokonałeś potwory w kamiennej krypcie na północ od Semos oraz pokonałeś armie balorughtów. "
				+ "Zasługujesz na odznakę wojownika Faumonii dwudziestego drugiego poziomu, tym razem zwiększy ona poziom twojej obrony. Po nastepne główne zadanie powinieneś przyjść do Mistrza Faumonii XXIII po przekroczeniu 260 poziomu doświadczenia.",
				new MultipleActions(actions));

				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new QuestCompletedCondition(IMMORTAL_SWORD))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Powinieneś wybrać się na zachód od miasta Semos, odszukać podziemnego kowala Vulcanusa i wykuć miecz nieśmiertelnych.",
				null);
				
				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new QuestCompletedCondition(KILL_STONE))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Powinieneś wybrać się na północ od centrum Semos, odszukać Luka i wyczyścić dla niego pobliską kamienną kryptę.",
				null);
				
				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Musisz udowodnić swoje męstwo pokonując armie balorughtów. Idź i zabij 60 balorughtów wojowników, 60 balorughtów żołnierzy, 40 balorughtów liderów oraz 20 balorughtów komandorów aby udowodnić swoje męstwo. Znajdziesz je w podziemiach willi na południe od miasta Kirdneh.",
				null);
		
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Wojownik Faumonii 22",
				"Mistrz Faumonii XXII ma dla ciebie dwudzieste drugie główne zadanie które pomoże ci zostać wielkim wojownikiem Faumonii.",
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
				res.add("Muszę odnaleźć kowala Vulcanusa i wykuć miecz nieśmiertelnych, podobno znajde go kierując się na zachod od Semos.	 Nastepie powinienem wybrać się na północ od centrum Semos, odszukać Lucka i wyczyścić dla niego kamienną kryptę.	Następnie muszę udowodnić swoje męstwo i zabić 60 balorughtów wojowników, 60 balorughtów żołnierzy, 40 balorughtów liderów oraz 20 balorughtów komandorów. 	Balorughty powinienem znaleźć w podziemiach willi zlokalizowanej na południe od miasta Kirdneh.");
			} else if(isRepeatable(player)){
				res.add("Błąd gry. Zgłoś go administracji.");
			} else {
				res.add("Ukończyłem dwudzieste drugie zadanie od Mistrza Faumonii i otrzymałem odznakę wojownika Faumonii dwudziestego drugiego poziomu.	Następne główne zadanie otrzymam po przekroczeniu 260 poziomu doświadczenia.");
			}
			return res;
	}


	@Override
	public String getName() {
		return "MainTask22";
	}
	
	@Override
	public int getMinLevel() {
		return 250;
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"done").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Mistrz Faumonii XXII";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	
}
