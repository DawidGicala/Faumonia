/* $Id: FindJefsMom.java,v 1.23 2012/04/19 18:27:49 kymara Exp $ */
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

import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerCanEquipItemCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
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
 * QUEST: Find Jefs mum
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Jef</li>
 * <li>Amber</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Jef waits for his mum in Kirdneh for a longer time now and is frightened that something happened to her</li>
 * <li> You go to find Amber somewhere in Fado forest</li>
 * <li> She gives you a flower which you have to bring to Jef</li>
 * <li> You return and give the flower to Jef</li>
 * <li> Jef will reward you well</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> 5000 XP</li>
 * <li> Red lionfish which Jef got by someone who made holidays on Amazon island earlier (between 1-6)</li>
 * <li> Karma: 15</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> Once every 7200 minutes. (5 days)</li>
 * </ul>
 *
 * @author Vanessa Julius
 *
 */
public class FindJefsMom extends AbstractQuest {

	// 7200 minutes (5 days)
	private static final int REQUIRED_MINUTES = 7200;

	private static final String QUEST_SLOT = "find_jefs_mom";


	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	private void offerQuestStep() {
		final SpeakerNPC npc = npcs.get("Jef");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new OrCondition(new QuestNotStartedCondition(QUEST_SLOT), new QuestInStateCondition(QUEST_SLOT, 0, "rejected")),
			ConversationStates.QUEST_OFFERED,
			"Mama miała tylko pójść na zakupy, ale bardzo długo nie wraca. Nazywa się Amber i czasem, gdy coś ją martwi, szuka spokoju w lesie. Poszukasz jej i sprawdzisz, czy nic jej nie jest?",
			null);

