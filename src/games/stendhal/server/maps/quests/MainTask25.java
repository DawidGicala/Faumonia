/* $Id: MainTask25.java,v 1.18 2020/12/08 1:20:17 davvids Exp $ */
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

public class MainTask25 extends AbstractQuest {

	private static final String QUEST_SLOT = "main_task_25";
	
	private static final String STONE = "kill_stone";
	private static final String KIKA = "kill_kika";
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Mistrz Faumonii XXV");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new LevelGreaterThanCondition(319)),
				ConversationStates.QUEST_OFFERED,
				"Witaj, mam dla ciebie główne zadanie dwudziestego-piątego poziomu."
				+ " Jesteś zainteresowany?",
				null);

		final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("dwugłowy niebieski smok", new Pair<Integer, Integer>(0,2));
		toKill.put("błękitny smok", new Pair<Integer, Integer>(0,2));
		toKill.put("zielony smok", new Pair<Integer, Integer>(0,1));
		toKill.put("dwugłowy zielony smok", new Pair<Integer, Integer>(0,2));
		toKill.put("czarny szkielet gigant", new Pair<Integer, Integer>(0,2));
		toKill.put("olbrzymia wieża", new Pair<Integer, Integer>(0,1));
		toKill.put("wieża", new Pair<Integer, Integer>(0,2));
		toKill.put("gigantyczny kamienny golem", new Pair<Integer, Integer>(0,1));
		toKill.put("lawina kamienna", new Pair<Integer, Integer>(0,1));
		toKill.put("dwugłowy czarny smok", new Pair<Integer, Integer>(0,1));
		toKill.put("złoty smok", new Pair<Integer, Integer>(0,1));
		toKill.put("czarny smok", new Pair<Integer, Integer>(0,1));
		toKill.put("czarne smoczysko", new Pair<Integer, Integer>(0,1));
		toKill.put("coś", new Pair<Integer, Integer>(0,1));
		toKill.put("orzeł gigant", new Pair<Integer, Integer>(0,1));
		toKill.put("coś niszczącego", new Pair<Integer, Integer>(0,1));
		toKill.put("królowa pająków", new Pair<Integer, Integer>(0,2));
		toKill.put("czerwony smok", new Pair<Integer, Integer>(0,2));
		toKill.put("upadły anioł", new Pair<Integer, Integer>(0,2));
		toKill.put("olbrzymi ptasznik", new Pair<Integer, Integer>(0,1));
		toKill.put("czarna śmierć", new Pair<Integer, Integer>(0,1));
		toKill.put("książę szkieletów", new Pair<Integer, Integer>(0,1));
		toKill.put("balrog", new Pair<Integer, Integer>(0,1));
		toKill.put("anioł", new Pair<Integer, Integer>(0,3));
		toKill.put("anioł ciemności", new Pair<Integer, Integer>(0,2));
		toKill.put("archanioł", new Pair<Integer, Integer>(0,3));
		toKill.put("archanioł ciemności", new Pair<Integer, Integer>(0,1));
		toKill.put("pegaz", new Pair<Integer, Integer>(0,1));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Czas na prawdziwe wyzwania. Wybierz się do kopalni krasnoludów w Orril, odnajdź Serha i wykonaj dla niego zadanie. Nastepnie wybierz się na północ od miasta Semos, odnajdź Lucka i zbadaj tajemniczą kryptę. Na sam koniec wybierz się do Kikareukin i wybij #wszystkie potwory.",
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

		final SpeakerNPC npc = npcs.get("Mistrz Faumonii XXV");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new DropItemAction("odznaka wojownika XXIV",1));
	    actions.add(new EquipItemAction("odznaka wojownika XXV", 1));
		actions.add(new SetQuestAction(QUEST_SLOT, "done;1"));	
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		
		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);		
		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new QuestCompletedCondition(STONE),
						new QuestCompletedCondition(KIKA),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.ATTENDING, 
				"Widze, że wykonałeś swoje dwudzieste-piąte zadanie. Wykonałeś zadanie dla przerażonego krasnoluda Serha, zwiedziłeś opuszczoną kryptę i zabiłeś wszystkie potwory na Kikareukin. "
				+ "Zasługujesz na odznakę wojownika Faumonii dwudziestego-piątego poziomu. Po nastepne główne zadanie powinieneś przyjść do Mistrza Faumonii XXV po przekroczeniu 350 poziomu doświadczenia.",
				new MultipleActions(actions));

				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new QuestCompletedCondition(STONE))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Powinieneś wybrać się na północ, odnaleźć Lucka i poznać tajemniczą krypte.",
				null);
				
				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new QuestCompletedCondition(KIKA))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Powinieneś wybrać się do kopalni krasnoludów, odnaleźć Serha i wykonać dla niego zadanie.",
				null);
				
				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Miałeś wybrać się do Kikareukin i zabić #wszystkie potwory.",
				null);
		
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Wojownik Faumonii 25",
				"Mistrz Faumonii XXV ma dla ciebie dwudzieste-piąte główne zadanie które pomoże ci zostać wielkim wojownikiem Faumonii.",
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
				res.add("Muszę wybrać się na północ, odnaleźć Lucka i zbadać tajemniczą kryptę.	Następnie powinienem wrócić do kopalni krasnoludów pod Orril, porozmawiać z Serhem i wykonać dla niego zadanie.	Na sam koniec, muszę wybrać się do Kikareukin i zabić wszystkie potwory.");
			} else if(isRepeatable(player)){
				res.add("Błąd gry. Zgłoś go administracji.");
			} else {
				res.add("Ukończyłem dwudzieste-piąte zadanie od Mistrza Faumonii i otrzymałem odznakę wojownika Faumonii dwudziestego-piątego poziomu.	Następne główne zadanie otrzymam po przekroczeniu 350 poziomu doświadczenia.");
			}
			return res;
	}


	@Override
	public String getName() {
		return "MainTask25";
	}
	
	@Override
	public int getMinLevel() {
		return 320; 
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"done").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Mistrz Faumonii XXV";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	
}
