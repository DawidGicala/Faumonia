/* $Id: GarbiddleNPC.java,v 1.7 2012/08/23 20:05:45 yoriy Exp $ */
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
package games.stendhal.server.maps.semos.gnomevillage;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Inside Gnome Village.
 */
public class GarbiddleNPC implements ZoneConfigurator {
    private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildgarbiddle(zone);
	}

	private void buildgarbiddle(final StendhalRPZone zone) {
		final SpeakerNPC garbiddle = new SpeakerNPC("Garbiddle") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(37, 112));
				nodes.add(new Node(41, 112));
				setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				addGreeting("Witam w naszej cudownej wiosce.");
				addJob("Jestem tutaj, aby skupować towary na deszczowy dzień.");
				addHelp("Skupuję kilka rzeczy. Poczytaj znak, aby dowiedzieć się czego potrzebujemy.");
				addOffer("Poczytaj znak, aby dowiedzieć się czego potrzebujemy.");
				addQuest("Dziękuję za pytanie, ale czuję się dobrze.");
				addGoodbye("Dowidzenia. Cieszę się, że odwiedziłeś nas.");
 				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buy4gnomes")), false);
			}
		};

		garbiddle.setEntityClass("gnomenpc");
		garbiddle.setPosition(37, 112);
		garbiddle.initHP(100);
		garbiddle.setDescription("Widzisz Garbiddle, małą panią gnom. Czeka na klientów.");
		zone.add(garbiddle);
	}
}
