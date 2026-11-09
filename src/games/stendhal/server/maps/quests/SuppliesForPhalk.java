/* $Id: SuppliesForPhalk.java,v 1.15 2012/04/24 17:01:18 kymara Exp $ */
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

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropInfostringItemAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasInfostringItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * QUEST: Supplies For Phalk
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Phalk, the dwarf guarding Semos mines</li>
 * <li>Wrvil, a kobold weapon trader in Wo'fol</li>
 * <li>Mrotho, in Ados barracks</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>1. Phalk asks for some food and drink</li>
 * <li>2. Once you brought him the food and drink, Phalk asks you to collect his clothes from Wrvil and Mrotho</li>
 * <li>3. Wrvil gives you Phalk's special dwarf cloak but you must pay for it in arrows</li>
 * <li>4. Mrotho gives you Phalk's special golden armor but you must pay for it in gold bars</li>
 * <li>5. Phalk accepts only the special items from Wrvil and Mrotho with his name on</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>Dwarvish armor</li>
 * <li>5000 XP in total</li>
 * <li>Karma</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>Not repeatable.</li>
 * </ul>
 */
 
 public class SuppliesForPhalk extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeCollectionDetails {
 
 	private static final String QUEST_SLOT = "supplies_for_phalk";
 	
 	private static Logger logger = Logger.getLogger(SuppliesForPhalk.class);
 	
 	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	private void askForFood() {
		final SpeakerNPC npc = npcs.get("Phalk");	
		
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new QuestNotStartedCondition(QUEST_SLOT),
            ConversationStates.QUEST_OFFERED,
            "Stoję tutaj i ostrzegam wędrowców, a zapasy dawno mi się skończyły. Nie mogę po prostu porzucić posterunku i pójść do tawerny. Przyniesiesz mi coś do jedzenia i picia?",
            null);
							
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new QuestCompletedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Dziękuję za przyniesienie mi trochę jedzenia i ubrań. Sądzę, że teraz mogę stać tutaj i ostrzegać ludzi przez kilka miesięcy.",
            null);
		

		npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.YES_MESSAGES,
            new games.stendhal.server.entity.npc.condition.QuestNotStartedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Przynieś po trzy kanapki, soki z chmielu i napoje z winogron. Kanapek szukaj w piekarni, a napojów w tawernie. Gdy będziesz mieć całość, powiedz #jedzenie.",
            new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.NO_MESSAGES,
            new games.stendhal.server.entity.npc.condition.QuestNotStartedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Och to nie jest miłe..., ale dobrze. Może następna osoba mi pomoże.",
            new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
		
		npc.addReply("sok z chmielu", "Oczywiście w tawernie!");
		npc.addReply("napój z winogron", "Oczywiście w tawernie!");
		npc.addReply(Arrays.asList("sandwiches", "sandwich","kanapki","kanapka","sandwiche"), "Zapytaj w piekarni!");
	}
	
	private void receiveFood() {
	final SpeakerNPC npc = npcs.get("Phalk");	
	
		npc.add(ConversationStates.ATTENDING,
            Arrays.asList("food", "jedzenie", "jedzenia"),
            new QuestInStateCondition(QUEST_SLOT, "start"),
            ConversationStates.QUEST_ITEM_QUESTION,
            "Masz 3 kanapki, 3 butelki soku z chmielu i 3 szklanki napoju z winogron?",
            null);
		
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseXPAction(1000));
		actions.add(new DropItemAction("kanapka",3));
		actions.add(new DropItemAction("sok z chmielu",3));
		actions.add(new DropItemAction("napój z winogron",3));
		// the extra parts in the quest state are for wrvil and mrotho not to give them cloaks and armor twice
		actions.add(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "clothes;none;none", 2.0));		
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
            ConversationPhrases.YES_MESSAGES,
            new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new PlayerHasItemWithHimCondition("kanapka",3),
						new PlayerHasItemWithHimCondition("sok z chmielu",3),
						new PlayerHasItemWithHimCondition("napój z winogron",3)),
            ConversationStates.ATTENDING,
            "Wreszcie mogę coś zjeść, dziękuję! Potrzebuję jeszcze ubrań oznaczonych moim imieniem: płaszcz czeka u Wrvila w Wofol za 20 żelaznych strzał, a zbroja u Mrotho w Ados za 10 żelaza. Powiedz im #Phalk, odbierz obie rzeczy i wróć z hasłem #ubrania.",
            new MultipleActions(actions));
		
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
            ConversationPhrases.YES_MESSAGES,
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"),
				new NotCondition(
						new AndCondition(
								new PlayerHasItemWithHimCondition("kanapka",3),
								new PlayerHasItemWithHimCondition("sok z chmielu",3),
								new PlayerHasItemWithHimCondition("napój z winogron",3)))),
            ConversationStates.ATTENDING,
            "Nie masz jeszcze całego zapasu. Potrzebuję po trzy kanapki, soki z chmielu i napoje z winogron; sprawdź, czego brakuje, i wróć z kompletem.",
            null);
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
            ConversationPhrases.NO_MESSAGES,
            new QuestInStateCondition(QUEST_SLOT, "start"),
            ConversationStates.ATTENDING,
            "W takim razie poczekam. Daj znać, kiedy uzbierasz cały zapas; sam nie mogę zejść z posterunku.",
            null);
		
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new QuestInStateCondition(QUEST_SLOT, "start"),
            ConversationStates.ATTENDING,
            "Już cię poprosiłem o przyniesienie #jedzenia!",
            null);
		
		npc.add(ConversationStates.ATTENDING,
            Arrays.asList("cloak", "płaszcza"),
            new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),
            ConversationStates.ATTENDING,
            "Wrvil mieszka w Wofol i przygotował dla mnie płaszcz krasnoludzki. Powiedz mu moje imię i zapłać 20 żelaznymi strzałami. Przyjmę tylko jego egzemplarz z wyszytym imieniem Phalk.",
            null);
		
		npc.add(ConversationStates.ATTENDING,
            Arrays.asList("armor", "zbroi"),
            new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),
            ConversationStates.ATTENDING,
            "Mrotho w Ados przygotował dla mnie zbroję krasnoludzką. Powiedz mu moje imię i zapłać 10 sztukami żelaza. Zwykła zbroja nie wystarczy, potrzebuję tej z moim oznaczeniem.",
            null);
		
	}
	
	private void getCloak() {
	final SpeakerNPC npc = npcs.get("Wrvil");
	
		npc.add(ConversationStates.ATTENDING,
            "Phalk",
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 1, "none")),
            ConversationStates.ATTENDING,
            "Płaszcz dla Phalka jest gotowy i ma właściwe oznaczenie. Ureguluj #zapłatę: potrzebuję 20 żelaznych strzał. Przynieś cały komplet, a wydam zamówienie.",
            null);
		
		npc.add(ConversationStates.ATTENDING,
            Arrays.asList("payment", "zapłatę"),
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 1, "none")),
            ConversationStates.QUEST_ITEM_QUESTION,
            "Phalk zamówił mocny płaszcz do pracy pod ziemią, z imieniem wyszytym na metce. Za wydanie potrzebuję 20 żelaznych strzał. Masz cały komplet?",
            null);	
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
            ConversationPhrases.NO_MESSAGES,
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 1, "none")),
            ConversationStates.ATTENDING,
            "Cóż nie mogę dać tobie płaszcza! Najpierw zapłata!",
            null);
		
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseXPAction(2000));
		actions.add(new DropItemAction("strzała żelazna",20));
		actions.add(new ChatAction() {
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				final Item cloak = SingletonRepository.getEntityManager().getItem("płaszcz krasnoludzki");
				cloak.setInfoString("Phalk");
				cloak.setDescription("Oto piękny nowy płaszcz krasnoludzki z imieniem 'Phalk' wyszytym na metce przez Wrvila.");
				// remember the description
				cloak.setPersistent(true);
				cloak.setBoundTo(player.getName());
				player.equipOrPutOnGround(cloak);
			}
		});
		// the extra parts in the quest state are for wrvil and mrotho not to give them cloaks and armor twice
		actions.add(new SetQuestAction(QUEST_SLOT, 1, "cloak"));
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
            ConversationPhrases.YES_MESSAGES,
            new AndCondition(
						new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 1, "none")),
						new PlayerHasItemWithHimCondition("strzała żelazna",20)),
            ConversationStates.ATTENDING,
            "Dobrze, trzymaj.",
            new MultipleActions(actions));
		
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
            ConversationPhrases.YES_MESSAGES,
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 1, "none"),
				new NotCondition(new PlayerHasItemWithHimCondition("strzała żelazna",20))),
            ConversationStates.ATTENDING,
            "Nie masz jeszcze pełnej zapłaty. Przynieś 20 żelaznych strzał, a wydam ci płaszcz oznaczony imieniem Phalka.",
            null);
		
		
		// player got the cloak already but lost it?
		npc.add(ConversationStates.ATTENDING,
            "Phalk",
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 1, "cloak")),
            ConversationStates.QUEST_ITEM_QUESTION,
            "Płaszcz z zamówienia już ci wydałem. Jeśli go zgubiłeś, przygotuję kolejny oznaczony egzemplarz za 10000 monet. Chcesz zapłacić za zastępstwo?",
            null);
		
		final List<ChatAction> actions2 = new LinkedList<ChatAction>();
		actions2.add(new DropItemAction("money",10000));
		actions2.add(new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final Item cloak = SingletonRepository.getEntityManager().getItem("płaszcz krasnoludzki");
					cloak.setInfoString("Phalk");
					cloak.setDescription("Oto nowy płaszcz krasnoludzki z imieniem 'Phalk' wyszytym w metkę Wrvila.");
					// remember the description
					cloak.setPersistent(true);
					cloak.setBoundTo(player.getName());
					player.equipOrPutOnGround(cloak);
				}
			});		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
            ConversationPhrases.YES_MESSAGES,
            new AndCondition(
						new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 1, "cloak")),
						new PlayerHasItemWithHimCondition("money",10000)),
            ConversationStates.ATTENDING,
            "Dobrze oto on.",
            new MultipleActions(actions2));
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
            ConversationPhrases.YES_MESSAGES,
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 1, "cloak"),
				new NotCondition(new PlayerHasItemWithHimCondition("money",10000))),
            ConversationStates.ATTENDING,
            "Nie masz wystarczająco dużo pieniędzy.",
            null);
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
            ConversationPhrases.NO_MESSAGES,
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 1, "cloak")),
            ConversationStates.ATTENDING,
            "Dobrze, ale Phalk akceptuje tylko płaszcz krasnoludzki ode mnie z jego imieniem wyszytym.",
            null);

	}
	
	private void getArmor() {
		final SpeakerNPC npc = npcs.get("Mrotho");
		
		npc.add(ConversationStates.ATTENDING,
            "Phalk",
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 2, "none")),
            ConversationStates.QUEST_ITEM_QUESTION,
            "Zbroję dla Phalka już odłożyłem, jego imię jest na niej wyryte. Do zapłaty pozostało 10 sztuk żelaza. Masz cały komplet i chcesz odebrać zamówienie?",
            null);
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
            Arrays.asList("payment", "zapłatę"),
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 2, "none")),
            ConversationStates.QUEST_ITEM_QUESTION,
            "Zbroja jest gotowa, a imię Phalka wyryłem po wewnętrznej stronie. Do rozliczenia potrzebuję 10 sztuk żelaza. Masz je przy sobie?",
            null);	
		
		// incase player goes on to ask something else, accept payment from attending too.
		npc.add(ConversationStates.ATTENDING,
            Arrays.asList("payment", "zapłatę"),
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 2, "none")),
            ConversationStates.QUEST_ITEM_QUESTION,
            "Zbroja jest gotowa, a imię Phalka wyryłem po wewnętrznej stronie. Do rozliczenia potrzebuję 10 sztuk żelaza. Masz je przy sobie?",
            null);	
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
            ConversationPhrases.NO_MESSAGES,
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 2, "none")),
            ConversationStates.ATTENDING,
            "Ba! Nie dam tobie zbroi bez zapłaty!",
            null);
		
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseXPAction(5000));
		actions.add(new DropItemAction("żelazo",10));
		actions.add(new ChatAction() {
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				final Item armor = SingletonRepository.getEntityManager().getItem("zbroja krasnoludzka");
				armor.setInfoString("Phalk");
				armor.setDescription("Oto błyszcząca zbroja z imieniem 'Phalk' wygrawerowanym w środku.");
				// remember the description
				armor.setPersistent(true);
				armor.setBoundTo(player.getName());
				player.equipOrPutOnGround(armor);
			}
		});
		// the extra parts in the quest state are for wrvil and mrotho not to give them cloaks and armor twice
		actions.add(new SetQuestAction(QUEST_SLOT, 2, "armor"));
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
            ConversationPhrases.YES_MESSAGES,
            new AndCondition(
						new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 2, "none")),
						new PlayerHasItemWithHimCondition("żelazo",10)),
            ConversationStates.ATTENDING,
            "Dobrze, trzymaj.",
            new MultipleActions(actions));
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
            ConversationPhrases.YES_MESSAGES,
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 2, "none"),
				new NotCondition(new PlayerHasItemWithHimCondition("żelazo",10))),
            ConversationStates.ATTENDING,
            "Wojskowa dyscyplina jest poważna. Nie próbuj mnie oszukać.",
            null);
		
		// player got the armor already but lost it?
		npc.add(ConversationStates.ATTENDING,
            "Phalk",
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 2, "armor")),
            ConversationStates.QUEST_ITEM_QUESTION,
            "Zbroję dla Phalka już odebrałeś. Mogę wykonać zastępczy egzemplarz z tym samym oznaczeniem za 10000 monet. Chcesz zapłacić za nową zbroję?",
            null);
		
		final List<ChatAction> actions2 = new LinkedList<ChatAction>();
		actions2.add(new DropItemAction("money",10000));
		actions2.add(new ChatAction() {
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				final Item armor = SingletonRepository.getEntityManager().getItem("zbroja krasnoludzka");
				armor.setInfoString("Phalk");
				armor.setDescription("Oto lśniąca zbroja krasnoludzka z nazwą 'Phalk' wyrytą na niej.");
				// remember the description
				armor.setPersistent(true);
				armor.setBoundTo(player.getName());
				player.equipOrPutOnGround(armor);
			}
		});		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
            ConversationPhrases.YES_MESSAGES,
            new AndCondition(
						new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 2, "armor")),
						new PlayerHasItemWithHimCondition("money",10000)),
            ConversationStates.ATTENDING,
            "Dobrze oto ona.",
            new MultipleActions(actions2));
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
            ConversationPhrases.YES_MESSAGES,
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 2, "armor"),
				new NotCondition(new PlayerHasItemWithHimCondition("money",10000))),
            ConversationStates.ATTENDING,
            "Nie masz wystarczająco dużo pieniędzy.",
            null);
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
            ConversationPhrases.NO_MESSAGES,
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 2, "armor")),
            ConversationStates.ATTENDING,
            "Dobrze, ale Phalk przyjmie zbroję tylko ode mnie ze swoim imieniem wyrytym na niej.",
            null);
		
	}
		
	
	private void receiveClothes() { 
	final SpeakerNPC npc = npcs.get("Phalk");	
	
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseXPAction(50000));
		actions.add(new IncreaseAtkXPAction(20000));
		actions.add(new IncreaseDefXPAction(10000));
		actions.add(new DropInfostringItemAction("zbroja krasnoludzka","Phalk"));
		actions.add(new DropInfostringItemAction("płaszcz krasnoludzki","Phalk"));
		actions.add(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "done", 5.0));	
		actions.add(new EquipItemAction("zbroja phalka", 1, true));
		
		npc.add(ConversationStates.ATTENDING,
            Arrays.asList("clothes", "ubrania"),
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),
				new PlayerHasInfostringItemWithHimCondition("zbroja krasnoludzka","Phalk"),
				new PlayerHasInfostringItemWithHimCondition("płaszcz krasnoludzki","Phalk")),
            ConversationStates.ATTENDING,
            "Tak, to moje oznaczenia - płaszcz i zbroja pasują do zamówienia. Dzięki jedzeniu i nowemu ubraniu mogę dalej pilnować tego miejsca. Przyjmij moją starą zbroję oraz nagrodę za całą pomoc.",
            new games.stendhal.server.entity.npc.action.QuestFeeAction(QUEST_SLOT, new MultipleActions(actions)));

		npc.add(ConversationStates.ATTENDING,
            Arrays.asList("clothes", "ubrania"),
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),
				new NotCondition(
						new AndCondition(
								new PlayerHasInfostringItemWithHimCondition("zbroja krasnoludzka","Phalk"),
								new PlayerHasInfostringItemWithHimCondition("płaszcz krasnoludzki","Phalk")))),
            ConversationStates.ATTENDING,
            "Potrzebuję dwóch konkretnych rzeczy: płaszcza od Wrvila i zbroi krasnoludzkiej od Mrotho. Obie muszą mieć oznaczenie Phalk; zwykły zakup podobnego wyposażenia nie wystarczy. Powiedz tym rzemieślnikom moje imię, a zajmą się zamówieniem.",
            null);
		
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),
            ConversationStates.ATTENDING,
            "Czekam na ciebie aż przyniesiesz mi nowe #ubrania od Wrvila i Mrotho.",
            null);
	}
	
	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Zapasy dla Phalka",
				"Phalk strzeże przejścia w kopalniach Semos i nie może sam ruszyć po zapasy. Odbierz wskazane przesyłki od jego dostawców i zanieś je na posterunek.",
				false);
		askForFood();
		receiveFood();
		getCloak();
		getArmor();
		receiveClothes();
	}
	
	@Override
	public List<String> getHistory(final Player player) {
        return games.stendhal.server.core.rp.quest.CollectionQuestProgress.history(this, player);
    }
	
	@Override
	public String getName() {
		return "SuppliesForPhalk";
	}
	
	@Override
	public int getMinLevel() {
		return 30;
	}
	@Override
	public String getNPCName() {
		return "Phalk";
	}
		
	@Override
	public String getRegion() {
		return Region.SEMOS_MINES;
	}



    public String getCollectionKind(){return "special";}
    public java.util.Map<String,Integer> getCollectionItems(){return java.util.Collections.emptyMap();}
    public String getCollectionHint(){return "Najpierw przynieś Phalkowi po trzy kanapki, soki z chmielu i napoje z winogron. Później odbierz płaszcz od Wrvila w Wofol za 20 żelaznych strzał oraz zbroję od Mrotho w Ados za 10 żelaza. Phalk uzna wyłącznie egzemplarze oznaczone swoim imieniem.";}
    
}
