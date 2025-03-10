/* $Id: StandardHeaderedEventLine.java,v 1.2 2010/09/19 02:18:31 nhnb Exp $ */
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
package games.stendhal.client.gui.chatlog;

import games.stendhal.common.NotificationType;

public class StandardHeaderedEventLine extends EventLine {

	public StandardHeaderedEventLine(final String header, final String text) {
		// color font
		super(header, text, NotificationType.NORMAL);
	}

}
