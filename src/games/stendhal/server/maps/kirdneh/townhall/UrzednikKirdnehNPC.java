/* $Id: UrzednikKirdnehNPC.java,v 1.9 2029/11/27 14:38:15 davvids Exp $ */
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
package games.stendhal.server.maps.kirdneh.townhall;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.QuestCompletedSellerBehaviour;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class UrzednikKirdnehNPC implements ZoneConfigurator {

	public static final int BUYING_PRICE = 400;
 	private static final String QUEST_SLOT = "kirdneh_badge";

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildUrzednik(zone);
	}

	private void buildUrzednik(final StendhalRPZone zone) {
		final SpeakerNPC Urzednik = new SpeakerNPC("Urzędnik Kirdneh") {

			@Override
			protected void createPath() {
				// NPC doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {

				final Map<String, Integer> items = new HashMap<String, Integer>();
				items.put("zwój kirdneh", BUYING_PRICE);

				new SellerAdder().addSeller(this, new QuestCompletedSellerBehaviour(QUEST_SLOT, "Sprzedaje zwoje do miasta tylko zasłużonym mieszkańcom Kirdneh.", items));
				// for quest see games.stendhal.server.maps.quest.HelpMrsYeti
				addGreeting("Dzień dobry. Witaj w urzędzie miasta Kirdneh.");
				addJob("Aktualnie nie mamy żadnego zadania urzędowego. Mieszkańcy miasta Kirdneh często potrzebują kogoś do pomocy, powinieneś zapytać ich o #zadanie.");
				addHelp("Miasto Kirdneh znajduje się w południowej części Faumonii w pobliżu lasu.");				
				addGoodbye();
			}
		};

		Urzednik.setEntityClass("urzednikkirdnehnpc");
		Urzednik.setDescription("Oto Urzędnik Kirdneh. Powinieneś z nim porozmawiać i zapytać o zadanie.");
		Urzednik.setPosition(8, 3);
		Urzednik.initHP(100);
		zone.add(Urzednik);
	}
}
