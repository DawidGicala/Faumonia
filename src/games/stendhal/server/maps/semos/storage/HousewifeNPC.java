/* $Id: HousewifeNPC.java,v 1.19 2011/06/27 20:51:21 bluelads99 Exp $ */
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
package games.stendhal.server.maps.semos.storage;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HousewifeNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildSemosStorageArea(zone, attributes);
	}

	private void buildSemosStorageArea(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC npc = new SpeakerNPC("Eonna") {

			/**
			 * Creates a path around the table with the beers and to the furnance.
			 */
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 12)); 
				nodes.add(new Node(15, 13));
				nodes.add(new Node(15, 9));
				nodes.add(new Node(10, 9));
				nodes.add(new Node(10, 13));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
                addReply("tematy", "Możemy wrócić do sprawy piwnicy albo porozmawiać o domu i zapasach. Miotłę na razie mam pod ręką.");
				addGreeting("Witaj, wejdź śmiało. Tylko zerknij pod nogi - wolę nie mieć niespodziewanych gości przy zapasach.");
				addJob("Dbam o ten dom i pilnuję, żeby zapasy się nie marnowały. Z kurzem poradzę sobie miotłą, ale szczury to zupełnie inna sprawa. Na sam szelest za schodami robi mi się nieswojo.");
                addHelp("Zajrzyj do Leandera w piekarni, jeśli chcesz przygotować kanapki na drogę. Potrzebuje chleba, sera, szynki i sałaty, a z takich zapasów potrafi zrobić porządny posiłek. Ja lubię mieć jedzenie pod ręką, o ile szkodniki nie dobiorą się do niego pierwsze.");
				addGoodbye("Do widzenia. I uważaj, co biega pod nogami!");
			}
		};

		npc.setEntityClass("welcomernpc");
		npc.setDescription("Widzisz Eonnę, gospodynię z domu obok piekarni w Semos. Miotłę trzyma z gotowością, która wiele mówi o jej stosunku do szczurów.");
		npc.setPosition(4, 13);
		npc.initHP(100);
		zone.add(npc);
	}
}
