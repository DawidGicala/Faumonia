/* $Id: ZielonaSkarpeta.java,v 1.17 2012/12/03 16:27:50 kymara Exp $ */
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
package games.stendhal.server.entity.item;

import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.player.Player;

import java.util.Map;

/**
 * a ZielonaSkarpeta which can be opened
 * 
 * @author kymara
 */
public class ZielonaSkarpeta extends Box {

	private static final String[] ITEMS = { "świąteczna tarcza VII", "świąteczna tarcza VIII", "świąteczna tarcza IX", "świąteczna zbroja VII", "świąteczna zbroja VIII", "świąteczna zbroja IX", "świąteczny hełm VI", "świąteczny hełm VII", "świąteczny hełm VIII", "świąteczny płaszcz VII", "świąteczny płaszcz VIII", "świąteczne spodnie VII", "świąteczne spodnie VIII", "świąteczne spodnie IX", "świąteczne buty VII", "świąteczne buty VIII", "świąteczne buty IX", "świąteczne rękawice V", "świąteczne rękawice VI", "świąteczne rękawice VII", "świąteczny sztylet III", "świąteczny sztylet IV", "świąteczny sztylet V", "świąteczny miecz VII", "świąteczny miecz VIII", "świąteczny miecz IX", "świąteczny naszyjnik II", "świąteczna różdżka IV", "świąteczna różdżka V", "świąteczny topór III", "świąteczna kusza II", "świąteczna kusza III", "świąteczny medal I", "świąteczny młot"};

	/**
	 * Creates a new ZielonaSkarpeta.
	 * 
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public ZielonaSkarpeta(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param item
	 *            item to copy
	 */
	public ZielonaSkarpeta(final ZielonaSkarpeta item) {
		super(item);
	}

	@Override
	protected boolean useMe(final Player player) {
		this.removeOne();
		final String itemName = ITEMS[Rand.rand(ITEMS.length)];
		final Item item = SingletonRepository.getEntityManager().getItem(
				itemName);
		player.sendPrivateText("Gratulacje otrzymałeś "
				+ Grammar.a_noun(itemName));
		player.equipOrPutOnGround(item);
		player.notifyWorldAboutChanges();
		return true;
	}

}
