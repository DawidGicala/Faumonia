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

import games.stendhal.server.entity.npc.action.DialogueRewardAction;

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

    public static java.util.Map<String,Integer> getRequiredItems() {
        games.stendhal.server.util.ItemCollection items=new games.stendhal.server.util.ItemCollection();
        items.addFromQuestStateString(NEEDED_ITEMS);
        java.util.Map<String,Integer> result=new java.util.LinkedHashMap<String,Integer>();
        for(java.util.Map.Entry<String,Integer> e:items.entrySet()) result.put(e.getKey(),e.getValue());
        return java.util.Collections.unmodifiableMap(result);
    }


	public static final String QUEST_SLOT = "cheese_for_soro";

	/**
	 * required items for the quest.
	 */
	protected static final String NEEDED_ITEMS = "ser=3";

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Soro poprosił mnie o 3 kawałki sera. Mogę znaleźć je przy pokonanych szczurach w Semos i przynosić partiami.");
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
					new QuestNotStartedCondition(QUEST_SLOT),
					new NotCondition(new QuestInStateCondition(QUEST_SLOT,"rejected"))),
			ConversationStates.QUESTION_1, 
			"Nie odmówię dobrego sera, lecz moje zapasy znowu się skończyły. Szukam kogoś, kto pomoże je uzupełnić. Chcesz usłyszeć szczegóły?", null);

		npc.add(ConversationStates.ATTENDING, 
			ConversationPhrases.QUEST_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT,"rejected"),
			ConversationStates.QUEST_OFFERED, 
			"Chcesz jednak pomóc? Nadal potrzebuję trzech kawałków sera. Przyniesiesz mi je?", null);

		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Potrzebuję trzech kawałków sera, najlepiej zanim znów dopadnie mnie głód. W zamian podzielę się ekwipunkiem, który może jeszcze komuś posłużyć. Porozmawiajmy o #serze.",
			null);

		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.NO_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Jestem Soro, pochodzę z #Ados. W Semos znalazłem trochę spokoju po latach podróży. Jeśli zmienisz zdanie w sprawie sera, wróć do rozmowy.",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("ser", "cheese", "żółty"),
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Szczury kręcące się po Semos potrafią podkradać ser. Pokonaj kilka i sprawdź, co po nich zostało; odkładaj zdobycz, aż uzbierasz trzy sztuki. Przyniesiesz mi taki zapas?",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			null,
			new MultipleActions(DialogueRewardAction.karma(new SetQuestAndModifyKarmaAction(QUEST_SLOT, NEEDED_ITEMS, 5.0)),
								new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Jak miło! Proszę przynieś mi: [items].")));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Ech... Cóż, niedobrze... Taka twoja wola.",
			DialogueRewardAction.karma(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0)));

		npc.add(
			ConversationStates.ATTENDING,
			"ser",
			null,
			ConversationStates.ATTENDING,
			"Ser możesz znaleźć przy pokonanych szczurach w Semos. Jest też pożywieniem, więc oddziel swoją porcję od trzech sztuk obiecanych mnie. Przyjmę dostawę również partiami.",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			"semos",
			null,
			ConversationStates.ATTENDING,
			"Jesteśmy w Semos. Wielu przybyszów zaczyna tutaj swoje podróże, a mieszkańcy mają dla nich drobne zlecenia. Warto poznać sąsiadów, zanim ruszysz dalej.",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("ados", "stolica"),
			null,
			ConversationStates.ATTENDING,
			"Ados leży na wschód od Semos. To duże miasto z rynkiem i wieloma rzemieślnikami. Na dłuższą wyprawę przygotuj zapasy i sprawny ekwipunek.",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			"ekwipunek",
			null,
			ConversationStates.ATTENDING,
			"Broń i pancerz to podstawa, ale zostaw też miejsce na jedzenie i eliksiry. O lekarstwa zapytaj Carmen w Semos. Pokażę ci arandulę - to jedno z ziół, których potrzebuje.",
			new ExamineChatAction("arandula.png", "Soro's drawing", "Arandula"));

	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Soro");
	
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Witaj ponownie. Sprawdźmy, czy udało się zdobyć trochę sera. Przyjmę to, co masz, i przypomnę, ile jeszcze brakuje.",
				null);

		/* player asks what exactly is missing (says ingredients) */
		npc.add(ConversationStates.ATTENDING, "tak", new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_2, null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Brakuje mi jeszcze: [items]. Jeśli masz ser, wybierz oddanie z odpowiedzi poniżej albo napisz #ser."));

		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_2, 
			null, new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Brakuje mi jeszcze: [items]. Jeśli masz ser, wybierz oddanie z odpowiedzi poniżej albo napisz #ser."));		

		/* player says he has a required item with him (says yes) */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.QUESTION_2, "Możesz mi go teraz przekazać. Wybierz oddanie sera albo napisz #ser.",
				null);

		ChatAction completeAction = new  MultipleActions(
				new SetQuestAction(QUEST_SLOT, "done"),
				new SayTextAction("Taki zapas sera od razu poprawia mi humor. Weź mój dawny ekwipunek oraz statuetkę aniołka - Furgo w banku Semos może ją od ciebie odkupić. Niech ten podarunek ułatwi ci pierwsze wyprawy."),
				DialogueRewardAction.xp(1000),
				DialogueRewardAction.attackXP(500),
				DialogueRewardAction.defenseXP(500),
				DialogueRewardAction.karma(new IncreaseKarmaAction(10)),
				DialogueRewardAction.item("statuetka aniołka", 1),
				DialogueRewardAction.item("kiścień", 1),
				DialogueRewardAction.item("mieczyk", 1),
				DialogueRewardAction.item("skórzany hełm", 1),
				DialogueRewardAction.item("koszula", 1),
				DialogueRewardAction.item("skórzane spodnie", 1),
				DialogueRewardAction.item("buty skórzane", 1),
				DialogueRewardAction.item("peleryna", 1),
				DialogueRewardAction.item("puklerz", 1)
				);

        // A migrated delivery may already cover the new total. Claim explicitly, once.
        npc.add(ConversationStates.ANY, "odbiór", new QuestInStateCondition(QUEST_SLOT, "ser=0"),
                ConversationStates.ATTENDING, null, completeAction);
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
			    null, (player, sentence, raiser) -> raiser.say(player.isQuestCompleted("help_sedro")
                    ? "Z serem już mi pomogłeś, a Sedro też otrzymał pomoc. Zajrzyj do przewodnika, jeśli chcesz sprawdzić, co zostało przed wyjściem z Semos. Uważaj na siebie!"
                    : "Z serem już mi pomogłeś, dziękuję. Odwiedź Sedro przed domkiem startowym i zapytaj go o potwory. Ekwipunek, który ci dałem, przyda się podczas polowania."));
	}
	
	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Ser dla Soro",
				"Soro potrzebuje 3 kawałków sera. Możesz go zdobyć ze szczurów w mieście Semos.",
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
