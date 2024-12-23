/* $Id: BreksNPCSummer2024Summer2024.java,v 1.20 2024/06/25 22:17:58 davvids Exp $ */
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
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds ados dakes NPC.
 * He may give an items quest later
 * Now he sells ados scrolls
 * @author kymara
 */
public class BreksNPCSummer2024 implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		builddakes(zone);
	}

	private void builddakes(final StendhalRPZone zone) {
		final SpeakerNPC dakes = new SpeakerNPC("Breks") {
			
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(12,7));
				nodes.add(new Node(12,9));
				nodes.add(new Node(8,9));
				nodes.add(new Node(8,12));
				nodes.add(new Node(11,12));
				nodes.add(new Node(11,14));
				nodes.add(new Node(22,14));
				nodes.add(new Node(22,16));
				nodes.add(new Node(13,16));
				nodes.add(new Node(13,14));
				nodes.add(new Node(12,14));
				nodes.add(new Node(12,8));
				nodes.add(new Node(12,7));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj, podróżniku. Strasznie dziś gorąco...");
				addJob("Nic dla Ciebie nie mam, zresztą.. jak chciałbyś coś robić w ten upał?");
				addHelp("Hmm, zastanawiam się nad wybraniem do moich starych przyjaciół na egzotycznych wyspach.");
				addGoodbye("Na razie, nie zapomnij pić dużo wody!");
			}
		};

		dakes.setDescription("Widzisz Breksa. Wygląda dosyć podejrzanie.");
		dakes.setEntityClass("swimmer2npc");
		dakes.setPosition(19, 7);
		dakes.initHP(100);
		zone.add(dakes);
	}
}
