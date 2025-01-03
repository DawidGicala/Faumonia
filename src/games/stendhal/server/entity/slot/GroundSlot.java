/* $Id: GroundSlot.java,v 1.13 2011/04/30 11:36:33 nhnb Exp $ */
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
package games.stendhal.server.entity.slot;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.pathfinder.Path;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.player.Player;

import java.awt.Rectangle;
import java.util.List;


/**
 * a pseudo slot which represents a location on the ground
 *
 * @author hendrik
 */
public class GroundSlot extends EntitySlot {
	private final StendhalRPZone zone;
//	private int itemid;
	private Entity item;
	private final int x;
	private final int y;

	/**
	 * creates a new GroundSlot
	 *
	 * @param zone zone
	 * @param x x-coordinate
	 * @param y y-coordinate
	 */
	public GroundSlot(StendhalRPZone zone, int x, int y) {
		this.zone = zone;
		this.x = x;
		this.y = y;
	}

	/**
	 * creates a new GroundSlot with an item.
	 * @param zone zone
	 * @param item item on the ground
	 */
	public GroundSlot(StendhalRPZone zone, Entity item) {
		this.zone = zone;
		this.item = item;
		this.x = item.getX();
		this.y = item.getY();
	}

	@Override
	public boolean isReachableForTakingThingsOutOfBy(Entity entity) {

		// TODO: test me
		int xDistance = Math.abs(entity.getX() - x);
		int yDistance = Math.abs(entity.getY() - y);
		if ((xDistance <= 1) && (yDistance <= 1)) {
			setErrorMessage("Jest zbyt daleko");
			return false;
		}

		String playerName = getOtherPlayerStandingOnItem(entity);
		if (playerName != null) {
			setErrorMessage("Ten przedmiot jest chroniony przez " + playerName);
			return false;
		}
		return true;
	}

	@Override
	public boolean isReachableForThrowingThingsIntoBy(Entity entity) {
		// and in reach
		if (entity.squaredDistance(x, y) > (8 * 8)) {
			setErrorMessage("Jest zbyt daleko.");
			return false;
		}
		
		if (!isGamblingZoneAndIsDice(item)) {
			// and there is a path there
			final List<Node> path = Path.searchPath(entity, zone,
					entity.getX(), entity.getY(), new Rectangle(x, y, 1, 1),
					64 /* maxDestination * maxDestination */, false);
			if (path.isEmpty()) {
				setErrorMessage("Nie ma łatwej drogi do tego miejsca.");
				return false;
			}
		}

		return true;
	}

	/** 
	 * returns true if zone is semos tavern and entity is dice
	 *
	 * @param entity the item
	 */
	private boolean isGamblingZoneAndIsDice(final Entity entity) {
		return "int_semos_tavern_0".equals(zone.getName()) && ("kości do gry").equals(entity.getTitle());
	}



	/**
	 * Checks whether the item is below <b>another</b> player.
	 * 
	 * @return name of other player standing on the item or <code>null</code>
	 */
	private String getOtherPlayerStandingOnItem(final Entity player) {
		if (item == null) {
			return null;
		}
		final List<Player> players = player.getZone().getPlayers();
		for (final Player otherPlayer : players) {
			if (player.equals(otherPlayer)) {
				continue;
			}
			if (otherPlayer.getArea().intersects(item.getArea())) {
				return otherPlayer.getName();
			}
		}
		return null;
	}


	/**
	 * gets the type of the slot ("slot", "ground", "market")
	 *
	 * @return slot type
	 */
	@Override
	public String getSlotType() {
		return "ground";
	}

	// TODO: ((Entity) getOwner() will not work
}
