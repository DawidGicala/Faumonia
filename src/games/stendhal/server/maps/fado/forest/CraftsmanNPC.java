/* $Id: CraftsmanNPC.java,v 1.19 2012/08/23 20:05:44 yoriy Exp $ */
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
package games.stendhal.server.maps.fado.forest;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.QuestCompletedBuyerBehaviour;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds an albino elf NPC .
 * He is a trader and takes part in a quest (maps/quests/ElvishArmor.java)
 *
 * @author kymara
 */
public class CraftsmanNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Lupos") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(3, 11));
				nodes.add(new Node(12, 11));
				nodes.add(new Node(12, 2));
				nodes.add(new Node(7, 2));
				nodes.add(new Node(7, 6));
				nodes.add(new Node(3, 6));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
                addReply("tematy", "Możemy omówić badania elfickiego rynsztunku albo moje rzemiosło. Jeśli skończyliśmy zbieranie próbek, chętnie pokażę również ofertę skupu.");
			    //addGreeting("Welcome to this forest, south of Or'ril river.");
				addJob("Jestem rzemieślnikiem i próbuję zrozumieć, jak zielone elfy osiągają taką jakość swoich wyrobów. Same opowieści o ich kunszcie mi nie wystarczają. Potrzebuję prawdziwych próbek, które można dokładnie obejrzeć i porównać.");
				addHelp("Jeśli ciekawi cię historia elfów albinosów, odwiedź mojego przyjaciela Orchiwalda. Potrafi długo opowiadać o naszym ludzie i drodze, która nas tutaj sprowadziła. Ja najwięcej mogę powiedzieć o rzemiośle i badaniu ekwipunku.");
				new BuyerAdder().addBuyer(this, new QuestCompletedBuyerBehaviour("elvish_armor", "Najpierw potrzebuję wszystkich próbek elfickiego rynsztunku. Gdy skończymy zadanie, będę mógł je od ciebie skupować.", shops.get("buyelvish")), false);
				addGoodbye("Do widzenia. Niech twoje znaleziska dobrze ci służą.");
			}
		};

		npc.setDescription("Widzisz Luposa, rzemieślnika elfów albinosów. Badawczo ogląda każdy przedmiot, który trafia do jego pracowni.");
		npc.setEntityClass("albinoelfnpc");
		npc.setPosition(3, 11);
		npc.initHP(100);
		
		zone.add(npc);
	}
}
