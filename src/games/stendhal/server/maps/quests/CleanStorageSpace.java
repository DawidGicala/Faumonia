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

import games.stendhal.server.entity.npc.action.DialogueRewardAction;

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
		res.add("Spotkałem Eonnę w domu obok piekarni w Semos.");
		final String questState = player.getQuest(QUEST_SLOT, 0);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę wyczyścić jej piwnicy z potworów.");
		return res;	
		}
		res.add("Obiecałem Eonnie zabić szczury i węże w piwnicy.");
		if (("start".equals(questState) && new KilledForQuestCondition(QUEST_SLOT, 1).fire(player, null, null)) || "done".equals(questState)) {
		res.add("Pokonałem wymagane szkodniki od chwili przyjęcia zadania. Mogę wrócić do Eonny.");
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
				"Z piwnicy znów dochodzi chrobotanie, a sama miotła już nie wystarcza. Trzeba rozprawić się ze szczurem, szczurem jaskiniowym i wężem, zanim dobiorą się do zapasów. Zejdziesz na dół i mi pomożesz?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Wciąż czekam na wieści z piwnicy. Trzeba pokonać po jednym szczurze, szczurze jaskiniowym i wężu od chwili przyjęcia mojej prośby. Zapytaj o #postęp, a sprawdzimy, co jeszcze zostało.", null);

		final List<ChatAction> start = new LinkedList<ChatAction>();
		
		final HashMap<String, Pair<Integer, Integer>> toKill = 
			new HashMap<String, Pair<Integer, Integer>>();
		// first number is required solo kills, second is required shared kills
		toKill.put("szczur", new Pair<Integer, Integer>(0,1));
		toKill.put("szczur jaskiniowy", new Pair<Integer, Integer>(0,1));
		toKill.put("wąż", new Pair<Integer, Integer>(0,1));

		start.add(DialogueRewardAction.karma(new IncreaseKarmaAction(2.0)));
		start.add(new SetQuestAction(QUEST_SLOT, 0, "start"));
		start.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(
				ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Uff, dobrze mieć kogoś do pomocy. Schody tutaj prowadzą do piwnicy; rozpraw się z jednym szczurem, jednym szczurem jaskiniowym i jednym wężem. Ja zostanę na górze z miotłą, gdyby któryś próbował przemknąć obok ciebie.",
				new MultipleActions(start));

		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Szkoda, sama nie odważę się zejść do tych szkodników. Jeszcze trochę popilnuję schodów z miotłą. Jeśli zmienisz zdanie, wróć - zapasy same się nie obronią.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -2.0));

		npc.add(
				ConversationStates.QUEST_OFFERED,
				Arrays.asList("piwnica", "piwnicy", "basement", "storage space"),
				null,
				ConversationStates.QUEST_OFFERED,
				"Zejdź schodami w tym domu do piwnicy. Szukaj szczura, szczura jaskiniowego i węża - trzeba pokonać po jednym z każdego rodzaju, a możesz korzystać z pomocy towarzyszy. Podejmiesz się tego?",
				null);
	}

	private void step_2() {
		// Go kill at least a rat, a cave rat and a snake.
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Eonna");
		
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(DialogueRewardAction.karma(new IncreaseKarmaAction(25.0)));
		reward.add(DialogueRewardAction.xp(1000));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
        npc.add(ConversationStates.ATTENDING, "postęp",
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"), new KilledForQuestCondition(QUEST_SLOT, 1)),
            ConversationStates.ATTENDING,
            "Wszystkie trzy rodzaje szkodników zostały pokonane. Wreszcie mogę zająć się zapasami zamiast nasłuchiwać przy schodach. Dziękuję za pomoc!", new MultipleActions(reward));
        npc.add(ConversationStates.ATTENDING, "postęp",
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"), new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
            ConversationStates.ATTENDING,
            "Jeszcze nie wszystkie szkodniki zostały pokonane. Sprawdź naszą listę: trzeba po jednym szczurze, szczurze jaskiniowym i wężu od przyjęcia zadania. Schody do piwnicy są tutaj, a ja poczekam na górze.", null);
        npc.add(ConversationStates.ATTENDING, "wskazówki", new QuestInStateCondition(QUEST_SLOT, 0, "start"), ConversationStates.ATTENDING,
            "Zejdź schodami w tym domu do piwnicy. Pokonaj po jednym szczurze, szczurze jaskiniowym i wężu; możesz walczyć razem z towarzyszami. Potem wróć i przywitaj się albo zdaj sprawę z postępu.", null);


		// the player returns to Eonna after having started the quest.
		// Eonna checks if the player has killed one of each animal race.
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "start"), new KilledForQuestCondition(QUEST_SLOT,1)),
				ConversationStates.ATTENDING, "Nareszcie mogę odetchnąć i zajrzeć do zapasów bez ściskania miotły jak włóczni. Wszystkie trzy rodzaje szkodników zostały pokonane. Dziękuję, naprawdę oszczędziło mi to strachu!",
				new MultipleActions(reward));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "start"), new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING,
				"Dobrze cię widzieć, ale w piwnicy wciąż zostało coś do zrobienia. Potrzebuję pomocy ze szczurem, szczurem jaskiniowym i wężem. Możemy sprawdzić #postęp, zanim zejdziesz znowu na dół.",
				null);

		npc.add(
				ConversationStates.QUEST_STARTED,
				Arrays.asList("basement", "piwnicy", "piwnica"),
				null,
				ConversationStates.ATTENDING,
				"Wejście do piwnicy jest na dole schodów w tym domu. Pokonaj po jednym szczurze, szczurze jaskiniowym i wężu; liczy się też wspólna walka z towarzyszami. Potem wróć do mnie na górę.",
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
