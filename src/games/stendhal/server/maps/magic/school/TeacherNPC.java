/* $Id: TeacherNPC.java,v 1.9 2010/12/29 15:02:06 martinfuchs Exp $ */
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
package games.stendhal.server.maps.magic.school;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a Teacher NPC who flies on a broomstick. 
 *
 * @author kymara
 */
public class TeacherNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Esolte Vietta") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(29, 19));
				nodes.add(new Node(36, 19));
				nodes.add(new Node(36, 21));
				nodes.add(new Node(29, 21));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			    protected void createDialog() {
				add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
						new GreetingMatchesNameCondition(getName()), true,
						ConversationStates.IDLE, "Siedź cicho i obserwuj mnie!", null);
	 	     }
		    
		};

		npc.setDescription("Oto wiedźma latająca na miotle. Wygląda na to, że robi pokaz dla grupy uczniów.");
		npc.setEntityClass("witch3npc");
		npc.setPosition(29, 19);
		npc.initHP(100);
		zone.add(npc);
	}
}
