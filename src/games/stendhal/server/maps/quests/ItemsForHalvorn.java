/* $Id: ItemsForHalvorn.java,v 1.21 2024/12/04 19:13:16 davvids Exp $ */
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
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.CollectRequestedItemsAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.ExamineChatAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemsFromCollectionAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.ItemCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * QUEST: Herbs For Halvorn
 * 
 * PARTICIPANTS:
 * <ul>
 * <li>Halvorn (the healer in Semos)</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li>Halvorn introduces herself and asks for some items to help her heal people.</li>
 * <li>You collect the items.</li>
 * <li>Halvorn sees yours items, asks for them then thanks you.</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li>50 XP</li>
 * <li>2 antidote</li>
 * <li>Karma: 10</li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 */
public class ItemsForHalvorn extends AbstractQuest {

	public static final String QUEST_SLOT = "items_for_halvorn";

	/**
	 * required items for the quest.
	 */
	protected static final String NEEDED_ITEMS = "lodowe igloo 2024=800";

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Halvorn poprosił mnie o zebranie lodowych igloo, aby przełamać magiczną barierę i otworzyć drogę do Smoka Wawelskiego.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chciałem pomóc Halvornowi. Myślę, że znajdzie kogoś innego, kto podejmie się tego zadania.");
		} else if (!"done".equals(questState)) {
			final ItemCollection missingItems = new ItemCollection();
			missingItems.addFromQuestStateString(questState);
			res.add("Wciąż muszę przynieść Halvorn " + Grammar.enumerateCollection(missingItems.toStringList()) + ".");
		} else {
			res.add("Pomogłem Halvornowi zebrać lodowe igloo. Dzięki temu udało mu się stworzyć Smoczą Pieczęć, która otwiera drogę do Smoka Wawelskiego. W nagrodę za ukończenie zadania otrzymałem: 10 000 pkt doświadczenia, 500 pkt ataku, 500 pkt obrony, 10 pkt karmy i Smoczą Pieczęć.");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Halvorn");

		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
					new LevelGreaterThanCondition(2),
					new QuestNotStartedCondition(QUEST_SLOT),
					new NotCondition(new QuestInStateCondition(QUEST_SLOT,"rejected"))),
			ConversationStates.QUESTION_1, 
			"Hej, podróżniku. Widziałeś, ile śniegu spadło na Semos? Czy chciałbyś mi pomóc?", null);

		npc.add(ConversationStates.ATTENDING, 
			ConversationPhrases.QUEST_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT,"rejected"),
			ConversationStates.QUEST_OFFERED, 
			"Wróciłeś? Czy jednak chcesz mi pomóc?", null);

		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Świetnie, słuchaj uważnie. Potrzebuję lodowych #igloo. Pingwiny w okolicy roznoszą je, a ja muszę zebrać aż 800.",
  
			null);


		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("igloo", "pingwiny", "lodowe"),
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Lodowe igloo, które niosą pingwiny, to klucz do przełamania mroźnych barier. Przyniesiesz mi 800 takich igloo? Postaram się otworzyć drogę do Smoka Wawelskiego.",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			null,
			new MultipleActions(new SetQuestAndModifyKarmaAction(QUEST_SLOT, NEEDED_ITEMS, 5.0),
								new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Dobrze, potrzebuję [items] Znajdziesz je u pingwinów.")));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"To rozczarowujące... Ale cóż, taka jest twoja wola. Pamiętaj jednak, że bez twojej pomocy Smok Wawelski może pozostać zamknięty na zawsze.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Halvorn");
	
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Witaj ponownie. Pamiętasz, że miałeś mi przynieść #lodowe #igloo? Wciąż ich potrzebuje, by otworzyć drogę do Smoka Wawelskiego.",
				null);

		/* player asks what exactly is missing (says ingredients) */
		npc.add(ConversationStates.ATTENDING, "lodowe igloo", null,
				ConversationStates.QUESTION_2, null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Potrzebuję [items]. Czy masz coś ze sobą?"));

		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_2, 
			null, new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Potrzebuję [items]. Czy masz je przy sobie?"));		

		/* player says he has a required item with him (says yes) */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.QUESTION_2, "Dobrze, a co jeszcze masz?",
				null);

		ChatAction completeAction = new  MultipleActions(
				new SetQuestAction(QUEST_SLOT, "done"),
				new SayTextAction("Wspaniale! Właśnie tego potrzebowałem. Czekaj… Jeszcze chwila… Majstruję nad czymś wyjątkowym. Te lodowe igloo kryją w sobie pradawną magię, która po odpowiednim uformowaniu może posłużyć jako klucz. #Halvorn #wyciąga #dziwaczne #narzędzia #i #zaczyna #ostrożnie #łączyć #lodowe #fragmenty. #Śnieżny #pył #unosi #się #w #powietrzu, #a #mroźny #wiatr #zdaje #się #cichnąć. Gotowe! Udało mi się stworzyć Smoczą Pieczęć. To przedmiot o niezwykłej mocy, stworzony z łuski pradawnego smoka i otoczony aurą gorącego powietrza. Pieczęć ta pozwoli ci przejść przez magiczne bariery na szczycie Lodowej Góry Ados. Tylko ci, którzy udowodnią swoją wartość w walce, będą mogli zmierzyć się z legendarnym Smokiem Wawelskim. Niech twoje czyny przejdą do historii, wędrowcze. Smok Wawelski czeka, dodatkowo przez tą przeklętą zimę zdaje się być dużo słabszy..."),
				new IncreaseXPAction(10000),
				new IncreaseAtkXPAction(500),
				new IncreaseDefXPAction(500),
				new IncreaseKarmaAction(10),
				new EquipItemAction("smocza pieczęć 2024", 1)
				);

		/* add triggers for the item names */
		final ItemCollection items = new ItemCollection();
		items.addFromQuestStateString(NEEDED_ITEMS);
		for (final Map.Entry<String, Integer> entry : items.entrySet()) {
			String itemName = entry.getKey();

			String singular = Grammar.singular(itemName);
			List<String> sl = new ArrayList<String>();
			sl.add(itemName);

			// handle the porcino/porcini singular/plural case with item name "borowik"
			if (!singular.equals(itemName)) {
				sl.add(singular);
			}
			// also allow to understand the misspelled "porcinis"
			if (itemName.equals("borowik")) {
				sl.add("borowik");
			}

			npc.add(ConversationStates.QUESTION_2, sl, null,
					ConversationStates.QUESTION_2, null,
					new CollectRequestedItemsAction(
							itemName, QUEST_SLOT,
							"Dobra, masz coś jeszcze?"," " +
							Grammar.quantityplnoun(entry.getValue(), itemName) + " już przyniosłeś dla mnie, ale dziękuję i tak.",
							completeAction, ConversationStates.ATTENDING));
		}

		/* player says he didn't bring any items (says no) */
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Rozumiem, wędrowcze. Pingwiny nie są łatwymi przeciwnikami. #Halvorn #delikatnie #się #uśmiechnął. Wróć do mnie, gdy zdobędziesz lodowe igloo.", 
				null);

		/* player says he didn't bring any items to different question */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"W porządku. Czekam na ciebie – bariera do Smoka Wawelskiego nie przełamie się sama.", null);

    /* says quest and quest can't be started nor is active*/
		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
				null,
			    ConversationStates.ATTENDING, 
			    "W tej chwili niczego nie potrzebuję. Wróć później, może znajdzie się coś do zrobienia.",
			    null);
	}
	
	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Igloo dla Halvorna",
				"Halvorn, tajemniczy wędrowiec z Semos, potrzebuje lodowych igloo, by przełamać magiczne bariery i otworzyć drogę do Smoka Wawelskiego. Może powinienem mu pomóc zebrać te niezwykłe przedmioty?",
				true);
		prepareRequestingStep();
		prepareBringingStep();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "ItemsForHalvorn";
	}

	public String getTitle() {
		
		return "Items for Halvorn";
	}
	
	@Override
	public int getMinLevel() {
		return 1;
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Halvorn";
	}
}
