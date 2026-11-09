/* $Id: SedroNPC.java,v 1.20 2020/11/01 15:31:46 bluelads99 Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2020 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.semos.village;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

/**
 * An old man (original name: Sedro) who stands around and gives directions
 * to newcomers. He's the brother of RetireeNPC (original name: Diogenes).
 *
 * @see games.stendhal.server.maps.quests.KillHigh

 */
public class SedroNPC implements ZoneConfigurator {

	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}
	
	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Sedro") {
			@Override
			public void createDialog() {
				addReply("tematy", "Dobrze, posiedźmy jeszcze chwilę. Możemy wrócić do sprawy potworów albo po prostu zamienić kilka słów.");
				addGreeting("Witaj. Szukam kogoś, kto pomoże mi z potworami w okolicy. Jeśli masz chwilę, zapytaj mnie o #zadanie.");
				addJob("Żyję tu na uboczu i przywykłem do własnego towarzystwa. Nie znaczy to jednak, że nie obchodzą mnie sprawy okolicy. Dobrze czasem zamienić kilka słów z wędrowcem i dowiedzieć się, co dzieje się poza znajomą drogą.");
				addOffer("Nie prowadzę sklepu i nie handluję. Mogę za to podzielić się tym, co wiem o okolicy, a za pomoc z potworami mam przygotowany ekwipunek.");
				addGoodbye("Do widzenia i dziękuję za rozmowę.");
				// All further behaviour is defined in quest classes.
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.LEFT);
			}
			
			
			
		};
		npc.setPosition(20, 39);
		npc.setEntityClass("Sedronpc");
		npc.setDescription("Widzisz Sedro, wiekowego samotnika z Semos. Mówi spokojnie i z uwagą słucha przechodzących wędrowców.");
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}

}
