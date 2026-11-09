/* $Id: Urzednik1NPC.java,v 1.18 2010/09/19 02:35:31 nhnb Exp $ */
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
package games.stendhal.server.maps.semos.townhall;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Urzednik1NPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildSemosTownhallAreaMayor(zone);
	}

	/**
	 * Adds a Mayor to the townhall who gives out daily quests.
	 * @param zone zone to be configured with this
	 */
	private void buildSemosTownhallAreaMayor(final StendhalRPZone zone) {
		// We create an NPC
		final SpeakerNPC npc = new SpeakerNPC("Urzędnik Hans") {

			@Override
			protected void createDialog() {
                games.stendhal.server.entity.npc.dialogue.CollectionDialogueTopics.install(this);
				addGreeting("Witam obywatelu! Potrzebujesz #pomocy?");
				addJob("Przyjmuję zgłoszenia dotyczące potworów w okolicy Semos. Wspólnie z Billem i burmistrzem prowadzimy jeden rejestr polowań, więc nie musisz trzykrotnie wyjaśniać tej samej sprawy. Powiedz, czy przychodzisz po zlecenie, czy z meldunkiem.");
				addHelp("Zanim ruszysz na polowanie, sprawdź cel i potrzebną liczbę przeciwników. Zapytaj o #zadanie, a odczytam twoje zlecenie. Bill i burmistrz mają dostęp do tych samych meldunków.");
				addGoodbye("Życzę miłego dnia!");
			}
		};

		npc.setEntityClass("urzedniknpc");
		npc.setDescription("Widzisz urzędnika Hansa z Semos. Uważnie porównuje zasłyszane meldunki z własnymi notatkami.");
		npc.setPosition(4, 15);
		npc.initHP(100);
		zone.add(npc);
	}
}
