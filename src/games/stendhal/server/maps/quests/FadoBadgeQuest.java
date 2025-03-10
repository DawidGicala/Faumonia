/* $Id: UltimateCollector.java 2020/11/27 16:04:11 davvids $ */
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

public class FadoBadgeQuest extends AbstractQuest {

	//this quest slot
	private static final String QUEST_SLOT = "fado_badge";

	private static final String FIRST_QUEST_SLOT = "kill_fairy";
	private static final String SECOND_QUEST_SLOT = "icecream_for_annie";
	private static final String THIRD_QUEST_SLOT = "learn_karma";
	
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
		res.add("Zdecydowałem się pomóc mieszkańcom Fado.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chcę pomóc mieszkańcom Fado");
			return res;
		}
		res.add("Podobno Sarzina, Annie Jone i bankier Yance potrzebują pomocy. Powinienem z nimi porozmawiać.");
		if (!isCompleted(player)) {
			res.add("Żeby ukończyć zadanie musze przynieść " + Grammar.a_noun(player.getRequiredItemName(QUEST_SLOT,0)) + ".");
		}
		if (isCompleted(player)) {
			res.add("Zostałem zasłużonym mieszkańcem Fado i od teraz moge kupować zwoje.");
		}
		return res;
	}
	
	private void checkCollectingQuests() {
		final SpeakerNPC npc = npcs.get("Urzędnik Fado");
		
		
		npc.add(
		ConversationStates.IDLE,
		ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
			new QuestNotStartedCondition(QUEST_SLOT)),
		ConversationStates.ATTENDING,
		"Witaj. Jestem urzędnikiem Fado. Zajmuje się sprawami urzędowymi i sprzedaje zwoje do miasta zasłużonym mieszkańcom.",
		null);
		
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("mieszkańcom", "zadanie", "zasłużonu", "task"),
			new AndCondition(new QuestNotCompletedCondition(FIRST_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Jeśli chcesz zostać zasłużonym mieszkańcem Fado to musisz pomóc bankierowi Yance.",
			null);
			
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("mieszkańcom", "zadanie", "zasłużonu", "task"),
			new AndCondition(new QuestNotCompletedCondition(SECOND_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Jeśli chcesz zostać zasłużonym mieszkańcem Fado to musisz pomóc małej dziewczynce Annie Jone na placu zabaw.",
			null);	
					
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("mieszkańcom", "zadanie", "zasłużonu", "task"),
			new AndCondition(new QuestNotCompletedCondition(THIRD_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Jeśli chcesz zostać zasłużonym mieszkańcem Fado to musisz porozmawiac z Sarzine i dowiedzieć się czym jest karma.",
			null);
			
	}

	private void requestItem() {
		
		final SpeakerNPC npc = npcs.get("Urzędnik Fado");
		final Map<String,Integer> items = new HashMap<String, Integer>();
		
		
		
		items.put("czterolistna koniczyna",1);
		items.put("polano",15);
		items.put("szpinak",28);
		items.put("kalafior",14);
		
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("mieszkańcom", "zadanie", "zasłużonu", "task"),
				new AndCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestCompletedCondition(SECOND_QUEST_SLOT),
						new QuestCompletedCondition(FIRST_QUEST_SLOT),
						new QuestCompletedCondition(THIRD_QUEST_SLOT)),
					ConversationStates.ATTENDING,
					null,
					new StartRecordingRandomItemCollectionAction(QUEST_SLOT, items, "Pomogłeś już wielu mieszkańcom Fado. Aby ostatecznie udowodnić mi, że jesteś oddany Fado,"
						+ " proszę przynieś [item]."));
	}
						
	private void collectItem() {
			final SpeakerNPC npc = npcs.get("Urzędnik Fado");
			
					npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.QUEST_ITEM_QUESTION, 
				"Przyniosłeś przedmioty, o które cię prosiłem?",
				null);
				
			npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES, 
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								new NotCondition(new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT))),
				ConversationStates.ATTENDING, 
				null,
				new SayRequiredItemAction(QUEST_SLOT, "Nie masz przy sobie [item]."));

					npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES, 
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING, 
				"Dziękuje Ci w imieniu mieszkańców Fado. Oficjalnie honoruje cię zasłużonym mieszkańcem Fado. Od teraz możesz kupić u mnie zwoje do miasta.",
				new MultipleActions(new DropRecordedItemAction(QUEST_SLOT), 
									new SetQuestAction(QUEST_SLOT, "done"),
									new IncreaseXPAction(40000),
									new IncreaseAtkXPAction(2500),
									new IncreaseDefXPAction(3000),
									new EquipItemAction("zwój fado", 20, true),
									new IncreaseKarmaAction(10)));
									
				npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING, 
				null,
				new SayRequiredItemAction(QUEST_SLOT, "Wróć kiedy będziesz mieć [item] ze sobą."));
	}			

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Mieszkaniec Fado",
				"Urzędnik Fado uhonoruje cie jako zasłużonego mieszkańca jeśli pomożesz kilku tutejszym mieszkańcom.",
				true);
		
		checkCollectingQuests();
		requestItem();
		collectItem();

	}	
	
		@Override
	public String getName() {
		return "FadoBadgeQuest";
	}
	
	@Override
	public int getMinLevel() {
		return 25;
	}

	@Override
	public String getNPCName() {
		return "Urzędnik Fado";
	}
	
	@Override
	public String getRegion() {
		return Region.FADO_CITY;
	}
}
		
	        	