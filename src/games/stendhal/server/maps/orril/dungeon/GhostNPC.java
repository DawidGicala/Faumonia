/* $Id: GhostNPC.java,v 1.24 2010/09/19 02:31:28 nhnb Exp $ */
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
package games.stendhal.server.maps.orril.dungeon;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.GhostNPCBase;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a Ghost NPC.
 *
 * @author kymara
 */
public class GhostNPC implements ZoneConfigurator {
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
		final SpeakerNPC ghost = new GhostNPCBase("Goran") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(216, 127));
				nodes.add(new Node(200, 127));
				nodes.add(new Node(200, 120));
				nodes.add(new Node(216, 120));
				nodes.add(new Node(216, 122));
				nodes.add(new Node(200, 122));
				nodes.add(new Node(200, 124));
				nodes.add(new Node(216, 124));
				setPath(new FixedPath(nodes, true));
			}
		};

		ghost.setDescription("Oto duch mężczyzny. Wygląda na to, że zginął w walce.");
		ghost.setResistance(0);
		ghost.setEntityClass("deadmannpc");
		// he is a ghost so he is see through
		ghost.setVisibility(70);
		ghost.setPosition(216, 127);
		// he has low HP
		ghost.initHP(30);
		ghost.setBaseHP(100);
		zone.add(ghost);
	}
}
