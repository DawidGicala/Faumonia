/* $Id: KillElfes.java,v 1.12 2020/11/27 11:05:16 davvids Exp $ */
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

public class KillElfes extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeHuntDetails {

	private static final String QUEST_SLOT = "kill_elfes";
	protected HashMap<String, Pair<Integer, Integer>> creaturestokill = new HashMap<String, Pair<Integer,Integer>>();
	
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	public KillElfes() {
		super();
		
		 creaturestokill.put("elf milicjant",
				 new Pair<Integer, Integer>(0, 40));
				 
		 creaturestokill.put("elf żołnierz",
				 new Pair<Integer, Integer>(0, 40));
				 
		 creaturestokill.put("pani elf",
				 new Pair<Integer, Integer>(0, 20));
				 
		 creaturestokill.put("panna elf",
				 new Pair<Integer, Integer>(0, 20));
				 
		 creaturestokill.put("elf",
				 new Pair<Integer, Integer>(0, 20));
				 
		 creaturestokill.put("elf łucznik",
				 new Pair<Integer, Integer>(0, 15));
				 
		 creaturestokill.put("elf komandor",
				 new Pair<Integer, Integer>(0, 10));
		 		
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Lorithien");

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new QuestNotStartedCondition(QUEST_SLOT),
                ConversationStates.QUEST_OFFERED,
                "Rabusie regularnie napadają na listonoszy, a przesyłki nie docierają do mieszkańców Nalwor. Poczta potrzebuje ochrony szlaków, nie kolejnych obietnic. Pomożesz rozprawić się z napastnikami?",
                null);

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new TimePassedCondition(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*2)),
                ConversationStates.QUEST_OFFERED,
                "Znów otrzymuję wiadomości o napadach na listonoszy. Czy mogę liczyć na twoją pomoc przy zabezpieczeniu poczty?",
                null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*2)), new QuestStateStartsWithCondition(QUEST_SLOT, "done")),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*2, "Na razie poczta jest bezpieczna, ale możliwe, że znów będę potrzebował twojej pomocy za"));
	

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, creaturestokill));

		
		npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.YES_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "Wśród rabusiów są żołnierze, łucznicy i dowódcy, więc jedna zwycięska potyczka nie rozwiąże problemu. Sprawdź cały wykaz i wróć, gdy spełnisz każdy z celów." + "\nCele: " + games.stendhal.server.core.rp.quest.HuntQuestProgress.targetText(this,null) + ".",
                new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.NO_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "Rozumiem, nie jest to mała przysługa. Jeśli zmienisz zdanie, zapytaj mnie ponownie o sprawę poczty.",
                new SetQuestAction(QUEST_SLOT, 0, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Lorithien");
		
		ChatAction addRandomNumberOfItemsAction = new ChatAction() {
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				//add random number of soups
				final StackableItem soup = (StackableItem) SingletonRepository.getEntityManager()
						.getItem("sztabka mithrilu");
				int amount;
				// between 1 and 5 soup
				amount = Rand.rand(4) + 2;
				soup.setQuantity(amount);
				player.equipOrPutOnGround(soup);
			}
		};

		final List<ChatAction> actions = new LinkedList<ChatAction>();
	    actions.add(addRandomNumberOfItemsAction);
		actions.add(new IncreaseXPAction(100000));
		actions.add(new EquipItemAction("duży eliksir", 50));
		actions.add(new EquipItemAction("wielki eliksir", 20));
		actions.add(new EquipItemAction("żelazo", 14));
		actions.add(new EquipItemAction("sztabka złota", 11));
		actions.add(new EquipItemAction("skrzynia skarbów I", 1));
		actions.add(new EquipItemAction("skrzynia skarbów II", 1));
		actions.add(new EquipItemAction("skrzynia skarbów III", 1));
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
                "Dzięki tobie listonosze mają szansę bezpiecznie wrócić z drogi. Przyjmij przygotowane sztabki, eliksiry i skrzynie; Nalwor zapamięta tę pomoc.",
                new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING,
                triggers,
                new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
                ConversationStates.ATTENDING,
                null,
                (p, sentence, raiser) -> raiser.say("Jeszcze nie wszystkie oddziały rabusiów zostały rozbite. Przejrzyj ze mną listę, żeby kolejna wyprawa dotyczyła tych, których naprawdę brakuje." + "\n" + games.stendhal.server.core.rp.quest.HuntQuestProgress.progressText(KillElfes.this,p)));
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Zabij Rabusiów",
				"Rabusie regularnie napadają na listonoszy z Nalwor. Lorithien potrzebuje wojownika, który osłabi wskazane grupy i pomoże przywrócić bezpieczeństwo przesyłkom.",
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
		return "KillElfes";

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
		return "Lorithien";
	}
	
	@Override
	public String getRegion() {
		return Region.NALWOR_CITY;
	}

    @Override public java.util.Map<String, Pair<Integer,Integer>> getDialogueHuntTargets(Player player) {
        return java.util.Collections.unmodifiableMap(new java.util.TreeMap<String,Pair<Integer,Integer>>(creaturestokill));
    }
    @Override public int getDialogueHuntTotal(Player player) { return 0; }
    @Override public int getDialogueKillIndex() { return 1; }
    @Override public String getDialogueHuntHint(Player player) { return "Lorithien potrzebuje ochrony poczty Nalwor przed siedmioma rodzajami elfich rabusiów. Sprawdź osobne cele dla żołnierzy, milicjantów, łuczników i dowódców, zanim wrócisz z meldunkiem."; }
}
