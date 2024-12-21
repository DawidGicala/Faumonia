/* $Id: JuhasNPC.java,v 1.8 2011/03/09 00:48:01 Legolas Exp $ */
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
package games.stendhal.server.maps.pol.zakopane.home;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Ados MithrilForger (Inside / Level 0).
 *
 * @author kymara
 */
public class JuhasNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */

	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildJuhas(zone, attributes);
	}

	private void buildJuhas(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC juhas = new SpeakerNPC("Juhas") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Pozdrawiam.");
				addJob("Sprzedaję #magiczne #zwoje. Zapytaj mnie o #ofertę.");
				addHelp("Sprzedaję #zwoje, które mogą uratować Tobie życie.");

				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("juhas")));

				add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
						null,
						ConversationStates.ATTENDING,
						"Nie mam dla Ciebie zadania. Jedynie mam do zaoferowania #'magiczne zwoje'.", null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("magiczne zwoje", "zwoje"),
						null,
						ConversationStates.ATTENDING,
						"Oferuję zwoje i bilety, które pomagają w szybszym podróżowaniu: zwój ados, zwój fado, zwój semos, zwój kirdneh, zwój nalwor oraz #'bilet turystyczny'.", null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("ados", "zwój ados"),
						null,
						ConversationStates.ATTENDING,
						"Zwój do Ados przeniesie cie do stolicy Faumonii!", null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("semos", "zwój semos"),
						null,
						ConversationStates.ATTENDING,
						"Zwój Semos przeniesie cie do miasta w którym aktualnie jesteśmy.", null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("nalwor", "zwój nalwor"),
						null,
						ConversationStates.ATTENDING,
						"Zwój nalwor przeniesie cie do miasta elfów.", null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("fado", "zwój fado"),
						null,
						ConversationStates.ATTENDING,
						"Zwój fado przeniesie cie do południowego miasta Fado", null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("kirdneh", "zwój kirdneh"),
						null,
						ConversationStates.ATTENDING,
						"Zwój kirdneh przeniesie cie do południowego miasta Kirdneh.", null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("turystyczny", "bilet turystyczny"),
						null,
						ConversationStates.ATTENDING,
						"Bilet turystyczny zabiera na pustynie w pobliżu piramid. Udając się tam zaopatrz się w wodę, a także pamiętaj o upalnych dniach i zimnych nocach oraz niebezpiecznych burzach piaskowych!", null);

				addGoodbye("Dowidzenia i udanej podróży.");
				setDirection(Direction.UP);

			}
		};

		juhas.setEntityClass("npcjuhas");
		juhas.setPosition(4, 3);
		juhas.setDirection(Direction.DOWN);
		juhas.initHP(100);
		zone.add(juhas);
	}
}
