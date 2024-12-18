/* $Id: HalvornNPC.java,v 1.20 2024/12/04 18:54:46 davvids Exp $ */
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
package games.stendhal.server.maps.semos.city;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

/**
 * An old man (original name: Halvorn) who stands around and gives directions
 * to newcomers. He's the brother of RetireeNPC (original name: Diogenes).
 *
 * @see games.stendhal.server.maps.quests.KillHigh

 */
public class HalvornNPC implements ZoneConfigurator {

	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}
	
	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Halvorn") {
			@Override
			public void createDialog() {
				addGreeting("Ah, witaj, wędrowcze. Czujesz to? Lodowy wiatr niesie opowieści o zimie, jakiej Semos dawno nie widziało. Śnieg spowija każdy zakątek, a ja przypominam sobie legendy, które dawno uznano za bajki.");
				addJob("W tych mroźnych czasach to nie tylko śnieg przyniósł zmiany. Lodowe potwory coraz śmielej wychodzą ze swoich kryjówek. Giganty i golemy, a nawet smoki arktyczne – zimowe istoty powracają, jakby strzegły czegoś cennego. Mówią, że to one wiedzą, gdzie ukryty jest Smok Wawelski. Jeśli masz odwagę, możesz zmierzyć się z nimi... ale pamiętaj, to próba dla najodważniejszych.");
				addGoodbye("Niech twoja siła będzie jak wieczny lód, wędrowcze. Uważaj na siebie w tych mroźnych krainach – zima w Faumonii nie wybacza słabości.");
				// All further behaviour is defined in quest classes.
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.LEFT);
			}
			
			
			
		};
		npc.setPosition(78, 26);
		npc.setEntityClass("npcmagik");
		npc.setDescription("Halvorn to stary wędrowiec, który zasiada na ławce przed bankiem. W oczach Halvorna widać wiekową mądrość, ale i troskę – jakby zimowy krajobraz przypominał mu coś o wiele starszego niż współczesny świat Faumonii.");
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}

}