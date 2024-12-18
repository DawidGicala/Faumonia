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

public class KillElfes extends AbstractQuest {

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
				"Ci przeklęci rabusie ciągle okradają naszych listonoszy... musimy znaleźć kogoś kto da im nauczke. Może mi pomożesz?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new TimePassedCondition(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*2)),
				ConversationStates.QUEST_OFFERED,
				"Poczta znowu ma problemy z rabusiami. Może znów mi pomożesz?",
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
				"Dziękuję! Idź i zabij 40 elfów żołnierzy, 40 elfów milicjantów, 20 pani elfów, 20 pann elfów, 20 elfów, 15 elfów łuczników i 10 elfów komandorów.",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED, 
				ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING,
				"Co za szkoda... Może kiedyś zmienisz zdanie i nam pomożesz",
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
		actions.add(new EquipItemAction("Skrzynia skarbów I", 1));
		actions.add(new EquipItemAction("Skrzynia skarbów II", 1));
		actions.add(new EquipItemAction("Skrzynia skarbów III", 1));
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
				"Bardzo dziękuję! Teraz poczta jest bezpieczna. Proszę przyjmij te sztabki i eliksiry.",
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Proszę pomóż mi i zabij tych rabusiów!",
				null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Zabij Rabusiów",
				"Listonosze w Nalwor regularnie są napadani przez rabusiów. Może powinienem im pomóc.",
				false);
		step_1();
		step_2();
		step_3();
	}
	
	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			if (!isCompleted(player)) {
				res.add("Musze zabić 40 elfów milicjantów, 40 elfów żołnierzy, 20 pann elfów, 20 pani elfów, 20 elfów, 15 elfów łuczników i 10 elfów komandorów, aby wykonać polecenie Lorithiena.");
			} else if(isRepeatable(player)){
				res.add("Teraz po dwóch tygodniach powinienem odwiedzić poczte w Nalwor. Może znów potrzebuje mojej pomocy!");
			} else {
				res.add("Pokonałem rabusiów i poczta w Nalwor jest już bezpieczna.	W nagrodę za ukończenie zadania otrzymałem: 50.000 pkt doświadczenia, 40 eliksirów, 20 dużych eliksirów i trochę sztabek.");
			}
			int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
			if (repetitions > 0) {
				res.add("Pomogłem Lorithienowi "
						+ Grammar.quantityplnoun(repetitions, "razy") + ".");
			}
			return res;
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
}
