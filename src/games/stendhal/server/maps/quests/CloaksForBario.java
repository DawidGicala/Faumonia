/* $Id: CloaksForBario.java,v 1.55 2011/12/27 15:17:32 bluelads99 Exp $ */
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
 * QUEST: Cloaks for Bario
 * 
 * PARTICIPANTS:
 * <ul>
 * <li> Bario, a guy living in an underground house deep under the Ados Wildlife Refuge</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Bario asks you for a number of blue elf cloaks.</li>
 * <li> You get some of the cloaks somehow, e.g. by killing elves.</li>
 * <li> You bring the cloaks to Bario and give them to him.</li>
 * <li> Repeat until Bario received enough cloaks. (Of course you can bring up
 * all required cloaks at the same time.)</li>
 * <li> Bario gives you a golden shield in exchange.</li>
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
public class CloaksForBario extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeCollectionDetails {

	private static final int REQUIRED_CLOAKS = 10;

	private static final String QUEST_SLOT = "cloaks_for_bario";


	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Bario");

		// player says hi before starting the quest
		npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotStartedCondition(QUEST_SLOT)),
            ConversationStates.ATTENDING,
            "Hej, ciszej! Jestem Bario i wolę, żeby krasnale nie wiedziały, gdzie się zaszyłem. Skoro już tu jesteś, może pomożesz mi z pewnym zimnym kłopotem?",
            null);
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING,
            "dostawa",
            new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestNotStartedCondition(QUEST_SLOT)),
            ConversationStates.ATTENDING,
            "Hej, ciszej! Jestem Bario i wolę, żeby krasnale nie wiedziały, gdzie się zaszyłem. Skoro już tu jesteś, może pomożesz mi z pewnym zimnym kłopotem?",
            null);

		// player is willing to help
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new QuestNotStartedCondition(QUEST_SLOT),
            ConversationStates.QUEST_OFFERED,
            "Ukradłem krasnalom beczkę piwa i teraz nie śpieszę się wracać na górę. Tutaj jednak chłód wciska się w każdą szczelinę. Przyniesiesz mi płaszcze, żebym nie zamarzł w tej kryjówce?",
            null);

		// player should already be getting cloaks
		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new QuestActiveCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Pod tymi płaszczami robi się już cieplej, ale wciąż czuję chłód. Przynieś resztę lazurowych płaszczy elfickich, a może wreszcie przestanę szczękać zębami.",
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
            "Potrzebuję dziesięciu lazurowych płaszczy elfickich, takich jak te spotykane w Nalwor. Przynoś je po jednym; każda kolejna warstwa osłoni mnie od chłodu. Za pełną dostawę oddam ci przygotowany ekwipunek i zapasy.",
            new SetQuestAndModifyKarmaAction(QUEST_SLOT, Integer.toString(REQUIRED_CLOAKS), 5.0));

		// player is not willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.NO_MESSAGES,
            new games.stendhal.server.entity.npc.condition.QuestNotStartedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Och nie... Będę miał kłopoty...",
            new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	private void step_2() {
		// Just find some of the cloaks somewhere and bring them to Bario.
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Bario");

		// player returns while quest is still active
		npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
            ConversationStates.QUESTION_1,
            null,
            new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Brakuje mi jeszcze lazurowych płaszczy elfickich: " + player.getQuest(QUEST_SLOT) + ". Tymi, które przyniosłeś wcześniej, już się okryłem. Oddasz mi teraz kolejny?");
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
						raiser.say("Brakuje mi jeszcze lazurowych płaszczy elfickich: " + player.getQuest(QUEST_SLOT) + ". Tymi, które przyniosłeś wcześniej, już się okryłem. Oddasz mi teraz kolejny?");
					}
				});

		// player returns after finishing the quest
		npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT)),
            ConversationStates.ATTENDING,
            "Witaj! Jeszcze raz dziękuję za te płaszcze.",
            null);
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING,
            "dostawa",
            new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestCompletedCondition(QUEST_SLOT)),
            ConversationStates.ATTENDING,
            "Witaj! Jeszcze raz dziękuję za te płaszcze.",
            null);

		// player says he doesn't have any blue elf cloaks with him
		npc.add(ConversationStates.QUESTION_1,
            ConversationPhrases.NO_MESSAGES,
            null,
            ConversationStates.ATTENDING,
            "Niedobrze.",
            null);

		// player says he has a blue elf cloak with him but he needs to bring more than one still
		// could also have used GreaterThanCondition for Quest State but this is okay, note we can only get to question 1 if we were active
		npc.add(ConversationStates.QUESTION_1,
            ConversationPhrases.YES_MESSAGES,
            new AndCondition(new QuestNotInStateCondition(QUEST_SLOT, "1"), new PlayerHasItemWithHimCondition("lazurowy płaszcz elficki")),
            ConversationStates.QUESTION_1,
            null,
            new MultipleActions(
						new DropItemAction("lazurowy płaszcz elficki"),
						new ChatAction() {
							public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
								// find out how many cloaks the player still has to
								// bring. incase something has gone wrong and we can't parse the slot, assume it was just started
								final int toBring = MathHelper.parseIntDefault(player.getQuest(QUEST_SLOT),  REQUIRED_CLOAKS) -1;

								player.setQuest(QUEST_SLOT,
										Integer.toString(toBring));
								raiser.say("Przyjmuję ten egzemplarz i odliczam go od dostawy. Pozostało: " + toBring + ". Chcesz przekazać kolejną sztukę?");

							}
						}));
		
		// player says he has a blue elf cloak with him and it's the last one
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("lazurowy płaszcz elficki"));
		reward.add(new EquipItemAction("rękawice Bario", 1, true));
		reward.add(new EquipItemAction("złoty hełm", 1, true));
		reward.add(new EquipItemAction("złota zbroja", 1, true));
		reward.add(new EquipItemAction("złote spodnie", 1, true));
		reward.add(new EquipItemAction("buty złote", 1, true));
		reward.add(new EquipItemAction("złota tarcza", 1, true));
		reward.add(new EquipItemAction("złoty płaszcz", 1, true));
		reward.add(new EquipItemAction("duży eliksir", 100, true));
		reward.add(new IncreaseXPAction(50000));
		reward.add(new IncreaseAtkXPAction(10000));
		reward.add(new IncreaseDefXPAction(10000));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(25));
		npc.add(ConversationStates.QUESTION_1,
            ConversationPhrases.YES_MESSAGES,
            new AndCondition(new QuestInStateCondition(QUEST_SLOT, "1"), new PlayerHasItemWithHimCondition("lazurowy płaszcz elficki")),
            ConversationStates.ATTENDING,
            "Teraz mam dość płaszczy, żeby przetrwać ten chłód. Przyjmij moje rękawice, złoty ekwipunek i eliksiry w podziękowaniu. I jeśli ktoś zapyta o beczkę piwa, najlepiej zmień temat.",
            new games.stendhal.server.entity.npc.action.QuestFeeAction(QUEST_SLOT, new MultipleActions(reward)));
		
		npc.add(ConversationStates.QUESTION_1,
            ConversationPhrases.YES_MESSAGES,
            new NotCondition(new PlayerHasItemWithHimCondition("lazurowy płaszcz elficki")),
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
				"Płaszcze dla Bario",
				"Bario ukrywa się w chłodnych podziemiach Ados i marznie coraz bardziej. Dowiedz się, jakich płaszczy potrzebuje, i pomóż mu osłonić się przed zimnem.",
				false);
	}
	
	@Override
	public List<String> getHistory(final Player player) {
        return games.stendhal.server.core.rp.quest.CollectionQuestProgress.history(this, player);
    }
	
	@Override
	public String getName() {
		return "CloaksForBario";
	}
	
	@Override
	public int getMinLevel() {
		return 60;
	}
	@Override
	public String getNPCName() {
		return "Bario";
	}



    public String getCollectionKind(){return "count";}
    public java.util.Map<String,Integer> getCollectionItems(){return games.stendhal.server.core.rp.quest.JourneyQuestProgress.items("lazurowy płaszcz elficki",REQUIRED_CLOAKS);}
    public String getCollectionHint(){return "Bario potrzebuje dziesięciu lazurowych płaszczy elfickich. Przyjmuje po jednym egzemplarzu, a przekazane płaszcze pozostają odliczone. Inny rodzaj płaszcza nie zastąpi zamówienia.";}
    
}
