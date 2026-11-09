/* $Id: KillMag.java,v 1.18 2020/12/04 12:55:17 davvids Exp $ */
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
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
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
import games.stendhal.server.entity.npc.condition.OrCondition;
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
 * QUEST: Kill Mag
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li> Staro, in Fado tavern
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Staro asks the player to investigate a strange island with magic users.
 * <li> The player kills the required magic users and returns to Staro.
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> 250000 XP
 * <li> Magic items
 * <li> Treasure chests
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> after 7 days.
 * </ul>
 */

public class KillMag extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeHuntDetails {

	private static final String QUEST_SLOT = "kill_mag";
	private static final int KILLS_INDEX = 1;
	private static final int WEEK_IN_MINUTES = MathHelper.MINUTES_IN_ONE_HOUR * 24 * 7;

	private static final String QUEST_REQUIREMENTS_TEXT = "20 #pustelników, 10 #uczniów #czarnoksiężnika, 10 #uczniów #czarnoksiężnika #mroku, 10 #czarnoksiężników, 10 #czarnoksiężników #mroku oraz 3 #czarownice z #Aenye";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private String getKillProgress(final Player player) {
		final String killData = player.getQuest(QUEST_SLOT, KILLS_INDEX);

		if (killData == null) {
			return "nie można teraz wyświetlić postępu polowania; zgłoś problem administracji";
		}

		final List<String> tokens = Arrays.asList(killData.split(","));

		if ((tokens.size() % 5) != 0) {
			return "nie można teraz wyświetlić postępu polowania; zgłoś problem administracji";
		}

		final StringBuilder progress = new StringBuilder();

		for (int i = 0; i < tokens.size(); i += 5) {
			final String creatureName = tokens.get(i);

			final int requiredSoloKills = MathHelper.parseIntDefault(tokens.get(i + 1), 0);
			final int requiredSharedKills = MathHelper.parseIntDefault(tokens.get(i + 2), 0);
			final int initialSoloKills = MathHelper.parseIntDefault(tokens.get(i + 3), 0);
			final int initialSharedKills = MathHelper.parseIntDefault(tokens.get(i + 4), 0);

			final int currentSoloKills = player.getSoloKill(creatureName);
			final int currentSharedKills = player.getSharedKill(creatureName);

			final int soloKillsDone = Math.max(0, currentSoloKills - initialSoloKills);
			final int sharedKillsDone = Math.max(0, currentSoloKills + currentSharedKills - initialSoloKills - initialSharedKills);

			if (requiredSoloKills > 0) {
				appendKillProgress(progress, markCreatureName(creatureName) + " solo", soloKillsDone, requiredSoloKills);
			}

			if (requiredSharedKills > 0) {
				if (requiredSoloKills > 0) {
					appendKillProgress(progress, markCreatureName(creatureName) + " razem", sharedKillsDone, requiredSharedKills);
				} else {
					appendKillProgress(progress, markCreatureName(creatureName), sharedKillsDone, requiredSharedKills);
				}
			}
		}

		if (progress.length() == 0) {
			return "nie mam nic do zliczenia";
		}

		return progress.toString();
	}

	private void appendKillProgress(final StringBuilder progress, final String creatureName, final int killsDone, final int killsRequired) {
		final int safeKillsDone = Math.min(Math.max(killsDone, 0), killsRequired);
		final int killsLeft = Math.max(0, killsRequired - safeKillsDone);

		if (progress.length() > 0) {
			progress.append(", ");
		}

		progress.append(creatureName);
		progress.append(": ");
		progress.append(safeKillsDone);
		progress.append("/");
		progress.append(killsRequired);
		progress.append(" - zostało ");
		progress.append(killsLeft);
	}

