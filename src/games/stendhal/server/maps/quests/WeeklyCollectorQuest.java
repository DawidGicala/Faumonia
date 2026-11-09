/* $Id: WeeklyCollectorQuest.java,v 1.66 2012/04/19 18:27:49 kymara Exp $ */
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

import games.stendhal.common.MathHelper;
import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
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
 * QUEST: Weekly Item Fetch Quest.
 * <p>
 * PARTICIPANTS:
 * <ul><li> Wandy, Museum Curator of Kirdneh
 * <li> some items
 * </ul>
 * STEPS:<ul>
 * <li> talk to Museum Curator to get a quest to fetch a rare item
 * <li> bring the item to the Museum Curator
 * <li> if you cannot bring it in 6 weeks she offers you the chance to fetch
 * 
 * another instead </ul>
 * 
 * REWARD:
 * <ul><li> xp
 * <li> between 100 and 600 money
 * <li> can buy kirdneh house if other eligibilities met
 * <li> 10 Karma
 * </ul>
 * REPETITIONS:
 * <ul><li> once a week</ul>
 */
public class WeeklyCollectorQuest extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeCollectionDetails {

	private static final String QUEST_SLOT = "weekly_collector";
	
	/** How long until the player can give up and start another quest */
	private static final int expireDelay = MathHelper.MINUTES_IN_ONE_WEEK * 3; 
	
	/** How often the quest may be repeated */
	private static final int delay = MathHelper.MINUTES_IN_ONE_WEEK; 
	
	/**
	 * All items which are hard enough to find but not tooo hard and not in Daily quest. If you want to do
	 * it better, go ahead. *
	 */
	private static Map<String,Integer> items;

	private static void buildItemsMap() {
		items = new HashMap<String, Integer>();
		items.put("tarcza jednorożca",1);
		items.put("zbroja balorughtów",1);
		items.put("płaszcz balorughtów",1);
		items.put("buty superczłowieka",1);
		items.put("spodnie superczłowieka",1);
		items.put("elficki naszyjnik",1);
		items.put("lodowa kusza",1);
		items.put("różdżka Strzyboga",1);
		items.put("trójząb Trzygłowa",1);
		items.put("różdżka Wołosa",1);
		items.put("rękawice cieni",1);
		items.put("topór oburęczny magiczny",1);
		items.put("miecz demonów",1);
		items.put("pogromca",1);
		items.put("miecz elfów ciemności",1);
		items.put("miecz nieśmiertelnych",1);
		items.put("miecz xenocyjski",1);
		items.put("piekielny sztylet",1);
		items.put("sztylet mroku",1);
		items.put("czarny sztylet",1);
		items.put("sztylet mordercy",1);
		items.put("dziub ptaka",1);
		items.put("ząb potwora",1);
		items.put("ząb potwora",2);
		items.put("róg jednorożca",1);
		items.put("róg jednorożca",2);
		items.put("diament",1);
		items.put("diament",2);
		}
	
