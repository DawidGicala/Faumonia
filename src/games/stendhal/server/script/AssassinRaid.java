/* $Id: AssassinRaid.java,v 1.4 2020/12/07 10:26:26 davvidsExp $ */
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
public class AssassinRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("uczeń mordercy", 25);
		attackArmy.put("bandyta", 25);
		attackArmy.put("morderca", 25);
		attackArmy.put("morderca aplikant", 20);
		attackArmy.put("łowca", 20);
		attackArmy.put("cichy morderca", 20);
		attackArmy.put("mistrz morderca", 20);
		// no dark archangel here as archers can still hit you as you run from them
		return attackArmy;
	}
	
	@Override
	protected String getInfo() {
		return "Niebezpieczny dla wojowników poniżej poziomu 150";
	}
}
