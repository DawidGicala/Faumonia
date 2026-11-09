/* $Id: CampingGirlNPC.java,v 1.16 2010/09/19 02:30:48 nhnb Exp $ */
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
package games.stendhal.server.maps.orril.river;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

/**
 * Configure Orril River South Campfire (Outside/Level 0).
 */
public class CampingGirlNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildCampfireArea(zone);
	}

	private void buildCampfireArea(final StendhalRPZone zone) {
		final SpeakerNPC sally = new SpeakerNPC("Sally") {

			@Override
			protected void createPath() {
				// NPC does not move
				setPath(null);
			}

			@Override
			protected void createDialog() {
                addReply("tematy", "Możemy porozmawiać o obozowaniu albo drewnie do ogniska. Ja przez ten czas dopilnuję pieczenia.");
				//addGreeting();
				addJob("Jestem skautką i uczę się radzić sobie poza miastem. Tata, Leander z piekarni w Semos, zna się na jedzeniu, a ja próbuję dopilnować go tutaj przy ognisku. Jak widać, to też wymaga pracy!");
				addHelp("W lesie znajdziesz drewno i grzyby, ale nie każdy grzyb nadaje się do jedzenia. Polan szukaj w lesie na północ stąd. Na wyprawę zabierz zapasy, żeby zgłodniały brzuch nie zmuszał cię do zgadywania, co można zjeść.");
				addGoodbye("Do widzenia! Wpadnij jeszcze odpocząć przy ogniu.");
				// remaining behaviour is defined in maps.quests.Campfire.				
			}
		};

		sally.setEntityClass("littlegirlnpc");
		sally.setPosition(40, 61);
		sally.setDirection(Direction.RIGHT);
		sally.initHP(100);
		sally.setDescription("Widzisz Sally, córkę piekarza Leandera z Semos. Obozuje nad rzeką Orril i uważnie pilnuje ogniska.");
		zone.add(sally);
	}
}
