/* $Id: LookBookforCeryl.java,v 1.58 2011/11/13 17:14:15 kymara Exp $ */
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
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Look book for Ceryl PARTICIPANTS: - Ceryl - Jynath
 * 
 * STEPS: - Talk with Ceryl to activate the quest. - Talk with Jynath for the
 * book. - Return the book to Ceryl
 * 
 * REWARD: - 100 XP - 50 gold coins
 * 
 * REPETITIONS: - None.
 */
public class LookBookforCeryl extends AbstractQuest {
	private static final String QUEST_SLOT = "ceryl_book";



	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step1LearnAboutQuest() {

		final SpeakerNPC npc = npcs.get("Ceryl");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"Na półce od miesiąca brakuje czarnej księgi, a kolejni czytelnicy pytają właśnie o nią. Wypożyczyła ją #Jynath; chciałbym wreszcie odzyskać tę #książkę.", null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"Czarna księga wróciła już na swoje miejsce. Dziękuję, że udało ci się dopilnować zwrotu; w tej sprawie nie potrzebuję niczego więcej.", null);
			
     /** Other conditions not met e.g. quest completed */
		npc.addReply(Arrays.asList("book", "książki", "książka"),"Jeśli chcesz dowiedzieć się więcej, porozmawiaj z moim przyjacielem Wikipedianem w bibliotece Ados.", null);

		/** If quest is not started yet, start it. */
		npc.add(
			ConversationStates.ATTENDING,
			"book", new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Jynath mieszka na południe od zamku Orril. Poproś ją o zwrot czarnej księgi i przynieś ją do biblioteki w Semos. Podejmiesz się tego?",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Dziękuję. U Jynath łatwo stracić rachubę czasu pośród receptur i mikstur, więc przypomnij jej, że przysyła cię Ceryl. Będę czekał tutaj, w bibliotece.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Szkoda, czytelnicy wciąż pytają o ten tom. Jeśli zmienisz zdanie, zajrzyj do mnie.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			"jynath",
			null,
			ConversationStates.QUEST_OFFERED,
			"Jynath jest wiedźmą mieszkającą na południe od zamku Or'ril, a na południowy zachód stąd. Zdobędziesz dla mnie tę książkę?",
			null);

		/** Remind player about the quest */
		npc.add(ConversationStates.ATTENDING, Arrays.asList("book", "książki", "książka"),
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"Czarnej księgi nadal nie ma na półce. Jynath mieszka na południe od zamku Orril; porozmawiaj z nią i przynieś mi wypożyczony tom.", null);

