/* $Id: VampireLordCreature.java,v 1.4 2010/09/19 02:35:39 nhnb Exp $ */
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
package games.stendhal.server.maps.semos.catacombs;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.ItemGuardCreature;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;

import java.util.Map;

/**
 * Configure Catacombs to include a Vampire lord Creature who carries a skull ring. 
 * Then it should give an skull ring that is bound to the player, only while they have that quest active.
 */
public class VampireLordCreature implements ZoneConfigurator {
	
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildDrowTunnelArea(zone, attributes);
	}

	private void buildDrowTunnelArea(final StendhalRPZone zone, final Map<String, String> attributes) {
		final EntityManager manager = SingletonRepository.getEntityManager();
		final Creature creature = new ItemGuardCreature(manager.getCreature("lord wampir"), "pierścień z czaszką", "vs_quest", "start");
		final CreatureRespawnPoint point = new CreatureRespawnPoint(zone, 30, 11, creature, 1);
		zone.add(point);
	}
}
