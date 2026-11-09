/* $Id: DailyItemQuest.java,v 1.76 2011/11/13 17:13:16 kymara Exp $ */
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

import games.stendhal.common.MathHelper;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropRecordedItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.IncreaseXPDependentOnLevelAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartRecordingRandomItemCollectionAction;
import games.stendhal.server.entity.npc.action.SayRequiredItemAction;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasRecordedItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * QUEST: Daily Item Fetch Quest.
 * <p>
 * PARTICIPANTS:
 * <li> Mayor of Ados
 * <li> some items
 * <p>
 * STEPS:
 * <li> talk to Mayor of Ados to get a quest to fetch an item
 * <li> bring the item to the mayor
 * <li> if you cannot bring it in one week he offers you the chance to fetch
 * another instead
 * <p>
 * REWARD:
 * <li> xp 
 * <li> 10 Karma
 * <p>
 * REPETITIONS:
 * <li> once a day
 */
public class DailyItemQuest extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeCollectionDetails {

	private static final String QUEST_SLOT = "daily_item";
	
	/** How long until the player can give up and start another quest */
	private static final int expireDelay = MathHelper.MINUTES_IN_ONE_WEEK; 
	
	/** How often the quest may be repeated */
	private static final int delay = MathHelper.MINUTES_IN_ONE_DAY; 
	
	/**
	 * All items which are possible/easy enough to find. If you want to do
	 * it better, go ahead. *
	 * not to use yet, just getting it ready.
	 */
	private static Map<String,Integer> items;

	private static void buildItemsMap() {
		items = new HashMap<String, Integer>();
		items.put("mieczyk",1);
		items.put("sztylecik",1);
		items.put("krótki miecz",1);
		items.put("miecz",1);
		items.put("bułat",1);
		items.put("katana",1);
		items.put("miecz dwuręczny",1);
		items.put("pałasz",1);
		items.put("miecz zaczepny",1);
		items.put("kosa pordzewiała",1);
		items.put("toporek",1);
		items.put("topór jednoręczny",1);
		items.put("topór",1);
		items.put("topór bojowy",1);
		items.put("berdysz",1);
		items.put("kosa",1);
		items.put("topór oburęczny",1);
		items.put("halabarda",1);
		items.put("maczuga",1);
		items.put("kij",1);
		items.put("buzdygan",1);
		items.put("kiścień",1);
		items.put("złoty kiścień",1);
		items.put("pyrlik",1);
		items.put("młot bojowy",1);
		items.put("drewniany łuk",1);
		items.put("długi łuk",1);
		items.put("strzała",1);
		items.put("strzała żelazna",1);
		items.put("puklerz",1);
		items.put("drewniana tarcza",1);
		items.put("tarcza ćwiekowa",1);
		items.put("tarcza płytowa",1);
		items.put("lwia tarcza",1);
		items.put("tarcza jednorożca",1);
		items.put("tarcza z czaszką",1);
		items.put("tarcza królewska",1);
		items.put("koszula",1);
		items.put("skórzana zbroja",1);
		items.put("skórzany kirys",1);
		items.put("zbroja ćwiekowa",1);
		items.put("kolczuga",1);
		items.put("zbroja łuskowa",1);
		items.put("zbroja płytowa",1);
		items.put("skórzany hełm",1);
		items.put("hełm nabijany ćwiekami",1);
		items.put("hełm kolczy",1);
		items.put("skórzane spodnie",1);
		items.put("spodnie nabijane ćwiekami",1);
		items.put("spodnie kolcze",1);
		items.put("buty skórzane",1);
		items.put("buty nabijane ćwiekami",1);
		items.put("peleryna",1);
		items.put("peleryna elficka",1);
		items.put("płaszcz krasnoludzki",1);
		items.put("szmaragdowy płaszcz smoczy",1);
		items.put("ser",10);
		items.put("marchew",10);
		items.put("sałata",10);
		items.put("jabłko",5);
		items.put("chleb",5);
		items.put("mięso",10);
		items.put("szynka",10);
		items.put("kanapka",5);
		items.put("tarta",5);
		items.put("jajo",1);
		items.put("pieczarka",10);
		items.put("borowik",10);
		items.put("muchomor",15);
		items.put("sok z chmielu",10);
		items.put("napój z winogron",10);
		items.put("mały eliksir",5);
		items.put("antidotum",5);
		items.put("mocne antidotum",5);
		items.put("eliksir",5);
		items.put("duży eliksir",5);
		items.put("trucizna",5);
		items.put("flasza",5);
		items.put("money",100);
		items.put("arandula",5);
		items.put("polano",10);
		items.put("zboże",20);
		items.put("mąka",5);
		items.put("ruda żelaza",10);
		items.put("żelazo",5);
		items.put("kości do gry",1);
		items.put("pluszowy miś",1);
		items.put("okoń",5);
		items.put("płotka",5);
		items.put("palia alpejska",5);
		items.put("pstrąg",5);
		items.put("pokolec",5);
		items.put("cebula",5);
		items.put("por",5);
		items.put("błazenek",5);
		items.put("skórzana zbroja łuskowa",1);
		items.put("skórzany kirys z naramiennikami",1);
		items.put("kolczuga wzmocniona",1);
		items.put("żelazna zbroja łuskowa",1);
		items.put("złota kolczuga",1);
		items.put("żelazny kirys z naramiennikami",1);
		items.put("lazurowy płaszcz elficki",1);
		items.put("piernacz",1);
		items.put("złoty buzdygan",1);
		items.put("złoty młot",1);
		items.put("misiurka",1);
		items.put("klejony łuk",1);
		items.put("wzmocniona lwia tarcza",1);
		items.put("szpinak",5);
		items.put("cukinia",5);
		items.put("kapusta",5);
		items.put("węgiel",10);
		items.put("kilof",1);
		items.put("grillowany stek",1);
	}
	
