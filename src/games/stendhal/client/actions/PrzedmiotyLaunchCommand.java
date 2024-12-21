/* $Id: AtlasBrowserLaunchCommand.java,v 1.1 2011/08/04 21:17:04 nhnb Exp $ */
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

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.entity.User;
import games.stendhal.client.gui.BareBonesBrowserLaunch;
import games.stendhal.client.gui.chatlog.HeaderLessEventLine;
import games.stendhal.common.NotificationType;

/**
 * opens the atlas at the current position in the browser.
 *
 * @author hendrik
 */
public class PrzedmiotyLaunchCommand implements SlashAction{

	/**
	 * Opens the atlas URL at the current position
	 * 
	 * @param params ignored
	 * @param remainder ignored
	 * @return <code>true</code>
	 */
	public boolean execute(final String[] params, final String remainder) {
		StringBuilder url = new StringBuilder();
		User user = User.get();
		url.append("" + System.getProperty("user.dir") + "/lib/temp/item.html");
	
		
		String urlString = url.toString();
		ClientSingletonRepository.getUserInterface().addEventLine(new HeaderLessEventLine(
				"Próbuję otworzyć adres #" + urlString + " w twojej przeglądarce internetowej. ",
		NotificationType.CLIENT));
		BareBonesBrowserLaunch.openURL(urlString);
		return true;
	}

	/**
	 * Get the maximum number of formal parameters.
	 * 
	 * @return The parameter count.
	 */
	public int getMaximumParameters() {
		return 0;
	}

	/**
	 * Get the minimum number of formal parameters.
	 * 
	 * @return The parameter count.
	 */
	public int getMinimumParameters() {
		return 0;
	}

}