/* $Id: ThorgalNPC.java,v 1.9 2024/05/18 12:38:15 davvids Exp $ */
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
package games.stendhal.server.maps.semos.mines;

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


public class ThorgalNPC implements ZoneConfigurator {

	public static final int BUYING_PRICE = 10000;
 	private static final String QUEST_SLOT = "chaos_tunnels";

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildThorgal(zone);
	}

	private void buildThorgal(final StendhalRPZone zone) {
		final SpeakerNPC Thorgal = new SpeakerNPC("Thorgal") {

			@Override
			protected void createPath() {
				// NPC doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {
                games.stendhal.server.entity.npc.dialogue.HuntDialogueTopics.install(this);

				final Map<String, Integer> items = new HashMap<String, Integer>();
				items.put("zwój chaosu", BUYING_PRICE);

				new SellerAdder().addSeller(this, new QuestCompletedSellerBehaviour(QUEST_SLOT, "Sprzedaje zwoje do tuneli chaosu tylko swoim dobrym znajomym, którzy udowodnili swoją wartość w tych niebezpiecznych podziemiach.", items));
				// for quest see games.stendhal.server.maps.quest.HelpMrsYeti
				addGreeting("Witaj w tunelach chaosu. Szukasz drogi, zwojów czy rozmowy o ekwipunku?");
				addJob("Od lat przemierzam tunele chaosu pod Semos. Znam trud podróży przez te korytarze, dlatego dbam o sprzęt i zapasy zwojów; dobry ekwipunek pozwala zajść dalej i wrócić do domu.");
				addHelp("W tunelach chaosu łatwo zużyć zapasy, zanim dotrzesz do celu. Sprawdź ekwipunek i sposób powrotu przed dalszą drogą. O moim zamówieniu na wyposażenie możemy porozmawiać osobno; za tę pomoc udostępniam handel zwojami.");				
				addGoodbye("Powodzenia, wojowniku. Niech Twoja odwaga i siła prowadzą Cię przez mroki tuneli chaosu. Wracaj bezpiecznie.");
			}
		};

		Thorgal.setEntityClass("minernpc");
		Thorgal.setDescription("Widzisz Thorgala, podróżnika badającego tunele chaosu. Nawet podczas odpoczynku myśli o kolejnej wyprawie.");
		Thorgal.setPosition(117, 85);
		Thorgal.initHP(100);
		zone.add(Thorgal);
	}
}
