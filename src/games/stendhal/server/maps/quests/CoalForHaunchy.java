/* $Id: CoalForHaunchy.java,v 1.16 2011/11/13 17:12:18 kymara Exp $ */
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

import games.stendhal.server.entity.npc.action.DialogueRewardAction;

import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * QUEST: Coal for Haunchy
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Haunchy Meatoch, the BBQ grillmaster on the Ados market</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Haunchy Meatoch asks you to fetch coal for his BBQ</li>
 * <li>Find some coal in Semos Mine or buy some from other players</li>
 * <li>Take the coal to Haunchy</li>
 * <li>Haunchy gives you a tasty reward</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>Karma +20 in all</li>
 * <li>XP +200 in all</li>
 * <li>Some grilled steaks, random between 1 and 4.</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>You can repeat it each 2 days.</li>
 * </ul>
 * 
 * @author Vanessa Julius and storyteller
 */
public class CoalForHaunchy extends AbstractQuest {

	private static final String QUEST_SLOT = "coal_for_haunchy";

	// The delay between repeating quests is 48 hours or 2880 minutes
	private static final int REQUIRED_MINUTES = 2880;

	private void offerQuestStep() {
		final SpeakerNPC npc = npcs.get("Haunchy Meatoch");
		
		// player says quest when he has not ever done the quest before (rejected or just new)
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, 
				"Nie mogę wykorzystać polan do tego wielkiego grilla. Aby utrzymać temperaturę potrzebuję węgla, ale nie zostało go dużo."+
			    "Problem w tym, że nie mogę go zdobyć ponieważ moje steki mogłby się spalić i dlatego muszę tu zostać."+
			    "Czy mógłbyś przynieść mi 25 kawałków #węgla do mojego grilla?",
				null);

		npc.add(
				ConversationStates.QUEST_OFFERED,
				Arrays.asList("węgiel","węgla"),
				null,
				ConversationStates.QUEST_OFFERED,
				"Węgla szukaj pod ziemią, między innymi w starej kopalni Semos. Potrzebuję dwudziestu pięciu sztuk, żeby utrzymać równy żar w grillu. Polana nie zastąpią mi tej dostawy.",
				null);

        // player has completed the quest (doesn't happen here)
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Mogę teraz grilować moje pyszne steki! Dziękuję!",
				null);

		// player asks about quest which he has done already and he is allowed to repeat it
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES), new QuestStateStartsWithCondition(QUEST_SLOT, "waiting;")),
				ConversationStates.QUEST_OFFERED,
				"Ostatnio węgiel, który mi przyniosłeś już wykorzystałem. Przyniesiesz mi go więcej?",
				null);
		
		// player asks about quest which he has done already but it is not time to repeat it
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)), new QuestStateStartsWithCondition(QUEST_SLOT, "waiting;")),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES, "Zapas węgla jest wystarczająco spory. Nie będę potrzebował go w ciągu "));

		// Player agrees to get the coal
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Nazbieraj dwadzieścia pięć sztuk węgla i przynieś je wszystkie razem. Gdy wrócisz, powiedz #węgiel, a od razu sprawdzę dostawę. Odwdzięczę się porcją steków z grilla.",
				DialogueRewardAction.karma(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 10.0)));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
				"Oh nie ważne. Myślałem, że kochasz grillowane steki jak ja. Żegnaj.",
				DialogueRewardAction.karma(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0)));
	}

	/*
	 * Get Coal Step :
	 * Players will get some coal in Semos Mine and with buying some from other players.
	 * 
	 */
	private void bringCoalStep() {
		final SpeakerNPC npc = npcs.get("Haunchy Meatoch");
		
		final List<String> triggers = new ArrayList<String>();
		triggers.add("węgiel");
		triggers.add("stone coal");
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);

		// player asks about quest or says coal when they are supposed to bring some coal and they have it
		npc.add(
				ConversationStates.ATTENDING, triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new PlayerHasItemWithHimCondition("węgiel",25)),
				ConversationStates.ATTENDING, 
				null,
				new MultipleActions(
						new DropItemAction("węgiel",25), 
						DialogueRewardAction.xp(5000),
						DialogueRewardAction.attackXP(2000),
						DialogueRewardAction.defenseXP(2000),
						new ChatAction() {
							public void fire(final Player player,
									final Sentence sentence,
									final EventRaiser npc) {
								int grilledsteakAmount = Rand.rand(20) + 1;
								DialogueRewardAction.item("grillowany stek", grilledsteakAmount, true).fire(player, sentence, npc);
								npc.say("Dziękuję!! Przyjmij te " + Grammar.thisthese(grilledsteakAmount) + " " +
										Grammar.quantityNumberStrNoun(grilledsteakAmount, "grillowany stek") + " z mojego grilla!");
								DialogueRewardAction.karma(new SetQuestAndModifyKarmaAction(getSlotName(), "waiting;" 
										+ System.currentTimeMillis(), 10.0)).fire(player, sentence, npc);
							}
						}));

		// player asks about quest or says coal when they are supposed to bring some coal and they don't have it
		npc.add(
				ConversationStates.ATTENDING, triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new NotCondition(new PlayerHasItemWithHimCondition("węgiel",25))),
				ConversationStates.ATTENDING,
				"Do tej dostawy potrzeba dwudziestu pięciu sztuk węgla. Zachowaj zebrane kawałki i donieś resztę; dopiero wtedy zabiorę cały zapas. Węgla możesz szukać w starej kopalni Semos.",
				null);

		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("węgiel","stone coal"),
				new QuestNotInStateCondition(QUEST_SLOT,"start"),
				ConversationStates.ATTENDING,
				"Czasami mógłbyś mi wyświadczyć #przysługę ...", null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Węgiel dla Haunchy",
				"Haunchy Meatoch pilnuje grilla na rynku w Ados, lecz kończy mu się węgiel. Przynieś zapas, zanim żar zgaśnie pod przygotowywanymi stekami.",
				true);
		offerQuestStep();
		bringCoalStep();
	}


	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Haunchy Meatoch powitał mnie na rynku w Ados.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Poprosił mnie o dostarzenie kilku kawałków węgla, ale nie mam czasu na ich zbieranie.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start") || isCompleted(player)) {
			res.add("Ze względu, że płomień w grillu jest bardzo mały to przyrzekłem Haunchy, że pomogę mu zdobyć 25 kawałków węgla.");
		}
		if (("start".equals(questState) && player.isEquipped("węgiel",25)) || isCompleted(player)) {
			res.add("Znalazłem 25 kawałków węgla dla Haunchy. Sądzę, że się ucieszy.");
		}
		if (isCompleted(player)) {
			if (isRepeatable(player)) {
				res.add("Wziąłem 25 kawałków węgla do Haunchy, ale założe się to mało i będze potrzebował więcej. Może wezmę więcej pszynych steków z grilla.");
			} else {
				res.add("Haunchy Meatoch był zadowolony, gdy dałem mu węgiel. Ma go teraz wystarczająco dużo. Dał mi kilka pysznych steków jakich w życiu nie jadłem!");
			}			
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "CoalForHaunchy";
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"waiting;"),
				new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)).fire(player,null, null);
	}

	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"waiting;").fire(player, null, null);
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Haunchy Meatoch";
	}
}
