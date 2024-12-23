/* $Id: CoalSource.java,v 1.11 2011/04/02 15:44:18 kymara Exp $ */
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
package games.stendhal.server.entity.mapstuff.useable;


import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

import org.apache.log4j.Logger;

/**
 * A coal source is a spot where a player can pick for coal. He
 * needs a pick, time, and luck.
 * 
 * Picking coals takes 7-11 seconds; during this time, the player keep standing
 * next to the coal source. In fact, the player only has to be there when the
 * prospecting action has finished. Therefore, make sure that two sources
 * are always at least 5 sec of walking away from each other, so that the player
 * can't prospect at several sites simultaneously.
 *
 * @author hendrik
 */
public class CoalSource extends PlayerActivityEntity {
	private static final Logger logger = Logger.getLogger(CoalSource.class);

	/**
	 * The equipment needed.
	 */
	private static final String NEEDED_EQUIPMENT = "kilof";

	/**
	 * The name of the item to be found.
	 */
	private final String itemName;

	/**
	 * Create a gold source.
	 */
	public CoalSource() {
		this("węgiel");
	}

	/**
	 * Create a coal source.
	 * 
	 * @param itemName
	 *            The name of the item to be prospected.
	 */
	public CoalSource(final String itemName) {
		this.itemName = itemName;

		setRPClass("useable_entity");
		put("type", "useable_entity");
		put("class", "source");
		put("name", "coal_source");
		put("state", 0);

		setDescription("Jest coś czarnego na skale.");
		handleRespawn();
	}

	/**
	 * source name.
	 */
	@Override
	public String getName() {
		return("kawałek węgla");
	}


	//
	// PlayerActivityEntity
	//

	/**
	 * Get the time it takes to perform this activity.
	 * 
	 * @return The time to perform the activity (in seconds).
	 */
	@Override
	protected int getDuration() {
		return 7 + Rand.rand(4);
	}

	/**
	 * Decides if the activity can be done.
	 * 
	 * @return <code>true</code> if successful.
	 */
	@Override
	protected boolean isPrepared(final Player player) {
		if (player.isEquipped(NEEDED_EQUIPMENT)) {
			return true;
		}

		player.sendPrivateText("Potrzebujesz kilofa, aby wydobyć węgiel.");
		return false;
	}

	/**
	 * Decides if the activity was successful.
	 * 
	 * @return <code>true</code> if successful.
	 */
	@Override
	protected boolean isSuccessful(final Player player) {
		return getState() > 0;
	}

	/**
	 * Called when the activity has finished.
	 * 
	 * @param player
	 *            The player that did the activity.
	 * @param successful
	 *            If the activity was successful.
	 */
	@Override
	protected void onFinished(final Player player, final boolean successful) {
		if (successful) {
			final Item item = SingletonRepository.getEntityManager().getItem(itemName);

			if (item != null) {
				player.equipOrPutOnGround(item);
				player.incMinedForItem(item.getName(), item.getQuantity());
			    SingletonRepository.getAchievementNotifier().onObtain(player);
				player.sendPrivateText("Znalazłeś "
						+ Grammar.a_noun(item.getTitle()) + ".");
			} else {
				logger.error("could not find item: " + itemName);
			}
			setState(getState()- 1);
			handleRespawn();
		} else {
			player.sendPrivateText("Nic nie znalazłeś.");
		}
	}

	/**
	 * triggers the respawn if the coal was compeletly picked
	 */
	private void handleRespawn() {
		if (getState() == 0) {
			final int time = Rand.randExponential(6000);
			int turn = Math.max(time, 6000);
			TurnNotifier.get().notifyInTurns(turn, new Refiller());
		}
	}

	/**
	 * Called when the activity has started.
	 * 
	 * @param player
	 *            The player starting the activity.
	 */
	@Override
	protected void onStarted(final Player player) {
		player.sendPrivateText("Rozpocząłeś wydobywanie węgla.");
	}

	/**
	 * refills the coal
	 *
	 * @author hendrik
	 */
	private class Refiller implements TurnListener {

		public void onTurnReached(int currentTurn) {
			setState(Rand.randUniform(1, 3));
		}
		
	}
}