	private ChatAction startQuestAction() {
		// common place to get the start quest actions as we can both starts it and abort and start again
		
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new StartRecordingRandomItemCollectionAction(QUEST_SLOT, 0, items, "Sprawy Ados to także codzienne dostawy dla mieszkańców. W tym zamówieniu potrzebuję [item]. Przynieś wskazaną ilość i powiedz #zakończone, a zapiszę twoją pomoc."));	
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		
		return new MultipleActions(actions);
	}
	
	private void getQuest() {
		final SpeakerNPC npc = npcs.get("Mayor Chalmers");
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,expireDelay))),
            ConversationStates.ATTENDING,
            null,
            new SayRequiredItemAction(QUEST_SLOT, 0, "Twoje zamówienie pozostaje aktualne: [item]. Nie musisz pytać o nowe zadanie, przynieś tę dostawę i powiedz #zakończone."));
		
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new TimePassedCondition(QUEST_SLOT,1,expireDelay)),
            ConversationStates.ATTENDING,
            null,
            new SayRequiredItemAction(QUEST_SLOT, 0, "Nadal czekam na [item]. Poszukiwania trwają już dość długo, więc mogę przyjąć prośbę o #inne zamówienie; możesz też dokończyć obecne. Dostarczony komplet zgłoś słowem #zakończone."));
	
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,delay))),
            ConversationStates.ATTENDING,
            null,
            new SayTimeRemainingAction(QUEST_SLOT,1, delay, "Możesz dostać tylko jedno zadanie dziennie. Proszę wróć za"));

		
		
		
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new OrCondition(new QuestNotStartedCondition(QUEST_SLOT),
								new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
												 new TimePassedCondition(QUEST_SLOT,1,delay))),
            ConversationStates.ATTENDING,
            null,
            startQuestAction());
	}
	
	private void completeQuest() {
		final SpeakerNPC npc = npcs.get("Mayor Chalmers");
		
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.FINISH_MESSAGES,
            new QuestNotStartedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Obawiam się, że jeszcze nie dałem tobie #zadania.",
            null);
		
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.FINISH_MESSAGES,
            new QuestCompletedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Już ukończyłeś ostatnie zadanie, które ci dałem.",
            null);
		
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new DropRecordedItemAction(QUEST_SLOT,0));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		actions.add(new IncrementQuestAction(QUEST_SLOT, 2, 1));
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "done"));
		actions.add(new IncreaseXPDependentOnLevelAction(8, 90.0));
		actions.add(new IncreaseKarmaAction(20));
		actions.add(new IncreaseAtkXPAction(2000));
		actions.add(new EquipItemAction("skrzynia skarbów III", 1));
		actions.add(new IncreaseDefXPAction(2500));
		
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.FINISH_MESSAGES,
            new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,0)),
            ConversationStates.ATTENDING,
            "W imieniu mieszkańców Ados dziękuję za dostawę. To kolejna sprawa, której nie trzeba już odkładać na jutro. Przyjmij zapłatę za swoją pomoc.",
            new MultipleActions(actions));
		
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.FINISH_MESSAGES,
            new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new NotCondition(new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,0))),
            ConversationStates.ATTENDING,
            null,
            new SayRequiredItemAction(QUEST_SLOT, 0, "Do pełnej dostawy brakuje ci jeszcze: [item]. Nie zabieram części zamówienia; wróć z całym kompletem i powiedz #zakończone."));

	}
	
	private void abortQuest() {
		final SpeakerNPC npc = npcs.get("Mayor Chalmers");
		
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.ABORT_MESSAGES,
            new AndCondition(new QuestActiveCondition(QUEST_SLOT),
						 		 new TimePassedCondition(QUEST_SLOT,1,expireDelay)),
            ConversationStates.ATTENDING,
            null,
            // start quest again immediately
				startQuestAction());
		
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.ABORT_MESSAGES,
            new AndCondition(new QuestActiveCondition(QUEST_SLOT),
						 		 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,expireDelay))),
            ConversationStates.ATTENDING,
            "Nie minęło zbyt wiele czasu, od rozpoczęcia zadania. Nie pozwolę Tobie poddać się tak szybko.",
            null);
		
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.ABORT_MESSAGES,
            new QuestNotActiveCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Obawiam się, że jeszcze nie dałem tobie #zadania.",
            null);
		
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	@Override
	public List<String> getHistory(final Player player) {
        return games.stendhal.server.core.rp.quest.CollectionQuestProgress.history(this, player);
    }

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Dzienne Zadanie na Przedmiot",
				"Burmistrz Chalmers organizuje zaopatrzenie mieszkańców Ados. Zapytaj o dzisiejsze zamówienie i przynieś wskazany przedmiot.",
				true);
		
		buildItemsMap();
		
		getQuest();
		completeQuest();
		abortQuest();
	}

	@Override
	public String getName() {
		return "DailyItemQuest";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}
	
	@Override
	public boolean isRepeatable(final Player player) {
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,delay)).fire(player, null, null);
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Mayor Chalmers";
	}



    public String getCollectionKind(){return "random";}
    public java.util.Map<String,Integer> getCollectionItems(){return new java.util.TreeMap<String,Integer>(items);}
    public String getCollectionHint(){return "Liczy się tylko przedmiot i ilość zapisane w aktualnym zamówieniu. Przynieś całą dostawę i zgłoś jej zakończenie. Kolejne zlecenie oraz wymiana trudnego zamówienia są dostępne po upływie odpowiedniego czasu.";}
    public int getCollectionWait(){return delay;} public int getCollectionExpiry(){return expireDelay;}

}
