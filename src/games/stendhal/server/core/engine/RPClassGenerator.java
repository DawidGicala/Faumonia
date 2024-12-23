/* $Id: RPClassGenerator.java,v 1.43 2012/12/21 14:30:58 madmetzger Exp $ */
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
package games.stendhal.server.core.engine;

import games.stendhal.common.constants.Events;
import games.stendhal.server.entity.ActiveEntity;
import games.stendhal.server.entity.Blood;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.BabyDragon;
import games.stendhal.server.entity.creature.Cat;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.Owczarek;
import games.stendhal.server.entity.creature.OwczarekPodhalanski;
import games.stendhal.server.entity.creature.Pet;
import games.stendhal.server.entity.creature.Sheep;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.ItemInformation;
import games.stendhal.server.entity.mapstuff.ExpirationTracker;
import games.stendhal.server.entity.mapstuff.Fire;
import games.stendhal.server.entity.mapstuff.area.AreaEntity;
import games.stendhal.server.entity.mapstuff.area.WalkBlocker;
import games.stendhal.server.entity.mapstuff.block.Block;
import games.stendhal.server.entity.mapstuff.chest.Chest;
import games.stendhal.server.entity.mapstuff.game.GameBoard;
import games.stendhal.server.entity.mapstuff.office.ArrestWarrant;
import games.stendhal.server.entity.mapstuff.office.RentedSign;
import games.stendhal.server.entity.mapstuff.portal.Door;
import games.stendhal.server.entity.mapstuff.portal.Gate;
import games.stendhal.server.entity.mapstuff.portal.HousePortal;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.mapstuff.sign.Sign;
import games.stendhal.server.entity.mapstuff.sound.LoopedSoundSource;
import games.stendhal.server.entity.mapstuff.spawner.GrowingPassiveEntityRespawnPoint;
import games.stendhal.server.entity.mapstuff.spawner.PassiveEntityRespawnPoint;
import games.stendhal.server.entity.mapstuff.spawner.SheepFood;
import games.stendhal.server.entity.mapstuff.useable.FishSource;
import games.stendhal.server.entity.mapstuff.useable.GoldSource;
import games.stendhal.server.entity.mapstuff.useable.UseableEntity;
import games.stendhal.server.entity.mapstuff.useable.WaterSpringSource;
import games.stendhal.server.entity.mapstuff.useable.WellSource;
import games.stendhal.server.entity.mapstuff.useable.SourceAmetyst;
import games.stendhal.server.entity.mapstuff.useable.SourceCarbuncle;
import games.stendhal.server.entity.mapstuff.useable.SourceEmerald;
import games.stendhal.server.entity.mapstuff.useable.SourceGold;
import games.stendhal.server.entity.mapstuff.useable.SourceIron;
import games.stendhal.server.entity.mapstuff.useable.SourceMithril;
import games.stendhal.server.entity.mapstuff.useable.SourceObsidian;
import games.stendhal.server.entity.mapstuff.useable.SourceSalt;
import games.stendhal.server.entity.mapstuff.useable.SourceSapphire;
import games.stendhal.server.entity.mapstuff.useable.SourceSilver;
import games.stendhal.server.entity.mapstuff.useable.SourceSulfur;
import games.stendhal.server.entity.mapstuff.useable.WoodSource;
import games.stendhal.server.entity.npc.NPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.spell.Spell;
import games.stendhal.server.entity.trade.Earning;
import games.stendhal.server.entity.trade.Market;
import games.stendhal.server.entity.trade.Offer;
import games.stendhal.server.events.AttackEvent;
import games.stendhal.server.events.BuddyLoginEvent;
import games.stendhal.server.events.BuddyLogoutEvent;
import games.stendhal.server.events.ExamineEvent;
import games.stendhal.server.events.GroupChangeEvent;
import games.stendhal.server.events.GroupInviteEvent;
import games.stendhal.server.events.HealedEvent;
import games.stendhal.server.events.ImageEffectEvent;
import games.stendhal.server.events.PlayerLoggedOnEvent;
import games.stendhal.server.events.PlayerLoggedOutEvent;
import games.stendhal.server.events.PrivateTextEvent;
import games.stendhal.server.events.ProgressStatusEvent;
import games.stendhal.server.events.ReachedAchievementEvent;
import games.stendhal.server.events.ShowItemListEvent;
import games.stendhal.server.events.SoundEvent;
import games.stendhal.server.events.TextEvent;
import games.stendhal.server.events.TradeStateChangeEvent;
import games.stendhal.server.events.TransitionGraphEvent;
import games.stendhal.server.events.ViewChangeEvent;
import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;

/**
 * defines all RPClass
 */
public class RPClassGenerator {
	private static boolean inited = false;

