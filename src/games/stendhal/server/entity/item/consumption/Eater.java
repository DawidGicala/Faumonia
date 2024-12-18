/* $Id: Eater.java,v 1.9 2011/04/19 19:59:07 kymara Exp $ */
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
package games.stendhal.server.entity.item.consumption;

import games.stendhal.common.NotificationType;
import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.ConsumableItem;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

class Eater implements Feeder {

	public boolean feed(final ConsumableItem item, final Player player) {
		if (player.isChokingToDeath()) {
			int playerHP = player.getHP();
			int chokingDamage = Rand.rand(2 * playerHP / 3);
			player.setHP(playerHP - chokingDamage);
			if (player.getGender().equals("F")) {
				player.sendPrivateText(NotificationType.NEGATIVE, "Zjadłaś tak dużo, że zwymiotowałaś na ziemię i straciłaś " + Integer.toString(chokingDamage) + " punkt" + " życia.");
			} else {
				player.sendPrivateText(NotificationType.NEGATIVE, "Zjadłeś tak dużo, że zwymiotowałeś na ziemię i straciłeś " + Integer.toString(chokingDamage) + " punkt" + " życia.");
			}
			final Item sick = SingletonRepository.getEntityManager().getItem("wymioty");
			player.getZone().add(sick);
			sick.setPosition(player.getX(), player.getY() + 1);
			player.clearFoodList();
			player.notifyWorldAboutChanges();
			return false;
		}
		
		if (player.isChoking()) {
			// remove some HP so they know we are serious about this
			int playerHP = player.getHP();
			int chokingDamage = Rand.rand(playerHP / 3);
			player.setHP(playerHP - chokingDamage);
			if (player.getGender().equals("F")) {
				player.sendPrivateText(NotificationType.NEGATIVE, "Zjadłaś tak dużo na raz, że zadławiłaś się jedzeniem i straciłaś " + Integer.toString(chokingDamage) + " punkt" + " życia. Jeżeli zjesz więcej to możesz się pochorować.");
			} else {
				player.sendPrivateText(NotificationType.NEGATIVE, "Zjadłeś tak dużo na raz, że zadławiłeś się jedzeniem i straciłeś " + Integer.toString(chokingDamage) + " punkt" + " życia. Jeżeli zjesz więcej to możesz się pochorować.");
			}
			player.notifyWorldAboutChanges();
		} else if (player.isFull()) {
			if (player.getGender().equals("F")) {
				player.sendPrivateText(NotificationType.PRIVMSG, "Jesteś teraz najedzona i już więcej nie powinnaś jeść.");
			} else {
				player.sendPrivateText(NotificationType.PRIVMSG, "Jesteś teraz najedzony i już więcej nie powinieneś jeść.");
			}
		} 
		player.eat((ConsumableItem) item.splitOff(1));
		return true;
	}

}
