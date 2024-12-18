/* $Id: AlbinoElfRaid.java,v 1.3 2010/09/19 02:36:26 nhnb Exp $ */
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
 * @author gummipferd
 * 
 * Less safe for players below level 30
 */
public class AlbinoElfRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("elf albinos rycerz", 35);
		attackArmy.put("elf albinos łucznik", 30);
		attackArmy.put("elf albinos magik", 30);
		attackArmy.put("elf albinos królowa", 25);
		attackArmy.put("elf albinos król", 25);

		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "Mniej bezpieczny dla wojowników poniżej poziomu 30.";
	}
}
