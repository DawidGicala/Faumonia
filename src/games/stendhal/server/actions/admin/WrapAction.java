/* $Id: WrapAction.java,v 1.6 2013/01/06 16:23:55 nhnb Exp $ */
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
package games.stendhal.server.actions.admin;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.actions.ActionListener;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.Present;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;

import org.apache.log4j.Logger;

/**
 * @author Martin Fuchs
 */
public class WrapAction implements ActionListener {

	static final Logger logger = Logger.getLogger(WrapAction.class);

	/**
	 * Registers the "wrap" action handler.
	 */
	public static void register() {
		final WrapAction wrap = new WrapAction();
		CommandCenter.register("wrap", wrap, 13);
	}

	@Override
	public void onAction(final Player player, final RPAction action) {
		if (action.get("type").equals("wrap")) {
			onWrap(player, action);
		}
	}

	private void onWrap(final Player player, final RPAction action) {
		String itemName = action.get("target");
		final String args = action.get("args");

		if ((args != null) && (args.length() > 0)) {
			itemName += ' ';
			itemName += args;
		}

		itemName = Grammar.singular(itemName);

		final Item item = player.getFirstEquipped(itemName);

		if (item != null) {

			final Present present = (Present) SingletonRepository.getEntityManager().getItem("prezent");
			present.setContent(itemName);
			player.drop(itemName);
			player.equipToInventoryOnly(present);

			new GameEvent(player.getName(), "wrap", itemName).raise();

			player.updateItemAtkDef();
		} else {
			player.sendPrivateText("Nie posiadasz żadnego " + itemName);
		}
	}

}
