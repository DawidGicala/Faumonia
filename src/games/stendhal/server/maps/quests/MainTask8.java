/* $Id: MainTask8.java 2020/12/02 18:02:31 davvids $ */
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
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainTask8 extends AbstractQuest {

	//this quest slot
	private static final String QUEST_SLOT = "main_task_8";
	
	private static final String FIRST_QUEST_SLOT = "Podroznik_quest";
	private static final String SECOND_QUEST_SLOT = "supplies_for_phalk";
	
	private static final String MAIN1 = "main_task_1";
	private static final String MAIN2 = "main_task_2";
	
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
		res.add("Podjąłem się głownego zadania na wojownika Faumonii");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chce podjąć się głównego zadania na wojownika Faumonii");
			return res;
		}
		res.add("Zanim Mistrz Faumonii VIII przyzna mi kolejne zadanie musze wykonać kilka zadań.	Muszę odnaleźć Tajemniczego Podróżnika i wykonać zadanie na naszyjnik. Podobno znajdę go w jaskini Semos-Ados w pobliżu olbrzymów.	Muszę wybrać się do tuneli prowadzących do podziemnego miasta koboldów - Wofol, odnaleźć krasnala Phalka i wykonać dla niego zadanie.");
		if (!isCompleted(player)) {
			res.add("Żeby ukończyć główne zadanie musze odnaleźć niedźwiedzie siedliska i przynieść " + Grammar.a_noun(player.getRequiredItemName(QUEST_SLOT,0)) + "	Informacji na temat powinienem szukać u graczy lub na stronie internetowej: www.faumonia.pl w zakładce expowiska.");
		}
		if (isCompleted(player)) {
			res.add("Ukończyłem ósme zadanie od Mistrza Faumonii i otrzymałem odznakę wojownika Faumonii ósmego poziomu.	Następne główne zadanie otrzymam po przekroczeniu 60 poziomu doświadczenia.	W międzyczasie mogę nauczyć się gotować zupy rybne które lecza ogromne ilości życia. W tym celu powinienem udać się do stolicy Ados i porozmawiać z tamtejszymi rybakami.");
		}
		return res;
	}
	
	private void checkCollectingQuests() {
		final SpeakerNPC npc = npcs.get("Mistrz Faumonii VIII");
		
		npc.add(
		ConversationStates.IDLE,
		ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new QuestCompletedCondition(MAIN1),
			new QuestCompletedCondition(MAIN2),
			new LevelGreaterThanCondition(39),
			new GreetingMatchesNameCondition(npc.getName()),
			new QuestNotStartedCondition(QUEST_SLOT)),
		ConversationStates.ATTENDING,
		"Witaj, mam dla ciebie główne #zadanie ósmego poziomu.",
		null);
		
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
			new AndCondition(new QuestCompletedCondition(MAIN1),
			new QuestCompletedCondition(MAIN2),
			new LevelGreaterThanCondition(39),
			new QuestNotCompletedCondition(FIRST_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Zanim przyznam ci kolejne główne zadanie, powinieneś w końcu zdobyć jakiś wisiorek. Wybierz się do Tajemniczego Wędrowcy i wykonaj jego zadanie. Znajdziesz go w jaskiniach Semos-Ados w pobliżu olbrzymów. Uważaj! To zadanie wymaga sprytu aby nie zginąć!",
			null);
			
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
			new AndCondition(new QuestCompletedCondition(MAIN1),
			new QuestCompletedCondition(MAIN2),
			new LevelGreaterThanCondition(39),
			new QuestNotCompletedCondition(SECOND_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Zanim przyznam ci kolejne główne zadanie, musisz komuś pomóc. Jesteś już w miarę doświadczonym wojownikiem i znasz krainę Faumonii. Wybierz się do krasnala Phalka którego znajdziesz w tunelach prowadzących do podziemnego miasta koboldów Wofol i wykonaj dla niego zadanie. Pamiętaj, że jeśli nie wiesz jak gdzieś trafić to zawsze możesz odwiedzić naszą stronę internetową: www.faumonia.pl i przeglądnąć atlas.",
			null);	
			
	}

	private void requestItem() {
		
		final SpeakerNPC npc = npcs.get("Mistrz Faumonii VIII");
		final Map<String,Integer> items = new HashMap<String, Integer>();
		
		items.put("zwój kalavan",8);
		items.put("zwój kalavan",6);
		items.put("zwój kalavan",12);
		items.put("zwój kalavan",4);
		
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("główne zadanie", "zadanie", "główne", "task"),
				new AndCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestCompletedCondition(MAIN1),
						new QuestCompletedCondition(MAIN2),
						new LevelGreaterThanCondition(39),
						new QuestCompletedCondition(SECOND_QUEST_SLOT),
						new QuestCompletedCondition(FIRST_QUEST_SLOT)),
					ConversationStates.ATTENDING,
					null,
					new StartRecordingRandomItemCollectionAction(QUEST_SLOT, items, "Wykonałeś już zadania o które cie prosiłem i zdobyłeś naszyjnik oraz nową zbroję"
						+ ". Czas abyś znowu trochę powalczył. Na południu od miasta Fado znajduje się niebezpieczne miasto Kalavan. Idź tam, pokonaj tamtejszych mieszkańców i przynieś mi: [item]."));
	}
						
	private void collectItem() {
			final SpeakerNPC npc = npcs.get("Mistrz Faumonii VIII");
			
					npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.QUEST_ITEM_QUESTION, 
				"Powinieneś iść do miasta Kalavan znajdującego się na południe od miasta Fado i pozyskać zwoje od tamtejszych mieszkańców. Przyniosłeś zwoje o które cię prosiłem?",
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
				"Poznałeś miasto Kalavan, zdobyłeś nową zbroję oraz naszyjnik. Zasługujesz na odznakę wojownika Faumonii ósmego poziomu. Następne główne zadanie otrzymasz po przekroczeniu 60 poziomu doświadczenia. Weź tę 3 zupy rybne. Najlepiej zachowaj je na później, leczą ogromną ilość życia. W wolnej chwili możesz nauczyć się gotować taką zupę rybną, w tym celu powinieneś odnaleźć rybaków w stolicy Ados i wykonać kilka kursów.",
				new MultipleActions(new DropRecordedItemAction(QUEST_SLOT), 
									new SetQuestAction(QUEST_SLOT, "done"),
									new DropItemAction("odznaka wojownika VII",1),
									new EquipItemAction("zupa rybna", 3, true),
									new EquipItemAction("odznaka wojownika VIII", 1, true)));
																
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
				"Wojownik Faumonii 8",
				"Mistrz Faumonii VIII ma dla ciebie ósme główne zadanie które pomoże ci zostać wielkim wojownikiem Faumonii.",
				true);
		
		checkCollectingQuests();
		requestItem();
		collectItem();

	}	
	
		@Override
	public String getName() {
		return "MainTask8";
	}
	
	@Override
	public int getMinLevel() {
		return 40;
	}

	@Override
	public String getNPCName() {
		return "Mistrz Faumonii VIII";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}
}
		
	        	