/* $Id: EldirNPC.java,v 1.9 2024/12/16 4:17:45 davvids Exp $ */
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

public class EldirNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Eldir") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
protected void createDialog() {
    
    String greetingBasis = "Witaj, wojowniku! Jestem tutaj, aby pomóc ci zrozumieć podstawy gry. ";

    add(
            ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                    new LevelLessThanCondition(600)
            ),
            ConversationStates.ATTENDING,
            greetingBasis + "To miejsce to samouczek, który przygotuje cię do dalszej przygody. Mogę opowiedzieć Ci o podnoszeniu #przedmiotów, #miksturach, #zadaniach, #mistrzach #gry, #czacie #globalnym oraz wielu innych rzeczach. Zapytaj śmiało, jeśli potrzebujesz pomocy! Powinieneś używać słów podświetlonych na niebiesko.",
            null);

    addHelp("Możesz zadać mi pytania o #czat #globalny, #podnoszenie #przedmiotów, #mikstury, #zadania, #walkę albo #mistrzów #gry. Pamiętaj, że słowa podświetlone na niebiesko to sposób do dalszej rozmowy!");

    addReply(Arrays.asList("podnoszenie przedmiotów", "przedmioty", "podnoszenie", "przedmiotów"),
             "Podnoszenie przedmiotów jest bardzo proste. Kliknij na przedmiot leżący na ziemi, przytrzymaj go i przeciągnij do swojego #plecaka. W ten sam sposób możesz przenosić przedmioty w swoim ekwipunku.");

    addReply(Arrays.asList("mikstury", "mikstura", "używanie mikstur", "miksturach", "mikstur"),
             "Mikstury i jedzenie pomagają odzyskać zdrowie. Aby użyć mikstury, kliknij na nią prawym przyciskiem myszy i wybierz 'Użyj'. Znajdziesz je w poległych potworach oraz otrzymas za wykonywanie zadań.");

    addReply(Arrays.asList("walka", "atakowanie", "potwory", "walkę"),
             "Aby zaatakować potwora, kliknij na niego prawym przyciskiem myszy i wybierz opcję 'Atakuj'. Pamiętaj, że niektóre potwory, jak szczury, mogą uciekać – będziesz musiał je gonić, aby je pokonać. Uważaj też na swoje zdrowie i korzystaj z #mikstur, gdy zajdzie potrzeba!");

    addReply(Arrays.asList("zadania", "questy", "quest", "zadaniach"),
             "Zadania są niezbędne do zdobywania doświadczenia i postępu na samym początku gry. Wyjdź z domku i udaj się do #mistrzów #gry w czerwonym domku po prawej stronie – tam otrzymasz pierwsze zadania które pomogą Ci poznać krainę Faumonii.");

    addReply(Arrays.asList("mistrzowie gry", "mistrzowie", "mistrz gry", "mistrzach gry", "mistrzach", "mistrzów gry"),
             "Mistrzowie gry znajdują się w czerwonym domku na zewnątrz. Pomogą ci poznać mechaniki gry, zlecą pierwsze #zadania i przygotują cię do dalszej przygody.");

    addReply(Arrays.asList("plecak", "ekwipunek", "przedmioty w plecaku", "plecaka"),
             "Plecak to miejsce, gdzie przechowujesz swoje przedmioty. Znajduje się po prawej stronie ekranu, pod Twoim ekwipunkiem..");

    addReply(Arrays.asList("czat", "czat globalny", "globalny czat", "czacie globalnym"),
             "Czat globalny to miejsce, gdzie możesz zadać pytanie innym graczom. Wpisz #/k #wiadomość, aby poprosić o pomoc lub porady.");

    addQuest("Moim zadaniem jest pomaganie nowym wojownikom. Jeśli czegoś nie wiesz, pytaj o słowa podświetlone na niebiesko – wszystko ci wyjaśnię!");
    addGoodbye("Powodzenia, wojowniku! Koniecznie odwiedź czerwony domek na zewnątrz i wykonaj pierwsze #zadania. Pamiętaj, że na #czacie #globalnym zawsze znajdziesz pomoc. Trzymaj się!");
}
			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.addInitChatMessage(null, new ChatAction() {
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (!player.hasQuest("EldirFirstChat")) {
					player.setQuest("EldirFirstChat", "done");
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
			}
		});

		npc.setEntityClass("EldirNPC");
		npc.setDescription("To Eldir, przewodnik w domku startowym. Pomoże Ci on poznać podstawy gry i przygotować się do pierwszych wyzwań.");
		npc.setPosition(13, 35);
		npc.setDirection(Direction.LEFT);
		npc.initHP(100);
		npc.setPerceptionRange(4);
		zone.add(npc);
	}
}
