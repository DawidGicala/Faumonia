/* $Id: KillGnomes.java,v 1.18 2026/05/10 23:48:42 davvids Exp $ */
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
 * QUEST: Kill Gnomes
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li> Jenny, by the mill in Semos Plains
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Gnomes have been stealing carrots so Jenny asks you to kill some.
 * <li> You go kill the Gnomes in the gnome village and you get the reward from Jenny
 * </ul>
 * <p>
 * REWARD:
 * <ul>
 * <li> 30 potions
 * <li> treasure chest I
 * <li> 1000 XP
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> after 2 days.
 * </ul>
 */

public class KillGnomes extends AbstractQuest {

	private static final String QUEST_SLOT = "kill_gnomes";
	private static final int KILLS_INDEX = 1;
	private static final int WEEK_IN_MINUTES = MathHelper.MINUTES_IN_ONE_HOUR * 24 * 2;

	private static final String QUEST_REQUIREMENTS_TEXT = "20 #gnomów, 10 #gnomów #zwiadowców oraz 10 #gnomów #kawalerzystów";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private String getKillProgress(final Player player) {
		final String killData = player.getQuest(QUEST_SLOT, KILLS_INDEX);

		if (killData == null) {
			return "nie mam jeszcze zapisanego postępu zadania";
		}

		final List<String> tokens = Arrays.asList(killData.split(","));

		if ((tokens.size() % 5) != 0) {
			return "nie mogę odczytać postępu zadania";
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
		final SpeakerNPC npc = npcs.get("Jenny");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new OrCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestInStateCondition(QUEST_SLOT, 0, "rejected")),
				ConversationStates.QUEST_OFFERED,
				"Gnomy kradną marchewki z naszej farmy na północ od #Semos. "
				+ "Potrzebują dobrej lekcji. Pomożesz?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"),
						 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"Te zuchwałe gnomy znowu kradną nasze marchewki. Sądzę, że potrzebują kolejnej lekcji. Pomożesz?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"),
						 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES))),
				ConversationStates.ATTENDING,
				"Gnomy nie sprawiają problemu od momentu, gdy dałeś im dobrą lekcje.",
				null);

		final Map<String, Pair<Integer, Integer>> creaturesToKill = new TreeMap<String, Pair<Integer, Integer>>();
		creaturesToKill.put("gnom", new Pair<Integer, Integer>(0, 20));
		creaturesToKill.put("gnom zwiadowca", new Pair<Integer, Integer>(0, 10));
		creaturesToKill.put("gnom kawalerzysta", new Pair<Integer, Integer>(0, 10));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, KILLS_INDEX, creaturesToKill));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Doskonale. Zabij " + QUEST_REQUIREMENTS_TEXT + ". "
				+ "Obozowisko #gnomów znajdziesz na północny-zachód od #Semos. "
				+ "Stan zadania sprawdzisz przez #Menu oraz #Dziennik #Zadań.",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Masz rację. Chyba to zbyt okropne, aby wybijać gnomy, które kradną marchewki. "
				+ "Może farma powinna zwiększyć ochronę. ",
				new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures */
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Jenny");

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new EquipItemAction("eliksir", 30));
		actions.add(new EquipItemAction("skrzynia skarbów I", 1));
		actions.add(new IncreaseXPAction(1000));
		actions.add(new SetQuestAction(QUEST_SLOT, "done;1"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));

		final LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);

		npc.add(ConversationStates.ATTENDING,
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KilledForQuestCondition(QUEST_SLOT, KILLS_INDEX)),
				ConversationStates.ATTENDING,
				"Widzę, że zabiłeś gnomy. Mam nadzieje, że przez jakiś czas nie będą się zbliżać do marchwi! "
				+ "Proszę weź te mikstury w dowód uznania.",
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING,
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, KILLS_INDEX))),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Musisz jeszcze nauczyć te zuchwałe gnomy lekcji. "
								+ "Postęp: " + getKillProgress(player) + ". "
								+ "Do zabicia zostało: " + QUEST_REQUIREMENTS_TEXT + ". "
								+ "Obozowisko #gnomów znajdziesz na północny-zachód od #Semos. "
								+ "Stan zadania sprawdzisz przez #Menu oraz #Dziennik #Zadań.");
					}
				});
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Zabij Gnomy",
				"Pomocy! Gnomy ukradły marchewki młynarzowej Jenny! Idź do wioski gnomów, położonej na północny-zachód od Semos i zemścij się w jej imieniu.",
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

		if (player.isQuestInState(QUEST_SLOT, "rejected")) {
			res.add("Odmówiłem Jenny pomocy z gnomami. Jeśli zmienię zdanie, mogę ponownie z nią porozmawiać.");
			return res;
		}

		if (!isCompleted(player)) {
			res.add("Muszę zabić 20 gnomów, 10 gnomów zwiadowców oraz 10 gnomów kawalerzystów.");

			if (player.isQuestInState(QUEST_SLOT, 0, "start")) {
				res.add("Mój obecny postęp: " + getKillProgress(player) + ".");
			}

			res.add("Obozowisko gnomów znajdę na północny-zachód od Semos. Jenny chce, żebym dał im lekcję za kradzież marchewek z farmy.");
		} else if (isRepeatable(player)) {
			res.add("Te brzydkie gnomy zapomniały lekcji jaką im dałem. Kradną jeszcze więcej! Jenny potrzebuje mojej pomocy.");
		} else {
			res.add("Pomogłem Jenny. Gnomy trzymają się teraz z dala od marchewek. Hura! W nagrodę za ukończenie zadania otrzymałem: 1.000 pkt doświadczenia, 30 eliksirów oraz skrzynię skarbów I.");
		}

		return res;
	}

	@Override
	public String getName() {
		return "KillGnomes";
	}

	@Override
	public int getMinLevel() {
		return 5;
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
		return "Jenny";
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
}