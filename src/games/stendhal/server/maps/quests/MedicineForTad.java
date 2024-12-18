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
			res.add("Tad poprosił mnie o zaniesienie flaszy do Ilisa w świątyni w Semos.");
		}
		if (player.isQuestInState(QUEST_SLOT, "corpse&herbs", "eliksir", "done")) {
			res.add("Ilisa poprosiła mnie o dostarczenie zioła zwanego Arandula, które rośnie na górę od Semos w okolicy zagajnika.");
		}
		if ((questState.equals("corpse&herbs") && player.isEquipped("arandula"))
				|| player.isQuestInState(QUEST_SLOT, "eliksir", "done")) {
			res.add("Znalazłem trochę ziół Arandula i zaniosę je do ilisy.");
		}
		if (player.isQuestInState(QUEST_SLOT, "eliksir", "done")) {
			res.add("Ilisa zrobiła silne lekarstwo, który pomoże Tadowi. Poprosiła mnie o przekazanie wiadomości mu że jest już gotowe.");
		}
		if (questState.equals("done")) {
			res.add("Tad podziękował mi.	W nagrodę za ukończenie zadania otrzymałem: 800 pkt doświadczenia, 200 pkt ataku, 200 pkt obrony, płaszcz karmazynowy, tarczę królewską, miecz, 50 butelek wody i 10 zwoji semos");
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
				"Nie czuję się dobrze... Potrzebuję butelkę z lekarstwem. Czy możesz zdobyć dla mnie pustą #flaszę?",
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
				"Wspaniale! Proszę idź szybko jak możesz. *Apsiiik*",
				new SetQuestAction(QUEST_SLOT, "start"));

		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Och, proszę nie zmienisz zdania? *Apsiiiik*", null);

		npc.add(ConversationStates.QUEST_OFFERED,
				"margaret", null,
				ConversationStates.QUEST_OFFERED,
				"Margaret jest kelnerką w tawernie. Idź ulicą w dół i w prawo. Pomożesz mi?",
				null);

		/** Remind player about the quest */
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("flasza", "flaszka", "flaszę"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new NotCondition(new PlayerHasItemWithHimCondition("flasza"))),
				ConversationStates.ATTENDING,
				"*kaszlnięcie* Och... Potrzebuję lekarstwa! Wracaj szybko z flaszą od #Margaret.",
				null);

        /** Remind player about the quest */
        npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new QuestInStateCondition(QUEST_SLOT, "start"),
                ConversationStates.ATTENDING,
                "*kaszlnięcie* Och... Potrzebuję lekarstwa! Wracaj szybko z flaszą od #Margaret.",
                null);

		npc.add(ConversationStates.ATTENDING, "margaret", null,
				ConversationStates.ATTENDING,
				"Margaret jest kelnerką w tawernie, do której idzie się w prawo ulica.", null);
	}

	private void step_2() {
		/** Just buy the stuff from Margaret. It isn't a quest */
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Tad");

		final List<ChatAction> processStep = new LinkedList<ChatAction>();
		processStep.add(new EquipItemAction("money", 10));
		processStep.add(new IncreaseXPAction(100));
		processStep.add(new SetQuestAction(QUEST_SLOT, "ilisa"));
		
		// staring the conversation the first time after getting a flask.
		// note Ilisa is spelled with a small i here because I
		// and l cannot be told apart in game
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new PlayerHasItemWithHimCondition("flasza")),
				ConversationStates.ATTENDING, 
				"Dobrze, masz flaszę! Teraz potrzebuję, abyś wziął ją do #Ilisa... ona będzie widziała co robić.",
				new MultipleActions(processStep));

		// player said hi with flask on ground then picked it up and said flask
		npc.add(ConversationStates.ATTENDING, Arrays.asList("flasza", "flaszka", "flaszę"),
                new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new PlayerHasItemWithHimCondition("flasza")),
                ConversationStates.ATTENDING,
                "Dobrze, masz flaszę! Teraz potrzebuję, abyś wziął ją do #Ilisa... ona będzie widziała co robić.",
                new MultipleActions(processStep));


		// remind the player to take the flask to ilisa.
		// note Ilisa is spelled with a small i here because I
		// and l cannot be told apart in game
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "ilisa"),
						new PlayerHasItemWithHimCondition("flasza")),
				ConversationStates.ATTENDING, 
				"Dobrze, masz flaszę! Teraz potrzebuję, abyś wziął ją do #Ilisa... ona będzie widziała co robić.",
				null);

		// another reminder incase player says task again
        npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
                new QuestInStateCondition(QUEST_SLOT, "ilisa"),
                ConversationStates.ATTENDING,
                "Potrzebuję Ciebie, abyś wziął flaszę do #ilisa... ona będzie widziała co robić.",
                null);

		npc.add(ConversationStates.ATTENDING, "ilisa", null,
				ConversationStates.ATTENDING,
				"Ilisa jest uzdrowicielką w świątyni w Semos. Znajdziesz ją obok tawerny Margaret.", null);
	}

	private void step_4() {
		final SpeakerNPC npc = npcs.get("Ilisa");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "ilisa"),
						new NotCondition(new PlayerHasItemWithHimCondition("flasza"))),
				ConversationStates.ATTENDING, 
				"Lekarstwo dla #Tad? Nie powiedział Tobie, aby przynieść flaszę?", null);

		final List<ChatAction> processStep = new LinkedList<ChatAction>();
		processStep.add(new DropItemAction("flasza"));
		processStep.add(new IncreaseXPAction(100));
		processStep.add(new SetQuestAction(QUEST_SLOT, "corpse&herbs"));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "ilisa"),
						new PlayerHasItemWithHimCondition("flasza")),
				ConversationStates.ATTENDING, 
				"Ach widzę, że masz flaszę. #Tad potrzebuje lekarstwa? Hmm... Potrzebuję kilku #ziół. Pomożesz?",
				new MultipleActions(processStep));

		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("herb", "arandula", "yes", "ok", "tak", "dobrze", "ziół", "zioła"),
