/* $Id: MagicyRaid.java,v 1.19 2010/10/07 17:06:26 nhnb Exp $ */
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
 * @author Tomko
 */
public class MagicyRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("pustelnik", 30);
		attackArmy.put("uczeń czarnoksiężnika", 25);
		attackArmy.put("czarnoksiężnik", 25);
		attackArmy.put("uczeń czarnoksiężnika mroku", 25);
		attackArmy.put("czarnoksiężnik mroku", 20);
		attackArmy.put("czarownica z Aenye", 5);
		attackArmy.put("mega mag", 3);

		return attackArmy;
	}
}
