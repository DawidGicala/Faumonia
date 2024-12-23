/* $Id: FoodMill.java,v 1.10 2012/06/27 19:38:16 kymara Exp $ */
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

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.entity.RPEntity;

import java.util.Map;

import marauroa.common.game.RPObject;

public class FoodMill extends Item implements UseListener {

	/** The item to be processed */
	private String input;
	/** The required container for processing */
	private String container;
	/** The resulting processed item */
	private String output;
	
    public FoodMill(final String name, final String clazz,
            final String subclass, final Map<String, String> attributes) {
        super(name, clazz, subclass, attributes);
        init();
    }

    public FoodMill(final FoodMill item) {
        super(item);
        init();
    }
    
    /** Sets up the input, output and container based on item name */
    private void init() {
    	if ("młynek do cukru".equals(getName())) {
    		input = "trzcina cukrowa";
    		container = "pusty worek";
    		output = "cukier";
    	} else if ("zwój czyszczący".equals(getName())) {
    		input = "zwój zapisany";
    		container = "money";
    		output = "niezapisany zwój";
    	} else {
    		input = "jabłko";
    		container = "buteleczka";
    		output = "sok jabłkowy";
    	}
    }

    public boolean onUsed(final RPEntity user) {
    	/* is the mill equipped at all? */
    	if (!isContained()) {
    		user.sendPrivateText("Powinieneś mieć " + getName() + ", aby móc go użyć.");
    		return false;
    	}

    	final String slotName = getContainerSlot().getName();

    	/* is it in a hand? */
    	if (!slotName.endsWith("hand")) {
    		user.sendPrivateText("Powinieneś trzymać " + getName() + " w drugiej ręce, aby móc go użyć.");
    		return false;
    	}

    	String otherhand = getOtherHand(slotName);

    	final RPObject first = user.getSlot(otherhand).getFirst();

    	/* is anything in the other hand? */
    	if (first == null) {
    		user.sendPrivateText("Twoja druga ręka wygląda na pustą.");
    		return false;
    	}

    	/*
    	 * the player needs to equip at least the input in his other hand
    	 * and have the correct container in his inventory
    	 */
    	if (!input.equals(first.get("name"))) {
    		user.sendPrivateText("Musisz mieć conajmniej " + Grammar.a_noun(input) + " w drugiej dłoni");
    		return false;
    	}

    	if (!user.isEquipped(container)) {
    		user.sendPrivateText("Nie masz " + Grammar.a_noun(container) + " ze sobą");
    		return false;
    	}

        /* all is okay, lets process this item */
    	final Item item = SingletonRepository.getEntityManager().getItem(output);
    	
    	if (first instanceof StackableItem) {
			StackableItem dropOneOfMe = (StackableItem) first;
			dropOneOfMe.removeOne();
		} else {
			user.drop((Item) first);
		}
    	user.drop(container);
    	user.equipOrPutOnGround(item);

    	return true;
    } 

    
    /**
     * @param handSlot should be rhand or lhand
     * @return the opposite hand to handSlot 
     */
    private String getOtherHand(final String handSlot) {
        if ("rhand".equals(handSlot)) {
            return "lhand";
        } else {
            return "rhand";
        }
    }
    
}
