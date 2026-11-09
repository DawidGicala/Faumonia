/* $Id: HealerNPC.java,v 1.19 2020/09/19 02:35:36 Szygolek Exp $ */
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
package games.stendhal.server.maps.ados.mountain;

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


public class SamaronNPC implements ZoneConfigurator {
	
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Samaron") {
			@Override
			public void createDialog() {
                games.stendhal.server.entity.npc.dialogue.JourneyDialogueTopics.install(this);
				addGreeting("Witaj, podróżniku. Jestem Samaron i szukam fragmentów różdżki Mokoszy. Sam nie dotrę do wszystkich miejsc, w których mogą się znajdować; jeśli interesuje cię ta wyprawa, mam dla ciebie #zadanie.");
				addJob("Badam magię i alchemię, ale najbardziej zajmuje mnie teraz różdżka Mokoszy. Jej fragmenty trzeba najpierw odnaleźć, a dopiero później połączyć przy użyciu odpowiednich składników. Sama opowieść o potędze artefaktu nie zastąpi tej pracy.");
				addHelp("Pracuję nad odtworzeniem różdżki Mokoszy. Trzeba odnaleźć trzy części i przygotować składniki do ich połączenia. Jeśli chcesz mi pomóc, zapytaj o #zadanie - wyjaśnię ci kolejność prac.");
				addGoodbye("Powodzenia, niech magia będzie z Tobą. Wracaj, gdy będziesz gotów na kolejne wyzwania.");
			}


		};
		npc.setPosition(112, 45);
		npc.setDescription("Widzisz Samarona, maga badającego części różdżki Mokoszy. Uważnie słucha wieści przynoszonych z dalekich stron.");
		npc.setEntityClass("samaronnpc");
		zone.add(npc);
	}
}
