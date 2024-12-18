/* $Id: BareBonesBrowserLaunchCommandsFactory.java,v 1.9 2011/08/04 21:22:56 nhnb Exp $ */
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
package games.stendhal.client.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Factory to create all known {@link SlashAction}s that open a specified URL in the browser
 *  
 * @author madmetzger
 */
public class BareBonesBrowserLaunchCommandsFactory {
	
	private static Map<String, String> commandsAndUrls;
	
	private static void initialize() {
		commandsAndUrls = new HashMap<String, String>();
		commandsAndUrls.put("expowiska", "https://faumonia.pl/expowiska/");
		commandsAndUrls.put("mapy", "https://faumonia.pl/atlas-map/");
		commandsAndUrls.put("potwory", "https://faumonia.pl/bestariusz/");
		commandsAndUrls.put("przedmioty", "https://faumonia.pl/przedmioty/");
		commandsAndUrls.put("npce", "https://faumonia.pl/npce/");
		commandsAndUrls.put("sklepy", "https://faumonia.pl/sklepy/");
		commandsAndUrls.put("zadania", "https://faumonia.pl/zadania/");
	}
	
	/**
	 * creates {@link SlashAction}s for all in initialize specified values 
	 * 
	 * @return map of the created actions
	 */
	public static Map<String, SlashAction> createBrowserCommands() {
		initialize();
		Map<String, SlashAction> commandsMap = new HashMap<String, SlashAction>();
		for(Entry<String, String> entry : commandsAndUrls.entrySet()) {
			commandsMap.put(entry.getKey(), new BareBonesBrowserLaunchCommand(entry.getValue()));
		}
		return commandsMap;
	}

}
