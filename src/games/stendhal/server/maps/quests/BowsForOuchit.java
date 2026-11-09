/* $Id: BowsForOuchit.java,v 1.10 2011/11/13 17:12:18 kymara Exp $ */
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

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DialogueHintContextAction;
import games.stendhal.server.entity.npc.action.DialogueNarrationAction;
import games.stendhal.server.entity.npc.action.DialogueRewardAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Bows for Ouchit
 * 
 * PARTICIPANTS:
 * <ul>
 * <li> Ouchit, ranged items seller</li>
 * <li> Karl, farmer</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Ouchit asks for polano for his bows and arrows. </li>
 * <li> Puchit asks you to fetch horse hair from Karl also.</li>
 * <li> Return and you get some equipment as reward.<li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li> 1 XP<li>
 * <li> Scale armor</li>
 * <li> Chain legs</li>
 * <li> Karma: 14<li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li> None.</li>
 * </ul>
 */

public class BowsForOuchit extends AbstractQuest {

	public static final String QUEST_SLOT = "bows_ouchit";

	public void prepareQuestStep() {

		/*
		 * get a reference to the Ouchit NPC
		 */
		SpeakerNPC npc = npcs.get("Ouchit");

		/*
		 * Add a reply on the trigger phrase "quest" to Ouchit
		 */
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUESTION_1,
				"Przy łukach i strzałach zawsze znajdzie się coś do zrobienia. Chętnie dokończyłbym nową partię, ale muszę najpierw uzupełnić materiały. Znajdziesz chwilę, żeby mi pomóc?",
				new DialogueNarrationAction("Ouchit na chwilę odkłada swoją pracę."));

