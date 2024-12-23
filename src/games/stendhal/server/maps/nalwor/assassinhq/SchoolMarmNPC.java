/* $Id: SchoolMarmNPC.java,v 1.5 2010/12/29 15:02:24 martinfuchs Exp $ */
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
package games.stendhal.server.maps.nalwor.assassinhq;

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
 * Builds a Teacher NPC who tries to make disciple assassins behave. 
 *
 * @author kymara with modifications by tigertoes
 */
public class SchoolMarmNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("Miss Parillaud") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(7, 3));
				nodes.add(new Node(13, 3));
				nodes.add(new Node(13, 18));
				nodes.add(new Node(7, 18));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			    protected void createDialog() {
				add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
						new GreetingMatchesNameCondition(getName()), true,
						ConversationStates.IDLE, "Dlaczego zawracasz mi głowę. Nie widzisz, że mam ręce pełne roboty! Teraz lil johnnnny. Powiedziałam Tobie, żebyś nie podbijał mu oka!", null);
	 	     }
		    
		};

		npc.setDescription("Oto raczej strapiona szkolna nauczycielka. Ma pełne ręce roboty z tymi małymi mordercami.");
		npc.setEntityClass("woman_014_npc");
		npc.setPosition(7, 3);
		npc.initHP(100);
		zone.add(npc);
	}
}
