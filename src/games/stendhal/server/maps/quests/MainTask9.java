/* $Id: MainTask9.java,v 1.18 2020/12/02 20:22:42 davvids Exp $ */
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
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import marauroa.common.Pair;

public class MainTask9 extends AbstractQuest {

	private static final String QUEST_SLOT = "main_task_9";
	
	private static final String EASYMONSTERS = "kill_easy_monsters";
	private static final String NALWORBADGE = "nalwor_badge";
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Mistrz Faumonii IX");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new LevelGreaterThanCondition(59)),
				ConversationStates.QUEST_OFFERED,
				"Witaj, mam dla ciebie główne zadanie dziewiątego poziomu."
				+ " Jesteś zainteresowany?",
				null);

		final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("zielony smok", new Pair<Integer, Integer>(0,5));
		toKill.put("błękitny smok", new Pair<Integer, Integer>(0,3));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Czasz abyś pokazał, że umiesz walczyć. Zanim przyznam ci główne zadanie, idź do hotelu w mieście Fado i pomóż Lindzie. Następnie odwiedź miasto Nalwor i wykonaj zadania u Urzędnika Nalwor aby uzyskać status zasłużonego mieszkańca elfickiego miasta. Miasto Nalwor zlokalizowane jest w środku lasu na południe od Semos. Jak już to zrobisz to zabij 5 zielonych smoków oraz 3 błękitne smoki aby udowodnić swoje męstwo. Smoki znajdziesz w wielu jaskiniach w całej krainie Faumonii, podziemia Fado słyną ze smoczych siedlisk lecz uważaj, jest tam bardzo niebezpiecznie. Na co czekasz? Do roboty!",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED, 
				ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING,
				"Nie ma problemu. "
				+ "Wróć gdy zdecydujesz się zostać wojownikiem Faumonii. ",
				new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Mistrz Faumonii IX");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new DropItemAction("odznaka wojownika VIII",1));
	    actions.add(new EquipItemAction("odznaka wojownika IX", 1));
		actions.add(new SetQuestAction(QUEST_SLOT, "done;1"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		
		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);		
		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new QuestCompletedCondition(EASYMONSTERS),
						new QuestCompletedCondition(NALWORBADGE),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.ATTENDING, 
				"Widze, że wykonałeś swoje dziewiąte zadanie, poznałeś miasto Nalwor i pomogłeś tamtejszym mieszkańcom, zabiłeś potwory na zlecenie hotelarki w mieście Fado oraz pokonałeś smoki. "
				+ "Zasługujesz na odznakę wojownika Faumonii dziewiątego poziomu, tym razem zwiększy ona poziom twojego ataku. Po nastepne główne zadanie powinieneś przyjść do Mistrza Faumonii X po przekroczeniu 70 poziomu doświadczenia.",
				new MultipleActions(actions));

				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new QuestCompletedCondition(EASYMONSTERS))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Powinieneś odwiedzić hotelarke Linde w Fado i pomóc jej z grupą rzezimieszków.",
				null);
				
				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new QuestCompletedCondition(NALWORBADGE))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Powinieneś odwiedzić urzędnika Nalwor w elfickim mieście i wykonać dla niego zadania aby uzyskać pozwolenie na zakup zwoi do miasta, przy okazji trochę powalczysz.",
				null);
				
				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Musisz udowodnić swoje męstwo zabijając smoki. Idź i zabij: 5 zielonychi 3 błękitne smoki. Znajdziesz je w wielu jaskiniach w całej krainie Faumonii. Jaskinie pod Fado słyną z wielu smoczych siedlisk, lecz uważaj, gdyż jest tam bardzo niebezpiecznie!",
				null);
		
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Wojownik Faumonii 9",
				"Mistrz Faumonii IX ma dla ciebie dziewiąte główne zadanie które pomoże ci zostać wielkim wojownikiem Faumonii.",
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
				res.add("Muszę odnaleźć elfickie miasto, zlokalizowane w lesie na południe od Semos i wykonać zadanie u Urzędnika Nalwor aby móc kupować zwoje.	Powinienem znowu wybrać się do Fado i pomóc hotelarce Lindzie z rzezimieszkami.	Następnie muszę udowodnić swoje męstwo i zabić 5 zielonych smoków oraz 3 błękitne smoki. Smoki powinienem znaleźć w jaskiniach, podobno jaskinie pod Fado słyną z dużej ilości smoczych siedlisk, lecz jest tam bardzo niebezpiecznie.");
			} else if(isRepeatable(player)){
				res.add("Błąd gry. Zgłoś go administracji.");
			} else {
				res.add("Ukończyłem dziewiąte zadanie od Mistrza Faumonii i otrzymałem odznakę wojownika Faumonii dziewiątego poziomu.	Następne główne zadanie otrzymam po przekroczeniu 70 poziomu doświadczenia.");
			}
			return res;
	}


	@Override
	public String getName() {
		return "MainTask9";
	}
	
	@Override
	public int getMinLevel() {
		return 60;
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"done").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Mistrz Faumonii IX";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	
}
