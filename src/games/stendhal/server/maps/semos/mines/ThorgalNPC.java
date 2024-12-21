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

	public static final int BUYING_PRICE = 4000;
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

				final Map<String, Integer> items = new HashMap<String, Integer>();
				items.put("zwój chaosu", BUYING_PRICE);

				new SellerAdder().addSeller(this, new QuestCompletedSellerBehaviour(QUEST_SLOT, "Sprzedaje zwoje do tuneli chaosu tylko swoim dobrym znajomym, którzy udowodnili swoją wartość w tych niebezpiecznych podziemiach.", items));
				// for quest see games.stendhal.server.maps.quest.HelpMrsYeti
				addGreeting("Powitanie, podróżniku! Jestem Thorgal, wędrowiec, który od lat przemierza tunele chaosu. Jeśli masz odwagę zmierzyć się z niebezpieczeństwami tych mrocznych podziemi, mogę mieć dla Ciebie pewne zadanie. Przynieś mi: hełm chaosu, zbroję chaosu, spodnie chaosu, buty chaosu, płaszcz chaosu, tarczę chaosu oraz miecz chaosu. #Musisz #mieć #wszystkie #przedmioty #przy #sobie. ");
				addJob("Jestem podróżnikiem, który odkrywa sekrety ogromnych tuneli chaosu, ukrytych głęboko w jaskiniach pod Semos. Te tunele skrywają wiele tajemnic i niebezpieczeństw, które tylko czekają na śmiałków gotowych je stawić czoła. Aktualnie nie potrzebuje pomocy.");
				addHelp("Tunele chaosu są pełne niebezpieczeństw i potworów. Jeśli zdecydujesz się zejść głębiej w te mroczne korytarze, bądź przygotowany na wszystko. Znajdziesz tam zarówno cenne skarby, jak i śmiertelne zagrożenia.");				
				addGoodbye("Powodzenia, wojowniku. Niech Twoja odwaga i siła prowadzą Cię przez mroki tuneli chaosu. Wracaj bezpiecznie.");
			}
		};

		Thorgal.setEntityClass("minernpc");
		Thorgal.setDescription("Oto Thorgal, podróżnik, który od lat zwiedza tunele chaosu pod Semos. Być może powinieneś z nim porozmawiać i zapytać o niebezpieczeństwa i sekrety tych mrocznych podziemi");
		Thorgal.setPosition(117, 85);
		Thorgal.initHP(100);
		zone.add(Thorgal);
	}
}
