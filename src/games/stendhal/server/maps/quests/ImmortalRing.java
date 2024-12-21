/* $Id: ImmortalRing.java,v 1.38 2011/11/13 17:14:57 kymara Exp $ */
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

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.CollectRequestedItemsAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemsFromCollectionAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.ItemCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * QUEST: The Sad Scientist.
 * 
 * PARTICIPANTS:
 * <ul>
 * <li>Vasi Elos, a scientist in Kalavan</li>
 * <li>Mayor Sakhs, the mayor of semos</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * 		<li>Talk to Vasi Elos, a lonely scientist.</li>
 * 		<li>Give him all stuff he needs for a present for his honey.</li>
 * 		<li>Talk to semos mayor.</li>
 * 		<li>Bring Elos mayor's letter.</li>
 * 		<li>Kill the Imperial Scientist.</li>
 *		<li>Give him the flask with his brother's blood.</li> 
 * </ul>
 * 
 * REWARD:
 * <ul>
 * 		<li>a pair of black legs</li>
 * 		<li>20 Karma</li>
 * 		<li>10000 XP</li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * 		<li>None</li>
 * </ul>
 */
public class ImmortalRing extends AbstractQuest {
	
	private static Logger logger = Logger.getLogger(ImmortalRing.class);

	private static final String LETTER_DESCRIPTION = "LIST DO MOJEGO PRZYJACIELA.";
	private static final String QUEST_SLOT = "immortal_ring";
	private static final int REQUIRED_MINUTES = 60480;
	private static final String NEEDED_ITEMS = "róg demona=100;róg jednorożca=5;ząb potwora=25;dziub ptaka=5;serce olbrzyma=10000;kieł smoka=250;pazur czerwonego smoka=2;zaklęcie pustelnika=25000;balonik=100";
	
