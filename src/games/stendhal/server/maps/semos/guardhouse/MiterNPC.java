/* $Id: MiterNPC.java,v 1.19 2010/09/19 02:35:36 nhnb Exp $ */
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
package games.stendhal.server.maps.semos.guardhouse;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.HealerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A young lady (original name: Miter) who heals players without charge. 
 */
public class MiterNPC implements ZoneConfigurator {
	
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Miter") {
			@Override
			public void createDialog() {
				addGreeting("Witaj, jestem Miter. Sprzedaje podstawowy ekwipunek aby poznać moją ofertę napisz #oferta. Mogę cie również uleczyć wystarczy, że napiszesz #ulecz. Jesli potrzebujesz w czymś pomocy skorzystaj z chatu globalnego, napisz: #/k #wiadomość");
				addJob("Moja niezwykła moc pomaga mi uleczyć rany. Sprzedaję także lecznicze mikstury i ekwipunek.");
				addHelp("Mogę Cię za darmo uleczyć. Powiedz tylko #ulecz. Moge też sprzedać ci swój stary ekwipunek w tym celu napisz #oferta. Statystyki przedmiotów możesz sprawdzić klikając na nie prawym przyciskiem myszy oraz #zobacz, jeżeli chcesz podnieść jakiś przedmiot z ziemi, #naciśnij #i #przeciągnij #go #do #plecaka po prawej stronie.");
				addEmotionReply("hugs", "hugs");
				addGoodbye();
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(18, 5));
				nodes.add(new Node(18, 8));
				setPath(new FixedPath(nodes, true));
			}
		};
		new SellerAdder().addSeller(npc, new SellerBehaviour(SingletonRepository.getShopList().get("startkasa")));
		new HealerAdder().addHealer(npc, 0);
		npc.setPosition(18, 6);
		npc.setDescription("Widzisz przyjazną Miter. Wygląda ona na kogoś, od kogo możesz coś kupić. Przywitaj się pisząc #cześć");
		npc.setEntityClass("miternpc");
		zone.add(npc);
	}
}
