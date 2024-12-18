/* $Id: SkinsForGeranesSummer2024.java,v 1.21 2024/06/29 11:13:16 davvids Exp $ */
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


public class SkinsForGeranesSummer2024 extends AbstractQuest {

	public static final String QUEST_SLOT = "skins_for_geranes_2024";

	/**
	 * required items for the quest.
	 */
	protected static final String NEEDED_ITEMS = "skóra balroga 2024=1;skóra śmierci 2024=1;skóra smocza 2024=1";

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Geranes poprosił mnie o pokonanie kilku potworów i zdarciu z nich skóry.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę, pomóc Geranesowi.			Myślę, że znajdzie kogoś innego..");
		} else if (!"done".equals(questState)) {
			final ItemCollection missingItems = new ItemCollection();
			missingItems.addFromQuestStateString(questState);
			res.add("Wciąż muszę przynieść Geranesowi " + Grammar.enumerateCollection(missingItems.toStringList()) + ".");
		} else {
			res.add("Pomogłem Geranesowi.	W nagrodę za ukończenie zadania otrzymałem: 100 000 pkt doświadczenia, 20 000 pkt ataku, 20 000 pkt obrony, 50 pkt karmy i klucz do piekła.");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Geranes");

		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
					new LevelGreaterThanCondition(2),
					new QuestNotStartedCondition(QUEST_SLOT),
					new NotCondition(new QuestInStateCondition(QUEST_SLOT,"rejected"))),
			ConversationStates.QUESTION_1, 
			"Stój, do ciebie mówię! Wyglądasz na wojownika, czy mógłbyś mi w czymś pomóc?", null);

		npc.add(ConversationStates.ATTENDING, 
			ConversationPhrases.QUEST_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT,"rejected"),
			ConversationStates.QUEST_OFFERED, 
			"Hej, chcesz mi jakoś pomóc?", null);

		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Świetnie, kolekcjonuje naróżniejsze skóry. Od dawna mam na oku kilka bardzo rzadkich egzemplarzy, ale nie da się ich nigdzie kupić. Chce, abyś pokonał kilka potworów i przyniósł mi #skóry",
			null);

		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.NO_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Jestem Geranes. Kolekcjonuje naróżniejsze #skóry. Poszukuje wojownika który pokona kilka potworów i zedrze z nich skóre.",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("skora", "skóry", "skory"),
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Szukam kogoś kto pokona kilka potworów i przyniesie mi ich skóry. Czy chciałbyś mi pomóc?",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			null,
			new MultipleActions(new SetQuestAndModifyKarmaAction(QUEST_SLOT, NEEDED_ITEMS, 5.0),
								new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Świetnie! Wybierz się na wyprawe i przynieś mi: [items].")));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Ech... Cóż, nie to nie.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));


		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("skóra balroga", "skóra balroga 2024"),
			null,
			ConversationStates.ATTENDING,
			"Skóre balroga pozyskasz pokonując Balroga i zdzierając z niego skóre. Szansa na pozyskanie skóry z balroga wynosi 25%",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("skóra śmierci", "skóra śmierci 2024"),
			null,
			ConversationStates.ATTENDING,
			"Skóre śmierci pozyskasz pokonując silne śmierci i zdzierając z nich skóre. Szanse na pozyskanie skóry ze złotej śmierci, srebrnej śmierci oraz czarnej śmierci wynosi 10%",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("smocza skóra", "smocza skóra 2024"),
			null,
			ConversationStates.ATTENDING,
			"Smoczą skórę pozyskasz pokonując latające smoki i zdzierając z nich skórę. Szanse na pozyskanie smoczej skóry wynoszą 10% z latającego złotego smoka oraz 40% z latającego czarnego smoka.",
			null);

	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Geranes");
	
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Witaj ponownie. Pamietasz, że miałeś mi przynieść #skóry?",
				null);

		/* player asks what exactly is missing (says ingredients) */
		npc.add(ConversationStates.ATTENDING, "skóry", null,
				ConversationStates.QUESTION_2, null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Potrzebuję [items]. Czy masz coś ze sobą?"));

		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_2, 
			null, new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Potrzebuję [items]. Czy masz to?"));		

		/* player says he has a required item with him (says yes) */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.QUESTION_2, "Świetnie, dziękuje! Co jeszcze masz?",
				null);

		ChatAction completeAction = new  MultipleActions(
				new SetQuestAction(QUEST_SLOT, "done"),
				new SayTextAction("Wspaniale! Moja kolekcja jest teraz największa w naszej krainie. W ramach rekompensaty przyjmij ten klucz, pozwoli Ci na zwiedzenie sekretnego pokoju w piekle."),
				new IncreaseXPAction(100000),
				new IncreaseAtkXPAction(20000),
				new IncreaseDefXPAction(20000),
				new IncreaseKarmaAction(50),
				new EquipItemAction("klucz do piekła 2024", 1)
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
				"Dobrze, powinieneś szukać konkretnych potworów i zedrzeć z nich skórę.", 
				null);

		/* player says he didn't bring any items to different question */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Dobrze, powinieneś szukać konkretnych potworów i zedrzeć z nich skórę.", null);

    /* says quest and quest can't be started nor is active*/
		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
				null,
			    ConversationStates.ATTENDING, 
			    "Nic już nie potrzebuje, dziękuje.",
			    null);
	}
	
	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Skóry dla Geranes",
				"Geranes poszukuje kogoś kto pokona silne potwory i zedrze z ich skóre. Może powinienem mu pomóc?",
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
		return "SkinsForGeranesSummer2024";
	}

	public String getTitle() {
		
		return "Skins For Geranes";
	}
	
	@Override
	public int getMinLevel() {
		return 3;
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Geranes";
	}
}
