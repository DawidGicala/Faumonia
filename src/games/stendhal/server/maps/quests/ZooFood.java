/* $Id: ZooFood.java,v 1.51 2012/04/19 18:26:42 kymara Exp $ */
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

import games.stendhal.common.MathHelper;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropRecordedItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemAction;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartRecordingRandomItemCollectionAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasRecordedItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * QUEST: Zoo Food
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li> Katinka, the keeper at the Ados Wildlife Refuge
 * <li> Dr.Feelgood, the veterinary
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Katinka asks you for food for the animals.
 * <li> You get the food, e.g. by killing other animals ;) or harvesting it
 * <li> You give the food to Katinka.
 * <li> Katinka thanks you.
 * <li> You can then buy cheap medicine from Dr. Feelgood.
 * </ul>
 * 
 * REWARD: <ul>
 * <li> 200 XP 
 * <li> 7 Karma 
 * <li> Supply for cheap medicine and free pet healing for one week
 * </ul>
 * REPETITIONS: - Once per week.
 */
public class ZooFood extends AbstractQuest {

	private static final int REQUIRED_HAM = 10;
	private static final int DELAY = MathHelper.MINUTES_IN_ONE_WEEK;

	private static final String QUEST_SLOT = "zoo_food";

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
		res.add("Spotkałem Katinkę w zoo");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie mam czasu na zwierzęta i ich problemy z jedzeniem");
			return res;
		}
		res.add("Nie chcę widzieć jak te biedne zwierzęta umierają! Pomogę im w zdobyciu jedzenia!");
		if (questState.startsWith("start;")) {
			String questItem = player.getRequiredItemName(QUEST_SLOT,1);
			int amount = player.getRequiredItemQuantity(QUEST_SLOT,1);
			if (!player.isEquipped(questItem, amount)) {
				res.add(String.format("Zostałem poproszony, aby przynieść " +Grammar.quantityplnoun(amount, questItem, "a") + " dla zwierząt."));
			} else {
				res.add(String.format("Mam " +Grammar.quantityplnoun(amount, questItem, "a") + " dla zwierząt, muszę to zanieść do schroniska w Ados."));
			}
		}
		if (isCompleted(player)) {
			if(new TimePassedCondition(QUEST_SLOT, 1, DELAY).fire(player, null, null)) {
				res.add("Zwierzęta znów są głodne! Muszę zapytać Katinki czego potrzebują.");
			} else {
				res.add("Zwierzęta nie są głodne!");
			}
		}
		return res;
	}

	public static Map<String,Integer> getRequiredItems() {
final Map<String,Integer> items = new HashMap<String, Integer>();		
		items.put("szynka",10); 
		items.put("mięso",15);
		items.put("zboże",20);
		items.put("sałata",10);
		items.put("pieczarka",8);
		items.put("marchew",10);
		items.put("szpinak",7);
		items.put("jabłko",10);
		items.put("płotka",3);
return java.util.Collections.unmodifiableMap(items);
}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Katinka");
        npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
            new QuestStateStartsWithCondition(QUEST_SLOT,"start;"), ConversationStates.ATTENDING,null,
            new SayRequiredItemAction(QUEST_SLOT,1,"Nasza umowa nadal obowiązuje. Potrzebuję [item] w jednej dostawie. Gdy zbierzesz całość, porozmawiamy o przekazaniu karmy."));


        // Player has never done the zoo quest
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Witaj w schronisku w Ados. Każde z naszych zwierząt je co innego, a zapasy wciąż topnieją. Jeśli możesz pomóc z jedną dostawą, zapytaj o #zadanie.",
				null
		);

        // Same authoritative step, without requiring the player to leave and greet again.
        npc.add(ConversationStates.ATTENDING, "karma dla zwierząt",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Witaj w schronisku w Ados. Każde z naszych zwierząt je co innego, a zapasy wciąż topnieją. Jeśli możesz pomóc z jedną dostawą, zapytaj o #zadanie.",
				null
		);

        // Player returns within one week of completing quest
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT), 
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY))),
                ConversationStates.ATTENDING, "Witam ponownie w schronisku dla dzikich zwierząt w Ados! Jeszcze raz dziękuję za uratowanie naszych zwierząt!",
				null
		);

        // Same authoritative step, without requiring the player to leave and greet again.
        npc.add(ConversationStates.ATTENDING, "karma dla zwierząt",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestCompletedCondition(QUEST_SLOT), 
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY))),
                ConversationStates.ATTENDING, "Witam ponownie w schronisku dla dzikich zwierząt w Ados! Jeszcze raz dziękuję za uratowanie naszych zwierząt!",
				null
		);

        // Player returns and longer than a week has passed, ask to help again
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT), 
						new TimePassedCondition(QUEST_SLOT, 1, DELAY)),
				ConversationStates.QUEST_OFFERED, "Witam ponownie w schronisku dla dzikich zwierząt w Ados. "
                + " Nasze zwierzęta są znowu głodne, pomożesz ponownie?",
                null);

        // Same authoritative step, without requiring the player to leave and greet again.
        npc.add(ConversationStates.ATTENDING, "karma dla zwierząt",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestCompletedCondition(QUEST_SLOT), 
						new TimePassedCondition(QUEST_SLOT, 1, DELAY)),
				ConversationStates.QUEST_OFFERED, "Witam ponownie w schronisku dla dzikich zwierząt w Ados. "
                + " Nasze zwierzęta są znowu głodne, pomożesz ponownie?",
                null);


        // Player has never done the zoo quest, player asks what the task was
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new games.stendhal.server.entity.npc.condition.OrCondition(new QuestNotStartedCondition(QUEST_SLOT), new QuestInStateCondition(QUEST_SLOT, "rejected")), 
				ConversationStates.QUEST_OFFERED, "Nasze zwierzęta sę głodne. Potrzebujemy więcej " +
						"jedzenia do ich wykarmienia. Pomożesz nam?",
				null);

	    final Map<String,Integer> items = getRequiredItems();
		
        // Player has done quest before and agrees to help again
		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING, null,
                new MultipleActions(DialogueRewardAction.karma(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start;", 2.0)),
				new StartRecordingRandomItemCollectionAction(QUEST_SLOT, 1, items, "Dziękuję! Proszę" 
                + " przynieś [item] w jednej dostawie. Zachowam dla ciebie to zamówienie, dopóki go nie zrealizujesz."))
		);


		// player is not willing to help
		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING, "Och... Chyba będziemy musieli nakarmić je jeleniami...",
				DialogueRewardAction.karma(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0))
		);
		
        // Player returns within one week of completing quest
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestCompletedCondition(QUEST_SLOT), 
                                 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY))),
				ConversationStates.ATTENDING, null, 
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY,  "Dziękuję, ale nie mamy teraz problemów."));

	}

	private void step_2() {
		// Just find the food somewhere. It isn't a quest
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Katinka");

		// compatibility with old quests:
		// player returns while initial quest is still active, set it to match the new way
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start")),
				ConversationStates.QUEST_ITEM_BROUGHT,
				"Witaj ponownie! Czy przyniosłeś nam "
						+ Grammar.quantityplnoun(REQUIRED_HAM, "szynka") + "?",
			new SetQuestAction(QUEST_SLOT,"start;szynka=10"));

        // Same authoritative step, without requiring the player to leave and greet again.
        npc.add(ConversationStates.ATTENDING, "karma dla zwierząt",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestInStateCondition(QUEST_SLOT, "start")),
				ConversationStates.QUEST_ITEM_BROUGHT,
				"Witaj ponownie! Czy przyniosłeś nam "
						+ Grammar.quantityplnoun(REQUIRED_HAM, "szynka") + "?",
			new SetQuestAction(QUEST_SLOT,"start;szynka=10"));

		// player returns while quest is active
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "start;")),
				ConversationStates.QUEST_ITEM_BROUGHT,
				null,
				new SayRequiredItemAction(QUEST_SLOT, 1, "Witaj ponownie! Czy przyniosłeś nam [item]?"));

        // Same authoritative step, without requiring the player to leave and greet again.
        npc.add(ConversationStates.ATTENDING, "karma dla zwierząt",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestStateStartsWithCondition(QUEST_SLOT, "start;")),
				ConversationStates.QUEST_ITEM_BROUGHT,
				null,
				new SayRequiredItemAction(QUEST_SLOT, 1, "Witaj ponownie! Czy przyniosłeś nam [item]?"));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new DropRecordedItemAction(QUEST_SLOT,1));
		actions.add(DialogueRewardAction.karma(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "done;1", 5.0)));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		actions.add(DialogueRewardAction.xp(2000));
		actions.add(DialogueRewardAction.attackXP(1000));
		actions.add(DialogueRewardAction.defenseXP(1000));
        actions.add(DialogueRewardAction.access("Opieka i sklep Dr. Feelgooda przez tydzień"));
	
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES, 
			new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,1),
			ConversationStates.ATTENDING, "Dziękuję, rozdzielę dostawę między zwierzęta i uzupełnię zapasy. Przez najbliższy tydzień Dr. Feelgood będzie mógł leczyć twoje zwierzęta i udostępni ci swoją ofertę. Przyjmij także nagrodę za tę pomoc.",
			new MultipleActions(actions));

        npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
        		ConversationPhrases.YES_MESSAGES,
        		new NotCondition(new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,1)),
        		ConversationStates.ATTENDING, null,
        		new SayRequiredItemAction(QUEST_SLOT, 1, "W tej dostawie potrzeba: [item]. Przynieś całą zamówioną ilość, bo muszę od razu rozdzielić karmę między zwierzęta. Zachowaj to, co już udało ci się zebrać.")
        		);

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING, "Zwierzęta czekają na obiecaną dostawę. Przynieś całą zamówioną ilość, abym mógł rozdzielić zapasy między podopiecznych.",
				null);
	}

	private void step_4() {
		final SpeakerNPC npc = npcs.get("Dr. Feelgood");

		// player returns while quest is still active
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT), 
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY))),
			ConversationStates.ATTENDING, "Dzięki twojej dostawie mam chwilę, żeby zająć się także innymi pacjentami. Przez tydzień po pomocy Katince możesz korzystać z mojej oferty i leczenia zwierząt. Powiedz, czego potrzebujesz.",
				null
		);

        // Same authoritative step, without requiring the player to leave and greet again.
        npc.add(ConversationStates.ATTENDING, "karma dla zwierząt",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestCompletedCondition(QUEST_SLOT), 
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY))),
			ConversationStates.ATTENDING, "Dzięki twojej dostawie mam chwilę, żeby zająć się także innymi pacjentami. Przez tydzień po pomocy Katince możesz korzystać z mojej oferty i leczenia zwierząt. Powiedz, czego potrzebujesz.",
				null
		);

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new OrCondition(new QuestNotCompletedCondition(QUEST_SLOT),
								new AndCondition(new QuestCompletedCondition(QUEST_SLOT), new TimePassedCondition(QUEST_SLOT, 1, DELAY))
						)),
				ConversationStates.IDLE, "Przepraszam, ale nie mogę teraz rozmawiać. Wszystkie zwierzęta są chore ponieważ nie mają jedzenia. Nie chciałbyś się rozejrzeć?",
				null
		);

        // Same authoritative step, without requiring the player to leave and greet again.
        npc.add(ConversationStates.ATTENDING, "karma dla zwierząt",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new OrCondition(new QuestNotCompletedCondition(QUEST_SLOT),
								new AndCondition(new QuestCompletedCondition(QUEST_SLOT), new TimePassedCondition(QUEST_SLOT, 1, DELAY))
						)),
				ConversationStates.IDLE, "Przepraszam, ale nie mogę teraz rozmawiać. Wszystkie zwierzęta są chore ponieważ nie mają jedzenia. Nie chciałbyś się rozejrzeć?",
				null
		);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Jedzenie dla Zoo",
				"Katinka opiekuje się zwierzętami w schronisku pod Ados. Przyjmij zamówienie na karmę i przynieś wskazane jedzenie, aby jej podopiecznym nie zabrakło zapasów.",
				true);
		step_1();
		step_2();
		step_3();
		step_4();
	}

	@Override
	public String getName() {
		return "ZooFood";
	}
	
	@Override
	public boolean isRepeatable(final Player player) {
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,DELAY)).fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Katinka";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_SURROUNDS;
	}
}
