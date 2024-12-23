/* $Id: BakerNPC.java,v 1.28 2011/03/19 18:31:36 kymara Exp $ */
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
package games.stendhal.server.maps.ados.bakery;

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
 * Ados Bakery (Inside / Level 0).
 *
 * @author hendrik
 */
public class BakerNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildBakery(zone, attributes);
	}

	private void buildBakery(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC baker = new SpeakerNPC("Arlindo") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				// to the well
				nodes.add(new Node(15, 3));
				// to a barrel
				nodes.add(new Node(15, 8));
				// to the baguette on the table
				nodes.add(new Node(13, 9));
				// around the table
				nodes.add(new Node(13, 10));
				nodes.add(new Node(10, 10));
				// to the sink
				nodes.add(new Node(10, 12));
				// to the pizza/cake/whatever
				nodes.add(new Node(7, 12));
				nodes.add(new Node(7, 10));
				// to the pot
				nodes.add(new Node(3, 10));
				// towards the oven
				nodes.add(new Node(3, 4));
				nodes.add(new Node(5, 4));
				// to the oven
				nodes.add(new Node(5, 3));
				// one step back
				nodes.add(new Node(5, 4));
				// towards the well
				nodes.add(new Node(15, 4));

				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				// addGreeting("Hi, most of the people are out of town at the moment.");
				addJob("Jestem lokalnym piekarzem. Mimo iż dostajemy większość zapasów z Semos to i tak jest sporo pracy do zrobienia.");
				addReply(Arrays.asList("mąka", "szynka", "marchew"),
               "W Ados brakuje zapasów. Dostajemy większość jedzenia z Semos, które jest na zachód stąd.");  
				addReply(Arrays.asList("button mushroom","pieczarka"),
					    "Doszły nas słuchy, że w kuchni brakuje żywności. Postanowiliśmy powiększyć zapasy grzybów. Teraz znajdziesz ich większą ilość w lasach. ");
				addHelp("Jeżeli posiadasz mnóstwo mięsa lub sera to możesz sprzedać Siandrze w barze Ados.");
				addGoodbye();

				// Arlindo makes pies if you bring him flour, meat, carrot and a mushroom
				// (uses sorted TreeMap instead of HashMap)
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("mąka", Integer.valueOf(1));
				requiredResources.put("szynka", Integer.valueOf(1));
				requiredResources.put("marchew", Integer.valueOf(2));
				requiredResources.put("pieczarka", Integer.valueOf(1));

				final ProducerBehaviour behaviour = new ProducerBehaviour("arlindo_make_pie", Arrays.asList("make", "upiecz"), "tarta",
				        requiredResources, 7 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				        "Witam! Założę się, że słyszałeś o moim słynnym placku. Mówiąc #upiecz zrobię go dla ciebie.");
			}
		};

		baker.setEntityClass("bakernpc");
		baker.setDirection(Direction.DOWN);
		baker.setPosition(15, 3);
		baker.initHP(100);
		baker.setDescription("Arlindo jest oficjalnym piekarzem w Ados. Znany jest z przepysznego placka");
		zone.add(baker);
	}

}
