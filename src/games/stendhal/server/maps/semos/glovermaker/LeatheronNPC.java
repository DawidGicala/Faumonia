/* $Id: LeatheronNPC.java,v 1.21 2020/11/28 08:11:26 davvids Exp $ */
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


public class LeatheronNPC implements ZoneConfigurator {

	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("Leatheron") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>(); 
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addJob("Szyję rękawice ze skóry tygrysa. Przy krojeniu pilnuję, żeby dobrze leżały na dłoni, a szwy nie rozchodziły się podczas pracy. Zapytaj o ofertę, jeśli szukasz pary dla siebie."); 
				addReply(Arrays.asList("pieniadze", "money"),
				"1500 monet to zapłata za uszycie pary rękawic. Skóry przynosisz osobno; pełną listę przypomnę, gdy zapytasz o ofertę.");
				addReply("skóra tygrysa",
		        "Ze skóry tygrysa szyję wierzch rękawic. Potrzebuję czterech sztuk na jedną parę, a do tego ośmiu skór zwierzęcych na pozostałe części.");
				addReply("skóra zwierzęca",
		        "Skóry zwierzęce wykorzystuję do pozostałych części rękawic i wzmocnień. Zbieraj je podczas polowań; na parę potrzeba ośmiu sztuk.");
				addOffer("Na rękawice ze skóry tygrysa przynieś 4 skóry tygrysa, 8 skór zwierzęcych i 1500 monet. Szycie potrwa 30 minut. Żeby złożyć zamówienie, powiedz: wykonaj rękawice ze skóry tygrysa.");
				addHelp("Jeśli zbierasz skóry, mogę uszyć z nich rękawice. Zapytaj o ofertę, a podam materiały i cenę. Na początek potrzebne będą skóry tygrysa.");
				addGoodbye("Trzymaj się.");

				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("money", 1500);
				requiredResources.put("skóra tygrysa", 4);
				requiredResources.put("skóra zwierzęca", 8);
				final games.stendhal.server.entity.npc.behaviour.impl.RepricedProducerBehaviour behaviour = new games.stendhal.server.entity.npc.behaviour.impl.RepricedProducerBehaviour("leatheron_gloves",
						Arrays.asList("concoct", "wykonaj"), "rękawice ze skóry tygrysa", requiredResources, 30 * 60, 1000);
                behaviour.installPayment(this);

				new ProducerAdder().addProducer(this, behaviour,
				"Witaj, jestem Leatheron. Szyję rękawice ze skóry tygrysa; zapytaj o ofertę, jeśli chcesz zamówić parę.");
			}
		};
		npc.setEntityClass("weaponsellernpc");
		npc.setDescription("Widzisz Leatherona, rzemieślnika szyjącego rękawice ze skóry tygrysa. Wśród skór i narzędzi starannie przygotowuje kolejne zamówienia.");
		npc.setPosition(15, 5);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		zone.add(npc);
	}
}
