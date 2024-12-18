/* $Id: PrezentSwiateczny2020.java,v 1.19 2020/12/23 10:34:18 davvids Exp $ */
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
 * A PrezentSwiateczny2020 which can be unwrapped.
 * 
 * @author kymara
 */
public class PrezentSwiateczny2020 extends Box {

	private static final String[] ITEMS = { "czapka świąteczna 2020", "sztylet z lodu",
			"sztylet z lodu", "sztylet z lodu", "sztylet z lodu", "sztylet z wiecznego lodu", "sztylet z wiecznego lodu", "karp świąteczny 2020", "karp świąteczny 2020", "karp świąteczny 2020", "karp świąteczny 2020", "karp świąteczny 2020", "karp świąteczny 2020", "karp świąteczny 2020", "karp świąteczny 2020", "śledź świąteczny 2020", "śledź świąteczny 2020", "śledź świąteczny 2020", "śledź świąteczny 2020", "śledź świąteczny 2020", "śledź świąteczny 2020", "śledź świąteczny 2020", "śledź świąteczny 2020", "laska cukrowa 2020", "laska cukrowa 2020", "laska cukrowa 2020", "laska cukrowa 2020", "laska cukrowa 2020", "laska cukrowa 2020", "choinka 2020", "choinka 2020", "choinka 2020", "czerwone kamienie szlachetne", "czerwone kamienie szlachetne", "czerwone kamienie szlachetne", "czerwony kamień szlachetny", "czerwony kamień szlachetny", "czerwony kamień szlachetny", "czerwony kamień szlachetny", "czerwony kamień szlachetny", "czerwony kamień szlachetny", "czerwony kamyczek szlachetny", "czerwony kamyczek szlachetny", "czerwony kamyczek szlachetny", "czerwony kamyczek szlachetny", "czerwony kamyczek szlachetny", "czerwony kamyczek szlachetny", "czerwony kamyczek szlachetny", "czerwony kamyczek szlachetny", "czerwony kamyczek szlachetny" };

	/**
	 * Creates a new PrezentSwiateczny2020.
	 * 
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public PrezentSwiateczny2020(final String name, final String clazz, final String subclass,
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
	public PrezentSwiateczny2020(final PrezentSwiateczny2020 item) {
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
