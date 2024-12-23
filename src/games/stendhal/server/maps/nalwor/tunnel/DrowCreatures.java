/* $Id: DrowCreatures.java,v 1.8 2010/11/24 23:45:18 martinfuchs Exp $ */
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
package games.stendhal.server.maps.nalwor.tunnel;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.CircumstancesOfDeath;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.magic.school.SpidersCreatures;
import games.stendhal.server.util.Area;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

/**
 * Configure Drow Tunnel -1 to include a Thing Creature who carries an amulet. 
 * Then it should give an amulet that is bound to the player.
 */
public class DrowCreatures implements ZoneConfigurator {
	private static final String QUEST_SLOT="kill_dark_elves";
	// at the beginning places there must be creatures from DarkElvesCreatures.class
	private final List<String> creatures = 
		Arrays.asList("elf ciemności kapitan",
				          "elf ciemności generał",
				          "elf ciemności rycerz",
				          "elf ciemności czarownik",
				          "elf ciemności czarnoksiężnik",
				          "elf ciemności królewicz",
				          "elf ciemności matrona",
					        "elf ciemności łucznik elitarny",
				          "elf ciemności łucznik");
	
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildSecretRoomArea(zone, attributes);
	}
	
	/**
	 * function will fill information about victim to killer's quest slot.
	 * @param circ - information about victim,zone and killer.
	 */
	private void updatePlayerQuest(final CircumstancesOfDeath circ) {
		final RPEntity killer = circ.getKiller();
		final String victim = circ.getVictim().getName();
		Logger.getLogger(SpidersCreatures.class).debug(
				"w "+circ.getZone().getName()+
				": "+circ.getVictim().getName()+
				" zabity przez "+circ.getKiller().getName());
		
		// check if was killed by other animal/pet
		if(!circ.getKiller().getClass().getName().equals(Player.class.getName()) ) {
			return;
		}
		final Player player = (Player) killer;
		// check if player started his quest already
		if(!player.hasQuest(QUEST_SLOT) || !player.getQuest(QUEST_SLOT, 0).equals("started")) {
			return;
		}
		int slot=creatures.indexOf(victim);
		if(slot!=-1) {
			player.setQuest(QUEST_SLOT, 1+slot, victim);
		}
	}
	
	class DrowObserver implements Observer {
		public void update(Observable o, Object arg) {
			updatePlayerQuest((CircumstancesOfDeath) arg);
		}
	}

		private void buildSecretRoomArea(final StendhalRPZone zone, final Map<String, String> attributes) {
		Observer observer = new DrowObserver();
		
		// describe secret room tunnel here
		final Area a1 = new Area(zone, 33, 50, 10, 20);
		final Area a2 = new Area(zone, 23, 0,  21, 49);
		
		for(CreatureRespawnPoint p:zone.getRespawnPointList()) {
			if(p!=null) {
				if(a1.contains(p) || a2.contains(p)) {
					if(creatures.indexOf(p.getPrototypeCreature().getName())!=-1) {
						// it is our creature, set up observer now
						p.addObserver(observer);					
					}
				}				
			}
		}
	}
}
