/* $Id: Sheep.java,v 1.111 2011/01/03 18:34:02 kymara Exp $ */
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
package games.stendhal.server.entity.creature;

import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.pathfinder.Path;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.mapstuff.spawner.SheepFood;
import games.stendhal.server.entity.player.Player;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;
import marauroa.common.game.SyntaxException;
import marauroa.common.game.Definition.Type;

import org.apache.log4j.Logger;

/**
 * A sheep is a domestic animal that can be owned by a player. It eats berries
 * from bushes and can be sold.
 */
/**
 * @author Daniel Herding
 *
 */
public class Sheep extends DomesticAnimal {

	/** the logger instance. */
	private static final Logger logger = Logger.getLogger(Sheep.class);

	/**
	 * The amount of hunger that indicates hungry.
	 */
	protected static final int HUNGER_HUNGRY = 50;

	/**
	 * The amount of hunger that indicates extremely hungry.
	 */
	protected static final int HUNGER_EXTREMELY_HUNGRY = 500;

	/**
	 * The amount of hunger that indicates starvation.
	 */
	protected static final int HUNGER_STARVATION = 1000;

	/**
	 * The weight at which the sheep will stop eating.
	 */
	public static final int MAX_WEIGHT = 100;

	private static final int HP = 30;

	private static final int ATK = 5;

	private static final int DEF = 15;

	private static final int XP = 0;

	/**
	 * Random timing offset to give sheep non-synchronized reactions.
	 */
	private final int timingAdjust;

	private int hunger;

	@Override
	public void setAttackStrategy(final Map<String, String> aiProfiles) {
		final Map<String, String> sheepProfile = new HashMap<String, String>();
		sheepProfile.put("gandhi", null);
		super.setAttackStrategy(aiProfiles);
	}

	public static void generateRPClass() {
		try {
			final RPClass sheep = new RPClass("sheep");
			sheep.isA("creature");
			sheep.addAttribute("weight", Type.BYTE);
		} catch (final SyntaxException e) {
			logger.error("cannot generate RPClass", e);
		}
	}

	/**
	 * Creates a new wild Sheep.
	 */
	public Sheep() {
		this(null);

		// set the default perception range for enemy detection
		setPerceptionRange(20);

		// set the default movement range
		setMovementRange(20);
	}

	/**
	 * Creates a new Sheep that is owned by a player.
	 * @param owner
	 */
	public Sheep(final Player owner) {
		super();
		super.setOwner(owner);
		setRPClass("sheep");
		put("type", "sheep");

		setAtk(ATK);
		setDef(DEF);
		setXP(XP);
		initHP(HP);
		baseSpeed = 0.25;
		hunger = 0;
		timingAdjust = Rand.rand(10);

		if (owner != null) {
			// add sheep to zone and create RPID to be used in setSheep()
			owner.getZone().add(this);
			owner.setSheep(this);
		}
		
		update();
		logger.debug("Created Sheep: " + this);
	}

	/**
	 * Creates a Sheep based on an existing sheep RPObject, and assigns it to a
	 * player.
	 *
	 * @param object
	 * @param owner
	 *            The player who should own the sheep
	 */
	public Sheep(final RPObject object, final Player owner) {
		super(object, owner);

		setRPClass("sheep");
		put("type", "sheep");
		baseSpeed = 0.25;
		hunger = 0;
		timingAdjust = Rand.rand(10);

		if (owner != null) {
			// add sheep to zone and create RPID to be used in setSheep()
			owner.getZone().add(this);
			owner.setSheep(this);
		}

		update();
		logger.debug("Created Sheep: " + this);
	}

	/**
	 * Is called when the sheep dies. Removes the dead sheep from the owner.
	 *
	 */
	@Override
	public void onDead(final Entity killer, final boolean remove) {

		cleanUpSheep();

		super.onDead(killer, remove);
	}

	private void cleanUpSheep() {
		if (owner != null) {
			if (owner.hasSheep()) {
				owner.removeSheep(this);
			} else {
				logger.debug("INCOHERENCE: Sheep " + this + " isn't owned by " + owner);
			}
		}
	}

	/**
	 * Returns a list of SheepFood in the given range ordered by distance.
	 *
	 * The first in list is the nearest.
	 *
	 * @param range
	 *            The maximum distance to a SheepFood
	 * @return a list of SheepFood or emptyList if none is found in the given
	 *         range
	 */
	protected List<SheepFood> getFoodinRange(final double range) {

		final List<SheepFood> resultList = new LinkedList<SheepFood>();

		for (final SheepFood food : getZone().getSheepFoodList()) {

			if ((food.getAmount() > 0) && (squaredDistance(food) < range * range)) {
				resultList.add(food);
			}
		}
		Collections.sort(resultList, new Comparator<SheepFood>() {

			public int compare(final SheepFood o1, final SheepFood o2) {
				return Double.compare(squaredDistance(o1), squaredDistance(o2));

			}
		});
		return resultList;
	}

