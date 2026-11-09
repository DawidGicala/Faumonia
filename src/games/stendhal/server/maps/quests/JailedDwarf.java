/* $Id: JailedDwarf.java,v 1.25 2012/04/24 17:01:18 kymara Exp $ */
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

import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * QUEST: Jailed Dwarf
 * 
 * PARTICIPANTS: - Hunel, the guard of the Dwarf Kingdom's Prison
 * 
 * STEPS: - You see Hunel locked in the cell - You get the key by killing the
 * Duergar King - You speak to Hunel when you have the key. - Hunel wants to
 * stay in, he is afraid. - You can then sell chaos equipment to Hunel.
 * 
 * REWARD: - 2000 XP - everlasting place to sell chaos equipment
 * 
 * REPETITIONS: - None.
 */
public class JailedDwarf extends AbstractQuest {

	private static final String QUEST_SLOT = "jailed_dwarf";



	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Hunel");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Krata nie jest już moim największym zmartwieniem, ale wciąż trudno mi zebrać odwagę do wyjścia. Dziękuję za klucz i za to, że tu wracasz. Mogę teraz porozmawiać z tobą o skupie ekwipunku.",
				null);
        // Reuse the native stage and its guards inside the current conversation.
        npc.add(ConversationStates.ATTENDING, "klucz hunela",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestCompletedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Krata nie jest już moim największym zmartwieniem, ale wciąż trudno mi zebrać odwagę do wyjścia. Dziękuję za klucz i za to, że tu wracasz. Mogę teraz porozmawiać z tobą o skupie ekwipunku.",
				null);

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotCompletedCondition(QUEST_SLOT),
						new NotCondition(new PlayerHasItemWithHimCondition("klucz do więzienia Kanmararn"))),
				ConversationStates.ATTENDING,
				"Pomocy! Duergary opanowały więzienie i zamknęły mnie w celi, której sam miałem pilnować. Zdobądź klucz do więzienia Kanmararn i wróć z nim do mnie. Bez niego pozostanę po tej stronie krat.",
				new SetQuestAction(QUEST_SLOT, "start"));
        // Reuse the native stage and its guards inside the current conversation.
        npc.add(ConversationStates.ATTENDING, "klucz hunela",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestNotCompletedCondition(QUEST_SLOT),
						new NotCondition(new PlayerHasItemWithHimCondition("klucz do więzienia Kanmararn"))),
				ConversationStates.ATTENDING,
				"Pomocy! Duergary opanowały więzienie i zamknęły mnie w celi, której sam miałem pilnować. Zdobądź klucz do więzienia Kanmararn i wróć z nim do mnie. Bez niego pozostanę po tej stronie krat.",
				new SetQuestAction(QUEST_SLOT, "start"));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotCompletedCondition(QUEST_SLOT),
						new PlayerHasItemWithHimCondition("klucz do więzienia Kanmararn")),
				ConversationStates.ATTENDING,
				"Masz klucz, aby mnie wypuścić! *mamrocze*  Errrr ... nie wygląda, aby było tam dla mnie bezpiecznie ... Chyba zostanę tutaj ... może ktoś #zaoferuje mi dobry ekwipunek ... ",
				new MultipleActions(new SetQuestAction(QUEST_SLOT, "done"),
						 			 new IncreaseXPAction(10000)));
        // Reuse the native stage and its guards inside the current conversation.
        npc.add(ConversationStates.ATTENDING, "klucz hunela",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestNotCompletedCondition(QUEST_SLOT),
						new PlayerHasItemWithHimCondition("klucz do więzienia Kanmararn")),
				ConversationStates.ATTENDING,
				"Masz klucz, aby mnie wypuścić! *mamrocze*  Errrr ... nie wygląda, aby było tam dla mnie bezpiecznie ... Chyba zostanę tutaj ... może ktoś #zaoferuje mi dobry ekwipunek ... ",
				new MultipleActions(new SetQuestAction(QUEST_SLOT, "done"),
						 			 new IncreaseXPAction(10000)));
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Uwięziony Krasnal",
				"Hunel miał strzec więzienia Kanmararn, lecz po napadzie duergarów sam trafił za kraty. Porozmawiaj z nim o ucieczce i pomóż zdobyć to, czego potrzebuje.",
				true);
		step_1();
	}
	
	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			res.add("Muszę zdobyć klucz aby uwolnić Hunela.");
			if (isCompleted(player)) {
				res.add("Zabiłem króla Duergara i zdobyłem klucz do celi Hunela. Teraz jest zbyt przestraszony aby wyjść. Kupi każdą ilość chaosów Biedny Hunel.	W nagrodę za ukończenie zadania otrzymałem: 10.000 pkt doświadczenia i dostęp do sklepu Hunela.");
			}
			return res;
	}
	
	@Override
	public String getName() {
		return "JailedDwarf";
	}
	
	@Override
	public int getMinLevel() {
		return 60;
	}

	@Override
	public String getNPCName() {
		return "Hunel";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_DUNGEONS;
	}
}
