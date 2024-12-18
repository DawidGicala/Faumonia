/* $Id: StendhalQuestSystem.java,v 1.107 2012/12/13 22:35:30 nhnb Exp $ */
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
package games.stendhal.server.core.rp;


import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.*;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Loads and manages all quests.
 */
public class StendhalQuestSystem {

	private static final StendhalQuestSystem stendhalQuestSystem = new StendhalQuestSystem();

	
	/** the logger instance. */
	private static final Logger logger = Logger.getLogger(StendhalQuestSystem.class);

	private final static List<IQuest> quests = new LinkedList<IQuest>();


	private StendhalQuestSystem() {
		// hide constructor, this is a Singleton
	}

	/**
	 * gets the singleton instance of the StendhalQuestSystem
	 *
	 * @return StendhalQuestSystem
	 */
	public static StendhalQuestSystem get() {
		return stendhalQuestSystem;
	}

	/**
	 * 
	 * @param player
	 */
	public static void updatePlayerQuests(Player player) {
		for(int i=0; i<quests.size(); i++) {
			quests.get(i).updatePlayer(player);
		}
	}

	/**
	 * Initializes the QuestSystem.
	 */
	public void init() {
		
		loadQuest(new AdosDeathmatch());
		loadQuest(new AmazonPrincess());
		//loadQuest(new BalloonForBobby());
		loadQuest(new BeerForHayunn());
		loadQuest(new Blackjack());
		loadQuest(new BowsForOuchit());
		loadQuest(new Campfire());
		// deactivated capture the flag
		//loadQuest(new CaptureFlagQuest());
		loadQuest(new ChocolateForElisabeth());
		loadQuest(new CleanAthorsUnderground());
		loadQuest(new CleanStorageSpace());
		loadQuest(new CloakCollector());
		loadQuest(new CloakCollector2());
		loadQuest(new ememedal());
		loadQuest(new obsmedal());
		loadQuest(new rubmedal());
		loadQuest(new sapmedal());
		loadQuest(new DailyGeneralQuest());
		loadQuest(new CloaksForBario());
		loadQuest(new ClubOfThorns());
		loadQuest(new CoalForHaunchy());
		loadQuest(new CrownForTheWannaBeKing());
		loadQuest(new DailyItemQuest());
		loadQuest(new DailyMonsterQuest());
		loadQuest(new DiceGambling());
		loadQuest(new DragonLair());
		loadQuest(new ElfPrincess());
		loadQuest(new ElvishArmor());
		loadQuest(new FindGhosts());
		loadQuest(new FindJefsMom());
		loadQuest(new FindRatChildren());
		loadQuest(new FishermansLicenseQuiz());
		loadQuest(new FishermansLicenseCollector());
		loadQuest(new FishSoup());
		loadQuest(new FishSoupForHughie());
		loadQuest(new FruitsForCoralia());
        loadQuest(new GuessKills());
		loadQuest(new HelpTomi());
		loadQuest(new HelpMrsYeti());
		loadQuest(new HelpSedro());
		loadQuest(new HerbsForCarmen());
		loadQuest(new HouseBuying());
		loadQuest(new HungryJoshua());
		loadQuest(new IcecreamForAnnie());
		loadQuest(new ImperialPrincess());
		loadQuest(new JailedBarbarian());
		loadQuest(new JailedDwarf());
		loadQuest(new LearnAboutKarma());
		loadQuest(new LearnAboutOrbs());
		loadQuest(new LookBookforCeryl());
		loadQuest(new LookUpQuote());
		loadQuest(new KanmararnSoldiers());
		loadQuest(new KillDarkElves());
		loadQuest(new KillGnomes());
		loadQuest(new KillMonks());
		loadQuest(new KillSpiders());
		loadQuest(new KillEasyMonsters());
		loadQuest(new KillMediumMonsters());
		loadQuest(new KillAlbinos());
		loadQuest(new KillDwarfs());
		loadQuest(new KillGiants());
		loadQuest(new KillSedah());
		loadQuest(new KillBarbarians());
		loadQuest(new KillForWod());
		loadQuest(new KillMag());
		loadQuest(new KillMadaram());
		loadQuest(new KillChaos());
		loadQuest(new KillKika());
		loadQuest(new KillMithril());
		loadQuest(new KillOni());
		loadQuest(new KillForVerdan());
		loadQuest(new KillStone());
		loadQuest(new KillAmazon());
		loadQuest(new KillLast());
		loadQuest(new KillGlut1());
		loadQuest(new KillGlut2());
		loadQuest(new KillGlut3());
		loadQuest(new KillGlut4());
		loadQuest(new KillGlut());
		loadQuest(new KoboldishTorcibud());
		loadQuest(new Marriage());
		loadQuest(new Maze());
		loadQuest(new McPeglegIOU());
		loadQuest(new MeetBunny());
		loadQuest(new MedicineForTad());
		loadQuest(new MeetSanta());
		loadQuest(new MeetZynn());
		loadQuest(new MithrilCloak());
		loadQuest(new NewsFromHackim());
		loadQuest(new ObsidianKnife());
		loadQuest(new PizzaDelivery());
		loadQuest(new PlinksToy());
		loadQuest(new RainbowBeans());
		loadQuest(new ReverseArrow());
		loadQuest(new RingMaker());
		loadQuest(new SheepGrowing());
		loadQuest(new SolveRiddles());
		loadQuest(new SevenCherubs());
		loadQuest(new Snowballs());
		loadQuest(new Soup());
		loadQuest(new StuffForBaldemar());
		loadQuest(new StuffForVulcanus());
		loadQuest(new SuntanCreamForZara());
		loadQuest(new SuppliesForPhalk());
		loadQuest(new TakeGoldforGrafindle());
		loadQuest(new ToysCollector());
		loadQuest(new VampireSword());
		loadQuest(new WeaponsCollector());
		loadQuest(new WeaponsCollector2());
		loadQuest(new WizardBank());
		loadQuest(new ZooFood());
		loadQuest(new DragonCave());
		loadQuest(new Podroznik());
		loadQuest(new ElementalDagger1());
		loadQuest(new ElementalDagger2());
		loadQuest(new DeadWife());
		loadQuest(new RedStone());
		loadQuest(new BlueStone());
		loadQuest(new GoldenStone());
		loadQuest(new SemosBadgeQuest());
		loadQuest(new AdosBadgeQuest());
		loadQuest(new KirdnehBadgeQuest());
		loadQuest(new NalworBadgeQuest());
		loadQuest(new FadoBadgeQuest());
		loadQuest(new DailyMonsterQuest1());
		loadQuest(new DailyMonsterQuest2());
		loadQuest(new AndrzejCollector());
		loadQuest(new KillHigh());
		loadQuest(new KillDragons());
		loadQuest(new KillAngels());
		loadQuest(new CheeseForSoro());
		loadQuest(new MagicStaff());
		loadQuest(new WeeklyCollectorQuest());
		loadQuest(new WeeklyItemQuest());
		loadQuest(new ScrollDragon());
		loadQuest(new ScrollHeaven());
		loadQuest(new ScrollDeath());
		loadQuest(new ScrollAngel());
		loadQuest(new ScrollGiant());
		loadQuest(new ScrollMaleus());
		loadQuest(new KillFairy());
		loadQuest(new SwordsForNnyddion());
		loadQuest(new KillElfes());
		loadQuest(new ResourceForDiehelm());
		loadQuest(new ResourceForCzek());
		loadQuest(new BringBlack());
		loadQuest(new BringMithril());
		loadQuest(new SkillerTask1());
		loadQuest(new SkillerTask2());
		loadQuest(new SkillerTask3());
		loadQuest(new SkillerTask4());
		loadQuest(new ZrodelkaTask1());
		loadQuest(new ZrodelkaTask2());
		loadQuest(new ZrodelkaTask3());
		loadQuest(new MainTask1());
		loadQuest(new MainTask2());
		loadQuest(new MainTask3());
		loadQuest(new MainTask4());
		loadQuest(new MainTask5());
		loadQuest(new MainTask6());
		loadQuest(new MainTask7());
		loadQuest(new MainTask8());
		loadQuest(new MainTask9());
		loadQuest(new MainTask10());
		loadQuest(new MainTask11());
		loadQuest(new MainTask12());
		loadQuest(new MainTask13());
		loadQuest(new MainTask14());
		loadQuest(new MainTask15());
		loadQuest(new MainTask16());
		loadQuest(new MainTask17());
		loadQuest(new MainTask18());
		loadQuest(new MainTask19());
		loadQuest(new KillXeno());
		loadQuest(new TaskHayunn());
		loadQuest(new DailyMikolaj());
		loadQuest(new BalwanekQuest());
		loadQuest(new DailyGnom());
		loadQuest(new CollectChaosThorgal());
		loadQuest(new KillChaosMonsters());
		loadQuest(new MainTask20());
		loadQuest(new DailyItemQuestSummer2024());
		loadQuest(new KillForWidnosSummer2024());
		loadQuest(new SkinsForGeranesSummer2024());
		loadQuest(new StoneForBreksSummer2024());
		loadQuest(new KillForQuinlanSummer2024());
		loadQuest(new DailyMonsterQuestSummer2024());
		loadQuest(new IceSwordsSummer2024());
		loadQuest(new MainTask21());
		loadQuest(new ItemsForHalvorn());



		
		if (System.getProperty("stendhal.christmas") != null) {
			loadQuest(new GoodiesForRudolph());
		}
		if (System.getProperty("stendhal.minetown") != null) {
			loadQuest(new PaperChase()); // needs to be loaded before SemosMineTownRevivalWeeks
			loadQuest(new MineTownRevivalWeeks());
		}

		TurnNotifier.get().notifyInTurns(10, new DumpSpeakerNPCtoDB());
	}

