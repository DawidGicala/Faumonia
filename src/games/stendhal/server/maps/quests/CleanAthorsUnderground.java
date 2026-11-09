/* $Id: CleanAthorsUnderground.java,v 1.8 2012/07/07 21:52:50 bluelads99 Exp $ */
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
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import marauroa.common.Pair;

/**
 * QUEST: Clean Athors underground
 *
 * PARTICIPANTS: <ul>
 * <li> NPC on Athor island
 * <li> one of each creature in Athor underground
 * </ul>
 *
 * STEPS:<ul>
 * <li> John on Athor island asks players to kill some creatures of the dungeon for him, cause he can't explore it otherwise
 * <li> Kill them for him and go back to the NPC to get your reward
 * </ul>
 * 
 *
 * REWARD:<ul>
 * <li> 5000 XP
 * <li> 10 greater potion
 * <li> some karma
 * </ul>
 *
 * REPETITIONS: <ul><li>once in a week</ul>
 * 
 * @author Vanessa Julius, idea by anoyyou

 */

public class CleanAthorsUnderground extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeHuntDetails {

	private static final String QUEST_SLOT = "clean_athors_underground";
	private static final int WEEK_IN_MINUTES = MathHelper.MINUTES_IN_ONE_HOUR * 24 * 7 * 2;
	
	
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
		
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("John");

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new QuestNotStartedCondition(QUEST_SLOT),
                ConversationStates.QUEST_OFFERED,
                "Przyjechaliśmy z Jane na Athor, żeby odpocząć i trochę pozwiedzać. Plaża jest piękna, ale potwory skutecznie odcięły nam drogę do podziemi. Pomożesz otworzyć nam drogę do tych miejsc?",
                null);

		npc.add(
				ConversationStates.QUEST_OFFERED,
				Arrays.asList("Unfortunately", "Niestety"),
				null,
				ConversationStates.QUEST_OFFERED,
				"Tak niestety. Chcieliśmy spedzić wspaniale czas, ale jedyne co zrobiliśmy to spędziliśmy czas na plaży.",
				null);
		
		npc.add(
				ConversationStates.QUEST_OFFERED,
				Arrays.asList("creatures", "potwory", "potworów"),
				null,
				ConversationStates.QUEST_OFFERED,
				"Chcemy zwiedzić pierwszą część podziemi, która wygląda na bardzo interesującą, ale te okropne coś! rzuca się na nas, nawet mumie!",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)), new QuestStateStartsWithCondition(QUEST_SLOT, "done")),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, WEEK_IN_MINUTES, "Te #potwory nie wrócą szybko i dzięki temu możemy zobaczyć wspaniałe miejsca na wyspie. Wróć za"));
		
		
		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)),
                ConversationStates.QUEST_OFFERED,
                "Potwory wróciły, a my znów musieliśmy zawrócić z podziemi. Czy pomożesz nam jeszcze raz?",
                null);

	

		final Map<String, Pair<Integer, Integer>> toKill = getDialogueHuntTargets(null);

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		
		npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.YES_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "Zejdź do podziemi Athor i pokonaj po jednym przeciwniku z każdego rodzaju na liście. Zwykła mumia i królewska to dwa osobne cele, podobnie jak minotaur i jego król." + "\nCele: " + games.stendhal.server.core.rp.quest.HuntQuestProgress.targetText(this,null) + ".",
                new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.NO_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "Trudno, zostaniemy jeszcze trochę na plaży. Jeśli zmienisz zdanie, znajdziesz mnie tutaj z Jane.",
                new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("John");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
	    actions.add(new EquipItemAction("wielki eliksir", 50));
		actions.add(new EquipItemAction("zwój athor", 1));
		actions.add(new IncreaseXPAction(50000));
		actions.add(new SetQuestAction(QUEST_SLOT, "done;1"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		actions.add(new IncreaseKarmaAction(25.0));

		
		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);		
		npc.add(ConversationStates.ATTENDING,
                triggers,
                new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
                ConversationStates.ATTENDING,
                "Wspaniała wiadomość; może wreszcie zobaczymy coś poza plażą. Przyjmij eliksiry i zwój Athor w podziękowaniu od nas obojga.",
                new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING,
                triggers,
                new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
                ConversationStates.ATTENDING,
                null,
                (p, sentence, raiser) -> raiser.say("Nie wszystkie miejsca są jeszcze bezpieczne. Sprawdź, którego z pięciu przeciwników brakuje, a my zaczekamy tutaj." + "\n" + games.stendhal.server.core.rp.quest.HuntQuestProgress.progressText(CleanAthorsUnderground.this,p)));
		
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Posprzątaj podziemia Athor",
				"John i Jane przyjechali odpocząć na Athor, lecz potwory uniemożliwiają im zwiedzanie podziemi. Porozmawiaj z Johnem i pomóż oczyścić drogę z wymienionych zagrożeń.",
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
		return "CleanAthorsUnderground";

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
		return "John";
	}

	@Override
	public String getRegion() {
		return Region.ATHOR_ISLAND;
	}

    @Override public java.util.Map<String, Pair<Integer,Integer>> getDialogueHuntTargets(Player player) {
        final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("mumia", new Pair<Integer, Integer>(0,1));
		toKill.put("mumia królewska", new Pair<Integer, Integer>(0,1));
		toKill.put("minotaur",new Pair<Integer, Integer>(0,1));
		toKill.put("błękitny smok",new Pair<Integer, Integer>(0,1));
		toKill.put("król minotaur",new Pair<Integer, Integer>(0,1));
        return java.util.Collections.unmodifiableMap(toKill);
    }
    @Override public int getDialogueHuntTotal(Player player) { return 0; }
    @Override public int getDialogueKillIndex() { return 1; }
    @Override public String getDialogueHuntHint(Player player) { return "Zejdź do podziemi Athor i pokonaj po jednej mumii, mumii królewskiej, minotaurze, błękitnym smoku i królu minotaurze. Potem wróć do Johna na wyspie."; }
}
