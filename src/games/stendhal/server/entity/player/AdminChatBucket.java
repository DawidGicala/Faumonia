/* $Id: AdminChatBucket.java,v 1.3 2012/02/26 10:56:49 nhnb Exp $ */
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
package games.stendhal.server.entity.player;

/**
 * a special PlayerChatBucket for admins which does not limit the message
 * (postman generates at least twice the amount of manages than the attacker).
 *
 * @author hendrik
 */
public class AdminChatBucket extends PlayerChatBucket {

	@Override
	public boolean checkAndAdd(int count) {
		return true;
	}
	
}
