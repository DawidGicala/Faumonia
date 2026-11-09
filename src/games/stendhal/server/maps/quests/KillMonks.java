/* $Id: KillMonks.java,v 1.12 2012/04/20 17:42:28 kymara Exp $ */
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
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.Pair;

/**
 * QUEST: KillMonks
 *
 * PARTICIPANTS: <ul>
 * <li> Andy on Ados cemetery
 * <li> Darkmonks and normal monks
 * </ul>
 *
 * STEPS:<ul>
 * <li> Andy who is sad about the death of his wife, wants revenge for her death 
 * <li> Kill 25 monks and 25 darkmonks for him for reaching his goal
 * </ul>
 * 
 *
 * REWARD:<ul>
 * <li> 15000 XP
 * <li> 1-5 soup
 * <li> some karma
 * </ul>
 *
 * REPETITIONS: <ul><li>once in two weeks</ul>
 * 
 * @author Vanessa Julius, idea by anoyyou

 */

public class KillMonks extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeHuntDetails {

	private static final String QUEST_SLOT = "kill_monks3";
	protected HashMap<String, Pair<Integer, Integer>> creaturestokill = new HashMap<String, Pair<Integer,Integer>>();
	
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	public KillMonks() {
		super();
		
		 creaturestokill.put("mnich ciemności",
				 new Pair<Integer, Integer>(0, 25));
				 
		 creaturestokill.put("mnich",
				 new Pair<Integer, Integer>(0, 25));
		 		
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Andy");

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new QuestNotStartedCondition(QUEST_SLOT),
                ConversationStates.QUEST_OFFERED,
                "Moja żona szła do Wofol po pizzę od Kroipa i już nie wróciła. Napadli na nią mnisi, a ja do dziś nie potrafię się z tym pogodzić. Pomożesz powstrzymać tych, którzy odebrali mi spokój?",
                null);

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new TimePassedCondition(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*2)),
                ConversationStates.QUEST_OFFERED,
                "Mnisi znów nie dają mi spokoju, a wspomnienia wracają. Jeśli możesz, pomóż mi jeszcze raz się z nimi uporać.",
                null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*2)), new QuestStateStartsWithCondition(QUEST_SLOT, "done")),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*2, "Ci mnichowie dostali lekcje, ale możliwe, że znów będę potrzebował twojej pomocy za"));
	

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, creaturestokill));

		
		npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.YES_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "Proszę cię o pokonanie obu grup: mnichów i mnichów ciemności. Wiem, że nie zwróci mi to żony, ale może kolejna osoba bezpiecznie dotrze do domu." + "\nCele: " + games.stendhal.server.core.rp.quest.HuntQuestProgress.targetText(this,null) + ".",
                new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.NO_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "Rozumiem, to ciężka prośba. Nie będę miał ci za złe, jeśli teraz odejdziesz.",
                new SetQuestAction(QUEST_SLOT, 0, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Andy");
		
		ChatAction addRandomNumberOfItemsAction = new ChatAction() {
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				//add random number of soups
				final StackableItem soup = (StackableItem) SingletonRepository.getEntityManager()
						.getItem("ciasto z wiśniami");
				int amount;
				// between 1 and 5 soup
				amount = Rand.rand(4) + 1;
				soup.setQuantity(amount);
				player.equipOrPutOnGround(soup);
			}
		};

		final List<ChatAction> actions = new LinkedList<ChatAction>();
	    actions.add(addRandomNumberOfItemsAction);
		actions.add(new IncreaseXPAction(15000));
		actions.add(new EquipItemAction("duży eliksir", 50));
		actions.add(new EquipItemAction("skrzynia skarbów I", 1));
		actions.add(new EquipItemAction("skrzynia skarbów II", 1));
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "done"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		actions.add(new IncrementQuestAction(QUEST_SLOT,2,1));
		
		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);		
		npc.add(ConversationStates.ATTENDING,
                triggers,
                new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
                ConversationStates.ATTENDING,
                "Dziękuję, że wracasz z taką wiadomością. Przygotowałem ciasto, eliksiry i skrzynie; to niewiele wobec wspomnień, ale chcę się z tobą podzielić tym, co mam.",
                new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING,
                triggers,
                new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
                ConversationStates.ATTENDING,
                null,
                (p, sentence, raiser) -> raiser.say("Wiem, że ta droga nie jest łatwa. Zostało jeszcze trochę do zrobienia przy obu grupach mnichów; proszę, uważaj na siebie." + "\n" + games.stendhal.server.core.rp.quest.HuntQuestProgress.progressText(KillMonks.this,p)));
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Zabij Mnichy",
				"Andy z Ados stracił żonę z rąk mnichów. Chce pomścić jej śmierć; wysłuchaj go i poznaj warunki wyprawy, zanim zdecydujesz się pomóc.",
				false);
		step_1();
		step_2();
		step_3();
	}
	
	@Override
	public List<String> getHistory(final Player player) {
        return games.stendhal.server.core.rp.quest.HuntQuestProgress.history(this,player);
    }


	@Override
	public String getName() {
		return "KillMonks";

	}
	
	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
				 new TimePassedCondition(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*2)).fire(player,null, null);
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"done").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Andy";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

    @Override public java.util.Map<String, Pair<Integer,Integer>> getDialogueHuntTargets(Player player) {
        return java.util.Collections.unmodifiableMap(new java.util.TreeMap<String,Pair<Integer,Integer>>(creaturestokill));
    }
    @Override public int getDialogueHuntTotal(Player player) { return 0; }
    @Override public int getDialogueKillIndex() { return 1; }
    @Override public String getDialogueHuntHint(Player player) { return "Andy stracił żonę na drodze do Wofol. Pokonaj po 25 mnichów i mnichów ciemności, a następnie wróć do niego z wieścią o zakończeniu wyprawy."; }
}
