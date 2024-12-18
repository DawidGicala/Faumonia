/* $Id: ItemEventType.java,v 1.2 2010/09/19 01:28:55 nhnb Exp $ */
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
package games.stendhal.tools.loganalyser.itemlog.contraband;

import games.stendhal.tools.loganalyser.itemlog.consistency.LogEntry;

/**
 * handles item events
 */
public interface ItemEventType {

	
	/**
	 * processes a log entry
	 *
	 * @param entry LogEntry
	 * @param info ItemInfo
	 */
	public void process(LogEntry entry, ItemInfo info);
}
