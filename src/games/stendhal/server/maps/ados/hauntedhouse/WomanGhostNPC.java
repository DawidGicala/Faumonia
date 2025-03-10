/* $Id: WomanGhostNPC.java,v 1.22 2012/08/31 17:19:06 bluelads99 Exp $ */
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
package games.stendhal.server.maps.ados.hauntedhouse;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a female Ghost NPC.
 *
 * @author kymara
 */
public class WomanGhostNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone, attributes);
	}
	private void buildNPC(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC woman = new SpeakerNPC("Carena") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(3, 4));
				nodes.add(new Node(13, 4));
				nodes.add(new Node(13, 14));
				nodes.add(new Node(3, 14));
				nodes.add(new Node(3, 26));
				nodes.add(new Node(11, 26));
				nodes.add(new Node(11, 7));
				nodes.add(new Node(23, 7));
				nodes.add(new Node(23, 29));
				nodes.add(new Node(29, 29));
				nodes.add(new Node(29, 2));
				nodes.add(new Node(22, 2));
				nodes.add(new Node(22, 7));
				nodes.add(new Node(3, 7));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				//addGreeting("Wooouhhhhhh!");
				addJob("Nic pożytecznego nie mogłam zrobić na tym świecie. Teraz nawiedzam ten dom.");
				addHelp("Tutaj jest ostrzeżenie: Jeżeli zginiesz to staniesz się duchem jak ja częściowo widocznym i niematerialnym. Jeżeli znajdziesz swoją drogę wyjścia z afterlife to narodzisz się na nowo.");
				addGoodbye("Żegnaj");
				// remaining behaviour defined in games.stendhal.server.maps.quests.FindGhosts
			}
		};
		woman.setDescription("Widzisz ducha kobiety, która wygląda na zmartwionego.");
		woman.setResistance(0);

		woman.setEntityClass("woman_011_npc");
		// She is a ghost so she is see through
		woman.setVisibility(40);
		woman.setPosition(3, 4);
		// She has low HP
		woman.initHP(30);
		woman.setBaseHP(100);
		zone.add(woman);
	}
}