	/**
	 * creates the RPClass definitions, unless this was already done.
	 */
	public void createRPClassesWithoutBaking() {
		if (inited) {
			return;
		}
		inited = true;

		if (!RPClass.hasRPClass("entity")) {
			Entity.generateRPClass();
		}

		// Entity sub-classes
		if (!RPClass.hasRPClass("active_entity")) {
			ActiveEntity.generateRPClass();
		}
		if (!RPClass.hasRPClass("area")) {
			AreaEntity.generateRPClass();
		}
		if (!RPClass.hasRPClass("blood")) {
			Blood.generateRPClass();
		}
		if (!RPClass.hasRPClass("chest")) {
			Chest.generateRPClass();
		}
		if (!RPClass.hasRPClass("corpse")) {
			Corpse.generateRPClass();
		}
		if (!RPClass.hasRPClass("door")) {
			Door.generateRPClass();
		}
		if (!RPClass.hasRPClass("expiration_tracker")) {
			ExpirationTracker.generateRPClass();
		}
		if (!RPClass.hasRPClass("fire")) {
			Fire.generateRPClass();
		}
		if (!RPClass.hasRPClass("block")) {
			Block.generateRPClass();
		}
		if (!RPClass.hasRPClass("fish_source")) {
			FishSource.generateRPClass();
		}
		if (!RPClass.hasRPClass("game_board")) {
			GameBoard.generateRPClass();
		}
		if (!RPClass.hasRPClass("gate")) {
			Gate.generateGateRPClass();
		}
		if (!RPClass.hasRPClass("gold_source")) {
			GoldSource.generateRPClass();
		}
	
		if (!RPClass.hasRPClass("water_source")) {
			WaterSpringSource.generateRPClass();
		}

		if (!RPClass.hasRPClass("well_source")) {
			WellSource.generateRPClass();
		}
		if (!RPClass.hasRPClass("source_ametyst")) {
			SourceAmetyst.generateRPClass();
		}
		if (!RPClass.hasRPClass("source_carbuncle")) {
			SourceCarbuncle.generateRPClass();
		}
		if (!RPClass.hasRPClass("source_emerald")) {
			SourceEmerald.generateRPClass();
		}
		if (!RPClass.hasRPClass("source_gold")) {
			SourceGold.generateRPClass();
		}
		if (!RPClass.hasRPClass("source_iron")) {
			SourceIron.generateRPClass();
		}
		if (!RPClass.hasRPClass("source_mithril")) {
			SourceMithril.generateRPClass();
		}
		if (!RPClass.hasRPClass("source_obsidian")) {
			SourceObsidian.generateRPClass();
		}
		if (!RPClass.hasRPClass("source_salt")) {
			SourceSalt.generateRPClass();
		}
		if (!RPClass.hasRPClass("source_sapphire")) {
			SourceSapphire.generateRPClass();
		}
		if (!RPClass.hasRPClass("source_silver")) {
			SourceSilver.generateRPClass();
		}
		if (!RPClass.hasRPClass("source_sulfur")) {
			SourceSulfur.generateRPClass();
		}
		if (!RPClass.hasRPClass("wood_source")) {
			WoodSource.generateRPClass();
		}
		if (!RPClass.hasRPClass("item")) {
			Item.generateRPClass();
		}
		if (!RPClass.hasRPClass("item_information")) {
			ItemInformation.generateRPClass();
		}
		if (!RPClass.hasRPClass("plant_grower")) {
			PassiveEntityRespawnPoint.generateRPClass();
		}
		if (!RPClass.hasRPClass("portal")) {
			Portal.generateRPClass();
		}
		if (!RPClass.hasRPClass("sign")) {
			Sign.generateRPClass();
		}
		if (!RPClass.hasRPClass("spell")) {
			Spell.generateRPClass();
		}
		if (!RPClass.hasRPClass("wallblocker")) {
			WalkBlocker.generateRPClass();
		}
		if (!RPClass.hasRPClass("house_portal")) {
			HousePortal.generateRPClass();
		}
		if (!RPClass.hasRPClass("useable_entity")) {
			UseableEntity.generateRPClass();
		}
		// ActiveEntity sub-classes
		if (!RPClass.hasRPClass("rpentity")) {
			RPEntity.generateRPClass();
		}

		// RPEntity sub-classes
		if (!RPClass.hasRPClass("npc")) {
			NPC.generateRPClass();
		}
		if (!RPClass.hasRPClass("player")) {
			Player.generateRPClass();
		}

		// NPC sub-classes
		if (!RPClass.hasRPClass("creature")) {
			Creature.generateRPClass();
		}

		// Creature sub-classes
		if (!RPClass.hasRPClass("sheep")) {
			Sheep.generateRPClass();
		}
		if (!RPClass.hasRPClass("pet")) {
			Pet.generateRPClass();
		}
		if (!RPClass.hasRPClass("cat")) {
			Cat.generateRPClass();
		}
		if (!RPClass.hasRPClass("baby_dragon")) {
			BabyDragon.generateRPClass();
		}
		if (!RPClass.hasRPClass("owczarek")) {
			Owczarek.generateRPClass();
		}
		if (!RPClass.hasRPClass("owczarek_podhalanski")) {
			OwczarekPodhalanski.generateRPClass();
		}

		// PassiveEntityRespawnPoint sub-class
		if (!RPClass.hasRPClass("ambient_sound_source")) {
			LoopedSoundSource.generateRPClass();
		}
		if (!RPClass.hasRPClass("growing_entity_spawner")) {
			GrowingPassiveEntityRespawnPoint.generateRPClass();
		}
		if (!RPClass.hasRPClass("food")) {
			SheepFood.generateRPClass();
		}

		// zone storage
		if (!RPClass.hasRPClass("arrest_warrant")) {		
			ArrestWarrant.generateRPClass();
		}
		if (!RPClass.hasRPClass("rented_sign")) {
			RentedSign.generateRPClass();
		}
		if (!RPClass.hasRPClass(Market.MARKET_RPCLASS_NAME)) {
			Market.generateRPClass();
		}
		if (!RPClass.hasRPClass(Offer.OFFER_RPCLASS_NAME)) {
			Offer.generateRPClass();
		}
		if (!RPClass.hasRPClass(Earning.EARNING_RPCLASS_NAME)) {
			Earning.generateRPClass();
		}

		// rpevents
		if (!RPClass.hasRPClass(Events.ATTACK)) {
			AttackEvent.generateRPClass();
		}
		if (!RPClass.hasRPClass("buddy_login")) {
			BuddyLoginEvent.generateRPClass();
		}
		if (!RPClass.hasRPClass("buddy_logout")) {
			BuddyLogoutEvent.generateRPClass();
		}
		if (!RPClass.hasRPClass("examine")) {
			ExamineEvent.generateRPClass();
		}
		if (!RPClass.hasRPClass("healed")) {
			HealedEvent.generateRPClass();
		}
		if (!RPClass.hasRPClass(Events.IMAGE)) {
			ImageEffectEvent.generateRPClass();
		}
		if (!RPClass.hasRPClass(Events.PRIVATE_TEXT)) {
			PrivateTextEvent.generateRPClass();
		}
		if (!RPClass.hasRPClass(Events.PROGRESS_STATUS_CHANGE)) {
			ProgressStatusEvent.generateRPClass();
		}
		if (!RPClass.hasRPClass("show_item_list")) {
			ShowItemListEvent.generateRPClass();
		}
		if (!RPClass.hasRPClass(Events.SOUND)) {
			SoundEvent.generateRPClass();
		}
		if (!RPClass.hasRPClass(Events.PUBLIC_TEXT)) {
			TextEvent.generateRPClass();
		}
		if (!RPClass.hasRPClass(Events.GROUP_CHANGE)) {
			GroupChangeEvent.generateRPClass();
		}
		if (!RPClass.hasRPClass(Events.GROUP_INVITE)) {
			GroupInviteEvent.generateRPClass();
		}
		if (!RPClass.hasRPClass("transition_graph")) {
			TransitionGraphEvent.generateRPClass();
		}
		if (!RPClass.hasRPClass(Events.TRADE_STATE_CHANGE)) {
			TradeStateChangeEvent.generateRPClass();
		}

		if (!RPClass.hasRPClass(Events.PLAYER_LOGGED_ON)) {
			PlayerLoggedOnEvent.generateRPClass();
		}
		
		if (!RPClass.hasRPClass(Events.PLAYER_LOGGED_OUT)) {
			PlayerLoggedOutEvent.generateRPClass();
		}

		if (!RPClass.hasRPClass(Events.REACHED_ACHIEVEMENT)) {
			ReachedAchievementEvent.generateRPClass();
		}
		
		if (!RPClass.hasRPClass(Events.VIEW_CHANGE)) {
			ViewChangeEvent.generateRPClass();
		}

		if (!RPClass.hasRPClass("chat")) {
			createChatActionRPClass();
			createCidActionRPClass();
			createMoveToActionRPClass();
			createTellActionRPClass();
		}
	}

