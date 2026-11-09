/* $Id: HerbsForCarmen.java,v 1.21 2011/11/13 17:13:16 kymara edited by szyg Exp $ */
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


public class DeadWife extends AbstractQuest {

	public static final String QUEST_SLOT = "dead_wife";

	/**
	 * required items for the quest.
	 */
	protected static final String NEEDED_ITEMS = "krwawa suknia=1;krwawa obrączka=1";

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Adalbert z Kirdneh prosi o odzyskanie pamiątek po żonie: krwawej sukni i krwawej obrączki.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie podejmuję się jeszcze szukania pamiątek. Mogę wrócić do Adalberta.");
		} else if (!"done".equals(questState)) {
			final ItemCollection missingItems = new ItemCollection();
			missingItems.addFromQuestStateString(questState);
			res.add("Do odzyskania pozostało: " + Grammar.enumerateCollection(missingItems.toStringList()) + ".");
		} else {
			res.add("Oddałem obie pamiątki. Adalbert przekazał mi krwawy pierścień oraz 25000 XP postaci, 5000 XP ataku, 5000 XP obrony i 10 karmy.");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Adalbert");

		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
					new LevelGreaterThanCondition(2),
					new QuestNotStartedCondition(QUEST_SLOT),
					new NotCondition(new QuestInStateCondition(QUEST_SLOT,"rejected"))),
			ConversationStates.QUESTION_1, 
			"Przyszedłeś zapytać, czy potrzebuję pomocy? Nie chodzi o zwykłe zamówienie. Chciałbym odzyskać pamiątki po mojej żonie.", null);

		npc.add(ConversationStates.ATTENDING, 
			ConversationPhrases.QUEST_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT,"rejected"),
			ConversationStates.QUEST_OFFERED, 
			"Niełatwo prosić obcego o tak osobistą przysługę. Chodzi o rzeczy mojej zmarłej żony, jedyne pamiątki, jakie mogą po niej pozostać. Zechcesz mnie wysłuchać?", null);

		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Dziękuję, że chcesz mnie wysłuchać. Moja żona nie wróciła z ostatniej podróży i trudno mi pogodzić się z tym, że nie zostało po niej nic osobistego. Jeśli możesz mi #pomóc, opowiem o jej rzeczach.",
			null);

		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.NO_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Nazywam się Adalbert. Moja żona zginęła podczas wyprawy w góry Semos, a ja nie mam nawet drobiazgu, który mógłbym po niej zachować. Jeśli możesz mi #pomóc, opowiem, czego szukam.",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("żona", "pomóc"),
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Moja żona wyruszyła w góry Semos i nie wróciła po spotkaniu z duchami. Szukam jej krwawej sukni i krwawej obrączki; nie mają dla mnie ceny, są po prostu jej. Spróbujesz odnaleźć te dwie pamiątki?",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			null,
			new MultipleActions(new SetQuestAndModifyKarmaAction(QUEST_SLOT, NEEDED_ITEMS, 5.0),
								new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Dziękuję, że chcesz spróbować. Brakuje mi jeszcze: [items]. Możesz przynieść każdą pamiątkę osobno; nie zapomnę, co już zostało odzyskane.")));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"...",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Adalbert");
	
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Witaj ponownie. Każdy powrót z gór daje mi odrobinę nadziei. Czy udało się odnaleźć coś, o co #prosiłem?",
				null);

		/* player asks what exactly is missing (says ingredients) */
		npc.add(ConversationStates.ATTENDING, "prosiłem", new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_2, null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Potrzebuję [items]. Czy masz może coś z tych rzeczy ze sobą?"));

		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_2, 
			null, new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Potrzebuję [items]. Czy masz to?"));		

		/* player says he has a required item with him (says yes) */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.QUESTION_2, "Dziękuję! Masz coś jeszcze?",
				null);

		ChatAction completeAction = new  MultipleActions(
				new SetQuestAction(QUEST_SLOT, "done"),
				new SayTextAction("To jej rzeczy. Po tylu dniach czekania wreszcie mogę coś po niej zachować. Przyjmij krwawy pierścień po moim ojcu; chcę w ten sposób podziękować ci za pomoc."),
				new IncreaseXPAction(25000),
				new IncreaseAtkXPAction(5000),
				new IncreaseDefXPAction(5000),
				new IncreaseKarmaAction(10),
				new EquipItemAction("krwawy pierścień", 1)
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
			if (itemName.equals("krwawa suknia")) {
				sl.add("krwawa suknia");
			}

			npc.add(ConversationStates.QUESTION_2, sl, null,
					ConversationStates.QUESTION_2, null,
					new CollectRequestedItemsAction(
							itemName, QUEST_SLOT,
							("krwawa suknia".equals(itemName) ? "Poznaję tę suknię. Dziękuję, że udało ci się ją odnaleźć. Masz też obrączkę?" : "To jej obrączka. Zachowam ją przy sobie. Udało ci się odnaleźć także suknię?"),
                            "Tę pamiątkę już mi przyniosłeś. Jest u mnie, nie musisz szukać drugiej.",
							completeAction, ConversationStates.ATTENDING));
		}

		/* player says he didn't bring any items (says no) */
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Rozumiem. Jeśli odnajdziesz którąś z pamiątek, przynieś ją do mnie.", 
				null);

		/* player says he didn't bring any items to different question */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Poczekam. Nie musisz przynosić obu pamiątek naraz.", null);

    /* says quest and quest can't be started nor is active*/
		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
				null,
			    ConversationStates.ATTENDING, 
			    "Dziękuję za pomoc!",
			    null);
	}
	
	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Przedmioty dla Adalberta",
				"Adalbert z Kirdneh stracił żonę podczas jej wyprawy w góry Semos. Pomóż mu odzyskać dwie pamiątki: krwawą suknię i krwawą obrączkę.",
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
		return "DeadWife";
	}

	public String getTitle() {
		
		return "Dead Wife";
	}
	
	@Override
	public int getMinLevel() {
		return 20;
	}
	
	@Override
	public String getRegion() {
		return Region.KIRDNEH;
	}

	@Override
	public String getNPCName() {
		return "Adalbert";
	}

    public static java.util.Map<String,Integer> getRequiredItems(){
        games.stendhal.server.util.ItemCollection original=new games.stendhal.server.util.ItemCollection();
        original.addFromQuestStateString(NEEDED_ITEMS);
        java.util.Map<String,Integer> items=new java.util.LinkedHashMap<String,Integer>();
        for(java.util.Map.Entry<String,Integer> e:original.entrySet())items.put(e.getKey(),e.getValue());
        return java.util.Collections.unmodifiableMap(items);
    }

}
