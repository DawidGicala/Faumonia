/* $Id: KillSausid.java,v 1.12 2025/06/22 11:55:16 davvids Exp $ */
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
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.Pair;

public class KillSausid extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeHuntDetails {

	private static final String QUEST_SLOT = "kill_sausid";
	protected HashMap<String, Pair<Integer, Integer>> creaturestokill = new HashMap<String, Pair<Integer,Integer>>();
	
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	public KillSausid() {
		super();
		
		 creaturestokill.put("orzeł gigant",
				 new Pair<Integer, Integer>(0, 10));
				 
		 creaturestokill.put("krasnal golem",
				 new Pair<Integer, Integer>(0, 5));

		 creaturestokill.put("gigantyczny krasnal",
				 new Pair<Integer, Integer>(0, 1));
		 		
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Sausid");

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new QuestNotStartedCondition(QUEST_SLOT),
                ConversationStates.QUEST_OFFERED,
                "Mów ciszej, proszę; nie chcę, żeby cała karczma znała moje kłopoty. Potrzebuję pomocy z kilkoma stworami, do których sam nie podejdę. Podejmiesz się dyskretnej wyprawy?",
                null);

		npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new TimePassedCondition(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*999999)),
                ConversationStates.QUEST_OFFERED,
                "Znów potrzebuję twojej pomocy z tymi stworami. Omówmy to spokojnie, bez zwracania na siebie uwagi.",
                null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*999999)), new QuestStateStartsWithCondition(QUEST_SLOT, "done")),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*999999, "Dziękuje za Twoją pomoc, pamiętaj, aby o tym nie rozpowiadać..."));
	

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, creaturestokill));

		
		npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.YES_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "Chodzi o orły giganty, krasnale golemy i jednego gigantycznego krasnala. Sprawdź dokładne nazwy, bo zwykłe ptaki i krasnale nie rozwiążą mojego problemu." + "\nCele: " + games.stendhal.server.core.rp.quest.HuntQuestProgress.targetText(this,null) + ".",
                new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.NO_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "W porządku, nie naciskam. Zachowaj tylko tę rozmowę dla siebie.",
                new SetQuestAction(QUEST_SLOT, 0, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Sausid");
		
		ChatAction addRandomNumberOfItemsAction = new ChatAction() {
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				//add random number of soups
				final StackableItem soup = (StackableItem) SingletonRepository.getEntityManager()
						.getItem("zwój do krypty");
				int amount;
				// between 1 and 5 soup
				amount = Rand.rand(2) + 8;
				soup.setQuantity(amount);
				player.equipOrPutOnGround(soup);
			}
		};

		final List<ChatAction> actions = new LinkedList<ChatAction>();
	    actions.add(addRandomNumberOfItemsAction);
		actions.add(new IncreaseXPAction(200000));
		actions.add(new IncreaseAtkXPAction(10000));
        actions.add(new IncreaseDefXPAction(12000));
		actions.add(new EquipItemAction("bilet turystyczny", 1));
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
                "Dobrze, naprawdę dobrze; właśnie takiego meldunku potrzebowałem. Weź bilet turystyczny i zwoje do krypty, a o szczegółach naszej umowy lepiej nie opowiadaj całej karczmie.",
                new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING,
                triggers,
                new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
                ConversationStates.ATTENDING,
                null,
                (p, sentence, raiser) -> raiser.say("Jeszcze nie wszystkie stworzenia z listy zostały pokonane. Powiem ci dokładnie, czego brakuje, żebyś nie wracał tu bez potrzeby." + "\n" + games.stendhal.server.core.rp.quest.HuntQuestProgress.progressText(KillSausid.this,p)));
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Tajemnicza misja Sausida",
				"Sausid z tawerny Fado woli rozmawiać o swoich kłopotach ściszonym głosem. Poznaj jego listę przeciwników i wróć, gdy rozprawisz się ze wszystkimi wymaganymi celami.",
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
		return "KillSausid";

	}
	
	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
				 new TimePassedCondition(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*999999)).fire(player,null, null);
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"done").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Sausid";
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
    @Override public String getDialogueHuntHint(Player player) { return "Sausid oczekuje pokonania dziesięciu orłów gigantów, pięciu krasnali golemów i jednego gigantycznego krasnala. Olbrzymi ptasznik to inny przeciwnik i nie zastąpi orła."; }
}
