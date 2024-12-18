/* $Id: BatMakerNPC.java,v 1.21 2020/11/28 13:25:31 davvids Exp $ */
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

public class BatMakerNPC implements ZoneConfigurator {

	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("Same Taker") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>(); 
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addJob("Witaj. Ja i mój brat jesteśmy tutejszymi grabarzami, czy chcesz zamówić jakąś #usługę?"); 
				addReply(Arrays.asList("usługe", "truchło"),
				"Razem z bratem prowadzimy usługi pogrzebowe dla burmistrza Semos lecz ja hobbystycznie zajmuje się wytwarzaniem eliksirów z nietoperzego truchła. Jeśli jesteś zainteresowany to powiedz #sporządź #1 #duży #eliksir.");
				addReply("truchło nietoperza",
		        "Po prostu zabij nietoperza i wyjmij jego wnętrzności!");
				addOffer("Mogę sporządzić #'duży eliksir' dla Ciebie. Do tego będę potrzebował #'truchło nietoperza'. Powiedz tylko #sporządź.");
				addReply("duży eliksir", "Jest to klasyczny eliksir leczniczy. Jeżeli chcesz to poproś mnie, abym go przyrządził mówiąc #sporządź #1 #duży #eliksir.");
				addReply("money", "Nie ma nic za darmo. Babranie się we wnętrznościach nie jest niczym przyjemnym. Musisz zapłacić!");
				addHelp("Aktualnie niczego nie potrzebuje.");
				addGoodbye("Trzymaj się.");

				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("money", 50);
				requiredResources.put("truchło nietoperza", 1);
				final ProducerBehaviour behaviour = new ProducerBehaviour("sametaker_bat",
						Arrays.asList("concoct", "sporządź"), "duży eliksir", requiredResources, 1 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				"Witaj, moge wytworzyć dla ciebie duży eliksir, powiedz tylko #sporządź #1 #duży #eliksir.");
			}
		};
		npc.setEntityClass("sametakernpc");
		npc.setDescription("Oto Same Taker. Jeden z tutejszych braci grabarzy. Gdy na niego spoglądasz ogarnia cię chłód.");
		npc.setPosition(13, 10);
		npc.initHP(100);
		zone.add(npc);
	}
}
