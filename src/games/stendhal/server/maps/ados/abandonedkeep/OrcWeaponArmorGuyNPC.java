/* $Id: OrcWeaponArmorGuyNPC.java,v 1.8 2010/09/19 02:28:01 nhnb Exp $ */
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
package games.stendhal.server.maps.ados.abandonedkeep;

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

/**
 * Inside Ados Abandoned Keep - level -1 .
 */
public class OrcWeaponArmorGuyNPC implements ZoneConfigurator {
    private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildHagnurk(zone);
	}

	private void buildHagnurk(final StendhalRPZone zone) {
		final SpeakerNPC hagnurk = new SpeakerNPC("Hagnurk") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(104, 3));
				nodes.add(new Node(109, 3));
				nodes.add(new Node(109, 8));
				nodes.add(new Node(108, 8));
				nodes.add(new Node(108, 10));
				nodes.add(new Node(109, 10));
				nodes.add(new Node(109, 13));
				nodes.add(new Node(104, 13));
				setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Jestem sprzedawcą. Kim jesteś?");
				addHelp("Sprzedaję przedmioty spójrz na tablicę na ścianie.");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("sellbetterstuff1")), false);
				addOffer("Spójrz na tablicę na ścianie, aby zapoznać się z moją ofertą.");
				addQuest("Jestem szczęśliwy. Nie potrzebuję niczego.");
				addGoodbye();
			}
		};

		hagnurk.setEntityClass("orcsalesmannpc");
		hagnurk.setPosition(106, 5);
		hagnurk.initHP(100);
		hagnurk.setDescription("Oto Hagnurk. Jest sprzedawcą u orków.");
		zone.add(hagnurk);
	}
}
