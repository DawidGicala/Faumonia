/* $Id: DragonCave.java,v 1.21 2011/11/13 17:13:16 kymara Exp $ */
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
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
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
 * QUEST: Dragon Cave
 *
 * author: davvids
 */
public class DragonCave extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeCollectionDetails {
	
	private static final int REQUIRED_LEVEL = 100;

	public static final String QUEST_SLOT = "dragon_cave";

	/**
	 * required items for the quest.
	 */
	protected static final String NEEDED_ITEMS = "skóra zielonego smoka=6;skóra niebieskiego smoka=4;skóra czerwonego smoka=2;skóra złotego smoka=1;skóra czarnego smoka=1;szmaragd=14;szafir=11;rubin=7;obsydian=1;kieł smoka=12;szmaragdowy płaszcz smoczy=1;lazurowy płaszcz smoczy=1;kościany płaszcz smoczy=1;karmazynowy płaszcz smoczy=1";

	@Override
	public List<String> getHistory(final Player player) {
        return games.stendhal.server.core.rp.quest.CollectionQuestProgress.history(this, player);
    }

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Terry");

		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new AndCondition(
					new LevelGreaterThanCondition(99),
					new QuestNotStartedCondition(QUEST_SLOT),
					new NotCondition(new QuestInStateCondition(QUEST_SLOT,"rejected"))),
            ConversationStates.QUESTION_1,
            "Hej, podróżniku! Opiekuję się małymi smokami i zbieram rzeczy związane z ich większymi krewniakami. Pomożesz mi uzupełnić tę kolekcję?",
            null);

		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new AndCondition(new QuestInStateCondition(QUEST_SLOT,"rejected"), new LevelGreaterThanCondition(REQUIRED_LEVEL - 1)),
            ConversationStates.QUEST_OFFERED,
            "Wróciła ci ochota na smoczą wyprawę? Nadal szukam brakujących okazów do kolekcji. Jeśli przyjmiesz prośbę, zapiszę dokładną listę.",
            null);

		npc.add(ConversationStates.QUESTION_1,
            ConversationPhrases.YES_MESSAGES,
            new QuestNotStartedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Smoki fascynują mnie nie tylko podczas walki. Chcę porównać ich skóry, kamienie i wykonane z nich płaszcze. Zapytaj o #przedmiotów, a pokażę ci pełną listę do przyjęcia.",
            null);

		npc.add(ConversationStates.QUESTION_1,
            ConversationPhrases.NO_MESSAGES,
            new QuestNotStartedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Jestem Terry i opiekuję się małymi smokami. Zbieram też okazy po ich większych krewniakach, żeby lepiej poznać różnice między gatunkami. Gdy zechcesz pomóc, zapytaj o #przedmiotów do kolekcji.",
            null);

		npc.add(ConversationStates.ATTENDING,
            Arrays.asList("things", "rzeczy", "przedmiotów"),
            new AndCondition(new QuestNotStartedCondition(QUEST_SLOT), new LevelGreaterThanCondition(REQUIRED_LEVEL - 1)),
            ConversationStates.QUEST_OFFERED,
            "Zbieram skóry różnych smoków, kamienie, kły oraz cztery rodzaje smoczych płaszczy. Każdy okaz przyjmę osobno i odnotuję w kolekcji. Podejmiesz się zebrania całej listy?",
            null);

		npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.YES_MESSAGES,
            new AndCondition(new QuestNotStartedCondition(QUEST_SLOT), new LevelGreaterThanCondition(REQUIRED_LEVEL - 1)),
            ConversationStates.ATTENDING,
            null,
            new MultipleActions(new SetQuestAndModifyKarmaAction(QUEST_SLOT, NEEDED_ITEMS, 5.0),
								new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Cudownie! Proszę przynieś mi te rzeczy: [items].")));

		npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.NO_MESSAGES,
            new games.stendhal.server.entity.npc.condition.QuestNotStartedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Ech... Cóż, niedobrze... Poszukam kogoś innego.",
            new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(ConversationStates.ATTENDING,
            "szmaragd",
            null,
            ConversationStates.ATTENDING,
            "Szmaragdy to bardzo cenne kryształy, spróbuj ich poszukać przy zielonych smokach.",
            null);

		npc.add(ConversationStates.ATTENDING,
            "szafir",
            null,
            ConversationStates.ATTENDING,
            "Szafiry to przepiękne kryształy, spróbuj poszukać ich przy błękitnych smokach.",
            null);

		npc.add(ConversationStates.ATTENDING,
            Arrays.asList("skóra zielonego smoka", "skóra niebieskiego smoka"),
            null,
            ConversationStates.ATTENDING,
            "Szukaj skór w łupach po odpowiednich smokach. Liczy się dokładny gatunek zapisany na liście, więc skóra jednego koloru nie zastąpi drugiej. Zdobyte okazy możesz przynosić stopniowo.",
            null);

		npc.add(ConversationStates.ATTENDING,
            "kościany płaszcz smoczy",
            null,
            ConversationStates.ATTENDING,
            "Kościany płaszcz smoczy jest rzadkim okazem, którego nie mam jeszcze w zbiorze. Nie podam ci pewnego miejsca zdobycia, bo sam takiego nie znam. Zachowaj go dla mnie, jeśli trafisz na ten konkretny egzemplarz.",
            null);

	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Terry");
	
		npc.add(ConversationStates.ATTENDING,
            Arrays.asList("rzeczy", "przedmioty", "przedmiotów", "things", "zadanie", "quest", "task"),
            new AndCondition(
					new QuestNotStartedCondition(QUEST_SLOT), 
					new LevelLessThanCondition(REQUIRED_LEVEL)),
            ConversationStates.ATTENDING,
            "Smocze okazy wymagają wyprawy, do której trzeba się dobrze przygotować. Wróć po osiągnięciu 100 poziomu, a powierzę ci zebranie kolekcji.",
            null);
			
		npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
            ConversationStates.ATTENDING,
            "Witaj ponownie. Przyjdź do mnie jak będziesz miał wszystkie #rzeczy przy sobie.",
            null);

				
		/* player asks what exactly is missing (says ingredients) */
		npc.add(ConversationStates.ATTENDING,
            "rzeczy",
            new QuestActiveCondition(QUEST_SLOT),
            ConversationStates.QUESTION_2,
            null,
            new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Potrzebuję [items]. Czy masz coś ze sobą?"));

		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new QuestActiveCondition(QUEST_SLOT),
            ConversationStates.QUESTION_2,
            null,
            new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Potrzebuję [items]. Czy masz to?"));		

		/* player says he has a required item with him (says yes) */
		npc.add(ConversationStates.QUESTION_2,
            ConversationPhrases.YES_MESSAGES,
            null,
            ConversationStates.QUESTION_2,
            "Dobrze, co masz ze sobą?",
            null);

		ChatAction completeAction = new  MultipleActions(
				new SetQuestAction(QUEST_SLOT, "done"),
				new SayTextAction("Wreszcie mam komplet i mogę porównać wszystkie smocze okazy. Przyjmij obiecany smoczy zwój; pozwoli ci dotrzeć do krainy pełnej smoków. Korzystaj z niego dopiero wtedy, gdy będziesz gotów na taką wyprawę."),
				new IncreaseXPAction(200000),
				new IncreaseAtkXPAction(10000),
				new IncreaseDefXPAction(20000),
				new IncreaseKarmaAction(20),
				new EquipItemAction("smoczy zwój", 1)
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

			npc.add(ConversationStates.QUESTION_2,
            sl,
            null,
            ConversationStates.QUESTION_2,
            null,
            new CollectRequestedItemsAction(
							itemName, QUEST_SLOT,
							"Dobra, masz coś jeszcze?"," " +
							Grammar.quantityplnoun(entry.getValue(), itemName) + " już przyniosłeś dla mnie, ale dziękuję i tak.",
							completeAction, ConversationStates.ATTENDING));
		}

		/* player says he didn't bring any items (says no) */
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.NO_MESSAGES,
            new QuestActiveCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Dobrze i daj mi znać jeśli mogę ci kiedyś #pomóc..",
            null);

		/* player says he didn't bring any items to different question */
		npc.add(ConversationStates.QUESTION_2,
            ConversationPhrases.NO_MESSAGES,
            new QuestActiveCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Ok, i daj mi znać jeśli mogę #pomóc w czymkolwiek innym.",
            null);

    /* says quest and quest can't be started nor is active*/
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            null,
            ConversationStates.ATTENDING,
            "Nic nie potrzebuję teraz, dziękuję.",
            null);
	}
	
	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Przedmioty do kolekcji Terrego",
				"Terry z jaskiń Semos gromadzi smocze okazy do swojej kolekcji. Zapytaj o brakujące rzeczy i dostarcz je, aby pomóc mu w dalszych obserwacjach.",
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
		return "DragonCave";
	}

	public String getTitle() {
		
		return "Dragon Cave";
	}
	
	@Override
	public int getMinLevel() {
		return 100;
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Terry";
	}



    public String getCollectionKind(){return "collection";}
    public java.util.Map<String,Integer> getCollectionItems(){games.stendhal.server.util.ItemCollection c=new games.stendhal.server.util.ItemCollection();c.addFromQuestStateString(NEEDED_ITEMS);return new java.util.LinkedHashMap<String,Integer>(c);}
    public String getCollectionHint(){return "Terry zbiera smocze skóry, kamienie, kły i cztery rodzaje płaszczy. Przekazuj brakujące rzeczy osobno; Terry odłoży każdy okaz do kolekcji. Za pełną kolekcję otrzymasz smoczy zwój i rozwój umiejętności.";}
    
}
