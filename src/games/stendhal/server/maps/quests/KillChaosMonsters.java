/* $Id: KillChaosMonsters.java,v 1.18 2024/05/18 23:31:42 davvids Exp $ */
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
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
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

/**
 * QUEST: Kill Gnomes
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li> Ivar, by the mill in Semos Plains
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Gnomes have been stealing carrots so Ivar asks you to kill some.
 * <li> You go kill the Gnomes in the gnome village and you get the reward from Ivar
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

public class KillChaosMonsters extends AbstractQuest {

	private static final String QUEST_SLOT = "kill_chaos_monsters";
	private static final int WEEK_IN_MINUTES = MathHelper.MINUTES_IN_ONE_HOUR * 9999 * 9999;
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Ivar");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new LevelGreaterThanCondition(199),
				new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.QUEST_OFFERED,
				"Sytuacja w tunelach chaosu wymyka się spod kontroli. "
				+ "Potwory atakują naszych kupców i destabilizują finanse miasta. Potrzebuję kogoś odważnego, kto wejdzie do tych tuneli i pokona potwory. Czy jesteś gotów podjąć się tego wyzwania?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"Jeszcze raz dziękuje za pomoc z potworami w tunelach Ados! Bardzo nam pomogłeś.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES))),
				ConversationStates.ATTENDING,
				"Jeszcze raz dziękuje za pomoc z potworami w tunelach Ados! Bardzo nam pomogłeś.",
				null);

		final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("superczłowiek olbrzym", new Pair<Integer, Integer>(0,1));
		toKill.put("jeździec chaosu na czerwonym smoku", new Pair<Integer, Integer>(0,2));
		toKill.put("jeździec chaosu na zielonym smoku", new Pair<Integer, Integer>(0,4));
		toKill.put("czarny szkielet gigant",new Pair<Integer, Integer>(0,1));
		toKill.put("kostucha wielka",new Pair<Integer, Integer>(0,1));
		toKill.put("krasnal golem",new Pair<Integer, Integer>(0,4));
		toKill.put("upadły anioł",new Pair<Integer, Integer>(0,1));
		toKill.put("czarny olbrzym",new Pair<Integer, Integer>(0,3));
		toKill.put("olbrzymi ptasznik",new Pair<Integer, Integer>(0,1));
		toKill.put("złoty smok",new Pair<Integer, Integer>(0,1));
		toKill.put("czarny smok",new Pair<Integer, Integer>(0,1));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Doskonale. Dziękuję za podjęcie się tego wyzwania. Idź i przeszukaj wszystkie tunele chaosu, pokonując najsilniejsze potwory: superczłowieka olbrzyma, 2 jeźdźców chaosu na czerwonym smoku, 4 jeźdźców chaosu na zielonym smoku, czarnego szkieleta giganta, kostuchę wielką, 4 krasnale golemy, upadłego anioła, 3 czarne olbrzymy, olbrzymiego ptasznika, złotego smoka i czarnego smoka. I ogromnego szkieleta gashadokuro... nie, gashadokuro odpuścimy... Powodzenia!",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED, 
				ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING,
				"Rozumiem, że nie chcesz podjąć się tego zadania. To ogromne wyzwanie i nie każdy jest na nie gotowy.. "
				+ "Znajdę kogoś innego, kto może będzie miał odwagę zmierzyć się z tymi potworami. Jeśli zmienisz zdanie, wróć do mnie. Twoja pomoc jest naprawdę potrzebna. ",
				new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Ivar");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseXPAction(500000));
		actions.add(new IncreaseAtkXPAction(20000));
		actions.add(new IncreaseDefXPAction(20000));

		for (int i = 0; i < 10; i++) {
			actions.add(new EquipItemAction("zwój chaosu"));
		}

		actions.add(new SetQuestAction(QUEST_SLOT, "done;1"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 10));
		
		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);		
		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.ATTENDING, 
				"Widzę, że udało ci się zabić potwory w tunelach chaosu! Dziękuję ci w imieniu Ados, bez twojej pomocy nie poradzilibyśmy sobie.  "
				+ "W nagrodę weź te zwoje do jaskiń chaosów. I tak nam tu zalegają... Powodzenia, dzielny wojowniku!",
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Musisz odnaleźć tunele chaosu pod Ados i pokonać znajdujące się tam potwory. "
				+ "Upewnij się, że zabiłeś wszystkie: superczłowiek olbrzym, 2 jeźdźców chaosu na czerwonym smoku, 4 jeźdźców chaosu na zielonym smoku, czarny szkielet gigant, kostucha wielka, 4 krasnale golemy, upadły anioł, 3 czarne olbrzymy, olbrzymi ptasznik, złoty smok, czarny smok. I ogromnego szkieleta gashadokuro... nie, gashadokuro mieliśmy odpuscić... Powodzenia",
				null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Potwory w tunelach Chaosu",
				"Ivar, urzędnik z ratusza Ados, ma poważne problemy z potworami w tunelach chaosu, które destabilizują finanse miasta przez atakowanie kupców. Prosi cię o pomoc w eliminacji tych zagrożeń. Musisz udać się do tuneli chaosu pod Ados i pokonać określone potwory.",
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
				res.add("Muszę zabić wszystkie potwory w tunelach chaosu: superczłowieka olbrzyma, 2 jeźdźców chaosu na czerwonym smoku, 4 jeźdźców chaosu na zielonym smoku, czarnego szkieleta giganta, kostuchę wielką, 4 krasnale golemy, upadłego anioła, 3 czarne olbrzymy, olbrzymiego ptasznika, złotego smoka i czarnego smoka. I ogromnego szkieleta gashadokuro... nie, gashadokuro mieliśmy odpuścić..");
			} else if(isRepeatable(player)){
				res.add("Jeszcze raz dziękuje za pomoc z potworami w tunelach Ados! Bardzo nam pomogłeś.");
			} else {
				res.add("Odnalazłem tunele chaosu i pokonałem wszystkie znajdujące się tam potwory.	W nagrodę za ukończenie zadania otrzymałem: 500 000 pkt doświadczenia, 20 000 pkt ataku, 20 000 pkt obrony oraz 10 zwoi chaosu.");
			}
			return res;
	}


	@Override
	public String getName() {
		return "KillChaosMonsters";
	}
	
	@Override
	public int getMinLevel() {
		return 200;
	}
	
	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
				 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)).fire(player,null, null);
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"done").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Ivar";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	
}
