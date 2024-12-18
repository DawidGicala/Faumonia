/* $Id: PokrzywaMakerNPC.java,v 1.21 2020/11/28 08:11:26 davvids Exp $ */
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
package games.stendhal.server.maps.semos.herbal;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.HealerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The healer (original name: Zen). He makes mega potions. 
 */

public class PokrzywaMakerNPC implements ZoneConfigurator {

	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("Tus") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>(); 
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addJob("Od dziecka uczyłem się sporządzać mikstury lecznice z ziół rosnących w naszej krainie."); 
				addReply(Arrays.asList("zioła", "mikstura"),
				"Czytałem wiele tekstów i uczyłem się o produkcji przeróżnych rzeczy. Teraz potrafię sporządzić specjalną magiczną miksturę #'napar z pokrzywy' z zioła pokrzywa. Powiedz tylko #sporządź.");
				addReply("pokrzywa",
		        "Pewnego razu jeden z kupców z odległych krain przyniósł do nas zioło zwane pokrzywa. Okazało się, że pomaga na wiele chorób. W naszej krainie nie występuje naturalnie, powinieneś ich szukać wokół siedlisk.");
				addOffer("Mogę sporządzić #'napar z pokrzywy' dla Ciebie. Do tego będę potrzebował #'pokrzywa'. Powiedz tylko #sporządź.");
				addReply("napar z pokrzywy", "Jest to silny eliksir ziołowy. Jeżeli chcesz to poproś mnie, abym go przyrządził mówiąc #sporządź #1 #napar #z #pokrzywy.");
				addReply("money", "Sporządzanie mikstur traktuję jak swoją pracę więc oczekuje za nią drobnej zapłaty.");
				addHelp("Aktualnie niczego nie potrzebuje.");
				addGoodbye("Trzymaj się.");

				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("money", 10);
				requiredResources.put("pokrzywa", 1);
				final ProducerBehaviour behaviour = new ProducerBehaviour("tus_pokrzywa",
						Arrays.asList("concoct", "sporządź"), "napar z pokrzywy", requiredResources, 1 * 40);

				new ProducerAdder().addProducer(this, behaviour,
				"Witaj, moge wytworzyć dla ciebie eliksiry ziołowe, powiedz tylko #sporządź #1 #napar #z #pokrzywy.");
			}
		};
		npc.setEntityClass("tusnpc");
		npc.setDescription("Oto Tus. Wygląda na osobę doświadczoną w zielarstwie.");
		npc.setPosition(27, 11);
		npc.setDirection(Direction.LEFT);
		npc.initHP(100);
		zone.add(npc);
	}
}
