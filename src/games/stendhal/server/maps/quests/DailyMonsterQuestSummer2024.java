/* $Id: DailyMonsterQuestSummer2024.java,v 1.80 2024/06/29 15:47:16 davvids Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import games.stendhal.common.MathHelper;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.LevelBasedComparator;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.IncreaseXPDependentOnLevelAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;


public class DailyMonsterQuestSummer2024 extends AbstractQuest {

	private static final String QUEST_SLOT = "daily_monster_summer_2024";
	private final SpeakerNPC npc = npcs.get("Oren");
	private static Logger logger = Logger.getLogger("daily_monster_summer_2024");

	private final static int delay = MathHelper.MINUTES_IN_ONE_DAY;
	private final static int expireDelay = MathHelper.MINUTES_IN_ONE_WEEK;

	
	/** All creatures, sorted by level. */
	private static List<Creature> sortedcreatures;	
	
	private static void refreshCreaturesList(final String excludedCreature) {
		final Collection<Creature> creatures = SingletonRepository.getEntityManager().getCreatures();
		sortedcreatures = new LinkedList<Creature>();
		for (Creature creature : creatures) {
			if (!creature.isRare() && !creature.getName().equals(excludedCreature)) {
				sortedcreatures.add(creature);
			}
		}
		Collections.sort(sortedcreatures, new LevelBasedComparator());
	}	
	
	/**
	 * constructor for quest
	 */
	public DailyMonsterQuestSummer2024() {
		refreshCreaturesList(null);
	}
	
	static class daily_monster_summer_2024QuestAction implements ChatAction {
		//private String debugString;
		
		public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {

			if (player.isBadBoy()) {
				raiser.say("Z twej ręki zginął dzielny, szlachetny i poważany rycerz! Precz mi z oczu!");
			} else {
				final String questInfo = player.getQuest(QUEST_SLOT);
				String questCount = null;
				String questLast = null;

            	String previousCreature = null;
            
				if (questInfo != null) {
					final String[] tokens = (questInfo + ";0;0;0").split(";");
					if(!"done".equals(tokens[0])) {
						// can't use this method because this class is static
						// previousCreature = getCreatureToKillFromPlayer(player);
						String[] split = tokens[0].split(",");
						previousCreature = split[0];
					}
					//questLast = tokens[1];
					questCount = tokens[2];
				}
			
				refreshCreaturesList(previousCreature);
			
				// Creature selection magic happens here
				final Creature pickedCreature = pickIdealCreature(player.getLevel(),
						false, sortedcreatures);

				// shouldn't happen
				if (pickedCreature == null) {
					raiser.say("Dziękuję, ale teraz nie mam dla Ciebie zadania.");
					return;
				}

				String creatureName = pickedCreature.getName();

			
				raiser.say("Szukam kogoś kto znajdzie i pokona dla mnie pewnego potwora. Idź zabij " + Grammar.a_noun(creatureName)
						+ " i powiedz #załatwione, gdy skończysz.");

				questLast = "" + (new Date()).getTime();
				player.setQuest(
						QUEST_SLOT, 
						creatureName + ",0,1,"+
						player.getSoloKill(creatureName)+","+
						player.getSharedKill(creatureName)+";" + 
						questLast + ";"+ 
						questCount);
			}
  		}

		// Returns a random creature near the players level, returns null if
		// there is a bug.
		// The ability to set a different level is for testing purposes
		public Creature pickIdealCreature(final int level, final boolean testMode, final List<Creature> creatureList) {
			// int level = player.getLevel();

			// start = lower bound, current = upper bound, for the range of
			// acceptable monsters for this specific player
			int current = -1;
			int start = 0;

			boolean lowerBoundIsSet = false;
			for (final Creature creature : creatureList) {
				current++;
				// Set the strongest creature
				if (creature.getLevel() > level + 100) {
					current--;
					break;
				}
				// Set the weakest creature
				if ((!lowerBoundIsSet) && (creature.getLevel() > 0)
						&& (creature.getLevel() >= level + 80)) {
					start = current;
					lowerBoundIsSet = true;
				}
			}

			// possible with low lvl player and no low lvl creatures.
			if (current < 0) {
				current = 0;
			}

			// possible if the player is ~5 levels higher than the highest level
			// creature
			if (!lowerBoundIsSet) {
				start = current;
			}

			// make sure the pool of acceptable monsters is at least
			// minSelected, the additional creatures will be weaker
			if (current >= start) {
				final int minSelected = 5;
				final int numSelected = current - start + 1;
				if (numSelected < minSelected) {
					start = start - (minSelected - numSelected);
					// don't let the lower bound go too low
					if (start < 0) {
						start = 0;
					}
				}
			}

			// shouldn't happen
			if ((current < start) || (start < 0)
					|| (current >= creatureList.size())) {
				if (testMode) {
					logger.debug("ERROR: <"+level + "> start=" + start + 
							", current=" + current);
				}
				return null;
			}

			// pick a random creature from the acceptable range.
			final int result = start + new Random().nextInt(current - start + 1);
			final Creature cResult = creatureList.get(result);

			if (testMode) {
				logger.debug("OK: <" + level + "> start=" + start
						+ ", current=" + current + ", result=" + result
						+ ", cResult=" + cResult.getName() + ". OPTIONS: ");
				for (int i = start; i <= current; i++) {
					final Creature cTemp = creatureList.get(i);
					logger.debug(cTemp.getName() + ":" + cTemp.getLevel()	+ "; ");
				}
			}
			return cResult;
		}

		/*
		// Debug Only, Performs tests
		// Populates debugString with test data.
		public void testAllLevels() {
			debugString = "";
			int max = Level.maxLevel();
			// in case max level is set to infinity in the future.
			if (max > 1100) {
				max = 1100;
			}
			for (int i = 0; i <= max; i++) {
				pickIdealCreature(i, true, sortedcreatures);
			}
		}
		*/
	}	

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Orena na mostku w Semos");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę pomóc Orenowi.");
			return res;
		}

		res.add("Chcę pomóc Orenowi.");
		if (player.hasQuest(QUEST_SLOT) && !player.isQuestCompleted(QUEST_SLOT)) {
			final boolean questDone = new KilledForQuestCondition(QUEST_SLOT, 0)
					.fire(player, null, null);
			final String creatureToKill = getCreatureToKillFromPlayer(player);
			if (!questDone) {
				res.add("Zostałem poproszony o zabicie" + creatureToKill
						+ ", aby pomóc Orenowi. Jeszcze go nie zabiłem.");
			} else {
				res.add("Zabiłem " + creatureToKill
						+ ", aby pomóc Orenowi.");
			}
		}
		if (player.isQuestCompleted(QUEST_SLOT)) {
			final String[] tokens = (questState + ";0;0;0").split(";");
			final String questLast = tokens[1];
			final long timeRemaining = (Long.parseLong(questLast) + MathHelper.MILLISECONDS_IN_ONE_DAY)
					- System.currentTimeMillis();

			if (timeRemaining > 0L) {
				res.add("Zabiłem potwora o którego prosił Oren. Prawdopodobnie nie potrzebuje już mojej pomocy.");
			} else {
				res.add("Zabiłem potwora o którego prosił Oren. Prawdopodobnie znowu potrzebuje mojej pomocy.");
			}
		}
		// add to history how often player helped Semos so far
		final int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
		if (repetitions > 0) {
			res.add("pomogłem Orenowi "
					+ Grammar.quantityplnoun(repetitions, "razy") + " do tej pory.");
		}
		return res;
	}
	
	private String getCreatureToKillFromPlayer(Player player) {
		String actualQuestSlot = player.getQuest(QUEST_SLOT, 0);
		String[] split = actualQuestSlot.split(",");
		if (split.length > 1) {
			// only return object if the slot was in the format expected (i.e. not done;timestamp;count etc)
			return split[0];
		}
		return null;
	}

	/**
	 * player said "quest"
	 */
	private void step_1() {
		// player asking for quest when he have active non-expired quest 
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new NotCondition(
								new OrCondition(
										new QuestNotStartedCondition(QUEST_SLOT),
										new QuestCompletedCondition(QUEST_SLOT))),
						new NotCondition(
								new TimePassedCondition(QUEST_SLOT, 1, expireDelay))), 
				ConversationStates.ATTENDING, 
				null,
				new ChatAction() {
					public void fire(Player player, Sentence sentence, EventRaiser npc) {
						npc.say("Strasznie dziś gorąco, nie chce mi się powtarzać. Już dostałeś zadanie na zgładzenie " + 
								Grammar.a_noun(player.getQuest(QUEST_SLOT,0).split(",")[0]) + 
								". Powiedz #załatwione kiedy to zrobisz!");
					}			
				});
		
		// player have expired quest time
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new NotCondition(
								new OrCondition(
										new QuestNotStartedCondition(QUEST_SLOT),
										new QuestCompletedCondition(QUEST_SLOT))),
						new TimePassedCondition(QUEST_SLOT, 1, expireDelay)), 
				ConversationStates.ATTENDING, 
				null,
				new ChatAction() {
					public void fire(Player player, Sentence sentence, EventRaiser npc) {
						if(player.getQuest(QUEST_SLOT, 0)!=null) {
								npc.say("Strasznie dziś gorąco, nie chce mi się powtarzać. Już otrzymałeś zadanie na zgładzenie " + 
										Grammar.a_noun(player.getQuest(QUEST_SLOT, 0).split(",")[0]) + 
										". Powiedz #załatwione kiedy to zrobisz!" +
										" Jeżeli nie możesz go znaleźć to może znaczyć, że już nie chodzi po naszej krainie. Możesz zabić #innego potwora jeżeli chcesz.");
						}
					}
				});
		
		// player asking for quest before allowed time interval 
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new QuestCompletedCondition(QUEST_SLOT),
						new NotCondition( 
								new TimePassedCondition(QUEST_SLOT, 1, delay))), 
				ConversationStates.ATTENDING, 
				null,
				new SayTimeRemainingAction(QUEST_SLOT,1, delay, "Na tą chwile nie potrzebuje Twojej pomocy, jest strasznie goraco.. nie mam siły myśleć. Proszę wróć za "));

		// player asked for quest first time or repeat it after passed proper time
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new OrCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new AndCondition(
								new QuestCompletedCondition(QUEST_SLOT),
								new TimePassedCondition(QUEST_SLOT, 1, delay))), 
				ConversationStates.ATTENDING, 
				null,
				new daily_monster_summer_2024QuestAction());		
	}

	/**
	 * player killing monster
	 */
	private void step_2() {
		// kill the monster
	}

	/**
	 * player said "done"
	 */
	private void step_3() {
		// player never asked for this quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Wydaje mi się, że moge mieć dla Ciebie pewne #zadanie",
				null);

		// player already completed this quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Ukończyłes ostatnie #zadanie które Ci dałem.",
				null);

		// player didn't killed creature 
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(
						new QuestStartedCondition(QUEST_SLOT),
						new QuestNotCompletedCondition(QUEST_SLOT),
						new NotCondition(
						        new KilledForQuestCondition(QUEST_SLOT, 0))),
				ConversationStates.ATTENDING, 
				null,
				new ChatAction() {
					public void fire(Player player, Sentence sentence, EventRaiser npc) {
							final String questKill = player.getQuest(QUEST_SLOT, 0).split(",")[0];
							npc.say("Strasznie dziś gorąco, nie chce mi się powtarzać. Jeszcze nie zabiłeś " + Grammar.a_noun(questKill)
									+ ". Idź i zrób to i powiedz #załatwione, gdy skończysz.");
					}
				});

		// player killed creature
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(
						new QuestStartedCondition(QUEST_SLOT),
						new QuestNotCompletedCondition(QUEST_SLOT),
				        new KilledForQuestCondition(QUEST_SLOT, 0)),
				ConversationStates.ATTENDING, 
				"Dziękuje za pomoc! Było ciężko.. prawda? Weź ten zwój, przeniesie Cie do krainy pełnej smoków. Jest strasznie kruchy, dlatego wykorzystaj go w trakcie lata zanim się zniszczy...",
				new MultipleActions(
						new IncreaseXPDependentOnLevelAction(5, 35.0),
						new IncreaseKarmaAction(50.0),
						new IncreaseAtkXPAction(1000),
						new IncreaseDefXPAction(1000),
						new EquipItemAction("zwój na smocze pustkowia 2024", 1),
						new IncrementQuestAction(QUEST_SLOT, 2, 1),
						new SetQuestToTimeStampAction(QUEST_SLOT,1),
						new SetQuestAction(QUEST_SLOT,0,"done")
		));
	}

	/**
	 * player said "another"
	 */
	private void step_4() {
		// player have no active quest and trying to get another
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES, 
				new OrCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestCompletedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING, 
				"Chyba będe miał dla Ciebie jedno #zadanie", 
				null);
		
		// player have no expired quest 
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES, 
				new NotCondition( 
						new TimePassedCondition(QUEST_SLOT, 1, expireDelay)),
				ConversationStates.ATTENDING, 
				"Nie minęło zbyt wiele czasu od rozpoczęcia zadania. Nie pozwolę Ci poddać się tak szybko.", 
				null);
		
		// player have expired quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES, 
				new TimePassedCondition(QUEST_SLOT, 1, expireDelay),
				ConversationStates.ATTENDING, 
				null, 
				new daily_monster_summer_2024QuestAction());
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Dzienne zadanie u OrenA",
				"Oren potrzebuje pomocy z przerażającym potworem.",
				true);
		step_1();
		step_2();
		step_3();
		step_4();
	}

	@Override
	public String getName() {
		return "daily_monster_summer_2024";
	}
	
	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,delay)).fire(player, null, null);
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Oren";
	}
}
