/* $Id: TaskHayunn.java,v 1.21 2020/11/01 16:02:16 davvids Exp $ */
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

public class TaskHayunn extends AbstractQuest {

    public static java.util.Map<String,Integer> getRequiredItems() {
        games.stendhal.server.util.ItemCollection items=new games.stendhal.server.util.ItemCollection();
        items.addFromQuestStateString(NEEDED_ITEMS);
        java.util.Map<String,Integer> result=new java.util.LinkedHashMap<String,Integer>();
        for(java.util.Map.Entry<String,Integer> e:items.entrySet()) result.put(e.getKey(),e.getValue());
        return java.util.Collections.unmodifiableMap(result);
    }


	public static final String QUEST_SLOT = "task_hayunn";

	/**
	 * required items for the quest.
	 */
	protected static final String NEEDED_ITEMS = "woda=1";

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Hayunn poprosił mnie o jedną wodę. Powinienem przywitać się z Miterem w tym samym domku, wybrać ofertę, kupić 1 wodę za 5 monet i potwierdzić zakup.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę pomóc ukończyć samouczka u Hayunna.");
		} else if (!"done".equals(questState)) {
			final ItemCollection missingItems = new ItemCollection();
			missingItems.addFromQuestStateString(questState);
			res.add("Wciąż muszę przynieść Hayunnowi " + Grammar.enumerateCollection(missingItems.toStringList()) + ".");
		} else {
			res.add("Przyniosłem Hayunnowi butelkę wody i nauczyłem się podstaw rozmowy z NPCami.");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Hayunn");

		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
					new QuestNotStartedCondition(QUEST_SLOT),
					new NotCondition(new QuestInStateCondition(QUEST_SLOT,"rejected"))),
			ConversationStates.QUESTION_1, 
			"Zaschło mi w gardle. Przyniesiesz mi butelkę wody od Mitera? Przy okazji pokażę ci, jak robić zakupy i załatwiać sprawy z mieszkańcami.", null);

		npc.add(ConversationStates.ATTENDING, 
			ConversationPhrases.QUEST_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT,"rejected"),
			ConversationStates.QUEST_OFFERED, 
			"Zaschło mi w gardle. Przyniesiesz mi butelkę wody od Mitera? Przy okazji pokażę ci, jak robić zakupy i załatwiać sprawy z mieszkańcami.", null);

		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Miter stoi w tym samym domku. Podejdź do niego, napisz „cześć” i zapytaj o ofertę. Niebieskie słowa możesz klikać, a odpowiedź wybrać z listy lub wpisać samodzielnie. Powiedz #ok, a przypomnę ci, co kupić.",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("ok", "ser"),
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Kup u Mitera jedną butelkę wody za 5 monet. Zapytaj o #ofertę, wybierz wodę i potwierdź zakup. Przyniesiesz mi ją?",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			null,
			new MultipleActions(DialogueRewardAction.karma(new SetQuestAndModifyKarmaAction(QUEST_SLOT, NEEDED_ITEMS, 5.0)),
								new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Dziękuję! Potrzebuję: [items]. Kup butelkę u Mitera i wróć do mnie.")));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Dobrze, wróć, kiedy będziesz mieć chwilę.",
			DialogueRewardAction.karma(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0)));

	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Hayunn");
	
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Witaj ponownie! Masz już wodę od Mitera? Jeśli nie wiesz, jak ją kupić, chętnie podpowiem.",
				null);

		/* player asks what exactly is missing (says ingredients) */
		npc.add(ConversationStates.ATTENDING, "tak", new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_2, null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Brakuje mi jeszcze: [items]. Jeśli masz wodę, wybierz ją z odpowiedzi poniżej albo napisz #woda."));

		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_2, 
			null, new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Brakuje mi jeszcze: [items]. Jeśli masz wodę, wybierz ją z odpowiedzi poniżej albo napisz #woda."));		

		/* player says he has a required item with him (says yes) */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.QUESTION_2, "Świetnie! Wybierz oddanie butelki z odpowiedzi poniżej albo napisz #woda.",
				null);

		ChatAction completeAction = new  MultipleActions(
				new SetQuestAction(QUEST_SLOT, "done"),
				new SayTextAction("Dziękuję, właśnie tego potrzebowałem. Wiesz już, jak porozmawiać z mieszkańcem, zrobić zakupy i oddać obiecany przedmiot. Teraz odwiedź Mistrza Faumonii w czerwonym domu i przyjmij jego pierwszą próbę; przewodnik wskaże ci drogę."),
				DialogueRewardAction.xp(200),
				DialogueRewardAction.attackXP(50),
				DialogueRewardAction.defenseXP(50),
				DialogueRewardAction.karma(new IncreaseKarmaAction(10))
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
				"Nie ma pośpiechu. Kup wodę u Mitera i wróć, kiedy będziesz ją mieć.", 
				null);

		/* player says he didn't bring any items to different question */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Nie ma pośpiechu. Kup wodę u Mitera i wróć, kiedy będziesz ją mieć.", null);

    /* says quest and quest can't be started nor is active*/
		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
				null,
			    ConversationStates.ATTENDING, 
			    "Wykonałeś już swoje zadanie. Czas wyruszyć na podbój krainy Faumonii!",
			    null);
	}
	
	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Przewodnik Hayunn",
				"Hayunn, mieszkający w domku startowym w Semos nauczy cię podstaw rozmowy z NPCami.",
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
		return "TaskHayunn";
	}

	public String getTitle() {
		
		return "Cheese for Hayunn";
	}
	
	@Override
	public int getMinLevel() {
		return 0;
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Hayunn";
	}
}