//				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "corpse&herbs"), new NotCondition(new PlayerHasItemWithHimCondition("arandula"))),
				ConversationStates.ATTENDING,
				"Na górę od Semos koło trzech wzgórz rośnie zioło zwane arandula. Oto rysunek, który narysowałam. Teraz już wiesz czego szukać. Wystarczy mi jedna sztuka. Jeśli dalej masz problem to skorzystaj z uprzejmości innych graczy. Napisz '/k wiadomość'",
				new ExamineChatAction("arandula.png", "Ilisa rysuje", "Arandula"));

		npc.add(
				ConversationStates.ATTENDING,
				"tad",
				null,
				ConversationStates.ATTENDING,
				"Potrzebuje silnego lekarstwa, aby się wyleczyć. Oferuje dobrą nagrodę temu kto mu pomoże.",
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

		final List<ChatAction> processStep = new LinkedList<ChatAction>();
		processStep.add(new DropItemAction("arandula"));
		processStep.add(new IncreaseXPAction(200));
		processStep.add(new SetQuestAction(QUEST_SLOT, "eliksir"));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "corpse&herbs"),
						new PlayerHasItemWithHimCondition("arandula")),
				ConversationStates.ATTENDING, 
				"Dobrze! Dziękuję. Teraz wymieszam... szczypta tego... i kilka kropli... jest! Przekaż #Tadowi, żeby szybko po nie przyszedł. Chcę zobaczyć jak się ma.",
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
		processStep.add(new IncreaseXPAction(500));
		processStep.add(new IncreaseAtkXPAction(200));
		processStep.add(new IncreaseDefXPAction(200));
		processStep.add(new EquipItemAction("płaszcz karmazynowy", 1, true));
		processStep.add(new EquipItemAction("butelka wody", 50, true));
		processStep.add(new EquipItemAction("zwój semos", 10, true));
		processStep.add(new EquipItemAction("tarcza królewska", 1, true));
		processStep.add(new EquipItemAction("miecz", 1, true));
		processStep.add(new SetQuestAction(QUEST_SLOT, "done"));
		
		// note Ilisa is spelled with a small i here because I
		// and l cannot be told apart in game
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "eliksir")),
				ConversationStates.ATTENDING, "Dziękuję!!! Idę porozmawiać z #Ilisa tak szybko jak tylko mogę. Trzymaj w nagrodę moją tarczę i płaszcz. Lata temu dostałem je od samego króla ale jeszcze powinny dobrze służyć. Weź też zapas wody i kilka map Semos. Dzięki nim wrócisz do miasta bez zbędnego chodzenia.",
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
                "Dowidzenia. Nie zapomnij sprawdzić Tada.  Mam nadzieje, że czuję się lepiej.",
                null);

        npc.add(ConversationStates.ATTENDING, 
        		ConversationPhrases.GOODBYE_MESSAGES,
        		new QuestNotStartedCondition(QUEST_SLOT),
                ConversationStates.IDLE,
                "Żegnaj. Spotkałeś Tada w hostelu? Jeżeli będziesz miał okazję to proszę zaglądnij do niego. Słyszałam, że nie czuje się najlepiej. Hostel możesz znaleść w wiosce Semos nad Bankiem.",
                null);

	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Lekarstwo dla Tada",
				"Tad chłopiec w hostelu Semos, potrzebuje pomocy w zdobyciu lekarstwa.",
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
