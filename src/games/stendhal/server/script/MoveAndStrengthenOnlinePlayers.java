/* $Id: MoveAndStrengthenOnlinePlayers.java,v 1.11 2011/08/05 14:58:35 madmetzger Exp $ */
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
package games.stendhal.server.script;

import games.stendhal.common.Direction;
import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.PlayerList;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.Task;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import marauroa.common.game.IRPZone;
/**
 * Script to make all players stronger and immune to poison before randomly distributing them 
 * over all zones of the running server
 *  
 * @author madmetzger
 */
public class MoveAndStrengthenOnlinePlayers extends ScriptImpl {
	
	private List<StendhalRPZone> zones = new ArrayList<StendhalRPZone>();
	
	/**
	 * Create the script and initialize the list of zones
	 */
	public MoveAndStrengthenOnlinePlayers() {
		StendhalRPWorld rpWorld = SingletonRepository.getRPWorld();
		for (IRPZone irpZone : rpWorld) {
			StendhalRPZone irpZone2 = (StendhalRPZone) irpZone;
			if (!irpZone2.getName().startsWith("int")) {
				zones.add(irpZone2);
			}
		}
	}

	@Override
	public void execute(final Player admin, List<String> args) {
		Collection<Player> onlinePlayers = SingletonRepository.getRuleProcessor().getOnlinePlayers().getAllPlayers();
		PlayerList pl = new PlayerList();
		int packet = 1;
		for (Player p : onlinePlayers) {
			pl.add(p);
			if(pl.getAllPlayers().size() == 5) {
				SingletonRepository.getTurnNotifier().notifyInTurns(packet, new MoveAndStrengthenPlayersTurnListener(pl, admin));
				pl = new PlayerList();
				packet += 1;
			}
		}
		
	}
	
	private class MoveAndStrengthenPlayersTurnListener implements TurnListener {
		
		private final PlayerList playersToDealWith;
		
		private final Player admin;
		
		MoveAndStrengthenPlayersTurnListener(PlayerList pl, Player executor) {
			playersToDealWith = pl;
			admin = executor;
		}

		public void onTurnReached(int currentTurn) {
			playersToDealWith.forAllPlayersExecute(new Task<Player>() {

				public void execute(Player player) {
					equipPlayer(player);
					fillBag(player);
					player.setDefXP(999999999);
					player.addXP(999999999);
					player.setImmune();
					StendhalRPZone zone = zones.get(Rand.rand(zones.size()));
					int x = Rand.rand(zone.getWidth() - 4) + 2;
					int y = Rand.rand(zone.getHeight() - 5) + 2;
					player.teleport(zone, x, y, Direction.DOWN, admin);
				}

				private void fillBag(Player player) {
					String[] items = {"por", "borowik", "eliksir", "antidotum", "sok z chmielu", "mały eliksir", "zwój semos", "zwój ados", "niezapisany zwój"};
					for(String item : items) {
						StackableItem stackable = (StackableItem) SingletonRepository.getEntityManager().getItem(item);
						stackable.setQuantity(50);
						player.equipToInventoryOnly(stackable);
					}
				}

				private void equipPlayer(Player player) {
					StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
					money.setQuantity(5000);
					player.equipToInventoryOnly(money);
					StackableItem potions = (StackableItem) SingletonRepository.getEntityManager().getItem("duży eliksir");
					potions.setQuantity(5000);
					player.equipToInventoryOnly(potions);
					if(!player.isEquipped("sztylet chaosu")) {
						Item first = (Item) player.getSlot("rhand").getFirst();
						player.drop(first);
						Item dagger = SingletonRepository.getEntityManager().getItem("sztylet chaosu");
						player.equip("rhand", dagger);
					}
					if(!player.isEquipped("tarcza chaosu")) {
						Item first = (Item) player.getSlot("lhand").getFirst();
						player.drop(first);
						Item shield = SingletonRepository.getEntityManager().getItem("tarcza chaosu");
						player.equip("lhand", shield);
					}
					if(!player.isEquipped("czarny hełm")) {
						Item first = (Item) player.getSlot("head").getFirst();
						player.drop(first);
						Item helmet = SingletonRepository.getEntityManager().getItem("czarny hełm");
						player.equip("head", helmet);
					}
					if(!player.isEquipped("spodnie elfickie")) {
						Item first = (Item) player.getSlot("legs").getFirst();
						player.drop(first);
						Item legs = SingletonRepository.getEntityManager().getItem("spodnie elfickie");
						player.equip("legs", legs);
					}
					if(!player.isEquipped("buty zabójcy")) {
						Item first = (Item) player.getSlot("feet").getFirst();
						player.drop(first);
						Item boots = SingletonRepository.getEntityManager().getItem("buty zabójcy");
						player.equip("feet", boots);
					}
					if(!player.isEquipped("szmaragdowy płaszcz smoczy")) {
						Item first = (Item) player.getSlot("cloak").getFirst();
						player.drop(first);
						Item cloak = SingletonRepository.getEntityManager().getItem("szmaragdowy płaszcz smoczy");
						player.equip("cloak", cloak);
					}
				}
			});
		}
		
	}

}