	/**
	 * loads the quests and adds it to the world
	 *
	 * @param quest a Quest
	 */
	public void loadQuest(final IQuest quest) {

		// for quicker startup, check the stendhal.quest.regex parameter
		final String regex = System.getProperty("stendhal.quest.regex", ".*");
		if (!quest.getName().matches(regex)) {
			return;
		}
		
		// load the quest and add it to the world
		try {
			initQuestAndAddToWorld(quest);
		} catch (Exception e) {
			logger.error("Quest(" + quest.getName() + ") loading failed.", e);
		}
	}

	/**
	 * adds a quest to the world
	 *
	 * @param quest Quest to add
	 */
	private void initQuestAndAddToWorld(final IQuest quest) {
		logger.info("Loading Quest: " + quest.getName());
		quest.addToWorld();
		quests.add(quest);
	}

	/**
	 * 
	 * @param sb - string builder of mother function
	 * @param quest - show this quest to payer
	 * @param player - player which quest history need to be shown to himself
	 */
	private void dumpQuest(final StringBuilder sb, final IQuest quest, final Player player) {
		final QuestInfo questInfo = quest.getQuestInfo(player);
		sb.append(questInfo.getName() + " : ");
		sb.append(questInfo.getDescription() + "\r\n");
		if (questInfo.getSuggestedMinLevel() > player.getLevel()) {
			sb.append("(To zadanie może być zbyt niebezpieczne jak na twój poziom doświadczenia)\r\n");
		}
		
		final List<String> history = quest.getHistory(player);
		for (final String entry : history) {
			sb.append("\t * " + entry + "\r\n");
		}
		
		final List<String> hints = quest.getHint(player);
		for (final String entry : hints) {
			sb.append("\t - " + entry + "\r\n");
		}
	}

