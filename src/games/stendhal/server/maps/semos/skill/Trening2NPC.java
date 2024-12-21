/* $Id: Trening2NPC.java,v 1.12 2020/11/28 19:03:40 davvids Exp $ */
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
package games.stendhal.server.maps.semos.skill;

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
 * Builds the NPC who deals in rainbow beans.
 * Other behaviour defined in maps/quests/RainbowBeans.java
 *
 * @author kymara
 */
public class Trening2NPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone, attributes);
	}

	//
	// IL0_GreeterNPC
	//

	private void buildNPC(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC Trening2NPC = new SpeakerNPC("Kazo") {

			@Override
			protected void createPath() {
				// doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addJob("Chyba wiesz czym się zajmuje.");
				addHelp("Niczego nie potrzebuje.");
				addQuest("Nie mam nic dla Ciebie, kolego.");
				addOffer("Ha! Tabliczka na drzwiach jest przykrywką! To nie jest hotel. Jeżeli chcesz się napić to wracaj do miasta.");
				addGoodbye("Dowidzenia.");
			}
		};

		Trening2NPC.setEntityClass("kazonpc");
		Trening2NPC.setPosition(27, 27);
		Trening2NPC.initHP(100);
		Trening2NPC.setDirection(Direction.LEFT);
		Trening2NPC.setDescription("Widzisz Kazo. Wymieni twoje szlachetne kryształy na bilet na arene treningową.");
		zone.add(Trening2NPC);
	}
}
