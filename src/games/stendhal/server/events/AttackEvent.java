/* $Id: AttackEvent.java,v 1.5 2012/07/11 15:10:51 kiheru Exp $ */
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
package games.stendhal.server.events;

import games.stendhal.common.constants.Nature;
import games.stendhal.common.constants.Events;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;
import marauroa.common.game.Definition.Type;

/**
 * An RPEntity attacks another
 */
public class AttackEvent extends RPEvent {
	private static final String HIT_ATTR = "hit";
	private static final String DAMAGE_ATTR = "damage";
	private static final String DAMAGE_TYPE_ATTR = "type";
	private static final String TARGET_ATTR = "target";
	private static final String RANGED_ATTR = "ranged";
	
	/**
	 * Creates the rpclass.
	 */
	public static void generateRPClass() {
		final RPClass rpclass = new RPClass(Events.ATTACK);
		rpclass.addAttribute(HIT_ATTR, Type.FLAG);
		rpclass.addAttribute(DAMAGE_ATTR, Type.INT);
		rpclass.addAttribute(DAMAGE_TYPE_ATTR, Type.INT);
		rpclass.addAttribute(RANGED_ATTR, Type.FLAG);

		// there is a name clash with the attack event
		rpclass.addAttribute(TARGET_ATTR, Type.STRING);
	}

	/**
	 * Construct a new <code>AttackEvent</code>
	 * 
	 * @param canHit <code>false</code> for missed hits, <code>true</code> for wounding or blocked hits
	 * @param damage damage done
	 * @param type damage type of the attack
	 * @param ranged <code>true</code> if the attack is ranged, otherwise
	 * 	<code>false</code>
	 */
	public AttackEvent(boolean canHit, int damage, Nature type, boolean ranged) {
		super(Events.ATTACK);
		if (canHit) {
			put(HIT_ATTR, "");
		}
		put(DAMAGE_ATTR, damage);
		put(DAMAGE_TYPE_ATTR, type.ordinal());
		if (ranged) {
			put(RANGED_ATTR, "");
		}
	}
}
