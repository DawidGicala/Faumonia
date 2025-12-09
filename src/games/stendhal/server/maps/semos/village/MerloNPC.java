/* $Id: MerloNPC.java,v 1.9 2024/12/17 2:08:45 davvids Exp $ */
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

public class MerloNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Merlo") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
protected void createDialog() {

    String greetingBasis = "Witaj, wojowniku! Jestem Merlo i znam się na #handlu. Mogę opowiedzieć, jak #kupować, #sprzedawać, korzystać z #globalnego #czatu i wystawiać #tabliczki #z #ogłoszeniami. Zapytaj mnie o #kupowanie, #sprzedaż lub #ogłoszenia, a wszystko ci wyjaśnię!";

    add(
            ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                    new LevelLessThanCondition(600)
            ),
            ConversationStates.ATTENDING,
            greetingBasis + "Pamiętaj, że słowa podświetlone na niebiesko to te, których możesz użyć w rozmowie ze mną.",
            null);

    addHelp("Możesz zapytać o #handlowanie, #sprzedaż, #kupowanie, #tabliczki lub o #globalny #czat.");

    addReply(Arrays.asList("handel", "handlowanie", "kupowanie", "sprzedawanie", "handlu"),
             "Handlowanie to niezbędna część życia. Aby #sprzedać lub #kupić przedmiot u NPC, musisz najpierw przywitać się i użyć słów takich jak oferta, kup, lub sprzedaj.");

    addReply(Arrays.asList("kupowanie", "jak kupować", "kup", "kupować", "kupić"),
             "Aby coś kupić, porozmawiaj z NPC i użyj słowa kluczowego 'kup' lub zapytaj go, czym 'handluje'. Na przykład: '#kup #mikstura'.");

    addReply(Arrays.asList("sprzedawanie", "jak sprzedawać", "sprzedaj", "sprzedawać", "sprzedaż"),
             "Jeśli chcesz coś sprzedać, porozmawiaj z NPC i użyj słowa 'sprzedaj'. Na przykład: '#sprzedaj #statuetka #aniołka'. Możesz sprzedać ją Furgo, który stoi tutaj w banku.");

    addReply(Arrays.asList("skupowanie", "skup", "gdzie sprzedać", "sprzedać"),
             "Niektórzy NPC skupują konkretne przedmioty. Na przykład Furgo skupuje statuetkę aniołka z jednego z zadań. Upewnij się, że pytasz NPC, czym handluje, pisząc 'oferta.");

    addReply(Arrays.asList("czat", "globalny czat", "czat globalny", "globalnego czatu", "ogłoszenia"),
             "Globalny czat to miejsce, gdzie możesz zadać pytanie innym graczom lub coś ogłosić. Wpisz #/k i swoją wiadomość, aby poprosić o pomoc lub sprzedać przedmioty.");

    addReply(Arrays.asList("tabliczki", "wystawianie ogłoszeń", "tabliczki z ogłoszeniami"),
             "Jeśli chcesz coś ogłosić, możesz również wystawić tabliczkę z ogłoszeniem w Semos u Gordona. Znajdziesz go w dolnej części miasta koło skrzynki. Tabliczki znikają po 24 godzinach.");

    addReply(Arrays.asList("furgo", "skupowanie u furgo", "statuetka aniołka"),
             "Furgo znajduje się po mojej prawej stronie. Możesz sprzedać mu statuetkę aniołka, pisząc '#sprzedaj #statuetka #aniołka'.");

    addReply(Arrays.asList("ogłoszenia na discordzie", "discord", "sprzedaż na discordzie"),
             "Nie zapomnij, że możesz też wystawić ogłoszenie na kanale rynkowym na Discordzie Faumonii. Warto spróbować, jeśli chcesz szybko znaleźć kupca!");

    addQuest("Moim zadaniem jest nauczenie cię podstaw #handlu i korzystania z #globalnego #czatu. Jeśli masz pytania, śmiało pytaj o #kupowanie, #sprzedaż lub #tabliczki #z #ogłoszeniami!");
    addGoodbye("Powodzenia, wojowniku! Pamiętaj, aby korzystać z #globalnego #czatu i sprawdzać #tabliczki #z #ogłoszeniami. Trzymaj się!");
}

			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.addInitChatMessage(null, new ChatAction() {
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (!player.hasQuest("MerloFirstChat")) {
					player.setQuest("MerloFirstChat", "done");
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
			}
		});

		npc.setEntityClass("MerloNPC");
		npc.setDescription("Widzisz Merlo, specjalistę od handlu i ogłoszeń. Na pewno doradzi, jak sprzedawać przedmioty i korzystać z globalnego czatu.");
		npc.setPosition(35, 6);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		npc.setPerceptionRange(4);
		zone.add(npc);
	}
}
