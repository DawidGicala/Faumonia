/* $Id: PostNPC.java,v 1.9 2010/09/19 02:31:34 nhnb Exp $ */
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
package games.stendhal.server.maps.nalwor.postoffice;

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
 * Builds the post office elf NPC.
 * She may be used later for something else like a newspaper. 
 * Now she sells nalwor scrolls
 * @author kymara
 */
public class PostNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Lorithien") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(11, 3));
				nodes.add(new Node(16, 3));
				nodes.add(new Node(16, 8));
				nodes.add(new Node(11, 8));
				nodes.add(new Node(11, 5));
				nodes.add(new Node(7, 5));
				nodes.add(new Node(7, 2));
				nodes.add(new Node(3, 2));
				nodes.add(new Node(3, 5));
				nodes.add(new Node(3, 2));
				nodes.add(new Node(7, 2));
				nodes.add(new Node(7, 5));
				nodes.add(new Node(11, 5));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć. W czym mogę #pomóc?");
				addJob("Pracuję na poczcie. Jesteś tutaj nowy i nie budzisz jeszcze zaufania.");
				addHelp("Nie mam tej #pracy od dawna ... wróć za jakiś czas może dam Ci coś interesującego do zrobienia.");
				addGoodbye("Dowidzenia. Miło było Cię poznać!");
			}
		};

		npc.setDescription("Oto piękna kobieta elf.");
		npc.setEntityClass("postelfnpc");
		npc.setPosition(11, 3);
		npc.initHP(100);
		zone.add(npc);
	}
}
