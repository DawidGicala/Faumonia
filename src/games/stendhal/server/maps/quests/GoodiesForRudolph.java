/* $Id: GoodiesForRudolph.java,v 1.6 2012/12/15 11:21:53 kymara Exp $ */
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

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Christmas Goodies For Rudolph
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Rudolph (the Red-Nosed Reindeer) - walking around Semos during Christmas season.</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Rudolph asks you for some reindeer moss, carrots and apples.</li>
 * <li>You get his goodies by collecting them from around Semos..</li>
 * <li>Rudolph sees you have collected goodies and asks for them and then thanks you.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>100 XP</li>
 * <li>50 gold</li>
 * <li>Karma: 60</li>
 * <li>snowglobe</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 */
public class GoodiesForRudolph extends AbstractQuest {

	private static final String QUEST_SLOT = "goodies_rudolph";

	

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Rudolpha. Jest on Czerwononosym Reniferem biegającym w pobliżu Semos.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Zapytał mnie o znalezienie przysmaków dla niego, ale odrzuciłem jego prośbę.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add("Przyrzekłem, że znajdę dla niego przysmaki ponieważ jest miłym reniferem.");
		}
		if (("start".equals(questState) && (player.isEquipped("mech renifera", 5)  && player.isEquipped("marchew", 10) && player.isEquipped("jabłko", 10))) || "done".equals(questState)) {
			res.add("Mam wszystkie przysmaki, które zabiorę do Rudolpha.");
		}
		if ("done".equals(questState)) {
			res.add("Wziąłem przysmaki do Rudolpha. Jako podziękowanie dał MI trochę przysmaków. :)");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Rudolph");

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED, 
			"Chcę pyszne przysmaki tylko ty możesz mi pomóc je zdobyć. Czy możesz mi pomóc?",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"Dziękuję bardzo za przysmaki, ale nie mam więcej zadań dla ciebie w tym roku. Życzę wspaniałych świąt.",
			null);

		// player is willing to help
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Słyszałem o wspaniałych #przysmakach, które masz tutaj w Semos. Jeśli zdobędziesz 5 mchów renifera, 10 jabłek i 10 marchew to dam ci nagrodę.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		// player is not willing to help
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"W takim razie zapytam kogoś innego. Biada mi.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		// player wants to know what goodies he is referring to
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("goodies", "przysmakach"),
			null,
			ConversationStates.ATTENDING,
			"Mech reniferów jest jasnozielony, który rośnie wokół tego miasta. Jabłka można znaleść na farmie na wschód od miasta, a marchew jest na północny-wschód od miasta.",
			null);
	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Rudolph");

		// player returns while quest is still active
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "start"),
				new AndCondition(
					new PlayerHasItemWithHimCondition("mech renifera", 5),
					new PlayerHasItemWithHimCondition("jabłko", 10),
					new PlayerHasItemWithHimCondition("marchew", 10))),
			ConversationStates.QUEST_ITEM_BROUGHT, 
			"Przepraszam! Widzę, że masz pyszne smakołyki. Są one dla mnie?",
			null);

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "start"), 
				new NotCondition(new AndCondition(
					new PlayerHasItemWithHimCondition("mech renifera", 5),
					new PlayerHasItemWithHimCondition("jabłko", 10),
					new PlayerHasItemWithHimCondition("marchew", 10)))),
			ConversationStates.ATTENDING, 
			"Oh nie mogę się doczekać tych przysmaków, o które cię prosiłem. Mam nadzieję, że nie będę czekał długo nim mi je przyniesiesz.",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new EquipItemAction("money", 50));
		reward.add(new EquipItemAction("zima zaklęta w kuli"));
		reward.add(new IncreaseXPAction(100));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(60));

		final List<ChatAction> reward1 = new LinkedList<ChatAction>(reward);
		reward1.add(new DropItemAction("mech renifera", 5));
		reward1.add(new DropItemAction("marchew", 10));
		reward1.add(new DropItemAction("jabłko", 10));

		
		
		
		
		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			// make sure the player isn't cheating by putting the goodies
			// away and then saying "yes"
			
			new AndCondition(
					new PlayerHasItemWithHimCondition("mech renifera", 5),
					new PlayerHasItemWithHimCondition("jabłko", 10),
					new PlayerHasItemWithHimCondition("marchew", 10)),

			ConversationStates.ATTENDING, "Jestem tak podekscytowany! Tak chciałem je zjeść od dłuższego czasu. Dziękuję bardzo. Przytoczę powiedzenie Ho Ho Ho, Wesołych Świąt.",
			new MultipleActions(reward1));


		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Cóż mam nazieję, że znajdziesz jakieś przysmaki nim padnę z głodu.",
			null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Przysmaki dla Rudolpha",
				"Rudolph jest ulubionym reniferem świętego Mikołaja, który rozpaczliwie chce przysmaków.",
				false);
		prepareRequestingStep();
		prepareBringingStep();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "GoodiesForRudolph";
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
		return "Rudolph";
	}
}
