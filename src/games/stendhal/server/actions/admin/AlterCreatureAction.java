/* $Id: AlterCreatureAction.java,v 1.14 2011/01/02 16:19:34 kymara Exp $ */
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

import static games.stendhal.common.constants.Actions.ALTERCREATURE;
import static games.stendhal.common.constants.Actions.TARGET;
import static games.stendhal.common.constants.Actions.TEXT;
import games.stendhal.common.MathHelper;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;

class AlterCreatureAction extends AdministrationAction {

	public static void register() {
		CommandCenter.register(ALTERCREATURE, new AlterCreatureAction(), 17);

	}

	@Override
	public void perform(final Player player, final RPAction action) {

		if (action.has(TARGET) && action.has(TEXT)) {
			final Entity changed = getTarget(player, action);

			if (changed == null) {
				logger.debug("Entity not found");
				player.sendPrivateText("Jednostka nie została znaleziona");
				return;
			}

			/*
			 * It will contain a string like: name;atk;def;hp;xp
			 */
			final String stat = action.get(TEXT);

			final String[] parts = stat.split(";");
			if (!(changed instanceof Creature)) {
				logger.debug("Target " + changed.getTitle() + " was not a creature.");
				player.sendPrivateText("Obiekt " + changed.getTitle() + " nie jest potworem.");
				return;
			}

			if (parts.length != 5) {
				logger.debug("Incorrect stats string for creature.");
				player.sendPrivateText("/altercreature <id> name;atk;def;hp;xp - Użyj, aby zamienić domyślne wartości.");
				return;
			}


			final Creature creature = (Creature) changed;
			new GameEvent(player.getName(), "alter", action.get(TARGET), stat).raise();

			final int newatk = MathHelper.parseIntDefault(parts[1], creature.getAtk());
			final int newdef = MathHelper.parseIntDefault(parts[2], creature.getDef());
			final int newHP = MathHelper.parseIntDefault(parts[3], creature.getBaseHP());
			final int newXP = MathHelper.parseIntDefault(parts[4], creature.getXP());

			if(!"-".equals(parts[0])) {
				creature.setName(parts[0]);
			}
			creature.setAtk(newatk);
			creature.setDef(newdef);
			creature.initHP(newHP);
			creature.setXP(newXP);

			creature.update();
			creature.notifyWorldAboutChanges();


		}
	}

}
