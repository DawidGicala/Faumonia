/* $Id: ItemsOnTable.java,v 1.4 2010/11/27 13:59:21 nylon0700 Exp $ */
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
package games.stendhal.server.maps.ados.bakery;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.spawner.PassiveEntityRespawnPoint;

import java.util.Map;

/**
 * Creates items in Ados bakery
 */
public class ItemsOnTable implements ZoneConfigurator {
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildBakeryArea(zone);
	}

	private void buildBakeryArea(final StendhalRPZone zone) {

		// grower for an empty eared bottle (1h)
		final PassiveEntityRespawnPoint bottleGrower1 = new PassiveEntityRespawnPoint("butla czwórniaczka", 12000);
		bottleGrower1.setPosition(8, 13);
		bottleGrower1.setDescription("Wygląda na miejsce, gdzie można znaleść butelkę.");
		zone.add(bottleGrower1);

		bottleGrower1.setToFullGrowth();

	}

}