	/**
	 * Called when the sheep is hungry.
	 *
	 * @return <code>true</code> if the sheep is hunting for food.
	 */
	protected boolean onHungry() {
		/*
		 * Will try to eat if one of... - Food already on the mind and not
		 * moving (collision?) - Food not on the mind and hunger pains (every
		 * 10)
		 */
		if ("food".equals(getIdea())) {
			if (!stopped()) {
				return true;
			}
		} else {
			/*
			 * Only do something on occasional hunger pains
			 */
			if ((hunger % 10) != 0) {
				return false;
			}
		}

		return searchForFood();
	}

	protected boolean searchForFood() {

		final List<SheepFood> list = getFoodinRange(6);

		for (final SheepFood food : list) {
			if (food.nextTo(this)) {
				logger.debug("Sheep eats");
				setIdea("eat");
				eat(food);
				clearPath();
				stop();
				return true;
			} else {

				final List<Node> path = Path.searchPath(this, food, 6 * 6);
				if (path.size() != 0) {

					logger.debug("Sheep moves to food");
					setIdea("food");

					setPath(new FixedPath(path, false));
					return true;
				}

			}

		}
		setIdea(null);
		return false;
	}

	/**
	 * Called when the sheep is idle.
	 */
	protected void onIdle() {
		final int turn = SingletonRepository.getRuleProcessor().getTurn() + timingAdjust;

		if (owner == null) {
			/*
			 * Check if player near (creature's enemy)
			 */
			if (((turn % 15) == 0) && isEnemyNear(getPerceptionRange())) {
				logger.debug("Sheep (ownerless) moves randomly");
				setIdea("walk");
				moveRandomly();
			} else {
				logger.debug("Sheep sleeping");
				setIdea(null);
			}
		} else if (((turn % 10) == 0) && (hunger >= HUNGER_EXTREMELY_HUNGRY)) {
			/*
			 * An extremely hungry sheep becomes agitated
			 */
			setIdea("food");
			setRandomPathFrom(owner.getX(), owner.getY(), getMovementRange());
			setSpeed(getBaseSpeed());
		} else if (!nextTo(owner)) {
			moveToOwner();
		} else {
			if ((turn % 100) == 0) {
				logger.debug("Sheep is bored");
				setRandomPathFrom(owner.getX(), owner.getY(), getMovementRange()/2);
				setSpeed(getBaseSpeed());
			} else {
				logger.debug("Sheep has nothing to do");
				setIdea(null);
			}
		}
	}

	/**
	 * Called when the sheep is starving.
	 */
	protected void onStarve() {
		if (weight > 0) {
			setWeight(weight - 1);
		} else {
			delayedDamage(1, "starvation");
		}
		logger.warn("Sheep starve " + getZone().getName() + " " + getX() + ": " + getY());
		hunger /= 2;
	}

	/**
	 * Let the sheep eat some food.
	 *
	 * @param food
	 *            The food to eat.
	 */
	protected void eat(final SheepFood food) {
		final int amount = food.getAmount();

		if (amount > 0) {
			food.onFruitPicked(null);

			if (weight < MAX_WEIGHT) {
				setWeight(weight + 10);
			}

			heal(5);
			hunger = 0;
		}
	}

	//
	// RPEntity
	//

	/**
	 * Determines what the sheep shall do next.
	 */
	@Override
	public void logic() {

		if (!getZone().getPlayers().isEmpty()) {
			hunger++;
		}

		

		/*
		 * Allow owner to call sheep (will override other reactions)
		 */
		if (isOwnerCallingMe()) {
			moveToOwner();
		} else if (stopped()) {
			/*
			 * Hungry?
			 */
			if ((hunger < HUNGER_HUNGRY) || !onHungry()) {
				/*
				 * If not hunting for food, do other things
				 */
				onIdle();
			}
		} else if (hunger >= HUNGER_EXTREMELY_HUNGRY) {
			onHungry();
		}

		/*
		 * Starving?
		 */
		if (hunger >= HUNGER_STARVATION) {
			onStarve();

		}

		
		applyMovement();


		notifyWorldAboutChanges();
	}

	//
	// Entity
	//

	@Override
	public String describe() {
		String text = "Oto owca. Po upasieniu jej do 100, zaprowadź ją do NPC Sato, aby ukończyć zadanie. Wygląda na to, że waży około " + weight + ".";
		if (hasDescription()) {
			text = getDescription();
		}
		return (text);
	}
}