	/**
	 * lists all quest the player knows about, including open and completed quests.
	 *
	 * @param player Player to create the report for
	 * @return quest report
	 */
	public String listQuests(final Player player) {
		final StringBuilder sb = new StringBuilder();

		// Open quests
		sb.append("\r\n#'Otwarte zadania': ");
		boolean first = true;
		for (final IQuest quest : quests) {
			if (quest.isStarted(player) && !quest.isCompleted(player)) {
				if (!first) {
					sb.append(", ");
				}
				sb.append(quest.getName());
				first = false;
			}
		}

		// Completed Quests
		sb.append("\r\n#'Ukończone zadania': ");
		first = true;
		for (final IQuest quest : quests) {
			if (quest.isCompleted(player)) {
				if (!first) {
					sb.append(", ");
				}
				sb.append(quest.getName());
				first = false;
			}
		}

		return sb.toString();
	}

	/**
	 * creates a report on a specified quest for a specified player
	 *
	 * @param player Player
	 * @param questName name of quest
	 * @return quest report
	 */
	public String listQuest(final Player player, final String questName) {
		final StringBuilder sb = new StringBuilder();
		for (final IQuest quest : quests) {
			if (quest.getName().equals(questName)) {
				dumpQuest(sb, quest, player);
			}
		}
		return sb.toString();
	}
	
