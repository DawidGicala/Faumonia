/* $Id: TraderNPC.java,v 1.24 2012/08/23 20:05:43 yoriy Exp $ */
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
package games.stendhal.server.maps.semos.tavern;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/*
 * Inside Semos Tavern - Level 0 (ground floor)
 */
public class TraderNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildXinBlanca(zone);
	}

	private void buildXinBlanca(final StendhalRPZone zone) {
		final SpeakerNPC xinBlanca = new SpeakerNPC("Xin Blanca") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(2, 14));
				nodes.add(new Node(2, 15));
				nodes.add(new Node(3, 15));
				nodes.add(new Node(3, 16));
				nodes.add(new Node(5, 16));
				nodes.add(new Node(5, 14));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Ciii! Sprzedaję rzeczy łowcom przygód.");
				addHelp("Kupuję i sprzedaję różne przedmioty. Zapytaj mnie jaka jest moja #oferta.");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("sellstuff")), false);
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buystuff")), false);
				addOffer("Spójrz na tablicę na ścianie, aby zobaczyć moje ofertę.");
				addQuest("Porozmawiaj z Hackim Easso w kuźni. Może on będzie Cię potrzebował.");
				addGoodbye();
			}
		};

		xinBlanca.setEntityClass("weaponsellernpc");
		xinBlanca.setDescription("Oto Xin Blanca. Jest popularny wśród młodych podróżników ze swojego sklepu z ekwipunkiem...");
		xinBlanca.setPosition(2, 15);
		xinBlanca.initHP(100);
		zone.add(xinBlanca);
	}
}
