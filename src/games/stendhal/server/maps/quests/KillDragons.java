/* $Id: KillDragons.java,v 1.18 2012/04/19 18:26:42 kymara Exp $ */
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
 * <li> Django, by the mill in Semos Plains
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Gnomes have been stealing carrots so Django asks you to kill some.
 * <li> You go kill the Gnomes in the gnome village and you get the reward from Django
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

public class KillDragons extends AbstractQuest {

	private static final String QUEST_SLOT = "kill_dragons3";
	private static final int WEEK_IN_MINUTES = MathHelper.MINUTES_IN_ONE_HOUR * 24 * 12;
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Django");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Poszukuje kogoś kto pozabija trochę smoków. "
				+ "Może mi pomożesz?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
						 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"Kolejny raz przyleciał do nas nadmiar smoków. Może znów mi pomożesz?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
						 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES))),
				ConversationStates.ATTENDING,
				"Na ten moment ograniczyliśmy populacje smoków.",
				null);

		final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("zielony smok", new Pair<Integer, Integer>(0,10));
		toKill.put("błękitny smok", new Pair<Integer, Integer>(0,5));
		toKill.put("czerwony smok",new Pair<Integer, Integer>(0,10));
		toKill.put("szkielet smoka",new Pair<Integer, Integer>(0,5));
		toKill.put("zgniły szkielet smoka",new Pair<Integer, Integer>(0,1));
		toKill.put("złoty smok",new Pair<Integer, Integer>(0,3));
		toKill.put("czarny smok",new Pair<Integer, Integer>(0,3));
		toKill.put("czarne smoczysko",new Pair<Integer, Integer>(0,1));
		toKill.put("smok arktyczny",new Pair<Integer, Integer>(0,1));
		toKill.put("dwugłowy niebieski smok",new Pair<Integer, Integer>(0,1));
		toKill.put("dwugłowy zielony smok",new Pair<Integer, Integer>(0,2));
		toKill.put("dwugłowy czerwony smok",new Pair<Integer, Integer>(0,1));
		toKill.put("dwugłowy czarny smok",new Pair<Integer, Integer>(0,1));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Dziękuję! Zabij 10 zielony smok, 5 błękitny smok, 10 czerwony smok, 5 szkielet smoka, zgniły szkielet smoka, 3 złoty smok, 3 czarny smok, czarne smoczysko, smok arktyczny, dwugłowy niebieski smok, 2 dwugłowy zielony smok, dwugłowy czerwony smok oraz dwugłowy czarny smok.",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED, 
				ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING,
				"Co za szkoda... Może kiedyś zmienisz zdanie i mi pomożesz. "
				+ "Może poradze sobie sam.. ",
				new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Django");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
	    actions.add(new EquipItemAction("skóra zielonego smoka", 2));
		actions.add(new EquipItemAction("skóra niebieskiego smoka", 1));
		actions.add(new EquipItemAction("skóra czerwonego smoka", 1));
		actions.add(new EquipItemAction("skóra czarnego smoka", 1));
		actions.add(new EquipItemAction("skóra złotego smoka", 1));
		actions.add(new EquipItemAction("szmaragd", 4));
		actions.add(new EquipItemAction("szafir", 3));
		actions.add(new EquipItemAction("rubin", 2));
		actions.add(new EquipItemAction("obsydian", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VI", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VII", 1));
		actions.add(new EquipItemAction("skrzynia skarbów VIII", 1));
		actions.add(new IncreaseXPAction(500000));
		actions.add(new IncreaseAtkXPAction(6000));
		actions.add(new IncreaseDefXPAction(8000));
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
				"Widzę, że zabiłeś smoki. Mam nadzieje, że przez jakiś czas będzie z nimi spokój. "
				+ "Proszę przyjmij te drobiazgi w dowód uznania.",
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Proszę pomóż mi. "
				+ "W dalszym ciągu nie zabiłeś wszystkich smoków...",
				null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Zabij nadmiar smoków",
				"Od czasu do czasu do Faumonii przylatuje nadmiar smoków. Django szuka kogoś kto zrobi z tym porządek.",
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
				res.add("Musze zabić 10 zielonych smoków, 5 błękitnych smoków, 10 czerwonych smoków, 5 szkieletów smoka, zgniły szkielet smoka, 3 złote smoki, 3 czarne smoki, czarne smoczysko, smoka arktycznego, dwugłowego niebieskiego smoka, 2  dwugłowe zielone smoki, dwugłowego czerwonego smoka oraz dwugłowego czarnego smoka.");
			} else if(isRepeatable(player)){
				res.add("Teraz po 12 dniach powinienem znowu odwiedzić Django. Może znów potrzebuje pomocy!");
			} else {
				res.add("Smoki zostały przepędzone. Hura!");
			}
			return res;
	}


	@Override
	public String getName() {
		return "KillDragons";
	}
	
	@Override
	public int getMinLevel() {
		return 120;
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
		return "Django";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	
}
