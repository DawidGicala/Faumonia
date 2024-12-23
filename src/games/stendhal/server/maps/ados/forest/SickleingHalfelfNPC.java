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

package games.stendhal.server.maps.ados.forest;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Provides Eheneumniranin, the sickle wielding NPC.
 * A Halfelf who lost his memory and now works in the grain fields at the farm
 * He will possibly offer a quest to help him find his past.
 *
 * @author omero
 */
public class SickleingHalfelfNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("Eheneumniranin") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(77, 96));
				nodes.add(new Node(77, 98));
				nodes.add(new Node(81, 98));
				nodes.add(new Node(81, 100));
				nodes.add(new Node(85, 100));
				nodes.add(new Node(85, 107));
				nodes.add(new Node(75, 107));
				nodes.add(new Node(75, 96));

				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addGreeting("Salve straniero...");
				addJob("Aby zebrać snop zboża moim #sierpem przed zabraniem go do młyna muszę... Jak się tutaj dostałem?... Gdybym mógł sobie przypomnieć...");
				addHelp("Ha! Jakie nie warte i nie zasługujące jest pytanie o ujawnienie... Kim jestem?... Mgła przyćmiła moje myśli...");
				addOffer("Oh?! Ponieważ miałem coś cennego chciałbym zaproponować spojrzenie prawdzie...");
				addQuest("Ehh... Nie jestwem gotowy obarczać cię moimi problemami... Ale wpadaj do mnie od czasu do czasu, a może, któregoś dnia coś będę miał dla Ciebie i będę potrzebował twojej pomocy...");
				addReply(Arrays.asList("sickle", "sierp", "sierpem"),"Użyteczne narzedzie rolnika tak jak kosa. Powinieneś zapytać jakiegoś kowala czy nie oferuje ostrych sierpów.");
				addGoodbye("In bocca al lupo...");
			}
	
		};

		npc.setEntityClass("sickleinghalfelfnpc");
		npc.setPosition(76,97);
		npc.initHP(100);
		npc.setDescription("Oto Eheneumniranin pół elf... Stracił pamięć i wygląda na zagubionego.");
		zone.add(npc);
	}
}
