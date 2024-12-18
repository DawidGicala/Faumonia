/* $Id: ElfRaid.java,v 1.3 2010/09/19 02:36:26 nhnb Exp $ */
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
public class ElfRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("elf", 35);
		attackArmy.put("elf milicjant", 30);
		attackArmy.put("elf żołnierz", 30);
		attackArmy.put("elf komandor", 25);
		attackArmy.put("pani elf", 25);
		attackArmy.put("panna elf", 25);
		attackArmy.put("elf łucznik", 30);
		attackArmy.put("drzewiec", 3);
		attackArmy.put("pasterz drzew", 2);

		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "Mniej bezpieczny dla wojowników poniżej poziomu 30.";
	}
}
