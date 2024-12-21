/* $Id: NavinNPC.java,v 1.9 2024/12/17 2:17:45 davvids Exp $ */
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


public class NavinNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Navin") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(49, 13));
				nodes.add(new Node(86, 13));
				nodes.add(new Node(86, 14));
				nodes.add(new Node(105, 14));
				nodes.add(new Node(105, 13));
				nodes.add(new Node(122, 13));
				nodes.add(new Node(122, 46));
				nodes.add(new Node(99, 46));
				nodes.add(new Node(99, 27));
				nodes.add(new Node(80, 27));
				nodes.add(new Node(80, 24));
				nodes.add(new Node(62, 24));
				nodes.add(new Node(62, 25));
				nodes.add(new Node(49, 25));
				setPath(new FixedPath(nodes, true));
			}

			@Override
protected void createDialog() {

    String greetingBasis = "Witaj, wojowniku! Uwielbiam krążyć po Semos, więc znam każdy budynek i zakątek miasta. Mogę opowiedzieć Ci o #banku, #ratuszu, #kuźni, #mgły, #źródełka #świątyni, #domku #mistrzów #gry, #skillowaniu i innych ważnych miejscach. Pytaj, jeśli chcesz się czegoś dowiedzieć!";

    add(
            ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                    new LevelLessThanCondition(20)
            ),
            ConversationStates.ATTENDING,
            greetingBasis + "Pamiętaj, że słowa podświetlone na niebiesko możesz wpisać, by uzyskać więcej informacji!",
            null);

    addHelp("Możesz zapytać mnie o #bank, #ratusz, #kuźnię, #świątynię, #skillowanie, #mgły, #źródełka lub o to, gdzie znaleźć #mistrzów #gry.");

    addReply(Arrays.asList("bank", "przechowywanie", "bank semos", "banku"),
             "Bank znajduje się niedaleko centrum Semos. Możesz tam bezpiecznie przechowywać swoje przedmioty i złoto oraz pohandlować z innymi graczami.");

    addReply(Arrays.asList("ratusz", "zadania ratusza", "burmistrz", "ratuszu"),
             "Ratusz znajduje się na samej górze, po środku miasta. Znajdziesz tam burmistrza, który zleca odnawialne zadania. Warto je wykonywać, by zdobyć kryształy do #skillowania i inne nagrody.");

    addReply(Arrays.asList("kuźnia", "kowal", "naprawianie ekwipunku", "kuźni", "kuźnię"),
             "Kuźnia jest miejscem, gdzie możesz kupić zbroje i bronie. Kowal może też przetopić rudę żelaza na sztabki żelaza potrzebne do zadań. Znajdziesz ją u góry po prawej stronie miasta.");

    addReply(Arrays.asList("świątynia", "kapłanka", "leczenie", "świątyni", "świątynie", "świątynię"),
             "Świątynia to miejsce, gdzie spotkasz kapłankę Illisę. Możesz zostać uleczony i przyjrzeć się bliżej magicznej kuli.");

    addReply(Arrays.asList("domek mistrza gry", "mistrzowie gry", "zadania", "domków mistrzów gry", "mistrzów gry", "domku mistrza gry"),
             "Domek mistrza gry znajduje się na lewo od centrum miasta w pobliżu domku startowego. To najważniejsze miejsce dla wszystkich wojowników. Znajdziesz tam pierwsze zadania, które przeprowadzą cię przez mechaniki gry oraz odblokują nowe lokacje.");

    addReply(Arrays.asList("skillowanie", "arena", "trening", "skillowaniu", "skillowania"),
             "Skillowanie pozwala rozwijać twoje umiejętności ataku i obrony Polega na walczeniu z innymi wojownikami lub potworami. Spójrz na statystyki pod minimapką w prawym górnym rogu, znajdziesz tam swój ATK oraz OBR. Pierwsza cyfra to Twoje punkty skilli, druga to Twój atak lub obrona wynikające z założonych przedmiotów. Liczby w nawiasach informują ile razy konieczne jest zadanie obrażenia, aby zwiększyć punkty Twoich skilli. Możesz trenować pod drzewami pod bankiem Semos lub udać się na arenę, gdzie znajdziesz #mgły i #źródełka. Wejściówki na arenę pozyskasz wykonując zadania odnawialne u różnych NPCów.");

    addReply(Arrays.asList("mgły", "kryształy", "trening mgieł"),
             "Będąc na arenie do #skillowania możesz zwiększyć swoje umiejętności. Mgły to miejsce na arenie treningowej, gdzie możesz rozwijać punkty ataku i musisz zwracać uwagę na swoje punkty życia, aby nie umrzeć.");

    addReply(Arrays.asList("źródełka", "trening obrony", "leczenie"),
             "Źródełka są miejscem, gdzie możesz bezpiecznie trenować umiejętności walki, ponieważ twoje zdrowie jest tam stale odnawiane. Aby tam wejść, potrzebujesz czerwonych kryształów które zdobędziesz wykonując zadania odnawialne.");

    addReply(Arrays.asList("domek zielarza", "zielarz", "zioła"),
             "Domek zielarza znajduje się w lewej części miasta w pobliżu kamieniołomu. Dowiesz się tam gdzie zbierać konkretne rośliny lecznicze i wytworzysz eliksiry.");

    addReply(Arrays.asList("biblioteka", "książki", "lore"),
             "Biblioteka Semos znajduje się obok banku. Znajdziesz w niej bibliotekarza Ceryla i Zynn Iwuhosema. Być może opowiedzą Ci ciekawe historie związane z krainą Faumonii.");

    addReply(Arrays.asList("tawerna", "odpoczynek", "skrzyneczka"),
             "W tawernie możesz odpocząć i spotkać innych graczy. Znajduje się ona w centranej części miasta, na prawo od podziemii. Pamiętaj też o skrzynce pod tawerną, gdzie silniejsi wojownicy zostawiają przedmioty dla potrzebujących.");

    addQuest("Chętnie opowiem Ci o Semos i nauczę podstaw gry. Możes zapytać mnie o #bank, #ratusz, #kuźnię, #świątynię, #skillowanie, #mgły, #źródełka lub o to, gdzie znaleźć #mistrzów #gry.");
    addGoodbye("Powodzenia, wojowniku! Nie zapomnij odwiedzić #domku #mistrza #gry oraz zapytać o zadania w ratuszu. Trzymaj się!");
}

			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.addInitChatMessage(null, new ChatAction() {
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (!player.hasQuest("NavinFirstChat")) {
					player.setQuest("NavinFirstChat", "done");
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
			}
		});

		npc.setEntityClass("NavinNPC");
		npc.setDescription("To Navin, mieszkaniec Semos, który zna wszystkie budynki i zakątki miasta. Chętnie pomaga nowym graczom.");
		npc.setPosition(48, 13);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		npc.setPerceptionRange(4);
		zone.add(npc);
	}
}
