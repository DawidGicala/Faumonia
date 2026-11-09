/* $Id: FindRatChildren.java,v 1.18 2012/04/24 17:01:18 kymara Exp $ */
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
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.npc.condition.TriggerInListCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * QUEST: Find Rat children
 *
 * PARTICIPANTS:
 * <ul>
 * <li> Agnus</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Agnus asks you to find her children and see if they are ok</li>
 * <li> You go find them and remember their names</li>
 * <li> You return and say the names</li>
 * <li> Agnus checks you have met them, then gives reward</li>
 * <li> Note: you can not meet the children before you started the quest with her</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> 5000 XP</li>
 * <li> Karma: 15</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> Once every 24 hours.</li>
 * </ul>
 */
public class FindRatChildren extends AbstractQuest {
    @Override public boolean isRepeatable(Player player) {
        return isCompleted(player) && new TimePassedCondition(QUEST_SLOT,1,REQUIRED_MINUTES).fire(player,null,null);
    }


	private static Logger logger = Logger.getLogger(FindRatChildren.class);

	private static final String QUEST_SLOT = "find_rat_kids";

	// twenty four hours
	private static final int REQUIRED_MINUTES = 240 * 60;
	
	// children names must be lower text as this is what we compare against
	private static final List<String> NEEDED_KIDS =
		Arrays.asList("avalon", "cody", "mariel", "opal");


	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private List<String> missingNames(final Player player) {
		if (!player.hasQuest(QUEST_SLOT)) {
			return NEEDED_KIDS;
		}
		/*
		 * the format of the list quest slot is
		 * "looking;name;name;...:said;name;name;..."
		 */
		// put the children name to lower case so we can match it, however the player wrote the name
		final String npcDoneText = player.getQuest(QUEST_SLOT).toLowerCase();
		final String[] doneAndFound = npcDoneText.split(":");
		final List<String> result = new LinkedList<String>();
		if (doneAndFound.length > 1) {
			final String[] done = doneAndFound[1].split(";");
			final List<String> doneList = Arrays.asList(done);
			for (final String name : NEEDED_KIDS) {
				if (!doneList.contains(name)) {
					result.add(name);
				}
			}
		}
		return result;
	}

	private void askingStep() {
		final SpeakerNPC npc = npcs.get("Agnus");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new OrCondition(new QuestNotStartedCondition(QUEST_SLOT), new QuestInStateCondition(QUEST_SLOT, "rejected")),
				ConversationStates.QUEST_OFFERED,
				"Jestem bardzo zmartwiona. Gdybym tylko wiedziała, że moje #dzieci są bezpieczne, czułabym się lepiej.",
				null);

