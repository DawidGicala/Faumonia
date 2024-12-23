/* $Id: AdminLevelAction.java,v 1.16 2013/01/06 16:23:55 nhnb Exp $ */
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

import static games.stendhal.common.constants.Actions.ADMINLEVEL;
import static games.stendhal.common.constants.Actions.NEWLEVEL;
import static games.stendhal.common.constants.Actions.TARGET;
import games.stendhal.common.NotificationType;
import games.stendhal.common.constants.Actions;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;

class AdminLevelAction extends AdministrationAction {

	public static void register() {
		CommandCenter.register(ADMINLEVEL, new AdminLevelAction(), 0);

	}

	@Override
	public void perform(final Player player, final RPAction action) {

		if (action.has(TARGET)) {

			final String name = action.get(TARGET);
			final Player target = SingletonRepository.getRuleProcessor().getPlayer(name);

			if ((target == null) || (target.isGhost() && !isAllowedtoSeeGhosts(player))) {
				player.sendPrivateText("Wojownik \"" + name + "\" nie został znaleziony");
				return;
			}

			final int oldlevel = target.getAdminLevel();
			String response = target.getTitle() + " posiada " + oldlevel + " poziom administratora";

			if (action.has(NEWLEVEL)) {
				// verify newlevel is a number
				int newlevel;
				try {
					newlevel = Integer.parseInt(action.get(NEWLEVEL));
				} catch (final NumberFormatException e) {
					player.sendPrivateText("Poziom administratora musi być liczbą całkowitą");

					return;
				}

				// If level is beyond max level, just set it to max.
				if (newlevel > REQUIRED_ADMIN_LEVEL_FOR_SUPER) {
					newlevel = REQUIRED_ADMIN_LEVEL_FOR_SUPER;
				}

				final int mylevel = player.getAdminLevel();
				if (mylevel < REQUIRED_ADMIN_LEVEL_FOR_SUPER) {
					response = "Potrzebujesz "
							+ REQUIRED_ADMIN_LEVEL_FOR_SUPER
							+ " poziomu administratora, aby móc go zmieniać.";
				} else {

					new GameEvent(player.getName(), ADMINLEVEL, target.getName(), ADMINLEVEL, action.get(NEWLEVEL)).raise();
					target.setAdminLevel(newlevel);
					dropPrivileges(target);
					target.update();
					target.notifyWorldAboutChanges();

					response = "Zmieniono poziom administratora " + target.getTitle()
							+ " z " + oldlevel + " na " + newlevel + ".";
					target.sendPrivateText(NotificationType.SUPPORT,
							player.getTitle() + " zmienił Twój poziom administratora z "
							+ oldlevel + " na " + newlevel + ".");
				}
			}

			player.sendPrivateText(response);
		}
	}

	/**
	 * Drop persistent administrator attributes if the player is no longer
	 * allowed to have them.
	 *  
	 * @param player the player whose privileges should be re-examined 
	 */
	private void dropPrivileges(Player player) {
		if (!AdministrationAction.isPlayerAllowedToExecuteAdminCommand(player, Actions.TELECLICKMODE, false)) {
			player.setTeleclickEnabled(false);
		}
		if (!AdministrationAction.isPlayerAllowedToExecuteAdminCommand(player, Actions.GHOSTMODE, false)) {
			player.setGhost(false);
		}
		if (!AdministrationAction.isPlayerAllowedToExecuteAdminCommand(player, Actions.INVISIBLE, false)) {
			player.setInvisible(false);
		}
	}

	private boolean isAllowedtoSeeGhosts(final Player player) {
		return AdministrationAction.isPlayerAllowedToExecuteAdminCommand(player, Actions.GHOSTMODE, false);
	}

}
