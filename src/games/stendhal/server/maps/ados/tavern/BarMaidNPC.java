/* $Id: BarMaidNPC.java,v 1.24 2011/11/17 18:26:38 kymara Exp $ */
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
package games.stendhal.server.maps.ados.tavern;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Ados Tavern (Inside / Level 0).
 *
 * @author hendrik
 */
public class BarMaidNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildTavern(zone, attributes);
	}

	private void buildTavern(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC tavernMaid = new SpeakerNPC("Coralia") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(13, 9));
				nodes.add(new Node(13, 7));
				nodes.add(new Node(6, 7));
				nodes.add(new Node(6, 12));
				nodes.add(new Node(10, 12));
				nodes.add(new Node(10, 5));
				nodes.add(new Node(17, 5));
                nodes.add(new Node(17, 3));
                nodes.add(new Node(3, 3));
                nodes.add(new Node(3, 6));
                nodes.add(new Node(13, 6));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Oh witam czy nie przeszkodziłam czasem w podziwianiu mojego pięknego #kapelusza?");
				addJob("Jestem kelnerką w tej oberży. Sprzedajemy zarówno importowane jak i lokalne piwo oraz dobre jedzenie.");
				addHelp("Ta oberża jest świetnym miejscem na odpoczynek i poznanie nowych ludzi! Jeżeli chciałbyś poznać naszą #ofertę to daj znać.");
				final Map<String, Integer> offerings = new HashMap<String, Integer>();
				offerings.put("sok z chmielu", 15);
				offerings.put("ser", 20);
				offerings.put("jabłko", 20);
				new SellerAdder().addSeller(this, new SellerBehaviour(offerings));
				addGoodbye();
			}
		};

		tavernMaid.setEntityClass("maidnpc");
		tavernMaid.setPosition(13, 9);
		tavernMaid.initHP(100);
		tavernMaid.setDescription("Oto Coralia. Na kapeluszu nosi egzotyczne jedzenie.");
		zone.add(tavernMaid);
	}
}
