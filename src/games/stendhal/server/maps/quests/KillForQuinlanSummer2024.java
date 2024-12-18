/* $Id: KillForQuinlanSummer2024Summer2024.java,v 1.18 2024/06/29 14:26:42 davvids Exp $ */
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


public class KillForQuinlanSummer2024 extends AbstractQuest {

	private static final String QUEST_SLOT = "kill_quinlan";
	private static final int WEEK_IN_MINUTES = MathHelper.MINUTES_IN_ONE_HOUR * 24 * 7 * 4 * 6;
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Quinlan");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Szukam dzielnego wojownika, który wybierze się w najdalsze zakątki tej krainy i pokona kilka potworów. "
				+ "Czy chciałbyś pomóc?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
						 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"Kolejny raz chciałym, aby ktoś pokonał różne potwory. Może znów mi pomożesz?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
						 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES))),
				ConversationStates.ATTENDING,
				"Na ten moment nie potrzebuje Twojej pomocy. Dziękuje!",
				null);

		
		final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("karzełek amarantowy", new Pair<Integer, Integer>(0,15));
		toKill.put("kobold żołnierz", new Pair<Integer, Integer>(0,15));
		toKill.put("człekoszczur", new Pair<Integer, Integer>(0,15));
		toKill.put("risecia zbir", new Pair<Integer, Integer>(0,15));
		toKill.put("gospodyni z kalavan", new Pair<Integer, Integer>(0,15));
		toKill.put("duch elfa", new Pair<Integer, Integer>(0,15));
		toKill.put("zbójnik leśny", new Pair<Integer, Integer>(0,15));
		toKill.put("ork wojownik", new Pair<Integer, Integer>(0,15));
		toKill.put("żabiczłek", new Pair<Integer, Integer>(0,15));
		toKill.put("samurai", new Pair<Integer, Integer>(0,15));
		toKill.put("zielony smok", new Pair<Integer, Integer>(0,3));
		
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Dziękuję! Pokonaj 20 karzełków amarantowych, 20 koboldów żołnierzy, 20 człekoszczurów, 20 risecia zbirów, 20 gospodyni z kalavan, 20 duchów elfa, 20 zbójników leśnych, 20 orków wojowników, 20 żabiczłeków, 20 samurajów oraz 3 zielone smoki. W nagrodę za Twoją pomoc, dam Ci mój pustynny ekwipunek który znaczaco ułatwi Ci rozgrywkę! Jeżeli nie wiesz gdzie szukać konkretnych potworów to zapytaj innych graczy na chacie globalnym pisząc #/k #wiadomość lub porozmawiaj ze Starkadem w banku Semos ",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED, 
				ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING,
				"Nie chcesz pustynnego ekwipunku to nie... "
				+ "Twój wybór.. ",
				new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Quinlan");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new EquipItemAction("hełm pustynny 2024", 1));
		actions.add(new EquipItemAction("zbroja pustynna 2024", 1));
		actions.add(new EquipItemAction("spodnie pustynne 2024", 1));
		actions.add(new EquipItemAction("buty pustynne 2024", 1));
		actions.add(new EquipItemAction("tarcza pustynna 2024", 1));
		actions.add(new EquipItemAction("płaszcz pustynny 2024", 1));
		actions.add(new EquipItemAction("sztylet pustynny 2024", 1));
		actions.add(new IncreaseXPAction(100000));
		actions.add(new SetQuestAction(QUEST_SLOT, "done;1"));
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
				"Dziękuje za pomoc. Trochę Ci to zajeło, ale... "
				+ "W ramach podziękowania przyjmij mój pustynny ekwipunek. Powinien pomóc Ci przetrwać w tym upale!",
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Zapomniałeś co miałeś zrobić? "
				+ "Miałeś pokonać 20 karzełków amarantowych, 20 koboldów zołnierzy, 20 człekoszczurów, 20 risecia zbirów, 20 gospodyń z kalavan, 20 duchów elfa, 20 zbójników leśnych, 20 orków wojowników, 20 żabiczłeków, 20 samurajów oraz 3 zielone smoki. W nagrodę za ukończenie zadania, dam Ci swój cenny ekwipunek pustynny, który pomoże Ci przetrwać w tym upale. Jeżeli nie wiesz gdzie szukać konkretnych potworów to zapytaj innych graczy na chacie globalnym pisząc #/k #wiadomość lub porozmawiaj ze Starkadem w banku Semos",
				null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Pustynny ekwipunek",
				"Quinlan poszukuje kogoś kto pokona dla niego troche potworów. W nagrode obiecał bardzo cenny ekwipunek.",
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
				res.add("Musze zabić 20 karzełków amarantowych, 20 koboldów żołnierzy, 20 człekoszczurów, 20 risecia zbirów, 20 gospodyni z kalavan, 20 duchów elfa, 20 zbójników leśnych, 20 orków wojowników, 20 żabiczłeków, 20 samurajów oraz 3 zielone smoki.");
			} else if(isRepeatable(player)){
				res.add("Chyba powinienem znowu wrócić do Quinlana.");
			} else {
				res.add("Pomogłem Quinlanowi z potworami. W nagrode otrzymałem cenny ekwipunek pustynny.");
			}
			return res;
	}


	@Override
	public String getName() {
		return "KillForQuinlanSummer2024";
	}
	
	@Override
	public int getMinLevel() {
		return 1;
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
		return "Quinlan";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	
}
