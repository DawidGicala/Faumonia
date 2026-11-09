/* $Id: BlacksmithNPC.java,v 1.26 2013/04/26 21:36:51 kiheru Exp $ */
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
package games.stendhal.server.maps.semos.blacksmith;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The blacksmith (original name: Xoderos). Brother of the goldsmith in Ados.
 * He refuses to sell weapons, but he casts iron for the player, and he sells
 * tools.
 * 
 * @author daniel
 * 
 * @see games.stendhal.server.maps.quests.HungryJoshua
 */
public class BlacksmithNPC implements ZoneConfigurator  {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Xoderos") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
                nodes.add(new Node(23,12));
                nodes.add(new Node(29,12));
                nodes.add(new Node(29,5));
                nodes.add(new Node(17,5));
                nodes.add(new Node(17,9));
                nodes.add(new Node(28,9));
                nodes.add(new Node(28,12));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
                addReply("tematy", "Mogę opowiedzieć o mojej pracy, odlać żelazo lub pokazać narzędzia. Jest też sprawa mojego brata Joshui, jeśli o niego pytasz.");
				addReply("polano",
				"Potrzebuję drewna do utrzymania ognia w palenisku. Znajdziesz wiele leżących kłód w lasach dookoła miasta.");

				addReply(Arrays.asList("ore", "iron", "iron ore", "ruda żelaza", "ruda", "żelaza", "rudę żelaza"),
				"Rudę żelaza znajdziesz w górach na zachód od Orril nieopodal kopalni krasnoludów. Uważaj tam na siebie!");

				addReply(Arrays.asList("gold pan", "misa do pukania złota", "misa"),
				"Mając misę będziesz mógł wypłukać złoto z potoków. Przy brzegach rzeki Orril, na południe od zamku nieopodal wodospadów jest jezioro. Niegdyś sam znalazłem tam #'bryłkę złota'. Może i Tobie się poszczęści.");

				addReply(Arrays.asList("gold nugget", "bryłkę złota"),
				"Specjalistą od tego jest mój brat Joshua, który mieszka w Ados. Potrafi wytopić z samorodków drogocenne sztabki czystego złota.");

				addReply(Arrays.asList("bobbin", "szpulka do maszyny", "szpulka"),"#Handluję narzędziami, ale nie mam szpulek. Są dla mnie zbyt skomplikowane do zrobienia. Spróbuj u krasnali.");
				addReply(Arrays.asList("oil", "can of oil", "olejek"),"Rybacy nas w to zaopatrują.");

				addHelp("Na jedną sztabę żelaza potrzebuję jednego polana i jednej rudy żelaza. Przynieś oba materiały, powiedz #odlej i daj mi pięć minut na pracę. Drewno zbierzesz w okolicznych lasach, a rudy szukaj w górach na zachód od Orril, przy kopalni krasnoludów.");
				addJob("Jestem kowalem i przy dobrej robocie nie uznaję drogi na skróty. Z powodu wojny broń mogę sprzedawać tylko wojskowym, ale dla podróżnych mam narzędzia i odlewam żelazo. Zapytaj o #ofertę albo powiedz #odlej.");
				addGoodbye("Do widzenia. Niech narzędzia dobrze ci służą.");
				new SellerAdder().addSeller(this, new SellerBehaviour(SingletonRepository.getShopList().get("selltools")));

		// Xoderos casts iron if you bring him wood and iron ore.
		final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();	
		requiredResources.put("polano", 1);
		requiredResources.put("ruda żelaza", 1);

		final ProducerBehaviour behaviour = new ProducerBehaviour("xoderos_cast_iron",
				 Arrays.asList("cast", "odlej"), "żelazo", requiredResources, 5 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				"Witaj w kuźni. Broń jest teraz zarezerwowana dla wojskowych, ale mogę pokazać narzędzia z mojej #oferty albo odlać żelazo z twoich materiałów. Jeśli chodzi o odlew, powiedz #odlej.");


			}};
			npc.setPosition(23, 12);
			npc.setEntityClass("blacksmithnpc");
			npc.setDescription("Widzisz Xoderosa, krzepkiego kowala z Semos. Nawet podczas rozmowy zerka na palenisko, pilnując żaru.");
			zone.add(npc);
	}
}