	/**
	 * creates the RPClass definitions, unless this was already done.
	 */
	public void createRPClasses() {
		if (inited) {
			return;
		}
		createRPClassesWithoutBaking();
		RPClass.bakeAll();
	}

	private void createChatActionRPClass() {
		RPClass chatAction = new RPClass("chat");
		chatAction.add(DefinitionClass.ATTRIBUTE, "type", Type.STRING);
		chatAction.add(DefinitionClass.ATTRIBUTE, "text", Type.LONG_STRING);
	}

	private void createCidActionRPClass() {
		RPClass action = new RPClass("cstatus");
		action.addAttribute("cid", Type.STRING);
		action.addAttribute("version", Type.STRING);
		action.addAttribute("build", Type.STRING);
		action.addAttribute("dist", Type.STRING);
	}

	private void createMoveToActionRPClass() {
		RPClass action = new RPClass("moveto");
		action.addAttribute("double_click", Type.FLAG);
		action.addAttribute("extend", Type.INT);
		action.addAttribute("x", Type.INT);
		action.addAttribute("y", Type.INT);
	}

	private void createTellActionRPClass() {
		RPClass chatAction;
		chatAction = new RPClass("tell");
		chatAction.add(DefinitionClass.ATTRIBUTE, "type", Type.STRING);
		chatAction.add(DefinitionClass.ATTRIBUTE, "text", Type.LONG_STRING);
		chatAction.add(DefinitionClass.ATTRIBUTE, "target", Type.LONG_STRING);
	}
}
