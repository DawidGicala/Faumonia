/* $Id: DiakesNPCSummer2024Summer2024.java,v 1.20 2024/06/19 15:27:58 davvids Exp $ */
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
 * Builds ados dakes NPC.
 * He may give an items quest later
 * Now he sells ados scrolls
 * @author kymara
 */
public class DiakesNPCSummer2024 implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		builddakes(zone);
	}

	private void builddakes(final StendhalRPZone zone) {
		final SpeakerNPC dakes = new SpeakerNPC("Diakes") {

			@Override
			protected void createDialog() {
				addGreeting("Witaj, podróżniku. Wpadnij i zrelaksuj się na plaży.");
				addJob("Leżę tutaj na plaży i cieszę się słońcem.");
				addHelp("Hmm, może mógłbyś przynieść mi coś do jedzenia?");
				addGoodbye("Na razie, nie zapomnij wrócić z przekąskami.");
			}
		};

		dakes.setDescription("Widzisz Diakesa. Wygląda na to, ze jest to leniwy wczasowicz, który leży na kocu i wyleguje się na słońcu.");
		dakes.setEntityClass("barmannpc");
		dakes.setPosition(16, 3);
		dakes.initHP(100);
		zone.add(dakes);
	}
}
