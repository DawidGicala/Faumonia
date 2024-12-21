/* $Id: OrcRaid.java,v 1.2 2010/10/07 17:10:26 nhnb Exp $ */
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
 * Atak hordy zbójeckiej
 */
public class ZbojnicyRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("zbójnik leśny oszust", 20);
		attackArmy.put("zbójnik leśny", 20);
		attackArmy.put("zbójnik leśny tchórzliwy", 20);
		attackArmy.put("zbójnik leśny starszy", 20);
		attackArmy.put("zbójnik leśny zwiadowca", 20);
		attackArmy.put("banitka", 20);
		attackArmy.put("banita", 20);
		attackArmy.put("banita gajowy", 20);
		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "Atak hordy zbójeckiej.";
	}
}
