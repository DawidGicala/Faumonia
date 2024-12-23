/* $Id: BlackDeathWizardNPC.java,v 1.6 2010/09/19 02:35:23 nhnb Exp $ */
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
package games.stendhal.server.maps.semos.wizardstower;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Ravashack, the death wizard of the Wizards Tower
 *
 * see games.stendhal.server.maps.quests.WizardRavashackPlainQuest
 */
public class BlackDeathWizardNPC implements ZoneConfigurator {

	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildRavashack(zone);
	}

	private void buildRavashack(final StendhalRPZone zone) {
		final SpeakerNPC ravashack = new SpeakerNPC("Ravashack") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(5, 18));
				nodes.add(new Node(7, 18));
				nodes.add(new Node(7, 15));
				nodes.add(new Node(7, 20));
				nodes.add(new Node(12, 20));
				nodes.add(new Node(12, 21));
				nodes.add(new Node(9, 21));
				nodes.add(new Node(9, 25));
				nodes.add(new Node(11, 25));
				nodes.add(new Node(9, 25));
				nodes.add(new Node(9, 26));
				nodes.add(new Node(9, 21));
				nodes.add(new Node(2, 21));
				nodes.add(new Node(2, 25));
				nodes.add(new Node(4, 25));
				nodes.add(new Node(4, 27));
				nodes.add(new Node(6, 27));
				nodes.add(new Node(4, 27));
				nodes.add(new Node(4, 28));
				nodes.add(new Node(2, 28));
				nodes.add(new Node(2, 18));
				nodes.add(new Node(7, 18));
				nodes.add(new Node(7, 15));
				nodes.add(new Node(7, 18));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Pozdrowienia smiertelniku! O co chodzi tym razem?");
				addHelp("Przepraszam śmiertelniku, jestem bardzo zajęty, pracuję nad wprowadzeniem część nekromantów do kręgu czarodziejów.");
				addJob("Jestem Ravashack. Reprezętuje nekromantów z #Wraithforge w kręgu czarodziejów.");
				addOffer("Przepraszam śmiertelniku, jestem bardzo zajęty, pracuje nad przyjęciem części nekromantów do kręgu czarodziejów.");
				addQuest("Magia w tym świecie dopiero się zaczyna i jestem bardzo zajęty. Mym celem jest wprowadzenie nekromantów z #Wraithforge do okręgu czarodziejów. Z czasem będę miał dla ciebie zadanie.");
				addReply("Wraithforge", "W centrum pola chwały leży Wraithforge, szkoła czarnej magii.");
				addGoodbye("Żegnaj, śmiertelniku!");

			} //remaining behaviour defined in maps.quests.WizardRavashackPlainQuest
		};

		ravashack.setDescription("Widzisz Ravashacka, wielkiego i mistycznego Nekromate.");
		ravashack.setEntityClass("largeblackwizardnpc");
		ravashack.setPosition(5, 17);
		ravashack.initHP(100);
		zone.add(ravashack);
	}
}
