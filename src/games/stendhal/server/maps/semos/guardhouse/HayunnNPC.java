/* $Id: HayunnNPC.java,v 1.19 2021/01/13 17:26:36 nhnb Exp $ */
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
package games.stendhal.server.maps.semos.guardhouse;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.HealerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A young lady (original name: Hayunn) who heals players without charge. 
 */
public class HayunnNPC implements ZoneConfigurator {
	
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Hayunn") {
			@Override
			public void createDialog() {
                addReply("wskazówki", "Miter jest tutaj, w domku startowym. Kup od niego jedną wodę, potwierdzając cenę, i przynieś ją w plecaku. Wybierz potem przekazanie dostawy i wskaż wodę.");
                addJob("Pomagam przybyszom stawiać pierwsze kroki w Faumonii. Zaczynam od drobnych przysług, bo przy nich najłatwiej poznać mieszkańców i ich zwyczaje.");
                addReply("tematy", "Możemy wrócić do twojej sprawy albo porozmawiać o czymś innym. Co chcesz wiedzieć?");
				addGreeting("Witaj, jestem Hayunn. Zanim wyruszysz poza Semos, pokażę ci, jak załatwiać sprawy z mieszkańcami i korzystać ze sklepu. Wybierz #zadanie - zaczniemy od drobnej przysługi.");
				addEmotionReply("hugs", "hugs");
				addGoodbye();
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 9));
				nodes.add(new Node(6, 9));
				nodes.add(new Node(6, 14));
				nodes.add(new Node(6, 9));
				nodes.add(new Node(11, 9));
				setPath(new FixedPath(nodes, true));
			}
		};

		npc.setPosition(4, 9);
		npc.setDescription("Widzisz Hayunna, doświadczonego przewodnika nowych przybyszów. Cierpliwie obserwuje, czy każdy potrafi odnaleźć się w Semos.");
		npc.setEntityClass("oldheronpc");
		zone.add(npc);
	}
}
