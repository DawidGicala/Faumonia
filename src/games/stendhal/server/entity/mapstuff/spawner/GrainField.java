/* $Id: GrainField.java,v 1.20 2010/12/10 20:52:45 madmetzger Exp $ */
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
package games.stendhal.server.entity.mapstuff.spawner;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

import java.util.LinkedList;
import java.util.List;

/**
 * A grain field can be harvested by players who have a scythe. After that, it
 * will slowly regrow; there are several regrowing steps in which the graphics
 * will change to show the progress.
 * 
 * @author daniel
 */
public class GrainField extends GrowingPassiveEntityRespawnPoint implements
		UseListener {
	/** How many growing steps are needed before one can harvest again. */
	public static final int RIPE = 5;
	
	/** Resistance of fully grown grain */
	private static final int MAX_RESISTANCE = 80;

	private String grainName;
	private List<String> tools;

	protected String getGrainName() {
		return grainName;
	}

	/**
	 * creates a new GrainField
	 *
	 * @param name   name of the field
	 * @param tools  list of tool that can be used to harvest. 
	 *               The first one will be used in the message to the player
	 *               if he is missing a suitable tool.
	 */
	public GrainField(final String name, final String grainview, final List<String> tools) {
		super(grainview + "_field", grainview + " field", "Harvest", RIPE, 1, 1);
		this.tools = new LinkedList<String>(tools);
		grainName = name;
	}

	/**
	 * Sets the ripeness
	 */
	@Override
	protected void setRipeness(final int ripeness) {
		super.setRipeness(ripeness);
		updateResistance();
	}

	/**
	 * Set resistance according the current ripeness.
	 */
	private void updateResistance() {
		// Ripeness starts from 0. Give it a tiny bit of resistance for
		// walking over newly ploughed ground.
		setResistance((getRipeness() + 1) * MAX_RESISTANCE / (RIPE + 1));
	}

	@Override
	public String describe() {
		String text;
		switch (getRipeness()) {
		case 0:
			text = "Oto pole, gdzie rośnie " + grainName + ", które dopiero co zostało skoszone.";
			break;
		case RIPE:
			text = "Oto skoszone pole, gdzie rośnie " + grainName + ".";
			break;
		default:
			text = "Oto nieskoszone pole, gdzie rośnie " + grainName + ".";
			break;
		}
		return text;
	}

	/**
	 * Is called when a player tries to harvest this grain field.
	 * @param entity the harvesting entity
	 * @return true if successful
	 */
	public boolean onUsed(final RPEntity entity) {
		if (!entity.nextTo(this)) {
			entity.sendPrivateText("Nie możesz stąd dosięgnąć pola, gdzie rośnie " + grainName + ".");
			return false;
		}

		if (getRipeness() != RIPE) {
			entity.sendPrivateText("" + grainName + " nie jest jeszcze gotowe do ścięcia.");
			return false;
		}

		if (!isNeededToolEquipped(entity)) {
			entity.sendPrivateText("Potrzebujesz " + tools.get(0) +", aby ściąć pole, gdzie rośnie " + grainName + ".");
			return false;
		}

		onFruitPicked(null);
		final Item grain = SingletonRepository.getEntityManager().getItem(grainName);
		entity.equipOrPutOnGround(grain);
		if(entity instanceof Player) {
			((Player) entity).incHarvestedForItem(grainName, 1);
			SingletonRepository.getAchievementNotifier().onObtain((Player) entity);
		}
		return true;
	}

	/**
	 * Checks whether one of the needed tools is equipped
	 *
	 * @param entity RPEntity to check
	 * @return true if a suitable tool is equiped; false otherwise.
	 */
	private boolean isNeededToolEquipped(RPEntity entity) {
		for (String tool : tools) {
			if (entity.isEquipped(tool)) {
				return true;
			}
		}
		return false;
	}
}
