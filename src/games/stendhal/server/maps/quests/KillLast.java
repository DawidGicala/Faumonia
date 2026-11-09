/* $Id: KillLast.java,v 1.18 2020/12/08 19:04:42 davvids Exp $ */
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
 * <li> Fag, by the mill in Semos Plains
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Gnomes have been stealing carrots so Fag asks you to kill some.
 * <li> You go kill the Gnomes in the gnome village and you get the reward from Fag
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

public class KillLast extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeHuntDetails {

	private static final String QUEST_SLOT = "kill_last";
	private static final int WEEK_IN_MINUTES = MathHelper.MINUTES_IN_ONE_HOUR * 24 * 7 * 8;
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Fag");

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new QuestNotStartedCondition(QUEST_SLOT),
                ConversationStates.QUEST_OFFERED,
                "Prawdziwe wyzwanie zaczyna się tam, gdzie sam widok przeciwnika każe zawrócić. Mam dla ciebie wyprawę przeciw wielkim kostuchom i najgroźniejszym odmianom śmierci. Chcesz poznać własne granice?",
                null);

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)),
                ConversationStates.QUEST_OFFERED,
                "Minął czas odpoczynku między próbami. Jeśli nadal szukasz wyzwania, powierzam ci tę samą listę groźnych przeciwników.",
                null);

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES))),
                ConversationStates.ATTENDING,
                "Tę próbę masz już za sobą. Kolejna będzie możliwa po ośmiu tygodniach od jej rozliczenia.",
                null);

		final Map<String, Pair<Integer, Integer>> toKill = getDialogueHuntTargets(null);

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.YES_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "Nie pomyl zwykłych kostuch z wielkimi. Każda odmiana ma własny cel, a złota śmierć wieńczy tę listę; wróć dopiero z kompletnym meldunkiem." + "\nCele: " + games.stendhal.server.core.rp.quest.HuntQuestProgress.targetText(this,null) + ".",
                new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.NO_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "Rozsądny wojownik wie, kiedy odłożyć walkę. Wróć do mnie, gdy będziesz przygotowany.",
                new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Fag");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new EquipItemAction("bilet śmierci", 1));
		actions.add(new EquipItemAction("anielski zwój", 1));
		actions.add(new EquipItemAction("smoczy zwój", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VI", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VI", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VI", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
		actions.add(new IncreaseXPAction(5000000));
		actions.add(new IncreaseAtkXPAction(10000));
		actions.add(new IncreaseDefXPAction(14000));
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
                "Próba została ukończona, a twoje zwycięstwa mówią więcej niż przechwałki. Bilet śmierci, zwoje i skrzynie należą do ciebie; korzystaj z nich równie rozważnie, jak z broni.",
                new MultipleActions(actions));
				
		npc.add(ConversationStates.ATTENDING,
                triggers,
                new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
                ConversationStates.ATTENDING,
                null,
                (p, sentence, raiser) -> raiser.say("Próba nadal trwa. Sprawdź dokładne odmiany przeciwników, bo podobna nazwa nie oznacza tego samego celu." + "\n" + games.stendhal.server.core.rp.quest.HuntQuestProgress.progressText(KillLast.this,p)));
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Wyzwanie Wojownika",
				"Fag przy pustynnej piramidzie szuka doświadczonego wojownika do dużego polowania. To jego rozległa próba walki, odrębna od krótkiego zwiadu przed wyprawą Mistrza XXV.",
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
		return "KillLast";
	}
	
	@Override
	public int getMinLevel() {
		return 450;
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
		return "Fag";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	

    @Override public java.util.Map<String, Pair<Integer,Integer>> getDialogueHuntTargets(Player player) {
        final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("kostucha wielka",new Pair<Integer, Integer>(0,20));
		toKill.put("kostucha różowa wielka",new Pair<Integer, Integer>(0,20));
		toKill.put("kostucha złota wielka",new Pair<Integer, Integer>(0,10));
		toKill.put("czarna śmierć",new Pair<Integer, Integer>(0,20));
		toKill.put("srebrna śmierć",new Pair<Integer, Integer>(0,10));
		toKill.put("złota śmierć",new Pair<Integer, Integer>(0,5));
        return java.util.Collections.unmodifiableMap(toKill);
    }
    @Override public int getDialogueHuntTotal(Player player) { return 0; }
    @Override public int getDialogueKillIndex() { return 1; }
    @Override public String getDialogueHuntHint(Player player) { return "Fag wymaga trzech odmian wielkich kostuch oraz czarnej, srebrnej i złotej śmierci. Liczą się dokładnie te odmiany; mniejsze kostuchy nie zastąpią wielkich."; }
}
