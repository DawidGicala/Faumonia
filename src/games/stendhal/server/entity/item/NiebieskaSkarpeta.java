/* $Id: NiebieskaSkarpeta.java,v 1.17 2012/12/03 16:27:50 kymara Exp $ */
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
 * a NiebieskaSkarpeta which can be opened
 * 
 * @author kymara
 */
public class NiebieskaSkarpeta extends Box {

	private static final String[] ITEMS = { "świąteczna tarcza IV", "świąteczna tarcza V", "świąteczna tarcza VI", "świąteczna zbroja IV", "świąteczna zbroja V", "świąteczna zbroja VI", "świąteczy hełm IV", "świąteczny hełm V, ", "świąteczny płaszcz IV", "świąteczny płaszcz V", "świąteczny płaszcz VI", "świąteczne buty IV", "świąteczne buty V", "świąteczne buty VI", "świąteczne rękawice III", "świąteczne rękawice IV", "świąteczny sztylet II", "świąteczny miecz IV", "świąteczny miecz V", "świąteczny miecz VI", "świąteczny naszyjnik I", "świąteczna różdżka I", "świąteczna różdżka II", "świąteczny topór I", "świąteczny topór II", "świąteczna kusza I"};

	/**
	 * Creates a new NiebieskaSkarpeta.
	 * 
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public NiebieskaSkarpeta(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param item
	 *            item to copy
	 */
	public NiebieskaSkarpeta(final NiebieskaSkarpeta item) {
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
