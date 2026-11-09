/* $Id: SwordsForNnyddion.java,v 1.55 2011/12/27 15:17:32 bluelads99 Exp $ */
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
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: swords for Nnyddion
 * 
 * PARTICIPANTS:
 * <ul>
 * <li> Nnyddion, a guy living in an underground house deep under the Ados Wildlife Refuge</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Nnyddion asks you for a number of blue elf swords.</li>
 * <li> You get some of the swords somehow, e.g. by killing elves.</li>
 * <li> You bring the swords to Nnyddion and give them to him.</li>
 * <li> Repeat until Nnyddion received enough swords. (Of course you can bring up
 * all required swords at the same time.)</li>
 * <li> Nnyddion gives you a golden shield in exchange.</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li> golden shield</li>
 * <li> 15000 XP</li>
 * <li> Karma: 25</li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li> None.</li>
 * </ul>
 */
public class SwordsForNnyddion extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeCollectionDetails {

	private static final int REQUIRED_SWORDS = 5;

	private static final String QUEST_SLOT = "swords_for_nnyddion";


	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Nnyddion");

		// player says hi before starting the quest
		npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotStartedCondition(QUEST_SLOT)),
            ConversationStates.ATTENDING,
            "Witaj w banku Nalwor, jestem Nnyddion. Muszę pilnować depozytów, więc nie mogę sam załatwić pewnej dostawy. Jeśli masz chwilę, przyda mi się twoja pomoc.",
            null);
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING,
            "dostawa",
            new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestNotStartedCondition(QUEST_SLOT)),
            ConversationStates.ATTENDING,
            "Witaj w banku Nalwor, jestem Nnyddion. Muszę pilnować depozytów, więc nie mogę sam załatwić pewnej dostawy. Jeśli masz chwilę, przyda mi się twoja pomoc.",
            null);

		// player is willing to help
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new QuestNotStartedCondition(QUEST_SLOT),
            ConversationStates.QUEST_OFFERED,
            "Nie mogę opuszczać posterunku, a do depozytu potrzebuję mieczy elfickich. Każda sztuka musi zostać sprawdzona i odnotowana. Pomożesz mi zgromadzić pięć egzemplarzy?",
            null);

		// player should already be getting swords
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new QuestActiveCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Dostawa mieczy elfickich nadal jest otwarta. Wcześniejsze egzemplarze odnotowałem; możemy sprawdzić pozostałe sztuki bez rozpoczynania sprawy od nowa.",
            null);

		// player has already finished the quest
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new QuestCompletedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Nie mam żadnego zadania dla Ciebie.",
            null);

		// player is willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.YES_MESSAGES,
            new games.stendhal.server.entity.npc.condition.QuestNotStartedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Dziękuję, potrzebuję pięciu mieczy elfickich. Przyjmę je pojedynczo, więc nie musisz przynosić wszystkich naraz. Rozglądaj się za elfim wyposażeniem podczas wypraw w okolicach Nalwor.",
            new SetQuestAndModifyKarmaAction(QUEST_SLOT, Integer.toString(REQUIRED_SWORDS), 5.0));

		// player is not willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.NO_MESSAGES,
            new games.stendhal.server.entity.npc.condition.QuestNotStartedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Och nie... Będę miał kłopoty...",
            new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	private void step_2() {
		// Just find some of the swords somewhere and bring them to Nnyddion.
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Nnyddion");

		// player returns while quest is still active
		npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
            ConversationStates.QUESTION_1,
            null,
            new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Pozostała dostawa: miecz elficki - " + player.getQuest(QUEST_SLOT) + ". Mam zapisane wcześniejsze egzemplarze. Przekażesz teraz jedną sztukę?");
					}
				});
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING,
            "dostawa",
            new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestActiveCondition(QUEST_SLOT)),
            ConversationStates.QUESTION_1,
            null,
            new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Pozostała dostawa: miecz elficki - " + player.getQuest(QUEST_SLOT) + ". Mam zapisane wcześniejsze egzemplarze. Przekażesz teraz jedną sztukę?");
					}
				});

		// player returns after finishing the quest
		npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT)),
            ConversationStates.ATTENDING,
            "Witaj! Jeszcze raz dziękuję za te miecze.",
            null);
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING,
            "dostawa",
            new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestCompletedCondition(QUEST_SLOT)),
            ConversationStates.ATTENDING,
            "Witaj! Jeszcze raz dziękuję za te miecze.",
            null);

		// player says he doesn't have any blue elf swords with him
		npc.add(ConversationStates.QUESTION_1,
            ConversationPhrases.NO_MESSAGES,
            null,
            ConversationStates.ATTENDING,
            "Niedobrze.",
            null);

		// player says he has a blue elf sword with him but he needs to bring more than one still
		// could also have used GreaterThanCondition for Quest State but this is okay, note we can only get to question 1 if we were active
		npc.add(ConversationStates.QUESTION_1,
            ConversationPhrases.YES_MESSAGES,
            new AndCondition(new QuestNotInStateCondition(QUEST_SLOT, "1"), new PlayerHasItemWithHimCondition("miecz elficki")),
            ConversationStates.QUESTION_1,
            null,
            new MultipleActions(
						new DropItemAction("miecz elficki"),
						new ChatAction() {
							public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
								// find out how many swords the player still has to
								// bring. incase something has gone wrong and we can't parse the slot, assume it was just started
								final int toBring = MathHelper.parseIntDefault(player.getQuest(QUEST_SLOT),  REQUIRED_SWORDS) -1;

								player.setQuest(QUEST_SLOT,
										Integer.toString(toBring));
								raiser.say("Przyjmuję ten egzemplarz i odliczam go od dostawy. Pozostało: " + toBring + ". Chcesz przekazać kolejną sztukę?");

							}
						}));
		
		// player says he has a blue elf sword with him and it's the last one
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("miecz elficki"));
		reward.add(new EquipItemAction("ruda żelaza", 40, true));
		reward.add(new EquipItemAction("bryłka mithrilu", 10, true));
		reward.add(new EquipItemAction("bryłka złota", 30, true));
		reward.add(new IncreaseXPAction(20000));
		reward.add(new IncreaseAtkXPAction(5000));
		reward.add(new IncreaseDefXPAction(7000));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(25));
		npc.add(ConversationStates.QUESTION_1,
            ConversationPhrases.YES_MESSAGES,
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, "1"), new PlayerHasItemWithHimCondition("miecz elficki")),
            ConversationStates.ATTENDING,
            "Depozyt jest uzupełniony, wszystkie pięć mieczy zostało przyjętych. Weź rudę żelaza oraz bryłki mithrilu i złota w podziękowaniu. Twoja pomoc bankowi będzie się liczyła także przy staraniach o obywatelstwo Nalwor.",
            new MultipleActions(reward));
		
		npc.add(ConversationStates.QUESTION_1,
            ConversationPhrases.YES_MESSAGES,
            new NotCondition(new PlayerHasItemWithHimCondition("miecz elficki")),
            ConversationStates.ATTENDING,
            "Naprawdę? Nie widzę żadnego...",
            null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();

		step_1();
		step_2();
		step_3();
		fillQuestInfo(
				"Miecze dla Nnyddion",
				"Nnyddion z banku Nalwor potrzebuje określonych mieczy do depozytu. Zapytaj o listę i dostarcz brakujące egzemplarze, aby mogła zamknąć zamówienie.",
				false);
	}
	
	@Override
	public List<String> getHistory(final Player player) {
        return games.stendhal.server.core.rp.quest.CollectionQuestProgress.history(this, player);
    }
	
	@Override
	public String getName() {
		return "SwordsForNnyddion";
	}
	
	@Override
	public int getMinLevel() {
		return 60;
	}
	@Override
	public String getNPCName() {
		return "Nnyddion";
	}



    public String getCollectionKind(){return "count";}
    public java.util.Map<String,Integer> getCollectionItems(){return games.stendhal.server.core.rp.quest.JourneyQuestProgress.items("miecz elficki",REQUIRED_SWORDS);}
    public String getCollectionHint(){return "Nnyddion potrzebuje pięciu mieczy elfickich do depozytu bankowego w Nalwor. Przyjmuje je pojedynczo i zapamiętuje każdą dostawę. Ukończenie tej sprawy pomaga w uzyskaniu obywatelstwa Nalwor.";}
    
}
