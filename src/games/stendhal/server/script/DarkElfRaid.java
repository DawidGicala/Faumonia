/* $Id: DarkElfRaid.java,v 1.4 2020/12/07 10:42:26 davvids Exp $ */
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
public class DarkElfRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("elf ciemności", 30);
		attackArmy.put("elf ciemności łucznik", 30);
		attackArmy.put("elf ciemności łucznik elitarny", 15);
		attackArmy.put("elf ciemności kapitan", 15);
		attackArmy.put("elf ciemnosci rycerz", 15);
		attackArmy.put("elf ciemności generał", 15);
		attackArmy.put("elf ciemności czarownik", 15);
		attackArmy.put("elf ciemności królewicz", 15);
		attackArmy.put("elf ciemności czarnoksiężnik", 15);
		attackArmy.put("elf ciemności matrona", 10);
		attackArmy.put("elf ciemności mistrz", 15);
		attackArmy.put("elf ciemności komandos", 15);
		attackArmy.put("elf ciemności admirał", 15);
		attackArmy.put("pająk", 6);
		attackArmy.put("pająk ptasznik", 6);
		attackArmy.put("królowa pająków", 3);
		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "Niebezpieczny dla wojowników poniżej poziomu 150.";
	}
}
