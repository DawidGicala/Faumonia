/* $Id: UseListener.java,v 1.5 2011/09/06 17:29:51 nhnb Exp $ */
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
package games.stendhal.server.core.events;

import games.stendhal.server.entity.RPEntity;

/**
 * Implementing classes will be called back when a player uses them.
 */
public interface UseListener {

	/**
	 * Invoked when the object is used.
	 * 
	 * @param user the RPEntity who uses the object
	 * @return true if successful
	 */
	boolean onUsed(RPEntity user);
}
