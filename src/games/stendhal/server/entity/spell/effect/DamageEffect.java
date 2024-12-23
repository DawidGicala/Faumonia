/* $Id: DamageEffect.java,v 1.14 2012/07/11 15:10:51 kiheru Exp $ */
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
package games.stendhal.server.entity.spell.effect;

import games.stendhal.common.constants.Nature;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnListenerDecorator;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.AttackEvent;

import org.apache.log4j.Logger;
/**
 * An effect to cause magical damage with a spell
 * 
 * Used attributes:
 * - amount: How often will this effect hit a player
 * - atk: for usage of the usual damage calcuation acting as a weapon
 * - lifesteal: percentage of health points healed based on damage done
 * 
 * @author madmetzger
 */
public class DamageEffect extends AbstractEffect implements TurnListener {

	private static final Logger LOGGER = Logger.getLogger(DamageEffect.class);

	/** the entity getting damaged */
	private RPEntity rpEntityToDamage;
	
	/** the player issuing the effect */
	private Player damageOrigin;

	private int numberOfLeftOverHits;

	public DamageEffect(Nature nature, int amount, int atk, int def,
			double lifesteal, int rate, int regen, double modifier) {
		super(nature, amount, atk, def, lifesteal, rate, regen, modifier);
	}

	public void act(Player caster, Entity target) {
		if (target instanceof RPEntity) {
		actInternal(caster, (RPEntity) target);
		} else {
			LOGGER.error("target is no instance of RPEntitty but: " + target, new Throwable());
		}
	}
	
	public void onTurnReached(int currentTurn) {
		if(numberOfLeftOverHits > 0 && rpEntityToDamage.getHP() > 0) {
			
			int damageDone = damageOrigin.damageDone(rpEntityToDamage, getAtk(), getNature());
			damageDone = Math.min(damageDone, rpEntityToDamage.getBaseHP());
			int toSteal = (int) Math.ceil(damageDone * Double.valueOf(getLifesteal()));
			
			if(damageDone > 0) {
				rpEntityToDamage.onDamaged(damageOrigin, damageDone);
				damageOrigin.addEvent(new AttackEvent(true, damageDone, getNature(), true));
			}
			
			damageOrigin.heal(toSteal);
			numberOfLeftOverHits = numberOfLeftOverHits -1;
			if (numberOfLeftOverHits > 0 && rpEntityToDamage.getHP() > 0) {
				SingletonRepository.getTurnNotifier().notifyInTurns(getRate(), new TurnListenerDecorator(this));
			}
		}
	}

	
	private void actInternal(Player caster, RPEntity target) {
		// remember caster and target
		rpEntityToDamage = target;
		damageOrigin = caster;
		numberOfLeftOverHits = this.getAmount();
		// use turn notifier to enable for damage over a certain amount of time
		SingletonRepository.getTurnNotifier().notifyInTurns(0, new TurnListenerDecorator(this));
	}

}
