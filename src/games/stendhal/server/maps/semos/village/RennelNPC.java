/* $Id: RennelNPC.java,v 1.9 2024/12/17 2:07:45 davvids Exp $ */
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

public class RennelNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Rennel") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
protected void createDialog() {

    String greetingBasis = "Witaj, wojowniku! Znam Semos jak własną kieszeń. Jeżeli tylko chcesz to mogę opowiedzieć ci o #banku, #ratuszu, #sklepie, #domku #mistrza #gry, #skillowaniu, #arenie i innych miejscach w okolicy. Zapytaj, jeśli coś Cie interesuje!";

    add(
            ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                    new LevelLessThanCondition(20)
            ),
            ConversationStates.ATTENDING,
            greetingBasis + "Pamiętaj, że słowa podświetlone na niebiesko to te, których możesz użyć w rozmowie ze mną!",
            null);

    addHelp("Możesz zapytać mnie o #bank, #ratusz, #sklep, #domek #mistrza #gry, #skillowanie albo #arenę. Jeśli się zgubisz, pomogę!");

    addReply(Arrays.asList("bank", "przechowywanie", "bank semos", "banku"),
             "Bank znajduje się tuż obok mnie, to ten zielony budynek na górze. Możesz tam przechowywać swoje przedmioty i złoto, aby je zabezpieczyć przed utratą.");

    addReply(Arrays.asList("ratusz", "urzędnik", "burmistrz"),
             "Ratusz znajdziesz idąc w górę i w prawo. Możesz tam porozmawiać z #burmistrzem, który codziennie ma dla Ciebie zadanie do wykonania, za które dostaniesz kamyczki na #arenę #treningową.");

    addReply(Arrays.asList("domek mistrza gry", "mistrz gry", "mistrzowie gry", "domku mistrza gry", "domku mistrzów gry"),
             "Domek mistrzów gry to miejsce do którego powinieneś często wracać. Znajduje się on na prawo od domku startowego - jest czerwony. Mistrzowie gry zlecają #zadania, które przeprowadzą Cie przez całą grę. Jeżeli nie masz pomysłu co robić - skieruj się do Mistrzów Gry!");

    addReply(Arrays.asList("skillowanie", "skille", "trening", "arena", "skillowaniu", "arenie", "arenę", "arenę treningową", "arene treningowa", "skillowania"),
             "Skillowanie pozwala rozwijać twoje umiejętności ataku i obrony. Poprzez walczenie z innymi wojownikami lub potworami. Spójrz na statystyki pod minimapką w prawym górnym rogu, znajdziesz tam swój ATK oraz OBR. Pierwsza cyfra to Twoje punkty skilli, druga to Twój atak lub obrona wynikające z założonych przedmiotów. Liczby w nawiasach informują ile razy konieczne jest zadanie obrażenia, aby zwiększyć punkty skilli." +
             " Możesz trenować pod drzewami pod bankiem Semos lub udać się na arenę, gdzie znajdziesz mgły i źródełka. Wejściówki na arenę pozyskasz wykonując zadania odnawialne u różnych NPCów.");

    addReply(Arrays.asList("mgły", "kryształy", "źródełka"),
             "Będąc na arenie do #skillowania możesz zwiększyć swoje umiejętności. Wejściówki na arenę pozyskasz wykonując zadania odnawialne. W nagrodę otrzymasz niebieskie lub czerwone kryształy. Niebieskie kryształy umożliwiają wejście na mgły - jest to miejsce na arenie, gdzie możesz rozwijać punkty ataku i musisz zwracać uwagę na swoje punkty życia, aby nie umrzeć. Źródełka pozwalają trenować bez konieczności ręcznego leczenia oraz umożliwiają skillowanie z innymi graczami, co znacząco przyspiesza zdobywanie skilli.");

    addReply(Arrays.asList("zadania", "questy", "misje"),
             "Zadania znacząco rozwijają Twoją postać. Jeżeli nie masz pomysłu jakie zadanie wykonać, udaj się do #domku #mistrzów #gry w który znajduje się obok domku startowego. Wykonuj je, aby eksplorować krainę Faumonii oraz zdobywać doświadczenie, złoto i cenne przedmioty.");

    addReply(Arrays.asList("burmistrz", "zadania burmistrza", "ratuszowe zadania", "burmistrzem"),
             "Burmistrz w ratuszu zleca odnawialne zadanie na pokonanie potwora, dzięki któremu możesz zdobyć kryształy umożliwiające wstęp na #arenę #treningową.");

    addReply(Arrays.asList("sklep", "kupowanie", "sprzedawanie", "sklepie"),
             "Sklep znajduje się w lewej części miasta Semos, w pobliżu kamieniołomu. Możesz tam sprzedać podstawowe przedmioty, ale miej oko na ceny – strasznie tam zdzierają! Lepiej wybierz się do banku lub skorzystaj z globalnego czatu pisząc #/k #wiadomość i sprzedaj je innym wojownikom.");

    addGoodbye("Dowidzenia, wojowniku! Jeśli czegoś nie wiesz, zawsze pytaj na czacie globalnym używając #/k #wiadomość. Powodzenia!");
}

			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.addInitChatMessage(null, new ChatAction() {
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (!player.hasQuest("RennelFirstChat")) {
					player.setQuest("RennelFirstChat", "done");
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
			}
		});

		npc.setEntityClass("RennelNPC");
		npc.setDescription("To Rennel, przewodnik po Semos. Zna wszystkie budynki i tajniki miasta.");
		npc.setPosition(69, 33);
		npc.setDirection(Direction.UP);
		npc.initHP(100);
		npc.setPerceptionRange(4);
		zone.add(npc);
	}
}
