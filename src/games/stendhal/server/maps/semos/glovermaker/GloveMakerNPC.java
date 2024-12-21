/* $Id: GloveMakerNPC.java,v 1.21 2020/11/28 08:11:26 davvids Exp $ */
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
package games.stendhal.server.maps.semos.glovermaker;

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


public class GloveMakerNPC implements ZoneConfigurator {

	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("Glover") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>(); 
				nodes.add(new Node(3,16));
                nodes.add(new Node(3,9));
				nodes.add(new Node(5,9));
				nodes.add(new Node(5,16));
				nodes.add(new Node(5,8));
				nodes.add(new Node(7,8));
				nodes.add(new Node(7,16));
				nodes.add(new Node(7,5));
				nodes.add(new Node(3,5));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addJob("Specjalizuję się w rzemiośle #rękawic #ze #skóry #lwa. To sztuka łącząca tradycję z nowoczesnymi technikami. Każda para rękawic jest unikalna i wykonana z największą starannością."); 
				addReply(Arrays.asList("pieniadze", "money"),
				"Pieniądze to wynagrodzenie za moją pracę i czas. Oprócz #skór potrzebuję #1000 monet, aby pokryć koszty materiałów i utrzymania warsztatu. Wysoka jakość wymaga odpowiedniego wynagrodzenia");
				addReply("skóra lwa",
		        "Skóra lwa to materiał pełen majestatu i siły. Wykorzystuję ją do tworzenia rękawic, które są zarówno trwałe, jak i prestiżowe. Jeśli chcesz to wykonam dla Ciebie parę #rękawic #ze #skóry #lwa");
				addReply("skóra zwierzęca",
		        "Skóre zwierzecą możesz pozyskać zabijajać różne zwierzęta w naszje krainie. Dzięki niej moge zadbać o wzmocnienia szwów i zwiększenia trwałości skórzanych rękawic.");
				addOffer("Mogę stworzyć dla Ciebie #rękawice #ze #skóry #lwa. Potrzebne mi do tego #1000 monet, #4 skóry lwa oraz #8 skór zwierzęcych. To nie tylko rękawice, ale dzieło sztuki, odzwierciedlające twoją siłę i status. Jeżeli chcesz to poproś mnie, abym go przyrządził mówiąc #wykonaj #rękawice #ze #skóry #lwa.");
				addHelp("Dziękuję, ale aktualnie nie potrzebuję pomocy. Moja praca to tworzenie rękawic ze skóry lwa. Jeśli potrzebujesz parę, powiedz #wykonaj #rękawice #ze #skóry #lwa.");
				addGoodbye("Trzymaj się.");

				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("money", 1000);
				requiredResources.put("skóra lwa", 4);
				requiredResources.put("skóra zwierzęca", 8);
				final ProducerBehaviour behaviour = new ProducerBehaviour("glover_gloves",
						Arrays.asList("concoct", "wykonaj"), "rękawice ze skóry lwa", requiredResources, 30 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				"Witaj w moim warsztacie. Jestem Glover, mistrz w tworzeniu #rękawic #ze #skóry #lwa. Jeśli chcesz, abym wykonał dla Ciebie parę, powiedz #wykonaj #rękawice #ze #skóry #lwa.");
			}
		};
		npc.setEntityClass("wellroundedguynpc");
		npc.setDescription("Glover to rzemieślnik o głębokim spojrzeniu i pewnych ruchach, co świadczy o latach doświadczenia. Jego warsztat jest pełen skór i narzędzi potrzebnych do tworzenia wysokiej jakości rękawic. Jego prace są cenione przez wojowników i znawców elegancji.");
		npc.setPosition(3, 8);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		zone.add(npc);
	}
}
