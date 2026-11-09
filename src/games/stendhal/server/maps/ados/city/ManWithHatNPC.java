/* $Id: ManWithHatNPC.java,v 1.5 2011/07/10 19:23:24 bluelads99 Exp $ */
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
package games.stendhal.server.maps.ados.city;

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
 * Builds a sad NPC (name: Andy) who lost his wife
 * 
 * @author Erdnuggel (idea) and Vanessa Julius (implemented)
 * 
 */

public class ManWithHatNPC implements ZoneConfigurator {

	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Andy") {
		    
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(41, 6));
				nodes.add(new Node(41, 11));
                nodes.add(new Node(64, 11));
                nodes.add(new Node(64, 6));  
                nodes.add(new Node(63, 6));
                nodes.add(new Node(63, 10)); 
                nodes.add(new Node(42, 10)); 
                nodes.add(new Node(42, 6));
                nodes.add(new Node(41, 6)); 
               	setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
                games.stendhal.server.entity.npc.dialogue.HuntDialogueTopics.install(this);
				addGreeting("Witaj.");
				addHelp("Z żoną często podróżowaliśmy; najbardziej lubiliśmy plażę na Athor. Dziś trudno mi wracać do tych wspomnień. Jeśli wybierasz się w drogę z kimś bliskim, nie zostawiaj go samego w niebezpieczeństwie.");
				addJob("Przestałem pracować po śmierci żony. Kiedyś dużo razem podróżowaliśmy, a teraz najtrudniej mi myśleć o drodze do Wofol, z której już nie wróciła.");
				addOffer("Nie mam nic do zaoferowania.");
				addGoodbye("Do widzenia. Dziękuję, że znalazłeś chwilę, żeby mnie wysłuchać.");
				
			}
		};

		npc.setDescription("Widzisz Andyego, przygnębionego mieszkańca Ados w kapeluszu. Czasem urywa zdanie, jakby wracał myślami do dawnej podróży.");
		npc.setEntityClass("manwithhatnpc");
		npc.setPosition(41, 6);
		npc.setDirection(Direction.RIGHT);
		npc.initHP(100);
		zone.add(npc);
	}
}
