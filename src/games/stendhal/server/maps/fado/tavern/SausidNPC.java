/* $Id: SausidNPC.java,v 1.19 2025/06/22 11:45:27 davvids Exp $ */
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
package games.stendhal.server.maps.fado.tavern;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

public class SausidNPC implements ZoneConfigurator {

	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildSausidNPCArea(zone);
		buildSausidNPCArea(zone);
	}

	private void buildSausidNPCArea(final StendhalRPZone zone) {
		final SpeakerNPC SausidNPC = new SpeakerNPC("Sausid") {

			@Override
			// he doesn't move.
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
                games.stendhal.server.entity.npc.dialogue.HuntDialogueTopics.install(this);
				addGreeting("Psst.... nie mów głośno, żeby nikt nie usłyszał...");
				addGoodbye("Żegnaj... pamiętaj, aby nic nie mówić o naszej rozmowie...");
				addHelp("Ciszej, proszę. Jeśli chcesz mi pomóc, zapytaj o #zadanie, a wyjaśnię, z którymi stworami mam kłopot. Nie potrzebuję do tej rozmowy całej karczmy.");
				addJob("Mam kłopot z kilkoma stworami, ale nie chcę robić z tego przedstawienia dla całej karczmy. Jeśli umiesz dotrzymać słowa, możemy spokojnie ustalić szczegóły wyprawy.");
			}
		};

		SausidNPC.setDescription("Widzisz Sausida w karczmie Fado. Ścisza głos, kiedy rozmowa schodzi na jego sprawy.");
		SausidNPC.setEntityClass("carenpc");
		SausidNPC.setPosition(11, 6);
		SausidNPC.setDirection(Direction.DOWN);
		SausidNPC.initHP(100);
		zone.add(SausidNPC);
	}
}
