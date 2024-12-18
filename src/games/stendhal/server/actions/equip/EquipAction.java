/* $Id: EquipAction.java,v 1.16 2013/01/06 16:23:53 nhnb Exp $ */
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
package games.stendhal.server.actions.equip;

import games.stendhal.common.Constants;
import games.stendhal.common.EquipActionConsts;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.actions.admin.AdministrationAction;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;

import org.apache.commons.lang.ArrayUtils;

public class EquipAction extends EquipmentAction {

	/**
	 * registers "equip" action processor.
	 */
	public static void register() {
		CommandCenter.register("equip", new EquipAction());
	}
	@Override
	protected void execute(final Player player, final RPAction action, final SourceObject source) {
		// get source and check it
	
		logger.debug("Getting entity name");
		// is the entity unbound or bound to the right player?
		final Entity entity = source.getEntity();
		final String itemName = source.getEntityName();

		logger.debug("Checking if entity is bound");
		if (entity instanceof Item) {
			final Item item = (Item) entity;
			if (item.isBound() && !player.isBoundTo(item) && player.getAdminLevel() < 7) {
				player.sendPrivateText("Oto " + itemName
						+ " jest specjalną nagrodą dla " + item.getBoundTo()
						+ ". Nie zasługujesz na to, aby jej używać.");
				return;
			} else if (player.getAdminLevel() > 6) {
			}
			
		}

		logger.debug("Checking destination");
		// get destination and check it
		final DestinationObject dest = new DestinationObject(action, player);
		if (dest.isInvalidMoveable(player, EquipActionConsts.MAXDISTANCE, validContainerClassesList)) {
			// destination is not valid
			logger.debug("Destination is not valid");
			return;
		}

		// List of slots which require minimum level
		if (dest.slot.equals("neck") || dest.slot.equals("head")
				|| dest.slot.equals("cloak") || dest.slot.equals("lhand")
				|| dest.slot.equals("armor") || dest.slot.equals("rhand")
				|| dest.slot.equals("finger") || dest.slot.equals("legs")
				|| dest.slot.equals("fingerb") || dest.slot.equals("feet")
				|| dest.slot.equals("glove") ||	dest.slot.equals("medal"))		{
			logger.debug("Checking required level for items");
			// check required level for item in character's window
			if (entity.has("minlevel")
					&& player.getLevel() < entity.getInt("minlevel")) {
				player.sendPrivateText("Nie jesteś wystarczająco doświadczony, aby używać "
						+ itemName + ". Wymagany jest poziom " + entity.getInt("minlevel"));
				return;
			}
		}
		logger.debug("Equip action agreed");
	
		// looks good
		if (source.moveTo(dest, player)) {
			int amount = source.getQuantity();

			// Warn about min level
			if (player.equals(dest.parent)
					&& ArrayUtils.contains(Constants.CARRYING_SLOTS, dest.slot)
					&& !"bag".equals(dest.slot)) {
				if(entity instanceof Item) {
					int minLevel = ((Item) entity).getMinLevel();
					if (minLevel > player.getLevel()) {
						player.sendPrivateText("Nie jesteś wystarczająco doświadczony, aby w pełni używać tego przedmiotu. Najlepiej dla ciebie będzie nie używanie tego przedmiotu przy twoim poziomie.");
					}
				}
			}

			// players sometimes accidentally drop items into corpses, so inform about all drops into a corpse 
			// which aren't just a movement from one corpse to another.
			// we could of course specifically preclude dropping into corpses, but that is undesirable.
			if (dest.isContainerCorpse() && !source.isContainerCorpse()) {
					player.sendPrivateText("Dla Twojej wiadomości. Właśnie wyrzuciłeś " 
							+ Grammar.quantityplnounWithHash(amount,entity.getTitle()) + " do zwłok, nad którymi właśnie stoisz.");
			}
			
			if(source.isLootingRewardable()) {
				if(entity instanceof Item) {
					((Item) entity).setFromCorpse(false);
				}
				player.incLootForItem(entity.getTitle(), amount);
				SingletonRepository.getAchievementNotifier().onItemLoot(player);
			}
			if (entity instanceof Item) {
				((Item) entity).autobind(player.getName());
			}

			new GameEvent(player.getName(), "equip", itemName, source.getSlot(), dest.getSlot(), Integer.toString(amount)).raise();
	
			player.updateItemAtkDef();
		}
	}

}
