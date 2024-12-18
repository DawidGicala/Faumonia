/* $Id: PtakiRaid.java,v 1.2 2010/09/19 02:36:26 nhnb Exp $ */
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
package games.stendhal.server.script;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gummipferd
 * 
 * Less safe for players below level 40
 */
public class PtakiRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("niebieski ptak", 30);
		attackArmy.put("żółty ptak", 30);
		attackArmy.put("zielony ptak", 30);
		attackArmy.put("kremowy ptak", 30);
		attackArmy.put("fioletowy ptak", 30);
		attackArmy.put("czerwony ptak", 30);
		return attackArmy;
	}
}
