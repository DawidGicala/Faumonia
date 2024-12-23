/* $Id: ItemsOnTable.java,v 1.6 2010/09/19 02:28:21 nhnb Exp $ */
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
package games.stendhal.server.maps.ados.rosshouse;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;

import java.util.Map;

/**
 * Creates the items on the tables and ground in the Ross' house.
 *
 * @author kymara
 */
public class ItemsOnTable implements ZoneConfigurator {
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildRossHouseArea(zone);
	}

	private void buildRossHouseArea(final StendhalRPZone zone) {
		addPersistentItem("pluszowy miś", zone, 9, 9);
		addPersistentItem("kości do gry", zone, 12, 10);
	}

	private Item addPersistentItem(final String name, final StendhalRPZone zone, final int x, final int y) {
		final Item item = SingletonRepository.getEntityManager().getItem(name);
		item.setPosition(x, y);
		zone.add(item, false);

		return item;
	}
}
