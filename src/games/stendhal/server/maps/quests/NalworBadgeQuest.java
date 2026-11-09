/* $Id: UltimateCollector.java 2020/11/27 16:14:11 davvids $ */
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
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropRecordedItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.StartRecordingRandomItemCollectionAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasRecordedItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NalworBadgeQuest extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeCollectionDetails {

	//this quest slot
	private static final String QUEST_SLOT = "nalwor_badge";

	private static final String FIRST_QUEST_SLOT = "grafindle_gold";
	private static final String SECOND_QUEST_SLOT = "swords_for_nnyddion";
	private static final String THIRD_QUEST_SLOT = "kill_elfes";
	
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getHistory(final Player player) {
        return games.stendhal.server.core.rp.quest.CollectionQuestProgress.history(this, player);
    }
	
	private void checkCollectingQuests() {
		final SpeakerNPC npc = npcs.get("Urzędnik Nalwor");
		
		
		npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
			new QuestNotStartedCondition(QUEST_SLOT)),
            ConversationStates.ATTENDING,
            "Witaj w urzędzie Nalwor. Tytuł zasłużonego mieszkańca nadajemy za pomoc ludziom z naszego miasta. Mogę sprawdzić twoje zasługi i wyjaśnić, czego jeszcze potrzeba.",
            null);
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING, "dostawa",
            new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
			new QuestNotStartedCondition(QUEST_SLOT)),
            ConversationStates.ATTENDING,
            "Witaj w urzędzie Nalwor. Tytuł zasłużonego mieszkańca nadajemy za pomoc ludziom z naszego miasta. Mogę sprawdzić twoje zasługi i wyjaśnić, czego jeszcze potrzeba.",
            null);
		
		
			
			
					
		npc.add(ConversationStates.ATTENDING,
            Arrays.asList("mieszkańcom", "zadanie", "zasłużonu", "task"),
            new AndCondition(new QuestNotStartedCondition(QUEST_SLOT), new games.stendhal.server.entity.npc.condition.OrCondition(new QuestNotCompletedCondition(FIRST_QUEST_SLOT), new QuestNotCompletedCondition(SECOND_QUEST_SLOT), new QuestNotCompletedCondition(THIRD_QUEST_SLOT))),
            ConversationStates.ATTENDING,
            null,
            new games.stendhal.server.entity.npc.ChatAction(){public void fire(Player player, games.stendhal.common.parser.Sentence sentence, games.stendhal.server.entity.npc.EventRaiser raiser){raiser.say(games.stendhal.server.core.rp.quest.CollectionQuestProgress.progressText(NalworBadgeQuest.this,player));}});
			
	}

	private void requestItem() {
		
		final SpeakerNPC npc = npcs.get("Urzędnik Nalwor");
		final Map<String,Integer> items = getCollectionItems();
		npc.add(ConversationStates.ATTENDING,
            Arrays.asList("mieszkańcom", "zadanie", "zasłużonu", "task"),
            new AndCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestCompletedCondition(SECOND_QUEST_SLOT),
						new QuestCompletedCondition(FIRST_QUEST_SLOT),
						new QuestCompletedCondition(THIRD_QUEST_SLOT)),
            ConversationStates.ATTENDING,
            null,
            new StartRecordingRandomItemCollectionAction(QUEST_SLOT, items, "Trzy wymagane sprawy zostały zakończone. Dopełnij jeszcze dostawy dla Nalwor: [item]. Gdy wrócisz z całym kompletem, będę mógł przyznać ci tytuł zasłużonego mieszkańca."));
	}
						
	private void collectItem() {
			final SpeakerNPC npc = npcs.get("Urzędnik Nalwor");
			
					npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
            ConversationStates.QUEST_ITEM_QUESTION,
            "Twoje wcześniejsze zasługi są już uznane. Pozostała dostawa, którą wpisałem do dokumentów. Masz przy sobie cały zamówiony komplet?",
            null);
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING, "dostawa",
            new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestActiveCondition(QUEST_SLOT)),
            ConversationStates.QUEST_ITEM_QUESTION,
            "Twoje wcześniejsze zasługi są już uznane. Pozostała dostawa, którą wpisałem do dokumentów. Masz przy sobie cały zamówiony komplet?",
            null);
				
			npc.add(ConversationStates.QUEST_ITEM_QUESTION,
            ConversationPhrases.YES_MESSAGES,
            new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								new NotCondition(new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT))),
            ConversationStates.ATTENDING,
            null,
            new SayRequiredItemAction(QUEST_SLOT, "W dokumentach widnieje jeszcze dostawa: [item]. Wróć z całym kompletem; twoje ukończone sprawy zachowują ważność."));

					npc.add(ConversationStates.QUEST_ITEM_QUESTION,
            ConversationPhrases.YES_MESSAGES,
            new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT)),
            ConversationStates.ATTENDING,
            "Dostawa przyjęta, a twoja pomoc mieszkańcom Nalwor została oficjalnie uznana. Od tej chwili możesz kupować u mnie zwoje do miasta. Przyjmij również nagrodę za zakończone starania.",
            new games.stendhal.server.entity.npc.action.QuestFeeAction(QUEST_SLOT, new MultipleActions(new DropRecordedItemAction(QUEST_SLOT), 
									new SetQuestAction(QUEST_SLOT, "done"),
									new IncreaseXPAction(80000),
									new IncreaseAtkXPAction(3500),
									new IncreaseDefXPAction(4000),
									new EquipItemAction("zwój nalwor", 10, true),
									new IncreaseKarmaAction(10))));
									
				npc.add(ConversationStates.QUEST_ITEM_QUESTION,
            ConversationPhrases.NO_MESSAGES,
            null,
            ConversationStates.ATTENDING,
            null,
            new SayRequiredItemAction(QUEST_SLOT, "W dokumentach widnieje jeszcze dostawa: [item]. Wróć z całym kompletem; twoje ukończone sprawy zachowują ważność."));
	}			

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Mieszkaniec Nalwor",
				"Pomóż mieszkańcom Nalwor, aby zasłużyć na oficjalne uznanie miasta. Urzędnik sprawdzi twoje zasługi, a przed zakończeniem wyjaśni wymaganą dostawę i opłatę.",
				true);
		
		checkCollectingQuests();
		requestItem();
		collectItem();

	}	
	
		@Override
	public String getName() {
		return "NalworBadgeQuest";
	}
	
	@Override
	public int getMinLevel() {
		return 60;
	}

	@Override
	public String getNPCName() {
		return "Urzędnik Nalwor";
	}
	
	@Override
	public String getRegion() {
		return Region.NALWOR_CITY;
	}



    public String getCollectionKind(){return "badge";}
    public java.util.Map<String,Integer> getCollectionItems(){final Map<String,Integer> items = new HashMap<String, Integer>();
		
		
		
		items.put("zbroja elficka",1);
		items.put("spodnie elfickie",1);
		items.put("buty elfickie",1);
		items.put("tarcza elficka",1);
		items.put("hełm elficki",1);
		items.put("płaszcz elficki",1);
		
		return items;}
    public String getCollectionHint(){return "Tytuł zasłużonego mieszkańca wymaga ukończenia trzech miejscowych spraw. Następnie urzędnik poprosi o jedną konkretną dostawę. Po jej przekazaniu otworzy dla ciebie zakup zwojów do miasta.";}
    public java.util.Map<String,String> getCollectionPrerequisites(){java.util.Map<String,String> m=new java.util.LinkedHashMap<String,String>();m.put(FIRST_QUEST_SLOT,"Dostarcz złoto Grafindlowi");m.put(SECOND_QUEST_SLOT,"Dostarcz miecze Nnyddion");m.put(THIRD_QUEST_SLOT,"Pomóż Lorithien z rabusiami");return m;}

}
