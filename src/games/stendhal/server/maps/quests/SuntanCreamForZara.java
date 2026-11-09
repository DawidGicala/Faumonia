/* $Id: SuntanCreamForZara.java,v 1.37 2012/04/20 17:39:19 kymara Exp $ */
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

import games.stendhal.server.entity.npc.action.DialogueRewardAction;

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
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Suntan Cream for Zara
 * <p>
 * PARTICIPANTS:
 * <li> Zara, a woman at the Athos beach
 * <li> David or Pam, the lifeguards.
 * <p>
 * STEPS:
 * <li> Zara asks you to bring her some suntan cream from the lifeguards.
 * <li> Pam or David want to have some ingredients. After you brought it to them
 * they mix a cream.
 * <li> Zara sees your suntan cream and asks for it and then thanks you.
 * <p>
 * REWARD:
 * <li> 1000 XP
 * <li> The key for a house in Ados where a personal chest with new slots is
 * inside
 * <p>
 * REPETITIONS: - None.
 */
public class SuntanCreamForZara extends AbstractQuest {

	private static final String QUEST_SLOT = "suntan_cream_zara";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Zarę na wyspie Athor.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chcę pomagać Zarze. Może się spiec.");
		}
		if (questState.equals("start") ||  questState.equals("done")) {
			res.add("Chcę pomóc Zarze w złagodzeniu opalenizny. Potrzebuję zdobyć krem od ratowników.");
		}
		if ((player.isEquipped("olejek do opalania") && questState.equals("start"))
				|| questState.equals("done")) {
			res.add("Mam olejek do opalania.");
		}
		if (questState.equals("done")) {
			res.add("Zabrałem olejek do Zary, a ona udostępniła mi klucz do swojego domu w północnej części miasta Ados. Powiedziała, że jest na końcu w niższym rzędzie.");
		}
		return res;
	}

	private void createRequestingStep() {
		final SpeakerNPC zara = npcs.get("Zara");

		zara.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"Nie mam nowego zadania dla ciebie. Ale dziękuję za krem do opalania. Czuję, że moja skóra ma się coraz lepiej!",
			null);
		
		zara.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "rejected"),
				ConversationStates.QUEST_OFFERED, 
				"Ostatnio zgodziłeś się mi pomóc, a moja skóra ma się coraz gorzej. " 
				+ "Proszę czy mógłbyś przynieść mi #'olejek do opalania', który #ratownicy produkują?",
				null);
		
		zara.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Zapomniałeś o obietnicy zapytania się #ratowników o #'olejek do opalania'?",
				null);
		
		zara.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestNotStartedCondition(QUEST_SLOT), new QuestNotInStateCondition(QUEST_SLOT, "rejected")),
				ConversationStates.QUEST_OFFERED, 
				"Tak przyjemnie szumiało morze, że zupełnie straciłam rachubę czasu na słońcu. Teraz skóra piecze mnie przy każdym ruchu. Przyniesiesz mi #'olejek do opalania' od tutejszych ratowników?",
				null);

		zara.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Dziękuję bardzo. Będę czekać na twój powrót!",
			DialogueRewardAction.karma(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0)));

		zara.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Dobrze. Mam dla ciebie nagrodę...",
			DialogueRewardAction.karma(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0)));

		zara.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("olejek do opalania", "suntan", "cream"),
			null,
			ConversationStates.QUEST_OFFERED,
			"#Ratownicy robią wspaniały olejek do ochrony przed słońcem i leczący oparzenia w tym samym czasie. Zdobędziesz go dla mnie?",
			null);

		zara.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("lifeguard", "ratownik"),
			null,
			ConversationStates.QUEST_OFFERED,
			"Pam i David pracują w przebieralniach na Athor. Olejek przygotowują z jednej aranduli, jednej kokudy i jednego małego eliksiru; poproś o jego zrobienie i wróć po dziesięciu minutach. Potem przynieś mi gotowy olejek.",
			null);

		zara.addReply(
			Arrays.asList("olejek do opalania", "suntan", "cream"),
			"#Ratownicy robią wspaniały olejek do ochrony przed słońcem i także leczący oparzenia w tym samym czasie.");

		zara.addReply(
			Arrays.asList("lifeguard", "ratowników", "ratownicy"),
			"Pam i David pracują w przebieralniach na Athor. Olejek przygotowują z jednej aranduli, jednej kokudy i jednego małego eliksiru; poproś o jego zrobienie i wróć po dziesięciu minutach. Potem przynieś mi gotowy olejek.");

	}

	private void createBringingStep() {
		final SpeakerNPC zara = npcs.get("Zara");

		zara.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(zara.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new PlayerHasItemWithHimCondition("olejek do opalania")),
			ConversationStates.QUEST_ITEM_BROUGHT, 
			"Wspaniale! Dostałeś olejek! Jest dla mnie?",
			null);
        zara.add(ConversationStates.ATTENDING, "olejek",
			new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new PlayerHasItemWithHimCondition("olejek do opalania")),
			ConversationStates.QUEST_ITEM_BROUGHT, 
			"Wspaniale! Dostałeś olejek! Jest dla mnie?",
			null);
		
		zara.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(zara.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new NotCondition(new PlayerHasItemWithHimCondition("olejek do opalania"))),
				ConversationStates.ATTENDING, 
				"Wiem, że #'olejek do opalania' jest trudno dostać, ale mam nadzieję, że nie zapomniałeś o moim problemie...",
				null);
        zara.add(ConversationStates.ATTENDING, "olejek",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new NotCondition(new PlayerHasItemWithHimCondition("olejek do opalania"))),
				ConversationStates.ATTENDING, 
				"Wiem, że #'olejek do opalania' jest trudno dostać, ale mam nadzieję, że nie zapomniałeś o moim problemie...",
				null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("olejek do opalania"));
		reward.add(DialogueRewardAction.item("kluczyk Zary", 1, true));
		reward.add(DialogueRewardAction.xp(1000));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(DialogueRewardAction.karma(new IncreaseKarmaAction(100)));

		zara.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			// make sure the player isn't cheating by putting the
			// cream away and then saying "yes"
			new PlayerHasItemWithHimCondition("olejek do opalania"),
			ConversationStates.ATTENDING,
			"Och, już sama myśl o odpoczynku od słońca przynosi ulgę. W podziękowaniu dostajesz kluczyk do mojego domu w Ados - możesz z niego korzystać podczas mojego pobytu na Athor. Dziękuję, że nie zostawiłeś mnie z tym kłopotem.",
			new MultipleActions(reward));

		zara.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Nie? Spójrz na mnie! Nie mogę uwierzyć, że jesteś takim samolubem!",
			null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Olejek do Opalania dla Zary",
				"Słońce Athor dało Zarze się we znaki. Potrzebuje olejku do opalania; porozmawiaj z nią, zanim wyruszysz na poszukiwania.",
				false);
		createRequestingStep();
		createBringingStep();
	}

	@Override
	public String getName() {
		return "SuntanCreamForZara";
	}

		@Override
	public int getMinLevel() {
		return 50;
	}

	@Override
	public String getNPCName() {
		return "Zara";
	}
	
	@Override
	public String getRegion() {
		return Region.ATHOR_ISLAND;
	}
}