	private ChatAction startQuestAction() {
		// common place to get the start quest actions as we can both starts it and abort and start again
		
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new StartRecordingRandomItemCollectionAction(QUEST_SLOT, 0, items, "Każdy rzadki przedmiot ma u mnie własne miejsce, lecz kolekcja wciąż ma luki. Tym razem szukam [item]. Przynieś ten konkretny okaz i zgłoś #zakończone, a przygotuję zapłatę."));	
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		
		return new MultipleActions(actions);
	}
	
	private void getQuest() {
		final SpeakerNPC npc = npcs.get("Wandy");
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,expireDelay))),
            ConversationStates.ATTENDING,
            null,
            new SayRequiredItemAction(QUEST_SLOT, 0, "W mojej kolekcji wciąż brakuje tego okazu: [item]. Przynieś go i powiedz #zakończone; nowe poszukiwania ustalimy później."));
		
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new TimePassedCondition(QUEST_SLOT,1,expireDelay)),
            ConversationStates.ATTENDING,
            null,
            new SayRequiredItemAction(QUEST_SLOT, 0, "Wciąż szukam [item], ale rozumiem, że trudno trafić na niektóre okazy. Możemy wybrać #inne zlecenie albo pozostać przy obecnym; gotową dostawę zgłoś słowem #zakończone."));
	
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,delay))),
            ConversationStates.ATTENDING,
            null,
            new SayTimeRemainingAction(QUEST_SLOT,1, delay, "Obecne znalezisko ma już miejsce w kolekcji. O następnym porozmawiamy za "));
		
		
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new StartRecordingRandomItemCollectionAction(QUEST_SLOT, 0, items, "Każdy rzadki przedmiot ma u mnie własne miejsce, lecz kolekcja wciąż ma luki. Tym razem szukam [item]. Przynieś ten konkretny okaz i zgłoś #zakończone, a przygotuję zapłatę."));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		
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
		final SpeakerNPC npc = npcs.get("Wandy");
		
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.FINISH_MESSAGES,
            new QuestNotStartedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Nie pamiętam, abym dawał Tobie #zadanie.",
            null);
		
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.FINISH_MESSAGES,
            new QuestCompletedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Już ukończyłeś ostatnie zadanie, które Ci dałem.",
            null);
		
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new DropRecordedItemAction(QUEST_SLOT,0));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		actions.add(new IncrementQuestAction(QUEST_SLOT,2,1));
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "done"));
		actions.add(new IncreaseXPDependentOnLevelAction(5.0/3.0, 290.0));
		actions.add(new IncreaseKarmaAction(10.0));
		actions.add(new IncreaseAtkXPAction(20000));
		actions.add(new IncreaseDefXPAction(25000));
		actions.add(new ChatAction() {
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				int goldamount;
				final StackableItem money = (StackableItem) SingletonRepository.getEntityManager()
								.getItem("money");
				goldamount = 100 * Rand.roll1D6();
				money.setQuantity(goldamount);
				player.equipOrPutOnGround(money);
				raiser.say("Właśnie takiego uzupełnienia potrzebowała moja kolekcja. Dziękuję za wytrwałość w poszukiwaniach. Pokryję ustalone wydatki. Otrzymujesz " + goldamount + " monet.");
			}
		});
		
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.FINISH_MESSAGES,
            new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,0)),
            ConversationStates.ATTENDING,
            null,
            new MultipleActions(actions));
		
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.FINISH_MESSAGES,
            new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new NotCondition(new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,0))),
            ConversationStates.ATTENDING,
            null,
            new SayRequiredItemAction(QUEST_SLOT, 0, "Nie masz jeszcze całego uzgodnionego zestawu: [item]. Przyjmę go razem, żeby uzupełnić tę część kolekcji. Wróć z kompletem i powiedz #zakończone."));
		
	}
	
	private void abortQuest() {
		final SpeakerNPC npc = npcs.get("Wandy");
		
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.ABORT_MESSAGES,
            new AndCondition(new QuestActiveCondition(QUEST_SLOT),
						 		 new TimePassedCondition(QUEST_SLOT,1,expireDelay)),
            ConversationStates.ATTENDING,
            null,
            startQuestAction());
		
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.ABORT_MESSAGES,
            new AndCondition(new QuestActiveCondition(QUEST_SLOT),
						 		 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,expireDelay))),
            ConversationStates.ATTENDING,
            "Jeszcze poczekajmy z wyborem innego okazu. Obecna prośba pozostaje aktualna.",
            null);
		
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.ABORT_MESSAGES,
            new QuestNotActiveCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Obawiam się, że jeszcze nie dałem Tobie #zadania.",
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
				"Wandy potrzebuje pomocy!",
				"Wandy z Ados szuka rzadkich przedmiotów do kolekcji. Uzgodnij z nim konkretny okaz; po wykonaniu zlecenia kolejne będzie dostępne po tygodniu.",
				true);
		buildItemsMap();
		
		getQuest();
		completeQuest();
		abortQuest();
	}

	@Override
	public String getName() {
		return "WeeklyCollectorQuest";
	}
	
	// the items requested are pretty hard to get, so it's not worth prompting player to go till they are higher level.
	@Override
	public int getMinLevel() {
		return 60;
	}
	
	@Override
	public boolean isRepeatable(final Player player) {
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,delay)).fire(player, null, null);
	}
	
	@Override
	public String getRegion() {
		return Region.KIRDNEH;
	}

	@Override
	public String getNPCName() {
		return "Wandy";
	}


    public String getCollectionKind(){return "random";}
    public java.util.Map<String,Integer> getCollectionItems(){return new java.util.TreeMap<String,Integer>(items);}
    public String getCollectionHint(){return "Liczy się tylko przedmiot i ilość zapisane w aktualnym zamówieniu. Przynieś całą dostawę i zgłoś jej zakończenie. Kolejne zlecenie oraz wymiana trudnego zamówienia są dostępne po upływie odpowiedniego czasu.";}
    public int getCollectionWait(){return delay;} public int getCollectionExpiry(){return expireDelay;}

}
