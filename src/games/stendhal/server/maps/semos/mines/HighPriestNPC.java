/* $Id: HighPriestNPC.java,v 1.23 2011/09/08 18:42:37 bluelads99 Exp $ */
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
package games.stendhal.server.maps.semos.mines;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

import java.util.Map;

public class HighPriestNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildMineArea(zone, attributes);
	}

	private void buildMineArea(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		final SpeakerNPC npc = new SpeakerNPC("Aenihata") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting(null, new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						String reply = "Utrzymuję barierę, która trzyma potwora zwanego #balrog z daleka.";

						if (player.getLevel() < 300) {
							reply += " Balrog Cię zabije. Uciekaj!";
						} else {
							reply += " Będę trzymał barierę, aby chronić Faumonii. Zabij to.";
						}
						raiser.say(reply);
					}
				});

				addReply("balrog",
						"Najstraszniejszy potwór w armii Balrogów.");
				addGoodbye();
			}
			
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.LEFT);
			}
		};


		npc.addInitChatMessage(null, new ChatAction() {
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (!player.hasQuest("AenihataReward")
						&& (player.getLevel() >= 300)) {
					player.setQuest("AenihataReward", "done");

					player.setAtkXP(50 + player.getAtkXP());
					player.setDefXP(100 + player.getDefXP());
					player.addXP(1000);
					player.incAtkXP();
					player.incDefXP();
				}

				if (!player.hasQuest("AenihataFirstChat")) {
					player.setQuest("AenihataFirstChat", "done");
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
				
			}
			
		});

		npc.setEntityClass("highpriestnpc");
		npc.setDescription("Oto Aenihata. Jest Wysokim Kapłanem, który próbuje chronić Faumonię swoimi umiejętnościami magicznymi.");
		npc.setPosition(23, 44);
		npc.setDirection(Direction.LEFT);
		npc.initHP(85);
		zone.add(npc);
	}
}
