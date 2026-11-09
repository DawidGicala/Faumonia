/* $Id: HelpSedro.java,v 1.18 2026/10/05 23:45:42 davvids Exp $ */
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
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DialogueHintContextAction;
import games.stendhal.server.entity.npc.action.DialogueNarrationAction;
import games.stendhal.server.entity.npc.action.DialogueRewardAction;
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
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import marauroa.common.Pair;

public class HelpSedro extends AbstractQuest {

	private static final String QUEST_SLOT = "help_sedro";
	private static final int KILLS_INDEX = 1;
	private static final int WEEK_IN_MINUTES = MathHelper.MINUTES_IN_ONE_HOUR * 999 * 999;

	private static final String QUEST_REQUIREMENTS_TEXT = "1 #gnoma oraz 1 #pannę #gnom";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private String getKillProgress(final Player player) {
		final String killData = player.getQuest(QUEST_SLOT, KILLS_INDEX);

		if (killData == null) {
			return "nie można teraz wyświetlić postępu polowania; zgłoś problem administracji";
		}

		final List<String> tokens = Arrays.asList(killData.split(","));

		if ((tokens.size() % 5) != 0) {
			return "nie można teraz wyświetlić postępu polowania; zgłoś problem administracji";
		}

		final StringBuilder progress = new StringBuilder();

		for (int i = 0; i < tokens.size(); i += 5) {
			final String creatureName = tokens.get(i);

			final int requiredSoloKills = MathHelper.parseIntDefault(tokens.get(i + 1), 0);
			final int requiredSharedKills = MathHelper.parseIntDefault(tokens.get(i + 2), 0);
			final int initialSoloKills = MathHelper.parseIntDefault(tokens.get(i + 3), 0);
			final int initialSharedKills = MathHelper.parseIntDefault(tokens.get(i + 4), 0);

			final int currentSoloKills = player.getSoloKill(creatureName);
			final int currentSharedKills = player.getSharedKill(creatureName);

			final int soloKillsDone = Math.max(0, currentSoloKills - initialSoloKills);
			final int sharedKillsDone = Math.max(0, currentSoloKills + currentSharedKills - initialSoloKills - initialSharedKills);

			if (requiredSoloKills > 0) {
				appendKillProgress(progress, markCreatureName(creatureName) + " solo", soloKillsDone, requiredSoloKills);
			}

			if (requiredSharedKills > 0) {
				if (requiredSoloKills > 0) {
					appendKillProgress(progress, markCreatureName(creatureName) + " razem", sharedKillsDone, requiredSharedKills);
				} else {
					appendKillProgress(progress, markCreatureName(creatureName), sharedKillsDone, requiredSharedKills);
				}
			}
		}

		if (progress.length() == 0) {
			return "nie mam nic do zliczenia";
		}

		return progress.toString();
	}

	private void appendKillProgress(final StringBuilder progress, final String creatureName, final int killsDone, final int killsRequired) {
		final int safeKillsDone = Math.min(Math.max(killsDone, 0), killsRequired);
		final int killsLeft = Math.max(0, killsRequired - safeKillsDone);

		if (progress.length() > 0) {
			progress.append(", ");
		}

		progress.append(creatureName);
		progress.append(": ");
		progress.append(safeKillsDone);
		progress.append("/");
		progress.append(killsRequired);
		progress.append(" - zostało ");
		progress.append(killsLeft);
	}

