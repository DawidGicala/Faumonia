/* $Id: KillHigh.java,v 1.18 2012/04/19 18:26:42 kymara Exp $ */
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
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import marauroa.common.Pair;

/**
 * QUEST: Kill Gnomes
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li> Octavio, by the mill in Semos Plains
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Gnomes have been stealing carrots so Octavio asks you to kill some.
 * <li> You go kill the Gnomes in the gnome village and you get the reward from Octavio
 * </ul>
 * <p>
 * REWARD:
 * <ul>
 * <li> 3 potions
 * <li> 100 XP
 * <li> No karma (deliberately. Killing gnomes is mean!)
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li> after 7 days.
 * </ul>
 */

public class KillHigh extends AbstractQuest {

	private static final String QUEST_SLOT = "kill_high4";
	private static final int WEEK_IN_MINUTES = MathHelper.MINUTES_IN_ONE_HOUR * 24 * 7 * 4 * 6;
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Octavio");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Poszukuje kogoś kto rozprawi się z silnymi potworami dręczącymi naszą krainę. "
				+ "Może mi pomożesz?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
						 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"Kolejny raz potężne potwory zagrażają bezpieczeństwu naszej krainy. Może znów mi pomożesz?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
						 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES))),
				ConversationStates.ATTENDING,
				"Na ten moment potwory zostały przepędzone. Na tą chwilę nie potrzebuje pomocy.",
				null);

		
		final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("czarna śmierć", new Pair<Integer, Integer>(0,1));
		toKill.put("upadły anioł", new Pair<Integer, Integer>(0,3));
		toKill.put("anioł ciemności", new Pair<Integer, Integer>(0,2));
		toKill.put("archanioł ciemności", new Pair<Integer, Integer>(0,1));
		toKill.put("kostucha wielka", new Pair<Integer, Integer>(0,2));
		toKill.put("kostucha złota wielka", new Pair<Integer, Integer>(0,1));
		toKill.put("kostucha różowa wielka", new Pair<Integer, Integer>(0,1));
		toKill.put("śmierć", new Pair<Integer, Integer>(0,10));
		toKill.put("kostucha", new Pair<Integer, Integer>(0,25));
		toKill.put("szkielet anioła", new Pair<Integer, Integer>(0,1));
		toKill.put("krasnal golem", new Pair<Integer, Integer>(0,3));
		toKill.put("balrog", new Pair<Integer, Integer>(0,1));
		toKill.put("arachne", new Pair<Integer, Integer>(0,2));
		toKill.put("lodowy olbrzym", new Pair<Integer, Integer>(0,10));
		toKill.put("elf ciemności matrona", new Pair<Integer, Integer>(0,3));
		toKill.put("chaosu lord wywyższony", new Pair<Integer, Integer>(0,5));
		toKill.put("gashadokuro", new Pair<Integer, Integer>(0,1));
		toKill.put("kasarkutominubat", new Pair<Integer, Integer>(0,3));
		toKill.put("kostucha różowa", new Pair<Integer, Integer>(0,10));
		
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Dziękuję! Zabij czarną śmierć, 3 upadłe anioły, 2 anioły ciemności, archanioła ciemności, 2 kostuchy wielkie, kostuchę różową wielką, kostuchę złotą wielką, 10 śmierci, 25 kostuch, 10 kostuch różowych, szkieleta anioła, 3 krasnale golemy, balroga, 10 lodowych olbrzymów, 5 chaosu lordów wywyższonych, gashadokuro, 3 kasarkutominubaty, 3 elfy ciemności matrony oraz 2 arachne aby ochronić naszą krainę.",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED, 
				ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING,
				"Co za szkoda... Może kiedyś zmienisz zdanie i pomożesz naszej krainie.. "
				+ "Może poradze sobie sam.. ",
				new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Octavio");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new EquipItemAction("wielki eliksir", 1000));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
		actions.add(new IncreaseXPAction(10000000));
		actions.add(new IncreaseAtkXPAction(20000));
		actions.add(new IncreaseDefXPAction(20000));
		actions.add(new SetQuestAction(QUEST_SLOT, "killed;1"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		
		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);		
		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.ATTENDING, 
				"Bardzo dziękuję! Teraz możemy spać trochę spokojniej. "
				+ "Proszę przyjmij te eliksiry.",
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Proszę pomóż naszej krainie! "
				+ "W dalszym ciągu nie zabiłeś któregoś z potworów... Sam nie wiem którego.. lepiej sobie przypomnij albo przeszukaj ponownie ich siedziby.",
				null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Zabij silne potwory",
				"Przerażąjąco silne potwory regularnie nawiedzają nasza kraine. Octavio szuka kogoś kto zrobi z tym porządek.",
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
				res.add("Musze zabić czarną śmierć, 3 upadłe anioły, 2 anioły ciemności, archanioła ciemności, 2 kostuchy wielkie, kostuche różową wielką, kostuchę złotą wielką, 10 śmierci, 25 kostuch, 10 kostuch  różowych, szkieleta anioła, 3 krasnale golemy, balroga, 10 lodowych olbrzymów,3 elfy ciemności matrony, 5 chaosu lordów wywyższonych, gashadokuro, 3 kasarakutominubaty oraz 2 arachne, aby uchronić nasza krainę przed zagładą!");
			} else if(isRepeatable(player)){
				res.add("Teraz po tygodniu powinienem znowu odwiedzić Octavio. Może potwory znów wróciły!");
			} else {
				res.add("Zabiłem przerażąjąco silne potwory zagrażające Faumonii. Teraz możemy spać spokojnie!");
			}
			return res;
	}


	@Override
	public String getName() {
		return "KillHigh";
	}
	
	@Override
	public int getMinLevel() {
		return 400;
	}
	
	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
				 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)).fire(player,null, null);
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"killed").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Octavio";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	
}
