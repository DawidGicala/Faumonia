/* $Id: FishermansLicenseCollector.java,v 1.38 2011/11/13 17:13:16 kymara Exp $ */
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

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Fisherman's license Collector
 * 
 * PARTICIPANTS:
 * <ul>
 * <li> Santiago the fisherman</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> The player must bring all kinds of fishes to the fisherman</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li> 2000 XP</li>
 * <li> The player gets a fisherman's license (i.e. fishing skills increased by
 *      0.2).</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> No repetitions.</li>
 * </ul>
 *
 * @author dine
 */

public class FishermansLicenseCollector extends AbstractQuest {

	public static final String QUEST_SLOT = "fishermans_license2";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private static final List<String> neededFish = 
		Arrays.asList("pstrąg", "okoń", "makrela", "dorsz", "płotka", "palia alpejska", "błazenek", "pokolec");

	/**
	 * Returns a list of the names of all fish that the given player still has
	 * to bring to fulfill the quest.
	 *
	 * @param player
	 *            The player doing the quest
	 * @param hash
	 *            If true, sets a # character in front of every name
	 * @return A list of fish names
	 */
	private List<String> missingFish(final Player player, final boolean hash) {
		final List<String> result = new LinkedList<String>();

		String doneText = player.getQuest(QUEST_SLOT);
		if (doneText == null) {
			doneText = "";
		}

		final List<String> done = Arrays.asList(doneText.split(";"));
		for (final String fish : neededFish) {
			if (!done.contains(fish)) {
				if (hash) {
					result.add("#" + fish);
				} else {
					result.add(fish);
				}
			}
		}
		return result;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Santiago");

		// player says hi before starting the quest
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestCompletedCondition(FishermansLicenseQuiz.QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Witaj znowu! Druga część twojego #egzaminu czeka na Ciebie!",
			null);

		// player is willing to help
		npc.add(ConversationStates.QUEST_2_OFFERED,
			ConversationPhrases.YES_MESSAGES, new AndCondition(new QuestCompletedCondition(FishermansLicenseQuiz.QUEST_SLOT), new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Przynieś po jednej rybie każdego gatunku: pstrąg, okoń, makrela, dorsz, płotka, palia alpejska, błazenek i pokolec. Możesz przynosić ryby pojedynczo; odłożę każdy brakujący okaz. Zapytaj o #rodzaju, żeby sprawdzić listę i przekazać połów.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "", 5.0));

		// player is not willing to help
		npc.add(ConversationStates.QUEST_2_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Dobrze możesz poćwiczyć trochę dłużej.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		// player asks what exactly is missing
		npc.add(ConversationStates.ATTENDING, Arrays.asList("species", "rodzaju", "rodzaj"),
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_2, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					final List<String> needed = missingFish(player, true);
					raiser.say("Oto " + Grammar.isare(needed.size())
							+ " "
							+ Grammar.quantityplnoun(needed.size(), "fish", "jeden")
							+ " wciąż brakuje: "
							+ Grammar.enumerateCollection(needed)
							+ ". Czy masz przy sobie którąś z tych ryb?");
				}
			});

		// player says he doesn't have required fish with him
		npc.add(ConversationStates.QUESTION_2, ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.IDLE, null, new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					final List<String> missing = missingFish(player, false);
					raiser.say("Daj mi znać, gdy znajdziesz "
							+ Grammar.itthem(missing.size()) + ". Dowidzenia.");
				}
			});

		// player says he has a required fish with him
		npc.add(ConversationStates.QUESTION_2,
			ConversationPhrases.YES_MESSAGES, new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_2, "Jaką rybę złapałeś?",
			null);

		for(final String itemName : neededFish) {
			npc.add(ConversationStates.QUESTION_2, itemName, null,
				ConversationStates.QUESTION_2, null,
				new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						List<String> missing = missingFish(player, false);

						if (missing.contains(itemName)) {
                            if (missing.size()==1 && player.isEquipped(itemName)
                                    && !games.stendhal.server.maps.quests.logic.QuestGoldFees.requirePaid(player, QUEST_SLOT, raiser)) { return; }
							if (player.drop(itemName)) {
								// register fish as done
								final String doneText = player.getQuest(QUEST_SLOT);
								player.setQuest(QUEST_SLOT, doneText + ";" + itemName);

								// check if the player has brought all fish
								missing = missingFish(player, true);

								if (!missing.isEmpty()) {
									raiser.say("Ten gatunek mogę już odhaczyć. Zachowam go jako część twojego egzaminu; kolejną rybę możesz oddać teraz albo przynieść później.");
								} else {
									player.addXP(2000);
									raiser.say("Mam już wszystkie osiem gatunków. Zaliczasz także praktyczną część egzaminu i otrzymujesz kartę wędkarską; twoje umiejętności połowu rosną. Niech następne wyprawy nad wodę będą udane.");
									player.setQuest(QUEST_SLOT, "done");
                                    raiser.setCurrentState(ConversationStates.ATTENDING);
									// once there are other ways to increase your
									// fishing skills, increase the old skills
									// instead of just setting to 0.5.
									player.setSkill("fishing", Double.toString(0.5));
									player.notifyWorldAboutChanges();
								}
							} else {
								raiser.say("Nie próbuj mnie oszukać! Wiem, że nie masz "
										+ Grammar.a_noun(itemName)
										+ ". Co masz dla mnie?");
							}
						} else {
							raiser.say("Mam już rybę tego gatunku. Nie potrzebuję drugiej sztuki; poszukaj okazów, których nadal brakuje na liście.");
						}
					}
				});
		}
	}

	private void step_2() {
		// Just find some of the fish somewhere and bring them to Santiago.
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Santiago");

		// player returns while quest is still active
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestActiveCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Witaj znowu. Mam nadzieję, że się nie ociągałeś i przyniosłeś mi inny #rodzaj ryby.",
			null);

		// player returns after finishing the quest
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestCompletedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Witaj rybaku! Miło Cię znowu widzieć. Życzę szczęścia w łowieniu ryb.",
			null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Karta Rybacka część 2",
				"Po zdanym egzaminie Santiago chce zobaczyć twój połów. Dostarcz po jednej rybie każdego wskazanego gatunku i zakończ praktyczną część zdobywania karty wędkarskiej.",
				true);
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
			res.add("Druga część egzaminu Santiago to połów  wiele gatunków ryb.");
			if (!isCompleted(player)) {
				res.add("Wciąż muszę przynieść " + Grammar.enumerateCollection(missingFish(player, false)) + " dla Santiago do sprawdzenia.");
			} else {
				res.add("Przyniosłem wszystkie gatunki ryb jakie Santiago chciał, a teraz jestem prawdziwym rybakiem! Będę miał więcej sukcesów podczas połowów.");
			}
			return res;
	}
	
	@Override
	public String getName() {
		return "FishermansLicenseCollector";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Santiago";
	}

    public static List<String> getRequiredFish(){ return java.util.Collections.unmodifiableList(neededFish); }

}
