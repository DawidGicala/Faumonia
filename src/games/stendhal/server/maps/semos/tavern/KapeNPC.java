/* $Id: KapeNPC.java,v 1.19 2020/12/04 11:59:27 davvids Exp $ */
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
package games.stendhal.server.maps.semos.tavern;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

public class KapeNPC implements ZoneConfigurator {
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
		final SpeakerNPC elemental = new SpeakerNPC("Kape") {

			@Override
			// he doesn't move.
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
                games.stendhal.server.entity.npc.dialogue.HuntDialogueTopics.install(this);
                addJob("Nie pochodzę stąd i chciałem po prostu obejrzeć tutejsze góry. Barbarzyńcy szybko ostudzili mój zapał do wycieczek; teraz szukam kogoś, kto potrafi poradzić sobie z ich osadą.");
				addGreeting("Witaj.");
				addGoodbye("Na razie.");
			}
		};

		elemental.setDescription("Widzisz Kapego, przybysza odpoczywającego w karczmie Semos. Na wspomnienie gór marszczy brwi.");
		elemental.setEntityClass("npc_bazylhalszka");
		elemental.setPosition(26, 11);
		elemental.setDirection(Direction.DOWN);
		elemental.initHP(100);
		zone.add(elemental);
	}
}
