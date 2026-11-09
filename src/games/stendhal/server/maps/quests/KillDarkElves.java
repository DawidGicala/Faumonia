/* $Id: KillDarkElves.java,v 1.40 2011/11/13 17:14:15 kymara Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
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

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.ExamineChatAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.KilledCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Kill Dark Elves
 * <p>
 * 
 * PARTICIPANTS:
 * <ul>
 * <li> Maerion
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Maerion asks you fix his dark elf problem
 * <li> You go kill at least a dark elf archer, captain, and thing
 * <li> The thing drops an amulet
 * <li> Maerion checks your kills, takes the amulet and gives you a ring of life
 * as reward
 * </ul>
 * REWARD: <ul><li> emerald ring <li> 10000 XP
 * <li>10 karma in total</ul>
 * 
 * REPETITIONS: - None.
 */
public class KillDarkElves extends AbstractQuest {
	private static final String QUEST_SLOT = "kill_dark_elves";
	protected static final List<String> creatures = 
		Arrays.asList("elf ciemności kapitan",
				      "elf ciemności generał",
				      "elf ciemności rycerz",
				      "elf ciemności czarownik",
				      "elf ciemności czarnoksiężnik",
				      "elf ciemności królewicz",
				      "elf ciemności matrona",
					  "elf ciemności łucznik elitarny",
				      "elf ciemności łucznik");
	
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Maerion");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Nasza umowa nadal obowiązuje. Zajmij się mrocznymi elfami w kryjówce pod sekretnym pokojem i przynieś amulet mutanta coś; zwykła wędrówka po Nalwor nie wystarczy.",
				null);

		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Dziękuję za twoją pomoc. Jestem zadowolony, że odzyskałem amulet.",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, 
				"Mroczne elfy miały dotrzymywać porozumienia, tymczasem ich kryjówka stała się zagrożeniem dla Nalwor. Wejście jest poniżej nas: sekretny pokój ma szary dach i twarz demona na drzwiach. Zejdziesz tam i odzyskasz dla mnie amulet mutanta coś?",
				null);

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		//actions.add(new StartRecordingKillsAction("dark elf archer", "dark elf captain", "thing"));
		actions.add(new IncreaseKarmaAction(5.0));
		actions.add(new SetQuestAction(QUEST_SLOT, "started"));
		actions.add(new ExamineChatAction("dark-elves-wanted.png", "List Gończy!", ""));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Zejdź do kryjówki i rozpraw się z każdym rodzajem mrocznych elfów z listy. Musisz osobiście pokonać je właśnie tam, a z mutanta coś przynieść amulet. Bez amuletu nie zakończymy tej sprawy.",
			new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED, 
			ConversationPhrases.NO_MESSAGES, 
			null,
			ConversationStates.ATTENDING,
			"W takim razie boję się o bezpieczeństwo elfów w Nalwor...",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("secret", "room", "sekretnym", "pokoju"),
			null,
			ConversationStates.QUEST_OFFERED,
			"Zejdź poniżej sali i szukaj szarego dachu oraz twarzy demona na drzwiach. W środku prowadzą eksperymenty z mutantem coś; to od niego trzeba odzyskać amulet. Podejmiesz tę wyprawę?",
			null);
	}

	private void step_2() {
		// Go kill the dark elves and get the amulet from the thing
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Maerion");

		// support for old-style quest
		
		// the player returns to Maerion after having started the quest.
		// Maerion checks if the player has killed one of enough dark elf types
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new NotCondition(new KilledCondition("elf ciemności łucznik", "elf ciemności kapitan", "coś"))),
				ConversationStates.ATTENDING, 
				"Twoja wyprawa nadal trwa. Pokonaj łucznika, kapitana mrocznych elfów oraz mutanta coś, a potem przynieś mi amulet.",
				null);
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING, "meldunek",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new NotCondition(new KilledCondition("elf ciemności łucznik", "elf ciemności kapitan", "coś"))),
				ConversationStates.ATTENDING, 
				"Twoja wyprawa nadal trwa. Pokonaj łucznika, kapitana mrocznych elfów oraz mutanta coś, a potem przynieś mi amulet.",
				null);
		
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new KilledCondition("elf ciemności łucznik", "elf ciemności kapitan", "coś"),
						new NotCondition(new PlayerHasItemWithHimCondition("amulet")))
				, ConversationStates.ATTENDING
				, "Przeciwnicy zostali pokonani, ale brakuje amuletu. Przynieś go z kryjówki mutanta coś; dopiero wtedy zamkniemy naszą umowę."
				, null);
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING, "meldunek",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new KilledCondition("elf ciemności łucznik", "elf ciemności kapitan", "coś"),
						new NotCondition(new PlayerHasItemWithHimCondition("amulet")))
				, ConversationStates.ATTENDING
				, "Przeciwnicy zostali pokonani, ale brakuje amuletu. Przynieś go z kryjówki mutanta coś; dopiero wtedy zamkniemy naszą umowę."
				, null);
	
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new KilledCondition("elf ciemności łucznik", "elf ciemności kapitan", "coś"),
						new PlayerHasItemWithHimCondition("amulet"))
				, ConversationStates.ATTENDING
				, "Amulet znów jest na swoim miejscu, a zagrożenie zostało odparte. Przyjmij pierścień szmaragdowy w dowód dotrzymania umowy; jeśli kiedyś ulegnie uszkodzeniu, Ognir zna się na takich naprawach.",
				new MultipleActions(new DropItemAction("amulet"),
						new EquipItemAction("pierścień szmaragdowy", 1, true),
						new IncreaseXPAction(20000),
						new IncreaseKarmaAction(25.0),
						new SetQuestAction(QUEST_SLOT, "done")));
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING, "meldunek",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new KilledCondition("elf ciemności łucznik", "elf ciemności kapitan", "coś"),
						new PlayerHasItemWithHimCondition("amulet"))
				, ConversationStates.ATTENDING
				, "Amulet znów jest na swoim miejscu, a zagrożenie zostało odparte. Przyjmij pierścień szmaragdowy w dowód dotrzymania umowy; jeśli kiedyś ulegnie uszkodzeniu, Ognir zna się na takich naprawach.",
				new MultipleActions(new DropItemAction("amulet"),
						new EquipItemAction("pierścień szmaragdowy", 1, true),
						new IncreaseXPAction(20000),
						new IncreaseKarmaAction(25.0),
						new SetQuestAction(QUEST_SLOT, "done")));
		
		
		// support for new-style quest
		
		// building string for completed quest state
		StringBuilder sb = new StringBuilder("started");
		for(int i=0;i<creatures.size();i++) {
			sb.append(";");
			sb.append(creatures.get(i));
		}
		final String completedQuestState = sb.toString();
		
		// the player returns to Maerion after having started the quest.
		// Maerion checks if the player has killed one of enough dark elf types
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT,0,"started"),
						new NotCondition(
								new QuestInStateCondition(QUEST_SLOT, completedQuestState))),
				ConversationStates.ATTENDING, 
				"Nie zakończyliśmy jeszcze sprawy mrocznych elfów. Sprawdź przywódców, czarodziejów i łuczników w kryjówce; matrona również musi zostać pokonana. Pamiętaj też o amulecie z mutanta coś.",
				new ExamineChatAction("dark-elves-wanted.png", "List Gończy!!", ""));
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING, "meldunek",
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT,0,"started"),
						new NotCondition(
								new QuestInStateCondition(QUEST_SLOT, completedQuestState))),
				ConversationStates.ATTENDING, 
				"Nie zakończyliśmy jeszcze sprawy mrocznych elfów. Sprawdź przywódców, czarodziejów i łuczników w kryjówce; matrona również musi zostać pokonana. Pamiętaj też o amulecie z mutanta coś.",
				new ExamineChatAction("dark-elves-wanted.png", "List Gończy!!", ""));
		
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, completedQuestState),
						new NotCondition(
								new PlayerHasItemWithHimCondition("amulet")))
				, ConversationStates.ATTENDING
				, "Przeciwnicy zostali pokonani, ale brakuje amuletu. Przynieś go z kryjówki mutanta coś; dopiero wtedy zamkniemy naszą umowę."
				, null);
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING, "meldunek",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestInStateCondition(QUEST_SLOT, completedQuestState),
						new NotCondition(
								new PlayerHasItemWithHimCondition("amulet")))
				, ConversationStates.ATTENDING
				, "Przeciwnicy zostali pokonani, ale brakuje amuletu. Przynieś go z kryjówki mutanta coś; dopiero wtedy zamkniemy naszą umowę."
				, null);
	
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, completedQuestState),
						new PlayerHasItemWithHimCondition("amulet"))
				, ConversationStates.ATTENDING
				, "Amulet znów jest na swoim miejscu, a zagrożenie zostało odparte. Przyjmij pierścień szmaragdowy w dowód dotrzymania umowy; jeśli kiedyś ulegnie uszkodzeniu, Ognir zna się na takich naprawach.",
				new MultipleActions(new DropItemAction("amulet"),
						new EquipItemAction("pierścień szmaragdowy", 1, true),
						new IncreaseXPAction(50000),
						new IncreaseKarmaAction(25.0),
						new SetQuestAction(QUEST_SLOT, "done")));
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING, "meldunek",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestInStateCondition(QUEST_SLOT, completedQuestState),
						new PlayerHasItemWithHimCondition("amulet"))
				, ConversationStates.ATTENDING
				, "Amulet znów jest na swoim miejscu, a zagrożenie zostało odparte. Przyjmij pierścień szmaragdowy w dowód dotrzymania umowy; jeśli kiedyś ulegnie uszkodzeniu, Ognir zna się na takich naprawach.",
				new MultipleActions(new DropItemAction("amulet"),
						new EquipItemAction("pierścień szmaragdowy", 1, true),
						new IncreaseXPAction(50000),
						new IncreaseKarmaAction(25.0),
						new SetQuestAction(QUEST_SLOT, "done")));
		
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("secret", "room", "sekretnego", "pokoju"),
			null,
			ConversationStates.ATTENDING,
			"Sekretny pokój jest poniżej nas, pod szarym dachem i za drzwiami z twarzą demona. Wejdź do kryjówki, pokonaj mroczne elfy z listy i odzyskaj amulet od mutanta coś.",
			null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Zabij Mroczne Elfy",
				"Maerion z Nalwor chce odzyskać amulet z kryjówki mrocznych elfów. Poznaj drogę do sekretnego pokoju, pokonaj wskazanych przeciwników i przynieś zgubę.",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "KillDarkElves";
	}
	
	/**
	 * function return list of drow creatures to kill
	 * @return - list of dark elves to kill
	 */
	public List<String> getDrowCreaturesList() {
		return(creatures);
	}
	
	// Killing the thing probably requires a level even higher than this - but they can get help
	@Override
	public int getMinLevel() {
		return 100;
	}
	
	@Override
	public List<String> getHistory(final Player player) {
 		LinkedList<String> history = new LinkedList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return history;
		}
		final String questState = player.getQuest(QUEST_SLOT, 0);

		if ("rejected".equals(questState)) {
			history.add("Nie chcę pomagać Maerion.");
			return history;
		}
		if ("done".equals(questState)) {
			history.add("Zakończyłem zadanie Maeriona i dostałem pierścień odnawiający życie!	W nagrodę za ukończenie zadania otrzymałem: 50.000 pkt doświadczenia, 25 karmy i pierścień szmaragdowy.");
			return history;
		}

		// we can be here only if player accepted this quest.
		history.add("Zgodziłem się na pomoc Maerion.");
		
		boolean ak=true;	
		
		if("started".equals(player.getQuest(QUEST_SLOT, 0))) {
			// checking which creatures player killed.		
			for(int i = 0; i<creatures.size();i++) {
				final boolean sp = creatures.get(i).equals(player.getQuest(QUEST_SLOT, i+1));
				ak = ak & sp;
				if(!sp) {
					history.add("Jeszcze nie zabiłem "+creatures.get(i)+" w sekretnym pokoju.");
				}
			}		
			for(int i = 0; i<creatures.size();i++) {
				final boolean sp = creatures.get(i).equals(player.getQuest(QUEST_SLOT, i+1));
				if(sp) {
					history.add("Zabiłem "+creatures.get(i)+" w sekretnym pokoju.");
				}
			}

			// all killed
			if (ak) {
				history.add("Zabiłem wszystkie potwory.");
			}
		}
		
		// here is support for old-style quest
		if ("start".equals(player.getQuest(QUEST_SLOT, 0))) {
			final boolean osp1 = player.hasKilled("elf ciemności kapitan");
			final boolean osp2 = player.hasKilled("elf ciemności łucznik");
			final boolean osp3 = player.hasKilled("coś");
			// first add killed creatures
			if (osp1) {
				history.add("Zabiłem elf ciemności kapitan w sekretnym pokoju.");
			}		
			if (osp2) {
				history.add("Zabiłem elf ciemności łucznik w sekretnym pokoju.");
			}
			if (osp3) {
				history.add("Zabiłem coś.");
			}
			
			// now add non-killed
			if (!osp1) {
				history.add("Jeszcze nie zabiłem elf ciemności kapitan w sekretnym pokoju.");				
			}	
			if (!osp2) {
				history.add("Jeszcze nie zabiłem elf ciemności łucznik w sekretnym pokoju.");				
			}		
			if (!osp3) {
				history.add("Jeszcze nie zabiłem cosia.");				
			}
			
			// all killed
			if (osp1 && osp2 && osp3) {
				history.add("Zabiłem wszystkie potwory.");
				ak=true;
			}	
		}
		
		// for both old- and new-style quests
		final boolean am = player.isEquipped("amulet");
		if (am) {
			history.add("Mam ze sobą amulet.");
		} else {
			history.add("Nie mam ze sobą amuletu.");
		}
		
		if (am && ak) {
			history.add("Czas wrócić do Maeriona po nagrodę.");
		}
		
		return history;		
	}
	
	@Override
	public String getRegion() {
		return Region.NALWOR_CITY;
	}

	@Override
	public String getNPCName() {
		return "Maerion";
	}

    public static List<String> getDialogueCreatures() { return java.util.Collections.unmodifiableList(creatures); }

}
