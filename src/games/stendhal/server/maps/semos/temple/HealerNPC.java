/* $Id: HealerNPC.java,v 1.21 2010/09/19 02:35:38 nhnb Exp $ */
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
package games.stendhal.server.maps.semos.temple;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.HealerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HealerNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildSemosTempleArea(zone, attributes);
	}

	private void buildSemosTempleArea(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC npc = new SpeakerNPC("Ilisa") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(9, 6));
				nodes.add(new Node(14, 6));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
                addReply("tematy", "Możemy porozmawiać o magicznych kulach, leczeniu albo zapasach na podróż. Powiedz, czego teraz potrzebujesz.");
				addGreeting("Witaj w świątyni. Zatrzymaj się na chwilę i powiedz, z czym przychodzisz.");
				addJob("Opiekuję się rannymi i przygotowuję lekarstwa dla podróżnych. Uczę też ostrożnego obchodzenia się z magią - nawet niewielka kula potrafi kryć wielką moc.");
				addHelp("Jeśli potrzebujesz leczenia, powiedz #ulecz, a ustalimy koszt. Na dalszą drogę możesz też kupić mikstury i antidota z mojej #oferty. Lepiej przygotować zapasy tutaj, niż szukać ich dopiero w niebezpieczeństwie.");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("healing")));
				// charge (2*the player level + 1) to heal
				new HealerAdder().addHealer(this, -2);
				addGoodbye("Do widzenia. Niech droga będzie dla ciebie łagodna.");
			}
		};

		npc.setEntityClass("welcomernpc");
		npc.setDescription("Widzisz Ilisę, uzdrowicielkę ze świątyni w Semos. Mówi spokojnie, a wokół niej unosi się delikatna magiczna aura.");
		npc.setPosition(9, 6);
		npc.initHP(100);
		zone.add(npc);
	}
}
