/* $Id: KillAdosSpiders.java,v 1.12 2025/06/22 11:55:16 davvids Exp $ */
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

public class KillAdosSpiders extends AbstractQuest {

	private static final String QUEST_SLOT = "kill_ados_spiders";
	protected HashMap<String, Pair<Integer, Integer>> creaturestokill = new HashMap<String, Pair<Integer,Integer>>();
	
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	public KillAdosSpiders() {
		super();
		
		 creaturestokill.put("pająk",
				 new Pair<Integer, Integer>(0, 50));
				 
		 creaturestokill.put("pająk ptasznik",
				 new Pair<Integer, Integer>(0, 50));
				 
		 creaturestokill.put("królowa pająków",
				 new Pair<Integer, Integer>(0, 30));
				 
		 creaturestokill.put("arachne",
				 new Pair<Integer, Integer>(0, 1));
				 
		 creaturestokill.put("olbrzymi ptasznik",
				 new Pair<Integer, Integer>(0, 1));
		 		
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Walt");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Pająki które zaczeły się pojawiać w większych ilościach straszą mojego syna. Poszukuje kogoś kto się z nimi rozprawi. Może mi pomożesz?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new TimePassedCondition(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*999999)),
				ConversationStates.QUEST_OFFERED,
				"Pajęczaki znów wróciły... Może znów mi pomożesz?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*999999)), new QuestStateStartsWithCondition(QUEST_SLOT, "done")),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*999999, "Dziękuje za Twoją pomoc z pajęczakami. Moja rodzina jest bezpieczna!"));
	

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, creaturestokill));

		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Dziękuję!!! Idź i zabij 50 pająków, 50 pająków ptaszników, 30 królowych pająków, 1 arachne oraz 1 olbrzymiego ptasznika. Jeżeli nie wiesz gdzie szukać konkretnego potwora, zapytaj Starkada w banku Semos. Olbrzymi ptasznik oraz arachne są niesamowicie niebezpieczne - możesz spróbować zaatakować je z dystansu.",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED, 
				ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING,
				"Co za szkoda... Moja rodzina jest w niebezpieczeństwie...",
				new SetQuestAction(QUEST_SLOT, 0, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Walt");
		
		ChatAction addRandomNumberOfItemsAction = new ChatAction() {
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				//add random number of soups
				final StackableItem soup = (StackableItem) SingletonRepository.getEntityManager()
						.getItem("sztabka złota");
				int amount;
				// between 1 and 5 soup
				amount = Rand.rand(9) + 2;
				soup.setQuantity(amount);
				player.equipOrPutOnGround(soup);
			}
		};

		final List<ChatAction> actions = new LinkedList<ChatAction>();
	    actions.add(addRandomNumberOfItemsAction);
		actions.add(new IncreaseXPAction(500000));
		actions.add(new IncreaseAtkXPAction(20000));
        actions.add(new IncreaseDefXPAction(25000));
		actions.add(new EquipItemAction("smoczy zwój", 1));
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
				"Bardzo dziękuję! Moja rodzina jest teraz bezpieczna. Weź te sztabki złota, odkładałem je przez pół życia na właśnie taką okazję... Weź również ten smoczy zwój, nie wiem do czego służy. Od dawna leży i się kurzy...",
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Proszę pomóż mojej rodzinie i zabij te pająki!!!",
				null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Rodzina Walta w niebezpieczeństwie",
				"Pajęczaki namnożyły się w krainie i zagrażają bezpieczeństwu rodziny Walta z Ados. Może powinienem im pomóc.",
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
				res.add("Musze zabić 50 pająków, 50 pająków ptaszników, 30 królowych pająków, arachne oraz olbrzymiego ptasznika, aby pomóc rodzinie Walta.");
			} else if(isRepeatable(player)){
				res.add("Mineło już sporo czasu... prawdopodobnie rodzina Walta znowu mnie potrzebuje!");
			} else {
				res.add("Pokonałem pajęczaki i rodzina Walta jest znów bezpieczna.	W nagrodę za ukończenie zadania otrzymałem: 500.000 pkt doświadczenia, 20.000 pkt ataku, 20.000 pkt obrony, smoczy zwój i zapas sztabek złota.");
			}
			int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
			if (repetitions > 0) {
				res.add("Pomogłem Waltowi "
						+ Grammar.quantityplnoun(repetitions, "razy") + ".");
			}
			return res;
	}


	@Override
	public String getName() {
		return "KillAdosSpiders";

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
		return "Walt";
	}
	
	@Override
	public String getRegion() {
		return Region.NALWOR_CITY;
	}
}