	@Override
	public String getName() {
		return "TheImmortalRing";
	}

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
		final String questState = player.getQuest(QUEST_SLOT);
		// it might have been rejected before Vasi even explained what he wanted.
		if ("rejected".equals(questState)) {
			res.add("Nie chcę pierścienia nieśmiertelnych");
			return res;
		} 
		if ("making:".equals(questState)) {
			res.add("Musze przynieść to o co poprosił mnie Yorphin");
			return res;
		} 
		if ("find_vera".equals(questState)) {
			res.add("Muszę dostarczyć list");
			return res;
		} 
		if ("kill_scientist".equals(questState)) {
			res.add("Muszę zabić Balroga");
			return res;
		} 
		if ("done".equals(questState)){
			return res;
		}
		final List<String> debug = new ArrayList<String>();
		debug.add("Stan zadania to: " + questState);
		logger.error("Historia nie pasuje do stanu poszukiwania " + questState);
		return debug;
	}

	
	/* (non-Javadoc)
	 * @see games.stendhal.server.maps.quests.AbstractQuest#addToWorld()
	 */
	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Pierścień nieśmiertelnych",
				"Yorphin Baos, mieszkający w Fado potrafi wykuć pierścień nieśmiertelnych",
				false);
		prepareQuestSteps();
	}

	private void prepareQuestSteps() {
		prepareScientist();
	}

	private void prepareScientist() {
		final SpeakerNPC scientistNpc = npcs.get("Yorphin Baos");
		final SpeakerNPC mayorNpc = npcs.get("Flake");
		startOfQuest(scientistNpc);
		bringItemsPhase(scientistNpc);
		playerReturnsAfterRequestForLegs(scientistNpc);
		playerReturnsAfterGivingTooEarly(scientistNpc);
		playerReturnsAfterGivingWhenFinished(scientistNpc);
		playerReturnsWithoutLetter(scientistNpc);
		playerVisitsMayorSakhs(mayorNpc);
		playerReturnsWithLetter(scientistNpc);
		playerReturnsWithoutKillingTheImperialScientistOrWithoutGoblet(scientistNpc);
		playerReturnsAfterKillingTheImperialScientist(scientistNpc);
		playerReturnsToFetchReward(scientistNpc);
		playerReturnsAfterCompletingQuest(scientistNpc);
	}

	private void playerReturnsToFetchReward(SpeakerNPC npc) {
		// time has passed
		final ChatCondition condition = new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT,"decorating"),
						new TimePassedCondition(QUEST_SLOT, 1, 5)
					);
		final ChatAction action = new MultipleActions(
											new SetQuestAction(QUEST_SLOT,"done"),
											new IncreaseKarmaAction(400),
											new IncreaseXPAction(10000000),
											new IncreaseAtkXPAction(200000),
											new IncreaseDefXPAction(300000),
											// here, true = bind them to player
											new EquipItemAction("pierścień nieśmiertelnych", 1, true)
										);
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES, 
				condition, 
				ConversationStates.IDLE, 
				"Oto pierścień nieśmiertelnych. Proszę - jest Twój!",
				action);
		
		// time has not yet passed
		final ChatCondition notCondition = new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestStateStartsWithCondition(QUEST_SLOT,"decorating"),
				new NotCondition( new TimePassedCondition(QUEST_SLOT, 1, 5))
			);
		ChatAction reply = new SayTimeRemainingAction(QUEST_SLOT, 1, 5, "Nie zakończyłem wytwarzania pierścienia. " +
				"Proszę sprawdź za ");
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES, 
				notCondition, 
				ConversationStates.IDLE, 
				null, 
				reply);
	}
	
	private void playerReturnsAfterKillingTheImperialScientist(SpeakerNPC npc) {
		final ChatCondition condition = new AndCondition(
				new QuestStateStartsWithCondition(QUEST_SLOT, "kill_scientist"),
				new KilledForQuestCondition(QUEST_SLOT, 1)
			);
		ChatAction action = new MultipleActions(
										new SetQuestAction(QUEST_SLOT, "decorating;"),
										new SetQuestToTimeStampAction(QUEST_SLOT, 1)
										);
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()), condition),
				ConversationStates.ATTENDING,  
				"Ok, to ja biorę się do pracy nad twoim #pierścieniem. Wróć za 5 minut.", 
				null);
		
		npc.add(ConversationStates.ATTENDING, "pierścieniem",
				condition, ConversationStates.IDLE, 
				"Ok, tworzę pierścień. Wróć za 5 minut.", 
				action);
	}


	private void playerReturnsWithoutKillingTheImperialScientistOrWithoutGoblet(
			SpeakerNPC npc) {
		final ChatCondition condition = new AndCondition(
		new GreetingMatchesNameCondition(npc.getName()),
				new QuestStateStartsWithCondition(QUEST_SLOT, "kill_scientist"),
				new NotCondition( 
						new AndCondition( 
										new KilledForQuestCondition(QUEST_SLOT, 1))
			));
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				condition, ConversationStates.IDLE, 
				"Zanim podaruje Ci pierścień udowodnij że jesteś wystarczająco mocny - pokonaj Balroga.", 
				null);
	}
	
	private void playerReturnsWithLetter(final SpeakerNPC npc) {
		final ChatCondition condition = new AndCondition(
				new QuestStateStartsWithCondition(QUEST_SLOT, "find_vera"),
				new PlayerHasItemWithHimCondition("karteczka")
			);

		final ChatAction action = new MultipleActions(
					new SetQuestAction(QUEST_SLOT, 0, "kill_scientist"),
					new StartRecordingKillsAction(QUEST_SLOT, 1, "balrog", 0, 1),
					new DropItemAction("karteczka")
				);
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()), condition),
				ConversationStates.INFORMATION_2,
				"Witam! Czy dostarczyłeś list?",
				null);

		npc.add(ConversationStates.INFORMATION_2, Arrays.asList("letter", "yes", "note", "list", "karteczka", "tak"),
				condition,
				ConversationStates.ATTENDING,
				"Pójdź zabij Balroga, a dopiero wtedy będe mógł podarować Ci pierścień.",
				action);
		
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.GOODBYE_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, 0, "kill_scientist"),
				ConversationStates.INFORMATION_2,
				"Zrób to!",
				null);
	}
	
	private void playerReturnsWithoutLetter(final SpeakerNPC npc) {
		final ChatCondition condition = new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestStateStartsWithCondition(QUEST_SLOT, "find_vera"),
				new NotCondition(new PlayerHasItemWithHimCondition("karteczka"))
			);
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				condition,
				ConversationStates.IDLE,
				"Mam jeszcze jedną sprawe... Proszę zanieś ten list osobie o imieniu Flake. Mieszka on w Ados. Powiedz mu po prostu #list.",
				null);
	}

	private void playerVisitsMayorSakhs(final SpeakerNPC npc) {
		final ChatAction action = new ChatAction() {
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				final Item item = SingletonRepository.getEntityManager().getItem("karteczka");
				item.setInfoString(player.getName());
				item.setDescription(LETTER_DESCRIPTION);
				item.setBoundTo(player.getName());
				player.equipOrPutOnGround(item);
			}
		};
		npc.add(ConversationStates.ATTENDING, Arrays.asList("list","Yorphin"),
				new QuestStateStartsWithCondition(QUEST_SLOT, "find_vera"),
				ConversationStates.ATTENDING, 
				"Co? list od Yorphina? Nie widziałem go kopę lat! Dzięki!",
				action);
	}

	private void playerReturnsAfterGivingWhenFinished(final SpeakerNPC npc) {
		final ChatCondition condition = new AndCondition(
				new QuestStateStartsWithCondition(QUEST_SLOT, "making;"),
				new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)
			);
		final ChatAction action = new SetQuestAction(QUEST_SLOT,"find_vera");
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()), condition),
				ConversationStates.INFORMATION_1, 
				"Proszę, zaniesiesz ten list do Flake? Mieszka on w Ados. Po prostu powiedz mu #list.",
				null);
		
		npc.add(ConversationStates.INFORMATION_1, ConversationPhrases.YES_MESSAGES,
				condition,
				ConversationStates.IDLE, 
				"Dziękuje. Będę czekać.",
				action);
		
		npc.add(ConversationStates.INFORMATION_1, ConversationPhrases.NO_MESSAGES,
				condition,
				ConversationStates.IDLE, 
				"Pa! Dowidzenia!",
				null);
	}

	private void playerReturnsAfterGivingTooEarly(final SpeakerNPC npc) {
		final ChatCondition condition = new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestStateStartsWithCondition(QUEST_SLOT, "making;"),
				new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))
			);
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				condition,
				ConversationStates.IDLE, 
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES,  "Czy myślisz, że mogę pracować tak szybko? Odejdź. " +
						"Wróć za"));
	}
	
	private void bringItemsPhase(final SpeakerNPC npc) {
		//condition for quest being active and in item collection phase
		ChatCondition itemPhaseCondition = getConditionForBeingInCollectionPhase();
		
		//player returns during item collection phase
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						itemPhaseCondition),
				ConversationStates.QUESTION_1, 
				"Witaj. Czy posiadasz #przedmioty potrzebne do wykonania tego pierścienia?",
				null);
		
		//player asks for items
		npc.add(ConversationStates.QUESTION_1, Arrays.asList("items", "item", "przedmioty", "przedmiot"),
				itemPhaseCondition,
				ConversationStates.QUESTION_1, 
				null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Proszę wróć, gdy będziesz miał wszystko co jest potrzebne do zrobienia pierścienia nieśmiertelnych. Potrzebuję [items]."));
		
		//player says no
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES,
				itemPhaseCondition,
				ConversationStates.IDLE, 
				"szkoda, szkoda...",
				null);
		
		//player says yes
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.YES_MESSAGES,
				itemPhaseCondition,
				ConversationStates.QUESTION_1, 
				"Dobrze! Więc co masz dla mnie?",
				null);
		
		//add transition for each item
		final ItemCollection items = new ItemCollection();
		items.addFromQuestStateString(NEEDED_ITEMS);
		for (final Map.Entry<String, Integer> item : items.entrySet()) {
			npc.add(ConversationStates.QUESTION_1, item.getKey(), null,
					ConversationStates.QUESTION_1, null,
					new CollectRequestedItemsAction(
							item.getKey(), QUEST_SLOT,
							"Dobrze masz coś jeszcze?",
							"To już od ciebie dostałem!",
							new MultipleActions(
									new SetQuestAction(QUEST_SLOT,"legs"),
									new SayTextAction("Proszę, przynieś mi teraz miecz nieśmiertelnych. Jest mi on bardzo potrzebny  " +
											"")), ConversationStates.IDLE
							));
		}
	}
	
	
