/* $Id: SkrzyniaSkarbow7.java,v 1.19 2024/04/07 00:44:18 davvids Exp $ */
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

import games.stendhal.common.ItemTools;
import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.player.Player;

import java.util.Map;

/**
 * A SkrzyniaSkarbow7 which can be unwrapped.
 * 
 * @author kymara
 */
public class SkrzyniaSkarbow7 extends Box {

	private static final String[] ITEMS = { "błękitne kamyczki szlachetne", "błękitny kamień szlachetny", "czerwony kamyczek szlachetny", "czerwony kamień szlachetny", "czerwony kamień szlachetny", "czerwone kamienie szlachetne", "czerwone kamienie szlachetne" };

	/**
	 * Creates a new SkrzyniaSkarbow7.
	 * 
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public SkrzyniaSkarbow7(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);

		setContent(ITEMS[Rand.rand(ITEMS.length)]);
	}

	/**
	 * Sets content.
	 * @param type of item to be produced.
	 */
	public void setContent(final String type) {
		setInfoString(type);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param item
	 *            item to copy
	 */
	public SkrzyniaSkarbow7(final SkrzyniaSkarbow7 item) {
		super(item);
	}

	@Override
	protected boolean useMe(final Player player) {
		this.removeOne();

		final String itemName = getInfoString();
		final Item item = SingletonRepository.getEntityManager().getItem(itemName);
		player.sendPrivateText("Gratulacje dostałeś " 
				+ Grammar.a_noun(ItemTools.itemNameToDisplayName(itemName)) + "!");

		player.equipOrPutOnGround(item);
		player.notifyWorldAboutChanges();

		return true;
	}

}
