/* $Id: MoveToAction.java,v 1.11 2013/01/06 17:37:08 nhnb Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.actions.move;

import static games.stendhal.common.constants.Actions.AWAY;
import static games.stendhal.common.constants.Actions.MOVETO;
import static games.stendhal.common.constants.Actions.TELECLICKMODE;
import static games.stendhal.common.constants.Actions.X;
import static games.stendhal.common.constants.Actions.Y;
import games.stendhal.common.Direction;
import games.stendhal.server.actions.ActionListener;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TutorialNotifier;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.pathfinder.Path;
import games.stendhal.server.entity.player.Player;

import java.util.List;

import marauroa.common.game.RPAction;

/**
 * Handles movement request from players
 *
 * @author hendrik
 */
public class MoveToAction implements ActionListener {

	/**
	 * registers the move to action
	 */
	public static void register() {
		final MoveToAction moveTo = new MoveToAction();
		CommandCenter.register(MOVETO, moveTo);
	}

	/**
	 * handles the move to action
	 *
	 * @param player player requesting the action
	 * @param action move-to action
	 */
	@Override
	public void onAction(final Player player, final RPAction action) {
		if (!player.getZone().isMoveToAllowed()) {
			player.sendPrivateText("Poruszanie się myszką jest tutaj niemożliwe. Użyj klawiatury.");
			return;
		}
		
		if (player.isPoisoned()) {
			player.sendPrivateText("Trucizna zdezorientowała Ciebie i nie możesz normalnie się poruszać. Wygląda na to, że możesz się poruszać tylko wstecz i nie możesz zaplanować drogi.");
			return;
		}
		
		if (player.hasPath()) {
			player.clearPath();
		}
		if (player.has(AWAY)) {
			player.remove(AWAY);
			player.setVisibility(100);
		}

		move(player, action);

		TutorialNotifier.move(player);
		player.applyClientDirection(false);
		player.notifyWorldAboutChanges();
	}


	/**
	 * calculates the path and starts to move the player (or teleports him in case of an admin in teleclickmode).
	 *
	 * @param player player requesting the action
	 * @param action move-to action
	 */
	private void move(final Player player, final RPAction action) {
		if (action.has(X) && action.has(Y)) {
			final int x = action.getInt(X);
			final int y = action.getInt(Y);

			if (player.has(AWAY)) {
				player.remove(AWAY);
				player.setVisibility(100);
			}

			if (player.has(TELECLICKMODE) && action.has("double_click")) {
				// Teleport
				final StendhalRPZone zone = player.getZone();
				player.teleport(zone, x, y, null, null);
			} else {
				// Walk
				final List<Node> path = Path.searchPath(player, x, y);
				extendPathForZoneChangeIfRequested(action, path);
				player.setPath(new FixedPath(path, false));
			}
		}
	}

	/**
	 * On request of the client add a node at the end of the path that goes 
	 * one step further into the requested direction. This allows the client
	 * to trigger a zone change with the mouse.
	 *
	 * @param player player requesting the action
	 * @param action move-to action
	 * @param path the path to extend if requested
	 */
	private void extendPathForZoneChangeIfRequested(RPAction action, List<Node> path) {
		if ((path == null) || path.isEmpty()) {
			return;
		}
		if (!action.has("extend")) {
			return;
		}
		Direction dir = Direction.build(action.getInt("extend"));
		Node lastNode = path.get(path.size() - 1);
		path.add(new Node(lastNode.getX() + dir.getdx(), lastNode.getY() + dir.getdy()));
	}
}
