/* $Id: BeholderRaid.java,v 1.1 2011/01/02 10:23:05 kymara Exp $ */
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
 * Not safe for players below level 10
 */
public class BeholderRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("oczko", 10);
		attackArmy.put("zielony glut", 10);
		attackArmy.put("brązowy glut", 10);
		attackArmy.put("czarny glut", 8);
		attackArmy.put("oko", 15);
		attackArmy.put("oko starsze", 10);
		attackArmy.put("wąż", 8);
		attackArmy.put("żmija", 8);
		return attackArmy;
	}
	
	@Override
	protected String getInfo() {
		return "Nie jest bezpieczna dla wojowników poniżej 12 poziomu";
	}
}
