/* $Id: Stocking.java,v 1.17 2012/12/03 16:27:50 kymara Exp $ */
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
 * a stocking which can be opened
 * 
 * @author kymara
 */
public class Stocking extends Box {

	private static final String[] ITEMS = { "wielki eliksir", "eliksir miłości", "granat",
			"zupa rybna", "bryłka mithrilu", "węgiel", "leżakowana nalewka litworowa"};

	/**
	 * Creates a new Stocking.
	 * 
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public Stocking(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param item
	 *            item to copy
	 */
	public Stocking(final Stocking item) {
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
