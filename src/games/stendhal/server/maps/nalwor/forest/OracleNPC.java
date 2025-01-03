/***************************************************************************
 *                   (C) Copyright 2003-2012 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.nalwor.forest;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayNPCNamesForUnstartedQuestsAction;
import games.stendhal.server.entity.npc.action.SayUnstartedQuestDescriptionFromNPCNameAction;
import games.stendhal.server.entity.npc.condition.TriggerIsNPCNameForUnstartedQuestCondition;
import games.stendhal.server.maps.Region;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * An oracle who lets players know how they can help others.
 */
public class OracleNPC implements ZoneConfigurator {
	
	/** 
	 * region that this NPC can give information about 
	 */
	private final List<String> regions = Arrays.asList(Region.NALWOR_CITY, Region.ORRIL_DUNGEONS, Region.HELL);

	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Zinnia") {
			@Override
			public void createDialog() {
				addGreeting("Cześć. Lepiej szepczmy, aby nie zwrócić na siebie uwagi elfów.");
				
				// use a standard action to list the names of NPCs for quests which haven't been started in this region 
				addReply(ConversationPhrases.HELP_MESSAGES, null, new SayNPCNamesForUnstartedQuestsAction(regions));
			    
				// if the player says an NPC name, describe the quest (same description as in the travel log)
			    add(ConversationStates.ATTENDING,
						"",
						new TriggerIsNPCNameForUnstartedQuestCondition(regions),
						ConversationStates.ATTENDING,
						null,
						new SayUnstartedQuestDescriptionFromNPCNameAction(regions));
				addQuest("W pobliżu są mieszkańcy w " + Grammar.enumerateCollection(regions) + " którzy mogą potrzebować twojej #pomocy.");
				addJob("Rozglądałam się tutaj. Czuć tutaj magię.");
				addOffer("Tak jak moje #siostry mogę ci #pomóc w #pomocy innym.");
				addReply(Arrays.asList("sisters", "siostry"), "Moje siostry mieszkają daleko. Znajdź je i naucz się #pomagać tym w ich rejonie. Jak ja mają #imona kwiatów.");
				addReply(Arrays.asList("name", "imiona"), "Zinnia jest kwiatkiem, którym może stać się zielonym szmaragdem jak moja sukienka. Sądzę, że dlatego tak lubie zielone lasy.");
				
				// just to be nice :)
				addEmotionReply("hugs", "hugs");
				addGoodbye("Dziękuję i pamiętaj, aby poruszać się ostrożnie po magicznym lesie.");
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(75, 117));
				nodes.add(new Node(75, 123));
				nodes.add(new Node(82, 123));
				nodes.add(new Node(82, 120));
				nodes.add(new Node(86, 120));
				nodes.add(new Node(86, 123));
				nodes.add(new Node(92, 123));
				nodes.add(new Node(92, 116));
				nodes.add(new Node(90, 116));
				nodes.add(new Node(90, 121));
				nodes.add(new Node(79, 121));
				nodes.add(new Node(79, 118));
				nodes.add(new Node(75, 118));
				setPath(new FixedPath(nodes, true));
			}
		};
		npc.setPosition(75, 117);
		npc.setDescription("Oto Zinnia. Wygląda jakoś wyjątkowo.");
		npc.setEntityClass("oracle3npc");
		zone.add(npc);
	}

}