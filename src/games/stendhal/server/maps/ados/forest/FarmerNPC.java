/* $Id: FarmerNPC.java,v 1.5 2010/12/29 23:39:13 martinfuchs Exp $ */
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
package games.stendhal.server.maps.ados.forest;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds Karl, the farmer NPC.
 * He gives horse hairs needed for the BowsForOuchit quest
 * He gives help to newcomers about the area
 * He suggests you can buy milk from his wife Philomena
 * @author kymara
 */
public class FarmerNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildFarmer(zone);
	}

	private void buildFarmer(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Karl") {

			/* 
			 * Karl walks around near the red barn and along the path some way.
			 */
			
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(64, 75));
				nodes.add(new Node(64, 86));	
				nodes.add(new Node(68, 86));	
				nodes.add(new Node(68, 84));
				nodes.add(new Node(76, 84));
				nodes.add(new Node(68, 84));
				nodes.add(new Node(68, 86));
				nodes.add(new Node(60, 86));
				nodes.add(new Node(60, 89));
				nodes.add(new Node(60, 86));
				nodes.add(new Node(64, 86));
				setPath(new FixedPath(nodes, true));
			}
			
			@Override
			protected void createDialog() {
				addReply("tematy", "Mów śmiało, tylko konkretnie. Robota sama się nie zrobi, ale na kilka słów znajdę czas.");
				addGreeting("Witaj w gospodarstwie. Jeśli czegoś potrzebujesz, mów, póki jestem w pobliżu.");
				addJob("Prowadzę to gospodarstwo i pilnuję, żeby zwierzętom niczego nie brakowało. Pracy jest tyle, że człowiek ledwie obejrzy się za siebie, a już trzeba zaczynać następną. Nie narzekam, ale rąk do roboty nigdy za wiele.");
                addOffer("Moja żona, #Philomena, zna się na mleku najlepiej. Ja sprzedaję #'puste worki'. W gospodarstwie zawsze warto mieć pod ręką coś, w co można spakować zapasy.");
				addReply("Philomena","Philomena jest w domku na południowy zachód stąd. Zajrzyj do niej, jeśli szukasz mleka; ja zajmuję się tutaj gospodarstwem.");
                addHelp("Znam okolicę z codziennej pracy, więc mogę ci wskazać drogę. Opowiem ci o #sąsiedztwie, jeśli nie wiesz, którędy ruszyć. Tylko nie każ mi przy tym porzucać gospodarstwa.");
				addReply(Arrays.asList("neighborhood.", "sąsiedztwie."),"Na północy znajduje się jaskinia z niedźwiedziami i innymi potworami. Jeżeli pójdziesz na północny-wschód " +
						"to po pewnym czasie dojdziesz do dużego miasta Ados. Na wschodzie jest duuuuża skała. Balduin " +
						"wciąż tam mieszka? Chcesz wyruszyć na południowy-wschód? Cóż.. możesz tamtędy dojść do Ados, ale " +
						"droga jest trochę trudniejsza.");
				addQuest("Teraz nie mam dla ciebie osobnego zlecenia. Muszę doglądać gospodarstwa, ale jeśli ktoś cię do mnie przysłał, powiedz kto.");
				addReply(Arrays.asList("empty sack", "puste worki"),"Mam na sprzedaż #'pusty worek' i mogę odstąpić ci więcej niż jeden. Wybierz, ile potrzebujesz, a podam cenę.");
                final Map<String, Integer> offerings = new HashMap<String, Integer>();
                offerings.put("pusty worek", 10);
                addReply(java.util.Arrays.asList("sklep", "shop"), "Sprzedaję puste worki. Przydają się do noszenia zapasów; wybierz, ile chcesz kupić.");
                new SellerAdder().addSeller(this, new SellerBehaviour(offerings), false);
				addGoodbye("Do zobaczenia. Uważaj po drodze, ja wracam do pracy.");
			}
		};

		npc.setDescription("Widzisz Karla, starszego rolnika. Jego spracowane dłonie i oszczędne słowa zdradzają człowieka przywykłego do ciężkiej pracy.");
		npc.setEntityClass("beardmannpc");
		npc.setPosition(64, 75);
		npc.initHP(100);
		zone.add(npc);
	}
}
