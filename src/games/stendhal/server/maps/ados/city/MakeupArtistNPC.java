/* $Id: MakeupArtistNPC.java,v 1.25 2011/12/12 20:38:50 nhnb Exp $ */
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
package games.stendhal.server.maps.ados.city;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.OutfitChangerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.OutfitChangerBehaviour;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates the NPCs and portals in Ados City.
 *
 * @author hendrik
 */
public class MakeupArtistNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildFidorea(zone, 20, 13);
	}

	/**
	 * creates Fidorea in the specified zone
	 *
	 * @param zone StendhalRPZone
	 * @param x x
	 * @param y y
	 */
	public void buildFidorea(final StendhalRPZone zone, int x, int y) {
		final SpeakerNPC npc = new SpeakerNPC("Fidorea") {

			@Override
			protected void createPath() {
				// npc does not move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć. Czy potrzebujesz mojej #pomocy?");
				addHelp("Sprzedaję maski. Jeżeli maska Ci się nie podoba to możesz mi #zwrócić, a ja ją wezmę, albo możesz poczekać godzinę, aż zostanie zdjęta.");
				
				// this is a hint that one of the items Anna wants is a dress (goblin dress)
				addQuest("Szukasz zabawek dla Anny? Ona kocha moje kostiumy. Może będzie chciała przymierzyć #ubranie. Jeżeli już masz to sądzę, że poczeka aż nie zrobię nowych kostiumów!");
				addJob("Jestem charakteryzatorką.");
				addReply(
				        Arrays.asList("dress", "ubranie"),
				        "Czytałam historię o goblinach, które noszą koszule jako zbroję! Jeżeli boisz się goblinów tak jak ja to możesz gdzieś kupić koszule. ");
				//addReply("offer", "Normally I sell masks. But I ran out of clothes and cannot by new ones until the cloth seller gets back from his search.");
				addGoodbye("Dowidzenia, zajrzyj jeszcze.");

				final Map<String, Integer> priceList = new HashMap<String, Integer>();
				priceList.put("mask", 20);
				// if you change the wear off time, change her Help message too please
				final OutfitChangerBehaviour behaviour = new OutfitChangerBehaviour(priceList, 5 * 60, "Twoja maska została zdjęta.");
				new OutfitChangerAdder().addOutfitChanger(this, behaviour, Arrays.asList("buy", "kupię"));
			}
		};

		npc.setEntityClass("woman_008_npc");
		npc.setPosition(x, y);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		npc.setDescription("Widzisz piękną kobietę. Jej imię to Fidorea, kocha kolory.");
		zone.add(npc);
	}
}
