/* $Id: elementalNPC.java,v 1.19 2020/11/01 8:24:27 szyg edited by davvids Exp $ */
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
package games.stendhal.server.maps.ados.city;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

public class ElementalNPC2 implements ZoneConfigurator {
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
		final SpeakerNPC elemental = new SpeakerNPC("Żywioł Wody") {

			@Override
			// he doesn't move.
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj, jestem Wodnym żywiołem.");
				addGoodbye("Na razie.");
				addHelp("Znam starodawne techniki magiczne. Mógłbym Ci pomóc w ulepszeniu sztyletu zwanego sztyletem żywiołów przy pomocy #specjalnego zaklęcia.");
				addJob("Kiedyś byłem jednym z najlepszych kowali na całą Faumonie.");
				
				add(
				        ConversationStates.ATTENDING,
				        Arrays.asList("special", "specjalnego"),
				        null,
				        ConversationStates.ATTENDING,
				        "Jako żywioły od wiekow byliśmy jednymi z najlepszych kowali. Znamy unikalne metody magiczne, których kiedyś używaliśmy przy produkcji broni. Moge ci pomóc, ale będziesz musiał wykonać dla mnie pewne #zadanie..",
				        null);
			}
		};

		elemental.setDescription("Oto Żywioł Wody. Czujesz się dziwnie stojąc w pobliżu.");
		elemental.setEntityClass("koreknpc2");
		elemental.setPosition(119, 115);
		elemental.setDirection(Direction.DOWN);
		elemental.initHP(100);
		zone.add(elemental);
	}
}
