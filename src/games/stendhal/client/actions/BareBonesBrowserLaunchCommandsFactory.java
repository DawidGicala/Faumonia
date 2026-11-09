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
 * Factory to create all known SlashActions that open a specified URL
 * in the browser.
 */
public class BareBonesBrowserLaunchCommandsFactory {

	private static Map<String, String> commandsAndUrls;

	private static void initialize() {
		commandsAndUrls = new HashMap<String, String>();

		/*
		 * Faumonia
		 */
		commandsAndUrls.put("faumonia_www", "https://faumonia.pl/");
		commandsAndUrls.put("aktualnosci", "https://faumonia.pl/aktualnosci-ze-swiata-faumonii/");
		commandsAndUrls.put("poradniki", "https://faumonia.pl/poradniki-do-gry/");
		commandsAndUrls.put("faumonia_discord", "https://discord.gg/sDPB6JKaVM");
		commandsAndUrls.put("hall_of_fame", "https://faumonia.pl/hall-of-fame/");
		commandsAndUrls.put("zasluzeni", "https://faumonia.pl/zasluzeni/");

		/*
		 * Baza gry
		 */
		commandsAndUrls.put("potwory", "https://faumonia.pl/potwory/");
		commandsAndUrls.put("przedmioty", "https://faumonia.pl/przedmioty/");
		commandsAndUrls.put("sklepy", "https://faumonia.pl/sklepy/");
		commandsAndUrls.put("mapy", "https://faumonia.pl/atlas-map/");
		commandsAndUrls.put("expowiska", "https://faumonia.pl/expowiska/");
		commandsAndUrls.put("zadania", "https://faumonia.pl/zadania/");

		/*
		 * Expowiska wedlug poziomu
		 */
		commandsAndUrls.put("exp_0_20", "https://faumonia.pl/expowiska-na-0-20-lvl/");
		commandsAndUrls.put("exp_20_60", "https://faumonia.pl/expowiska-na-20-60-lvl/");
		commandsAndUrls.put("exp_60_120", "https://faumonia.pl/expowiska-na-60-120-lvl/");
		commandsAndUrls.put("exp_120_200", "https://faumonia.pl/expowiska-na-120-200-lvl/");
		commandsAndUrls.put("exp_200_300", "https://faumonia.pl/expowiska-na-200-300-lvl/");
		commandsAndUrls.put("exp_300_plus", "https://faumonia.pl/expowiska-na-300-plus-lvl/");

		/*
		 * Zadania
		 */
		commandsAndUrls.put("quest_masters", "https://faumonia.pl/zadania-mistrzowie-gry/");
		commandsAndUrls.put("quest_semos", "https://faumonia.pl/zadania-miasto-semos/");
		commandsAndUrls.put("quest_ados", "https://faumonia.pl/zadania-stolica-ados/");
		commandsAndUrls.put("quest_south", "https://faumonia.pl/zadania-w-poludniowych-miastach-fado-kirdneh-kalavan/");
		commandsAndUrls.put("quest_repeatable", "https://faumonia.pl/zadania-zadania-odnawialne-na-pokonanie-potworow/");
		commandsAndUrls.put("quest_best_items", "https://faumonia.pl/zadania-na-najsilniejsze-przedmioty-w-grze/");
	}

	public static Map<String, SlashAction> createBrowserCommands() {
		initialize();

		final Map<String, SlashAction> commandsMap =
				new HashMap<String, SlashAction>();

		for (final Entry<String, String> entry : commandsAndUrls.entrySet()) {
			commandsMap.put(entry.getKey(),
					new BareBonesBrowserLaunchCommand(entry.getValue()));
		}

		return commandsMap;
	}
}
