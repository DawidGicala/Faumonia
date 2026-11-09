/* $Id: MedicineForTad.java,v 1.16 2012/09/15 15:33:06 kymara Exp $ */
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

import games.stendhal.server.entity.npc.action.DialogueRewardAction;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.ExamineChatAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Introduce new players to game <p>PARTICIPANTS:<ul>
 * <li> Tad
 * <li> Margaret
 * <li> Ilisa
 * <li> Ketteh Wehoh
 * </ul>
 * 
 * <p>
 * STEPS:<ul>
 * <li> Tad asks you to buy a flask to give it to Margaret.
 * <li> Margaret sells you a flask
 * <li>Tad thanks you and asks you to take the flask to Ilisa
 * <li> Ilisa asks you for a few herbs.
 * <li> Return the created dress potion to Tad.
 * <li> Ketteh Wehoh will reminder player about Tad, if quest is started but not complete.
 * </ul>
 * <p>
 * REWARD:<ul>
 * <li> 270 XP
 * <li> 10 gold coins
 * </ul>
 * <p>
 * REPETITIONS:<ul>
 * <li> None.
 * </ul>
 */
public class MedicineForTad extends AbstractQuest {
	private static final String QUEST_SLOT = "introduce_players";


	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (player.hasQuest("TadFirstChat")) {
			res.add("Spotkałem Tada w hostelu w Semos.");
		}
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);
		if (player.isQuestInState(QUEST_SLOT, "start", "ilisa", "corpse&herbs",
				"eliksir", "done")) {
			res.add("Poprosił mnie o kupienie flaszy od Margaret w tawernie w Semos.");
		}
		if ((questState.equals("start") && player.isEquipped("flasza"))
				|| player.isQuestInState(QUEST_SLOT, "ilisa", "corpse&herbs",
						"eliksir", "done")) {
			res.add("Mam flaszę i wkrótce przyniosę ją Tadowi.");
		}
		if (player.isQuestInState(QUEST_SLOT, "ilisa", "corpse&herbs",
				"eliksir", "done")) {
			res.add("Tad poprosił mnie o zaniesienie flaszy do Ilisy w świątyni w Semos.");
		}
		if (player.isQuestInState(QUEST_SLOT, "corpse&herbs", "eliksir", "done")) {
			res.add("Ilisa poprosiła mnie o dostarczenie zioła zwanego arandula, które rośnie na północ od Semos.");
		}
		if ((questState.equals("corpse&herbs") && player.isEquipped("arandula"))
				|| player.isQuestInState(QUEST_SLOT, "eliksir", "done")) {
			res.add("Znalazłem trochę aranduli i zaniosę ją do Ilisy.");
		}
		if (player.isQuestInState(QUEST_SLOT, "eliksir", "done")) {
			res.add("Ilisa przygotowała lekarstwo. Mam powiedzieć Tadowi, że jest gotowe i że powinien zgłosić się do niej osobiście.");
		}
		if (questState.equals("done")) {
			res.add("Tad podziękował mi.	W nagrodę za ukończenie zadania otrzymałem: 900 pkt doświadczenia łącznie, 200 pkt ataku, 200 pkt obrony, płaszcz karmazynowy, tarczę królewską, miecz, 50 butelek wody i 10 zwojów Semos");
		}
		return res;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Tad");
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, "Wszystko w porządku, dziękuję.", null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, 
				"Ten kaszel nie daje mi spokoju, a sam ledwie mam siłę wyjść z hostelu. Ilisa zna się na lekarstwach, ale najpierw potrzebuję pustej #flaszy. Zdobędziesz ją dla mnie?",
				null);

		/** In case Quest has already been completed */
		npc.add(ConversationStates.ATTENDING, Arrays.asList("flasza", "flaszka", "flaszę"),
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Już mi pomogłeś! Czuję się teraz lepiej.",
				null);

		/** If quest is not started yet, start it. */
		npc.add(ConversationStates.QUEST_OFFERED, Arrays.asList("flasza", "flaszka", "flaszę"),
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Mógłbyś zdobyć flaszę od #Margaret.", null);

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING, 
				"Dziękuję, naprawdę mi ulżyło. Margaret sprzedaje puste flasze w tawernie Semos. Kup jedną i pokaż mi ją, zanim zaniesiesz ją do uzdrowicielki.",
				new SetQuestAction(QUEST_SLOT, "start"));

		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Och, proszę nie zmienisz zdania? *Apsiiiik*", null);

		npc.add(ConversationStates.QUEST_OFFERED,
				"margaret", null,
				ConversationStates.QUEST_OFFERED,
				"Margaret obsługuje tawernę w Semos. Zapytaj ją o ofertę i kup pustą flaszę, a potem pokaż ją mnie. Taki pojemnik będzie potrzebny na lekarstwo.",
				null);

		/** Remind player about the quest */
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("flasza", "flaszka", "flaszę"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new NotCondition(new PlayerHasItemWithHimCondition("flasza"))),
				ConversationStates.ATTENDING,
				"Kaszel wciąż mnie męczy. Przynieś pustą flaszę od #Margaret z tawerny Semos; bez niej Ilisa nie przygotuje lekarstwa.",
				null);

        /** Remind player about the quest */
        npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new QuestInStateCondition(QUEST_SLOT, "start"),
                ConversationStates.ATTENDING,
                "Kaszel wciąż mnie męczy. Przynieś pustą flaszę od #Margaret z tawerny Semos; bez niej Ilisa nie przygotuje lekarstwa.",
                null);

		npc.add(ConversationStates.ATTENDING, "margaret", null,
				ConversationStates.ATTENDING,
				"Margaret obsługuje tawernę w Semos. Zapytaj ją o ofertę i kup pustą flaszę, a potem pokaż ją mnie. Taki pojemnik będzie potrzebny na lekarstwo.", null);
	}

	private void step_2() {
		/** Just buy the stuff from Margaret. It isn't a quest */
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Tad");

		final List<ChatAction> processStep = new LinkedList<ChatAction>();
		processStep.add(DialogueRewardAction.item("money", 10));
		processStep.add(DialogueRewardAction.xp(100));
		processStep.add(new SetQuestAction(QUEST_SLOT, "ilisa"));
		
		// staring the conversation the first time after getting a flask.
		// note Ilisa is spelled with a small i here because I
		// and l cannot be told apart in game
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new PlayerHasItemWithHimCondition("flasza")),
				ConversationStates.ATTENDING, 
				"Flasza wygląda dobrze, zachowaj ją w plecaku. Zanieś ją teraz Ilisie w świątyni Semos i powiedz, że chodzi o moje lekarstwo. Dorzucę dziesięć monet za fatygę.",
				new MultipleActions(processStep));

        // Same authoritative step, without requiring the player to leave and greet again.
        npc.add(ConversationStates.ATTENDING, "lekarstwo tada",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new PlayerHasItemWithHimCondition("flasza")),
				ConversationStates.ATTENDING, 
				"Flasza wygląda dobrze, zachowaj ją w plecaku. Zanieś ją teraz Ilisie w świątyni Semos i powiedz, że chodzi o moje lekarstwo. Dorzucę dziesięć monet za fatygę.",
				new MultipleActions(processStep));

		// player said hi with flask on ground then picked it up and said flask
		npc.add(ConversationStates.ATTENDING, Arrays.asList("flasza", "flaszka", "flaszę"),
                new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new PlayerHasItemWithHimCondition("flasza")),
                ConversationStates.ATTENDING,
                "Flasza wygląda dobrze, zachowaj ją w plecaku. Zanieś ją teraz Ilisie w świątyni Semos i powiedz, że chodzi o moje lekarstwo. Dorzucę dziesięć monet za fatygę.",
                new MultipleActions(processStep));


		// remind the player to take the flask to ilisa.
		// note Ilisa is spelled with a small i here because I
		// and l cannot be told apart in game
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "ilisa"),
						new PlayerHasItemWithHimCondition("flasza")),
				ConversationStates.ATTENDING, 
				"Flasza wygląda dobrze, zachowaj ją w plecaku. Zanieś ją teraz Ilisie w świątyni Semos i powiedz, że chodzi o moje lekarstwo. Dorzucę dziesięć monet za fatygę.",
				null);

        // Same authoritative step, without requiring the player to leave and greet again.
        npc.add(ConversationStates.ATTENDING, "lekarstwo tada",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestInStateCondition(QUEST_SLOT, "ilisa"),
						new PlayerHasItemWithHimCondition("flasza")),
				ConversationStates.ATTENDING, 
				"Flasza wygląda dobrze, zachowaj ją w plecaku. Zanieś ją teraz Ilisie w świątyni Semos i powiedz, że chodzi o moje lekarstwo. Dorzucę dziesięć monet za fatygę.",
				null);

		// another reminder incase player says task again
        npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
                new QuestInStateCondition(QUEST_SLOT, "ilisa"),
                ConversationStates.ATTENDING,
                "Zanieś pustą flaszę Ilisie w świątyni Semos. Powiedz jej, że przysyła cię Tad z hostelu. Ona będzie wiedziała, jakie lekarstwo przygotować.",
                null);

		npc.add(ConversationStates.ATTENDING, "ilisa", null,
				ConversationStates.ATTENDING,
				"Ilisa opiekuje się świątynią w Semos, niedaleko tawerny Margaret. Przywitaj się z nią i wspomnij o mnie. Pamiętaj, żeby zabrać flaszę, jeśli jeszcze jej nie oddałeś.", null);
	}

	private void step_4() {
		final SpeakerNPC npc = npcs.get("Ilisa");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "ilisa"),
						new NotCondition(new PlayerHasItemWithHimCondition("flasza"))),
				ConversationStates.ATTENDING, 
				"Tad nadal choruje? Pomogę mu, ale potrzebuję pustej flaszy na lekarstwo. Kup ją u Margaret w tawernie i przynieś tutaj.", null);

        // Same authoritative step, without requiring the player to leave and greet again.
        npc.add(ConversationStates.ATTENDING, "lekarstwo tada",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestInStateCondition(QUEST_SLOT, "ilisa"),
						new NotCondition(new PlayerHasItemWithHimCondition("flasza"))),
				ConversationStates.ATTENDING, 
				"Tad nadal choruje? Pomogę mu, ale potrzebuję pustej flaszy na lekarstwo. Kup ją u Margaret w tawernie i przynieś tutaj.", null);

		final List<ChatAction> processStep = new LinkedList<ChatAction>();
		processStep.add(new DropItemAction("flasza"));
		processStep.add(DialogueRewardAction.xp(100));
		processStep.add(new SetQuestAction(QUEST_SLOT, "corpse&herbs"));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "ilisa"),
						new PlayerHasItemWithHimCondition("flasza")),
				ConversationStates.ATTENDING, 
				"Dziękuję, odłożę tę flaszę do przygotowania lekarstwa. Brakuje mi jeszcze jednej aranduli, zioła rosnącego na północ od Semos. Przynieś ją, a zajmę się resztą.",
				new MultipleActions(processStep));

        // Same authoritative step, without requiring the player to leave and greet again.
        npc.add(ConversationStates.ATTENDING, "lekarstwo tada",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestInStateCondition(QUEST_SLOT, "ilisa"),
						new PlayerHasItemWithHimCondition("flasza")),
				ConversationStates.ATTENDING, 
				"Dziękuję, odłożę tę flaszę do przygotowania lekarstwa. Brakuje mi jeszcze jednej aranduli, zioła rosnącego na północ od Semos. Przynieś ją, a zajmę się resztą.",
				new MultipleActions(processStep));

		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("herb", "arandula", "yes", "ok", "tak", "dobrze", "ziół", "zioła"),
