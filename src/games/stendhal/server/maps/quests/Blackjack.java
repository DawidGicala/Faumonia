/* $Id: Blackjack.java,v 1.76 2012/02/13 00:34:02 bluelads99 Exp $ */
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
package games.stendhal.server.maps.quests;

import games.stendhal.common.Direction;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.BehaviourAction;
import games.stendhal.server.entity.npc.behaviour.impl.Behaviour;
import games.stendhal.server.entity.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Blackjack extends AbstractQuest {
	// spades ♠
	 private static final String SPADES = "\u2660";

	// hearts ♥
	private static final String HEARTS = "\u2665";

	// diamonds ♦
	private static final String DIAMONDS = "\u2666";

	// clubs ♣
	private static final String CLUBS = "\u2663";

	
	
	// 1 min at 300 ms/turn
	private static final int CHAT_TIMEOUT = 200;

	private static final int MIN_STAKE = 10;

	private static final int MAX_STAKE = 400;

	private int stake;

	private boolean bankStands;

	private boolean playerStands;

	private Map<String, Integer> cardValues;

	private Stack<String> deck;

	private final List<String> playerCards = new LinkedList<String>();

	private final List<String> bankCards = new LinkedList<String>();

	private StackableItem playerCardsItem;

	private StackableItem bankCardsItem;

	private SpeakerNPC ramon;

	private final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(
			"-1_athor_ship_w2");

	private void startNewGame(final Player player) {
		cleanUpTable();
		playerCards.clear();
		bankCards.clear();
		playerCardsItem = (StackableItem) SingletonRepository.getEntityManager().getItem("karty do gry");
		zone.add(playerCardsItem);
		playerCardsItem.setPosition(25, 38);
		bankCardsItem = (StackableItem) SingletonRepository.getEntityManager().getItem("karty do gry");
		bankCardsItem.setPosition(27, 38);
		zone.add(bankCardsItem);

		playerStands = false;
		bankStands = false;
		// Before each game, we put all cards back on the deck and
		// shuffle it.
		// We could change that later so that the player can
		// try to remember what's still on the deck
		deck = new Stack<String>();
		for (final String card : cardValues.keySet()) {
			deck.add(card);
		}
		Collections.shuffle(deck);

		dealCards(player, 2);
	}

	private void cleanUpTable() {
		if (playerCardsItem != null) {
			zone.remove(playerCardsItem);
			playerCardsItem = null;
		}
		if (bankCardsItem != null) {
			zone.remove(bankCardsItem);
			bankCardsItem = null;
		}
	}

	private int countAces(final List<String> cards) {
		int count = 0;
		for (final String card : cards) {
			if (card.startsWith("A")) {
				count++;
			}
		}
		return count;
	}

	private int sumValues(final List<String> cards) {
		int sum = 0;
		for (final String card : cards) {
			sum += cardValues.get(card).intValue();
		}
		int numberOfAces = countAces(cards);
		while ((sum > 21) && (numberOfAces > 0)) {
			sum -= 10;
			numberOfAces--;
		}
		return sum;
	}

	private boolean isBlackjack(final List<String> cards) {
		return (sumValues(cards) == 21) && (cards.size() == 2);
	}

	/**
	 * Deals <i>number</i> cards to the player, if the player is not standing,
	 * and to the bank, if the bank is not standing.
	 * @param rpEntity 
	 *
	 * @param number
	 *            The number of cards that each player should draw.
	 */
	private void dealCards(final RPEntity rpEntity, final int number) {
		StringBuffer messagebuf = new StringBuffer();
		messagebuf.append("\n");
		int playerSum = sumValues(playerCards);
		int bankSum = sumValues(bankCards);
		for (int i = 0; i < number; i++) {
			if (!playerStands) {
				final String playerCard = deck.pop();
				playerCards.add(playerCard);
				messagebuf.append("Masz " + playerCard + ".\n");
			}

			if (playerStands && (playerSum < bankSum)) {
				messagebuf.append("Bank stawia.\n");
				bankStands = true;
			}
			if (!bankStands) {
				final String bankCard = deck.pop();
				bankCards.add(bankCard);
				messagebuf.append("Bank dostał " + bankCard + ".\n");
			}
			playerSum = sumValues(playerCards);
			bankSum = sumValues(bankCards);
		}
		playerCardsItem.setQuantity(playerSum);
		playerCardsItem.setDescription("Oto karty gracza: "
				+ Grammar.enumerateCollection(playerCards));
		playerCardsItem.notifyWorldAboutChanges();
		bankCardsItem.setQuantity(bankSum);
		bankCardsItem.setDescription("Oto karty banku: "
				+ Grammar.enumerateCollection(bankCards));
		bankCardsItem.notifyWorldAboutChanges();
		if (!playerStands) {
			messagebuf.append("Masz " + playerSum + ".\n");
			if (playerSum == 21) {
				playerStands = true;
			}
		}
		if (!bankStands) {
			messagebuf.append("Bank ma " + bankSum + ".\n");
			if ((bankSum >= 17) && (bankSum <= 21) && (bankSum >= playerSum)) {
				bankStands = true;
				messagebuf.append("Bank stawia.\n");
			}
		}
		final String message2 = analyze(rpEntity);
		if (message2 != null) {
			messagebuf.append(message2);
		}
		ramon.say(messagebuf.toString());
	}

	/**
	 * @param rpEntity 
	 * @return The text that the dealer should say, or null if he shouldn't say
	 *         anything.
	 */
	private String analyze(final RPEntity rpEntity) {
		final int playerSum = sumValues(playerCards);
		final int bankSum = sumValues(bankCards);
		String message = null;
		if (isBlackjack(bankCards) && isBlackjack(playerCards)) {
			message = "Masz blackjacka, ale bank także ma. Co za presja. ";
			message += payOff(rpEntity, 1);
		} else if (isBlackjack(bankCards)) {
			message = "Bank ma blackjack. Następnym razem będzie lepiej!";
		} else if (isBlackjack(playerCards)) {
			message = "Masz blackjacka! Gratuluję! ";
			message += payOff(rpEntity, 3);
		} else if (playerSum > 21) {
			if (bankSum > 21) {
				message = "Obydwaj przegraliśmy! Remis. ";
				message += payOff(rpEntity, 1);
			} else {
				message = "Przegrałeś! Następnym razem będzie lepiej!";
			}
		} else if (bankSum > 21) {
			message = "Bank przegrał! Gratulacje! ";
			message += payOff(rpEntity, 2);
		} else {
			if (!playerStands) {
				message = "Chcesz następną kartę?";
				ramon.setCurrentState(ConversationStates.QUESTION_1);
			} else if (!bankStands) {
				letBankDrawAfterPause(ramon.getAttending().getName());
			} else if (bankSum > playerSum) {
				message = "Bank wygrał. Następnym razem będzie lepiej!";
			} else if (bankSum == playerSum) {
				message = "Remis. ";
				message += payOff(rpEntity, 1);
			} else {
				message = "Wygrałeś. Gratulacje! ";
				message += payOff(rpEntity, 2);
			}
		}
		return message;
	}

	private void letBankDrawAfterPause(final String playerName) {
		SingletonRepository.getTurnNotifier().notifyInSeconds(1, new TurnListener() {
			private final String name = playerName;

			public void onTurnReached(final int currentTurn) {
				if (name.equals(ramon.getAttending().getName())) {
					dealCards(ramon.getAttending(), 1);
				}

			}

		});
	}

	/**
	 * Gives the player <i>factor</i> times his stake.
	 *
	 * @param rpEntity
	 *            The player.
	 * @param factor
	 *            The multiplier. 1 for draw, 2 for win, 3 for win with
	 *            blackjack.
	 * @return A message that the NPC should say to inform the player.
	 */
	private String payOff(final RPEntity rpEntity, final int factor) {
		final StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
		money.setQuantity(factor * stake);
		rpEntity.equipOrPutOnGround(money);
		if (factor == 1) {
			return "Odebrałeś swoją stawkę.";
		} else {
			return "Oto twoja stawka plus " + (factor - 1) * stake
					+ " złota.";
		}
	}

	@Override
	// @SuppressWarnings("unchecked")
	public void addToWorld() {

		// TODO: move ramon into his own NPC file.
		ramon = new SpeakerNPC("Ramon") {
			@Override
			protected void createPath() {
				// Ramon doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {

				addGreeting("Witam przy stoliku do #blackjacka! Możesz tutaj zagrać dla zabicia czasu dopóki prom nie dopłynie. Powiedz #zagraj.");
				addJob("Byłem krupierem w oberży w Semos, ale straciłem licencję. Ale mój brat Ricardo wciąż tam pracuje.");
                addReply(
						"blackjack",
						"Blackjack jest prostą grą w karty. Zasady gry są na tablicy na ścianie.");
				addHelp("Uważaj by gra nie zajęła cię za bardzo, bo możesz przegapić prom! Słuchaj ogłoszeń.");
				addGoodbye("Dowidzenia!");
			}

			@Override
			protected void onGoodbye(final RPEntity player) {
				// remove the cards when the player stops playing.
				cleanUpTable();
			}
		};

		ramon.setEntityClass("naughtyteen2npc");
		ramon.setPosition(26, 36);
		ramon.setDescription("Ramon chce zagrać z tobą pare kolejek w blackjacka. Chcesz spróbować szczęścia?");
		ramon.setDirection(Direction.DOWN);
		ramon.initHP(100);
		zone.add(ramon);

		cardValues = new HashMap<String, Integer>();
		final String[] colors = { CLUBS, 
				DIAMONDS, 
				HEARTS,
				SPADES }; 
		final String[] pictures = { "J", "Q", "K" };
		for (final String color : colors) {
			for (int i = 2; i <= 10; i++) {
				cardValues.put(i + color, Integer.valueOf(i));
			}
			for (final String picture : pictures) {
				cardValues.put(picture + color, Integer.valueOf(10));
			}
			// ace values can change to 1 during the game
			cardValues.put("A" + color, Integer.valueOf(11));
		}

		// increase the timeout, as otherwise the player often
		// would use their stake because of reacting too slow.
		
		ramon.setPlayerChatTimeout(CHAT_TIMEOUT); 

		ramon.add(ConversationStates.ATTENDING, Arrays.asList("play", "graj", "zagraj", "zagram"), null,
				ConversationStates.ATTENDING,
				"Aby zagrać musisz #postawić conajmniej " + MIN_STAKE
						+ ", a najwięcej " + MAX_STAKE
						+ " money. Ile chcesz zaryzykować?", null);

		ramon.add(ConversationStates.ATTENDING, Arrays.asList("stake", "postaw", "postawić"), null,
				ConversationStates.ATTENDING, null,
				new BehaviourAction(new Behaviour(), Arrays.asList("stake", "postaw", "postawić"), "offer") {
					@Override
					public void fireSentenceError(Player player, Sentence sentence, EventRaiser npc) {
			        	npc.say(sentence.getErrorString() + " Powiedz mi ile chcesz zaryzykować na przykład #'postaw 50'.");
					}

					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						if (sentence.hasError()) {
							fireSentenceError(player, sentence, npc);
						} else {
							ItemParserResult res = behaviour.parse(sentence);

							// don't use res.wasFound() and avoid to call fireRequestError()
							fireRequestOK(res, player, sentence, npc);
						}
					}

					@Override
					public void fireRequestOK(final ItemParserResult res, Player player, Sentence sentence, EventRaiser npc) {
						stake = res.getAmount();

						if (stake < MIN_STAKE) {
							npc.say("Musisz postawić co najmniej " + MIN_STAKE + " money.");
						} else if (stake > MAX_STAKE) {
							npc.say("Najwięcej możesz postawić " + MAX_STAKE + " money.");
						} else if (player.drop("money", stake)) {
							startNewGame(player);
						} else {
							npc.say("Hej! Nie masz wystarczająco dużo pieniędzy!");
						}
					}
				});

		ramon.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING, null, new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						dealCards(player, 1);
					}
				});

		// The player says he doesn't want to have another card.
		// Let the dealer give cards to the bank.
		ramon.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING, null, new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						playerStands = true;
						if (bankStands) {
							// Both stand. Let the dealer tell the final resul
							final String message = analyze(player);
							if (message != null) {
								ramon.say(message);
							}
						} else {
							letBankDrawAfterPause(player.getName());
						}
					}
				});
		
		fillQuestInfo(
				"Blackjack",
				"Podczas podróży na wyspę Athor, dla zabicia czasu możesz zagrać w Blackjack",
				true);
	}

	@Override
	public String getSlotName() {
		return "blackjack";
	}

	@Override
	public String getName() {
		return "Blackjack";
	}
	
	@Override
	public int getMinLevel() {
		return 0;
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
		return "Ramon";
	}
}
