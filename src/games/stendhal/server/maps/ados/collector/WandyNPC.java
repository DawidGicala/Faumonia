/* $Id: WandyNPC.java,v 1.9 2011/09/08 17:20:59 bluelads99 Exp $ */
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
package games.stendhal.server.maps.ados.collector;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.Arrays;
import java.util.Map;

/**
 * Builds a Curator NPC in Kirdneh museum .
 *
 * @author kymara
 */
public class WandyNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone, attributes);
	}

	private void buildNPC(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC npc = new SpeakerNPC("Wandy") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
                games.stendhal.server.entity.npc.dialogue.CollectionDialogueTopics.install(this);
				addGreeting("Witaj przybyszu.");
				addJob("Mieszkam w Ados i zbieram rzadkie przedmioty. Nie każdy brak uzupełnię na miejscowym targu, więc czasem potrzebuję pomocy wędrowca. Do jednej wyprawy wybieram konkretne zamówienie.");
				addHelp("Szukam rzadkich okazów, których nie znajdę na pierwszym straganie. Zapytaj o #zadanie, a sprawdzę, czego brakuje mi do kolekcji. Najpierw uzgodnij ze mną zamówienie, żeby nie dźwigać zbędnych rzeczy.");
				addReply(Arrays.asList("exhibits", "eksponaty", "eksponatów"),"Być może będziesz mieć dryg do wyszukiwania przedmiotów i chciałbyś zrobić #zadanie dla mnie.");
				// remaining behaviour defined in games.stendhal.server.maps.quests.WeeklyItemQuest
				addGoodbye("Do zobaczenia. Jeśli trafisz na niezwykły okaz, pamiętaj o mojej kolekcji.");
			}
			
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.setEntityClass("wandynpc");
		npc.setPosition(8, 2);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		npc.setDescription("Widzisz Wandyego, mieszkańca Ados z zamiłowaniem do rzadkich przedmiotów. Chętnie słucha wieści o niezwykłych znaleziskach.");
		zone.add(npc);
	}
}
