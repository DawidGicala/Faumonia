/* $Id: KingNPC.java,v 1.12 2010/09/19 02:30:42 nhnb Exp $ */
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
package games.stendhal.server.maps.kalavan.castle;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds the king in Kalavan castle.
 *
 * @author kymara
 */
public class KingNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone, attributes);
	}

	private void buildNPC(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC kingNPC = new SpeakerNPC("King Cozart") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(40, 22));
				nodes.add(new Node(42, 22));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				// it's all in
				// games.stendhal.server.maps.quests.ImperialPrincess
			}
		};

		kingNPC.setEntityClass("kingcozartnpc");
		kingNPC.setPosition(40, 22);
		kingNPC.initHP(100);
		kingNPC.setDescription("Oto King Cozart, król Kalavan i jego obywateli.");
		zone.add(kingNPC);
	}
}
