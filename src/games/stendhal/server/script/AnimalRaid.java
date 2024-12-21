/* $Id: AnimalRaid.java,v 1.3 2020/07/12 10:24:26 davvids Exp $ */
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
 * Not safe for players below level 5
 */
public class AnimalRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("małpa", 5);
		attackArmy.put("żmija", 7);
		attackArmy.put("bóbr", 5);
		attackArmy.put("tygrys", 8);
		attackArmy.put("lew", 8);
		attackArmy.put("panda", 4);
		attackArmy.put("pingwin", 4);
		attackArmy.put("krab", 4);
		attackArmy.put("wąż", 4);
		attackArmy.put("kaiman", 5);
		attackArmy.put("nietoperz", 4);
		attackArmy.put("miś", 4);
		attackArmy.put("niedźwiedź grizli", 15);
		attackArmy.put("niedźwiedź", 15);
		attackArmy.put("dzik", 10);
		attackArmy.put("lisica", 8);
		attackArmy.put("wilk", 15);
		attackArmy.put("słoń", 5);
		attackArmy.put("krokodyl", 6);
		attackArmy.put("pająk", 1);
		attackArmy.put("nietoperz wampir", 1);
		attackArmy.put("zabójcza pszczoła", 1);
		attackArmy.put("pająk ptasznik", 1);

		return attackArmy;
	}
	
	@Override
	protected String getInfo() {
		return " * Niebezpieczny dla wojowników poniżej poziomu 5";
	}
}
