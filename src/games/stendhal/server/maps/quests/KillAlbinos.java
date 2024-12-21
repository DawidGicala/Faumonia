/* $Id: KillAlbinos.java,v 1.18 2012/04/19 18:26:42 kymara Exp $ */
/***************************************************************************
 *                   (C) Copyright 503-2010 - Stendhal                    *
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
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.npc.action.EquipItemAction;
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
 * <li> Orchiwald, by the mill in Semos Plains
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Gnomes have been stealing carrots so Orchiwald asks you to kill some.
 * <li> You go kill the Gnomes in the gnome village and you get the reward from Orchiwald
 * </ul>
 * <p>
 * REWARD:
 * <ul>
 * <li> 3 potions
 * <li> 40 XP
 * <li> No karma (deliberately. Killing gnomes is mean!)
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li> after 7 days.
 * </ul>
 */

public class KillAlbinos extends AbstractQuest {

	private static final String QUEST_SLOT = "kill_albinos";
	private static final int WEEK_IN_MINUTES = MathHelper.MINUTES_IN_ONE_HOUR * 24 * 28;
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Orchiwald");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Ostatnio banda albinosów ukradła wszystkie moje zwoje, teraz nie moge ich sprzedawać... i to mimo tego, że też jestem albinosem! "
				+ "Szukam kogoś kto pomoże mi się zemścić. Pomożesz?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"Ta banda znowu wróciła i napadła na mnie. Czas na zemste! Pomożesz?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES))),
				ConversationStates.ATTENDING,
				"Tamta banda nie sprawia problemu od momentu, gdy dałeś im dobrą lekcje.",
				null);

		final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("elf albinos rycerz", new Pair<Integer, Integer>(0,50));
		toKill.put("elf albinos łucznik", new Pair<Integer, Integer>(0,40));
		toKill.put("elf albinos magik", new Pair<Integer, Integer>(0,20));
		toKill.put("elf albinos królowa", new Pair<Integer, Integer>(0,20));
		toKill.put("elf albinos król", new Pair<Integer, Integer>(0,20));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Doskonale. Zapewne siedzą w jakiejś grupce w pobliżu. Upewnij się, że zabiłeś 50 elfów albinosów rycerzy, 40 elfów albinosów łuczników, 20 elfów albinosów magików, 20 elfów albinosów królowych i 20 elfów albinosów królów.",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED, 
				ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING,
				"Masz rację. "
				+ "Chyba poszukam kogoś odpowiedniejszego. ",
				new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Orchiwald");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseXPAction(200000));
		actions.add(new SetQuestAction(QUEST_SLOT, "done;1"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		actions.add(new EquipItemAction("bełt", 1500, true));
		actions.add(new EquipItemAction("bełt stalowy", 1000, true));
		actions.add(new EquipItemAction("bełt złoty", 500, true));
		actions.add(new EquipItemAction("bełt płonący", 200, true));
		actions.add(new EquipItemAction("skrzynia skarbów III", 1, true));
		actions.add(new EquipItemAction("skrzynia skarbów VI", 1, true));
		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);		
		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.ATTENDING, 
				"Widzę, że ich zabiłeś. Mam nadzieje, że przez jakiś czas będzie tutaj spokój! "
				+ "Proszę weź te bełty w dowód uznania, to jedyne co zdołałem ukryć...",
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Musisz nauczyć bande elfów albinosów, że nie powinni atakować swoich braci! "
				+ "Upewnij się, że zabiłeś 50 elfów albinosów rycerzy, 40 elfów albinosów łuczników, 20 elfów albinosów magików, 20 elfów albinosów królowych, 20 elfów albinosów królów.",
				null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Zabij Elfy Albinosy",
				"Grupka elfów albinosów napadła swojego brata Orchiwalda i ukradła mu wszystkie zwoje. Potrzebuje kogoś do pomocy aby dokonać zemsty.",
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
				res.add("Muszę zabić 50 elfów albinosów rycerzy, 40 elfów albinosów łuczników, 20 elfów albinosów magików, 20 elfów albinosów królowch, 20 elfów albinosów króli.");
			} else if(isRepeatable(player)){
				res.add("Te zuchwałe elfy albinosy, zapomniały lekcji jaką im dałem. Znowu go napadły! Orchiwald potrzebuje mojej pomocy.");
			} else {
				res.add("Pomogłem Orchiwaldowi. Albinosy trzymają się teraz zdala od niego.	Za ukończenie zadania otrzymałem: 200.000 pkt doświadczenia i trochę kamieni szlachetnych.");
			}
			return res;
	}


	@Override
	public String getName() {
		return "KillAlbinos";
	}
	
	@Override
	public int getMinLevel() {
		return 110;
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
		return "Orchiwald";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	
}
