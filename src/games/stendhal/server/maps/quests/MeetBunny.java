/* $Id: MeetBunny.java,v 1.49 2012/04/05 22:41:15 nhnb Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.behaviour.impl.ItemDroppingTeleporterBehaviour;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Meet the Easter Bunny anywhere around the World.
 * <p>
 *
 * PARTICIPANTS:<ul><li> Easter Bunny</ul>
 *
 * STEPS: <ul><li> Find Bunny <li> Say hi <li> Get reward </ul>
 *
 * REWARD: <ul><li> a basket which can be opened to obtain a random good reward: food,
 * money, potions, items, etc...</ul>
 *
 * REPETITIONS: None
 */
public class MeetBunny extends AbstractQuest {
	// quest slot changed ready for 2012
	private static final String QUEST_SLOT = "meet_bunny_13";

	/** the Bunny NPC. */
	protected SpeakerNPC bunny;

	private StendhalRPZone zone;
	
	private ItemDroppingTeleporterBehaviour teleporterBehaviour;
	
	/** the name of the quest */
	public static final String QUEST_NAME = "MeetBunny";

	// The default is 100 (30 seconds) so make ours half this
	private static final int TIME_OUT = 50;

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private SpeakerNPC createbunny() {
		bunny = new SpeakerNPC("Easter Bunny") {
			@Override
			protected void createPath() {
				// npc does not move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				// Greet players who have a basket but go straight back to idle to give others a chance
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(super.getName()),
								new QuestCompletedCondition(QUEST_SLOT)),
						ConversationStates.IDLE,
						"Witaj ponownie! Nie jedz zbyt dużo tych Wielkanocnych!", null);

				final List<ChatAction> reward = new LinkedList<ChatAction>();
				reward.add(new EquipItemAction("koszyk"));
				reward.add(new SetQuestAction(QUEST_SLOT, "done"));

				// Give unmet players a basket
				add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(new GreetingMatchesNameCondition(super.getName()),
							new QuestNotCompletedCondition(QUEST_SLOT)),
					ConversationStates.IDLE,
					"Szczęśliwej Wielkanocy! Mam wielkanocny koszyczek dla Ciebie. Dowidzenia!",
					new MultipleActions(reward));
			}
		};
		
		bunny.setEntityClass("easterbunnynpc");
		bunny.initHP(100);
		// times out twice as fast as normal NPCs
		bunny.setPlayerChatTimeout(TIME_OUT); 
		bunny.setDescription("Oto przyjacielski króliczek niosący kolorowy Wielkanocny koszyczek.");
		// start in int_admin_playground
		zone = SingletonRepository.getRPWorld().getZone("int_admin_playground");
		bunny.setPosition(17, 13);
		zone.add(bunny);
		// Do not add bunny to NPC list until all is known to be OK
		npcs.add(bunny);

		return bunny;
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Spotkanie Króliczka Wielkanocnego",
				"Wielkanoc wkrótce nadejdzie, a paru bohaterów już widziało króliczka skakjącego to tu to tam. Czy będziesz miał na tyle szczęscia, aby go złapać w określonym czasie? Trzymaj się jest bardzo szybki, a także nosi ze sobą niespodzianki...",
				false);
		
		if (System.getProperty("stendhal.easterbunny") != null) {
		    createbunny();
		    teleporterBehaviour = new ItemDroppingTeleporterBehaviour(bunny, "*hop* *hop* *hop* Szczęśliwej Wielkanocy!", true, "małe jajo wielkanocne"); 
		}
	}
		
	/**
	 * removes a quest from the world.
	 *
	 * @return true, if the quest could be removed; false otherwise.
	 */
	@Override
	public boolean removeFromWorld() {
		removeNPC("Easter Bunny");
		// remove the turn notifiers left from the TeleporterBehaviour
		SingletonRepository.getTurnNotifier().dontNotify(teleporterBehaviour);
		return true;
	}
	
	/**
	 * removes an NPC from the world and NPC list
	 *
	 * @param name name of NPC
	 */
	private void removeNPC(String name) {
		SpeakerNPC npc = NPCList.get().get(name);
		if (npc == null) {
			return;
		}
		npc.getZone().remove(npc);
	}

	@Override
	public String getName() {
		return QUEST_NAME;
	}
	
	@Override
	public boolean isVisibleOnQuestStatus() {
		return false;
	}
	
	@Override
	public List<String> getHistory(final Player player) {
		return new ArrayList<String>();
	}
	
	@Override
	public String getNPCName() {
		return "Easter Bunny";
	}

}
