/* $Id: KillAlbinos.java,v 1.18 2012/04/19 18:26:42 kymara Exp $ */
/***************************************************************************
 *                   (C) Copyright 503-2010 - Stendhal                    *
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
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.npc.action.EquipItemAction;
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
 * <li> Orchiwald, by the mill in Semos Plains
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Gnomes have been stealing carrots so Orchiwald asks you to kill some.
 * <li> You go kill the Gnomes in the gnome village and you get the reward from Orchiwald
 * </ul>
 * <p>
 * REWARD:
 * <ul>
 * <li> 3 potions
 * <li> 40 XP
 * <li> No karma (deliberately. Killing gnomes is mean!)
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li> after 7 days.
 * </ul>
 */

public class KillAlbinos extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeHuntDetails {

	private static final String QUEST_SLOT = "kill_albinos";
	private static final int WEEK_IN_MINUTES = MathHelper.MINUTES_IN_ONE_HOUR * 24 * 28;
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Orchiwald");

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new QuestNotStartedCondition(QUEST_SLOT),
                ConversationStates.QUEST_OFFERED,
                "Banda elfów albinosów zabrała moje zwoje, choć sam należę do ich ludu. Teraz nawet podróżni nie mają u mnie czego kupić. Pomożesz powstrzymać rabusiów?",
                null);

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)),
                ConversationStates.QUEST_OFFERED,
                "Ta sama banda znowu napadła na mój obóz. Znasz już ich zwyczaje; podejmiesz się kolejnej wyprawy?",
                null);

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES))),
                ConversationStates.ATTENDING,
                "Od twojej wyprawy w obozie jest spokojniej. Na razie mogę zająć się odbudową zapasów.",
                null);

		final Map<String, Pair<Integer, Integer>> toKill = getDialogueHuntTargets(null);

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.YES_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "Rozejrzyj się po okolicznym lesie i rozbij ich oddziały. Musisz dotrzeć także do królowych i królów; rozproszenie samych rycerzy nie wystarczy." + "\nCele: " + games.stendhal.server.core.rp.quest.HuntQuestProgress.targetText(this,null) + ".",
                new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.NO_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "Nie będę zatrzymywał cię siłą. Gdy zechcesz wrócić do tej sprawy, mój obóz znajdziesz tutaj.",
                new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Orchiwald");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseXPAction(200000));
		actions.add(new SetQuestAction(QUEST_SLOT, "done;1"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		actions.add(new EquipItemAction("bełt", 1500, true));
		actions.add(new EquipItemAction("bełt stalowy", 1000, true));
		actions.add(new EquipItemAction("bełt złoty", 500, true));
		actions.add(new EquipItemAction("bełt płonący", 200, true));
		actions.add(new EquipItemAction("skrzynia skarbów III", 1, true));
		actions.add(new EquipItemAction("skrzynia skarbów VI", 1, true));
		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);		
		npc.add(ConversationStates.ATTENDING,
                triggers,
                new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
                ConversationStates.ATTENDING,
                "Dobrze wiedzieć, że rabusie nie czują się już bezkarni. Ocalał mi zapas bełtów i kilka skrzyń; oddaję je tobie w podziękowaniu za pomoc.",
                new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING,
                triggers,
                new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
                ConversationStates.ATTENDING,
                null,
                (p, sentence, raiser) -> raiser.say("Banda wciąż ma swoich dowódców i strażników. Sprawdź, których oddziałów jeszcze brakuje, żeby nie wracać do lasu bez potrzeby." + "\n" + games.stendhal.server.core.rp.quest.HuntQuestProgress.progressText(KillAlbinos.this,p)));
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Zabij Elfy Albinosy",
				"Orchiwald został napadnięty przez innych elfów albinosów i stracił zapas zwojów. Wysłuchaj go w leśnej osadzie i dowiedz się, przeciw komu chce wysłać wojownika.",
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
		return "KillAlbinos";
	}
	
	@Override
	public int getMinLevel() {
		return 110;
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
		return "Orchiwald";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	

    @Override public java.util.Map<String, Pair<Integer,Integer>> getDialogueHuntTargets(Player player) {
        final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("elf albinos rycerz", new Pair<Integer, Integer>(0,50));
		toKill.put("elf albinos łucznik", new Pair<Integer, Integer>(0,40));
		toKill.put("elf albinos magik", new Pair<Integer, Integer>(0,20));
		toKill.put("elf albinos królowa", new Pair<Integer, Integer>(0,10));
		toKill.put("elf albinos król", new Pair<Integer, Integer>(0,10));
        return java.util.Collections.unmodifiableMap(toKill);
    }
    @Override public int getDialogueHuntTotal(Player player) { return 0; }
    @Override public int getDialogueKillIndex() { return 1; }
    @Override public String getDialogueHuntHint(Player player) { return "Szukaj elfów albinosów w pobliżu Orchiwalda, w lesie Fado. Każdy z pięciu rodzajów ma osobny cel; większa liczba rycerzy nie zastąpi brakującego króla."; }
}
