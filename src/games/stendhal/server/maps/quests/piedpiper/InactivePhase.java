/* $Id: InactivePhase.java,v 1.6 2012/03/26 20:12:21 nhnb Exp $ */
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
package games.stendhal.server.maps.quests.piedpiper;

import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class InactivePhase extends TPPQuest {
	
	private final int minPhaseChangeTime;
	private final int maxPhaseChangeTime;
	
	private void addConversations(final SpeakerNPC mainNPC) {
		TPP_Phase myphase = INACTIVE;
		
		// Player asking about rats
		mainNPC.add(
				ConversationStates.ATTENDING, 
				Arrays.asList("rats", "rats!", "szczurów", "szczurów!"),
				new TPPQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING, 
				"Ados nie jest teraz nawiedzane przez plagę szczurów. Wciąż możesz "+
				  "odebrać #nagrodę za ostatnią pomoc. Możesz zapytać o #szczegóły "+
				  "jeśli chcesz.", 
				null);
		
		// Player asking about details
		mainNPC.add(
				ConversationStates.ATTENDING, 
				Arrays.asList("details", "szczegóły"),
				new TPPQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING, 
				null, 
				new DetailsKillingsAction());
		
		// Player asked about reward
		mainNPC.add(
				ConversationStates.ATTENDING, 
				Arrays.asList("reward", "nagroda", "nagrodę"),
				new TPPQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING, 
				null, 
				new RewardPlayerAction());
	}
	
	/**
	 * constructor
	 * @param timings
	 */
	public InactivePhase(Map<String, Integer> timings) {
		super(timings);
		minPhaseChangeTime=timings.get(INACTIVE_TIME_MIN);
		maxPhaseChangeTime=timings.get(INACTIVE_TIME_MAX);
		addConversations(TPPQuestHelperFunctions.getMainNPC());
	}


	@Override
	public int getMinTimeOut() {
		return minPhaseChangeTime;
	}
	
	@Override
	public int getMaxTimeOut() {
		return maxPhaseChangeTime;
	}
	
	@Override
	public void phaseToDefaultPhase(List<String> comments) {
		// not used
	}

	@Override
	public void prepare() {

	}
	

	@Override
	public TPP_Phase getPhase() {
		return TPP_Phase.TPP_INACTIVE;
	}
	
}
