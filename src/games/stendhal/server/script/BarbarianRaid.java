/* $Id: BarbarianRaid.java,v 1.2 2010/09/19 02:36:26 nhnb Exp $ */
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

public class BarbarianRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("barbarzyńca", 40);
		attackArmy.put("barbarzyńca wilczur", 25);
		attackArmy.put("barbarzyńca elitarny", 22);
		attackArmy.put("barbarzyńca kapłan", 17);
		attackArmy.put("barbarzyńca szaman", 15);
		attackArmy.put("barbarzyńca lider", 13);
		attackArmy.put("król barbarzyńca", 8);

		return attackArmy;
	}

}
