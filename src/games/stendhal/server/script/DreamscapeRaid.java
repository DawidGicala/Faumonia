/* $Id: DreamscapeRaid.java,v 1.4 2020/12/07 10:42:26 davvids Exp $ */
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
 * @author miguel
 * 
 * Not safe for players below level 150
 */
public class DreamscapeRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("brain ahouga", 15);
		attackArmy.put("mściciel", 15);
		attackArmy.put("hybryda aruthon", 15);
		attackArmy.put("smrodliwy", 15);
		attackArmy.put("googon", 15);
		attackArmy.put("czerwony roohako", 15);
		attackArmy.put("latający glutek", 15);
		attackArmy.put("błękitny mohiko", 15);
		attackArmy.put("mroczny aruthon", 15);
		attackArmy.put("xenocium", 10);
		attackArmy.put("necrosophia", 10);
		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "Niebezpieczny dla wojowników poniżej poziomu 150.";
	}
}
