/* $Id: KillChaos.java,v 1.32 2020/12/06 16:11:19 davvids Exp $ */
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
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.Pair;


/**
 * QUEST: KillChaos
 *
 * PARTICIPANTS: <ul>
 * <li> Think
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

 public class KillChaos extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeHuntDetails {

	private static final String QUEST_NPC = "Think";
	private static final String QUEST_SLOT = "kill_chaos";
	private static final int delay = MathHelper.MINUTES_IN_ONE_WEEK;
	
	protected HashMap<String, Pair<Integer, String>> enemyForces = new HashMap<String, Pair<Integer,String>>();
	protected HashMap<String, List<String>> enemys = new HashMap<String, List<String>>();




	public KillChaos() {
		super();
		// fill monster types map
		enemyForces.put("chaosów",
				new Pair<Integer, String>(1000,"Przejdź dalej do tuneli zajętych przez armię chaosu. Do wspólnej sumy wliczam piechotę, dowódców, czarnoksiężników i jeźdźców z wykazu; pozostali mieszkańcy podziemi nie zastąpią tych oddziałów."));

		enemys.put("chaosów",
				Arrays.asList("żołnierz chaosu",
							  "jeździec chaosu na czerwonym smoku",
							  "wojownik chaosu",
							  "komandor chaosu",
							  "czarnoksiężnik chaosu",
							  "jeździec smoków chaosu",
							  "lord chaosu",
							  "chaosu lord wywyższony",
							  "jeździec chaosu na zielonym smoku"));

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
			speakerNPC.say("Potrzebujemy miejsca, żeby rozbudować nasze podziemne osiedle. Dalej stoją jednak oddziały chaosu, a nikt rozsądny nie poprowadzi tam ludzi bez zabezpieczenia tuneli.\nZlecenie: " + monstersType + ". Łącznie: " + enemyForces.get(monstersType).first() + " zwycięstw. " + enemyForces.get(monstersType).second());
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
			int moneyreward = 41473*Rand.roll1D6();
			if(killed == killsnumber) {
				// player killed no more no less then needed soldiers
				speakerNPC.say("Dzięki twojej wyprawie możemy myśleć o dalszej pracy w tunelach. Przyjmij pieniądze i przygotowane zapasy; następne zlecenie będzie możliwe po tygodniu.");
			} else {
				// player killed more then needed soldiers
				speakerNPC.say("Dzięki twojej wyprawie możemy myśleć o dalszej pracy w tunelach. Przyjmij pieniądze i przygotowane zapasy; następne zlecenie będzie możliwe po tygodniu. Ponad wymagany cel: " + (killed-killsnumber) + ".");
			}
			int karmabonus = 1*(1*killed/(killsnumber)-1);
			final StackableItem money = (StackableItem)
					SingletonRepository.getEntityManager().getItem("money");
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
            npc.say("Jeszcze nie osiągnęliśmy celu tej wyprawy. Sprawdźmy aktualny meldunek.\n"
                + games.stendhal.server.core.rp.quest.HuntQuestProgress.progressText(KillChaos.this,player));
        }
    }

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
				Arrays.asList("grupy", "potwory"),
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
				Arrays.asList("grupy", "potwory"),
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
		    				  new IncreaseXPAction(3000000),
							  new IncreaseAtkXPAction(15000),
							  new IncreaseDefXPAction(20000),
							  new EquipItemAction("zwój chaosu", 8, true),
							  new EquipItemAction("skrzynia skarbów VI", 1, true),
							  new EquipItemAction("skrzynia skarbów VI", 1, true),
							  new EquipItemAction("skrzynia skarbów VI", 1, true),
							  new EquipItemAction("skrzynia skarbów VI", 1, true),
							  new EquipItemAction("skrzynia skarbów VII", 1, true),
							  new EquipItemAction("skrzynia skarbów VII", 1, true),
							  new EquipItemAction("skrzynia skarbów VII", 1, true),
							  new EquipItemAction("skrzynia skarbów VII", 1, true),
							  new EquipItemAction("skrzynia skarbów VIII", 1, true),
							  new EquipItemAction("skrzynia skarbów VIII", 1, true),
							  new EquipItemAction("skrzynia skarbów VIII", 1, true),
							  new EquipItemAction("skrzynia skarbów VIII", 1, true),
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
				"Tunele Chaosów",
				"Think potrzebuje pomocy w oczyszczeniu dalszych tuneli z oddziałów chaosu. Ustal cele wyprawy, zanim wyruszysz torować drogę jego społeczności.",
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
		return("KillChaos");
	}
	
	@Override
	public int getMinLevel() {
		return 210;
	}	
	
	@Override
	public boolean isRepeatable(final Player player) {
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,delay)).fire(player, null, null);
	}
	
 	@Override
 	public List<String> getHistory(final Player player) {
        return games.stendhal.server.core.rp.quest.HuntQuestProgress.history(this,player);
    }

	@Override
	public String getNPCName() {
		return "Think";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}

    @Override public java.util.Map<String, Pair<Integer,Integer>> getDialogueHuntTargets(Player player) {
        java.util.Map<String, Pair<Integer,Integer>> result=new java.util.LinkedHashMap<String, Pair<Integer,Integer>>();
        String group=player.getQuest(QUEST_SLOT,1);
        if("start".equals(player.getQuest(QUEST_SLOT,0))&&enemyForces.containsKey(group))
            for(String creature:enemys.get(group))result.put(creature,new Pair<Integer,Integer>(0,0));
        return java.util.Collections.unmodifiableMap(result);
    }
    @Override public int getDialogueHuntTotal(Player player) { String group=player.getQuest(QUEST_SLOT,1);return "start".equals(player.getQuest(QUEST_SLOT,0))&&enemyForces.containsKey(group)?enemyForces.get(group).first():0; }
    @Override public int getDialogueKillIndex() { return 2; }
    @Override public String getDialogueHuntHint(Player player) { String group=player.getQuest(QUEST_SLOT,1);return "start".equals(player.getQuest(QUEST_SLOT,0))&&enemyForces.containsKey(group)?enemyForces.get(group).second():"Omów ze mną wyprawę. Po przyjęciu zlecenia poznasz aktualną grupę przeciwników."; }
}
