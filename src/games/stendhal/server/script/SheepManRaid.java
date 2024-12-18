/* $Id: SheepManRaid.java,v 1.2 2010/09/19 02:36:26 nhnb Exp $ */
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
 * Not safe for players below level 30
 */
public class SheepManRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("turoń", 30);
		attackArmy.put("turoń uzbrojony", 30);
		attackArmy.put("starszy turoń", 30);
		attackArmy.put("turoń elitarny", 30);
		return attackArmy;
	}
	
	@Override
	protected String getInfo() {
		return "Nie jest bezpieczny dla wojowników poniżej 30 poziomu";
	}
}
