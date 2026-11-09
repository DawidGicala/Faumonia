/* $Id: HusbandNPC.java,v 1.11 2011/07/10 15:32:39 bluelads99 Exp $ */

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
package games.stendhal.server.maps.athor.holiday_area;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

public class HusbandNPC implements ZoneConfigurator  {

	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("John") {
			@Override
			public void say(final String text) {
				// He doesn't move around because he's "lying" on his towel.
				say(text, false);
			}

			@Override
			protected void createPath() {
				// doesn't move
				setPath(null);
	}
	
			@Override
			public void createDialog() {
                games.stendhal.server.entity.npc.dialogue.HuntDialogueTopics.install(this);
				addGreeting("Cześć!");
				addJob("Na co dzień jestem woźnicą, ale tutaj nie ma nawet powozów. Przyjechałem z Jane odpocząć; szkoda tylko, że potwory w podziemiach tak skutecznie ograniczają nasze wycieczki.");
				addHelp("Nie próbuj rozmawiać z moją żoną, jest bardzo nieśmiała.");
				addGoodbye("Dowidzenia!");
			}

		};
		npc.setPosition(27, 44);
		npc.setEntityClass("swimmer5npc");
		npc.setDescription ("Widzisz Johna odpoczywającego na plaży Athor z żoną Jane. Chętnie zamieniłby leżenie na piasku na zwiedzanie wyspy.");
		npc.setDirection(Direction.DOWN);
		zone.add(npc);		
	}
}