	private String markCreatureName(final String creatureName) {
		return "#" + creatureName.replace(" ", " #");
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Sedro");
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "start;")),
				ConversationStates.ATTENDING, "Witaj ponownie. Jak idzie walka z potworami? Możemy omówić postęp zadania albo miejsca poszukiwań.", null);
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "done"),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES))),
				ConversationStates.ATTENDING, "Dobrze cię widzieć. Dzięki twojej pomocy jest teraz spokojniej. Na razie nie potrzebuję dalszej pomocy.", null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new OrCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestInStateCondition(QUEST_SLOT, "rejected")),
				ConversationStates.QUEST_OFFERED,
				"Gnomy z pobliskiej jaskini dają nam się we znaki. Sam bym się nimi zajął, ale nogi nie nadążają już za dobrymi chęciami. "
				+ "Pomożesz mi zaprowadzić trochę spokoju? W podzięce przygotuję dla ciebie ekwipunek na początek wędrówki.",
				new DialogueNarrationAction("Sedro opiera dłonie na kolanach."));

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"),
						 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"Potwory znów dają nam się we znaki. Pomożesz mi jeszcze raz? Jak poprzednio, odwdzięczę się ekwipunkiem.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"),
						 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES))),
				ConversationStates.ATTENDING,
				"Przez jakiś czas powinno być spokojniej i to także twoja zasługa. Na razie nie potrzebuję dalszej pomocy. Odpocznij i zadbaj o siebie przed kolejną wyprawą.",
				null);

		final Map<String, Pair<Integer, Integer>> creaturesToKill = new TreeMap<String, Pair<Integer, Integer>>();
		creaturesToKill.put("gnom", new Pair<Integer, Integer>(0, 1));
		creaturesToKill.put("panna gnom", new Pair<Integer, Integer>(0, 1));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, KILLS_INDEX, creaturesToKill));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Dziękuję. Trzeba pokonać " + QUEST_REQUIREMENTS_TEXT + ". "
				+ "Jeśli potrzebujesz wskazówek, zapytaj mnie, gdzie szukać.",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Rozumiem. Gdyby dobre chęci wystarczały, już byłoby po sprawie. Wróć, jeśli zmienisz zdanie.",
				new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		final SpeakerNPC npc = npcs.get("Sedro");
		npc.add(ConversationStates.ATTENDING, "wskazówki", null, ConversationStates.ATTENDING,
				"Wejdź do jaskini niebieskim wejściem pod czerwonym domem Mistrza. W bocznych komorach spotkasz gnoma i pannę gnom; pokonaj po jednym z każdego rodzaju. Atakuj pojedynczo i pilnuj zapasu jedzenia.",
				new DialogueHintContextAction("sedro_general"));
		npc.add(ConversationStates.ATTENDING, "szczury", null, ConversationStates.ATTENDING,
				"Szczury spotkasz w Semos i pod ziemią. Wejścia do podziemi szukaj pośrodku miasta. Tam rozejrzyj się także za szczurami jaskiniowymi.",
				new DialogueHintContextAction("sedro_rats"));
		npc.add(ConversationStates.ATTENDING, "podziemia", null, ConversationStates.ATTENDING,
				"Niebieskie wejście do jaskini znajdziesz pod czerwonym domem Mistrza w Semos. W bocznych komorach szukaj gnoma i panny gnom - do mojego zadania potrzebujesz po jednym z każdego rodzaju.", null);
		npc.add(ConversationStates.ATTENDING, "gnomy", null, ConversationStates.ATTENDING,
				"Gnomów szukaj w bocznych komorach jaskini pod czerwonym domem Mistrza. Niebieskie wejście znajduje się na południe od tego domu. Pamiętaj, że w mojej prośbie chodzi o gnoma i pannę gnom, po jednym z każdego rodzaju. Nie wdawaj się w walkę z całą gromadą naraz.",
				new DialogueHintContextAction("sedro_gnomes"));
		npc.add(ConversationStates.ATTENDING, "wioska", null, ConversationStates.ATTENDING,
				"Wejdź do jaskini pod czerwonym domem Mistrza i rozejrzyj się w bocznych komorach. Do mojego zadania nie musisz przekraczać bariery.", null);
		npc.add(ConversationStates.ATTENDING, "Starkad", null, ConversationStates.ATTENDING,
				"Starkad przebywa w banku w Semos. Bank znajdziesz w zielonym budynku na lewo od fontanny w centrum miasta.", null);
		npc.add(ConversationStates.ATTENDING, "dziennik", null, ConversationStates.ATTENDING,
				"Otwórz Menu i wybierz Dziennik Zadań. Tam sprawdzisz, ile potworów pozostało do pokonania.", null);
		npc.add(ConversationStates.ATTENDING, "przedmioty", null, ConversationStates.ATTENDING,
				"Kliknij przedmiot prawym przyciskiem myszy i wybierz 'zobacz'. Statystyki znajdziesz też w Menu, pod hasłem Lista przedmiotów.", null);
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Sedro");

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(DialogueRewardAction.item("zwój semos", 10));
		actions.add(DialogueRewardAction.item("eliksir", 10));
		actions.add(DialogueRewardAction.item("duży eliksir", 5));
		actions.add(DialogueRewardAction.item("butelka wody", 20));
		actions.add(DialogueRewardAction.item("sztylecik", 1));
		actions.add(DialogueRewardAction.item("maczeta", 1));
		actions.add(DialogueRewardAction.item("pałasz", 1));
		actions.add(DialogueRewardAction.item("drewniana tarcza", 1));
		actions.add(DialogueRewardAction.item("mięso", 50));
		actions.add(DialogueRewardAction.xp(500));
		actions.add(DialogueRewardAction.attackXP(500));
		actions.add(DialogueRewardAction.defenseXP(500));
		actions.add(new SetQuestAction(QUEST_SLOT, "done;1"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		actions.add(new DialogueNarrationAction("Sedro z ulgą kiwa głową."));

		final LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);

		npc.add(ConversationStates.ATTENDING,
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KilledForQuestCondition(QUEST_SLOT, KILLS_INDEX)),
				ConversationStates.ATTENDING,
				"Dobra robota, dotrzymałeś słowa. Może teraz przez jakiś czas będzie spokojniej, a ja nie będę musiał martwić się o każdego przechodnia. Przyjmij przygotowany ekwipunek i zapasy na dalszą drogę - niech dobrze ci służą.",
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING,
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, KILLS_INDEX))),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nie wszystkie potwory zostały jeszcze pokonane. "
								+ "Postęp: " + getKillProgress(player) + ". "
								+ "Chcesz, żebym przypomniał ci, gdzie szukać?");
					}
				});
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Pomóż Sedro",
				"Sedro potrzebuje pomocy z gnomem i panną gnom w jaskini pod domem Mistrza w Semos.",
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

		if (player.isQuestInState(QUEST_SLOT, "rejected")) {
			res.add("Odmówiłem Sedro pomocy z potworami. Jeśli zmienię zdanie, mogę ponownie z nim porozmawiać.");
			return res;
		}

		if (!isCompleted(player)) {
			res.add("Muszę zabić 1 gnoma oraz 1 pannę gnom.");

			if (player.isQuestInState(QUEST_SLOT, 0, "start")) {
				res.add("Mój obecny postęp: " + getKillProgress(player).replace("#", "") + ".");
			}

			res.add("Jeśli potrzebuję wskazówek, mogę poprosić o nie Sedro. Mogę też poszukać towarzystwa innych graczy, pisząc na czacie /k wiadomość.");
		} else if (isRepeatable(player)) {
			res.add("Teraz po czasie powinienem znowu odwiedzić Sedro. Może znów potrzebuje pomocy, w końcu ma już troche lat...");
		} else {
			res.add("Potwory zostały zgładzone. Sedro dał mi w nagrode podstawowy ekwipunek i troche jedzenia, które bardzo mi sie przyda na samym poczatku mojej wedrówki. W nagrodę za ukończenie zadania otrzymałem: 500 pkt doświadczenia, 500 pkt ataku, 500 pkt obrony, 10 zwoji semos, 10 eliksirów, 5 dużych eliksirów, 20 butelek wody, 50 mięsa, sztylecik, maczetę, pałasz i drewnianą tarczę.");
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
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"),
				 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)).fire(player, null, null);
	}

	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT, "done").fire(player, null, null);
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
