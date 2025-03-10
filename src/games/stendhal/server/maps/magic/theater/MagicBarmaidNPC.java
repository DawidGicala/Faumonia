/* $Id: MagicBarmaidNPC.java,v 1.6 2010/09/19 02:30:46 nhnb Exp $ */
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
package games.stendhal.server.maps.magic.theater;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/*
 * Inside Magic Theater)
 */
public class MagicBarmaidNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildmagicbarmaid(zone);
	}

	private void buildmagicbarmaid(final StendhalRPZone zone) {
		final SpeakerNPC magicbarmaid = new SpeakerNPC("Trillium") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(13, 3));
				nodes.add(new Node(19, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć. Mam nadzieję, że podoba się Tobie nasz wspaniały teatr.");
				addJob("Sprzedaję większość pysznego jedzenia w Magic City.");
				addHelp("Jeżeli jesteś głodny to sprawdź tablicę, aby dowiedzieć się jakie sprzedajemy jedzenie i po jakiej cenie.");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("sellmagic")), false);
				addOffer("Spójrz na tablicę, aby zobaczyć ceny.");
				addQuest("Nie potrzebuję twojej pomocy. Dziękuję.");
				addReply("lukrecja", "Biedny Baldemar ma alergię na lukrecję.");
				addGoodbye("Wspaniale miło było Cię spotkać. Wróć ponownie.");
			}
		};

		magicbarmaid.setEntityClass("woman_015_npc");
		magicbarmaid.setPosition(13, 3);
		magicbarmaid.initHP(100);
		magicbarmaid.setDescription("Widzisz Trillium. Ona jest barmanką teatru Magic City. Oferuje napoje i żywności.");
		zone.add(magicbarmaid);
	}
}
