/* $Id: KillGlut.java,v 1.18 2020/12/08 18:20:17 davvids Exp $ */
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
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import marauroa.common.Pair;

public class KillGlut extends AbstractQuest {

	private static final String QUEST_SLOT = "kill_glut";
	
	private static final String GLUT1 = "kill_glut1";
	private static final String GLUT2 = "kill_glut2";
	private static final String GLUT3 = "kill_glut3";
	private static final String GLUT4 = "kill_glut4";
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Kreg");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new LevelGreaterThanCondition(449)),
				ConversationStates.QUEST_OFFERED,
				"Witaj, mam dla ciebie nietypowe zadanie."
				+ " Jesteś zainteresowany?",
				null);

		final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("latający złoty smok", new Pair<Integer, Integer>(0,1));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"W mojej piwnicy znajdują się.... gluty... nie chcą z tamtąd wyjść, poszukuje kogoś kto do nich pójdzie i z nimi porozmawia. Mam nadzieje, że wtedy sobie pójdą...",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED, 
				ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING,
				"Ech... "
				+ "Szkoda... ",
				new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Kreg");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new EquipItemAction("obsydian", 1));
		actions.add(new SetQuestAction(QUEST_SLOT, "done;1"));	
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		
		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);		
		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new QuestCompletedCondition(GLUT1),
						new QuestCompletedCondition(GLUT2),
						new QuestCompletedCondition(GLUT3),
						new QuestCompletedCondition(GLUT4),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.ATTENDING, 
				"Pomogłeś glutom... dalej nie chcą z tamtąd wyjść... nie wiem co z nimi zrobie... "
				+ "W każdym razie, dziękuje. Weź tę zupę w ramach podziękowania, ugotowałem jej trochę za dużo...",
				new MultipleActions(actions));

				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new QuestCompletedCondition(GLUT1))),
				ConversationStates.ATTENDING, 
				"Zapomniałeś, co mi obiecałeś? "
				+ "Miałeś iść do... glutów... i z nimi porozmawiać.",
				null);
				
				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new QuestCompletedCondition(GLUT2))),
				ConversationStates.ATTENDING, 
				"Zapomniałeś, co mi obiecałeś? "
				+ "Miałeś iść do... glutów... i z nimi porozmawiać.",
				null);
				
				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new QuestCompletedCondition(GLUT3))),
				ConversationStates.ATTENDING, 
				"Zapomniałeś, co mi obiecałeś? "
				+ "Miałeś iść do... glutów... i z nimi porozmawiać.",
				null);
				
				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new QuestCompletedCondition(GLUT4))),
				ConversationStates.ATTENDING, 
				"Zapomniałeś, co mi obiecałeś? "
				+ "Miałeś iść do... glutów... i z nimi porozmawiać.",
				null);
				
				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Jak już zdecydowałeś się mi pomóc to mam dla ciebie jeszcze jedną sprawę... "
				+ "Niedaleko stąd lata złoty smok. Boje się, że gdy nie będe patrzył, spali mi dom. Idź i go zabij!",
				null);
		
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Problem z glutami",
				"Kreg ma bardzo... nietypowy problem...",
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
				res.add("Muszę wybrać się do podziemii Krega i porozmawiać z glutami.");
			} else if(isRepeatable(player)){
				res.add("Błąd gry. Zgłoś go administracji.");
			} else {
				res.add("Pomogłem Kregowi lecz gluty dalej nie chcą opuścić jego piwnicy... w każdym bądź razie dostałem trochę kamieni szlachetnych.");
			}
			return res;
	}


	@Override
	public String getName() {
		return "KillGlut";
	}
	
	@Override
	public int getMinLevel() {
		return 450; 
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"done").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Kreg";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	
}
