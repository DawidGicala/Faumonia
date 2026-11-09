/* $Id: MainTask24.java,v 1.0 2026/05/03 19:30:00 davvids Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify   *
 *   it under the terms of the GNU General Public License as published by   *
 *   the Free Software Foundation; either version 2 of the License, or      *
 *   (at your option) any later version.                                    *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import games.stendhal.common.Rand;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.core.rp.quest.MasterStage;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainTask24 extends AbstractQuest {

	private static final String QUEST_SLOT = "main_task_24";

	private static final String DRAGONS = "find_dragons";
	private static final String CLOAKS_COLLECTOR_2 = "cloaks_collector_2";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private static class RequiredClaw {
		private final String name;
		private final int amount;

		RequiredClaw(final String name, final int amount) {
			this.name = name;
			this.amount = amount;
		}
	}

	private RequiredClaw getRandomClaw() {
		final int random = Rand.rand(100);

		if (random < 5) {
			return new RequiredClaw("pazur niebieskiego smoka", 1);
		} else if (random < 10) {
			return new RequiredClaw("pazur czerwonego smoka", 1);
		} else if (random < 20) {
			return new RequiredClaw("pazur zielonego smoka", Rand.rand(2) + 1);
		} else if (random < 60) {
			return new RequiredClaw("pazur złotego smoka", Rand.rand(2) + 2);
		} else {
			return new RequiredClaw("pazur czarnego smoka", Rand.rand(2) + 2);
		}
	}

	private static RequiredClaw getRequiredClaw(final Player player) {
		if (!player.hasQuest(QUEST_SLOT)) {
			return null;
		}

		final String questState = player.getQuest(QUEST_SLOT);
		final String[] parts = questState.split(";");

		if (parts.length < 3) {
			return null;
		}

		if (!"start".equals(parts[0])) {
			return null;
		}

		try {
			return new RequiredClaw(parts[1], Integer.parseInt(parts[2]));
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	private static class FindDragonsCompletedCondition implements ChatCondition {
		public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
			if (!player.hasQuest(DRAGONS)) {
				return false;
			}

			final String questState = player.getQuest(DRAGONS);

			if (questState == null || questState.trim().isEmpty()) {
				return false;
			}

			if ("done".equals(questState)) {
				return true;
			}

			final String[] parts = questState.split(";");
			int found = 0;

			for (final String part : parts) {
				if (part != null && !part.trim().isEmpty()) {
					found++;
				}
			}

			return found >= 11;
		}
	}

	private static class MainTaskNotStartedOrRejectedCondition implements ChatCondition {
		public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
			if (!player.hasQuest(QUEST_SLOT)) {
				return true;
			}

			final String questState = player.getQuest(QUEST_SLOT);
			return "rejected".equals(questState);
		}
	}

	private static class PlayerHasRequiredClawCondition implements ChatCondition {
		public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
			final RequiredClaw claw = getRequiredClaw(player);
			return claw != null && player.isEquipped(claw.name, claw.amount);
		}
	}

	private static class SayRequiredClawAction implements ChatAction {
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			final RequiredClaw claw = getRequiredClaw(player);

			if (claw == null) {
				npc.say("Nie pamiętam, jakiego pazura od ciebie wymagałem. Zgłoś to administracji.");
			} else {
				npc.say("Nie masz przy sobie wymaganego przedmiotu: " + claw.amount + " x " + claw.name + ".");
			}
		}
	}

	private static class DropRequiredClawAction implements ChatAction {
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			final RequiredClaw claw = getRequiredClaw(player);

			if (claw != null) {
				player.drop(claw.name, claw.amount);
			}
		}
	}

	private class StartMainTaskAction implements ChatAction {
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			final RequiredClaw claw = getRandomClaw();
			player.setQuest(QUEST_SLOT, "start;" + claw.name + ";" + claw.amount);

			npc.say("Dobrze. Wykonałeś już wielkie zadania, ale zanim przyznam ci kolejną odznakę, musisz przynieść mi jeszcze "
					+ claw.amount + " x " + claw.name + ". Wróć, gdy będziesz mieć wymagane pazury przy sobie.");
		}
	}

	private void checkRequiredQuests() {
		final MasterStage npc = MasterStage.register(24, this);

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new GreetingMatchesNameCondition(npc.getName()),
				new LevelGreaterThanCondition(299),
				new MainTaskNotStartedOrRejectedCondition()),
			ConversationStates.ATTENDING,
			"Witaj, mam dla ciebie główne #zadanie dwudziestego-czwartego poziomu.",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
			new AndCondition(
				new LevelGreaterThanCondition(299),
				new NotCondition(new FindDragonsCompletedCondition()),
				new MainTaskNotStartedOrRejectedCondition()),
			ConversationStates.ATTENDING,
			"Zanim przyznam ci kolejne główne zadanie, musisz odnaleźć wszystkie smoki rozsiane po krainie Faumonii. Wróć do mnie, gdy ukończysz to wyzwanie.",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
			new AndCondition(
				new LevelGreaterThanCondition(299),
				new QuestNotCompletedCondition(CLOAKS_COLLECTOR_2),
				new MainTaskNotStartedOrRejectedCondition()),
			ConversationStates.ATTENDING,
			"Zanim przyznam ci kolejne główne zadanie, musisz ukończyć drugą kolekcję płaszczy i zdobyć buty zabójcy. Wróć do mnie, gdy Josephine uzna twoją pracę za zakończoną.",
			null);
	}

	private void offerQuest() {
		final MasterStage npc = MasterStage.register(24, this);

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
			new AndCondition(
				new MainTaskNotStartedOrRejectedCondition(),
				new LevelGreaterThanCondition(299),
				new FindDragonsCompletedCondition(),
				new QuestCompletedCondition(CLOAKS_COLLECTOR_2)),
			ConversationStates.QUEST_OFFERED,
			"Widzę, że odnalazłeś smoki i zdobyłeś buty zabójcy. To nie są już zadania dla zwykłych wojowników. Został ostatni dowód twojej siły. Jesteś gotowy?",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			null,
			new StartMainTaskAction());

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Nie ma problemu. Wróć, gdy zdecydujesz się podjąć główne zadanie.",
			new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void collectItem() {
		final MasterStage npc = MasterStage.register(24, this);

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, 0, "start")),
			ConversationStates.QUEST_ITEM_QUESTION,
			"Przyniosłeś pazury, o które cię prosiłem?",
			null);

		npc.add(
			ConversationStates.QUEST_ITEM_QUESTION,
			ConversationPhrases.YES_MESSAGES,
			new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, 0, "start"),
				new NotCondition(new PlayerHasRequiredClawCondition())),
			ConversationStates.ATTENDING,
			null,
			new SayRequiredClawAction());

		npc.add(
			ConversationStates.QUEST_ITEM_QUESTION,
			ConversationPhrases.YES_MESSAGES,
			new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, 0, "start"),
				new PlayerHasRequiredClawCondition()),
			ConversationStates.ATTENDING,
			"Odnalazłeś smoki, zdobyłeś buty zabójcy i przyniosłeś smocze pazury. Zasługujesz na odznakę wojownika Faumonii dwudziestego-czwartego poziomu.",
			new MultipleActions(
				new DropRequiredClawAction(),
				new DropItemAction("odznaka wojownika XXIII", 1),
				new EquipItemAction("odznaka wojownika XXIV", 1, true),
				new EquipItemAction("klucz do tawerny semos", 1, true),
				new SetQuestAction(QUEST_SLOT, "done")));

		npc.add(
			ConversationStates.QUEST_ITEM_QUESTION,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			null,
			new SayRequiredClawAction());
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Wojownik Faumonii 24",
			"Mistrz Faumonii ma dla ciebie dwudzieste-czwarte główne zadanie, które sprawdzi twoją wytrwałość, odwagę i gotowość na dalszą drogę wojownika Faumonii.",
			true);

		checkRequiredQuests();
		offerQuest();
		collectItem();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();

		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}

		res.add("Podjąłem się głównego zadania na wojownika Faumonii dwudziestego-czwartego poziomu.");

		final String questState = player.getQuest(QUEST_SLOT);

		if (questState.equals("rejected")) {
			res.add("Nie chcę jeszcze podjąć się głównego zadania na wojownika Faumonii dwudziestego-czwartego poziomu.");
			return res;
		}

		if (!isCompleted(player)) {
			res.add("Muszę odnaleźć wszystkie smoki rozsiane po krainie Faumonii oraz ukończyć drugą kolekcję płaszczy, aby zdobyć buty zabójcy.");

			final RequiredClaw claw = getRequiredClaw(player);
			if (claw != null) {
				res.add("Mistrz Faumonii poprosił mnie, abym przyniósł: " + claw.amount + " x " + claw.name + ".");
			}
		} else {
			res.add("Ukończyłem dwudzieste-czwarte zadanie od Mistrza Faumonii i otrzymałem odznakę wojownika Faumonii dwudziestego-czwartego poziomu.");
		}

		return res;
	}

	@Override
	public String getName() {
		return "MainTask24";
	}

	@Override
	public int getMinLevel() {
		return 300;
	}

	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT, "done").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return MasterStage.NPC_NAME;
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
}