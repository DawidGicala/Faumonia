/* $Id: HyrikNPC.java,v 1.9 2024/12/16 2:10:45 davvids Exp $ */
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

public class HyrikNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Hyrik") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
protected void createDialog() {

    String greetingBasis = "Witaj, wojowniku! Jestem Hyrik i pomogę ci postawić pierwsze kroki w Faumonii.";

    add(
            ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                    new LevelLessThanCondition(600)
            ),
            ConversationStates.ATTENDING,
            greetingBasis + " Wyjdź z domku i zacznij od walki z potworami. Możesz mnie zapytać, jak #atakować, jak się #leczyć albo jak #podnosić #przedmioty. Jeśli masz pytania, śmiało – słowa podświetlone na niebiesko to te których powinieneś używać w rozmowie!",
            null);

    addHelp("Możesz mnie zapytać o #walkę, #leczenie, #przedmioty i inne podstawowe rzeczy. Nie bój się eksplorować terenu przed domkiem!");

    addReply(Arrays.asList("walka", "atakowanie", "jak walczyć", "walke", "walkę"),
             "Aby zaatakować potwora, kliknij na niego prawym przyciskiem myszy i wybierz opcję 'Atakuj'. Niektóre potwory, jak szczury, mogą próbować uciekać, więc czasem musisz za nimi pobiec. Zawsze uważaj na swoje zdrowie!");

    addReply(Arrays.asList("leczenie", "mikstury", "zdrowie", "leczyć", "jak się leczyć", "leczenia"),
             "Gdy twoje zdrowie spada, używaj mikstur lub jedzenia. Kliknij na przedmiot prawym przyciskiem myszy i wybierz opcję 'Użyj'. Mikstury znajdziesz u kupców, dostaniesz za wykonanie zadań albo zdobędziesz je z potworów.");

	addReply(Arrays.asList("walka", "potwory", "jak walczyć", "walkę", "jak atakować", "atakować"),
             "Aby zaatakować, kliknij na potwora prawym przyciskiem myszy i wybierz opcję 'Atakuj'. Pamiętaj, że niektóre potwory, jak szczury, mogą uciekać. Warto też mieć pod ręką #mikstury, aby szybko odzyskać zdrowie.");

    addReply(Arrays.asList("przedmioty", "podnoszenie", "jak podnosić przedmioty", "podnosić przedmioty", "jak podnosić przedmioty"),
             "Aby podnieść przedmiot z ziemi, kliknij na niego, przytrzymaj i przeciągnij go do swojego plecaka. Twój plecak znajduje się po prawej stronie ekranu.");

    addReply(Arrays.asList("co robić", "gdzie iść", "co dalej"),
             "Wyjdź z domku i spróbuj walczyć z potworami w pobliżu. Zacznij od szczurów obok domku – są słabe i idealne na początek. Jeśli potrzebujesz jedzenia, poszukaj u lokalnych kupców lub zbierz coś w okolicy.");

    addReply(Arrays.asList("szczury", "jak walczyć ze szczurami", "gdzie są szczury"),
             "Szczury znajdziesz tuż za drzwiami domku startowego. Pokonaj kilka, aby zdobyć pierwsze doświadczenie i przedmioty. To świetny pomysł, aby nauczyć się podstaw walki!");

    addReply(Arrays.asList("ekwipunek", "plecak"),
             "Plecak to twoje główne miejsce na przedmioty. Znajdziesz go po prawej stronie ekranu. Pilnuj, aby zawsze mieć przy sobie coś do #leczenia.");

    addQuest("Twoim pierwszym krokiem powinna być walka ze szczurami i zdobycie podstawowych umiejętności. Nie bój się wychodzić na zewnątrz i eksplorować terenu wokół domku!");
    addGoodbye("Powodzenia, wojowniku! Wyjdź z domku, spróbuj swoich sił ze szczurami i wybierz się do czerwonego domku Mistrzów Gry. Jeśli czegoś nie wiesz, zawsze możesz zapytać na chacie globalnym pisząc #/k #wiadomość. Trzymaj się!");
}

			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.addInitChatMessage(null, new ChatAction() {
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (!player.hasQuest("HyrikFirstChat")) {
					player.setQuest("HyrikFirstChat", "done");
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
			}
		});

		npc.setEntityClass("HyrikNPC");
		npc.setDescription("Oto Hyrik, przewodnik z domku startowego w Semos. Pomoże Ci zrozumieć podstawy gry – walkę, leczenie i podnoszenie przedmiotów.");
		npc.setPosition(16, 11);
		npc.setDirection(Direction.LEFT);
		npc.initHP(100);
		npc.setPerceptionRange(4);
		zone.add(npc);
	}
}
