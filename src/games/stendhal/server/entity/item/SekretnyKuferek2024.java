/* $Id: SekretnyKuferek2024.java,v 1.19 2020/12/23 10:34:18 davvids Exp $ */
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
 * A SekretnyKuferek2024 which can be unwrapped.
 * 
 * @author kymara
 */
public class SekretnyKuferek2024 extends Box {

	private static final String[] ITEMS = { 
    "zwój wofol", 
    "zwój nalwor", 
    "orcza mapa", 
    "zakrwawiony zwój", 
    "wampirzy zwój", 
    "zwój do lasu", 
    "zwój żywiołów", 
    "zwój chaosu", 
    "zwój ados", 
    "zwój athor", 
    "zwój amazon", 
    "zwój do kamiennego kręgu", 
    "zwój do krypty", 
    "szczurzy zwój", 
    "zwój assasynów", 
    "zwój kikareukin", 
    "zwój do pałacu", 
    "zwój fado", 
    "zwój kirdneh", 
    "zwój kalavan", 
    "zwój semos", 
    "bilet turystyczny", 
    "smoczy zwój", 
    "demoniczny zwój", 
    "magiczny bilet", 
    "bilet do niebios", 
    "bilet śmierci", 
    "anielski zwój", 
    "zwój olbrzymów" 
	};

	/**
	 * Creates a new SekretnyKuferek2024.
	 * 
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public SekretnyKuferek2024(final String name, final String clazz, final String subclass,
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
	public SekretnyKuferek2024(final SekretnyKuferek2024 item) {
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
