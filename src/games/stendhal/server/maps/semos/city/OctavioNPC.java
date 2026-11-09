/* $Id: OctavioNPC.java,v 1.20 2012/05/21 21:54:46 bluelads99 Exp $ */
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

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

/**
 * An old man (original name: Octavio) who stands around and gives directions
 * to newcomers. He's the brother of RetireeNPC (original name: Diogenes).
 *
 * @see games.stendhal.server.maps.quests.KillHigh

 */
public class OctavioNPC implements ZoneConfigurator {

	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}
	
	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Octavio") {
			@Override
			public void createDialog() {
                games.stendhal.server.entity.npc.dialogue.HuntDialogueTopics.install(this);
				addGreeting("Witaj.");
				addJob("Jestem bratem Sato i mam za sobą dość lat, żeby nie pamiętać każdego zajęcia. Wciąż jednak rozpoznaję zagrożenia, których nie załatwi krótka wyprawa za miasto; takim sprawom poświęcam teraz uwagę.");
				addOffer("Niczego nie potrzebuje, niczego nie sprzedaje.");
				addGoodbye("Dowidzenia i dziękuję za rozmowę.");
				// All further behaviour is defined in quest classes.
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.LEFT);
			}
			
			
			
		};
		npc.setPosition(73, 26);
		npc.setEntityClass("octavionpc");
		npc.setDescription("Widzisz Octavia, starego podróżnika z Semos. Wiele zapomniał, ale imiona groźnych stworzeń wciąż wymawia bez wahania.");
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}

}