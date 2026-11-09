/* $Id: GossipNPC.java,v 1.12 2011/03/31 15:30:57 bluelads99 Exp $ */
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
package games.stendhal.server.maps.kirdneh.city;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

/**
 * Builds a information giving NPC in Kirdneh city. 
 *
 * @author kymara
 */
public class GossipNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone, attributes);
	}

	private void buildNPC(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC npc = new SpeakerNPC("Jef") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}

			@Override
			protected void createDialog() {
                addReply("tematy", "Możemy porozmawiać o mamie albo o mieście. Ja zostanę tutaj, żeby wiedziała, gdzie mnie szukać.");
				addGreeting("Cześć. Zatrzymasz się na chwilę? Dobrze mieć z kim porozmawiać.");
				addJob("Jeszcze nie pracuję, jestem na to za mały. Czekam tutaj na mamę, Amber, i patrzę, kto przechodzi przez miasto. Ostatnio wypatruję jej częściej niż kogokolwiek innego.");
				addHelp("Nie znam wszystkich dróg, ale mogę opowiedzieć ci o mamie. Jeśli chcesz mi pomóc, zapytaj o #zadanie. Najbardziej zależy mi na wiadomości, że jest bezpieczna.");
				addOffer("Nie sprzedaję, ja czekam tylko na mamę. Mam #nowiny jeżeli chcesz wiedzieć.");
				addQuest("Co? Nie rozumiem Cię.");
				addReply(Arrays.asList("news", "nowiny"), "Paru sprzedawców będzie wkrótce na bazarze! Będzie fajnie, a na razie jest tutaj pusto.");
				addReply(Arrays.asList("shops", "sklepów"), "Tak, ona wyjechała z miasteczka. Jedynie co tutaj mamy to kwiaciarkę! Są #nowiny, że będziemy mieli własny bazar ...");
				addGoodbye("Do widzenia. Dzięki za chwilę rozmowy.");
			}
		};

		npc.setEntityClass("kid6npc");
		npc.setPosition(114, 67);
		npc.initHP(100);
		npc.setDescription("Widzisz Jefa, chłopca czekającego w Kirdneh. Wypatruje znajomej twarzy wśród przechodniów.");
		zone.add(npc);
	}
}
