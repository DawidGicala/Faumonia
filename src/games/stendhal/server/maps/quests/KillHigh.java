/* $Id: KillHigh.java,v 1.18 2012/04/19 18:26:42 kymara Exp $ */
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
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
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

/**
 * QUEST: Kill Gnomes
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li> Octavio, by the mill in Semos Plains
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Gnomes have been stealing carrots so Octavio asks you to kill some.
 * <li> You go kill the Gnomes in the gnome village and you get the reward from Octavio
 * </ul>
 * <p>
 * REWARD:
 * <ul>
 * <li> 3 potions
 * <li> 100 XP
 * <li> No karma (deliberately. Killing gnomes is mean!)
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li> after 7 days.
 * </ul>
 */

public class KillHigh extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeHuntDetails {

	private static final String QUEST_SLOT = "kill_high4";
	private static final int WEEK_IN_MINUTES = MathHelper.MINUTES_IN_ONE_HOUR * 24 * 7 * 4 * 6;
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Octavio");

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new QuestNotStartedCondition(QUEST_SLOT),
                ConversationStates.QUEST_OFFERED,
                "Wiele widziałem, ale pewne imiona wciąż budzą niepokój w całej krainie. Potrzebuję wojownika gotowego na długą wyprawę przeciw najgroźniejszym stworzeniom. Podejmiesz to wyzwanie?",
                null);

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
						 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)),
                ConversationStates.QUEST_OFFERED,
                "Potężne stworzenia znów dają o sobie znać. Jeśli masz dość sił, mogę powierzyć ci następną wielką wyprawę.",
                null);

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
						 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES))),
                ConversationStates.ATTENDING,
                "Twoje zwycięstwa zostały rozliczone. Na kolejną taką wyprawę przyjdzie czas po 168 dniach od ostatniego meldunku.",
                null);

		
		final Map<String, Pair<Integer, Integer>> toKill = getDialogueHuntTargets(null);
		
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		
		npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.YES_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "To dziewiętnaście rodzajów przeciwników, a nie jedno polowanie w najbliższej jaskini. Przygotuj zapasy, sprawdzaj listę po każdej wyprawie i pamiętaj, że gashadokuro również jest wymagany." + "\nCele: " + games.stendhal.server.core.rp.quest.HuntQuestProgress.targetText(this,null) + ".",
                new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.NO_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "Dobrze znasz własne siły i to się liczy. Wróć, gdy będziesz gotowy na tak długą próbę.",
                new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Octavio");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new EquipItemAction("wielki eliksir", 1000));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
		actions.add(new IncreaseXPAction(10000000));
		actions.add(new IncreaseAtkXPAction(20000));
		actions.add(new IncreaseDefXPAction(20000));
		actions.add(new SetQuestAction(QUEST_SLOT, "killed;1"));
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
                "Niewielu wojowników wróciłoby z pełną listą takich zwycięstw. Przyjmij eliksiry i skrzynie, a przede wszystkim odpocznij; twoja wyprawa przyniosła krainie trochę spokoju.",
                new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING,
                triggers,
                new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
                ConversationStates.ATTENDING,
                null,
                (p, sentence, raiser) -> raiser.say("Twoja lista zwycięstw nie jest jeszcze pełna. Przejrzyjmy brakujące cele, żebyś wiedział, dokąd skierować następne kroki." + "\n" + games.stendhal.server.core.rp.quest.HuntQuestProgress.progressText(KillHigh.this,p)));
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Zabij silne potwory",
				"Octavio z Semos pamięta zagrożenia, z którymi nie poradzi sobie początkujący wojownik. Poznaj jego listę silnych potworów i przygotuj się do dłuższej wyprawy.",
				false);
		step_1();
		step_2();
		step_3();
	}
	
	@Override
	public List<String> getHistory(final Player player) {
        return games.stendhal.server.core.rp.quest.HuntQuestProgress.history(this,player);
    }


	@Override
	public String getName() {
		return "KillHigh";
	}
	
	@Override
	public int getMinLevel() {
		return 400;
	}
	
	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
				 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)).fire(player,null, null);
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"killed").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Octavio";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	

    @Override public java.util.Map<String, Pair<Integer,Integer>> getDialogueHuntTargets(Player player) {
        final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("czarna śmierć", new Pair<Integer, Integer>(0,1));
		toKill.put("upadły anioł", new Pair<Integer, Integer>(0,3));
		toKill.put("anioł ciemności", new Pair<Integer, Integer>(0,2));
		toKill.put("archanioł ciemności", new Pair<Integer, Integer>(0,1));
		toKill.put("kostucha wielka", new Pair<Integer, Integer>(0,2));
		toKill.put("kostucha złota wielka", new Pair<Integer, Integer>(0,1));
		toKill.put("kostucha różowa wielka", new Pair<Integer, Integer>(0,1));
		toKill.put("śmierć", new Pair<Integer, Integer>(0,10));
		toKill.put("kostucha", new Pair<Integer, Integer>(0,25));
		toKill.put("szkielet anioła", new Pair<Integer, Integer>(0,1));
		toKill.put("krasnal golem", new Pair<Integer, Integer>(0,3));
		toKill.put("balrog", new Pair<Integer, Integer>(0,1));
		toKill.put("arachne", new Pair<Integer, Integer>(0,2));
		toKill.put("lodowy olbrzym", new Pair<Integer, Integer>(0,10));
		toKill.put("elf ciemności matrona", new Pair<Integer, Integer>(0,3));
		toKill.put("chaosu lord wywyższony", new Pair<Integer, Integer>(0,5));
		toKill.put("gashadokuro", new Pair<Integer, Integer>(0,1));
		toKill.put("kasarkutominubat", new Pair<Integer, Integer>(0,3));
		toKill.put("kostucha różowa", new Pair<Integer, Integer>(0,10));
        return java.util.Collections.unmodifiableMap(toKill);
    }
    @Override public int getDialogueHuntTotal(Player player) { return 0; }
    @Override public int getDialogueKillIndex() { return 1; }
    @Override public String getDialogueHuntHint(Player player) { return "To długa wyprawa przeciw dziewiętnastu rodzajom silnych przeciwników. Gashadokuro jest jednym z wymaganych celów; kieruj się listą i wracaj po zapasy między walkami."; }
}
