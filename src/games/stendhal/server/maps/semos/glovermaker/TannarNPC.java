/* $Id: TannarNPC.java,v 1.21 2020/11/28 08:11:26 davvids Exp $ */
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


public class TannarNPC implements ZoneConfigurator {

	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("Tannar") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>(); 
				nodes.add(new Node(16,8));
                nodes.add(new Node(16,15));
				nodes.add(new Node(12,15));
				nodes.add(new Node(12,9));
				nodes.add(new Node(5,9));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addJob("Jestem ekspertem w tworzeniu #rękawic #ze #skóry #jelenia. Ta delikatna, ale wytrzymała skóra nadaje się idealnie do produkcji rękawic o wyjątkowych właściwościach. Każda para jest starannie wykonana, łącząc w sobie tradycję z nowoczesnością."); 
				addReply(Arrays.asList("pieniadze", "money"),
				"Dla zachowania wysokiej jakości moich wyrobów, potrzebuję wynagrodzenia. Oprócz skór, do wykonania par rękawic potrzebuję #1000 money. To pozwala mi na zakup najlepszych materiałów i utrzymanie warsztatu na najwyższym poziomie.");
				addReply("skóra jelenia",
		        "Skóra jelenia jest wyjątkowo delikatna i elastyczna, idealna na rękawice. Używam jej, aby stworzyć wygodne, ale wytrzymałe rękawice, które służą na długie lata. Jeśli dostarczysz mi #4 #skóry #jelenia, z przyjemnością wykonam dla Ciebie parę #rękawic #ze #skóry #jelenia");
				addReply("skóra zwierzęca",
		        "Skóre zwierzecą możesz pozyskać zabijajać różne zwierzęta w naszje krainie. Dzięki niej moge zadbać o wzmocnienia szwów i zwiększenia trwałości skórzanych rękawic.");
				addOffer("Mogę stworzyć dla Ciebie #rękawice #ze #skóry #lwa. Potrzebne mi do tego #1000 monet, #4 skóry lwa oraz #8 skór zwierzęcych. To nie tylko rękawice, ale dzieło sztuki, odzwierciedlające twoją siłę i status. Jeżeli chcesz to poproś mnie, abym go przyrządził mówiąc #wykonaj #rękawice #ze #skóry #lwa.");
				addHelp("Dziękuję, ale aktualnie nie potrzebuję pomocy. Moja praca to tworzenie rękawic ze skóry lwa. Jeśli potrzebujesz parę, powiedz #wykonaj #rękawice #ze #skóry #lwa.");
				addGoodbye("Trzymaj się.");

				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("money", 1000);
				requiredResources.put("skóra jelenia", 4);
				requiredResources.put("skóra zwierzęca", 8);
				final ProducerBehaviour behaviour = new ProducerBehaviour("tannar_gloves",
						Arrays.asList("concoct", "wykonaj"), "rękawice ze skóry jelenia", requiredResources, 30 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				"Witaj w moim warsztacie. Jestem Tannar, specjalista od #rękawic #ze #skóry #jelenia. Jeśli chcesz, abym wykonał dla Ciebie wyjątkową parę, powiedz #wykonaj #rękawice #ze #skóry #jelenia.");
			}
		};
		npc.setEntityClass("ylriknpc");
		npc.setDescription("Tannar to spokojny i skupiony rzemieślnik, którego warsztat odzwierciedla jego miłość do natury i rzemiosła. Jego rękawice ze skóry jelenia są znane z wyjątkowej jakości i piękna, a jego umiejętności są cenione wśród mieszkańców i podróżników.");
		npc.setPosition(5, 8);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		zone.add(npc);
	}
}
