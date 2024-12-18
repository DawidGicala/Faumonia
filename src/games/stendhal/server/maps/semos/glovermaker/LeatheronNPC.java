/* $Id: LeatheronNPC.java,v 1.21 2020/11/28 08:11:26 davvids Exp $ */
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


public class LeatheronNPC implements ZoneConfigurator {

	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("Leatheron") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>(); 
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addJob("Specjalizuję się w sztuce tworzenia #rękawic #ze #skóry #tygrysa. To połączenie tradycyjnego rzemiosła z nowoczesnymi technikami. Każda para jest unikatowa, wykonana z najwyższą dbałością o detale."); 
				addReply(Arrays.asList("pieniadze", "money"),
				"Za moje usługi i czas pobieram wynagrodzenie. Poza #skórami potrzebuję #1000 #money, aby pokryć koszty materiałów i utrzymania warsztatu. Jakość, którą oferuję, wymaga adekwatnego wynagrodzenia.");
				addReply("skóra tygrysa",
		        "Skóra tygrysa to niezwykle wytrzymały i szlachetny materiał. Używam jej do tworzenia ekskluzywnych rękawic. Jeśli jesteś zainteresowany, wykonam dla Ciebie parę #rękawic #ze #skóry #tygrysa");
				addReply("skóra zwierzęca",
		        "Skóra zwierzęca, którą pozyskujemy z naszej krainy, jest niezbędna do wzmocnienia rękawic i zapewnienia im dodatkowej trwałości. Używam różnych rodzajów skór, aby nadać każdej parze rękawic unikalny charakter.");
				addOffer("Mogę stworzyć dla Ciebie rękawice ze skóry jelenia to moje specjalne dzieło. Do ich wykonania potrzebuję #1000 money, #4 #skóry #jelenia oraz #8 #skór #zwierzęcych. Są nie tylko praktyczne, ale i piękne, idealne dla koneserów elegancji. Powiedz #wykonaj #rękawice #ze #skóry #jelenia, a zacznę pracę..");
				addHelp("Obecnie nie potrzebuję dodatkowej pomocy, ale dziękuję za zainteresowanie. Skupiam się na tworzeniu rękawic ze skóry tygrysa. Jeśli chcesz parę, powiedz #wykonaj #rękawice #ze #skóry #tygrysa.");
				addGoodbye("Trzymaj się.");

				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("money", 1000);
				requiredResources.put("skóra tygrysa", 4);
				requiredResources.put("skóra zwierzęca", 8);
				final ProducerBehaviour behaviour = new ProducerBehaviour("leatheron_gloves",
						Arrays.asList("concoct", "wykonaj"), "rękawice ze skóry tygrysa", requiredResources, 30 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				"Witaj w moim warsztacie. Nazywam się Leatheron i jestem mistrzem tworzenia #rękawic #ze #skóry #tygrysa. Jeśli chcesz, abym wykonał dla Ciebie parę, powiedz #wykonaj #rękawice #ze #skóry #tygrysa.");
			}
		};
		npc.setEntityClass("weaponsellernpc");
		npc.setDescription("Leatheron to rzemieślnik o wyrafinowanym spojrzeniu i precyzyjnych ruchach, co świadczy o jego latach doświadczenia. Jego warsztat jest wypełniony skórami i specjalistycznymi narzędziami, niezbędnymi do produkcji rękawic najwyższej klasy. Jego prace są cenione przez miłośników wyrafinowanego stylu i jakości.");
		npc.setPosition(15, 5);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		zone.add(npc);
	}
}