		npc.add(
				ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT),
						new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"Pomożesz mi jeszcze raz odszukać moje dzieci?",
				null);

        // Same authoritative step, without requiring the player to leave and greet again.
        npc.add(
				ConversationStates.ATTENDING, "dzieci",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestCompletedCondition(QUEST_SLOT),
						new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"Pomożesz mi jeszcze raz odszukać moje dzieci?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Dlaczego są tak długo na dworze. Poszukaj je, sprawdź czy wszystko z nimi w porządku.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestCompletedCondition(QUEST_SLOT), new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),
				ConversationStates.ATTENDING,
				"Dziękuję! Czuję się lepiej wiedząc, że są bezpieczne.",
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Dziękuję, od razu czuję się mniej samotna z tym zmartwieniem. Szukaj dzieci w lochach Orril i przywitaj się z każdym z nich. Przy powrocie wymień ich imiona, żebym wiedziała, kto jest bezpieczny.",
				DialogueRewardAction.karma(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "looking:said", 5.0)));

		npc.add(
				ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Hmmm... Nic nie szkodzi. Jestem pewna, że znajdzie się ktoś kto mi pomoże.",
				DialogueRewardAction.karma(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -15.0)));

		npc.add(
				ConversationStates.QUEST_OFFERED,
				"dzieci",
				null,
				ConversationStates.QUEST_OFFERED,
				"Avalon, Cody, Mariel i Opal poszli bawić się w podziemnych tunelach, a ja coraz bardziej się martwię. Odszukasz każde z nich i sprawdzisz, czy jest bezpieczne? Potem wróć i przekaż mi wiadomości, podając ich imiona.",
				null);
	}

	private void findingStep() {
		// Player goes to look for the children
		
	}

	private void retrievingStep() {

		final SpeakerNPC npc = npcs.get("Agnus");

		// the player returns to Agnus after having started the quest, or found
		// some kids.
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.QUESTION_1,
				"Powiedz, od którego dziecka przynosisz wiadomość. Zapamiętam każdą potwierdzoną wieść, więc możesz wracać do mnie po kolejnych spotkaniach. Czekam na wiadomości od Avalona, Cody'ego, Mariel i Opal.", null);

        // Same authoritative step, without requiring the player to leave and greet again.
        npc.add(ConversationStates.ATTENDING, "dzieci",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.QUESTION_1,
				"Powiedz, od którego dziecka przynosisz wiadomość. Zapamiętam każdą potwierdzoną wieść, więc możesz wracać do mnie po kolejnych spotkaniach. Czekam na wiadomości od Avalona, Cody'ego, Mariel i Opal.", null);

		for(final String name : NEEDED_KIDS) {
			npc.add(ConversationStates.QUESTION_1, name, null,
					ConversationStates.QUESTION_1, null,
					new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String npcQuestText = player.getQuest(QUEST_SLOT).toLowerCase();
					final String[] npcDoneText = npcQuestText.split(":");
					final String lookingStr;
					final String saidStr;
					if (npcDoneText.length > 1) {
						lookingStr = npcDoneText[0];
						saidStr = npcDoneText[1];
					} else {
						// compatibility with broken quests - should never happen
						logger.warn("Player " + player.getTitle() + " found with find_rat_kids quest slot in state " + player.getQuest(QUEST_SLOT) + " - now setting this to done.");
						player.setQuest(QUEST_SLOT, "done");
						npc.say("Przepraszam, wygląda na to, że już je znalazłeś. Jestem roztrzepana.");
						player.notifyWorldAboutChanges();
						npc.setCurrentState(ConversationStates.ATTENDING);
						return;
					}

					final List<String> looking = Arrays.asList(lookingStr.split(";"));
					final List<String> said = Arrays.asList(saidStr.split(";"));
					String reply = "";
					List<String> missing = missingNames(player);
					final boolean isMissing = missing.contains(name);

					if (isMissing && looking.contains(name) && !said.contains(name)) {
						// we haven't said the name yet so we add it to the list
						player.setQuest(QUEST_SLOT, lookingStr
								+ ":" + saidStr + ";" + name);
						reply = "Dziękuję.";
					} else if (!looking.contains(name)) {
						// we have said it was a valid name but haven't seen them
						reply = "Nie mam jeszcze potwierdzenia, że udało ci się spotkać to dziecko. Odszukaj je i przywitaj się z nim, zanim przekażesz mi wiadomość. Wolę poczekać na pewne wieści.";
					} else if (!isMissing && said.contains(name)) {
						// we have said the name so we are stupid!
						reply = "Już mi mówiłeś, że z tym dzieckiem jest wszystko dobrze.";
					} else {
						assert false;
					}

					// we may have changed the missing list
					missing = missingNames(player);

					if (!missing.isEmpty()) {
						reply += " Jeżeli widziałeś inne z moich dzieci powiedz mi które.";
						npc.say(reply);
					} else {
						DialogueRewardAction.xp(5000).fire(player, null, npc);
						DialogueRewardAction.karma(new games.stendhal.server.entity.npc.action.IncreaseKarmaAction(50)).fire(player, null, npc);
						reply += " Uff.... teraz mogę odsapnąć wiedząc, że z dziećmi jest wszystko w porządku.";
						npc.say(reply);
						player.setQuest(QUEST_SLOT, "done;" + System.currentTimeMillis());
						player.notifyWorldAboutChanges();
						npc.setCurrentState(ConversationStates.ATTENDING);
					}
				}
			});
		}

		final List<String> triggers = new ArrayList<String>();
		triggers.add(ConversationPhrases.NO_EXPRESSION);
		triggers.addAll(ConversationPhrases.GOODBYE_MESSAGES);
		npc.add(ConversationStates.QUESTION_1, triggers, null,
				ConversationStates.IDLE, "Nie ma problemu, wróć później.", null);

		// player says something which isn't in the needed kids list.
		npc.add(
				ConversationStates.QUESTION_1,
				"",
				new NotCondition(new TriggerInListCondition(NEEDED_KIDS)),
				ConversationStates.QUESTION_1,
				"Przepraszam, ale nie zrozumiałam ciebie. Jakie imię powiedziałeś?",
				null);

		npc.add(
				ConversationStates.QUESTION_1,
				"dzieci",
				null,
				ConversationStates.QUESTION_1,
				"Pragnę aby z dziećmi było wszystko w porządku. Które dziecko widziałeś? Powiedz mi jego imię.",
				null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Znajdź Szczurze Dzieci",
				"Agnus z miasta szczurów niepokoi się o dzieci, które zapuściły się w tunele. Odnajdź je, porozmawiaj z nimi i przekaż matce wieści o każdym z nich.",
				true);
		askingStep();
		findingStep();
		retrievingStep();
	}

	@Override
	public String getName() {
		return "FindRatChildren";
	}
	
	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			res.add("Agnus naprawdę martwi się o swoje dzieci, które są w tunelach. Muszę je znaleźć i porozmawiać z nimi, aby sprawdzić czy są w porządku.");
			if ("rejected".equals(player.getQuest(QUEST_SLOT))) {
				res.add("Nie chce jej pomóc.");
				return res;
			}
			if (!isCompleted(player)) {
				res.add("Przekazano Agnus wieści o " + (NEEDED_KIDS.size()-missingNames(player).size()) + " z " + NEEDED_KIDS.size() + " dzieci. Pozostałe trzeba odszukać i potwierdzić ich bezpieczeństwo.");
			} else {
				res.add("Agnus jest szczęśliwa, że znalazłem jej dzieci. Znalezienie ich zaprocentowało większym doświadczeniem.	W nagrodę za ukończenie zadania otrzymałem: 5.000 pkt doświadczenia i 50 pkt karmy.");
			}
			return res;
	}

	@Override
	public int getMinLevel() {
		return 20;
	}

	@Override
	public String getNPCName() {
		return "Agnus";
	}
	
	@Override
	public String getRegion() {
		return Region.ORRIL_DUNGEONS;
	}
}
