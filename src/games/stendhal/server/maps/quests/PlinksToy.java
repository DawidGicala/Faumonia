/* $Id: PlinksToy.java,v 1.58 2012/04/19 18:26:42 kymara Exp $ */
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

import games.stendhal.server.entity.npc.condition.QuestInStateCondition;

import games.stendhal.server.entity.npc.action.DialogueRewardAction;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.spawner.PassiveEntityRespawnPoint;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Plink's Toy
 * <p>
 * PARTICIPANTS: <ul><li> Plink <li> some wolves </ul>
 * 
 * STEPS: <ul><li> Plink tells you that he got scared by some wolves and ran away
 * dropping his teddy. <li> Find the teddy in the Park Of Wolves <li> Bring it back to
 * Plink </ul>
 * 
 * REWARD: <ul><li> a smile <li> 20 XP <li> 10 Karma </ul>
 * 
 * REPETITIONS: <ul><li> None. </ul>
 */
public class PlinksToy extends AbstractQuest {

	private static final String QUEST_SLOT = "plinks_toy";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			if (player.isEquipped("pluszowy miś")) {
				res.add("Mam pluszowego misia. Warto sprawdzić, czy należy do Plinka.");
			}
			return res;
		}
		res.add("Spotkałem Plinka na równinie na północ od Semos.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Powinienem szukać pluszowego misia Plinka w pobliżu wilków");
			return res;
		}
		res.add("Obiecałem odszukać misia w parku na wschód od Plinka, w pobliżu wilków.");
		if ((player.isEquipped("pluszowy miś")) || isCompleted(player)) {
			res.add("Znalazłem pluszowego misia, którego szuka Plink.");
		}
		if (isCompleted(player)) {
			res.add("Oddałem Plinkowi misia. Nagroda: 200 XP postaci, 100 XP ataku, 150 XP obrony i 30 karmy.");
		}
		return res;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Plink");

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestNotStartedCondition(QUEST_SLOT),
					new NotCondition(new PlayerHasItemWithHimCondition("pluszowy miś"))),
			ConversationStates.QUEST_OFFERED,
			"W parku na wschód stąd zobaczyłem wilki i uciekłem tak szybko, że zgubiłem mojego #misia. Został tam sam, a ja boję się wrócić... Poszukasz go dla mnie?",
			null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, "Naprawdę go poszukasz? Zostawiłem go w parku na wschód stąd, tam gdzie kręcą się wilki. Będę czekać tutaj, tylko uważaj na siebie!",
			new SetQuestAction(QUEST_SLOT, "start"));

		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.QUEST_OFFERED,
			"Ale on tam został zupełnie sam... Rozumiem, jeśli też boisz się wilków. Gdyby udało ci się jednak go zobaczyć, proszę, pamiętaj o mnie.", null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("wolf", "wolves", "wilków"),
			null,
			ConversationStates.QUEST_OFFERED,
			"Wilki chodzą po parku na wschód stąd, na równinie na północ od Semos. Miś powinien leżeć na ziemi w ich pobliżu. Nie trzeba ich wszystkich pokonywać, ale nie podchodź nieostrożnie!",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("park", "parku"),
			null,
			ConversationStates.QUEST_OFFERED,
			"Park jest na wschód stąd, na równinie na północ od Semos. Rozglądaj się za pluszowym misiem leżącym na ziemi, niedaleko wilków. Jeśli go znajdziesz, przynieś go tutaj - już nie zostawię go samego.",
			null);

		npc.add(ConversationStates.QUEST_OFFERED, "pluszowy miś", null,
			ConversationStates.QUEST_OFFERED,
			"To mój ulubiony pluszowy miś. Zgubiłem go na ziemi w parku na wschód stąd, gdy uciekałem przed wilkami. Poznam go od razu, tylko przynieś go tutaj.",
			null);
	}


    private void continuation() {
        SpeakerNPC npc = npcs.get("Plink");
        npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
                new QuestInStateCondition(QUEST_SLOT, "start"),
                new NotCondition(new PlayerHasItemWithHimCondition("pluszowy miś"))),
            ConversationStates.ATTENDING,
            "Witaj z powrotem. Wciąż wypatruję mojego misia, ale do parku sam już nie pójdę. Potrzebujesz przypomnienia, gdzie go zgubiłem?", null);
        npc.add(ConversationStates.ATTENDING, "wskazówki", null, ConversationStates.ATTENDING,
            "Miś został na ziemi w parku na wschód stąd, na równinie na północ od Semos. Rozglądaj się w pobliżu wilków, ale uważaj na ich zęby. Przynieś mi zabawkę tutaj, jeśli ją znajdziesz.", null);
        npc.add(ConversationStates.QUEST_OFFERED, "wskazówki", null, ConversationStates.QUEST_OFFERED,
            "Miś leży na ziemi w parku na wschód stąd, niedaleko wilków na równinie na północ od Semos. Nie trzeba pokonywać wszystkich wilków, ale trzeba uważać, zbierając zabawkę. Podejmiesz się poszukiwań?", null);
    }

	private void step_2() {
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("0_semos_plains_n");
		final PassiveEntityRespawnPoint teddyRespawner = new PassiveEntityRespawnPoint("pluszowy miś", 1500);
		teddyRespawner.setPosition(107, 84);
		teddyRespawner.setDescription("Oto miś leżący w piasku.");
		zone.add(teddyRespawner);

		teddyRespawner.setToFullGrowth();
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Plink");

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("pluszowy miś"));
		reward.add(DialogueRewardAction.xp(200));
		reward.add(DialogueRewardAction.attackXP(100));
		reward.add(DialogueRewardAction.defenseXP(150));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(DialogueRewardAction.karma(new IncreaseKarmaAction(30)));
        npc.add(ConversationStates.ATTENDING, Arrays.asList("miś", "pluszowy miś"),
            new AndCondition(new QuestNotCompletedCondition(QUEST_SLOT), new PlayerHasItemWithHimCondition("pluszowy miś")),
            ConversationStates.ATTENDING,
            "To on, mój miś! Już myślałem, że nigdy go nie zobaczę. Dziękuję - teraz będę go mocno trzymać i pobawimy się z dala od wilków.", new MultipleActions(reward));

		
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
							new OrCondition(
									new QuestNotStartedCondition(QUEST_SLOT),
									new QuestNotCompletedCondition(QUEST_SLOT)),
							new PlayerHasItemWithHimCondition("pluszowy miś")),
			ConversationStates.ATTENDING, 
			"To on, mój miś! Już myślałem, że nigdy go nie zobaczę, a teraz znów możemy wrócić razem do domu. Dziękuję - będę go mocno trzymać!",
			new MultipleActions(reward));

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("pluszowy miś","miś"),
			new AndCondition(new QuestNotCompletedCondition(QUEST_SLOT), new NotCondition(new PlayerHasItemWithHimCondition("pluszowy miś"))),
			ConversationStates.ATTENDING,
			"Szukaj mojego misia na ziemi w parku na wschód stąd, na równinie na północ od Semos. W pobliżu chodzą wilki, więc uważaj. Ja zostanę tutaj, żebyśmy się nie minęli.",
			null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestCompletedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Miś jest już ze mną, dzięki tobie! Teraz bawimy się z dala od wilków. Myślę, że on też woli spokojniejsze zabawy.", null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Zabawka Plinka",
				"Plink zgubił pluszowego misia podczas ucieczki przed wilkami. Zabawka została w parku na równinie na północ od Semos.",
				false);
		step_1();
		step_2();
		step_3();
        continuation();
	}

	@Override
	public String getName() {
		return "PlinksToy";
	}

	@Override
	public String getNPCName() {
		return "Plink";
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	
}
