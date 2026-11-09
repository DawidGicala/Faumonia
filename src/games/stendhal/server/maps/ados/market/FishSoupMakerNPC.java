/* $Id: FishSoupMakerNPC.java,v 1.4.2.1 2010/10/11 21:49:22 nhnb Exp $ */
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
package games.stendhal.server.maps.ados.market;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a npc in Ados (name:Florence Boullabaisse) who is a fish soup maker on the market
 * 
 * @author Krupi (fish soup idea) Vanessa Julius (implemented)
 *
 */
public class FishSoupMakerNPC implements ZoneConfigurator {

	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Florence Boullabaisse") {
		    
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(63, 14));
				nodes.add(new Node(70, 14));
                nodes.add(new Node(70, 10));
                nodes.add(new Node(67, 10));  
                nodes.add(new Node(67, 14));
                nodes.add(new Node(64, 14)); 
                nodes.add(new Node(64, 10)); 
                nodes.add(new Node(63, 10));
               	setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
                addReply("wskazówki", "Informacje o miejscach połowu poszczególnych ryb znajdziesz w bibliotece Ados. Cebulę i por zbierzesz w Fado, a pomidory oraz czosnek kupisz od Sue w ogrodach Kalavan. Potrzebuję po jednej sztuce każdego składnika z przepisu; możesz donosić je partiami.");
                addReply("tematy", "Możemy wrócić do twojej sprawy albo porozmawiać o czymś innym. Co chcesz wiedzieć?");
				//addGreeting();
				addHelp("Mogę ugotować zupę rybną, jeśli przyniesiesz potrzebne składniki. Jeśli wolisz warzywa, odwiedź moją przyjaciółkę Old Mother Helenę w Fado. Każda z nas ma własny przepis i żadna nie lubi, gdy myli się jej garnek z cudzym.");

				addQuest("Nie mam dla Ciebie zadań, ale mogę zaoferować świeżo ugotowaną zupę rybną na twoje podróże."); 
				addJob("Specjalizuję się w zupach, a rybna jest moją dumą. Każda ryba wnosi inny smak, dlatego nie zastępuję jednego gatunku drugim. Jeśli wolisz warzywa, moja przyjaciółka Helena gotuje w Fado.");
				addOffer("Mogę ugotować zupę rybną na twoją podróż. Najpierw poznaj przepis i przynieś składniki; każda ryba i każde warzywo mają w nim swoje miejsce.");
				addGoodbye("Życzę miłego pobytu na rynku w Ados!");
				addEmotionReply("cuddle", "cuddle");
			}
		};

		npc.setDescription("Widzisz Florence Boullabaisse, kucharkę na rynku w Ados. Pilnuje garnka zupy rybnej z uwagą godną alchemika.");
		npc.setEntityClass("fishsoupmakernpc");
		npc.setPosition(63, 14);
		npc.setDirection(Direction.RIGHT);
		npc.initHP(100);
		zone.add(npc);
	}
}
