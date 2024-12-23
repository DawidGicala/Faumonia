/* $Id: SergeantNPC.java,v 1.3 2010/09/19 02:35:26 nhnb Exp $ */
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
package games.stendhal.server.maps.semos.kanmararn;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * An army sergeant who lost his company.
 * 
 * @see games.stendhal.server.maps.quests.KanmararnSoldiers
 */

public class SergeantNPC implements ZoneConfigurator  {
	  /**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildHideoutArea(zone, attributes);
	}

	private void buildHideoutArea(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC npc = new SpeakerNPC("Sergeant James") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(66, 46));
				nodes.add(new Node(66, 48));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Dzień dobry łowco przygód!");
				addJob("Jestem sierżantem w wojsku.");
				addGoodbye("Powodzenia i uważaj na siebie i na krasnale wokoło!");
				// all other behaviour is defined in the quest.
			}
		};

		npc.setEntityClass("royalguardnpc");
		npc.setDescription("Sergeant James jest sierżantem w Armii Semos. Wydaje się być nieco zagubiony ...");
		npc.setPosition(66, 46);
		npc.setBaseHP(100);
		npc.initHP(75);
		npc.setLevel(20);
		zone.add(npc);
	}
}
