/* $Id: KidGhostNPC.java,v 1.34 2011/05/01 19:50:07 martinfuchs Exp $ */
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

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a Ghost NPC.
 *
 * @author kymara
 */
public class KidGhostNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone, attributes);
	}
	private void buildNPC(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC ghost = new SpeakerNPC("Ben") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(34, 121));
				nodes.add(new Node(24, 121));
				nodes.add(new Node(24, 112));
				nodes.add(new Node(13, 112));
				nodes.add(new Node(13, 121));
				nodes.add(new Node(6, 121));
				nodes.add(new Node(6, 112));
				nodes.add(new Node(13, 112));
				nodes.add(new Node(13, 121));
				nodes.add(new Node(24, 121));
				nodes.add(new Node(24, 112));
				nodes.add(new Node(34, 112));
				setPath(new FixedPath(nodes, true));
			}

			@Override
		    protected void createDialog() {
			    add(ConversationStates.IDLE,
			    	ConversationPhrases.GREETING_MESSAGES,
			    	new GreetingMatchesNameCondition(getName()), true,
			    	ConversationStates.IDLE,
			    	null,
			    	new ChatAction() {
			    		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			    			if (!player.hasQuest("find_ghosts")) {
			    				player.setQuest("find_ghosts", "looking:said");
			    			}
			    			final String npcQuestText = player.getQuest("find_ghosts");
			    			final String[] npcDoneText = npcQuestText.split(":");
			    			final String lookStr;
							if (npcDoneText.length > 1) {
								lookStr = npcDoneText[0];
							} else {
								lookStr = "";
							}
			    			final String saidStr;
							if (npcDoneText.length > 1) {
								saidStr = npcDoneText[1];
							} else {
								saidStr = "";
							}
			    			final List<String> list = Arrays.asList(lookStr.split(";"));
						    if (list.contains(npc.getName()) || player.isQuestCompleted("find_ghosts")) {
								npc.say("Cześć. Cieszę się, że mnie pamiętasz. Przechadzam się tędy i czekam, aż ktoś się ze mną pobawi.");
							} else {
							    player.setQuest("find_ghosts", lookStr
									    + ";" + npc.getName()
									    + ":" + saidStr);
								npc.say("Cześć! Każdemu jest ciężko ze mną rozmawiać. Inne dzieci udają, że nie istnieję. Mam nadzieję, że mnie pamiętasz.");
							    player.addXP(100);
							    player.addKarma(10);
							}
						}
					});
			}
		};

		ghost.setDescription("Oto duch małego chłopca.");
		ghost.setResistance(0);
		ghost.setEntityClass("kid7npc");
		// He is a ghost so he is see through
		ghost.setVisibility(50);
		ghost.setPosition(34, 121);
		// He has low HP
		ghost.initHP(30);
		ghost.setBaseHP(100);
		zone.add(ghost);
	}
}