	/**
	 * dumps the internal quest states for the specified player. This is used for the InspectAction.
	 *
	 * @param player Player to create the report for
	 * @return report
	 */
	public String listQuestsStates(final Player player) {
		final StringBuilder sb = new StringBuilder();

		// Open quests
		sb.append("\r\n#'Otwarte zadania': ");

		for (final IQuest quest : quests) {
			if (quest.isStarted(player) && !quest.isCompleted(player)) {
				sb.append("\r\n" + quest.getName() + " (" + quest.getSlotName() + "): " + player.getQuest(quest.getSlotName()));
			}
		}

		// Completed Quests
		sb.append("\n#'Ukończone zadania': ");
		for (final IQuest quest : quests) {
			if (quest.isCompleted(player)) {
				sb.append("\r\n" + quest.getName() + " (" + quest.getSlotName() + "): " + player.getQuest(quest.getSlotName()));
			}
		}

		return sb.toString();
	}

	/**
	 * gets a list of open quests
	 *
	 * @param player Player to return the list for
	 * @return list of open quests
	 */
	public List<String> getOpenQuests(Player player) {
		List<String> res = new LinkedList<String>();
		for (final IQuest quest : quests) {
			if (quest.isStarted(player) && !quest.isCompleted(player) && quest.isVisibleOnQuestStatus()) {
				res.add(quest.getQuestInfo(player).getName());
			}
		}
		return res;
	}

	/**
	 * gets a list of completed quests
	 *
	 * @param player Player to return the list for
	 * @return list of completed quests
	 */
	public List<String> getCompletedQuests(Player player) {
		List<String> res = new LinkedList<String>();
		for (final IQuest quest : quests) {
			if (quest.isCompleted(player) && quest.isVisibleOnQuestStatus()) {
				res.add(quest.getQuestInfo(player).getName());
			}
		}
		return res;
	}


	/**
	 * gets the description of a quest
	 *
	 * @param player player to get the details for
	 * @param questName name of quest
	 * @return description
	 */
	public String getQuestDescription(final Player player, final String questName) {
		for (final IQuest quest : quests) {
			final QuestInfo questInfo = quest.getQuestInfo(player);
			if (questInfo.getName().equals(questName)) {
				return questInfo.getDescription();
			}
		}
		return null;
	}

