/* $Id: NewsFromHackim.java,v 1.51 2011/11/13 17:14:57 kymara Exp $ */
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

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * QUEST: News from Hackim PARTICIPANTS: - Hackim - Xin Blanca
 * 
 * STEPS: - Hackim asks you to give a message to Xin Blanca. - Xin Blanca thanks
 * you with a pair of leather legs.
 * 
 * REWARD: - 10 XP - a pair of leather legs
 * 
 * REPETITIONS: - None.
 */
public class NewsFromHackim extends AbstractQuest {
	private static final String QUEST_SLOT = "news_hackim";



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
				res.add("Hackim asystent kowala chce abym zaniósł tajną wiadomość do Xin Blanca w tawernie Semos.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("QUEST_REJECTED");
			return res;
		}
			res.add("Zdecydowałem sie wykonać to zadanie. Musze dostarczyć wiadomość Hackima do Xin Blanca w tawernie Semos.");
		if (isCompleted(player)) {
			res.add("Dostarczyłem wiadomości do Xin Blanca. W zamian dostałem nowy hełm.	W nagrodę za ukończenie zadania otrzymałem: 200 pkt doświadczenia, 200 pkt ataku, 300 pkt obrony i hełm kolczy.");
		}
		return res;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Hackim Easso");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Pssst! Podejdź tutaj... zrób mi przysługę i powiedz #Xin #Blanca że nowa dostawa broni jest gotowa. Powiesz mu?",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"Dziękuję, ale nie mam wiadomości dla #Xin. Nie mogę zbyt często przemycać... sądzę, że Xoderos zaczyna coś podejrzewać. W każdym razie daj mi znać czy coś mógłbym zrobić dla Ciebie.",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Dziękuję! Jestem pewien, że #Xin dobrze Cię wynagrodzi. Daj znać jeżeli czegoś byś potrzebował.",
			new SetQuestAction(QUEST_SLOT, "start"));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Tak pomyślę o tym, poza tym nie jest mądrze wtajemniczać w to zbyt wielu ludzi... Zapomnij, że rozmawialiśmy o tym. Dobrze? Nic nie słyszałeś jeżeli wiesz o czym mówię.",
			new SetQuestAction(QUEST_SLOT, "rejected"));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("Xin", "Xin Blanca", "Blanca"),
			null,
			ConversationStates.QUEST_OFFERED,
			"Nie wiesz kto to jest Xin? Każdy w oberży zna Xina. Jest to facet, który większości ludzi w Semos jest winny pieniądze za piwo! Zrobisz to?",
			null);

			npc.addReply(Arrays.asList("Xin", "Xin Blanca", "Blanca"), "Xin's jest super. Też bym chciał pracować w oberży. Ale mój tata mówi, że powinienem uczyć się handlu.");
	}

	private void step_2() {
		final SpeakerNPC npc = npcs.get("Xin Blanca");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					String answer;
					if (player.isEquipped("hełm kolczy")) {
						answer = "Weź ten nowy hełm! Daj mi znać jeżeli będziesz czegoś potrzebował.";
					} else {
						answer = "Weź ten nowy... aha już masz... Cóż możesz go sprzedać lub wymienić.";
					}
					// player.say("Well, to make a long story short; I know
					// your business with Hackim and I'm here to tell you
					// that the next shipment is ready.");
					raiser.say("W końcu gotowe! To dobra wiadomość! Pozwól mi się odwdzięczyć za twoją pomoc... "
									+ answer);
					player.setQuest(QUEST_SLOT, "done");

					final Item item = SingletonRepository.getEntityManager().getItem("hełm kolczy");
					player.equipOrPutOnGround(item);
					player.addXP(200);
					player.addatk_xp(200);
					player.adddef_xp(300);

					player.notifyWorldAboutChanges();
				}
			});
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Wiadomości od Hackima",
				"Hackim Easso asystent kowala, potrzebuje pomocy przy przekazaniu wiadomości do kogoś w Semos. Muszę mu pomóc.",
				false);
		step_1();
		step_2();
	}

	@Override
	public String getName() {
		return "NewsFromHackim";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Hackim Easso";
	}
}