	private String markCreatureName(final String creatureName) {
		return "#" + creatureName.replace(" ", " #");
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Staro");

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new OrCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestInStateCondition(QUEST_SLOT, 0, "rejected")),
                ConversationStates.QUEST_OFFERED,
                "Kiedyś trafiłem na dziwną wyspę, gdzie ludzie władali potężną magią. Do dziś zastanawiam się, czego tam uczą i dokąd prowadzą te nauki. Wyruszysz tam i zmierzysz się z jej mieszkańcami?",
                null);

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"),
						 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)),
                ConversationStates.QUEST_OFFERED,
                "Magowie znów gromadzą się na wyspie. Czy zechcesz sprawdzić to miejsce podczas kolejnej wyprawy?",
                null);

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"),
						 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES))),
                ConversationStates.ATTENDING,
                "Na razie mam dość wiadomości z wyspy. Wróć po tygodniu od rozliczenia wyprawy, jeśli zechcesz pomóc ponownie.",
                null);

		final Map<String, Pair<Integer, Integer>> creaturesToKill = getDialogueHuntTargets(null);

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, KILLS_INDEX, creaturesToKill));

		npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.YES_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "Odszukaj wyspę i sprawdź wszystkie odmiany z listy, od pustelników po czarownicę z Aenye. Uczniowie i czarnoksiężnicy są osobnymi celami; pokonanie jednych nie zastąpi drugich." + "\nCele: " + games.stendhal.server.core.rp.quest.HuntQuestProgress.targetText(this,null) + ".",
                new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.NO_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "Nie nalegam. Taką wyprawę warto zacząć z pełnymi zapasami i spokojną głową.",
                new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures */
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Staro");

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseXPAction(250000));
		actions.add(new SetQuestAction(QUEST_SLOT, "done;1"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		actions.add(new EquipItemAction("magia światła", 200));
		actions.add(new EquipItemAction("magia mroku", 400));
		actions.add(new EquipItemAction("magia płomieni", 600));
		actions.add(new EquipItemAction("magia deszczu", 600));
		actions.add(new EquipItemAction("magia ziemi", 1000));
		actions.add(new EquipItemAction("skrzynia skarbów VI", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VI", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VII", 1));

		final LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);

		npc.add(ConversationStates.ATTENDING,
                triggers,
                new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KilledForQuestCondition(QUEST_SLOT, KILLS_INDEX)),
                ConversationStates.ATTENDING,
                "Twoja wyprawa potwierdza, jak wielu różnych magów działa na wyspie. Zostawiam ci zapas magii do różdżek i przygotowane skrzynie; może wykorzystasz tę wiedzę w następnej podróży.",
                new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING,
                triggers,
                new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, KILLS_INDEX))),
                ConversationStates.ATTENDING,
                null,
                (p, sentence, raiser) -> raiser.say("Jeszcze nie wszystkie odmiany magów zostały odnotowane. Sprawdź listę przed wyprawą, zwłaszcza uczniów mroku i czarownicę z Aenye." + "\n" + games.stendhal.server.core.rp.quest.HuntQuestProgress.progressText(KillMag.this,p)));
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Czarnoksiężnicy",
				"Staro z tawerny Fado opowiada o odległej wyspie zamieszkanej przez ludzi władających magią. Wysłuchaj go i podejmij zlecenie dotyczące tamtejszych czarnoksiężników.",
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
		return "KillMag";
	}

	@Override
	public int getMinLevel() {
		return 150;
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"),
				 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)).fire(player, null, null);
	}

	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT, "done").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Staro";
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}

    @Override public java.util.Map<String, Pair<Integer,Integer>> getDialogueHuntTargets(Player player) {
        final Map<String, Pair<Integer, Integer>> creaturesToKill = new TreeMap<String, Pair<Integer, Integer>>();
		creaturesToKill.put("pustelnik", new Pair<Integer, Integer>(0, 20));
		creaturesToKill.put("uczeń czarnoksiężnika", new Pair<Integer, Integer>(0, 10));
		creaturesToKill.put("czarnoksiężnik", new Pair<Integer, Integer>(0, 10));
		creaturesToKill.put("uczeń czarnoksiężnika mroku", new Pair<Integer, Integer>(0, 10));
		creaturesToKill.put("czarnoksiężnik mroku", new Pair<Integer, Integer>(0, 10));
		creaturesToKill.put("czarownica z Aenye", new Pair<Integer, Integer>(0, 3));
        return java.util.Collections.unmodifiableMap(creaturesToKill);
    }
    @Override public int getDialogueHuntTotal(Player player) { return 0; }
    @Override public int getDialogueKillIndex() { return 1; }
    @Override public String getDialogueHuntHint(Player player) { return "Odszukaj wyspę, na której spotkasz pustelników, uczniów i czarnoksiężników. Lista obejmuje także czarownicę z Aenye; każdą odmianę liczę osobno."; }
}