		// player asks about quest which he has done already and he is allowed to repeat it
				npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"), new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
				ConversationStates.QUEST_OFFERED, 
				"Od ostatniej wiadomości od mamy minęło już sporo czasu. Staram się nie martwić, ale chciałbym wiedzieć, czy nadal dobrze się czuje. Zajrzysz jeszcze raz do Amber i przyniesiesz mi wieści?",
				null);

		// player asks about quest but time didn't pass yet
		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"), new NotCondition(new TimePassedCondition(QUEST_SLOT, 1,REQUIRED_MINUTES))), 
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES, "Dzięki tobie wiem, że mama ma się dobrze. Dajmy jej teraz trochę spokoju; o kolejne odwiedziny poproszę za "));


		// Player agrees to find mum
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Dziękuję, teraz przynajmniej ktoś jej poszuka. Rozejrzyj się za Amber w lesie na południe od Fado i powiedz, że przysyła cię Jef. Przynieś mi od niej wiadomość albo drobiazg, po którym poznam, że jest bezpieczna.",
			new MultipleActions(new SetQuestAction(QUEST_SLOT, 0, "start"),
								DialogueRewardAction.karma(new IncreaseKarmaAction(10.0))));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
			"Rozumiem, pewnie masz własne sprawy. Ja jeszcze poczekam tutaj na mamę. Gdyby udało ci się kiedyś znaleźć chwilę, nadal będę wdzięczny za pomoc.",
			new MultipleActions(new SetQuestAction(QUEST_SLOT, 0, "rejected"),
					new DecreaseKarmaAction(10.0)));

		// Player asks for quest but is already on it

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Czekam na wiadomość od Amber. Jeśli już ją spotkałeś, przynieś mi znak od niej i powiedz, że jest #cała i zdrowa. Wtedy będę wiedział, że naprawdę nic jej nie grozi.",
				null);

		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("mum", "mother", "mom", "mama"),
				null,
				ConversationStates.ATTENDING,
				"Mama nazywa się Amber. Miała zrobić zakupy, ale ostatnio dużo ją martwiło i chyba potrzebowała pobyć sama. Szukaj jej w lesie na południe od Fado i powiedz jej moje imię, Jef.",
				null);

		npc.add(
				ConversationStates.ATTENDING,
				("jeszcze"),
				null,
				ConversationStates.ATTENDING,
				"Amber rozstała się z Rogerem Framptonem i od tamtej pory jest smutna. Czasem chodzi do lasu na południe od Fado, żeby odpocząć od miasta. Ja nie potrzebuję znać wszystkich dorosłych spraw - chcę tylko wiedzieć, czy mama jest bezpieczna.",
				null);

		npc.add(
				ConversationStates.ATTENDING,
				("Jef"),
				null,
				ConversationStates.ATTENDING,
				"Tak, mam na imię Jef. Mama mówi, że bardzo lubi to imię. Jeśli ją spotkasz, wystarczy je wymienić, a będzie wiedziała, od kogo przychodzisz.",
				null);

		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("Roger Frampton", "Roger", "Frampton"),
				null,
				ConversationStates.ATTENDING,
				"Roger Frampton sprzedaje domy w Kirdneh i kiedyś był bliski mojej mamie. Teraz już się nie spotykają. Nie musisz go wypytywać, żeby mi pomóc - zależy mi na wiadomości od samej Amber.",
				null);

	}

	private void findMomStep() {
		final SpeakerNPC amber = npcs.get("Amber");

        // give the flower if it's at least 5 days since the player activated the quest the last time, and set the time slot again
		amber.add(ConversationStates.ATTENDING, "Jef",
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"start"),
							 new PlayerCanEquipItemCondition("bielikrasa")),
                          
			ConversationStates.ATTENDING, 
			"Jef cię przysłał? Powinnam była wcześniej dać mu znać, że nic mi nie jest. Weź tę bielikrasę do Kirdneh i powiedz synowi, że jestem cała i zdrowa - zna moje ulubione kwiaty i rozpozna ten znak.",
			new MultipleActions(DialogueRewardAction.item("bielikrasa", 1, true), 
                                new SetQuestAction(QUEST_SLOT, 0, "found_mom"))); 
                             

		// don't put the flower on the ground - if player has no space, tell them
		amber.add(ConversationStates.ATTENDING, "Jef",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
								 new NotCondition(new PlayerCanEquipItemCondition("bielikrasa"))),
				ConversationStates.ATTENDING, 
				"Chcę przekazać Jefowi bielikrasę, ale w twoim plecaku nie ma na nią miejsca. Zrób miejsce i ponownie wspomnij jego imię. Nie położę kwiatu na ziemi - zależy mi, żeby bezpiecznie dotarł do syna.",
				null);

        // don't give the flower if the quest state isn't start
	    amber.add(ConversationStates.ATTENDING, "Jef",
		     	new AndCondition(new QuestNotInStateCondition(QUEST_SLOT, 0, "start")),
		    	ConversationStates.ATTENDING,
		    	"Jef jest moim synem i czeka w Kirdneh. Jeśli potrzebuje pomocy, porozmawiaj najpierw z nim, żebym wiedziała, z czym przychodzisz. Nie chcę wysyłać mu sprzecznych wiadomości.", 
		    	null);


	}

	private void bringFlowerToJefStep() {
		final SpeakerNPC npc = npcs.get("Jef");

		ChatAction addRandomNumberOfItemsAction = new ChatAction() {
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				//add random number of red lionfish
				final StackableItem red_lionfish = (StackableItem) SingletonRepository.getEntityManager()
						.getItem("pokolec");
				int redlionfishamount;
				redlionfishamount = Rand.roll1D6();
				red_lionfish.setQuantity(redlionfishamount);
				int before = player.getNumberOfEquipped("pokolec");
                player.equipOrPutOnGround(red_lionfish);
                DialogueRewardAction.recordItem(player, npc, red_lionfish, before, redlionfishamount);
				npc.say("Bielikrasa! Mama uwielbia te kwiaty, więc teraz wierzę, że wszystko u niej dobrze. Weź ode mnie pokolce - " + redlionfishamount + " szt. - dostałem je od podróżnika z wyspy Amazonek. Dziękuję, że jej poszukałeś. Teraz nie będę się tak martwić.");

			}
		};
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("flower", "zantedeschia", "fine", "amber", "done", "cała", "zdrowa"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "found_mom"), new PlayerHasItemWithHimCondition("bielikrasa")),
				ConversationStates.ATTENDING, null,
				new MultipleActions(new DropItemAction("bielikrasa"), 
                                    DialogueRewardAction.xp(5000), 
									DialogueRewardAction.attackXP(1000),
									DialogueRewardAction.defenseXP(1500),
                                    DialogueRewardAction.karma(new IncreaseKarmaAction(25)),
									addRandomNumberOfItemsAction,
									new IncrementQuestAction(QUEST_SLOT, 2, 1),
									new SetQuestToTimeStampAction(QUEST_SLOT,1),
									new SetQuestAction(QUEST_SLOT, 0, "done")));


	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Znajdź matkę Jefa",
				"Jef, mały chłopiec w Kirdneh, czeka na swoją mamę Amber, która nie wróciła z zakupów na rynku.",
				false);
		offerQuestStep();
		findMomStep();
		bringFlowerToJefStep();
        npcs.get("Jef").add(ConversationStates.ATTENDING, "wskazówki", new QuestInStateCondition(QUEST_SLOT, 0, "start"), ConversationStates.ATTENDING,
            "Mama nazywa się Amber i chodzi po lesie na południe od Fado. Kiedy ją znajdziesz, powiedz, że przysyła cię Jef z Kirdneh. Jeśli da ci coś dla mnie, przynieś to tutaj - będę czekać w tym samym miejscu.", null);
        npcs.get("Jef").add(ConversationStates.ATTENDING, "bielikrasa", new QuestInStateCondition(QUEST_SLOT, 0, "found_mom"), ConversationStates.ATTENDING,
            "Chodzi o bielikrasę, kwiat od Amber. Sprawdź, czy nie został w miejscu, gdzie odkładasz rzeczy, i przynieś go tutaj. Ten drobiazg pozwoli mi uwierzyć, że wieści naprawdę pochodzą od mamy.", null);
        npcs.get("Amber").add(ConversationStates.ATTENDING, "wiadomość", new QuestInStateCondition(QUEST_SLOT, 0, "found_mom"), ConversationStates.ATTENDING,
            "Zanieś bielikrasę Jefowi w Kirdneh i powiedz, że jestem cała i zdrowa. To mój ulubiony kwiat, więc rozpozna wiadomość ode mnie. Dziękuję, że pomagasz nam znów się porozumieć.", null);

	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Znalazłem Jefa w Kirdneh. Czeka tam na swoją mamę.");
        final String questStateFull = player.getQuest(QUEST_SLOT);
        final String[] parts = questStateFull.split(";");
        final String questState = parts[0];

        if ("rejected".equals(questState)) {
			res.add("Znalezienie jego mamy kosztuje mnie teraz zbyt wiele czasu, dlatego musiałem odrzucić jego prośbę o pomoc w znalezieniu jej.");
		}
		if ("start".equals(questState)) {
			res.add("Jef poprosił mnie, żebym rozejrzał się za jego matką, Amber, która nie wróciła z zakupów na rynku. Powinienem poszukać jej w lesie na południe od Fado. Mam nadzieję, że wysłucha mnie, gdy powiem imię jej syna - Jefa.");
		}
		if ("found_mom".equals(questState)) {
			res.add("Spotkałem Amber w lesie na południe od Fado. Dała mi bielikrasę dla Jefa jako znak, że jest bezpieczna. Muszę wrócić z kwiatem do Kirdneh.");
		}
		if ("done".equals(questState)) {
            if (isRepeatable(player)) {
                res.add("Przyniosłem Jefowi bielikrasę, a on bardzo się ucieszył, gdy dowiedział się, że jego mama, Amber, trzyma się dobrze. Chociaż Jef nie chce, żebym pilnował jej znowu, trzeba zapytać go, czy nie zmienił zdania.");
            } else {
                res.add("Przyniosłem Jefowi bielikrasę, a on bardzo się ucieszył, gdy dowiedział się, że jego mama, Amber, trzyma się dobrze. Chce zostawić mamę samą na jakiś czas.	W nagrodę za ukończenie zadania otrzymałem: 5.000 pkt doświadczenia, 1.000 XP ataku, 1.500 XP obrony, 25 pkt karmy i trochę pokolców.");
            }
		}

		return res;

	}

	@Override
	public String getName() {
		return "FindJefsMom";
	}


	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
				 new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)).fire(player,null, null);
	}
	
	@Override
	public String getRegion() {
		return Region.KIRDNEH;
	}
	@Override
	public String getNPCName() {
		return "Jef";
	}
}
