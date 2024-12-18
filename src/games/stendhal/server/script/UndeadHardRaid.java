/* $Id: UndeadHardRaid.java,v 1.3 2010/09/19 02:36:26 nhnb Exp $ */
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
 * Not safe for players below level 80
 */
public class UndeadHardRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("demoniczny szkielet", 10);
		attackArmy.put("szkielet smoka", 3);
		attackArmy.put("upadły wojownik", 15);
		attackArmy.put("rycerz śmierci", 15);
		attackArmy.put("diabelska królowa", 15);
		attackArmy.put("niewidoczny człowiek", 15);
		attackArmy.put("zielona zjawa", 15);
		attackArmy.put("żywa zbroja", 15);
		attackArmy.put("upadły kapłan", 15);
		attackArmy.put("upadły wysoki kapłan", 15);
		attackArmy.put("licho", 15);
		attackArmy.put("martwe licho", 10);
		attackArmy.put("wysokie licho", 10);

		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "Niebezpieczny dla wojowników poniżej poziomu 80.";
	}
}
