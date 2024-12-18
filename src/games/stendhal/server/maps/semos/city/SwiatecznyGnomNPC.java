/* $Id: SwiatecznyGnomNPC.java,v 1.19 2023/11/26 23:59:27 davvids Exp $ */
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
package games.stendhal.server.maps.semos.city;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

public class SwiatecznyGnomNPC implements ZoneConfigurator {
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
		final SpeakerNPC elemental = new SpeakerNPC("Zimorodek") {

			@Override
			// he doesn't move.
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj w zimowym królestwie! Jestem Zimorodek, strażnik świątecznych tajemnic i radości. Czy przybywasz z daleka, by doświadczyć magii tych wyjątkowych dni i wykonać dla mnie drobne zadanie?");
				addGoodbye("Niech magia świąt towarzyszy Ci na każdym kroku! Dowidzenia i mam nadzieję, że wkrótce znowu mnie odwiedisz. Ho ho ho!");
			}
		};

		elemental.setDescription("Zimorodek to tajemniczy świąteczny gnomek, znany z serdeczności i miłości do zimowych świąt. Jego postać, choć niewielka, emanuje niezwykłą energią i radością. Otoczony atmosferą magii i świątecznego czaru, Zimorodek jest strażnikiem świątecznych tradycji i opiekunem zimowej radości. Jego obecność przypomina o cudach zimy i magii Świąt, a każde spotkanie z nim to szansa na doświadczenie prawdziwego ducha sezonu.");
		elemental.setEntityClass("santagnomenpc");
		elemental.setPosition(96, 38);
		elemental.setDirection(Direction.LEFT);
		elemental.initHP(100);
		zone.add(elemental);
	}
}