//									new SetQuestAction(QUEST_SLOT,"making;"), new SetQuestToTimeStampAction(QUEST_SLOT, 1),
	/**
	 * Creates a condition for quest being active and in item collection phase 
	 * @return the condition
	 */
	private AndCondition getConditionForBeingInCollectionPhase() {
		return new AndCondition(
													new QuestActiveCondition(QUEST_SLOT),
													new NotCondition(
															new QuestStateStartsWithCondition(QUEST_SLOT,"making;")
																	),
													new NotCondition(
															new QuestStateStartsWithCondition(QUEST_SLOT,"decorating;")
																	),
													new NotCondition(
															new QuestStateStartsWithCondition(QUEST_SLOT,"find_vera")
																	),
													new NotCondition(
															new QuestStateStartsWithCondition(QUEST_SLOT,"kill_scientist")
																	),
													new NotCondition(
															new QuestStateStartsWithCondition(QUEST_SLOT,"legs")
																	)
															);
	}
	
	private void playerReturnsAfterRequestForLegs(final SpeakerNPC npc) {
	//player returns without legs
	final AndCondition nolegscondition = new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
									new QuestInStateCondition(QUEST_SLOT, "legs"),
									new NotCondition(new PlayerHasItemWithHimCondition("miecz nieśmiertelnych"))
									);
	npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			nolegscondition,
			ConversationStates.IDLE, 
			"Witaj ponownie. Proszę wróć gdy będziesz miał przy sobie miecz nieśmiertelnych. Użyje go jako bazy do wytworzenia lepszego pierścienia.",
			null);		
		
	//player returns with legs
	final AndCondition legscondition = new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
								new QuestInStateCondition(QUEST_SLOT, "legs"),
								new PlayerHasItemWithHimCondition("miecz nieśmiertelnych")
								);
	final ChatAction action = new MultipleActions(
	new SetQuestAction(QUEST_SLOT,"making;"),
	new SetQuestToTimeStampAction(QUEST_SLOT, 1),
	new DropItemAction("miecz nieśmiertelnych"));
	npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			legscondition,
			ConversationStates.IDLE, 
			"Masz miecz nieśmiertelnych! Wspaniale!" +
			" Proszę wróc za 2 tygodnie, a pierścień będzie gotowy.",
			action);
	}

	private void startOfQuest(final SpeakerNPC npc) {
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES, 
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Odejdź!",null);

		//offer the quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Hm.... Wyglądasz jak byś chciał mi pomóc?",null);

		//accept the quest
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.QUEST_STARTED,
				"Widzę, że jesteś doświadczonym wojownikiem. Nie jest Ci potrzebny dobry #pierścień? " +
				"" ,
				null);
		
		// #gems
		npc.add(ConversationStates.QUEST_STARTED,
				Arrays.asList("pierścień", "nieśmiertelnych", "pierścionek", "zadania"),
				null,
				ConversationStates.QUEST_STARTED,
				"Zawsze chciałem zostać jubilerem. Teraz mam odpowiednie narzędzia i mogę spróbować wytworzyć naprawdę dobry pierścień #nieśmiertelnych." +
				"" ,
				null);
		
		// #legs
		npc.add(ConversationStates.QUEST_STARTED,
				Arrays.asList("nieśmiertelnych", "pierścień nieśmiertelnych"),
				null,
				ConversationStates.QUEST_STARTED,
				"Potrzebuję tak: 100 rogów demona, 5 rogów jednorożca, 10.000 serc olbrzyma, 250 kłów smoka, 2 pazury czerwonego smoka, 25.000 zaklęć pustelnika oraz 100 baloników. Chcesz mi pomóc?" +
				"" ,
				null);
		
		//yes, no after start of quest
		npc.add(ConversationStates.QUEST_STARTED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.IDLE,
				"Będę czekać cudzoziemcze." ,
				new SetQuestAction(QUEST_SLOT, NEEDED_ITEMS));
		
		npc.add(ConversationStates.QUEST_STARTED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.QUEST_STARTED,
				"Odejdź!!" ,
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
		
		//reject the quest
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Jeżeli zmienisz zdanie to możemy porozmawiać ponownie." ,
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
	}
	
	private void playerReturnsAfterCompletingQuest(final SpeakerNPC npc) {
		// after finishing the quest, just tell them to go away, and mean it.
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES, 
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT)),
				ConversationStates.IDLE,
				"Odejdź!",null);
	}
	
	// The items and surviving in the basement mean we shouldn't direct them till level 100 or so
	@Override
	public int getMinLevel() {
		return 400;
	}
	
	@Override
	public String getRegion() {
		return Region.KALAVAN;
	}

	@Override
	public String getNPCName() {
		return "Yorphin Baos";
	}
}
