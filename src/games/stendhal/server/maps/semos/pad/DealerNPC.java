/* $Id: DealerNPC.java,v 1.12 2010/09/19 02:35:40 nhnb Exp $ */
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
package games.stendhal.server.maps.semos.pad;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds the NPC who deals in rainbow beans.
 * Other behaviour defined in maps/quests/RainbowBeans.java
 *
 * @author kymara
 */
public class DealerNPC implements ZoneConfigurator {
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

	//
	// IL0_GreeterNPC
	//

	private void buildNPC(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC dealerNPC = new SpeakerNPC("Pdiddi") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 12));
				nodes.add(new Node(4, 10));
				nodes.add(new Node(8, 10));
				nodes.add(new Node(8, 6));
				nodes.add(new Node(2, 6));
				nodes.add(new Node(2, 4));
				nodes.add(new Node(12, 4));
				nodes.add(new Node(12, 6));
				nodes.add(new Node(6, 6));
				nodes.add(new Node(6, 12));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("Chyba wiesz czym się zajmuje.");
				addHelp("Jeśli mam być z Tobą szczery koleś to wiedz, że nie pomogę Ci z tym zbytnio, lepiej odejdź do miasta szukając odpowiedzi.");
				addQuest("Nie mam nic dla Ciebie, kolego.");
				addOffer("Ha! Tabliczka na drzwiach jest przykrywką! To nie jest hotel. Jeżeli chcesz się napić to wracaj do miasta.");
				addGoodbye("Dowidzenia.");
			}
		};

		dealerNPC.setEntityClass("drugsdealernpc");
		dealerNPC.setPosition(4, 12);
		dealerNPC.initHP(100);
		dealerNPC.setDescription("Widzisz Pdiddi. Wydaje się, że jest myślami w innym świecie ...");
		zone.add(dealerNPC);
	}
}