		npc.add(
			ConversationStates.ATTENDING,
			"jynath",
			null,
			ConversationStates.ATTENDING,
			"Jynath jest wiedźmą mieszkającą na południe od zamku Or'ril, a na południowy zachód stąd.",
			null);
	}

	private void step2getBook() {
		final SpeakerNPC npc = npcs.get("Jynath");

		/**
		 * If player has quest and is in the correct state, just give him the
		 * book.
		 */
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start")),
			ConversationStates.ATTENDING,
			"Ach, czarna księga Ceryla! Zostawiłam ją przy recepturach i zupełnie zapomniałam o terminie zwrotu. Weź ją do biblioteki w Semos i przekaż mu moje przeprosiny.",
			new MultipleActions(new EquipItemAction("księga czarna", 1, true), new SetQuestAction(QUEST_SLOT, "jynath")));
        // Reuse the native stage and its guards inside the current conversation.
        npc.add(
			ConversationStates.ATTENDING, "księga ceryla",
			new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
					new QuestInStateCondition(QUEST_SLOT, "start")),
			ConversationStates.ATTENDING,
			"Ach, czarna księga Ceryla! Zostawiłam ją przy recepturach i zupełnie zapomniałam o terminie zwrotu. Weź ją do biblioteki w Semos i przekaż mu moje przeprosiny.",
			new MultipleActions(new EquipItemAction("księga czarna", 1, true), new SetQuestAction(QUEST_SLOT, "jynath")));

		/** If player keep asking for book, just tell him to hurry up */
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "jynath")),
			ConversationStates.ATTENDING,
			"Lepiej weź tę książkę z powrotem do #Ceryl szybko... on czeka na Ciebie.",
			null);
        // Reuse the native stage and its guards inside the current conversation.
        npc.add(
			ConversationStates.ATTENDING, "księga ceryla",
			new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
					new QuestInStateCondition(QUEST_SLOT, "jynath")),
			ConversationStates.ATTENDING,
			"Lepiej weź tę książkę z powrotem do #Ceryl szybko... on czeka na Ciebie.",
			null);

		npc.add(ConversationStates.ATTENDING, "ceryl", null,
			ConversationStates.ATTENDING,
			"Ceryl jest bibliotekarzem w Semos.", null);

		/** Finally if player didn't start the quest, just ignore him/her */
		npc.add(
			ConversationStates.ATTENDING,
			"książka",
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Ciii! Koncentruję się nad recepturą mikstury... jest zawiła.",
			null);
	}

	private void step3returnBook() {
		final SpeakerNPC npc = npcs.get("Ceryl");

		/** Complete the quest */
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("księga czarna"));
		reward.add(new EquipItemAction("money", 50));
		reward.add(new IncreaseXPAction(100));
		reward.add(new IncreaseKarmaAction(10.0));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "jynath"),
					new PlayerHasItemWithHimCondition("księga czarna")),
			ConversationStates.ATTENDING,
			"To nasza czarna księga, poznaję oprawę. Wreszcie mogę oddać ją następnym czytelnikom. Przyjmij zapłatę za drogę i dziękuję za pomoc.",
			new MultipleActions(reward));
        // Reuse the native stage and its guards inside the current conversation.
        npc.add(
			ConversationStates.ATTENDING, "księga ceryla",
			new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
					new QuestInStateCondition(QUEST_SLOT, "jynath"),
					new PlayerHasItemWithHimCondition("księga czarna")),
			ConversationStates.ATTENDING,
			"To nasza czarna księga, poznaję oprawę. Wreszcie mogę oddać ją następnym czytelnikom. Przyjmij zapłatę za drogę i dziękuję za pomoc.",
			new MultipleActions(reward));

		// There is no other way to get the book.
		// Remove that quest slot so that the player can get
		// it again from Jynath
		// As the book is both bound and useless outside the
		// quest, this is not a problem
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "jynath"),
					new NotCondition(new PlayerHasItemWithHimCondition("księga czarna"))),
			ConversationStates.ATTENDING, 
			"Nie przyniosłeś tej #książki od #Jynath? Proszę idź jej poszukać. Szybko!",
			new SetQuestAction(QUEST_SLOT, null));
        // Reuse the native stage and its guards inside the current conversation.
        npc.add(
			ConversationStates.ATTENDING, "księga ceryla",
			new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
					new QuestInStateCondition(QUEST_SLOT, "jynath"),
					new NotCondition(new PlayerHasItemWithHimCondition("księga czarna"))),
			ConversationStates.ATTENDING, 
			"Nie przyniosłeś tej #książki od #Jynath? Proszę idź jej poszukać. Szybko!",
			new SetQuestAction(QUEST_SLOT, null));
	}

	
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Ceryla, bibliotekarza z Semos.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chcę szukać książki");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "jynath", "done")) {
			res.add("Mam odzyskać czarną księgę od Jynath, mieszkającej na południe od zamku Orril.");
		}
		if ((questState.equals("jynath") && player.isEquipped("księga czarna"))
				|| questState.equals("done")) {
			res.add("Odebrałem czarną księgę od Jynath. Mam zwrócić ją Cerylowi w bibliotece Semos.");
		}
		if (questState.equals("jynath") && !player.isEquipped("księga czarna")) {
			res.add("Nie mam przy sobie czarnej księgi otrzymanej od Jynath.");
		}
		if (questState.equals("done")) {
			res.add("Zwróciłem książkę Cerylowi i dostałem nagrodę.");
		}
		return res;
	}
	
	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Poszukiwania Książki dla Ceryla",
				"W bibliotece Semos brakuje czarnej księgi wypożyczonej przez Jynath. Ceryl prosi o jej odzyskanie; odwiedź czarownicę i zwróć tom bibliotekarzowi.",
				false);
		step1LearnAboutQuest();
		step2getBook();
		step3returnBook();
	}

	@Override
	public String getName() {
		return "LookBookforCeryl";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Ceryl";
	}
}
