/* $Id: LearnAboutOrbs.java,v 1.14 2011/11/13 17:14:15 kymara Exp $ */
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

import games.stendhal.server.entity.npc.action.DialogueRewardAction;

import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * QUEST: Learn about Orbs
 * 
 * PARTICIPANTS:
 * <ul>
 * <li>Ilisa, the summon healer in Semos temple</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li>Ilisa offers to teach you about orbs</li>
 * <li>You use the orb</li>
 * <li>You tell her if you were successful.</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li>50 XP</li>
 * <li>Ability to use orb in semos temple which teleports you outside into city</li>
 * <li>Ability to use other orbs e.g. in orril lich palace</li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li>Can always learn about orbs but not get the xp each time</li>
 * </ul>
 */
public class LearnAboutOrbs extends AbstractQuest {

	private static final String QUEST_SLOT = "learn_scrying";


	
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
		res.add("Spotkałem Ilisę w świątyni w Semos.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("done")) {
			res.add("Ilisa pokazała mi jak używać kul. Muszę uważać, ponieważ mogą mnie wysłać w inne niebezpieczne miejsce.");
		}
		return res;
	}

	private void step1() {
		final SpeakerNPC npc = npcs.get("Ilisa");
		
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED, 
			"Ta kula na stole nie jest zwykłą ozdobą. Niektóre kule pozwalają zajrzeć w odległe miejsca, a inne potrafią przenieść wędrowca daleko stąd. Mogę pokazać ci, jak ich #używać i na co uważać.", null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"Pamiętasz naszą lekcję o kulach? Jeśli coś ci umknęło, chętnie przypomnę, jak ich #używać. Przy magii ostrożność przydaje się bardziej niż pośpiech.", null);

		// player interested in orb
		npc.add(ConversationStates.QUEST_OFFERED,
			Arrays.asList("use", "używać"), 
			new LevelGreaterThanCondition(10),
			ConversationStates.QUESTION_1,
			"Podejdź tuż do kuli na stole, kliknij ją prawym przyciskiem i wybierz Użyj. Przeczytaj wiadomość, która się pojawi - przed dotknięciem obcej kuli warto poznać jej działanie. Czy udało ci się odczytać wiadomość?",
			null);

		// player interested in orb but level < 10
		npc.add(ConversationStates.QUEST_OFFERED,
			Arrays.asList("use", "używać"), 
			new NotCondition(new LevelGreaterThanCondition(10)),
			ConversationStates.ATTENDING,
			"Na tę lekcję przyjdzie pora, gdy osiągniesz 11. poziom. Najpierw oswój się z okolicą i nabierz doświadczenia, żeby magiczna podróż nie zaskoczyła cię zbyt daleko od domu. Wróć wtedy, a spokojnie przećwiczymy korzystanie z kul.",
			null);

		// player wants reminder on Use
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("use", "używać"),
			null,
			ConversationStates.ATTENDING,
			"Stań tuż przy kuli, kliknij ją prawym przyciskiem i wybierz Użyj. Uważnie przeczytaj wiadomość, zanim zrobisz kolejny krok. Kule mogą prowadzić do niebezpiecznych miejsc, więc zawsze pomyśl też o drodze powrotnej.",
			null);

		// player got message from orb
		npc.add(ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Dobrze, właśnie o to chodziło. Znasz już podstawy korzystania z kul, ale pamiętaj, że każda może działać inaczej. Zanim pozwolisz magii przenieść cię dalej, upewnij się, że potrafisz wrócić.",
			new MultipleActions(DialogueRewardAction.xp(50), new SetQuestAction(QUEST_SLOT, "done")));

		// player didn't get message, try again
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES,
			null, ConversationStates.QUESTION_1,
			"Spróbuj stanąć bezpośrednio obok kuli na stole. Kliknij ją prawym przyciskiem, wybierz Użyj i sprawdź wiadomość - z daleka nie dosięgniesz jej mocy. Czy teraz udało ci się ją odczytać?", null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Nauka o Kulach",
				"Ilisa ze świątyni w Semos uczy ostrożnego korzystania z magicznych kul.",
				false);
		step1();

	}

	@Override
	public String getName() {
		return "LearnAboutOrbs";
	}
	
	@Override
	public int getMinLevel() {
		return 11;
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Ilisa";
	}
}
