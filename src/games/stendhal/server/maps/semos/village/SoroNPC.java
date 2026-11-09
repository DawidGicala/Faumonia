/* $Id: SoroNPC.java,v 1.20 2012/05/21 21:54:46 bluelads99 Exp $ */
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
package games.stendhal.server.maps.semos.village;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

/**
 * An old man (original name: Soro) who stands around and gives directions
 * to newcomers. He's the brother of RetireeNPC (original name: Diogenes).
 *
 * @see games.stendhal.server.maps.quests.KillHigh

 */
public class SoroNPC implements ZoneConfigurator {

	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}
	
	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Soro") {
			@Override
			public void createDialog() {
                addReply("wskazówki", "Ser znajdziesz przy pokonanych szczurach w Semos. Na początek potrzebuję trzech kawałków; możesz przynosić je partiami. Zapytaj o #zadanie, a przypomnę, ile jeszcze brakuje.");
                addReply("tematy", "Możemy wrócić do twojej sprawy albo porozmawiać o czymś innym. Co chcesz wiedzieć?");
				addGreeting("Witaj. Skończył mi się ser, a chętnie wymienię kilka słów z kimś, kto pomoże uzupełnić zapasy. Zapytaj o #zadanie, jeśli masz chwilę.");
				addJob("Niejedną drogę mam już za sobą, ale dziś wolę spokojniejsze życie w Semos. Dobry ser nadal potrafi skusić mnie do rozmowy - zwłaszcza gdy ktoś pomoże uzupełnić zapasy.");
				addOffer("Nie prowadzę sklepu. Mam jednak trochę dawnego ekwipunku, którym podzielę się za pomoc w moim #zadaniu.");
				addGoodbye("Do widzenia. Zajrzyj jeszcze, gdy będziesz przechodzić przez Semos.");
				// All further behaviour is defined in quest classes.
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.LEFT);
			}
			
			
			
		};
		npc.setPosition(54, 43);
		npc.setEntityClass("soronpc");
		npc.setDescription("Widzisz Soro, bywałego mieszkańca Semos. Spogląda z zainteresowaniem na podróżnych, a jeszcze chętniej na ich zapasy sera.");
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}

}