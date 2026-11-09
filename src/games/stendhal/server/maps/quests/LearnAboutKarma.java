/* $Id: LearnAboutKarma.java,v 1.26 2011/11/13 17:14:15 kymara Exp $ */
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

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.EnableFeatureAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * QUEST: Learn about Karma
 * 
 * PARTICIPANTS:
 * <ul>
 * <li>Sarzina, the friendly wizardess who also sells potions in Fado</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li>Sarzina asks if you are a helpful person</li>
 * <li>You get good or bad karma depending on what you say</li>
 * <li>You get the chance to learn about karma and find out what yours is.</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li>Some Karma</li>
 * <li>Knowledge</li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li>Can always learn about karma but not get the bonus each time</li>
 * </ul>
 */
public class LearnAboutKarma extends AbstractQuest {

	private static final String QUEST_SLOT = "learn_karma";


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
		res.add("Spotkałem Sarzina w domku w Fado i zapytałem ją o zadanie.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("done")) {
			res.add("Sarzina powiedziała mi o karmie i o tym, że mogę wrócić, aby przypomnieć sobie jak to działa.");
		}
		return res;
	}

	private void step1() {
		final SpeakerNPC npc = npcs.get("Sarzina");
		
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED, 
			"Mieszam lekarstwa, ale nie każdą ranę leczy zawartość flaszy. Uczynki również zostawiają po sobie ślad, który nazywamy karmą. Starasz się pomagać innym?", null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"Jeśli chcesz by towarzyszyła ci dobra #karma jedyne co musisz robić, to pomagać innym. Znam dziewczę o imieniu Sally, która potrzebuje drewna, " 
			+ "i znam inne dziewczę co zwie się Annie, która uwielbia lody. Cóż, znam wielu mieszkańców tej krainy, którzy stale potrzebować będą pomocy. Jestem pewna, że jeśli im pomożesz czeka cię sowita zapłata.", null);

		// player is willing to help other people
		// player gets a little karma bonus
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Dobrze słyszeć, że dostrzegasz cudze kłopoty. Pokażę ci wskaźnik karmy, żebyś mógł obserwować, jak zmieniają ją twoje uczynki. Jeśli chcesz, wyjaśnię ci teraz, co oznacza #karma.",
			new MultipleActions(
					DialogueRewardAction.karma(new IncreaseKarmaAction(5.0)), 
					new SetQuestAction(QUEST_SLOT, "done"),
					DialogueRewardAction.unlock("karma_indicator", "Wskaźnik karmy")));

		// player is not willing to help other people
		// player gets a little karma removed
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Taka odpowiedź także ma swoje następstwa: tracisz dziesięć punktów karmy. Pokażę ci jednak jej wskaźnik, żebyś mógł śledzić własne wybory. O działanie karmy możesz zapytać mnie w każdej chwili.",
			new MultipleActions(
					new DecreaseKarmaAction(10.0), 
					new SetQuestAction(QUEST_SLOT, "done"),
					DialogueRewardAction.unlock("karma_indicator", "Wskaźnik karmy")));

		// player wants to know what karma is
		npc.add(
			ConversationStates.ATTENDING,
			"karma",
			null,
			ConversationStates.QUESTION_1,
			"Pomaganie mieszkańcom może poprawić twoją karmę. Dobra karma sprzyja powodzeniu, " 
			+ "na przykład podczas łowienia ryb i szukania złota, ale nie gwarantuje sukcesu. Część tego szczęścia zużywa się przy takich próbach, więc wskaźnik może zmieniać się także między zadaniami. " 
			+ "Chcesz wiedzieć, jaką masz teraz karmę?",
			null);

		// player wants to know what his own karma is
		npc.add(ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final long roundedkarma = Math.round(player.getKarma());
					final String Yk = "Twoja karma ";
					final String canseekarma = "Teraz zobaczymy jakiego koloru jest twoja karma. ";
					final String rk = Long.toString(roundedkarma);
                    if (roundedkarma > 499 ) {
                        npc.say(Yk+"jest niesamowicie wysoka, "+rk+"! Musiałeś zrobić bardzo dużo dobrych uczynków. " + canseekarma + " Jest na niebiesko." );
                    } else if (roundedkarma > 99) {
                        npc.say(Yk+"jest wysoka, "+rk+". " + canseekarma + " Jest jeszcze na niebiesko.");
                    } else if (roundedkarma > 5) {
                    	npc.say(Yk+"wynosi "+rk+" jest dobra. " + canseekarma + " Musisz uważać zbliża się do koloru czerwonego.");
                    } else if (roundedkarma > -5) {
                        npc.say(Yk+"wynosi "+rk+". " + canseekarma + " Znajduje się w połowie skali.");
                    } else if (roundedkarma > -99) {
                        npc.say(Yk+"to "+rk+" jest zła. " + canseekarma + " Postaraj się aby była na niebiesko.");
                    } else if (roundedkarma > -499) {
                        npc.say(Yk+"jest straszna, "+rk+"! " + canseekarma + " Jest na czerwono.");
                    } else {
                    	npc.say(Yk+"jest katasrofalna, "+rk+"!!! " + canseekarma + " Zabrakło skali dla niej. Musisz być bardzo złym człowiekiem... ");
                    }
				}
			});

		// player doesn't want to know what his own karma is
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES,
			null, ConversationStates.ATTENDING,
			"Zatem, mogę ci jeszcze jakoś pomóc?", null);
		
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.QUEST_MESSAGES,
				null, ConversationStates.QUESTION_1,
				"Jeśli popytasz o zadania tu i tam, wypełnisz je, wówczas twoja karma urośnie.Chcesz wiedzieć, jaką masz teraz karmę?", null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Nauka o Karmie", 
				"Sarzina wyjaśnia, jak uczynki wpływają na karmę. Wysłuchaj jej i zapytaj, co potrafi powiedzieć o twojej postaci.", 
				false);
		step1();
	}
	
	@Override
	public String getName() {
		return "LearnAboutKarma";
	}
	
	@Override
	public String getRegion() {
		return Region.FADO_CITY;
	}
	@Override
	public String getNPCName() {
		return "Sarzina";
	}
}
