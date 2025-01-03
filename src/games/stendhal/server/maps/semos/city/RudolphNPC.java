/* $Id: RudolphNPC.java,v 1.6 2012/12/15 11:20:40 kymara Exp $ */
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

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
/**
 * ZoneConfigurator configuring Rudolph the Red-Nosed Reindeer who clops around Semos city during Christmas season
 */
public class RudolphNPC implements ZoneConfigurator {

	
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		final SpeakerNPC npc = new SpeakerNPC("Rudolph") {

			@Override
			protected void createPath() {
				final List<Node> path = new LinkedList<Node>();
				path.add(new Node(2, 3));
				path.add(new Node(2, 14));
				path.add(new Node(36, 14));
				path.add(new Node(36, 46));
				path.add(new Node(51, 46));
				path.add(new Node(51, 48));
				path.add(new Node(62, 48));
				path.add(new Node(62, 55));
				path.add(new Node(51, 55));
				path.add(new Node(51, 58));
				path.add(new Node(32, 58));
				path.add(new Node(32, 53));
				path.add(new Node(18, 53));
				path.add(new Node(18, 43));
				path.add(new Node(20, 43));
				path.add(new Node(20, 26));
				path.add(new Node(26, 26));
				path.add(new Node(26, 14));
				path.add(new Node(21, 14));
				path.add(new Node(21, 3));
				setPath(new FixedPath(path, true));
			}			
			
			@Override
			public void createDialog() {
				addGreeting("Cześć jestem wesołym przyjacielem. Co za wspaniała pora roku!");
				addHelp("Oh nie mogę ci pomóc. To nie tak, że mogę wpłynąć na Mikołaja.");
				addJob("Ciągnę sanie w Bożo Narodzeniową noc. Mój świecący nos daje mi tyle zabawy, że Święty może zobaczyć dokąd leci.");
				addGoodbye("Miło było cię poznać.");
			
				// remaining behaviour defined in games.stendhal.server.maps.quests.GoodiesForRudolph
			
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
			
		};
		npc.setPosition(2, 3);
		npc.setDirection(Direction.DOWN);
		npc.setDescription("Oto Rudolph czerwononosy renifer. Jego nos jest taki duży, jaskrawy i świeci.");
		npc.setHP(950);
		npc.setBaseSpeed(1);
		npc.setEntityClass("rudolphnpc");
		zone.add(npc);
	}

}
