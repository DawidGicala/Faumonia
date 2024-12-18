/* $Id: RatRaid.java,v 1.4 2020/12/07 10:42:26 davvids Exp $ */
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
 * @author miguel
 * 
 * Not safe for players below level 150
 */
public class RatRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("szczur", 30);
		attackArmy.put("mysz domowa", 5);
		attackArmy.put("szczur jaskiniowy", 25);
		attackArmy.put("krwiożerczy szczur", 20);
		attackArmy.put("wściekły szczur", 20);
		attackArmy.put("szczur zombie", 20);
		attackArmy.put("szczur olbrzymi", 5);
		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "Niebezpieczny dla wojowników poniżej poziomu 150.";
	}
}
