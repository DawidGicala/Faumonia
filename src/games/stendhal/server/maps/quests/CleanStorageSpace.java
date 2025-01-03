/* $Id: CleanStorageSpace.java,v 1.50 2011/11/13 17:12:18 kymara Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
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

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.Pair;

/**
 * QUEST: CleanStorageSpace
 * <p>
 * PARTICIPANTS:
 * <li> Eonna
 * <p>
 * STEPS:
 * <li> Eonna asks you to clean her storage space.
 * <li> You go kill at least a rat, a cave rat and a cobra.
 * <li> Eonna checks your kills and then thanks you.
 * <p>
 * REWARD:
 * <li> 100 XP, karma
 * <p>
 * REPETITIONS:
 * <li> None.
 */
public class CleanStorageSpace extends AbstractQuest {
	private static final String QUEST_SLOT = "clean_storage";

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
		res.add("Spotkałem Eonne w jej domu obok piekarni.");
		final String questState = player.getQuest(QUEST_SLOT, 0);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę wyczyścić jej piwnicy z potworów.");
		return res;	
		}
		res.add("Obiecałem Eonnie zabić szczury i węże w piwnicy.");
		if (("start".equals(questState) && player.hasKilled("szczur") && player.hasKilled("szczur jaskiniowy") && player.hasKilled("wąż")) || "done".equals(questState)) {
		res.add("Musze wyczyścić piwnicę Eonny z gryzoni i węży.");
		}
		if ("done".equals(questState)) {
			res.add("Eonna podziękowała mi i nazwała mnie swoim bohaterem.	W nagrodę za ukończenie zadania otrzymałem: 1.000 pkt doświadczenia i 25 pkt karmy.");
		}
		return res;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Eonna");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Moja #piwnica jest pełna szczurów. Pomożesz mi?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Dziękuję jeszcze raz! Sądzę, że na dole jest nadal czysto.", null);

		final List<ChatAction> start = new LinkedList<ChatAction>();
		
		final HashMap<String, Pair<Integer, Integer>> toKill = 
			new HashMap<String, Pair<Integer, Integer>>();
		// first number is required solo kills, second is required shared kills
		toKill.put("szczur", new Pair<Integer, Integer>(0,1));
		toKill.put("szczur jaskiniowy", new Pair<Integer, Integer>(0,1));
		toKill.put("wąż", new Pair<Integer, Integer>(0,1));

		start.add(new IncreaseKarmaAction(2.0));
		start.add(new SetQuestAction(QUEST_SLOT, 0, "start"));
		start.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(
				ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Och, dziękuję! Poczekam tutaj, a jeżeli spróbują uciec to uderzę je moją miotłą!",
				new MultipleActions(start));

		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				"*chlip* Cóż może ktoś inny będzie moim bohaterem...",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -2.0));

		npc.add(
				ConversationStates.QUEST_OFFERED,
				Arrays.asList("piwnica", "piwnicy", "basement", "storage space"),
				null,
				ConversationStates.QUEST_OFFERED,
				"Tak, idź na dół po schodach. Tam jest cała gromada obrzydliwych szczurów. Chyba widziałam tam też węża. Powinieneś uważać... wciąż chcesz mi pomóc?",
				null);
	}

	private void step_2() {
		// Go kill at least a rat, a cave rat and a snake.
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Eonna");
		
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new IncreaseKarmaAction(25.0));
		reward.add(new IncreaseXPAction(1000));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));

		// the player returns to Eonna after having started the quest.
		// Eonna checks if the player has killed one of each animal race.
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "start"), new KilledForQuestCondition(QUEST_SLOT,1)),
				ConversationStates.ATTENDING, "Nareszcie bohater! Dziękuję!",
				new MultipleActions(reward));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "start"), new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.QUEST_STARTED,
				"Nie pamiętasz, że obiecałeś mi pomóc w oczyszczeniu mojej #piwnicy ze szczurów?",
				null);

		npc.add(
				ConversationStates.QUEST_STARTED,
				Arrays.asList("basement", "piwnicy", "piwnica"),
				null,
				ConversationStates.ATTENDING,
				"Tak jak powiedziałam w dół schodami. Proszę wyczyść ze wszystkich szczurów i zobacz czy nie ma tam węża!",
				null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Porządki w Piwnicy",
				"Eonna posiada piwnicę, w której zagnieździły się szczury i węże. Potrzebuje mnie, prawdziwego bohatera, abym jej pomógł.",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "CleanStorageSpace";
	}
	
	@Override
	public int getMinLevel() {
		return 0;
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Eonna";
	}
}
