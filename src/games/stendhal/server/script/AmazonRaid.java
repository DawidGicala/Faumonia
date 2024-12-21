/* $Id: AmazonRaid.java,v 1.3 2011/05/03 19:15:53 kymara Exp $ */
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
public class AmazonRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("amazonka łucznik", 30);
		attackArmy.put("amazonka łowca", 20);
		attackArmy.put("amazonka strażniczka wybrzeża", 20);
		attackArmy.put("amazonka łucznik komandor", 20);
		attackArmy.put("amazonka elitarna strażniczka", 20);
		attackArmy.put("amazonka ochroniarz", 20);
		attackArmy.put("amazonka mistrzyni strażniczek", 20);
		attackArmy.put("amazonka komandor", 20);
		attackArmy.put("amazonka zwiadowca", 20);
		attackArmy.put("amazonka imperator", 20);
		return attackArmy;
	}
	
	@Override
	protected String getInfo() {
		return "Nie jest bezpieczny dla wojowników poniżej 150 poziomu";
	}
}
