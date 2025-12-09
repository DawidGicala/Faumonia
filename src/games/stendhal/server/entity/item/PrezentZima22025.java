/* $Id: PrezentZima22025.java,v 1.19 2025/12/05 21:34:18 davvids Exp $ */
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
 * A PrezentZima22025 which can be unwrapped.
 * 
 * @author kymara
 */
public class PrezentZima22025 extends Box {

	private static final String[] ITEMS = { "hełm lodowego w 2025", "zbroja lodowego w 2025", "spodnie lodowego w 2025", "buty lodowego w 2025", "tarcza lodowego w 2025", "płaszcz lodowego w 2025", "rękawice lodowego w 2025", "miecz lodowego w 2025", "topór lodowego w 2025", "maczuga lodowego w 2025", "zielony kamień szlachetny zima 2025", "zielony kamień szlachetny zima 2025", "zielony kamień szlachetny zima 2025", "zielony kamień szlachetny zima 2025", "zielony kamień szlachetny zima 2025", "zielony kamień szlachetny zima 2025", "zielony kamień szlachetny zima 2025", "zielony kamień szlachetny zima 2025", "zielony kamień szlachetny zima 2025", "zielony kamień szlachetny zima 2025" };

	/**
	 * Creates a new PrezentZima22025.
	 * 
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public PrezentZima22025(final String name, final String clazz, final String subclass,
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
	public PrezentZima22025(final PrezentZima22025 item) {
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
