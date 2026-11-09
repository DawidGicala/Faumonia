/* $Id: Campfire.java,v 1.65 2012/11/18 03:53:53 tigertoes Exp $ */
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
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Campfire
 * 
 * PARTICIPANTS:
 * <ul>
 * <li> Sally, a scout sitting next to a campfire near Or'ril</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Sally asks you for wood for her campfire</li>
 * <li> You collect 10 pieces of wood in the forest</li>
 * <li> You give the wood to Sally.</li>
 * <li> Sally gives you 10 meat or ham in return.<li>
 * </ul>
 * 
 * REWARD:
 * <ul> 
 * <li> 10 meat or ham</li>
 * <li> 50 XP</li>
 * <li> Karma: 10</li>
 * </ul>
 * 
 * REPETITIONS: 
 * <ul>
 * <li> Unlimited, but 5 minutes of waiting are required between repetitions</li>
 * </ul>
 */
public class Campfire extends AbstractQuest {

	private static final int REQUIRED_WOOD = 10;
	
	private static final int REQUIRED_MINUTES = 60;

	private static final String QUEST_SLOT = "campfire";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return player.hasQuest(QUEST_SLOT) && !"start".equals(player.getQuest(QUEST_SLOT)) && !"rejected".equals(player.getQuest(QUEST_SLOT));
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestNotInStateCondition(QUEST_SLOT, "start"), new QuestStartedCondition(QUEST_SLOT), new TimePassedCondition(QUEST_SLOT,REQUIRED_MINUTES)).fire(player, null, null);
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Sally");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę pomagać Sally");
			return res;
		}
		res.add("Chcę pomóc Sally");
		if ((player.isEquipped("polano", REQUIRED_WOOD)) || isCompleted(player)) {
			res.add("Znalazłem 10 polan potrzebnych do rozpalenia ogniska");
		}
		if (isCompleted(player)) {
			res.add("Dałem Sally dziesięć polan. Nagroda: 10 sztuk mięsa lub szynki, 500 XP postaci, 500 XP ataku, 500 XP obrony i 10 karmy.");
		}
		if(isRepeatable(player)){
			res.add("Mogę ponownie zapytać Sally o drewno do ogniska.");
		} 
		return res;
	}



	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Sally");

		// player returns with the promised wood
		npc.add(ConversationStates.IDLE, 
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"), new PlayerHasItemWithHimCondition("polano", REQUIRED_WOOD)),
			ConversationStates.QUEST_ITEM_BROUGHT, 
			"Dobrze, że jesteś! Ognisko wciąż się trzyma, ale zapas drewna szybko maleje. Czy oddasz mi teraz obiecane dziesięć polan?",
			null);

		//player returns without promised wood
		npc.add(ConversationStates.IDLE, 
			ConversationPhrases.GREETING_MESSAGES,
		    new	AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"), new NotCondition(new PlayerHasItemWithHimCondition("polano", REQUIRED_WOOD))),
			ConversationStates.ATTENDING, 
			"Witaj z powrotem. Wciąż czekam na dziesięć polan, żeby podtrzymać ognisko. Drewna szukaj w lesie na północ stąd; ja przez ten czas dopilnuję żaru.",
			null);

		// first chat of player with sally
		npc.add(ConversationStates.IDLE, 
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING, "Cześć, przysiądź na chwilę przy ognisku. Pilnuję pieczenia, ale drewna zaczyna brakować. Przydałaby mi się drobna #przysługa.",
			null);

		// player who is rejected or 'done' but waiting to start again, returns
		npc.add(ConversationStates.IDLE, 
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestNotInStateCondition(QUEST_SLOT, "start"),
					new QuestStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Witaj ponownie, dobrze znów cię widzieć przy ognisku. Odpocznij chwilę, jeśli droga dała ci się we znaki.", 
			null);
		
		// if they ask for quest while on it, remind them
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"Potrzebuję obiecanych dziesięciu polan. Pozbieraj je w lesie na północ stąd i przynieś wszystkie razem. Za pomoc odłożę dla ciebie część jedzenia z ogniska.",
			null);

		// first time player asks/ player had rejected
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, 
				"Ognisko przygasa, a nie mogę zostawić pieczenia bez opieki. W lesie na północ stąd znajdziesz drewno - potrzebuję dziesięciu polan. Przyniesiesz je w zamian za trochę jedzenia na drogę?",
				null);
		
		// player returns - enough time has passed
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestNotInStateCondition(QUEST_SLOT, "start"), new QuestStartedCondition(QUEST_SLOT), new TimePassedCondition(QUEST_SLOT,REQUIRED_MINUTES)),
				ConversationStates.QUEST_OFFERED, 
				"Poprzedni zapas drewna już się wypalił, a na żarze czeka kolejna porcja jedzenia. Potrzebuję znów dziesięciu polan z lasu na północ stąd. Pomożesz mi jeszcze raz?",
				null);

		// player returns - enough time has passed
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestNotInStateCondition(QUEST_SLOT, "start"), new QuestStartedCondition(QUEST_SLOT), new NotCondition(new TimePassedCondition(QUEST_SLOT,REQUIRED_MINUTES))),
				ConversationStates.ATTENDING, 
				null,
				new SayTimeRemainingAction(QUEST_SLOT,REQUIRED_MINUTES,"Dziękuję, zapas z ostatniej wyprawy jeszcze wystarcza. Odpocznij, zanim znów ruszysz po drewno. Kolejnej pomocy będę potrzebowała za"));

		// player is willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Świetnie, teraz mogę spokojnie dopilnować jedzenia. Szukaj leżącego drewna w lesie na północ stąd i przynieś dziesięć polan naraz. Będę czekać przy tym ognisku.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		// player is not willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Szkoda, przydałaby mi się pomoc przy zbieraniu drewna. Spróbuję jeszcze trochę podtrzymać ogień tym, co zostało. Jeśli zmienisz zdanie, wiesz, gdzie mnie znaleźć.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Sally");
		// player has wood and tells sally, yes, it is for her
		
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("polano", REQUIRED_WOOD));
		reward.add(DialogueRewardAction.xp(500));
		reward.add(DialogueRewardAction.attackXP(500));
		reward.add(DialogueRewardAction.defenseXP(500));
		reward.add(new SetQuestToTimeStampAction(QUEST_SLOT));
		reward.add(DialogueRewardAction.karma(new IncreaseKarmaAction(10)));
		reward.add(new ChatAction() {
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				String rewardClass;
				if (Rand.throwCoin() == 1) {
					rewardClass = "mięso";
				} else {
					rewardClass = "szynka";
				}
				npc.say("O, z takim zapasem ognisko szybko nie zgaśnie. Odłożyłam dla ciebie jedzenie na drogę: " + rewardClass + " - " + REQUIRED_WOOD + ". Dziękuję za pomoc i odpocznij chwilę, zanim wyruszysz dalej.");
				final StackableItem reward = (StackableItem) SingletonRepository.getEntityManager().getItem(rewardClass);
				reward.setQuantity(REQUIRED_WOOD);
				int before = player.getNumberOfEquipped(rewardClass);
                player.equipOrPutOnGround(reward);
                DialogueRewardAction.recordItem(player, npc, reward, before, REQUIRED_WOOD);
				player.notifyWorldAboutChanges();
			}
		});
		
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES, 
			new PlayerHasItemWithHimCondition("polano", REQUIRED_WOOD),
			ConversationStates.ATTENDING, null,
			new MultipleActions(reward));

		//player said the polano was for her but has dropped it from his bag or hands
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES, 
			new NotCondition(new PlayerHasItemWithHimCondition("polano", REQUIRED_WOOD)),
			ConversationStates.ATTENDING, 
			"Nie widzę już całych dziesięciu polan. Sprawdź, czy niczego nie odłożono po drodze, i przynieś komplet. Poczekam przy ognisku.",
			null);

		// player had wood but said it is not for sally
		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Dobrze, zostawmy drewno na razie w twoim plecaku. Kiedy zechcesz je przekazać, powiedz mi o polanach. Będę tutaj pilnować ognia.",
			null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Obozowisko", 
				"Sally chce rozpalić ognisko, ale nie posiada drewna.", 
				true);
		prepareRequestingStep();
		prepareBringingStep();
        SpeakerNPC npc = npcs.get("Sally");
        npc.add(ConversationStates.ATTENDING, "polano",
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new PlayerHasItemWithHimCondition("polano", REQUIRED_WOOD)),
            ConversationStates.QUEST_ITEM_BROUGHT,
            "Widzę, że udało ci się nazbierać drewna. Czy mam wziąć dziesięć polan na ognisko? Odłożę dla ciebie jedzenie na dalszą drogę.", null);
        npc.add(ConversationStates.ATTENDING, "wskazówki", new QuestInStateCondition(QUEST_SLOT, "start"), ConversationStates.ATTENDING,
            "Polana leżą w lesie na północ od mojego obozowiska przy rzece Orril. Pozbieraj dziesięć i przynieś je razem, żebym miała zapas do pieczenia. Wróć tutaj i powiedz o polanach - nie trzeba znów przyjmować zadania.", null);

	}

	@Override
	public String getName() {
		return "Campfire";
	}
	
	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public String getNPCName() {
		return "Sally";
	}
	
	@Override
	public String getRegion() {
		return Region.ORRIL;
	}
}
