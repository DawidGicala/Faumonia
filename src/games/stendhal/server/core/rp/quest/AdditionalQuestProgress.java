package games.stendhal.server.core.rp.quest;

import games.stendhal.common.QuestProgress;
import games.stendhal.common.QuestProgress.Objective;
import games.stendhal.common.QuestProgress.State;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.*;
import java.util.*;

/** Presentation only: reads the existing seven quest formats without migrating saves. */
public final class AdditionalQuestProgress {
    private AdditionalQuestProgress() { }
    public static boolean supports(IQuest quest) {
        return quest instanceof PlinksToy || quest instanceof LearnAboutOrbs || quest instanceof HungryJoshua
                || quest instanceof FindJefsMom || quest instanceof CleanStorageSpace
                || quest instanceof Campfire || quest instanceof ElvishArmor;
    }
    public static QuestProgress read(IQuest quest, Player player) {
        if (!supports(quest) || !quest.isStarted(player) || !quest.isVisibleOnQuestStatus()) { return null; }
        String state = player.getQuest(quest.getSlotName());
        if (quest.isCompleted(player)) {
            String next = "Zadanie ukończone.";
            if (quest instanceof Campfire || quest instanceof FindJefsMom) {
                next = quest.isRepeatable(player) ? "Możesz ponownie zapytać o zadanie."
                        : "Zadanie ukończone. Zleceniodawca potrzebuje przerwy przed kolejną prośbą.";
            }
            return snapshot(quest, State.COMPLETED, "", next, Collections.<Objective>emptyList());
        }
        if ("rejected".equals(state) || "rejected".equals(player.getQuest(quest.getSlotName(), 0))) {
            return snapshot(quest, State.DECLINED, quest.getNPCName(), "Jeśli zmienisz zdanie, wróć do rozmowy o zadaniu.", Collections.<Objective>emptyList());
        }
        try {
            if (quest instanceof PlinksToy && "start".equals(state)) {
                return delivery(quest, player, "Plink", "pluszowy miś", 1,
                        "Odszukaj misia w parku na wschód od Plinka i przynieś mu go.");
            }
            if (quest instanceof HungryJoshua) {
                if ("start".equals(state)) { return delivery(quest, player, "Joshua", "kanapka", 5,
                        "Zanieś pięć kanapek do złotnika Joshuy w Ados i porozmawiaj o jedzeniu."); }
                if ("joshua".equals(state)) { return snapshot(quest, State.READY_TO_DELIVER, "Xoderos",
                        "Joshua dostał jedzenie. Wróć do Xoderosa w Semos i przekaż wiadomość o bracie.", Collections.<Objective>emptyList()); }
            }
            if (quest instanceof FindJefsMom) {
                String step = player.getQuest(quest.getSlotName(), 0);
                if ("start".equals(step)) { return snapshot(quest, State.ACTIVE, "Amber",
                        "Znajdź Amber w lesie na południe od Fado. Powiedz jej, że przysyła cię Jef.", Collections.<Objective>emptyList()); }
                if ("found_mom".equals(step)) { return delivery(quest, player, "Jef", "bielikrasa", 1,
                        "Amber jest bezpieczna. Zanieś bielikrasę Jefowi w Kirdneh i przekaż wiadomość."); }
            }
            if (quest instanceof CleanStorageSpace) { return PilotQuestProgress.hunt(quest, player, "Eonna",
                    "Wróć do Eonny. Przywitaj się albo zdaj jej sprawę z postępu, aby zakończyć zadanie."); }
            if (quest instanceof Campfire && "start".equals(state)) { return delivery(quest, player, "Sally", "polano", 10,
                    "Zbierz dziesięć polan dla Sally przy rzece Orril. Potwierdź, że oddajesz je na ognisko."); }
            if (quest instanceof ElvishArmor) {
                List<String> required = ((ElvishArmor) quest).getNeededItems();
                Set<String> delivered = new HashSet<String>(Arrays.asList(state.split(";")));
                for (String item : delivered) { if (!item.isEmpty() && !required.contains(item)) { throw new IllegalArgumentException("Unknown sample"); } }
                List<Objective> goals = new ArrayList<Objective>(); boolean ready = true;
                for (String item : required) {
                    boolean done = delivered.contains(item); int carried = player.getNumberOfEquipped(item);
                    goals.add(new Objective(item, "Oddano: " + item, done ? 1 : 0, 1, carried));
                    ready &= done || carried > 0;
                }
                return snapshot(quest, ready ? State.READY_TO_DELIVER : State.ACTIVE, "Lupos",
                        "Zanieś brakujące próbki Luposowi w leśnej chacie pod Fado. Możesz oddawać je pojedynczo, pytając o rynsztunek.", goals);
            }
        } catch (IllegalArgumentException e) { /* Keep unknown legacy states intact. */ }
        return snapshot(quest, State.UNKNOWN, quest.getNPCName(),
                "Nie można wyświetlić postępu tego zadania. Zgłoś problem administracji, podając nazwę zadania.", Collections.<Objective>emptyList());
    }
    private static QuestProgress delivery(IQuest quest, Player player, String npc, String item, int count, String next) {
        int carried = Math.max(0, player.getNumberOfEquipped(item));
        return snapshot(quest, carried >= count ? State.READY_TO_DELIVER : State.ACTIVE, npc, next,
                Collections.singletonList(new Objective(item, "Masz: " + item, Math.min(carried, count), count, -1)));
    }
    private static QuestProgress snapshot(IQuest quest, State state, String npc, String next, List<Objective> goals) {
        return new QuestProgress(quest.getName(), state, npc, next, goals);
    }
}
