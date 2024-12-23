/* $Id: OniRaid.java,v 1.1 2011/02/12 14:11:07 kymara Exp $ */
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
 * Less safe for players below level 70
 */
public class OniRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("oni wojownik", 30);
		attackArmy.put("oni łucznik", 30);
		attackArmy.put("oni królowa", 30);
		attackArmy.put("oni król", 30);
		attackArmy.put("oni kapłan", 30);


		return attackArmy;
	}
}
