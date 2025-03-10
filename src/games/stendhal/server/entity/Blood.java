/* $Id: Blood.java,v 1.40 2010/12/05 14:10:10 martinfuchs Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity;

import games.stendhal.common.MathHelper;
import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import marauroa.common.game.RPClass;
import marauroa.common.game.Definition.Type;

/**
 * Represents a blood puddle that is left on the ground after an entity was
 * injured or killed.
 */
public class Blood extends PassiveEntity {
	/**
	 * Blood will disappear after so many seconds.
	 */
	public static final int DEGRADATION_TIMEOUT = 10 * MathHelper.SECONDS_IN_ONE_MINUTE; 

	public static void generateRPClass() {
		final RPClass blood = new RPClass("blood");
		blood.isA("entity");
		blood.addAttribute("class", Type.STRING);
		blood.addAttribute("amount", Type.BYTE);
	}

	private TurnListener turnlistener = new TurnListener() {

		public void onTurnReached(final int currentTurn) {
			Blood.this.onTurnReached(currentTurn);
		}
		
	};

	@Override
	public void onRemoved(final StendhalRPZone zone) {
		SingletonRepository.getTurnNotifier().dontNotify(turnlistener);
		super.onRemoved(zone);
	}
	
	/**
	 * Create a blood entity.
	 */
	public Blood() {
		this("red", Rand.rand(4));
	}

	/**
	 * Create a blood entity.
	 * 
	 * @param clazz
	 *            The class of blood.
	 * @param amount
	 *            The amount of blood.
	 */
	public Blood(final String clazz, final int amount) {
		setRPClass("blood");
		put("type", "blood");
		setEntityClass(clazz);
		put("amount", amount);

		SingletonRepository.getTurnNotifier().notifyInSeconds(DEGRADATION_TIMEOUT, this.turnlistener);
	}

	//
	// Entity
	//

	/**
	 * Get the entity description.
	 * 
	 * @return The description text.
	 */
	@Override
	public String describe() {
		return ("Karmazynowa plama krwi zbrukała to miejsce.");
	}

	//
	// TurnListener
	//

	/**
	 * This method is called when the turn number is reached.
	 * 
	 * @param currentTurn
	 *            The current turn number.
	 */
	public void onTurnReached(final int currentTurn) {
		getZone().remove(this);
	}
}