//				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "corpse&herbs"), new NotCondition(new PlayerHasItemWithHimCondition("arandula"))),
				ConversationStates.ATTENDING,
				"Arandula rośnie na północ od Semos, w okolicy trzech wzgórz. Przyjrzyj się rysunkowi, żeby rozpoznać zioło, i przynieś mi jedną sztukę. Nie musisz szukać innych składników do tego lekarstwa.",
				new ExamineChatAction("arandula.png", "Ilisa rysuje", "Arandula"));

		npc.add(
				ConversationStates.ATTENDING,
				"tad",
				null,
				ConversationStates.ATTENDING,
				"Tad potrzebuje lekarstwa, które przygotuję tutaj, w świątyni. Najpierw muszę dostać flaszę, a później jedną arandulę. Gdy skończę, poproszę cię o przekazanie mu wiadomości.",
				null);
	}

	private void step_5() {
		final SpeakerNPC npc = npcs.get("Ilisa");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "corpse&herbs"),
						new NotCondition(new PlayerHasItemWithHimCondition("arandula"))),
				ConversationStates.ATTENDING, 
				"Masz przy sobie te #zioła do #lekarstwa?", null);

        // Same authoritative step, without requiring the player to leave and greet again.
        npc.add(ConversationStates.ATTENDING, "lekarstwo tada",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestInStateCondition(QUEST_SLOT, "corpse&herbs"),
						new NotCondition(new PlayerHasItemWithHimCondition("arandula"))),
				ConversationStates.ATTENDING, 
				"Masz przy sobie te #zioła do #lekarstwa?", null);

		final List<ChatAction> processStep = new LinkedList<ChatAction>();
		processStep.add(new DropItemAction("arandula"));
		processStep.add(DialogueRewardAction.xp(200));
		processStep.add(new SetQuestAction(QUEST_SLOT, "eliksir"));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "corpse&herbs"),
						new PlayerHasItemWithHimCondition("arandula")),
				ConversationStates.ATTENDING, 
				"To odpowiednie zioło. Przygotuję wyciąg i przeleję go do flaszy, którą przyniosłeś. Powiedz Tadowi, że lekarstwo jest gotowe i że chcę go zobaczyć osobiście.",
				new MultipleActions(processStep));

        // Same authoritative step, without requiring the player to leave and greet again.
        npc.add(ConversationStates.ATTENDING, "lekarstwo tada",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestInStateCondition(QUEST_SLOT, "corpse&herbs"),
						new PlayerHasItemWithHimCondition("arandula")),
				ConversationStates.ATTENDING, 
				"To odpowiednie zioło. Przygotuję wyciąg i przeleję go do flaszy, którą przyniosłeś. Powiedz Tadowi, że lekarstwo jest gotowe i że chcę go zobaczyć osobiście.",
				new MultipleActions(processStep));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("eliksir",
				"medicine", "mikstura", "lekarstwo", "lekarstwa"), null, ConversationStates.ATTENDING,
				"Lekarstwo, na które czeka #Tad.", null);
	}

	private void step_6() {
		SpeakerNPC npc = npcs.get("Tad");

        // another reminder incase player says task again                                                                                                    
        npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
                new QuestInStateCondition(QUEST_SLOT, "corpse&herbs"),
                ConversationStates.ATTENDING,
                "*kaszlnięcie* Mam nadzieję, że #Ilisa pospieszy się z moim lekarstwem...",
                null);

		final List<ChatAction> processStep = new LinkedList<ChatAction>();
		processStep.add(DialogueRewardAction.xp(500));
		processStep.add(DialogueRewardAction.attackXP(200));
		processStep.add(DialogueRewardAction.defenseXP(200));
		processStep.add(DialogueRewardAction.item("płaszcz karmazynowy", 1, true));
		processStep.add(DialogueRewardAction.item("butelka wody", 50, true));
		processStep.add(DialogueRewardAction.item("zwój semos", 10, true));
		processStep.add(DialogueRewardAction.item("tarcza królewska", 1, true));
		processStep.add(DialogueRewardAction.item("miecz", 1, true));
		processStep.add(new SetQuestAction(QUEST_SLOT, "done"));
		
		// note Ilisa is spelled with a small i here because I
		// and l cannot be told apart in game
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "eliksir")),
				ConversationStates.ATTENDING, "To najlepsza wiadomość, jaką dziś usłyszałem. Zaraz zgłoszę się do Ilisy; w podziękowaniu przyjmij tarczę, płaszcz, miecz i zapasy na drogę. Zwoje Semos pozwolą ci wrócić do miasta, gdy wyprawa zaprowadzi cię daleko.",
				new MultipleActions(processStep));

        // Same authoritative step, without requiring the player to leave and greet again.
        npc.add(ConversationStates.ATTENDING, "lekarstwo tada",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestInStateCondition(QUEST_SLOT, "eliksir")),
				ConversationStates.ATTENDING, "To najlepsza wiadomość, jaką dziś usłyszałem. Zaraz zgłoszę się do Ilisy; w podziękowaniu przyjmij tarczę, płaszcz, miecz i zapasy na drogę. Zwoje Semos pozwolą ci wrócić do miasta, gdy wyprawa zaprowadzi cię daleko.",
				new MultipleActions(processStep));
	
		/*
		 * if player has not finished this quest, ketteh will remind player about him.
		 * if player has not started, and not finished, ketteh will ask if player has met him.
		 */
		npc = npcs.get("Ketteh Wehoh");

        npc.add(ConversationStates.ATTENDING, 
        		ConversationPhrases.GOODBYE_MESSAGES,
        		new AndCondition(new QuestStartedCondition(QUEST_SLOT),
        					     new QuestNotCompletedCondition(QUEST_SLOT)),
                ConversationStates.IDLE,
                "Do widzenia. Zajrzyj do Tada, jeśli będziesz w pobliżu hostelu. Mam nadzieję, że czuje się już lepiej.",
                null);

        npc.add(ConversationStates.ATTENDING, 
        		ConversationPhrases.GOODBYE_MESSAGES,
        		new QuestNotStartedCondition(QUEST_SLOT),
                ConversationStates.IDLE,
                "Do widzenia. Jeśli będziesz przy hostelu w Semos, zajrzyj do Tada. Słyszałam, że źle się czuje; hostel znajdziesz nad bankiem.",
                null);

	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Lekarstwo dla Tada",
				"Tad z hostelu w Semos potrzebuje lekarstwa. Porozmawiaj z chłopcem, a następnie poproś Ilisę w świątyni o pomoc w jego przygotowaniu.",
				false);
		step_1();
		step_2();
		step_3();
		step_4();
		step_5();
		step_6();
	}
	@Override
	public String getName() {
		return "MedicineForTad";
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}
	@Override
	public String getNPCName() {
		return "Tad";
	}
}
