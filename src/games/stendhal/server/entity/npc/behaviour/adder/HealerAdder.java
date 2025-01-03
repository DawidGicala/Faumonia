/* $Id: HealerAdder.java,v 1.24 2012/08/23 20:48:57 yoriy Exp $ */
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
package games.stendhal.server.entity.npc.behaviour.adder;

import games.stendhal.common.MathHelper;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.HealerBehaviour;
import games.stendhal.server.entity.npc.behaviour.journal.ServicersRegister;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;

public class HealerAdder {

    private final ServicersRegister servicersRegister = SingletonRepository.getServicersRegister();
    
	/**
	 * Behaviour parse result in the current conversation.
	 * Remark: There is only one conversation between a player and the NPC at any time.
	 */
	private ItemParserResult currentBehavRes;

	/**
	 *<p>Makes this NPC a healer, i.e. someone who sets the player's hp to
	 * the value of their base hp.
	 *
	 *<p>Player killers are not healed at all even by healers who charge.
	 *
	 *<p>Too strong players (atk >35 or def > 35) cannot be healed for free.
	 *
	 *<p>Players who have done PVP in the last 2 hours cannot be healed free,
	 * unless they are very new to the game.
	 * 
	 * @param npc
	 *            SpeakerNPC
	 * @param cost
	 *            The price which can be positive for a lump sum cost, 0 for free healing
	 *            or negative for a price dependent on level of player.
	 */
	public void addHealer(final SpeakerNPC npc, final int cost) {
		final HealerBehaviour healerBehaviour = new HealerBehaviour(cost);
		servicersRegister.add(npc.getName(), healerBehaviour);
		
		final Engine engine = npc.getEngine();

		engine.add(ConversationStates.ATTENDING,
				ConversationPhrases.OFFER_MESSAGES, null,
				false, ConversationStates.ATTENDING, "Mogę Cię uleczyć. Powiedz tylko #ulecz.", null);

		engine.add(ConversationStates.ATTENDING, Arrays.asList("heal", "ulecz", "wylecz"), null,
				false, ConversationStates.ATTENDING,
				null, new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						ItemParserResult res = new ItemParserResult(true, "heal", 1, null);

						int cost = healerBehaviour.getCharge(res, player);
						currentBehavRes = res;
						
						String badboymsg = "";
						if (player.isBadBoy()) {
							cost = cost * 2;
							currentBehavRes.setAmount(2);
							badboymsg = " Leczenie tych co zabili innych kosztuję więcej.";
						}

						if (cost > 0) {
							raiser.say("Leczenie kosztuje " + cost
									+ "." + badboymsg + " Posiadasz tyle?");

							raiser.setCurrentState(ConversationStates.HEAL_OFFERED); // success
						} else if (cost < 0) {
							// price depends on level if cost was set at -1
							// where the factor is |cost| and we have a +1
							// to avoid 0 charge.
							cost = player.getLevel() * Math.abs(cost) + 1;
							raiser.say("Uleczenie kogoś z Twoimi zdolnościami kosztuje "
									+ cost
									+ " money. Posiadasz tyle?");

							raiser.setCurrentState(ConversationStates.HEAL_OFFERED); // success
						} else {
							if ((player.getAtk() > 35) || (player.getDef() > 35)) {
									raiser.say("Przepraszam, ale nie mogę Cię uleczyć ponieważ jesteś zbyt potężny jak na moje możliwości");
							} else if ((!player.isNew()
									&& (player.getLastPVPActionTime() > System
											.currentTimeMillis()
											- 2
											* MathHelper.MILLISECONDS_IN_ONE_HOUR) || player.isBadBoy())) {
								// ignore the PVP flag for very young
								// characters
								// (low atk, low def AND low level)
								raiser.say("Przepraszam, ale posiadasz złą aurę i teraz nie mogę Ciebie uleczyć.");
							} else {
								raiser.say("Zostałeś uleczony. W czym jeszcze mogę pomóc?");
								healerBehaviour.heal(player);
							}
						}

					}
				});

		engine.add(ConversationStates.HEAL_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				false, ConversationStates.ATTENDING,
				null, new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						int cost = healerBehaviour.getCharge(currentBehavRes, player);
						if (cost < 0) {
							cost = player.getLevel() * Math.abs(cost) + 1;
						}
						if (player.drop("money",
								cost)) {
							healerBehaviour.heal(player);
							raiser.say("Zostałeś uleczony. W czym jeszcze mogę pomóc?");
						} else {
							raiser.say("Przepraszam, ale nie możesz sobie na to pozwolić.");
						}

						currentBehavRes = null;
					}
				});

		engine.add(ConversationStates.HEAL_OFFERED,
				ConversationPhrases.NO_MESSAGES, null,
				false, ConversationStates.ATTENDING,
				"Dobrze w czym jeszcze mogę pomóc?", null);
	}

}
