/* $Id: HealerNPC.java,v 1.19 2010/09/19 02:35:36 nhnb Exp $ */
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
package games.stendhal.server.maps.semos.city;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.HealerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A young lady (original name: Carmen) who heals players without charge. 
 */
public class HealerNPC implements ZoneConfigurator {
	
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Carmen") {
			@Override
			public void createDialog() {
				addReply("tematy", "Oczywiście, mam dla ciebie chwilę. Powiedz, czy potrzebujesz leczenia, zapasu lekarstw, czy chcesz wrócić do mojej prośby.");
				addGreeting("Witaj, dobrze cię widzieć. Jeśli droga dała ci się we znaki, powiedz #ulecz, a sprawdzę, czy mogę ci pomóc. Mam też pewne #zadanie dla kogoś, kto zechce pomóc rannym w Semos.");
				addJob("Pomagam rannym, korzystając z uzdrawiającej mocy i przygotowanych lekarstw. Jedni trafiają do mnie po walce, inni chcą po prostu dobrze przygotować się do drogi. Staram się znaleźć chwilę dla każdego, choć moje umiejętności mają swoje granice.");
				addHelp("Jeśli moje umiejętności wystarczą, uleczę cię bez opłat - powiedz #ulecz. Przygotowuję też lekarstwa na drogę, bo daleko od miasta nie zawsze znajdzie się ktoś do pomocy. Zapytaj o moją #ofertę, jeśli chcesz uzupełnić zapasy.");
				addEmotionReply("hugs", "hugs");
				addGoodbye("Do zobaczenia. Uważaj na siebie i wróć, jeśli będziesz potrzebować pomocy.");
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(68, 46));
				nodes.add(new Node(81, 46));
				setPath(new FixedPath(nodes, true));
			}
		};
		String offer = "W drodze warto mieć przy sobie zapas lekarstw. Sprzedaję " + games.stendhal.common.grammar.Grammar.enumerateCollection(SingletonRepository.getShopList().get("healing").keySet()) + ". Wybierz to, czego potrzebujesz.";
		npc.addOffer(offer); npc.addReply(java.util.Arrays.asList("sklep", "shop"), offer);
		new SellerAdder().addSeller(npc, new SellerBehaviour(SingletonRepository.getShopList().get("healing")), false);
		new HealerAdder().addHealer(npc, 0);
		npc.setPosition(68, 46);
		npc.setDescription("Widzisz Carmen, uzdrowicielkę z Semos. Wita cię łagodnym uśmiechem i odkłada na chwilę swoje zajęcie, by cię wysłuchać.");
		npc.setEntityClass("welcomernpc");
		zone.add(npc);
	}
}
