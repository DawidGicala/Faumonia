/* $Id: LibrarianNPC.java,v 1.16 2010/09/19 02:35:43 nhnb Exp $ */
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
package games.stendhal.server.maps.semos.library;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LibrarianNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildSemosLibraryArea(zone, attributes);
	}

	private void buildSemosLibraryArea(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC npc = new SpeakerNPC("Ceryl") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(28, 12));
				nodes.add(new Node(28, 21));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
                games.stendhal.server.entity.npc.dialogue.JourneyDialogueTopics.install(this);
				addGreeting();
				addJob("Opiekuję się biblioteką Semos. Każda księga ma tu swoje miejsce, choć czytelnicy nie zawsze pamiętają o terminie zwrotu. Jeśli szukasz konkretnego tomu, powiedz, dla kogo i do czego jest potrzebny.");
				addHelp("Rozejrzyj się po księgach; nieraz oszczędzą ci błądzenia po całej krainie. Jeśli ktoś przysłał cię po konkretny tom, powiedz, o jaką sprawę chodzi. Przydałaby mi się też pomoc z jedną zaległą książką - zapytaj o #zadanie.");
				addGoodbye();
			}
		};

		npc.setEntityClass("investigatornpc");
		npc.setDescription("Widzisz Ceryla, bibliotekarza z Semos. Przegląda półki tak uważnie, jakby wciąż szukał brakującego tomu.");
		npc.setPosition(28, 12);
		npc.initHP(100);
		zone.add(npc);
	}
}
