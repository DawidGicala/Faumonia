/* $Id: MainTask10.java,v 1.18 2020/12/03 9:33:17 davvids Exp $ */
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

public class MainTask10 extends AbstractQuest {

	private static final String QUEST_SLOT = "main_task_10";
	
	private static final String ANGELS = "main_task_9";
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Mistrz Faumonii X");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new LevelGreaterThanCondition(69)),
				ConversationStates.QUEST_OFFERED,
				"Witaj, mam dla ciebie główne zadanie dziesiątego poziomu."
				+ " Jesteś zainteresowany?",
				null);

		final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("nimfa", new Pair<Integer, Integer>(0,10));
		toKill.put("żywioł ziemi", new Pair<Integer, Integer>(0,25));
		toKill.put("żywioł ognia", new Pair<Integer, Integer>(0,25));
		toKill.put("żywioł wody", new Pair<Integer, Integer>(0,25));
		toKill.put("żywioł powietrza", new Pair<Integer, Integer>(0,25));
		toKill.put("żywioł lodu", new Pair<Integer, Integer>(0,25));
		toKill.put("dżin", new Pair<Integer, Integer>(0,10));
		toKill.put("licho", new Pair<Integer, Integer>(0,10));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Znasz krainę Faumonii już wystarczająco dobrze aby rozpocząć to zadanie. Po świecie kraży 7 aniołków. Twoim zadaniem jest odnalezienie każdego z nich. Pierwszy znajduje się w naszym mieście Semos. Jeśli nie wiesz gdzie szukać, sprawdź dziennik zadań. Następnie musisz wybrać się na dziką wyspę. Dotrzesz do niej podziemnymi tunelami w kopalni Semos. Wróć do Krasnala Phalka i idź w północnym kierunku a powinieneś trafić. Kolejno odnajdź obozowisko żywiołaków na tej wyspie i powalcz, w ten sposób pozyskasz cenną magię, do niej jeszcze wrócimy. Uważaj! Wyspa jest niebezpieczna, jeśli zabłądzisz to łatwo zginiesz! Później musisz wybrać się do zamku Orril, znajdziesz go idąć w kierunku miasta Fado na południowy-wschód od Semos. Odnajdź przejście, pokonaj smoka, wejdź do krypty i pokonaj trochę lichów! Zabij po 25 żywiołów: ognia, wody, powietrza i lodu, 10 nimf, 10 dżinów i 10 lichów. Na co czekasz? Do roboty!",
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

		final SpeakerNPC npc = npcs.get("Mistrz Faumonii X");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new DropItemAction("odznaka wojownika IX",1));
	    actions.add(new EquipItemAction("odznaka wojownika X", 1));
		actions.add(new SetQuestAction(QUEST_SLOT, "done;1"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		
		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);		
		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new QuestCompletedCondition(ANGELS),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.ATTENDING, 
				"Widze, że wykonałeś swoje dziesiąte zadanie. Odnalazłeś aniołki, pokonałeś żywiołaki i licha. Zdobytą magię zachowaj lub sprzedaj komuś, jest bardzo cenna."
				+ "Zasługujesz na odznakę wojownika Faumonii dziesiątego poziomu. Po nastepne główne zadanie powinieneś przyjść do Mistrza Faumonii XI po przekroczeniu 75 poziomu doświadczenia.",
				new MultipleActions(actions));

				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new QuestCompletedCondition(ANGELS))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Powinieneś odnaleźć 7 aniołków krążących po naszej krainie. Pierwszego znajdziesz w mieście Semos. Jeśli nie wiesz gdzie szukać reszty, sprawdź dziennik zadań.",
				null);
				
				npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Zapomniałes jakie zadanie dostałeś? "
				+ "Miałeś wybrać się na dziką wyspę oraz do pałacu Licha. Na wyspie trafisz wędrując tunelami w podziemiach, wróć do krasnala Phalka i kieruj się na północ a trafisz. Odnajdź obozowisko żywiołaków i wybij je! Uważaj! Wyspa jest bardzo niebezpieczna, jeśli zabłądzisz to łatwo zginiesz! Następnie miałeś odnaleźć pałac Licha zlokalizowany w podziemiach zamku Orril. Zamek Orril odnajdziesz kierując się na południowy-wschód w kierunku Fado. Musisz zabić: 25 żywiołów ziemi, 25 żywiołów ognia, 25 żywiołów wody, 25 żywiołów powietrza, 25 żywiołów lodu, 10 dżinów, 10 nimf oraz 10 lichów.",
				null);
		
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Wojownik Faumonii 10",
				"Mistrz Faumonii X ma dla ciebie dziesiąte główne zadanie które pomoże ci zostać wielkim wojownikiem Faumonii.",
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
				res.add("Muszę odnaleźć 7 aniołków krażących po Faumonii, podobno pierwszy jest w mieście Semos.	Następnie muszę zabić żywiołaki na dzikiej wyspie, mogę się na nią dostać podróżując po tunelach w kopalnii Semos, idąc na północ od znanego już mi krasnala Phalka.	Kolejno muszę odnaleźć podziemny pałac Licha i pokonać kilku tamtejszych mieszkańcow. Podobno pałac zlokalizowany jest w zamku Orril który znajdę kierując się na południowy-wschód od Semos, na drodzę do Fado.	Muszę zabić: 25 żywiołów ziemi, 25 żywiołów ognia, 25 żywiołów wody, 25 żywiołów powietrza, 25 żywiołów lodu, 10 nimf, 10 dżinów i 10 lichów.");
			} else if(isRepeatable(player)){
				res.add("Błąd gry. Zgłoś go administracji.");
			} else {
				res.add("Ukończyłem dziesiąte zadanie od Mistrza Faumonii i otrzymałem odznakę wojownika Faumonii dziesiątego poziomu.	Następne główne zadanie otrzymam po przekroczeniu 75 poziomu doświadczenia.");
			}
			return res;
	}


	@Override
	public String getName() {
		return "MainTask10";
	}
	
	@Override
	public int getMinLevel() {
		return 70;
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"done").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Mistrz Faumonii X";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	
}
