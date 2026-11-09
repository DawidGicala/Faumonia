/* $Id: GloveMakerNPC.java,v 1.21 2020/11/28 08:11:26 davvids Exp $ */
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
package games.stendhal.server.maps.semos.glovermaker;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.HealerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class GloveMakerNPC implements ZoneConfigurator {

	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("Glover") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>(); 
				nodes.add(new Node(3,16));
                nodes.add(new Node(3,9));
				nodes.add(new Node(5,9));
				nodes.add(new Node(5,16));
				nodes.add(new Node(5,8));
				nodes.add(new Node(7,8));
				nodes.add(new Node(7,16));
				nodes.add(new Node(7,5));
				nodes.add(new Node(3,5));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addJob("Szyję rękawice ze skóry lwa. Dobrze dobrane skóry i mocne szwy są ważniejsze niż ozdoby; rękawice mają służyć podczas wypraw."); 
				addReply(Arrays.asList("pieniadze", "money"),
				"1500 monet to zapłata za uszycie pary rękawic. Skóry przynosisz osobno; pełną listę przypomnę, gdy zapytasz o ofertę.");
				addReply("skóra lwa",
		        "Ze skóry lwa szyję wierzch rękawic. Potrzebuję czterech sztuk na jedną parę, a do tego ośmiu skór zwierzęcych na pozostałe części.");
				addReply("skóra zwierzęca",
		        "Skóry zwierzęce wykorzystuję do pozostałych części rękawic i wzmocnień. Zbieraj je podczas polowań; na parę potrzeba ośmiu sztuk.");
				addOffer("Na rękawice ze skóry lwa przynieś 4 skóry lwa, 8 skór zwierzęcych i 1500 monet. Szycie potrwa 30 minut. Żeby złożyć zamówienie, powiedz: wykonaj rękawice ze skóry lwa.");
				addHelp("Jeśli zbierasz skóry, mogę uszyć z nich rękawice. Zapytaj o ofertę, a podam materiały i cenę. Na początek potrzebne będą skóry lwa.");
				addGoodbye("Trzymaj się.");

				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("money", 1500);
				requiredResources.put("skóra lwa", 4);
				requiredResources.put("skóra zwierzęca", 8);
				final games.stendhal.server.entity.npc.behaviour.impl.RepricedProducerBehaviour behaviour = new games.stendhal.server.entity.npc.behaviour.impl.RepricedProducerBehaviour("glover_gloves",
						Arrays.asList("concoct", "wykonaj"), "rękawice ze skóry lwa", requiredResources, 30 * 60, 1000);
                behaviour.installPayment(this);

				new ProducerAdder().addProducer(this, behaviour,
				"Witaj, jestem Glover. Szyję rękawice ze skóry lwa; zapytaj o ofertę, jeśli chcesz zamówić parę.");
			}
		};
		npc.setEntityClass("wellroundedguynpc");
		npc.setDescription("Widzisz Glovera, rzemieślnika szyjącego rękawice ze skóry lwa. Wśród skór i narzędzi starannie przygotowuje kolejne zamówienia.");
		npc.setPosition(3, 8);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		zone.add(npc);
	}
}
