/***************************************************************************
 *                      (C) Copyright 2011 - Stendhal                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.common.grammar;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Manage the rules for building full forms from words
 * by prefixing with word phrases like "piece of".
 */
final class PrefixManager
{
	/**
	 * Entry for registering singular/plural prefixes for a keyword.
	 */
	private static class PrefixEntry {
		public final String keyword;
		public final String prefixPlural;
		public final String prefixPlural2;
		public final String prefixSingular;

		public PrefixEntry(final String keyword, final String prefixSingular, final String prefixPlural, final String prefixPlural2) {
			this.keyword = keyword;
			this.prefixSingular = prefixSingular;
			this.prefixPlural = prefixPlural;
			this.prefixPlural2 = prefixPlural2;
		}
	}


	public static PrefixManager s_instance = new PrefixManager();

	private Collection<String> pluralPrefixes = new HashSet<String>();

	private Collection<String> plural2Prefixes = new HashSet<String>();

	private Collection<PrefixEntry> prefixEndList = new ArrayList<PrefixEntry>();


	private Map<String, PrefixEntry> prefixMap = new HashMap<String, PrefixEntry>();

	private Collection<String> singularPrefixes = new HashSet<String>();

	/**
	 * Initialise the map of nouns and prefix expressions.
	 */
	public PrefixManager() {
		register("piece of ", "pieces of ", "pieces of ", "meat");
		register("piece of ", "pieces of ", "pieces of ", "ham");
		register("piece of ", "pieces of ", "pieces of ", "cheese");
		register("piece of ", "pieces of ", "pieces of ", "wood");
		register("piece of ", "pieces of ", "pieces of ", "paper");
		register("piece of ", "pieces of ", "pieces of ", "iron");
		register("piece of ", "pieces of ", "pieces of ", "chicken");
		register("piece of ", "pieces of ", "pieces of ", "coal");
		register("piece of ", "pieces of ", "pieces of ", "beeswax");

		register("sack of ", "sacks of ", "sacks of ", "flour");
		register("sack of ", "sacks of ", "sacks of ", "sugar");

		register("sheaf of ", "sheaves of ", "sheaves of ", "grain");
		register("loaf of ", "loaves of ", "loaves of ", "bread");
		register("stick of ", "sticks of ", "sticks of ", "butter");
		register("bulb of ", "bulbs of ", "bulbs of ", "garlic");
		register("jar of ", "jars of ", "jars of ", "honey");
		register("glass of ", "glasses of ", "glasses of ", "wine");
		register("cup of ", "cups of ", "cups of ", "tea");
		register("sprig of ", "sprigs of ", "sprigs of ", "arandula");
		register("root of ", "roots of ", "roots of ", "mandragora");
		register("bunch of ", "bunches of ", "bunches of ", "daisies");
		register("bunch of ", "bunches of ", "bunches of ", "grapes");
		register("can of ", "cans of ", "cans of ", "oil");

		register("bottle of ", "bottles of ", "bottles of ", "beer");
		register("bottle of ", "bottles of ", "bottles of ", "water");
		register("bottle of ", "bottles of ", "bottles of ", "fierywater");
		register("bottle of ", "bottles of ", "bottles of ", "milk");

		registerEnd("bottle of ", "bottles of ", "bottles of ", "potion");
		registerEnd("bottle of ", "bottles of ", "bottles of ", "poison");
		registerEnd("bottle of ", "bottles of ", "bottles of ", "antidote");
		registerEnd("nugget of ", "nuggets of ", "nuggets of ", " ore");

		registerEnd("pair of ", "pairs of ", "pairs of ", " legs");
		registerEnd("pair of ", "pairs of ", "pairs of ", " boots");

		registerEnd("spool of ", "spools of ", "spools of ", " thread");

		registerPrefix("suit of ", "suits of ", "suits of "); // "armor"
	}


	/**
	 * Prefix one of the registered nouns with an expression like "piece of".
	 * 
	 * @param str noun to process
	 * @return noun with prefix
	 */
	public String fullForm(final String str, final String lowString) {
		String ret = lowString;

		PrefixEntry found = prefixMap.get(str);

		if (found != null) {
			ret = found.prefixSingular + ret;
		} else {
			for(PrefixEntry entry : prefixEndList) {
				if (str.endsWith(entry.keyword)) {
					ret = Grammar.addPrefixIfNotAlreadyThere(ret, entry.prefixSingular, entry.prefixPlural, entry.prefixPlural2);
					break;
				}
			}
		}

		return ret;
	}

	/**
	 * @return collection of all registered plural2 prefixes.
	 */
	public Collection<String> getPlural2Prefixes() {
		return plural2Prefixes;
	}
	/**
	 * @return collection of all registered plural prefixes.
	 */
	public Collection<String> getPluralPrefixes() {
		return pluralPrefixes;
	}
	/**
	 * @return collection of all registered singular prefixes.
	 */
	public Collection<String> getSingularPrefixes() {
		return singularPrefixes;
	}

	/**
	 * Define the singular and plural prefix strings for an item name with full match,
	 * for example "piece of paper".
	 * @param prefixSingular
	 * @param prefixPlural
	 * @param prefixPlural2
	 * @param noun
	 */
	private void register(final String prefixSingular, final String prefixPlural, final String prefixPlural2, final String noun) {
		prefixMap.put(noun, new PrefixEntry(noun, prefixSingular, prefixPlural, prefixPlural2));

		registerPrefix(prefixSingular, prefixPlural, prefixPlural2);
	}
	/**
	 * Define the singular and plural prefix strings for an item name to be matched at the end,
	 * for example "bottle of ... potion".
	 * @param prefixSingular
	 * @param prefixPlural
	 * @param prefixPlural2
	 * @param endString
	 */
	private void registerEnd(final String prefixSingular, final String prefixPlural, final String prefixPlural2, final String endString) {
		prefixEndList.add(new PrefixEntry(endString, prefixSingular, prefixPlural, prefixPlural2));

		registerPrefix(prefixSingular, prefixPlural, prefixPlural2);
	}


	/**
	 * Register a pair of singular and plural prefix strings to be removed
	 * when parsing item names, for example "suits of leather armor".
	 * @param prefixSingular
	 * @param prefixPlural
	 * @param prefixPlural2
	 */
	private void registerPrefix(final String prefixSingular, final String prefixPlural, final String prefixPlural2) {
		singularPrefixes.add(prefixSingular);
		pluralPrefixes.add(prefixPlural);
		plural2Prefixes.add(prefixPlural2);
	}
}
