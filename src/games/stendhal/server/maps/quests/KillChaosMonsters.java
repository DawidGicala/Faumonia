/* $Id: KillChaosMonsters.java,v 1.18 2024/05/18 23:31:42 davvids Exp $ */
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
 * <li> Ivar, by the mill in Semos Plains
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Gnomes have been stealing carrots so Ivar asks you to kill some.
 * <li> You go kill the Gnomes in the gnome village and you get the reward from Ivar
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

public class KillChaosMonsters extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeHuntDetails {

	private static final String QUEST_SLOT = "kill_chaos_monsters";
	private static final int WEEK_IN_MINUTES = MathHelper.MINUTES_IN_ONE_HOUR * 9999 * 9999;
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Ivar");

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(new LevelGreaterThanCondition(199),
				new QuestNotStartedCondition(QUEST_SLOT)),
                ConversationStates.QUEST_OFFERED,
                "Z tuneli chaosu docierają kolejne wieści o napadach na kupców. Nie wystarczy przepędzić kilku słabszych stworzeń; potrzebuję kogoś, kto zmierzy się z najgroźniejszymi. Podejmiesz się tego dla Ados?",
                null);

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)),
                ConversationStates.QUEST_OFFERED,
                "Znów potrzebuję sprawdzonego wojownika do tuneli chaosu. Czy podejmiesz kolejną wyprawę według przygotowanej listy?",
                null);

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES))),
                ConversationStates.ATTENDING,
                "Dziękuję za pomoc Ados. Twoja wyprawa jest już rozliczona; obecnie nie wystawiam kolejnego zlecenia.",
                null);

		final Map<String, Pair<Integer, Integer>> toKill = getDialogueHuntTargets(null);

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.YES_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "Przygotowałem listę jedenastu rodzajów przeciwników, którzy szczególnie zagrażają kupcom. Przeszukaj tunele i odnotuj każdego z nich; gashadokuro nie jest częścią tego zlecenia." + "\nCele: " + games.stendhal.server.core.rp.quest.HuntQuestProgress.targetText(this,null) + ".",
                new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.NO_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "Przyjmuję twoją decyzję. Wolę dostać pomoc przygotowanego wojownika, więc wróć, gdy poczujesz się na siłach.",
                new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Ivar");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseXPAction(500000));
		actions.add(new IncreaseAtkXPAction(20000));
		actions.add(new IncreaseDefXPAction(20000));

		for (int i = 0; i < 10; i++) {
			actions.add(new EquipItemAction("zwój chaosu"));
		}

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
                "To dobra wiadomość dla kupców i całego Ados. Wszystkie wskazane zagrożenia zostały rozliczone, a te zwoje chaosu są twoją zapłatą za trud wyprawy.",
                new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING,
                triggers,
                new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
                ConversationStates.ATTENDING,
                null,
                (p, sentence, raiser) -> raiser.say("Meldunek jest jeszcze niepełny. Sprawdź brakujące stworzenia i nie trać sił na przeciwników, których w tym zleceniu nie wymagam." + "\n" + games.stendhal.server.core.rp.quest.HuntQuestProgress.progressText(KillChaosMonsters.this,p)));
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Potwory w tunelach Chaosu",
				"Kupcy Ados tracą towary podczas ataków w tunelach chaosu. Ivar z ratusza prosi o osłabienie wskazanych potworów, aby handel mógł znów działać spokojniej.",
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
		return "KillChaosMonsters";
	}
	
	@Override
	public int getMinLevel() {
		return 200;
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
		return "Ivar";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	

    @Override public java.util.Map<String, Pair<Integer,Integer>> getDialogueHuntTargets(Player player) {
        final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("superczłowiek olbrzym", new Pair<Integer, Integer>(0,1));
		toKill.put("jeździec chaosu na czerwonym smoku", new Pair<Integer, Integer>(0,2));
		toKill.put("jeździec chaosu na zielonym smoku", new Pair<Integer, Integer>(0,4));
		toKill.put("czarny szkielet gigant",new Pair<Integer, Integer>(0,1));
		toKill.put("kostucha wielka",new Pair<Integer, Integer>(0,1));
		toKill.put("krasnal golem",new Pair<Integer, Integer>(0,4));
		toKill.put("upadły anioł",new Pair<Integer, Integer>(0,1));
		toKill.put("czarny olbrzym",new Pair<Integer, Integer>(0,3));
		toKill.put("olbrzymi ptasznik",new Pair<Integer, Integer>(0,1));
		toKill.put("złoty smok",new Pair<Integer, Integer>(0,1));
		toKill.put("czarny smok",new Pair<Integer, Integer>(0,1));
        return java.util.Collections.unmodifiableMap(toKill);
    }
    @Override public int getDialogueHuntTotal(Player player) { return 0; }
    @Override public int getDialogueKillIndex() { return 1; }
    @Override public String getDialogueHuntHint(Player player) { return "Przeszukaj tunele chaosu pod Ados. Na liście jest jedenaście rodzajów przeciwników; gashadokuro nie należy do tego zlecenia."; }
}
