/* $Id: ThyronNPC.java,v 1.9 2024/12/16 21:51:45 davvids Exp $ */
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

public class ThyronNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Thyron") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
protected void createDialog() {

    String greetingBasis = "Witaj, wojowniku! Stoję tutaj przy #przejściu, aby wyjaśnić, co musisz zrobić, by ruszyć dalej. " +
                           "Mogę opowiedzieć ci o #mistrzach #gry, #przejściu, #czerwonym #domku, #zadaniach, #jedzeniu, #piciu i wielu innych rzeczach. " +
                           "Zapytaj śmiało, a chętnie pomogę! Pamiętaj – słowa podświetlone na niebiesko to te, których możesz użyć w rozmowie!";

    add(
            ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                    new LevelLessThanCondition(600)
            ),
            ConversationStates.ATTENDING,
            greetingBasis + "Pamiętaj, aby przygotować się dobrze przed wyruszeniem dalej – wykonaj #zadanie w #czerwonym #domku po lewej stronie.",
            null);

    addHelp("Jeśli potrzebujesz szczegółów, zapytaj mnie o #mistrzów #gry, #barierę, #zadania, #jedzenie lub #picie. Pamiętaj – słowa podświetlone na niebiesko to klucz do dalszej rozmowy!");

    addReply(Arrays.asList("bariera", "przejście", "przejściu", "bariere"),
             "#Bariera blokuje przejście dalej, dopóki nie ukończysz samouczka. Musisz wykonać przynajmniej jedno #zadanie u #mistrza #gry i osiągnąć 7 poziom. " +
             "Przygotuj się dobrze – zadbaj o #jedzenie, #picie i podstawowe wyposażenie.");

    addReply(Arrays.asList("czerwony domek", "domku", "dom", "czerwonym domku"),
             "Czerwony domek znajduje się po lewej stronie od tego miejsca. To tam znajdziesz #mistrzów #gry, którzy zlecą ci pierwsze #zadania i przeprowadzą cię przez podstawy gry.");

    addReply(Arrays.asList("mistrz", "mistrzowie", "mistrz gry", "mistrzów gry", "mistrza gry"),
             "Mistrzowie gry, pomagają nowym graczom. Znajdziesz ich w #czerwonym #domku. Wykonaj ich #zadania, aby nauczyć się podstaw i ruszyć dalej!");

    addReply(Arrays.asList("zadanie", "zadania", "questy"),
             "Zadania to świetny sposób na zdobywanie doświadczenia, złota i przedmiotów. Zacznij podstawowej misji u #mistrza #gry w #czerwonym #domku. Jeśli nie wiesz, co robić, zapytaj na #czacie #globalnym!");

    addReply(Arrays.asList("jedzenie", "picie", "jedzenia", "picia"),
             "Jedzenie i picie są niezbędne, aby przetrwać podczas walki. Piekarz Leander w Semos robi świetne kanapki. Napojów szukaj w tawernie. Pamiętaj, że możesz używać ich klikając prawym przyciskiem i wybierając 'Użyj'.");

    addReply(Arrays.asList("czat", "czat globalny", "globalny czat", "czacie globalnym"),
             "Na czacie globalnym możesz zadawać pytania innym graczom. Wpisz #/k #wiadomość, aby poprosić o pomoc, porady lub dowiedzieć się czegoś więcej!");

    addReply(Arrays.asList("poziom", "level", "awans"),
             "Aby przejść przez #przejście, musisz zdobyć przynajmniej 7 poziom doświadczenia. Aby to zrobić powalcz ze szczurami, gnomami i koniecznie wykonaj #zadanie u #mistrza #gry, który nauczy Cie podstaw gry.");

    addQuest("Moim zadaniem jest pomaganie nowym graczom. Pamiętaj, aby przygotować się dobrze przed wyruszeniem dalej – wykonaj #zadanie w #czerwonym #domku po lewej stronie.");
    addGoodbye("Powodzenia! Pamiętaj, aby odwiedzić #czerwony #domek, wykonać pierwsze zadanie u #mistrza #gry i zapytać na #czacie #globalnym, jeśli czegoś nie wiesz!");
}
		};

		npc.addInitChatMessage(null, new ChatAction() {
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (!player.hasQuest("ThyronFirstChat")) {
					player.setQuest("ThyronFirstChat", "done");
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
			}
		});

		npc.setEntityClass("ThyronNPC");
		npc.setDescription("Oto Thyron, wojownik który stoi przy #przejściu, aby pomagać nowym graczom. Marzy, by w przyszłości zostać zawodowym strażnikiem miejskim.");
		npc.setPosition(60, 43);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		npc.setPerceptionRange(4);
		zone.add(npc);
	}
}
