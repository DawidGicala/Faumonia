/* $Id: DragonHardRaid.java,v 1.4 2020/12/07 10:34:04 davvids Exp $ */
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
 * Not safe for players below level 150
 */
public class DragonHardRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("latający czarny smok", 1);
		attackArmy.put("latający złoty smok", 2);
		attackArmy.put("zielony smok", 2);
		attackArmy.put("czerwony smok", 2);
		attackArmy.put("szkielet smoka", 2);
		attackArmy.put("błękitny smok", 2);
		attackArmy.put("pustynny smok", 2);
		attackArmy.put("złoty smok", 3);
		attackArmy.put("zgniły szkielet smoka", 2);
		attackArmy.put("dwugłowy zielony smok", 20);
		attackArmy.put("dwugłowy niebieski smok", 20);
		attackArmy.put("dwugłowy czerwony smok", 20);
		attackArmy.put("dwugłowy czarny smok", 20);
		attackArmy.put("jeździec chaosu na czerwonym smoku", 4);
		attackArmy.put("jeździec chaosu na zielonym smoku", 4);
		attackArmy.put("smok arktyczny", 10);
		attackArmy.put("czarny smok", 10);
		attackArmy.put("czarne smoczysko", 10);
		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "Niebezpieczny dla wojowników poniżej poziomu 150.";
	}
}
