/* $Id: LookingAtHorse.java,v 1.25 2025/08/06 19:22:37 davvids Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2025 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import games.stendhal.common.Direction;
import games.stendhal.common.MathHelper;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.CollectRequestedItemsAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.TeleportAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class LookingAtHorse extends AbstractQuest {

    private static final String QUEST_SLOT = "horse_looking";
    private static final String NEEDED_ITEMS = "money=1000";

    @Override
    public String getSlotName() {
        return QUEST_SLOT;
    }

    private void step_1() {
        final SpeakerNPC npc = npcs.get("Heniu");

        // explicit keyword trigger
        npc.add(ConversationStates.ATTENDING,
                Arrays.asList("zerknij"),
                new QuestNotStartedCondition(QUEST_SLOT),
                ConversationStates.QUEST_OFFERED,
                "Masz ochotę zerknąć na mojego rumaka? Raz na tydzień, bo królewski fiskus to odnotuje.",
                null);

        // Offer first visit (also on QUEST_MESSAGES)
        npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new QuestNotStartedCondition(QUEST_SLOT),
                ConversationStates.QUEST_OFFERED,
                "Masz ochotę zerknąć na mojego rumaka? Raz na tydzień, bo królewski fiskus to odnotuje.",
                null);

        // Too soon
        npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(
                        new QuestCompletedCondition(QUEST_SLOT),
                        new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK))),
                ConversationStates.ATTENDING,
                null,
                new SayTimeRemainingAction(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK,
                        "Na razie nie wpuszczam. Wróć za "));

        // After a week
        npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(
                        new QuestCompletedCondition(QUEST_SLOT),
                        new TimePassedCondition(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK)),
                ConversationStates.QUEST_OFFERED,
                "Rumak już wypoczął. Zwykła opłata 1000 złota. Zerkniesz jeszcze raz?",
                null);

        // Already active
        npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new QuestActiveCondition(QUEST_SLOT),
                ConversationStates.ATTENDING,
                "Poczekaj chwilkę, już cię wpuściłem.",
                null);

        // Accept with payment
        npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.YES_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "Dobra, wpuszczam cię. Spójrz i idź, bo dłużej tu nie można.",
                new MultipleActions(
                        new SetQuestAction(QUEST_SLOT, NEEDED_ITEMS),
                        new CollectRequestedItemsAction(
                                "money", QUEST_SLOT,
                                "Wpłaciłeś 1000 złota.",
                                null,
                                new MultipleActions(
                                        new TeleportAction("int_bandit_stable", 5, 13, Direction.UP),
                                        new SetQuestAction(QUEST_SLOT, "done;"),
                                        new SetQuestToTimeStampAction(QUEST_SLOT, 1)
                                ),
                                ConversationStates.ATTENDING
                        )
                ));

        // Reject
        npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.NO_MESSAGES,
                null,
                ConversationStates.ATTENDING,
                "Szkoda, rumak się obrazi. A królewski fiskus... odsapnie.",
                new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        step_1();
        fillQuestInfo(
                "Zdjęcie Rumaka",
                "Heniu, chytry stajenny, pilnuje rumaka i wpuszcza tylko raz na tydzień za 1000 złota.",
                true);
    }

    @Override
    public List<String> getHistory(final Player player) {
        final List<String> res = new ArrayList<String>();
        if (!player.hasQuest(QUEST_SLOT)) {
            return res;
        }
        final String questState = player.getQuest(QUEST_SLOT);
        res.add("Heniu zaoferował mi oglądanie swojego rumaka.");
        if ("rejected".equals(questState)) {
            res.add("Nie chciałem płacić, a skarbiec królewski odsapnął.");
            return res;
        }
        if (isRepeatable(player)) {
            res.add("Rumak znowu gotowy, mam ochotę spojrzeć znów.");
        } else {
            res.add("Za wcześnie, Heniu kazał wrócić później.");
        }
        return res;
    }

    @Override
    public String getName() {
        return "LookingAtHorse";
    }

    @Override
    public int getMinLevel() {
        return 10;
    }

    @Override
    public boolean isRepeatable(final Player player) {
        return new AndCondition(
                new QuestCompletedCondition(QUEST_SLOT),
                new TimePassedCondition(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK))
                .fire(player, null, null);
    }

    @Override
    public String getNPCName() {
        return "Heniu";
    }
}
