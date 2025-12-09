/* $Id: DregoNPC.java,v 1.9 2024/12/17 2:17:45 davvids Exp $ */
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

public class DregoNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Drego") {

			@Override
			protected void createPath() {
				setPath(null);
			}

    @Override
protected void createDialog() {

    String greetingBasis = "Witaj, wojowniku! Czy chciałbyś się dowiedzieć czegoś o okolicy? Znam wiele miejsc, gdzie możesz zdobywać doświadczenie. Mogę opowiedzieć o #podziemiach, #skrzynce #z #przedmiotami oraz #expowiskach, takich jak #wioska #startowa, #farma #Ados, #wioska #gnomów, #osada #skrzatów, #niedźwiedzia #dolina, czy #osada #gargulców. Pytaj śmiało!";

    add(
            ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                    new LevelLessThanCondition(600)
            ),
            ConversationStates.ATTENDING,
            greetingBasis + "Pamiętaj, że słowa podświetlone na niebiesko to te, których możesz użyć w rozmowie ze mną.",
            null);

    addHelp("Możesz zapytać mnie o #expowiska, takie jak #wioska #startowa, #farma #Ados, #wioska #gnomów, #osada #skrzatów, #niedźwiedzia #dolina i #osada #gargulców. Mogę też opowiedzieć o #podziemiach i #skrzynce #z #przedmiotami.");

    addReply(Arrays.asList("expowiska", "gdzie expić", "gdzie zdobywać doświadczenie", "expowiskach"),
             "Mogę opowiedzieć o następujących miejscach do zdobywania doświadczenia: #wioska #startowa, #farma #Ados, #wioska #gnomów, #osada #skrzatów, #niedźwiedzia #dolina, #osada #gargulców oraz #podziemiach #Semos. Zapytaj o dowolne miejsce, a wyjaśnię szczegóły!");

    addReply(Arrays.asList("podziemia", "expienie pod ziemią", "tawerna podziemia", "podziemiach", "podziemiach Semos", "podziemi"),
             "Podziemia Semos znajdują się tuż obok tawerny, kieruj się w lewo. To świetne miejsce, aby zdobyć podstawowy ekwipunek i doświadczenie, szczególnie na niższych poziomach.");

    addReply(Arrays.asList("wioska startowa", "szczury", "expowisko wioski"),
             "Wioska startowa znajduje się niedaleko stąd, idąc w lewo. To miejsce pełne szczurów, idealnych do zdobywania złota, podstawowego ekwipunku i doświadczenia.");

    addReply(Arrays.asList("farma ados", "wioska ados", "expowisko ados"),
             "Farma Ados znajduje się niedaleko stąd, wybierz się 2 mapy w prawo. Znajdziesz tam kurniki, świnie, krowy i konie, które są świetnym źródłem pożywienia oraz surowców.");

    addReply(Arrays.asList("wioska gnomów", "gnomy", "gnomowe expowisko"),
             "Wioska gnomów to miejsce, gdzie zdobędziesz lecznicze przedmioty i ekwipunek. Idealne miejsce dla wojowników z 10 poziomem. Znajdziesz ją kierując się mapą w lewo i w górę.");

    addReply(Arrays.asList("osada skrzatów", "skrzaty", "leśne skrzaty"),
             "Osada skrzatów jest znana z przedmiotów leczniczych, takich jak miód piwny czy żyntyca. Znajdziesz ją idąc cały czas w lewo. Dodatkowo na zachodzie tej osady znajdziesz cenne pokrzywy. Jednak po drodze uważaj na zbójników - mogą Cię bardzo łatwo zabić!");

    addReply(Arrays.asList("niedźwiedzia dolina", "niedźwiedzie", "dolina"),
             "W niedźwiedziej dolinie możesz zdobyć mięso, pazury i kły, które są przydatne w niektórych zadaniach. Znajdziesz ją kierując się 2 mapy w prawo oraz jedną w górę. To miejsce jest bardziej wymagające, ale mając 10 poziom doświadczenia powinieneś dać radę.");

    addReply(Arrays.asList("osada gargulców", "gargulce", "posągi"),
             "Osada gargulców jest ukryta w lasach Faumonii, kieruj się cały czas w dół. Jest to idealne miejsce na poznanie lepiej krainy Faumonii. Jednak uważaj! Znajdują się tam również silniejsze potwory, wybierz się tam dopiero po przekroczeniu 10 poziomu doświadczenia.");

    addReply(Arrays.asList("skrzynka", "skrzyneczka", "pomoc", "skrzynce z przedmiotami", "skrzynce"),
             "Za murkiem poniżej znajdziesz skrzynkę, do której wrzucają czasem wrzucają niepotrzebne przedmioty. Może znajdziesz tam coś, co pomoże ci w Twojej przygodzie?");

    addQuest("Moim zadaniem jest wskazanie ci najlepszych miejsc do zdobywania doświadczenia. Jeśli nie wiesz, gdzie iść i co robić, zapytaj o #expowiska, a znajdę coś dla ciebie!");
    addGoodbye("Powodzenia, wojowniku! Pamiętaj o #skrzynce i zajrzyj do #podziemi. Trzymaj się!");
}


			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.addInitChatMessage(null, new ChatAction() {
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (!player.hasQuest("DregoFirstChat")) {
					player.setQuest("DregoFirstChat", "done");
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
			}
		});

		npc.setEntityClass("DregoNPC");
		npc.setDescription("Oto Drego, wojownik odpoczywający przed tawerną. Zna wszystkie najbliższe expowiska i podpowie, gdzie łatwo zdobywać doświadczenie.");
		npc.setPosition(113, 41);
		npc.setDirection(Direction.LEFT);
		npc.initHP(100);
		npc.setPerceptionRange(4);
		zone.add(npc);
	}
}