		/*
		 * Player is interested in helping, so explain the quest.
		 */
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Do nowych strzał potrzebuję dziesięciu polan. Zajmę się ich obróbką, ale ktoś musi przynieść mi drewno, żebym mógł zostać przy warsztacie. Podejmiesz się tego?",
				null);

		/*
		 * Player refused to help - end the conversation.
		 */
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				"Rozumiem. Może innym razem. Do zobaczenia.",
				null);

		/*
		 * Player agreed to get polano, so tell them what they'll need to say
		 */
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Dziękuję, to pozwoli mi spokojnie zająć się robotą. Zbierz dziesięć polan i przynieś je wszystkie razem. Kiedy wrócisz, przypomnij mi o drewnie.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "polano", 2.0));

		/*
		 * Player asks about polano.
		 */
		npc.add(ConversationStates.QUEST_OFFERED,
				"polano",
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Drewna szukaj w lesie. Potrzebuję dziesięciu polan na nowe strzały. Pomożesz mi je zebrać?",
				null);

		/*
		 * Player refused to help - end the conversation.
		 */
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				"Dobrze, możesz wrócić później, jeżeli chcesz. Do zobaczenia.",
				null);
	}

	public void bringpolanoStep() {

		/*
		 * get a reference to the Ouchit NPC
		 */
		SpeakerNPC npc = npcs.get("Ouchit");
		
		/*
		 * Player asks about quest, remind what they're doing
		 */
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT,"polano"),
				ConversationStates.ATTENDING,
				"Wciąż czekam na dziesięć polan, z których przygotuję nowe strzały. Przynieś je wszystkie razem, żebym mógł od razu zabrać się do obróbki. Jeśli szukasz drewna, rozejrzyj się w lesie.",
				null);
		
		/*
		 * Player asks about polano, but hasn't collected any - remind them.
		 */
		npc.add(ConversationStates.ATTENDING,
				"polano",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT,"polano"),
								 new NotCondition (new PlayerHasItemWithHimCondition("polano",10))),
				ConversationStates.ATTENDING,
				"Widzę, że nie masz jeszcze przy sobie dziesięciu polan. Potrzebuję całej partii naraz, więc rozejrzyj się za drewnem w lesie i wróć, gdy zbierzesz resztę. Nie spiesz się kosztem własnego bezpieczeństwa.",
				null);

		/*
		 * Player asks about polano, and has collected some - take it and
ask for horse hair.
		 */
		npc.add(ConversationStates.ATTENDING,
				"polano",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT,"polano"),
								new PlayerHasItemWithHimCondition("polano",10)),
				ConversationStates.ATTENDING,
				"Dziękuję, te polana wystarczą mi na zaplanowaną partię strzał. Do łuku potrzebuję jeszcze cięciwy z końskiego włosa. " +
				"Pomoże nam #Karl, który prowadzi farmę na wschód od Semos - powiedz mu, że przysyła cię Ouchit.",
				new MultipleActions(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "hair", 2.0), new DropItemAction("polano", 10),
						new DialogueNarrationAction("Ouchit ogląda przyniesione polana.")));

		/*
		 * For simplicity, respond to 'Karl' at any time.
		 */
		npc.add(ConversationStates.ATTENDING, "Karl", null, ConversationStates.ATTENDING,
				"Karl prowadzi farmę na wschód od Semos. Hoduje także konie. Powiedz mu, że przysyła cię Ouchit, a pomoże nam zdobyć włos na cięciwę.",
				new DialogueHintContextAction("ouchit_karl"));
		npc.add(ConversationStates.ATTENDING, "farma", null, ConversationStates.ATTENDING,
				"Farmę Karla znajdziesz na wschód od Semos. Powiedz mu, że przysyła cię Ouchit po koński włos na cięciwę.", null);
	}

	public void getHairStep() {

		/*
		 * get a reference to the Karl NPC
		 */
		SpeakerNPC npc = npcs.get("Karl");

		npc.add(ConversationStates.ATTENDING,
				"Ouchit",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT,"hair"),
								new NotCondition (new PlayerHasItemWithHimCondition("koński włos",1))),
				ConversationStates.ATTENDING,
				"Ouchit znów potrzebuje włosia do łuku? Dobrze, mam koński włos, który możesz mu zanieść. " +
				"Przekaż mu pozdrowienia i pilnuj tego drobiazgu, żeby nie zginął ci po drodze.",
				new MultipleActions(new EquipItemAction("koński włos"),
						new DialogueNarrationAction("Karl podaje ci koński włos.")));

	}

	public void bringHairStep() {

		/*
		 * get a reference to the Ouchit NPC
		 */
		SpeakerNPC npc = npcs.get("Ouchit");

		/*
		 * Player asks about quest, remind what they're doing
		 */
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT,"hair"),
				ConversationStates.ATTENDING,
				"Drewno już mam, ale bez cięciwy łuk pozostanie niedokończony. Brakuje mi jeszcze materiału: #'włosie końskie'. Odwiedź Karla na farmie na wschód od Semos i powiedz, że cię przysyłam.",
				null);
		
		/*
		 * Player asks about horse hair, but hasn't collected any - remind them.
		 */
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("hair", "horse", "włosie końskie", "włosie", "końskie", "koński włos"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT,"hair"),
								new NotCondition (new PlayerHasItemWithHimCondition("koński włos"))),
				ConversationStates.ATTENDING,
				"Nie masz jeszcze końskiego włosa. Potrzebuję go na cięciwę. Pomoże nam #Karl - odwiedź go na farmie i powiedz moje imię.",
				null);

		/*
		 * These actions are part of the reward
		 */
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(DialogueRewardAction.item("łuk ouchita", 1, true));
		reward.add(DialogueRewardAction.item("strzała", 500, true));
		reward.add(DialogueRewardAction.item("strzała żelazna", 300, true));
		reward.add(DialogueRewardAction.xp(1000));
		reward.add(new DropItemAction("koński włos"));
		reward.add(DialogueRewardAction.karma(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "done", 10.0)));
		reward.add(new DialogueNarrationAction("Ouchit przygląda się włosiu i z zadowoleniem kiwa głową."));
		
		/*
		 * Player asks about horse hair, and has collected some - take it
and ask for horse hair.
		 */
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("hair", "horse", "horse hairs", "włosie końskie", "włosie", "końskie"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT,"hair"),
								new PlayerHasItemWithHimCondition("koński włos")),
				ConversationStates.ATTENDING,
				"Wspaniale, właśnie takiego włosia potrzebowałem na cięciwę. Dzięki tobie mogę dokończyć pracę przy łuku i strzałach. " +
				"Przyjmij mój łuk i zapas strzał - wolę, żeby służyły komuś w drodze, niż leżały u mnie bez pożytku.",
				new MultipleActions(reward));
		
		/*
		 * Player asks about quest, and it is finished
		 */
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Dzięki twojej pomocy mam już materiały, których potrzebowałem. Mam nadzieję, że łuk dobrze ci służy. Jeśli chcesz uzupełnić zapas strzał, zapytaj o moją #ofertę.",
				null);
		
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		prepareQuestStep();
		bringpolanoStep();
		getHairStep();
		bringHairStep();
		fillQuestInfo(
				"Łuki dla Ouchita",
				"Ouchit z tawerny w Semos potrzebuje materiałów do wyrobu łuków i strzał.",
				false);
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);
		res.add("Ouchit poprosił mnie o materiały do wyrobu łuków i strzał.");
		if ("polano".equals(questState)) {
			res.add(player.isEquipped("polano", 10)
					? "Mam przy sobie 10 polan. Muszę je jeszcze oddać Ouchitowi, mówiąc polano."
					: "Muszę zebrać 10 polan dla Ouchita. Gdy wrócę, mam powiedzieć polano.");
		}
		if(player.isQuestInState(QUEST_SLOT, "hair", "done")) {
			res.add("Ouchit przyjął drewno. Do cięciwy potrzebuje jeszcze końskiego włosa i polecił mi odwiedzić Karla na farmie na wschód od Semos.");
		}
		if (player.isEquipped("koński włos") && "hair".equals(questState)) {
			res.add("Mam koński włos. Muszę zanieść go Ouchitowi i powiedzieć włosie.");
		}
		if (isCompleted(player)) {
			res.add("Dostarczyłem Ouchitowi drewno i koński włos. W podzięce otrzymałem: 1000 pkt doświadczenia, łuk Ouchita, 500 strzał i 300 strzał żelaznych.");
		}
		return res;
	}
	
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "BowsForOuchit";
	}
	
	@Override
	public int getMinLevel() {
		return 0;
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Ouchit";
	}
}
