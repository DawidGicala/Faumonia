/* $Id: DwarfRaid.java,v 1.3 2010/09/19 02:36:26 nhnb Exp $ */
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
 * Less safe for players below level 30
 */
public class DwarfRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("krasnal", 30);
		attackArmy.put("krasnal strażnik", 30);
		attackArmy.put("krasnal starszy", 30);
		attackArmy.put("krasnal lider", 30);
		attackArmy.put("krasnal bohater", 30);
		attackArmy.put("duergar", 20);
		attackArmy.put("duergar starszy", 20);
		attackArmy.put("duergar z toporem", 20);
		attackArmy.put("gigantyczny krasnal", 2);

		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "Niebezpieczny dla wojowników poniżej poziomu 30.";
	}
}
