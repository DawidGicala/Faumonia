/* $Id: MikolajNPC.java,v 1.19 2023/11/26 23:59:27 davvids Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2020 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.semos.bank;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

public class MikolajNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildElementalArea(zone);
	}

	private void buildElementalArea(final StendhalRPZone zone) {
		final SpeakerNPC elemental = new SpeakerNPC("Święty Mikołaj") {

			@Override
			// he doesn't move.
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Ho ho ho! Witaj, przyjacielu! Jak miło cię widzieć w tych radosnych czasach.");
				addGoodbye("Niech magia i ciepło świąt wypełnią twoje serce radością! Ho ho ho! Dowidzenia i pamiętaj, aby zawsze mieć w sercu świątecznego ducha.");
			}
		};

		elemental.setDescription("Święty Mikołaj to postać owiana legendą i miłością. Jego charakterystyczny czerwony strój, biała broda i serdeczny śmiech są znane dzieciom i dorosłym na całym świecie. Mikołaj jest symbolem dobroci, hojności i świątecznej radości. Jego obecność przynosi poczucie cudu i magii, a jego misją jest rozsiewanie szczęścia i miłości podczas zimowych świąt. Jest przyjacielem dla wszystkich i każdego roku podróżuje po świecie, by obdarować prezenty tym, którzy w niego wierzą");
		elemental.setEntityClass("santaclausnpc");
		elemental.setPosition(36, 10);
		elemental.setDirection(Direction.LEFT);
		elemental.initHP(100);
		zone.add(elemental);
	}
}
