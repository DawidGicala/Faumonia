/* $Id: MadaramRaid.java,v 1.4 2020/12/07 10:42:26 davvids Exp $ */
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
public class MadaramRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("madaram łucznik", 15);
		attackArmy.put("madaram wieśniak", 15);
		attackArmy.put("madaram komandos", 15);
		attackArmy.put("madaram żołnierz", 15);
		attackArmy.put("madaram znachor", 15);
		attackArmy.put("madaram z toporem", 15);
		attackArmy.put("madaram królowa", 15);
		attackArmy.put("madaram bohater", 15);
		attackArmy.put("madaram wietrzny wędrowca", 15);
		attackArmy.put("madaram kawalerzysta", 15);
		attackArmy.put("madaram myśliwy", 15);
		attackArmy.put("madaram łamacz mieczy", 15);
		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "Niebezpieczny dla wojowników poniżej poziomu 150.";
	}
}
