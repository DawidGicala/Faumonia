/* $Id: VampireMakerNPC.java,v 1.21 2020/11/28 13:25:31 davvids Exp $ */
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
package games.stendhal.server.maps.semos.gravedigger;

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

/**
 * The healer (original name: Zen). He makes mega potions. 
 */

public class VampireMakerNPC implements ZoneConfigurator {

	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("Dane Taker") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>(); 
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addJob("Witaj. Ja i mój brat jesteśmy tutejszymi grabarzami, czy chcesz zamówić jakąś #usługę?"); 
				addReply(Arrays.asList("usługe", "truchło"),
				"Razem z bratem prowadzimy usługi pogrzebowe dla burmistrza Semos lecz ja hobbystycznie zajmuje się wytwarzaniem eliksirów z wampirzego truchła. Jeśli jesteś zainteresowany to powiedz #sporządź #1 #wielki #eliksir.");
				addReply("truchło wampira",
		        "Po prostu zabij wampira i wyjmij jego wnętrzności!");
				addOffer("Mogę sporządzić #'wielki eliksir' dla Ciebie. Do tego będę potrzebował #'truchło wampira'. Powiedz tylko #sporządź.");
				addReply("wielki eliksir", "Jest to klasyczny eliksir leczniczy. Jeżeli chcesz to poproś mnie, abym go przyrządził mówiąc #sporządź #1 #wielki #eliksir.");
				addReply("money", "Nie ma nic za darmo. Babranie się we wnętrznościach nie jest niczym przyjemnym. Musisz zapłacić!");
				addHelp("Aktualnie niczego nie potrzebuje.");
				addGoodbye("Trzymaj się.");

				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("money", 400);
				requiredResources.put("truchło wampira", 1);
				final ProducerBehaviour behaviour = new ProducerBehaviour("danetaker_vampire",
						Arrays.asList("concoct", "sporządź"), "wielki eliksir", requiredResources, 1 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				"Witaj, moge wytworzyć dla ciebie wielki eliksir, powiedz tylko #sporządź #1 #wielki #eliksir.");
			}
		};
		npc.setEntityClass("danetakernpc");
		npc.setDescription("Oto Dane Taker. Jeden z tutejszych braci grabarzy. Gdy na niego spoglądasz ogarnia cię chłód.");
		npc.setPosition(7, 7);
		npc.initHP(100);
		zone.add(npc);
	}
}
