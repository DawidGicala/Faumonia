/* $Id: KillKika.java,v 1.32 2020/12/08 00:23:44 davvids Exp $ */
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
package games.stendhal.server.maps.quests;

import games.stendhal.common.MathHelper;
import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KilledInSumForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.Pair;


/**
 * QUEST: KillKika
 *
 * PARTICIPANTS: <ul>
 * <li> Serh
 * <li> some creatures
 * </ul>
 *
 * STEPS:<ul>
 * <li> Despot asking you to kill some of enemy forces.
 * <li> Kill them and go back to Despot for your reward.
 * </ul>
 *
 *
 * REWARD:<ul>
 * <li> 100k of XP, or 300 karma.
 * <li> random moneys - from 10k to 60k, step 10k.
 * <li> 5 karma for killing 100% creatures
 * <li> 5 karma for killing every 50% next creatures
 * </ul>
 *
 * REPETITIONS: <ul><li> once a week.</ul>
 */

 public class KillKika extends AbstractQuest {

	private static final String QUEST_NPC = "Serh";
	private static final String QUEST_SLOT = "kill_kika";
	private static final int delay = MathHelper.MINUTES_IN_ONE_WEEK;
	
	protected HashMap<String, Pair<Integer, String>> enemyForces = new HashMap<String, Pair<Integer,String>>();
	protected HashMap<String, List<String>> enemys = new HashMap<String, List<String>>();




	public KillKika() {
		super();
		// fill monster types map
		enemyForces.put("orków górskich",
				new Pair<Integer, String>(10000,"Zapomniałeś co mi obiecałeś? Miałeś pokonać armie orków górskich."));
		enemyForces.put("zbójów górskich",
				new Pair<Integer, String>(10000,"Zapomniałeś co mi obiecałeś? Miałeś pokonać armie zbójów górskich."));

		enemys.put("orków górskich",
				Arrays.asList("górski ork",
							  "górski ork łowca",
							  "szef górskich orków",
							  "górski ork wojownik"));
		enemys.put("zbójów górskich",
				Arrays.asList("zbójnik górski goniec",
							  "zbójnik górski złośliwy",
							  "zbójnik górski zwiadowca",
							  "zbójnik górski starszy",
							  "zbójnik górski herszt",
						"zbójnik górski"));

	}

	/**
	 * function for choosing random enemy from map
	 * @return - enemy forces caption
	 */
	protected String chooseRandomEnemys() {
		final List<String> enemyList = new LinkedList<String>(enemyForces.keySet());
		final int enemySize = enemyList.size();
		final int position  = Rand.rand(enemySize);
		return(enemyList.get(position));
	}

	/**
	 * function returns difference between recorded number of enemy creatures
	 *     and currently killed creatures numbers.
	 * @param player - player for who we counting this
	 * @return - number of killed enemy creatures
	 */
	private int getKilledCreaturesNumber(final Player player) {
		int count = 0;
		String temp;
		int solo;
		int shared;
		int recsolo;
		int recshared;
		final String enemyType = player.getQuest(QUEST_SLOT,1);
		final List<String> monsters = Arrays.asList(player.getQuest(QUEST_SLOT,2).split(","));
		final List<String> creatures = enemys.get(enemyType);
		for(int i=0; i<creatures.size(); i++) {
			String tempName = creatures.get(i);
			temp = monsters.get(i*5+3);
			if (temp == null) {
				recsolo = 0;
			} else {
				recsolo = Integer.parseInt(temp);
			}
			temp = monsters.get(i*5+4);
			if (temp == null) {
				recshared = 0;
			} else {
				recshared = Integer.parseInt(temp);
			}

			temp = player.getKeyedSlot("!kills", "solo."+tempName);
			if (temp==null) {
				solo = 0;
			} else {
				solo = Integer.parseInt(temp);
			}

			temp = player.getKeyedSlot("!kills", "shared."+tempName);
			if (temp==null) {
				shared = 0;
			} else {
				shared = Integer.parseInt(temp);
			}

			count = count + solo - recsolo + shared - recshared;
		}
		return(count);
	}


	class GiveQuestAction implements ChatAction {
		/**
		 * function will update player quest slot.
		 * @param player - player for which we will record quest.
		 */
		public void fire(final Player player, final Sentence sentence, final EventRaiser speakerNPC) {
			final String monstersType = chooseRandomEnemys();
			speakerNPC.say("Oni tam są... pomocy... u góry... zabij grupę " + monstersType +
					" Zabij co najmniej " + enemyForces.get(monstersType).first()+
					" "+ monstersType +
					" prosze...");
			final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
			List<String> sortedcreatures = enemys.get(monstersType);
			player.setQuest(QUEST_SLOT, 0, "start");
			player.setQuest(QUEST_SLOT, 1, monstersType);
			for(int i=0; i<sortedcreatures.size(); i++) {
				toKill.put(sortedcreatures.get(i), new Pair<Integer, Integer>(0,0));
			}
			new StartRecordingKillsAction(QUEST_SLOT, 2, toKill).fire(player, sentence, speakerNPC);
		}
	}

	class RewardPlayerAction implements ChatAction {
		/**
		 * function will complete quest and reward player.
		 * @param player - player to be rewarded.
		 */
		public void fire(final Player player, final Sentence sentence, final EventRaiser speakerNPC) {
			final String monsters = player.getQuest(QUEST_SLOT, 1);
			int killed=getKilledCreaturesNumber(player);
			int killsnumber = enemyForces.get(monsters).first();
			int moneyreward = 2*Rand.roll1D6();
			if(killed == killsnumber) {
				// player killed no more no less then needed soldiers
				speakerNPC.say("Dziękuje!!! Weź te zwoje w ramach podziękowania. Jeszcze raz dziękuje! Myśle, że za tydzień będe miał dla ciebie kolejne zadanie.");
			} else {
				// player killed more then needed soldiers
				speakerNPC.say("Dziękuje!!! Zabiłeś "+(killed-killsnumber)+" "+
						Grammar.plnoun(killed-killsnumber, "więcej")+"! Weź te zwoje w ramach podziękowania. Jeszcze raz dziękuje! . Myśle, że za tydzień będe miała dla ciebie kolejne zadanie.");
			}
			int karmabonus = 1*(1*killed/(killsnumber)-1);
			final StackableItem money = (StackableItem)
					SingletonRepository.getEntityManager().getItem("bilet do niebios");
			money.setQuantity(moneyreward);

			player.equipOrPutOnGround(money);
			player.addKarma(karmabonus);
		
		}
	}



	/**
	 * class for quest talking.
	 */
	class ExplainAction implements ChatAction {

		public void fire(Player player, Sentence sentence, EventRaiser npc) {
				final String monsters = player.getQuest(QUEST_SLOT, 1);
				int killed=getKilledCreaturesNumber(player);
				int killsnumber = enemyForces.get(monsters).first();

				if(killed==0) {
					// player killed no creatures but asked about quest again.
					npc.say("Pomocy! Zapomniałeś? Obiecałeś, że rozprawisz się z grupą " + monsters + "!");
					return;
				}
				if(killed < killsnumber) {
					// player killed less then needed soldiers.
					npc.say("Zabiłeś tylko "+killed+" "+Grammar.plnoun(killed, player.getQuest(QUEST_SLOT, 1))+
							". Prosze pomóż... Zabij co najmniej "+killsnumber+" "+Grammar.plnoun(killed, player.getQuest(QUEST_SLOT, 1)));
					return;
				}

		}
	}

	/**
	 * add quest state to npc's fsm.
	 */
	private void step_1() {
		
		SpeakerNPC npc = npcs.get(QUEST_NPC);

		// quest can be given
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new OrCondition(
					new QuestNotStartedCondition(QUEST_SLOT),
					new AndCondition(
						new QuestCompletedCondition(QUEST_SLOT),
						new TimePassedCondition(QUEST_SLOT, 1, delay))),
				ConversationStates.ATTENDING,
				null,
				new GiveQuestAction());

		// time is not over
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new QuestCompletedCondition(QUEST_SLOT),
						new NotCondition(
								new TimePassedCondition(QUEST_SLOT, 1, delay))),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, delay, "Kolejne zadanie otrzymasz za"));

		// explanations
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("zadanie", "task", "grupy", "potwory"),
				new QuestInStateCondition(QUEST_SLOT, 0, "start"),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
						public void fire(Player player, Sentence sentence, EventRaiser npc) {
							npc.say(enemyForces.get(player.getQuest(QUEST_SLOT, 1)).second());
						}
				});

		// explanations
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("zadanie", "task", "grupy", "potwory"),
				new QuestNotInStateCondition(QUEST_SLOT, 0, "start"),
				ConversationStates.ATTENDING,
				"Na razie nie mam zadania dla ciebie, przyjdź za jakiś czas.",
				null);

		// checking for kills
		final List<String> creatures = new LinkedList<String>(enemyForces.keySet());
		for(int i=0; i<enemyForces.size(); i++) {
			final String enemy = creatures.get(i);

			  // player killed enough enemies.
		      npc.add(ConversationStates.ATTENDING,
		    		  ConversationPhrases.QUEST_MESSAGES,
		    		  new AndCondition(
		    				  new QuestInStateCondition(QUEST_SLOT, 1, enemy),
		    				  new KilledInSumForQuestCondition(QUEST_SLOT, 2, enemyForces.get(enemy).first())),
		    		  ConversationStates.ATTENDING,
		    		  null,
		    		  new MultipleActions(
		    				  new RewardPlayerAction(),
		    				  new IncreaseXPAction(5000000),
							  new IncreaseAtkXPAction(150000),
							  new IncreaseDefXPAction(200000),
		    				  new IncrementQuestAction(QUEST_SLOT,3,1),
		    				  // empty the 2nd index as we use it later
		    				  new SetQuestAction(QUEST_SLOT,2,""),
		    				  new SetQuestToTimeStampAction(QUEST_SLOT,1),
		    				  new SetQuestAction(QUEST_SLOT,0,"done")));

		      // player killed not enough enemies.
		      npc.add(ConversationStates.ATTENDING,
		    		  ConversationPhrases.QUEST_MESSAGES,
		    		  new AndCondition(
		    				  new QuestInStateCondition(QUEST_SLOT, 1, enemy),
		    				  new NotCondition(
		    						  new KilledInSumForQuestCondition(QUEST_SLOT, 2, enemyForces.get(enemy).first()))),
		    		  ConversationStates.ATTENDING,
		    		  null,
		    		  new ExplainAction());

		}
	}

	/**
	 * add quest to the Stendhal world.
	 */
	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Przerażony Krasnolud",
				"Krasnolud Serh panicznie boi się wyjść z kopalni, może powinienem mu pomóc?",
				true);
		step_1();
	}

	/**
	 * return name of quest slot.
	 */
	public String getSlotName() {
		return(QUEST_SLOT);
	}

	/**
	 * return name of quest.
	 */
	public String getName() {
		return("KillKika");
	}
	
	@Override
	public int getMinLevel() {
		return 320;
	}	
	
	@Override
	public boolean isRepeatable(final Player player) {
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,delay)).fire(player, null, null);
	}
	
 	@Override
 	public List<String> getHistory(final Player player) {
 		LinkedList<String> history = new LinkedList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return history;
		}
		
		if(player.getQuest(QUEST_SLOT, 0).equals("start")) {
	        final String givenEnemies = player.getQuest(QUEST_SLOT, 1);
	        final int givenNumber = enemyForces.get(givenEnemies).first(); 
	        final int killedNumber = getKilledCreaturesNumber(player);
	        
			history.add("Serh błagał mnie o zabicie "+
					givenNumber+" "+
					Grammar.plnoun(givenNumber, givenEnemies));
			String kn = Integer.valueOf(killedNumber).toString();
			if(killedNumber == 0) {
				kn="no";
			}
			history.add("Aktualnie zabiłem "+
					kn+" "+
					Grammar.plnoun(killedNumber, givenEnemies));
			if(new KilledInSumForQuestCondition(QUEST_SLOT, 2, givenNumber).fire(player, null, null)) {
				history.add("Zabiłem wystaczająco dużo potworów, aby dostać moją nagrodę.");
			} else {
				history.add("Zostało "+(givenNumber-killedNumber)+" "+
						Grammar.plnoun(givenNumber-killedNumber, givenEnemies)+" do zabicia.");	
			}
		}
		
		if(isCompleted(player)) {
			history.add("Pomogłem przerażonemu krasnoludowi Serhowi i otrzymałem zwoje!");
		}	
		if (isRepeatable(player)) {
			history.add("Krasnolud Serh znowu potrzebuje pomocy.");
		} 
		int repetitions = player.getNumberOfRepetitions(getSlotName(), 3);
		if (repetitions > 0) {
			history.add("Pokonałem grupę "
					+ Grammar.quantityplnoun(repetitions, "") + " dla krasnoluda.");
		}
		return history; 
 	}

	@Override
	public String getNPCName() {
		return "Serh";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
}

