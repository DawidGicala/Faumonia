/* $Id: FurgoNPC.java,v 1.19 2012/08/23 20:05:44 yoriy Exp $ */
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
package games.stendhal.server.maps.semos.bank;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a Bar Maid NPC to buy food from players.
 *
 * @author kymara
 */
public class FurgoNPC implements ZoneConfigurator {

	private final ShopList shops = SingletonRepository.getShopList();


	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Furgo") {

			@Override
			protected void createPath() {
				// NPC doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Witam!");
				addJob("Skupuje totemy aniołka. Możesz je dostać od Soro na mapiee startowej. Daj mi znać jak będziesz chciał je sprzedać: #oferta. Jeśli chcesz sprzedać napisz: #sprzedaj #statuetka #aniołka");
				addHelp("Moge kupić od ciebie totemy anioła jeśli jakieś posiadasz. Jeśli chcesz sprzedać napisz: #sprzedaj #statuetka #aniołka");
				addQuest("Nie mam dla ciebie żadnego zadania.");
 				addGoodbye("Dowidzenia, dowidzenia!");
 				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("furgo1")), true);
			}
		};
		npc.setDescription("Widzisz przyjaznego Furgo. Wygląda on na kogoś, kogo możesz poprosić o pomoc.");
		npc.setEntityClass("furgo_npc");
		npc.setPosition(43, 2);
		npc.initHP(100);
		zone.add(npc);
	}
}