	/**
	 * if the quest is too dangerous, add a warning
	 *
	 * @param player Player to get the warning for
	 * @param questName   quest
	 * @return warning or empty string
	 */
	public String getQuestLevelWarning(Player player, String questName) {
		for (final IQuest quest : quests) {
			final QuestInfo questInfo = quest.getQuestInfo(player);
			if (questInfo.getName().equals(questName)) {
				if (questInfo.getSuggestedMinLevel() > player.getLevel()) {
					return "To zadanie może być zbyt niebezpieczne jak na twój poziom doświadczenia.";
				}
			}
		}
		return "";
	}
	/**
	 * gets details on the progress of the quest
	 *
	 * @param player player to get the details for
	 * @param questName name of quest
	 * @return details
	 */
	public List<String> getQuestProgressDetails(final Player player, final String questName) {
		List<String> res = new LinkedList<String>();
		for (final IQuest quest : quests) {
				final QuestInfo questInfo = quest.getQuestInfo(player);
			if (questInfo.getName().equals(questName)) {
				final List<String> history = quest.getHistory(player);
				for (final String entry : history) {
					res.add(entry);
				}
			}
		}
		return res;
	}


	/**
	 * gets the IQuest object for a named quest.
	 *
	 * @param questName name of quest
	 * @return IQuest or <code>null</code> if it does not exist.
	 */
	public IQuest getQuest(String questName) {
		for (final IQuest quest : quests) {
			if (quest.getName().equals(questName)) {
				return quest;
			}
		}
		return null;
	}

	/**
	 * unloads a quest and removes the things related to it from the world.
	 * <p>Note: The quest in question has to support this</p>
	 *
	 * @param questName quest to unload
	 */
	public void unloadQuest(String questName) {
		IQuest quest = getQuest(questName);
		if (quest == null) {
			logger.error("Quest " + questName + " is not loaded", new Throwable());
			return;
		}

		boolean res = quest.removeFromWorld();
		if (res) {
			quests.remove(quest);
			logger.info("Unloading Quest: " + quest.getName());
		} else {
			logger.error(this.getClass() + " cannot be removed from the world");
		}
	}
	
	/**
	 * gets a list of incomplete quests in a specified region
	 *
	 * @param player Player to return the list for
	 * @param region Region to check in
	 * @return list of incomplete quests in the region
	 */
	public List<String> getIncompleteQuests(Player player, String region) {
		List<String> res = new LinkedList<String>();
		for (final IQuest quest : quests) {
			if (region.equals(quest.getRegion()) && !quest.isCompleted(player) && quest.isVisibleOnQuestStatus()) {
				res.add(quest.getQuestInfo(player).getName());
			}
		}
		return res;
	}
	


	/**
	 * gets a list of the unique npc names for unstarted quests in a specified region
	 *
	 * @param player Player to return the list for
	 * @param region Region to check in
	 * @return list of the unique npc names for unstarted quests in a specified region
	 */
	public List<String> getNPCNamesForUnstartedQuestsInRegionForLevel(Player player, String region) {
        final int playerlevel = player.getLevel();
		List<String> res = new LinkedList<String>();
		for (final IQuest quest : quests) {
			if (region.equals(quest.getRegion()) && !quest.isStarted(player) && quest.isVisibleOnQuestStatus() && quest.getMinLevel()<playerlevel) {
				// don't add a name twice
				if (!res.contains(quest.getNPCName())) {
					res.add(quest.getNPCName());
				}
			}
		}
		return res;
	}


	/**
	 * gets quest descriptions for unstarted quests in a specified region matching a specific npc name
	 *
	 * @param player Player to return the list for
	 * @param region Region to check in
	 * @return quest description (there may be more than one)
	 */
	public List<String> getQuestDescriptionForUnstartedQuestInRegionFromNPCName(Player player, String region, String name) {
        final int playerlevel = player.getLevel();
		List<String> res = new LinkedList<String>();
        if (name == null) {
            return res;
        }
		for (final IQuest quest : quests) {
			if (region.equals(quest.getRegion()) && !quest.isStarted(player) && quest.isVisibleOnQuestStatus() && quest.getMinLevel()<playerlevel && name.equals(quest.getNPCName())) {
				res.add(quest.getQuestInfo(player).getDescription());
			}
		}
		return res;
	}



}
