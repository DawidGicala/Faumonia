/* $Id: GreenNatureWizardNPC.java,v 1.7 2010/09/19 02:35:23 nhnb Exp $ */
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
 * Silvanus, the nature wizard of the Wizards Tower
 *
 * see games.stendhal.server.maps.quests.WizardSilvanusPlainQuest
 */
public class GreenNatureWizardNPC implements ZoneConfigurator {

	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildSilvanus(zone);
	}

	private void buildSilvanus(final StendhalRPZone zone) {
		final SpeakerNPC silvanus = new SpeakerNPC("Silvanus") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(10, 4));
				nodes.add(new Node(10, 3));
				nodes.add(new Node(1, 3));
				nodes.add(new Node(9, 3));
				nodes.add(new Node(9, 9));
				nodes.add(new Node(10, 9));
				nodes.add(new Node(9, 9));
				nodes.add(new Node(9, 12));
				nodes.add(new Node(11, 12));
				nodes.add(new Node(11, 11));
				nodes.add(new Node(9, 11));
				nodes.add(new Node(9, 9));
				nodes.add(new Node(10, 9));
				nodes.add(new Node(9, 9));
				nodes.add(new Node(9, 5));
				nodes.add(new Node(2, 5));
				nodes.add(new Node(2, 13));
				nodes.add(new Node(2, 11));
				nodes.add(new Node(6, 11));
				nodes.add(new Node(6, 10));
				nodes.add(new Node(5, 10));
				nodes.add(new Node(5, 9));
				nodes.add(new Node(2, 9));
				nodes.add(new Node(2, 5));
				nodes.add(new Node(10, 5));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj, młodziutki przyjacielu!");
				addHelp("Wybacz kolego, jestem bardzo zajęty. Przygotowuję druidów do spotkania w kręgu czarodziejów.");
				addJob("Na imię mi Silvanus. Jestem reprezentantem druidów z #Lyreade w kręgu czarodziejów.");
				addOffer("Wybacz kolego, jestem bardzo zajęty. Przygotowuję druidów do spotkania w kręgu czarodziejów.");
				addQuest("Magia dopiero zaczyna pojawiać się w świecie. Jestem przez to bardzo zajęty, muszę przygotować druidów z #Lyreade do spotkania w kręgu czarodziejów. Powiem Ci, gdy będę mieć jakieś zadanie dla Ciebie.");
				addReply("Lyreade", "Lyreade, szkoła naturalnej magii, jest ukryta w gęstych, elfickich lasach.");
				addGoodbye("Do zobaczenia, przyjacielu!");

			} //remaining behaviour defined in maps.quests.WizardSilvanusPlainQuest
		};

		silvanus.setDescription("Widzisz Silvanusa, najstarszego mędrca druidów.");
		silvanus.setEntityClass("greenelfwizardnpc");
		silvanus.setPosition(11, 4);
		silvanus.initHP(100);
		zone.add(silvanus);
	}
}
