/* $Id: Seed.java,v 1.10 2010/11/30 19:40:54 nhnb Exp $ */
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
package games.stendhal.server.entity.item;

import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.spawner.FlowerGrower;

import java.util.Map;

/**
 * A seed can be planted. 
 * The plant action defines the behaviour (e.g. only plantable on fertile ground).
 * The infostring stores what it will grow.
 */
public class Seed extends StackableItem implements UseListener {

	public Seed(final Seed item) {
		super(item);
	}

	/**
	 * Creates a new seed
	 * 
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public Seed(final String name, final String clazz, final String subclass, final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	public boolean onUsed(final RPEntity user) {
		if (!this.isContained()) {
			// the seed is on the ground, but not next to the player
			if (!this.nextTo(user)) {
				user.sendPrivateText("" + this.getName() + " jest zbyt daleko");
				return false;
	}

			// the infostring of the seed stores what it should grow
			final String infostring = this.getInfoString();
			FlowerGrower flowerGrower;
			// choose the default flower grower if there is none set
			if (infostring == null) {
				flowerGrower = new FlowerGrower();
			} else {
				flowerGrower = new FlowerGrower(this.getInfoString());
			}
			user.getZone().add(flowerGrower);
			// add the FlowerGrower where the seed was on the ground
			flowerGrower.setPosition(this.getX(), this.getY());
			// The first stage of growth happens almost immediately        
			TurnNotifier.get().notifyInTurns(3, flowerGrower);
			// remove the seed now that it is planted
			this.removeOne();
			return true;
		}
		// the seed was 'contained' in a slot and so it cannot be planted
		user.sendPrivateText("Musisz zasadzić " + this.getName() + " w ziemi, aby wyrosło głuptasie!");
		return false;
	}

	@Override
	public String describe() {
		final String flowerName = getInfoString();

		if (flowerName != null) {
			return "Oto " + flowerName + " " + this.getName() + "."
                + " Może być zasadzone wszędzie, ale kwitnąć może tylko na żyznej glebie.";
		} else {
			return "Oto nasionko. Może być zasadzone wszędzie, ale kwitnąć może tylko na żyznej glebie.";
		}
	}
}
