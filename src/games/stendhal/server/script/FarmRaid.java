/* $Id: FarmRaid.java,v 1.3 2010/09/19 02:36:26 nhnb Exp $ */
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
 * A raid safe for lowest level players
 */
public class FarmRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("prosiak", 20);
		attackArmy.put("krowa", 30);
		attackArmy.put("kokoszka", 15);
		attackArmy.put("koza", 10);
		attackArmy.put("koń", 10);
		attackArmy.put("pisklak", 5);
		attackArmy.put("byk", 10);
		attackArmy.put("baran", 55);
		attackArmy.put("mysz domowa", 5);
		attackArmy.put("biały koń", 10);
		return attackArmy;
	}
	
	@Override
	protected String getInfo() {
		return "Rajd bezpieczny dla wojowników z niskim poziomem.";
	}
}