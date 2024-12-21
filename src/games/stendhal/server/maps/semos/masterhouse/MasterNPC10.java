/* $Id: MasterNPC10.java,v 1.19 2020/12/02 20:47:41 davvids Exp $ */
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
package games.stendhal.server.maps.semos.masterhouse;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.QuestCompletedBuyerBehaviour;

import java.util.Map;

public class MasterNPC10 implements ZoneConfigurator {
   private final ShopList shops = SingletonRepository.getShopList();
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildRockArea(zone);
	}

	private void buildRockArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Mistrz Faumonii X") {

			@Override
			protected void createPath() {
				// NPC doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {
			  // This greeting is mostly not used as the quests override it
				addGreeting("Witaj wojowniku! Możesz zapytać mnie o #zadanie i #pomoc");
				addHelp("Kolejne zadanie dostępne jest dla graczy od 70 poziomu doświadczenia.");
				addJob("Swoje już powalczyłem, nie musze pracować.");
				addGoodbye("Miło było Cię poznać.");
				// will buy black items once the Ultimate Collector quest is completed
			}
			/* remaining behaviour is defined in:
			 * maps.quests.WeaponsCollector,
			 * maps.quests.WeaponsCollector2 and
			 * maps.quests.UltimateCollector. */
		};

		npc.setEntityClass("masternpc");
		npc.setPosition(25, 49);
		npc.setDirection(Direction.LEFT);
		npc.initHP(100);
		npc.setDescription("Oto Mistrz Faumonii X. Wygląda ci na kogoś bardzo doświadczonego. Może powinieneś zapytać go o zadanie?");
		zone.add(npc);
	}
}
