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

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Finds creatures with wrong sized corpses.
 */
public class FindBrokenCorpses extends ScriptImpl {
	/**
	 * Don't complain corpses that are too wide or too narrow by at most this
	 * amount.
	 */
	private static final int ALLOWED_EXTRA = 1;
	
	final HashMap<String, List<String>> problems = new HashMap<String, List<String>>();
	
	@Override
	public void execute(Player admin, List<String> args) {		
		EntityManager manager = SingletonRepository.getEntityManager();
		for (Creature creature : manager.getCreatures()) {
			int wDiff = (int) (creature.getWidth() - creature.getCorpseWidth()); 
			int hDiff = (int) (creature.getHeight() - creature.getCorpseHeight());
			String name = creature.getName();
			
			if (wDiff > 0) {
				addProblem(name, "Zwłoki zbyt wąskie o " + wDiff);
			} else if (wDiff < -ALLOWED_EXTRA) {
				addProblem(name, "Zwłoki zbyt szerokie o " + -wDiff);
			}
			
			if (hDiff > 0) {
				addProblem(name, "Zwłoki zbyt krótkie o " + hDiff);
			} else if (hDiff < -ALLOWED_EXTRA) {
				addProblem(name, "Zwłoki zbyt długie o " + -hDiff);
			}
		}
		
		reportProblems(admin);
	}

	/**
	 * Add a problem description to a creature.
	 * 
	 * @param creature
	 * @param problem
	 */
	private void addProblem(String creature, String problem) {
		List<String> problemList = problems.get(creature);
		if (problemList == null) {
			problemList = new ArrayList<String>();
			problems.put(creature, problemList);
		}
		problemList.add(problem);
	}

	/**
	 * Send a problem report to the admin.
	 * 
	 * @param admin
	 */
	private void reportProblems(Player admin) {
		String message;
		if (problems.isEmpty()) {
			message = "Nie znaleziono problemów związanych ze zwłokami!";
		} else {
			StringBuilder builder = new StringBuilder("Problemy:");
			for (Entry<String, List<String>> entry : problems.entrySet()) {
				builder.append("\n");
				builder.append(entry.getKey());
				builder.append(":\n");
				
				for (String problem : entry.getValue()) {
					builder.append(problem);
					builder.append("\n");
				}
			}
			message = builder.toString();
		}
		admin.sendPrivateText(message);
	}
}
