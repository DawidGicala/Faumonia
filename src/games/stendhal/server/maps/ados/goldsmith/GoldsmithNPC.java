/* $Id: GoldsmithNPC.java,v 1.29 2010/09/19 02:28:13 nhnb Exp $ */
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
package games.stendhal.server.maps.ados.goldsmith;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Ados Goldsmith (Inside / Level 0).
 *
 * @author dine
 */
public class GoldsmithNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildGoldsmith(zone, attributes);
	}

	private void buildGoldsmith(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC goldsmith = new SpeakerNPC("Joshua") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				// by the sink
				nodes.add(new Node(5, 7));
				// to the left wall
				nodes.add(new Node(2, 7));
				nodes.add(new Node(11, 7));
				// up one by the armor
				nodes.add(new Node(11, 6));
				nodes.add(new Node(12, 6));
				nodes.add(new Node(12, 5));
				// to the right wall
				nodes.add(new Node(18, 5));
				nodes.add(new Node(18, 3));
				// to the starting point
				nodes.add(new Node(5, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
                addReply("tematy", "Porozmawiajmy o złocie, materiałach do odlewania albo o moim bracie. Czasem dobrze oderwać na chwilę wzrok od pracy.");
				addGreeting("Witaj w warsztacie. Podejdź śmiało, tylko uważaj przy palenisku.");
				addJob("Jestem złotnikiem i najchętniej spędzam czas przy pracy z kruszcem. Z jednej bryłki złota i dwóch polan odlewam sztabkę; zajmuje mi to kwadrans. Powiedz #odlej, jeśli masz materiały.");
				addHelp("Mój brat Xoderos prowadzi kuźnię w Semos i sprzedaje narzędzia, w tym misę do płukania złota. Za jej pomocą możesz szukać bryłek w odpowiednich miejscach nad wodą. Przynieś bryłkę i dwa polana, jeśli zechcesz zamienić ją w sztabkę.");
				addGoodbye("Do widzenia. Ja wracam do paleniska.");

				// Joshua makes gold if you bring him gold nugget and wood
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("polano", 2);
				requiredResources.put("bryłka złota", 1);

				final ProducerBehaviour behaviour = new ProducerBehaviour("joshua_cast_gold",
						 Arrays.asList("cast", "odlej"), "sztabka złota", requiredResources, 15 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				        "Witaj w warsztacie. Jeśli przyniesiesz bryłkę złota i dwa polana, mogę odlać dla ciebie sztabkę w kwadrans. Powiedz #odlej, a przyjrzę się materiałom.");
				addReply("polano",
						"Potrzebuję trochę polan, aby podtrzymywać ogień. Możesz je znaleźć w lesie.");
				addReply(Arrays.asList("ore", "gold ore", "gold nugget", "bryłka złota"),
						"Aby otrzymać sztabki złota potrzebne są bryłki tego kruszcu.");
				addReply(Arrays.asList("gold bar", "gold", "bar", "sztabkę złota"),
				        "Sztabka to kruszec przygotowany do dalszej pracy. Mogę ją odlać z jednej bryłki złota, zużywając dwa polana na opał. Jeśli właśnie o to ci chodzi, powiedz #odlej.");
				addReply(Arrays.asList("gold pan", "misę do pukania złota"),
				        "Jeżeli już masz misę do pukania złota to możesz wydobywać złoto w pewnych miejscach.");
				addReply(Arrays.asList("oil","can of oil", "olejek"),"Rybacy nas w to zaopatrują.");
			}
		};

		goldsmith.setEntityClass("goldsmithnpc");
		goldsmith.setDirection(Direction.DOWN);
		goldsmith.setPosition(18, 3);
		goldsmith.initHP(100);
		goldsmith.setDescription("Widzisz Joshuę, złotnika z Ados i brata Xoderosa. Z uwagą przygląda się kruszcowi, jakby reszta świata na chwilę przestała istnieć.");
		zone.add(goldsmith);
	}
}
