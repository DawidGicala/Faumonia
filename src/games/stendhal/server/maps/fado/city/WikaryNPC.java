/* $Id: WikaryNPC.java,v 1.25 2011/06/21 02:28:01 Legolas Exp $ */
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
 //Zrobiony na podstawie GateKeeperNPC z Sedah city 
 
package games.stendhal.server.maps.fado.city;

import games.stendhal.common.Rand;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.BehaviourAction;
import games.stendhal.server.entity.npc.behaviour.impl.Behaviour;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a gatekeeper NPC Bribe him with at least 300 money to get the key for
 * the Sedah city walls. He stands in the doorway of the gatehouse till the
 * interior is made.
 *
 * @author kymara
 */
public class WikaryNPC implements ZoneConfigurator {

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
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Wikary") {
			private Player removalCustomer;
			private int removalPrice;

			private int removalPrice(final Player player) {
				int price;
				if (player.getLevel() < 10) {
					price = 1000;
				} else if (player.getLevel() >= 10 && player.getLevel() < 250) {
					price = 10000;
				} else if (player.getLevel() >= 250 && player.getLevel() < 350) {
					price = 20000;
				} else {
					price = 40000;
				}
				return price;
			}

			private boolean hasSkull(final Player player, final EventRaiser raiser) {
				if (!player.isBadBoy()) {
					removalCustomer = null;
					raiser.say("Nie nosisz piętna zabójcy. Nie musisz za nic płacić.");
					return false;
				}
				return true;
			}

			private void offerRemoval(final Player player, final EventRaiser raiser) {
				removalCustomer = player;
				removalPrice = removalPrice(player);
				raiser.say("Zdjęcie piętna zabójcy kosztuje " + removalPrice + " monet. Czy chcesz zapłacić?");
				raiser.setCurrentState(ConversationStates.SERVICE_OFFERED);
			}


			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(57, 51));
				nodes.add(new Node(57, 55));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting(null, new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (player.isEquipped("gigantyczny eliksir")) {
							// give some money to get
							// giant potion
							if (Rand.throwCoin() == 1) {
								raiser.say("Widzę, że potrzebujesz więcej.");
							} else {
								raiser.say("Witaj ponownie.");
							}
						} else {
							raiser.say("Witaj! Czyżbyś przybył tutaj, aby złożyć datek?");
						}
					}
				});

				addReply("nic", "Dobrze.");
				addJob("Jestem wikarym w Fado. Zbieram pieniądze na zakup organów do kaplicy, zapytaj mnie o #ofertę, aby dowiedzieć się jak mogę Cię wynagrodzić za ten gest.");
				addHelp("Wspomóż zakup organów do kaplicy, mówiąc #datek #<ilość> #money lub zdejmij klątwę z siebie, mówiąc #zdejmij");
				addQuest("Zbieram datki na organy do kaplicy. Możesz złożyć #datek albo poprosić o zdjęcie piętna zabójcy. W tej drugiej sprawie powiedz #zdejmij.");
				addOffer("Za #datek na organy, wynoszący co najmniej 1000 monet, podaruję ci tabliczkę czekolady. Mogę też #zdjąć piętno zabójcy - przed zapłatą podam ci cenę.");

				addReply("datek", null,
					new BehaviourAction(new Behaviour("money"), Arrays.asList("charity", "datek"), "offer") {
					@Override
					public void fireSentenceError(Player player, Sentence sentence, EventRaiser raiser) {
						raiser.say(sentence.getErrorString() + " Próbujesz mnie oszukać?");
					}

					@Override
						public void fireRequestOK(final ItemParserResult res, final Player player, final Sentence sentence, final EventRaiser raiser) {
						final int amount = res.getAmount();

						if (sentence.getExpressions().size() == 1) {
							// player only said 'datek'
							raiser.say("Przyjmuję datki od 1000 monet. Jeśli chcesz pomóc, powiedz na przykład: datek 1000 money.");
						} else {
							if (amount < 1000) {
								// Less than 1000 is not money for him
								raiser.say("Przyjmuję datki od 1000 monet. Jeśli chcesz pomóc, powiedz na przykład: datek 1000 money.");
							} else {
								if (player.isEquipped("money", amount)) {
									player.drop("money", amount);
									raiser.say("Bóg ci zapłać. Jesteśmy coraz bliżej zakupu nowych organów! Przyjmij tabliczkę czekolady w podziękowaniu.");
									final Item drink = SingletonRepository.getEntityManager().getItem(
											"tabliczka czekolady");
									player.equipOrPutOnGround(drink);
								} else {
									// player gave enough but doesn't have
									// the cash
									raiser.say("Nie masz " + amount + " money, aby wesprzeć zakup organów. Przyjdź kiedy indziej.");
								}
							}
						}
					}

					@Override
					public void fireRequestError(final ItemParserResult res, final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (sentence.getExpressions().size() == 1 || res.getChosenItemName() == null) {
							fireRequestOK(res, player, sentence, raiser);
						} else {
							// This bit is just in case the player says 'datek X potatoes', not money
							raiser.say("Potrzebuję pieniędzy na zakup organów.");
						}
					}
				});

				add(ConversationStates.ATTENDING, Arrays.asList("remove", "zdejmij", "zdjąć"), null,
					ConversationStates.ATTENDING, null, new ChatAction() {
						public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
							if (hasSkull(player, raiser)) {
								offerRemoval(player, raiser);
							}
						}
					});

				add(ConversationStates.SERVICE_OFFERED, ConversationPhrases.YES_MESSAGES, null,
					ConversationStates.ATTENDING, null, new ChatAction() {
						public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
							if (!hasSkull(player, raiser)) {
								return;
							}
							// Never charge a different player or a price they have not confirmed.
							if (removalCustomer != player || removalPrice != removalPrice(player)) {
								offerRemoval(player, raiser);
								return;
							}
							if (player.drop("money", removalPrice)) {
								player.rehabilitate();
								raiser.say("Zdjąłem z ciebie piętno zabójcy. Uważaj na siebie!");
							} else {
								raiser.say("Potrzebujesz " + removalPrice + " monet, aby zdjąć piętno zabójcy. Wróć, gdy je zbierzesz.");
							}
							removalCustomer = null;
						}
					});

				add(ConversationStates.SERVICE_OFFERED, ConversationPhrases.NO_MESSAGES, null,
					ConversationStates.ATTENDING, "Dobrze. Wróć, jeśli zmienisz zdanie.", new ChatAction() {
						public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
							removalCustomer = null;
						}
					});

				addGoodbye("Bywaj!");
			}
		};

		npc.setDescription("Oto Wikary. Zbiera pieniądze na zakup organów do kaplicy.");
		/*
		 * We don't seem to be using the recruiter images that lenocas made for
		 * the Fado Raid area so I'm going to put him to use here. If the raid
		 * part ever gets done, this image can change.
		 */
		npc.setEntityClass("npcwikary");
		npc.setPosition(57, 51);
		npc.initHP(100);
		zone.add(npc);
	}
}
