/* $Id: OutfitAction.java,v 1.29 2013/01/06 16:24:06 nhnb Exp $ */
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
package games.stendhal.server.actions;

import static games.stendhal.common.constants.Actions.OUTFIT;
import static games.stendhal.common.constants.Actions.VALUE;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;

/**
 * Changes the outfit for the player
 */
public class OutfitAction implements ActionListener {
	private static String COLOR_MAP = "outfit_colors";

	public static void register() {
		CommandCenter.register(OUTFIT, new OutfitAction());
	}

	/**
	 * Changes Player's outfit to the value provided in action. 
	 * @param player whose outfit is to be changed. Must not be <code>null</code>.
	 * @param action the action containing the outfit info in the attribute 'value'. Must not be <code>null</code>.
	 */
	@Override
	public void onAction(final Player player, final RPAction action) {
		if (action.has(VALUE)) {
			final Outfit outfit = new Outfit(action.getInt(VALUE));
			if (outfit.isChoosableByPlayers()) {
				new GameEvent(player.getName(), OUTFIT, action.get(VALUE)).raise();
				player.setOutfit(outfit, false);
				// players may change hair and dress colors
				String color = action.get("hair");
				if (color != null) {
					player.put(COLOR_MAP, "hair", color);
				} else {
					player.remove(COLOR_MAP, "hair");
				}
				color = action.get("dress");
				if (color != null) {
					player.put(COLOR_MAP, "dress", color);
				} else {
					player.remove(COLOR_MAP, "dress");
				}
			}
			if ((player.getOutfit().getBase() > 5 && player.getOutfit().getBase() < 12) || player.getOutfit().getBase() == 13) {
				player.setGender("F");
			} else {
				player.setGender("M");
			}
		}
	}
}
