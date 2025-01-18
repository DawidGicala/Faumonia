/* $Id: JorinNPC.java,v 1.9 2024/12/17 4:07:45 davvids Exp $ */
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
package games.stendhal.server.maps.semos.village;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasShieldEquippedCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;

import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;


public class JorinNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildMineArea(zone, attributes);
	}

	private void buildMineArea(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		final SpeakerNPC npc = new SpeakerNPC("Jorin") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(5, 36));
				nodes.add(new Node(5, 42));
				nodes.add(new Node(12, 42));
				nodes.add(new Node(12, 45));
				nodes.add(new Node(22, 45));
				nodes.add(new Node(22, 48));
				nodes.add(new Node(31, 48));
				nodes.add(new Node(31, 52));
				nodes.add(new Node(17, 52));
				nodes.add(new Node(17, 55));
				nodes.add(new Node(8, 55));
				nodes.add(new Node(8, 43));
				nodes.add(new Node(5, 43));
				setPath(new FixedPath(nodes, true));
			}

			@Override
protected void createDialog() {

    String greetingBasis = "Witaj, młody wojowniku! Nazywam się Jorin i krążę po tej wiosce, aby pomóc ci zrozumieć podstawy gry. Mogę opowiedzieć Ci o #zadaniach, #przedmiotach, #mistrzach #gry, #barierze czy #miksturach.";

    add(
            ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                    new LevelLessThanCondition(600)
            ),
            ConversationStates.ATTENDING,
            greetingBasis + "Zapytaj o cokolwiek, co cię interesuje. Słowa podświetlone na niebiesko to słowa których możesz użyć, aby ze mną porozmawiać.",
            null);

    addHelp("Możesz zapytać o #zadania, #barierę, #mikstury, #przedmioty, #walkę lub #mistrzów #gry.");

    addReply(Arrays.asList("zadania", "questy", "misje", "zadaniach", "zadanie"),
             "Zadania to podstawa twojego rozwoju. Udaj się do mmistrzów #gry w czerwonym domku. Wykonaj pierwsze zadanie, aby nauczyć się podstaw walki i skończyć samouczek.");

    addReply(Arrays.asList("mistrzowie gry", "mistrz gry", "czerwony domek", "mistrzach gry", "mistrzów gry"),
             "Mistrzowie gry znajdują się w czerwonym domku. To oni zlecają zadania, które przeprowadzą cię przez całą grę. Koniecznie się tam wybierz!");

    addReply(Arrays.asList("bariera", "przejście", "blokada", "barierze", "barierę"),
             "Próbując przejść dalej w prawo natrafisz na barierę. Aby przejść dalej, wykonaj pierwsze #zadanie u #mistrzów #gry i osiągnij co najmniej 7 poziom. Porozmawiać z Sedro który stoi przy przejściu o szybkim zadaniu z serem.");

    addReply(Arrays.asList("walka", "potwory", "jak walczyć", "walkę"),
             "Aby zaatakować, kliknij na potwora prawym przyciskiem myszy i wybierz opcję 'Atakuj'. Pamiętaj, że niektóre potwory, jak szczury, mogą uciekać. Warto też mieć pod ręką #mikstury, aby szybko odzyskać zdrowie.");

    addReply(Arrays.asList("mikstury", "leczenie", "jak używać mikstur", "walka", "walke", "miksturach"),
             "Mikstury są niezbędne podczas walki. Aby ich użyć, kliknij na miksturę prawym przyciskiem myszy i wybierz opcję 'Użyj'. Możesz je kupić u kupców lub znaleźć w pokonanych potworach. Tak samo możesz zrobić z jedzeniem, np. serem ze szczurów.");

    addReply(Arrays.asList("przedmioty", "podnoszenie przedmiotów", "plecak", "przedmiotach"),
             "Podnoszenie przedmiotów jest bardzo proste. Kliknij na przedmiot na ziemi, przytrzymaj go i przeciągnij do plecaka. Plecak znajdziesz po prawej stronie ekranu.");

    addQuest("Twoim pierwszym krokiem powinno być udanie się do #mistrzów #gry w czerwonym domku i wykonanie prostego zadania ze szczurami. Pamiętaj, że na #globalnym #czacie znajdziesz dodatkową pomoc! W tym celu napisz #/k #wiadomość.");
    addGoodbye("Powodzenia. Twoim pierwszym krokiem powinno być udanie się do #mistrzów #gry w czerwonym domku i wykonanie prostego zadania ze szczurami.");
}

			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.addInitChatMessage(null, new ChatAction() {
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (!player.hasQuest("JorinFirstChat")) {
					player.setQuest("JorinFirstChat", "done");
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
			}
		});

		npc.setEntityClass("JorinNPC");
		npc.setDescription("To Jorin, przewodnik po samouczkowej części Semos. Pomoże Ci w pierwszych zadaniach i przygotowaniu do przejścia do dalszej części miasta.");
		npc.setPosition(5, 36);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		npc.setPerceptionRange(4);
		zone.add(npc);
	}
}
