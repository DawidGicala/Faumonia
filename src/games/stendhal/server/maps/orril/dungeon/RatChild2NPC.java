/* $Id: RatChild2NPC.java,v 1.2 2010/09/19 02:31:28 nhnb Exp $ */
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
import games.stendhal.server.entity.npc.RatKidsNPCBase;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a Rat Child NPC.
 *
 * @author Norien
 */
public class RatChild2NPC implements ZoneConfigurator {
	

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
		final SpeakerNPC rat = new RatKidsNPCBase("Mariel") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
                                //path goes here
                                nodes.add(new Node(45, 19));
                                nodes.add(new Node(65, 19));
                                nodes.add(new Node(65, 54));
                                nodes.add(new Node(69, 54));
                                nodes.add(new Node(69, 15));
                                nodes.add(new Node(45, 15));
                                
                                setPath(new FixedPath(nodes, true));
			}
		};

		rat.setDescription("Widzisz dziecko człekoszczura.");
		rat.setEntityClass("ratchild2npc");
		rat.setPosition(45, 19);
		rat.initHP(100);
		zone.add(rat);
	}
}