/* $Id: SellerNPC.java,v 1.13 2010/12/30 16:09:12 kiheru Exp $ */
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
package games.stendhal.server.maps.fado.hut;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.Map;

/**
 * A lady wizard who sells potions and antidotes. Original name: Sarzina
 */
public class SellerNPC implements ZoneConfigurator {

	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}
	
	private void buildNPC(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("Sarzina") {
			
			@Override
			public void createDialog() {
				addGreeting();
				addJob("Wytwarzam mikstury i antidota, aby #zaoferować je wojownikom.");
				addHelp("Możesz wziąć przygotowane przeze mnie lekarstwo na podróż. Zapytaj mnie o #ofertę.");
				addGoodbye();
			}

			/*
			 * (non-Javadoc)
			 * @see games.stendhal.server.entity.npc.SpeakerNPC#onGoodbye(games.stendhal.server.entity.RPEntity)
			 */
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
			
		};
		new SellerAdder().addSeller(npc, new SellerBehaviour(SingletonRepository.getShopList().get("superhealing")));
		npc.setPosition(3, 5);
		npc.setEntityClass("wizardwomannpc");
		npc.setDirection(Direction.DOWN);
		npc.setDescription("Widzisz Sarzine. Jest uzdrowicielką i wiele wie o twej karmie.");
		zone.add(npc);
	}

}