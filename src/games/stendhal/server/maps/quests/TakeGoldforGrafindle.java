/* $Id: TakeGoldforGrafindle.java,v 1.34 2011/11/13 17:15:18 kymara Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
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

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
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
 * QUEST: Take gold for Grafindle
 * 
 * PARTICIPANTS: <ul>
 * <li> Grafindle
 * <li> Lorithien </ul>
 * 
 * STEPS:<ul>
 * <li> Talk with Grafindle to activate the quest.
 * <li> Talk with Lorithien for the money.
 * <li> Return the gold bars to Grafindle</ul>
 * 
 * REWARD:<ul>
 * <li> 200 XP
 * <li> key to nalwor bank customer room
 * </ul>
 * REPETITIONS: <ul><li> None.</ul>
 */
public class TakeGoldforGrafindle extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeCollectionDetails {
	
	private static final int GOLD_AMOUNT = 25;

	private static final String QUEST_SLOT = "grafindle_gold";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getHistory(final Player player) {
        return games.stendhal.server.core.rp.quest.CollectionQuestProgress.history(this, player);
    }

	private void step_1() {

		final SpeakerNPC npc = npcs.get("Grafindle");

		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new QuestNotCompletedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "W banku liczy się nie tylko pełna sakiewka, lecz także zaufanie. Szukam kogoś, kto bezpiecznie dostarczy #złota od naszej klientki. Jeśli zechcesz pomóc, wyjaśnię szczegóły.",
            null);
		
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new QuestCompletedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Pytam się Ciebie, bo wyglądasz na uczciwego.",
            null);

		/** In case quest is completed */
		npc.add(ConversationStates.ATTENDING,
            Arrays.asList("gold", "złota", "złoto"),
            new QuestCompletedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Złoto w banku jest bezpieczne. Dziękuję!",
            null);

		/** If quest is not started yet, start it. */
		npc.add(ConversationStates.ATTENDING,
            "złota",
            new QuestNotStartedCondition(QUEST_SLOT),
            ConversationStates.QUEST_OFFERED,
            "Lorithien z poczty Nalwor chce zdeponować złoto, ale nie może zostawić swoich obowiązków. Potrzebuję kogoś, kto odbierze przesyłkę i przyniesie ją tutaj w całości. Podejmiesz się tego?",
            null);

		npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.YES_MESSAGES,
            new games.stendhal.server.entity.npc.condition.QuestNotStartedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Dziękuję za przyjęcie tej odpowiedzialności. Odwiedź Lorithien na poczcie w Nalwor, odbierz złoto i wróć prosto do banku. Dopiero dostarczenie przesyłki zamknie tę sprawę.",
            new SetQuestAndModifyKarmaAction(QUEST_SLOT,"start", 5.0));

		npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.NO_MESSAGES,
            new games.stendhal.server.entity.npc.condition.QuestNotStartedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Cóż jesteś uczciwy, ponieważ od razu mi powiedziałeś.",
            new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(ConversationStates.QUEST_OFFERED,
            "Lorithien",
            null,
            ConversationStates.QUEST_OFFERED,
            "Pracuje na poczcie w Nalwor. To duża odpowiedzialność, zwłaszcza z tymi złotymi sztabkami, które są warte dużo pieniędzy. Czy mogę Ci zaufać?",
            null);

		/** Remind player about the quest */
		npc.add(ConversationStates.ATTENDING,
            "złoto",
            new QuestInStateCondition(QUEST_SLOT, "start"),
            ConversationStates.ATTENDING,
            "#Lorithien będzie się martwiła tym, że jej złoto nie jest bezpieczne! Proszę przynieś je!",
            null);

		npc.add(ConversationStates.ATTENDING,
            "lorithien",
            null,
            ConversationStates.ATTENDING,
            "Lorithien prowadzi pocztę w Nalwor. Tam odbierzesz złoto, jeśli przyjmiesz moje zlecenie. Ja dopilnuję jego przyjęcia do banku.",
            null);
	}

	private void step_2() {
		final SpeakerNPC npc = npcs.get("Lorithien");

		/**
		 * If player has quest and is in the correct state, just give him the
		 * gold bars.
		 */
		final List<ChatAction> givegold = new LinkedList<ChatAction>();
		givegold.add(new EquipItemAction("sztabka złota",GOLD_AMOUNT, true));
		givegold.add(new SetQuestAction(QUEST_SLOT, "lorithien"));	
		
		npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start")),
            ConversationStates.ATTENDING,
            "Grafindle uprzedził mnie, że ktoś pomoże z przesyłką. Przekazuję ci przygotowane złoto; zanieś je w całości do banku Nalwor. Będę spokojniejsza, kiedy znajdzie się pod opieką bankiera.",
            new MultipleActions(givegold));
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING,
            "przesyłka",
            new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
					new QuestInStateCondition(QUEST_SLOT, "start")),
            ConversationStates.ATTENDING,
            "Grafindle uprzedził mnie, że ktoś pomoże z przesyłką. Przekazuję ci przygotowane złoto; zanieś je w całości do banku Nalwor. Będę spokojniejsza, kiedy znajdzie się pod opieką bankiera.",
            new MultipleActions(givegold));

		/** If player keep asking for book, just tell him to hurry up */
		npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "lorithien")),
            ConversationStates.ATTENDING,
            "Złoto zostało już wydane do przewiezienia. Zanieś całą przesyłkę do Grafindla w banku Nalwor. Nie mogę przekazywać jej drugi raz.",
            null);
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING,
            "przesyłka",
            new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
					new QuestInStateCondition(QUEST_SLOT, "lorithien")),
            ConversationStates.ATTENDING,
            "Złoto zostało już wydane do przewiezienia. Zanieś całą przesyłkę do Grafindla w banku Nalwor. Nie mogę przekazywać jej drugi raz.",
            null);

		npc.add(ConversationStates.ATTENDING,
            "grafindle",
            null,
            ConversationStates.ATTENDING,
            "Grafindle jest starszym bankierem w Nalwor.",
            null);

		/** Finally if player didn't start the quest, just ignore him/her */
		npc.add(ConversationStates.ATTENDING,
            "złoto",
            new QuestNotStartedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Przepraszam, mam tyle spraw na głowie... Nie zrozumiałem Ciebie.",
            null);
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Grafindle");

		/** Complete the quest */
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("sztabka złota", GOLD_AMOUNT));
		reward.add(new EquipItemAction("klucz do banku Nalwor", 1, true));
		reward.add(new IncreaseXPAction(5000));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(50));
		
		npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "lorithien"),
						new PlayerHasItemWithHimCondition("sztabka złota", GOLD_AMOUNT)),
            ConversationStates.ATTENDING,
            "Złoto dotarło w całości, a przesyłka została przyjęta do banku. Dziękuję za rzetelną pomoc i przekazuję ci klucz do pokoju dla klientów. Tak właśnie zdobywa się zaufanie w Nalwor.",
            new games.stendhal.server.entity.npc.action.QuestFeeAction(QUEST_SLOT, new MultipleActions(reward)));
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING,
            "przesyłka",
            new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestInStateCondition(QUEST_SLOT, "lorithien"),
						new PlayerHasItemWithHimCondition("sztabka złota", GOLD_AMOUNT)),
            ConversationStates.ATTENDING,
            "Złoto dotarło w całości, a przesyłka została przyjęta do banku. Dziękuję za rzetelną pomoc i przekazuję ci klucz do pokoju dla klientów. Tak właśnie zdobywa się zaufanie w Nalwor.",
            new games.stendhal.server.entity.npc.action.QuestFeeAction(QUEST_SLOT, new MultipleActions(reward)));
		
		npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "lorithien"),
						new NotCondition(new PlayerHasItemWithHimCondition("sztabka złota", GOLD_AMOUNT))),
            ConversationStates.ATTENDING,
            "Do zamknięcia przesyłki potrzebuję wszystkich wydanych sztabek. Lorithien przekazała je już do przewiezienia; jeśli część została zgubiona lub wydana, uzupełnij brak przed powrotem. Nie rozliczę niepełnej dostawy.",
            null);
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING,
            "przesyłka",
            new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestInStateCondition(QUEST_SLOT, "lorithien"),
						new NotCondition(new PlayerHasItemWithHimCondition("sztabka złota", GOLD_AMOUNT))),
            ConversationStates.ATTENDING,
            "Do zamknięcia przesyłki potrzebuję wszystkich wydanych sztabek. Lorithien przekazała je już do przewiezienia; jeśli część została zgubiona lub wydana, uzupełnij brak przed powrotem. Nie rozliczę niepełnej dostawy.",
            null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Złoto dla Grafindla",
				"Grafindle z banku Nalwor szuka zaufanej osoby do przewiezienia złota. Wysłuchaj instrukcji i dopilnuj, by przesyłka dotarła do właściwych rąk.",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "TakeGoldforGrafindle";
	}
	
	// it is not easy to get to Nalwor
	@Override
	public int getMinLevel() {
		return 50;
	}
	
	@Override
	public String getRegion() {
		return Region.NALWOR_CITY;
	}

	@Override
	public String getNPCName() {
		return "Grafindle";
	}



    public String getCollectionKind(){return "special";}
    public java.util.Map<String,Integer> getCollectionItems(){return games.stendhal.server.core.rp.quest.JourneyQuestProgress.items("sztabka złota",GOLD_AMOUNT);}
    public String getCollectionHint(){return "Grafindle w banku Nalwor zleca odbiór złota od Lorithien na poczcie. Po odebraniu przesyłki wróć z całym złotem do bankiera. Nagrodą jest klucz do pokoju dla klientów.";}
    
}
