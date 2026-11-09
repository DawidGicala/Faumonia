/* $Id: LittleGirlNPC.java,v 1.2 2010/09/19 02:31:11 nhnb Exp $ */
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
package games.stendhal.server.maps.kalavan.citygardens;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a little girl called Annie Jones.
 *
 * @author kymara
 */
public class LittleGirlNPC implements ZoneConfigurator {
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
		createNPC(zone, attributes);
	}


	private void createNPC(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC npc = new SpeakerNPC("Annie Jones") {
			
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(44, 90));
				nodes.add(new Node(44, 86));
				nodes.add(new Node(42, 86));
				nodes.add(new Node(42, 90));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
                addReply("wskazówki", "Mamusia, Mrs Jones, siedzi na ławce obok placu zabaw. Najpierw porozmawiaj z nią o pozwoleniu, a potem kup jedną porcję lodów u Sama w ogrodach Kalavan. Przynieś je tutaj, żebym nie musiała oddalać się od mamy.");
                addReply("tematy", "Możemy wrócić do twojej sprawy albo porozmawiać o czymś innym. Co chcesz wiedzieć?");	
				// greeting and quest in maps/quests/IcecreamForAnnie.java
			addOffer("Jestem małą dziewczynką. Nie mam nic do zaoferowania.");
			addJob("Pomagam mamusi, a potem mogę pobawić się w ogrodzie. Tylko od tego biegania robi mi się strasznie gorąco!");
			addHelp("Zapytaj mamusi.");
			addGoodbye("Ta ta.");
			}
		};

		npc.setDescription("Widzisz Annie Jones bawiącą się w ogrodach Kalavan. Jej mama czuwa przy pobliskiej ławce.");
		npc.setEntityClass("pinkgirlnpc");
		npc.setPosition(44, 90);
		npc.initHP(100);
		zone.add(npc);
	}
}
