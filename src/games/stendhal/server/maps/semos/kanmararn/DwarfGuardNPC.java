/* $Id: DwarfGuardNPC.java,v 1.21 2012/08/23 20:05:43 yoriy Exp $ */
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
package games.stendhal.server.maps.semos.kanmararn;

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

public class DwarfGuardNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

		/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildPrisonArea(zone, attributes);
	}

	private void buildPrisonArea(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC npc = new SpeakerNPC("Hunel") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(10, 23));
				nodes.add(new Node(12, 23));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
                games.stendhal.server.entity.npc.dialogue.JourneyDialogueTopics.install(this);
			    addQuest("Boję się stąd wychodzić... Czy możesz mi #zaoferować naprawdę dobry sprzęt?");
				addJob("Pilnowałem tego więzienia, zanim zaatakowały duergary. Teraz sam siedzę za kratami i nasłuchuję każdego kroku. Przydałaby mi się pomoc kogoś, kto zdoła zdobyć klucz.");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buychaos")), true);

				addGoodbye("Dowidzenia ... bądź ostrożny ...");
			}
			// remaining behaviour is defined in maps.quests.JailedDwarf.
		};

		npc.setEntityClass("dwarfguardnpc");
		npc.setDescription("Widzisz Hunela, przestraszonego krasnoluda za kratami więzienia Kanmararn. Wciąż trudno mu pogodzić się z utratą kontroli nad strażnicą.");
		npc.setPosition(10, 23);
		npc.setPerceptionRange(7);
		npc.initHP(100);
		zone.add(npc);
	}
}
