/* $Id: KillMag.java,v 1.18 2020/12/04 12:55:17 davvids Exp $ */
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
 * <li> Staro, by the mill in Semos Plains
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Gnomes have been stealing carrots so Staro asks you to kill some.
 * <li> You go kill the Gnomes in the gnome village and you get the reward from Staro
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

public class KillMag extends AbstractQuest {

	private static final String QUEST_SLOT = "kill_mag";
	private static final int WEEK_IN_MINUTES = MathHelper.MINUTES_IN_ONE_HOUR * 24 * 7;
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Staro");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Dawno temu byłem na jakiejś bardzo dziwnej wyspie. Znajdowali się na niej ludzie którzy władali magią. "
				+ "Szukam kogoś kto wybierze się na tą wyspę i ją zbada. Pomożesz?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"Problem z magami został rozwiązany. Dziękuje za Twoją pomoc!",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES))),
				ConversationStates.ATTENDING,
				"Na tą chwile niczego nie potrzebuje.",
				null);

		final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("pustelnik", new Pair<Integer, Integer>(0,20));
		toKill.put("uczeń czarnoksiężnika", new Pair<Integer, Integer>(0,10));
		toKill.put("czarnoksiężnik",new Pair<Integer, Integer>(0,10));
		toKill.put("uczeń czarnoksiężnika mroku",new Pair<Integer, Integer>(0,10));
		toKill.put("czarnoksiężnik mroku",new Pair<Integer, Integer>(0,10));
		toKill.put("czarownica z Aenye",new Pair<Integer, Integer>(0,3));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Doskonale. Odnajdź tę wyspę i zabij: 20 pustelników, 10 uczniów czarnoksiężnika, 10 uczniów czarnoksiężnika mroku, 10 czarnoksiężników, 10 czarnoksiężników mroku i 3 czarownice z Aenye.",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED, 
				ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING,
				"Trudno. "
				+ "Może kiedyś się zdecydujesz. ",
				new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Staro");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseXPAction(250000));
		actions.add(new SetQuestAction(QUEST_SLOT, "done;1"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		actions.add(new EquipItemAction("magia światła", 200));
		actions.add(new EquipItemAction("magia mroku", 400));
		actions.add(new EquipItemAction("magia płomieni", 600));
		actions.add(new EquipItemAction("magia deszczu", 600));
		actions.add(new EquipItemAction("magia ziemi", 1000));	
		actions.add(new EquipItemAction("skrzynia skarbów VI", 1));			
		actions.add(new EquipItemAction("skrzynia skarbów VI", 1));		
		actions.add(new EquipItemAction("skrzynia skarbów VII", 1));		
		actions.add(new EquipItemAction("skrzynia skarbów VII", 1));		
		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);		
		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.ATTENDING, 
				"Widzę, że zabiłeś czarnoksieżniów. W międzyczasie dowiedziałem się w jaki sposób posługują się oni magią. Prawdopodobnie znajdują się gdzieś magiczne różdzki które wraz z magią stanowią świetną broń dystansową, podobną do łuku czy kusz."
				+ " Proszę weź te kamienie szlachetne w ramach podziękowania.",
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Zapomniałeś juz? Obiecałeś, żę pójdziesz na wyspę i rozglądniesz się za ludźmi posługującymi się magią... "
				+ "Upewnij się, że zabiłeś 20 pustelników, 10 uczniów czarnoksiężnika, 10 uczniów czarnoksiężnika mroku, 10 czarnoksiężników, 10 czarnoksiężników mroku i 3 czarownice z Aenye.",
				null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Czarnoksieżnicy",
				"W tawernie Fado spotkałem mężczyzne imieniem Staro który opowiedział mi o odległej wyspie na której moge spotkać ludzi posługujących się magią.",
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
				res.add("Musze odnaleźć wyspę i zabić: 20 pustelników, 10 uczniów czarnoksiężnika, 10 uczniów czarnoksiężnika mroku, 10 czarnoksiężników, 10 czarnoksiężników mroku i 3 czarownice z Aenye.");
			} else if(isRepeatable(player)){
				res.add("Problem z magami został rozwiązany.");
			} else {
				res.add("Pokonałem czarnoksiężników i dowiedziałem się, że jeśli odnajdę magiczną różdżkę to wraz z magią będę mógł nią miotać na odległość!	W nagrodę za ukończenie zadania otrzymałem: 250.000 pkt doświadczenia i kilka kamieni szlachetnych.");
			}
			return res;
	}


	@Override
	public String getName() {
		return "KillMag";
	}
	
	@Override
	public int getMinLevel() {
		return 150;
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
		return "Staro";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	
}
