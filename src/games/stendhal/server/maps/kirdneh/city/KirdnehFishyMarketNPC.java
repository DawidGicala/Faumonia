/* $Id: KirdnehFishyMarketNPC.java,v 1.8 2012/08/23 20:05:43 yoriy Exp $ */
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
package games.stendhal.server.maps.kirdneh.city;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * In Kirdneh open market .
 */
public class KirdnehFishyMarketNPC implements ZoneConfigurator {
    private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildfishyguy(zone);
	}

	private void buildfishyguy(final StendhalRPZone zone) {
		final SpeakerNPC fishyguy = new SpeakerNPC("Fishmonger") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(63, 89));
				nodes.add(new Node(63, 88));				
				nodes.add(new Node(64, 88));
				nodes.add(new Node(64, 87));
				nodes.add(new Node(68, 87));
				nodes.add(new Node(68, 89));
				setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				addGreeting("Ahoj! Widzę, że wyglądasz na groźnego.");
				addJob("Na moce! Ja będę skupował. Ty będziesz sprzedawał?");
				addReply(Arrays.asList("yes", "tak"), "Cóż dreszcz mnie przeszedł! Sprawdź tablicę, aby zobaczyć po jakiej cenie i co skupuję");
				addReply("aye", "Cóż dreszcz mnie przeszedł! Sprawdź tablicę, aby zobaczyć po jakiej cenie i co skupuję");
				addReply(Arrays.asList("nie", "no"), "Ty tchórzliwa lilio, łobuzie! Dlaczego marnujesz mój czas?");
				addHelp("Co sobie myślisz, że taki jak ja stary wyjadacz potrzebuje pomocy?");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buyfishes")), false);

				addOffer("Sprawdź na tablicy ile dublonów mogę dać.");
				addQuest("Nie masz towaru, którego ja potrzebuję.");
				addGoodbye("Arrgh, zostań pójdę z tobą!");

			}
		};

		fishyguy.setEntityClass("sailor1npc");
		fishyguy.setPosition(63, 89);
		fishyguy.initHP(100);
		fishyguy.setDescription("Widzisz Fishmonger. Śmierdzi rybami, które skupuje.");
		zone.add(fishyguy);
	}
}
