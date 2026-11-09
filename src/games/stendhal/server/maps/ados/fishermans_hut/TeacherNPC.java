/* $Id: TeacherNPC.java,v 1.22 2010/12/11 14:13:52 kymara Exp $ */
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
package games.stendhal.server.maps.ados.fishermans_hut;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Ados Fisherman's (Inside / Level 0).
 *
 * @author dine
 */
public class TeacherNPC implements ZoneConfigurator {
	
	// This is 1 minute at 300 ms per turn.
	private static final int TIME_OUT = 200;
	
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildTeacher(zone, attributes);
	}

	private void buildTeacher(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC fisherman = new SpeakerNPC("Santiago") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				// from left
				nodes.add(new Node(3, 3));
				// to right
				nodes.add(new Node(12, 3));
				// to left
				nodes.add(new Node(3, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
                games.stendhal.server.entity.npc.dialogue.JourneyDialogueTopics.install(this);
				addGreeting("Witaj nowicjuszu!");
				addJob("Uczę rybołówstwa i sprawdzam, czy przyszli wędkarze znają swoje połowy. Najpierw trzeba rozpoznać ryby, a potem dostarczyć po jednej z każdego wymaganego gatunku. Dopiero oba etapy składają się na kartę wędkarską.");
				addHelp("Dobry wędkarz rozpoznaje swój połów, zanim wrzuci go do koszyka. Zacznij od mojego #zadania: najpierw sprawdzimy znajomość ryb, a potem poproszę o okazy na część praktyczną. Nie musisz przynosić ich przed egzaminem.");
				addReply(Arrays.asList("oil", "olejek"),"Pomyliłeś się. Idź i zapytaj Pequoda o olejek. Mieszka w sąsiedniej chatce.");
			}
		};
		fisherman.setPlayerChatTimeout(TIME_OUT); 
		fisherman.setEntityClass("fishermannpc");
		fisherman.setDirection(Direction.DOWN);
		fisherman.setPosition(3, 3);
		fisherman.initHP(100);
		fisherman.setDescription("Widzisz Santiago, doświadczonego nauczyciela rybołówstwa z Ados. Przygotowuje stół do kolejnego egzaminu.");
		zone.add(fisherman);
	}
}
