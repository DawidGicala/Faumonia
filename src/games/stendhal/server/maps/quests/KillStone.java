/* $Id: KillStone.java,v 1.18 2020/12/07 23:31:42 davvids Exp $ */
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
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
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
 * <li> Luck, by the mill in Semos Plains
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Gnomes have been stealing carrots so Luck asks you to kill some.
 * <li> You go kill the Gnomes in the gnome village and you get the reward from Luck
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

public class KillStone extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeHuntDetails {

	private static final String QUEST_SLOT = "kill_stone";
	private static final int WEEK_IN_MINUTES = MathHelper.MINUTES_IN_ONE_HOUR * 7 * 4;
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Luck");

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(new LevelGreaterThanCondition(249),
				new QuestNotStartedCondition(QUEST_SLOT)),
                ConversationStates.QUEST_OFFERED,
                "Pod Semos kryje się krypta, której mieszkańcy nie witają gości przyjaźnie. Potrzebuję doświadczonego wojownika, który wejdzie tam i zmierzy się z najgroźniejszymi stworzeniami. Pomożesz?",
                null);

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)),
                ConversationStates.QUEST_OFFERED,
                "Krypta znów wymaga sprawdzenia. Jeśli jesteś gotowy, podejmij kolejną wyprawę przeciw jej mieszkańcom.",
                null);

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES))),
                ConversationStates.ATTENDING,
                "Twoja ostatnia wyprawa jest już rozliczona. Na następną trzeba zaczekać 28 godzin od jej zakończenia.",
                null);

		final Map<String, Pair<Integer, Integer>> toKill = getDialogueHuntTargets(null);

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.YES_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "Odnajdź kryptę pod Semos i trzymaj się dokładnej listy. Szczególnie uważaj na gigantycznego kamiennego golema; dwóch superczłowieków olbrzymów również trzeba odnotować osobno." + "\nCele: " + games.stendhal.server.core.rp.quest.HuntQuestProgress.targetText(this,null) + ".",
                new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.NO_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "Rozumiem. W takiej krypcie przygotowanie jest ważniejsze od pośpiechu.",
                new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Luck");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseXPAction(1000000));
		actions.add(new IncreaseAtkXPAction(4000));
		actions.add(new IncreaseDefXPAction(6000));
		actions.add(new EquipItemAction("skrzynia skarbów V", 1));
		actions.add(new EquipItemAction("skrzynia skarbów V", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
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
                "Dobra robota, wszystkie wskazane stworzenia zostały pokonane. Przyjmij nagrody za wyprawę; zdobyte doświadczenie przyda ci się przy następnym zejściu.",
                new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING,
                triggers,
                new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
                ConversationStates.ATTENDING,
                null,
                (p, sentence, raiser) -> raiser.say("Krypta nie została jeszcze w pełni sprawdzona. Przejrzyj brakujące cele, żeby następna wyprawa miała jasny kierunek." + "\n" + games.stendhal.server.core.rp.quest.HuntQuestProgress.progressText(KillStone.this,p)));
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Krypta pod Semos",
				"Luck na równinach Semos szuka śmiałka do wyprawy w podziemną kryptę. Wysłuchaj jego wskazówek i poznaj przeciwników, z którymi trzeba się tam zmierzyć.",
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
		return "KillStone";
	}
	
	@Override
	public int getMinLevel() {
		return 250;
	}
	
	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
				 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)).fire(player,null, null);
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"done").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Luck";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	

    @Override public java.util.Map<String, Pair<Integer,Integer>> getDialogueHuntTargets(Player player) {
        final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("czarny olbrzym", new Pair<Integer, Integer>(0,1));
		toKill.put("kostucha różowa wielka", new Pair<Integer, Integer>(0,1));
		toKill.put("czarna śmierć",new Pair<Integer, Integer>(0,1));
		toKill.put("superczłowiek olbrzym",new Pair<Integer, Integer>(0,2));
		toKill.put("gigantyczny kamienny golem",new Pair<Integer, Integer>(0,1));
        return java.util.Collections.unmodifiableMap(toKill);
    }
    @Override public int getDialogueHuntTotal(Player player) { return 0; }
    @Override public int getDialogueKillIndex() { return 1; }
    @Override public String getDialogueHuntHint(Player player) { return "Szukaj krypty pod Semos. Lista obejmuje czarnego olbrzyma, kostuchę różową wielką, czarną śmierć, dwóch superczłowieków olbrzymów i gigantycznego kamiennego golema."; }
}
