/* $Id: PrincessNPC.java,v 1.16 2010/09/19 02:31:47 nhnb Exp $ */
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
package games.stendhal.server.maps.nalwor.tower;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a Princess NPC who lives in a tower.
 *
 * @author kymara
 */
public class PrincessNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Tywysoga") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(17, 13));
				nodes.add(new Node(10, 13));
				nodes.add(new Node(10, 4));
				nodes.add(new Node(3, 4));
				nodes.add(new Node(3, 3));
				nodes.add(new Node(7, 3));
				nodes.add(new Node(7, 9));
				nodes.add(new Node(12, 9));
				nodes.add(new Node(12, 13));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć człeku, człowieku.");
				addJob("Jestem księżniczką. Co mogę zrobić?");
				addHelp("Stanowcza osoba mogłaby zrobić dla mnie #zadanie.");
				addOffer("Nie handluję. Moi rodzice mogliby to uważać za poniżające.");
				addGoodbye("Dowidzenia nieznajomy.");
			}
		};

		npc.setDescription("Oto piękna, ale samotna Najwyższa Elfka.");
		npc.setEntityClass("elfprincessnpc");
		npc.setPosition(17, 13);
		npc.initHP(100);
		zone.add(npc);
	}
}
