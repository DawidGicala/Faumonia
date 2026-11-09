/* $Id: elemental3NPC.java,v 1.19 2010/09/19 09:45:27 kymara Exp $ */
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
package games.stendhal.server.maps.ados.stronghold;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

public class Mage implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildelemental3Area(zone);
	}

	private void buildelemental3Area(final StendhalRPZone zone) {
		final SpeakerNPC elemental3 = new SpeakerNPC("Trener magów") {

			@Override
			// he doesn't move.
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
                games.stendhal.server.entity.npc.dialogue.CollectionDialogueTopics.install(this);
				addGreeting("Witaj, jestem trenerem magów.");
				addGoodbye("Żegnaj!");
				addHelp("Jeśli wykonasz #zadanie, to odznaczę Cię rubinowym medalem, pod warunkiem że posiadasz szafirowy.");
				addJob("Odpowiadam za magów i potrzebne im zapasy. Za wykonaną służbę mogę wymienić obsydianowy medal na szafirowy. Jeśli zechcesz starać się o dalsze odznaczenie, zachowaj go dla Majora Klykha.");
				
				add(
				        ConversationStates.ATTENDING,
				        Arrays.asList("odznaka", "medal"),
				        null,
				        ConversationStates.ATTENDING,
				        "Naprawdę chciałbyś wykonać #zadanie?",
				        null);
			}
		};

		elemental3.setDescription("Widzisz Trenera Magów z Ados. Spokojnie słucha meldunków, zanim oceni zasługi rozmówcy.");
		elemental3.setEntityClass("mboss");
		elemental3.setPosition(60, 29);
		elemental3.setDirection(Direction.LEFT);
		elemental3.initHP(100);
		zone.add(elemental3);
	}
}
