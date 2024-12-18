/* $Id: AngelRaid.java,v 1.4 2010/09/19 02:36:26 nhnb Exp $ */
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
 * @author kymara
 * 
 * Not safe for players below level 150
 */
public class AngelRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("aniołek", 40);
		attackArmy.put("anioł", 15);
		attackArmy.put("upadły anioł", 15);
		attackArmy.put("anioł ciemności", 5);
		attackArmy.put("archanioł", 7);
		attackArmy.put("archanioł ciemności", 3);
		// no dark archangel here as archers can still hit you as you run from them
		return attackArmy;
	}
	
	@Override
	protected String getInfo() {
		return "Niebezpieczny dla wojowników poniżej poziomu 150";
	}
}
