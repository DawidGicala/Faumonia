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
				addGreeting("Witaj, jestem Miter. Pomogę ci przygotować się do pierwszej wyprawy: pokażę podstawowy ekwipunek i opatrzę rany. Wybierz #oferta, aby zajrzeć do sklepu, albo #ulecz, jeśli potrzebujesz pomocy.");
				addJob("Pomagam początkującym przygotować się do drogi i opatruję ich rany. Sprzedaję podstawowy ekwipunek, a zbędne łupy możesz oddać mi na #skup. Zostaw sobie jedzenie i rzeczy potrzebne w zadaniach.");
				addHelp("Leczę bez opłat - wybierz #ulecz. W sklepie sprawdź cenę i ilość, a potem potwierdź zakup; kupione rzeczy trafiają do plecaka po prawej. Przedmiot z ziemi przeciągnij do plecaka, a gdy utkniesz, poproś graczy o pomoc przez /k i swoją wiadomość.");
				addEmotionReply("hugs", "hugs");
				addGoodbye("Do zobaczenia. Wróć, jeśli zabraknie ci zapasów albo trzeba będzie opatrzyć rany.");
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
		new games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder().addBuyer(npc,
                new games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour(SingletonRepository.getShopList().get("miterbuyback")), false);
        npc.addReply("skup", "Odkupuję prosty ekwipunek znaleziony przy szczurach i gnomach: maczugi, sztyleciki, peleryny, skórzane zbroje, spodnie, hełmy i buty, a także puklerze, drewniane i ćwiekowe tarcze oraz hełmy nabijane ćwiekami. Powiedz sprzedam i nazwę przedmiotu, a przed zatwierdzeniem podam cenę. Zachowaj jedzenie i rzeczy potrzebne do bieżących zadań.");
        new HealerAdder().addHealer(npc, 0);
		npc.setPosition(18, 6);
		npc.setDescription("Widzisz Mitera, który pomaga początkującym wojownikom. Ma pod ręką podstawowy ekwipunek i środki do opatrywania ran.");
		npc.setEntityClass("miternpc");
		zone.add(npc);
	}
}
