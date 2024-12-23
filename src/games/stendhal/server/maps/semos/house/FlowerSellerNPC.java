/* $Id: FlowerSellerNPC.java,v 1.20 2012/02/13 00:16:54 bluelads99 Exp $ */
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
package games.stendhal.server.maps.semos.house;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.TeleporterBehaviour;

import java.util.Map;

/**
 * Builds a Flower Seller NPC for the Elf Princess quest.
 *
 * @author kymara
 */
public class FlowerSellerNPC implements ZoneConfigurator {
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {

        	new TeleporterBehaviour(buildSemosHouseArea(), "Kwiatki! Świeże kwiatki!");
	}

	private SpeakerNPC buildSemosHouseArea() {

	    final SpeakerNPC rose = new SpeakerNPC("Róża Kwiaciarka") {
	                @Override
			protected void createPath() {
				// npc does not move
				setPath(null);
			}
	                @Override
			protected void createDialog() {
			    addJob("Jestem wędrowną kwiaciarką z dalekiego Grodu Kraka.");
			    addGoodbye("Wszystko zaczyna się od róż ... dowidzenia ...");
			    // the rest is in the ElfPrincess quest
			}
		};

		rose.setEntityClass("gypsywomannpc");
		rose.initHP(100);
		rose.setDescription("Oto Róża Kwiaciarka. Skacze z miejsca na miejsce z koszykiem pełnym pięknych róż.");

		// start in int_semos_house
		final StendhalRPZone	zone = SingletonRepository.getRPWorld().getZone("int_semos_house");
		rose.setPosition(5, 6);
		zone.add(rose);

		return rose;
	}
}
