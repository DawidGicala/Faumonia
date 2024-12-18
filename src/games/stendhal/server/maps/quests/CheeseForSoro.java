/* $Id: CheeseForSoro.java,v 1.21 2020/11/01 16:02:16 davvids Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2020 - Stendhal                    *
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

public class CheeseForSoro extends AbstractQuest {

	public static final String QUEST_SLOT = "cheese_for_soro";

	/**
	 * required items for the quest.
	 */
	protected static final String NEEDED_ITEMS = "ser=10";

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Soro poprosił mnie o zebranie 10 kawałków sera. Moge go zdobyć ze szczurów.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę pomóc Soro. Myślę, że znajdzie on kogoś innego do pomocy.");
		} else if (!"done".equals(questState)) {
			final ItemCollection missingItems = new ItemCollection();
			missingItems.addFromQuestStateString(questState);
			res.add("Wciąż muszę przynieść Soro " + Grammar.enumerateCollection(missingItems.toStringList()) + ".");
		} else {
			res.add("Pomogłem Soro, w nagrode dał mi swój stary ekwipunek i statuetke aniołka, podobno moge ja sprzedać za duże pieniadze w banku Semos. Chyba powinienem porozmawiać z tamtejszymi bywalcami.	W nagrodę za ukończenie zadania otrzymałem: 1.000 pkt doświadczenia, 500 pkt ataku, 500 pkt obrony, 10 pkt karmy, statuetkę aniołka, kiścień, mieczyk, skórzany hełm, koszulę, skórzane spodnie, buty skórzane, pelerynę i puklerz");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Soro");

		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
					new LevelGreaterThanCondition(4),
					new QuestNotStartedCondition(QUEST_SLOT),
					new NotCondition(new QuestInStateCondition(QUEST_SLOT,"rejected"))),
			ConversationStates.QUESTION_1, 
			"Hej ty! Tak, do ciebie mówię! Mógłbyś mi pomóc? Napisz #tak lub #nie", null);

		npc.add(ConversationStates.ATTENDING, 
			ConversationPhrases.QUEST_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT,"rejected"),
			ConversationStates.QUEST_OFFERED, 
			"No cóż, może innym razem..", null);

		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Świetnie, potrzebuje 10 kawałków sera. Jeśli zgadzasz się mi pomóc napisz: #ser",
			null);

		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.NO_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Jestem Soro. Pochodze z #Ados.",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("ser", "cheese", "żółty"),
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Ser możesz zdobyć ze szczurów pałętających się po naszym mieście. Leczy trochę życia, możesz też go sprzedać innym graczom za pare monet w banku Semos. Przyniesiesz mi 10 kawałków takiego sera? Jeśli się zgadzasz napisz: #tak",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			null,
			new MultipleActions(new SetQuestAndModifyKarmaAction(QUEST_SLOT, NEEDED_ITEMS, 5.0),
								new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Jak miło! Proszę przynieś mi: [items].")));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Ech... Cóż, niedobrze... Taka twoja wola.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(
			ConversationStates.ATTENDING,
			"ser",
			null,
			ConversationStates.ATTENDING,
			"Ser po zjedzeniu uleczy troche twojego zdrowia. Możesz go zdobyć ze szczurów pałetajacych sie po mieście.",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			"semos",
			null,
			ConversationStates.ATTENDING,
			"Semos to miasto w którym aktualnie sie znajdujemy. Jest jednym z miast Faumonii.",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("ados", "stolica"),
			null,
			ConversationStates.ATTENDING,
			"Idac ciagle na zachod trafisz do ogromnego miasta - Ados. Jest ono stolica całej Faumonii",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			"ekwipunek",
			null,
			ConversationStates.ATTENDING,
			"Ekwipunek przyda sie do ochrony przed potworami znajdujacymi sie w tej krainie. W grze przydaja sie rownież eliksiry, jeden z nich możesz zrobić w mieście przy pomocy tej rosliny - aranduli",
			new ExamineChatAction("arandula.png", "Soro's drawing", "Arandula"));

	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Soro");
	
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Witaj ponownie. Czy przyniosłeś mi ser?",
				null);

		/* player asks what exactly is missing (says ingredients) */
		npc.add(ConversationStates.ATTENDING, "tak", null,
				ConversationStates.QUESTION_2, null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Potrzebuję [items]. Czy masz ze sobą?"));

		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_2, 
			null, new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Potrzebuję [items]. Czy masz to?"));		

		/* player says he has a required item with him (says yes) */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.QUESTION_2, "Dobrze, a #co #dokładnie masz?",
				null);

		ChatAction completeAction = new  MultipleActions(
				new SetQuestAction(QUEST_SLOT, "done"),
				new SayTextAction("Wspaniale! Kocham ser. Trzymaj tę statuetkę aniołka, możesz ją sprzedać za trochę pieniędzy w banku Semos u Furgo. Bank Semos znajdziesz koło biblioteki u góry. Weź również mój stary ekwipunek, dzieki niemu łatwiej bedzie ci wytrwać w tej przeklętej krainie..."),
				new IncreaseXPAction(1000),
				new IncreaseAtkXPAction(500),
				new IncreaseDefXPAction(500),
				new IncreaseKarmaAction(10),
				new EquipItemAction("statuetka aniołka", 1),
				new EquipItemAction("kiścień", 1),
				new EquipItemAction("mieczyk", 1),
				new EquipItemAction("skórzany hełm", 1),
				new EquipItemAction("koszula", 1),
				new EquipItemAction("skórzane spodnie", 1),
				new EquipItemAction("buty skórzane", 1),
				new EquipItemAction("peleryna", 1),
				new EquipItemAction("puklerz", 1)
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
				"Ech.. szkoda...", 
				null);

		/* player says he didn't bring any items to different question */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Ech.. szkoda....", null);

    /* says quest and quest can't be started nor is active*/
		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
				null,
			    ConversationStates.ATTENDING, 
			    "Wróć do mnie gdy osiągniesz 5 poziom doświadczenia, wybierz się na przykład na mapę pod miastem lub do podziemi w centrum. Na tą chwile dziękuje.",
			    null);
	}
	
	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Ser dla Soro",
				"Soro potrzebuje 10 kawałków sera. Możesz go zdobyć ze szczurów w mieście Semos.",
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
		return "CheeseForSoro";
	}

	public String getTitle() {
		
		return "Cheese for Soro";
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
		return "Soro";
	}
}
