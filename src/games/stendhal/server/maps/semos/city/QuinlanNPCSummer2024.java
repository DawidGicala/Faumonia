/* $Id: QuinlanNPCSummer2024Summer2024.java,v 1.20 2024/06/29 14:27:58 davvids Exp $ */
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
package games.stendhal.server.maps.semos.city;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds ados quinlan NPC.
 * He may give an items quest later
 * Now he sells ados scrolls
 * @author kymara
 */
public class QuinlanNPCSummer2024 implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildquinlan(zone);
	}

	private void buildquinlan(final StendhalRPZone zone) {
		final SpeakerNPC quinlan = new SpeakerNPC("Quinlan") {

			@Override
			protected void createDialog() {
				addGreeting("Witaj, wojowniku... ale dziś gorąco...");
				addJob("Odpoczywam na plaży i ciesze się słońcem. Nic innego mi nie potrzeba...");
				addHelp("Hmm, chyba nic nie potrzebuje...");
				addGoodbye("Na razie, pamiętaj, aby się nie opalać zbyt długo");
			}
		};

		quinlan.setDescription("Widzisz Quinlana. Wygląda na to, ze jest to leniwy wczasowicz, który leży na kocu i wyleguje się na słońcu.");
		quinlan.setEntityClass("swimmer1npc");
		quinlan.setPosition(30, 16);
		quinlan.initHP(100);
		zone.add(quinlan);
	}
}
