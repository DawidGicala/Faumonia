/* $Id: ReverseArrow.java,v 1.101 2012/04/20 17:38:11 kymara Exp $ */
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
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.LoginListener;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.item.token.Token;
import games.stendhal.server.entity.mapstuff.portal.OnePlayerRoomDoor;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.mapstuff.sign.Sign;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayTextWithPlayerNameAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.game.IRPZone;

import org.apache.log4j.Logger;

/**
 * A quest where the player has to invert an arrow build out of stones by moving
 * only up to 3 tokens.
 *
 * @author hendrik
 */

public class ReverseArrow extends AbstractQuest implements
		Token.TokenMoveListener<Token>, LoginListener {

	private static final Logger LOGGER = Logger.getLogger(ReverseArrow.class);

	// constants
	private static final String QUEST_SLOT = "reverse_arrow";

	private static final String ZONE_NAME = "int_ados_reverse_arrow";

	/** Time (in Seconds) to solve the puzzle. */
	private static final int TIME = 60;

	/** Possible number of moves to solve the puzzle. */
	private static final int MAX_MOVES = 3;

	/** Horizontal position of the upper left token at the beginning. */
	private static final int OFFSET_X = 15;

	/** Vertical position of the upper left token at the beginning. */
	private static final int OFFSET_Y = 10;

	// "static" data
	protected StendhalRPZone zone;

	protected SpeakerNPC npc;

	protected List<Token> tokens;

	private OnePlayerRoomDoor door;

	private StendhalRPZone entranceZone;

	// quest instance data
	private int moveCount;

	protected Player player;

	private Timer timer;

	/**
	 * Checks the result.
	 */
	protected class ReverseArrowCheck implements TurnListener {

		/**
		 * Is the task solved?
		 *
		 * @return true on success, false on failure
		 */
		private boolean checkBoard() {
			if (tokens == null) {
				return false;
			}

			// We check the complete arrow (and not just the three moved
			// tokens) here for two reasons:

			// 1. there are 6 permutions so the code would quite messy
			// 2. there may be a solution i did not recognize

			// This aproach has the side effect that the code does not
			// tell the solution :-)

			// sort the tokens according to their position
			Collections.sort(tokens, new Comparator<Token>() {
				public int compare(final Token t1, final Token t2) {
					int d = t1.getY() - t2.getY();
					if (d == 0) {
						d = t1.getX() - t2.getX();
					}
					return d;
				}
			});
			// * * 0 * *
			// * 1 2 3 *
			// 4 5 6 7 8

			// get the position of the topmost token
			final int topX = tokens.get(0).getX();
			final int topY = tokens.get(0).getY();

			// check first row
			for (int i = 1; i <= 3; i++) {
				final Token token = tokens.get(i);
				if ((token.getX() != topX - 1 + (i - 1))
						|| (token.getY() != topY + 1)) {
					return false;
				}
			}

			// check second row
			for (int i = 4; i <= 8; i++) {
				final Token token = tokens.get(i);
				if ((token.getX() != topX - 2 + (i - 4))
						|| (token.getY() != topY + 2)) {
					return false;
				}
			}

			return true;
		}

		/**
		 * invoked shortly after the player did his/her third move.
		 * @param currentTurn on which it is invoked
		 */
		public void onTurnReached(final int currentTurn) {
			if (checkBoard() && (moveCount <= MAX_MOVES)) {
				if (player.isQuestCompleted(QUEST_SLOT)) {
					npc.say("Gratulacje znowu rozwiązałeś zadanie, ale nie mam dla ciebie nagrody.");
				} else {
					npc.say("Gratulacje rozwiązałeś zadanie.");
					final StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem(
									"money");
					money.setQuantity(50);
					player.equipToInventoryOnly(money);
					player.addXP(100);
				}
				player.setQuest(QUEST_SLOT, "done");
			} else {
				if (!player.isQuestCompleted(QUEST_SLOT)) {
					player.setQuest(QUEST_SLOT, "failed");
				}
				npc.say("To nie wygląda jak strzałka skierowana w górę do mnie.");
			}

			// teleport the player out after 2 seconds of delay
			SingletonRepository.getTurnNotifier().notifyInTurns(6,
					new FinishNotifier(true, player)); 
		}
	}

	/**
	 * Teleports the player out.
	 */
	protected class FinishNotifier implements TurnListener {
		private final boolean reset;

		private final Player finishPlayer;

		public FinishNotifier(final boolean reset, final Player player) {
			this.finishPlayer = player;
			this.reset = reset;
		}

		/**
		 * invoked shortly after the player did his job.
		 * @param currentTurn on which it is invoked
		 */
		public void onTurnReached(final int currentTurn) {
			finish(reset, finishPlayer);
		}
	}

	/**
	 * Tells the player the remaining time and teleports him out if the task is
	 * not completed in time.
	 */
	class Timer implements TurnListener {
		private final Player timerPlayer;

		/**
		 * Starts a teleport-out-timer.
		 *
		 * @param player
		 *            the player who started the timer
		 */
		protected Timer(final Player player) {
			timerPlayer = player;
		}

		private int counter = TIME;

		public void onTurnReached(final int currentTurn) {
			// check that the player is still in game and stop the timer
			// in case the player is not playing anymore.
			// Note that "player" always refers to the current player
			// in order not to teleport the next player out too early,
			// we have to compare it to the player who started this timer
			if ((player == timerPlayer) && (player != null)) {
				final IRPZone playerZone = player.getZone();

				if (playerZone.equals(zone)) {
					if (counter > 0) {
						npc.say("Zostało Tobie " + counter + " sekundę.");
						counter = counter - 10;
						SingletonRepository.getTurnNotifier().notifyInTurns(10 * 3, this);
					} else {
						// teleport the player out
						npc.say("Przepraszam, ale twój czas się skończył.");
						SingletonRepository.getTurnNotifier().notifyInTurns(1,
								new FinishNotifier(true, player));
						// need to do this on the next turn
					}
				}
			}
		}
	}

	/**
	 * A special door that only lets one player in at a time, and that notifies
	 * this script on successful usage.
	 */
	class NotifyingDoor extends OnePlayerRoomDoor {
		NotifyingDoor(final String clazz) {
			super(clazz);
		}

		@Override
		public boolean onUsed(final RPEntity user) {
			boolean success = super.onUsed(user);
			
			if (user instanceof Player) {
				start((Player) user);
			} else {
				LOGGER.error("user is no instance of Player but: " + user, new Throwable());
			}

			return success;
		}

		@Override
		public void onUsedBackwards(final RPEntity user) {
			super.onUsedBackwards(user);

			if (user instanceof Player) {
				finish(true, (Player) user);
			} else {
				LOGGER.error("user is no instance of Player but: " + user, new Throwable());
			}
		}
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "ReverseArrow";
	}

	/**
	 * Creates a token and adds it to the world.
	 *
	 * @param x
	 *            x-position
	 * @param y
	 *            y-position
	 */
	private void addTokenToWorld(final int x, final int y) {
		final Token token = (Token) SingletonRepository.getEntityManager().getItem("wskaźnik");
		token.setPosition(x, y);
		token.setTokenMoveListener(this);
		zone.add(token, false);
		tokens.add(token);
	}

	/**
	 * Adds the tokens to the game field.
	 */
	private void addAllTokens() {
		// 0 1 2 3 4
		// * 5 6 7 *
		// * * 8 * *
		tokens = new LinkedList<Token>();
		for (int i = 0; i < 5; i++) {
			addTokenToWorld(OFFSET_X + i, OFFSET_Y);
		}
		for (int i = 1; i < 4; i++) {
			addTokenToWorld(OFFSET_X + i, OFFSET_Y + 1);
		}
		addTokenToWorld(OFFSET_X + 2, OFFSET_Y + 2);
	}

	/**
	 * Removes all tokens (called after the player messed them up).
	 */
	private void removeAllTokens() {
		if (tokens != null) {
			for (final Token token : tokens) {
				if (token != null) {
					zone.remove(token.getID());
				}
			}
		}
		tokens = null;
	}

	private void step_1() {
		zone = SingletonRepository.getRPWorld().getZone(ZONE_NAME);
		step1CreateNPC();
		step1CreateDoors();
	}

	private void step1CreateNPC() {
		npc = new GamblosSpeakerNPC("Gamblos");

		npc.setEntityClass("oldwizardnpc"); 
		npc.setPosition(20, 8);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		zone.add(npc);
	}

	private static final class GamblosSpeakerNPC extends SpeakerNPC {
		private GamblosSpeakerNPC(String name) {
			super(name);
		}

		@Override
		protected void createPath() {
			// NPC doesn't move
			setPath(null);
		}

		@Override
		protected void createDialog() {
			add(ConversationStates.IDLE, 
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(new GreetingMatchesNameCondition(getName()),
							new QuestCompletedCondition(QUEST_SLOT)), 
					ConversationStates.ATTENDING, 
					null,
					new SayTextWithPlayerNameAction("Witaj ponownie [name]. Pamiętam, że rozwiązałeś już ten problem. Oczywiście możesz to zrobić ponownie."));
			
			add(ConversationStates.IDLE, 
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(new GreetingMatchesNameCondition(getName()),
							new QuestNotCompletedCondition(QUEST_SLOT)), 
					ConversationStates.ATTENDING, 
					"Cześć. Witam w naszej małej grze. Twoim zadaniem jest skierowanie tej strzałki do góry poprzez przemieszczenie trzech krążków.",
					null);
			
			addHelp("Musisz stanąć obok krążka, aby go przesunąć.");
			addJob("Jestem opiekunem tego zadania.");
			addGoodbye("Miło było Cię poznać.");
			addQuest("Twoim zadaniem w tej grze jest odwrócenie kierunku strzałki przesuwając tylko 3 krążki w "
					+ TIME + " sekundę.");
		}
	}

	private void step1CreateDoors() {
		// 0_semos_mountain_n2 at (95,101)
		final String entranceZoneName = "0_semos_mountain_n2";
		entranceZone = SingletonRepository.getRPWorld().getZone(entranceZoneName);
		door = new NotifyingDoor("housedoor");
		door.setPosition(95, 101);
		door.setIdentifier(Integer.valueOf((0)));
		door.setDestination(ZONE_NAME, Integer.valueOf(0));
		entranceZone.add(door);

		door.open();

		final Portal exit = new Portal();
		exit.setPosition(17, 20);
		exit.setIdentifier(Integer.valueOf(0));
		exit.setDestination(entranceZoneName, Integer.valueOf(0));
		zone.add(exit);

		final Sign sign = new Sign();
		sign.setPosition(96, 102);
		sign.setText("Jeżeli drzwi są zamknięte to będziesz musiał chwilę poczekać dopóki ostatni gracz nie skończy swojego zadania.");
		entranceZone.add(sign);
	}

	public void onLoggedIn(final Player player) {
		// need to do this on the next turn
		SingletonRepository.getTurnNotifier().notifyInTurns(1, new FinishNotifier(false, player));
	}

	/**
	 * The player moved a token.
	 *
	 * @param player Player
	 * @param token Token
	 */
	public void onTokenMoved(final Player player, Token token) {
		//TODO only count if the token really changed its position
		moveCount++;

		if (moveCount < MAX_MOVES) {
			npc.say("To był twój " + Grammar.ordered(moveCount) + " ruch.");
		} else if (moveCount == MAX_MOVES) {
			npc.say("To był twój " + Grammar.ordered(moveCount)
					+ " i ostatni ruch. Pozwól mi sprawdzić twoją pracę.");
			// notify in 2 seconds
			SingletonRepository.getTurnNotifier().notifyInTurns(6, new ReverseArrowCheck()); 
			if (timer != null) {
				SingletonRepository.getTurnNotifier().dontNotify(timer);
			}
		} else {
			npc.say("Przepraszam, ale możesz zrobić tylko " + MAX_MOVES + " ruchy");
		}
	}

	/**
	 * A player entered the zone.
	 *
	 * @param player
	 *            Player
	 */
	public void start(final Player player) {
		final IRPZone playerZone = player.getZone();

		if (playerZone.equals(zone)) {
			this.player = player;
			removeAllTokens();
			addAllTokens();
			timer = new Timer(player);
			SingletonRepository.getTurnNotifier().notifyInTurns(0, timer);
			moveCount = 0;
		}
	}

	/**
	 * Finishes the quest and teleports the player out.
	 *
	 * @param reset
	 *            reset it for the next player (set to false on login)
	 * @param player
	 *            the player to teleport out
	 */
	protected void finish(final boolean reset, final Player player) {
		if (player != null) {
			final IRPZone playerZone = player.getZone();

			if (playerZone.equals(zone)) {
				player.teleport(entranceZone, door.getX(), door.getY() + 1,
						Direction.DOWN, player);
			}
		}
		if (reset) {
			removeAllTokens();
			this.player = null;
			moveCount = 0;
			if (timer != null) {
				SingletonRepository.getTurnNotifier().dontNotify(timer);
			}
			door.open();
		}
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Odwrócona Strzała",
				"Sądzisz, że możesz rozwiązać tą małą zagadkę? Pośpiesz się masz kilka sekund.",
				false);

		SingletonRepository.getLoginNotifier().addListener(this);

		step_1();
	}
	
	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			final String questState = player.getQuest(QUEST_SLOT);
			res.add("Gamblos zadał mi zagadkę.");
			if ("failed".equals(questState)) {
				res.add("Nie rozwiązałem zagadki.");
			} 
			if (isCompleted(player)) {
				res.add("Ułożyłem puzle poprawnie.");
			}
			return res;
	}

	@Override
	public String getNPCName() {
		return "Gamblos";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
}
