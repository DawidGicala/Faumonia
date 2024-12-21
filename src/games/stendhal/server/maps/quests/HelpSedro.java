/* $Id: HelpSedro.java,v 1.18 2020/11/01 15:35:42 davvids Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2020 - Stendhal                    *
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

public class HelpSedro extends AbstractQuest {

	private static final String QUEST_SLOT = "help_sedro";
	private static final int WEEK_IN_MINUTES = MathHelper.MINUTES_IN_ONE_HOUR * 999 * 999;
 
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Sedro");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Poszukuje kogoś kto mi pomoże i pozabija trochę potworów. Wszystkie znajdują się w pobliżu. Mógłbym to zrobić sam ale ze względu na wiek... sam wiesz.."
				+ "Jeżeli chcesz mi pomóc napisz #tak",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"Kolejny raz szukam kogoś kto pozabija trochę potworów. Może znów mi pomożesz?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
						 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES))),
				ConversationStates.ATTENDING,
				"Na ten moment minął problem z potworami. Dziekuje, nie potrzebuje twojej pomocy. Powinieneś wybrać się do Mistrza Faumonii mieszkającego w tym czerwonym domku za mną, nauczy cię on podstaw gry i zleci trochę zadań.",
				null);

		final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("szczur", new Pair<Integer, Integer>(0,10));
		toKill.put("szczur jaskiniowy", new Pair<Integer, Integer>(0,5));
		toKill.put("gnom",new Pair<Integer, Integer>(0,3));
		toKill.put("panna gnom",new Pair<Integer, Integer>(0,3));
		toKill.put("wilk",new Pair<Integer, Integer>(0,5));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Dziękuję! Zabij 10 szczurów, 5 szczurów jaskiniowych, 3 gnomy, 3 panny gnomy oraz 5 wilków. Szczury znajdziesz w mieście oraz w podziemiach. Wilki znajdziesz na mapach pod i nad miastem. Gnomy czekają w niebieskiej jaskini pod domkiem startowym oraz w swojej wiosce na północny-wschod od miasta Semos. Jeżeli masz problem ze znalezieniem potworów to Starkad spacerujący po banku w Semos ci pomoże. Bank Semos znajduje się w zielonym budynku na lewo od fontanny w centrum miasta. Pamiętaj o tym, że o pomoc zawsze możesz poprosić innych graczy. W takiej sytuacji napisz #/k #wiadomość. Stan swojego zadania możesz sprawdzić klikając w prawym górnym rogu #Menu oraz #Dziennik #Zadań",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED, 
				ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING,
				"Co za szkoda... Może kiedyś zmienisz zdanie i pomożesz starcowi... "
				+ "Może poradze sobie sam.. ",
				new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Sedro");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
	    actions.add(new EquipItemAction("zwój semos", 10));
		actions.add(new EquipItemAction("eliksir", 10));
		actions.add(new EquipItemAction("duży eliksir", 5));
		actions.add(new EquipItemAction("butelka wody", 20));
		actions.add(new EquipItemAction("sztylecik", 1));
		actions.add(new EquipItemAction("maczeta", 1));
		actions.add(new EquipItemAction("pałasz", 1));
		actions.add(new EquipItemAction("drewniana tarcza", 1));
		actions.add(new EquipItemAction("mięso", 50));
		actions.add(new IncreaseXPAction(500));
		actions.add(new IncreaseAtkXPAction(500));
		actions.add(new IncreaseDefXPAction(500));
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
				"Widzę, że zabiłeś potwory. Mam nadzieje, że przez jakiś czas będzie z nimi spokój. "
				+ "Proszę przyjmij te przedmioty, na pewno pomogą ci na poczatku rozgrywki. Jeśli nie wiesz co robi dany przedmiot kliknij na niego prawym przyciskiem myszy następnie 'zobacz'. Statystyki wszystkich przedmiotów dostępnych w grze możesz sprawdzić klikając w #Menu w prawym górnym rogu ekranu a następnie #Lista #przedmiotów",
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Proszę pomóż mi. "
				+ "W dalszym ciągu nie zabiłeś wszystkich potworów... Miałeś zabić 10 szczurów, 5 szczurów jaskiniowych, 3 gnomy, 3 panny gnomy i 5 wilków. Szczury znajdziesz w mieście oraz w podziemiach. Wilki znajdziesz na mapach pod i nad miastem. Gnomy czekają w jaskini pod domkiem startowym oraz w swojej wiosce na północny-wschod od miasta Semos. Jeżeli masz problem ze znalezieniem potworów powinieneś odwiedzić bank i porozmawiać ze spacerującym tam Starkadem. Bank Semos znajdziesz w zielonym budynku na lewo od fontanny w centrum miasta. Pamiętaj o tym, że o pomoc zawsze możesz poprosić innych graczy. W takiej sytuacji napisz #/k #wiadomość. Stan swojego zadania możesz sprawdzić klikając w prawym górnym rogu #Menu oraz #Dziennik #Zadań",
				null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Pomóż Sedro",
				"Sedro szuka kogoś kto mu pomoże i pozabija trochę potworów.",
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
				res.add("Musze zabić 10 szczurów, 5 szczurów jaskiniowych, 3 gnomy, 3 panny gnomy oraz 5 wilków. Szczury znajde w mieście oraz w podziemiach po środku miasta Semos. Wilki znajde na mapach pod i nad miastem. Gnomy czekają na mnie w jaskini pod domkiem startowym oraz w swojej wiosce na północny-wschod od miasta Semos.	Może powinienem porozmawiać ze spacerującym w banku Starkadem. Podobno bank znajdę w zielonym budynku na lewo od fontanny w centrum miasta.	Dowiedziałem się, że zawsze mogę poprosić o pomoc innych graczy. W takiej sytuacji mam pisać #/k #wiadomość");
			} else if(isRepeatable(player)){
				res.add("Teraz po czasie powinienem znowu odwiedzić Sedro. Może znów potrzebuje pomocy, w końcu ma już troche lat...");
			} else {
				res.add("Potwory zostały zgładzone. Sedro dał mi w nagrode podstawowy ekwipunek i troche jedzenia, które bardzo mi sie przyda na samym poczatku mojej wedrówki.	W nagrodę za ukończenie zadania otrzymałem: 500 pkt doświadczenia, 500 pkt ataku, 500 pkt obrony, 10 zwoji semos, 10 eliksirów, 5 dużych eliksirów, 20 butelek wody, 50 mięsa, sztylecik, maczetę, pałasz i drewnianą tarczę.");
			}
			return res;
	}


	@Override
	public String getName() {
		return "HelpSedro";
	}
	
	@Override
	public int getMinLevel() {
		return 1;
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
		return "Sedro";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	
}
