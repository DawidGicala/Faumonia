package games.stendhal.server.core.rp.quest;

import games.stendhal.common.QuestProgress;
import games.stendhal.common.QuestProgress.Objective;
import games.stendhal.common.QuestProgress.State;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.HerbsForCarmen;
import games.stendhal.server.maps.quests.IQuest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Read-only adapters over existing saves. Quest actions remain authoritative. */
public final class PilotQuestProgress {
    private PilotQuestProgress() { }

    /** Only registered, started, visible quests may expose a snapshot to their owner. */
    public static QuestProgress read(IQuest quest, Player player) {
        if (quest == null || !quest.isVisibleOnQuestStatus() || !quest.isStarted(player)) {
            return null;
        }
        if (ExpeditionQuestProgress.supports(quest)) { return ExpeditionQuestProgress.read(quest,player); }
        if (CollectionQuestProgress.supports(quest)) { return CollectionQuestProgress.read(quest,player); }
        if (HuntQuestProgress.supports(quest)) { return HuntQuestProgress.read(quest,player); }
        if (JourneyQuestProgress.supports(quest)) { return JourneyQuestProgress.read(quest, player); }
        if (ResidentQuestProgress.supports(quest)) { return ResidentQuestProgress.read(quest, player); }
        if (AdditionalQuestProgress.supports(quest)) { return AdditionalQuestProgress.read(quest, player); }
        String id = quest.getName();
        if (!"HerbsForCarmen".equals(id) && !"HelpSedro".equals(id) && !"BowsForOuchit".equals(id)) {
            return null;
        }
        String state = player.getQuest(quest.getSlotName());
        if (quest.isCompleted(player)) {
            return snapshot(quest, State.COMPLETED, "", "Zadanie ukończone.", empty());
        }
        if ("rejected".equals(state)) {
            return snapshot(quest, State.DECLINED, quest.getNPCName(),
                    "Jeśli zmienisz zdanie, porozmawiaj ponownie o zadaniu.", empty());
        }
        try {
            if ("HerbsForCarmen".equals(id)) {
                return carmen(quest, player, state);
            } else if ("HelpSedro".equals(id)) {
                return hunt(quest, player, "Sedro", "Wróć do Sedro i porozmawiaj o zadaniu, aby je zakończyć.");
            } else {
                return ouchit(quest, player, state);
            }
        } catch (IllegalArgumentException e) {
            // Do not invent zero or completed progress from an unrecognised legacy save.
            return snapshot(quest, State.UNKNOWN, quest.getNPCName(),
                    "Nie można wyświetlić postępu tego zadania. Zgłoś problem administracji, podając nazwę zadania.", empty());
        }
    }

    private static QuestProgress carmen(IQuest quest, Player player, String state) {
        Map<String, Integer> required = HerbsForCarmen.getRequiredItems();
        Map<String, Integer> remaining = new HashMap<String, Integer>();
        if (state == null || state.isEmpty()) {
            throw new IllegalArgumentException("Missing collection state");
        }
        for (String token : state.split(";", -1)) {
            String[] pair = token.split("=", -1);
            if (pair.length != 2 || !required.containsKey(pair[0]) || remaining.containsKey(pair[0])) {
                throw new IllegalArgumentException("Unknown collection entry");
            }
            int amount = Integer.parseInt(pair[1]);
            if (amount <= 0 || amount > required.get(pair[0])) {
                throw new IllegalArgumentException("Invalid collection count");
            }
            remaining.put(pair[0], amount);
        }
        List<Objective> objectives = new ArrayList<Objective>();
        boolean ready = true;
        boolean partial = false;
        for (Map.Entry<String, Integer> item : required.entrySet()) {
            int left = remaining.containsKey(item.getKey()) ? remaining.get(item.getKey()) : 0;
            int carried = Math.max(0, player.getNumberOfEquipped(item.getKey()));
            ready &= carried >= left;
            partial |= left > 0 && carried > 0;
            objectives.add(new Objective(item.getKey(), "Oddano: " + item.getKey(),
                    item.getValue() - left, item.getValue(), carried));
        }
        String next = ready ? "Masz przy sobie wszystkie brakujące składniki. Wróć do Carmen."
                : partial ? "Możesz już oddać część składników Carmen, a resztę donieść później."
                : "Zbierz brakujące składniki. Carmen przyjmuje je także partiami.";
        next += " Powiedz jej „zapasy”, a następnie nazwę oddawanego składnika.";
        return snapshot(quest, ready ? State.READY_TO_DELIVER : State.ACTIVE, "Carmen", next, objectives);
    }

    static QuestProgress hunt(IQuest quest, Player player, String npc, String readyText) {
        if (!"start".equals(player.getQuest(quest.getSlotName(), 0))) {
            throw new IllegalArgumentException("Unknown hunt state");
        }
        String record = player.getQuest(quest.getSlotName(), 1);
        if (record == null) {
            throw new IllegalArgumentException("Missing hunt record");
        }
        String[] tokens = record.split(",", -1);
        if (tokens.length == 0 || tokens.length % 5 != 0 || tokens.length > 80) {
            throw new IllegalArgumentException("Invalid hunt record");
        }
        List<Objective> objectives = new ArrayList<Objective>();
        Set<String> names = new HashSet<String>();
        for (int i = 0; i < tokens.length; i += 5) {
            String name = tokens[i];
            if (name.isEmpty() || !names.add(name)) {
                throw new IllegalArgumentException("Invalid hunt name");
            }
            int solo = nonNegative(tokens[i + 1]);
            int additional = nonNegative(tokens[i + 2]);
            int initialSolo = nonNegative(tokens[i + 3]);
            int initialShared = nonNegative(tokens[i + 4]);
            long total = (long) solo + additional;
            if (total <= 0 || total > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Invalid hunt requirement");
            }
            long doneSolo = (long) player.getSoloKill(name) - initialSolo;
            long doneShared = (long) player.getSharedKill(name) - initialShared;
            if (solo > 0) {
                objectives.add(new Objective(name + ":solo", "Pokonano solo: " + name,
                        clamp(doneSolo, solo), solo, -1));
            }
            if (additional > 0) {
                objectives.add(new Objective(name + ":total", "Pokonano łącznie: " + name,
                        clamp(doneSolo + doneShared, (int) total), (int) total, -1));
            }
        }
        // The same condition that guards the reward, including solo/shared semantics.
        boolean ready = new KilledForQuestCondition(quest.getSlotName(), 1).fire(player, null, null);
        String next = ready ? readyText
                : "Pokonaj brakujące potwory. Liczą się zabicia od przyjęcia zadania; cele „łącznie” możesz wykonać samodzielnie lub z pomocą innych.";
        return snapshot(quest, ready ? State.READY_TO_DELIVER : State.ACTIVE, npc, next, objectives);
    }

    private static QuestProgress ouchit(IQuest quest, Player player, String state) {
        List<Objective> objectives = new ArrayList<Objective>();
        if ("polano".equals(state)) {
            int count = Math.max(0, player.getNumberOfEquipped("polano"));
            objectives.add(new Objective("wood", "Zebrano: polano", Math.min(count, 10), 10, -1));
            boolean ready = player.isEquipped("polano", 10);
            return snapshot(quest, ready ? State.READY_TO_DELIVER : State.ACTIVE, "Ouchit",
                    ready ? "Masz drewno przy sobie. Wróć do Ouchita i powiedz „polano”, żeby mu je oddać."
                          : "Zbierz 10 polan. Gdy wrócisz do Ouchita, powiedz „polano”.", objectives);
        }
        if ("hair".equals(state)) {
            boolean ready = player.isEquipped("koński włos");
            objectives.add(new Objective("hair", "Zebrano: koński włos", ready ? 1 : 0, 1, -1));
            return snapshot(quest, ready ? State.READY_TO_DELIVER : State.ACTIVE,
                    ready ? "Ouchit" : "Karl",
                    ready ? "Zanieś włos Ouchitowi i powiedz „włosie”."
                          : "Ouchit przyjął drewno. Odwiedź Karla na farmie na wschód od Semos i powiedz „Ouchit”.", objectives);
        }
        throw new IllegalArgumentException("Unknown bow state");
    }

    private static int nonNegative(String value) {
        int result = Integer.parseInt(value);
        if (result < 0) { throw new IllegalArgumentException("Negative hunt count"); }
        return result;
    }

    private static int clamp(long value, int maximum) {
        return (int) Math.max(0, Math.min(value, maximum));
    }

    private static List<Objective> empty() { return Collections.emptyList(); }

    private static QuestProgress snapshot(IQuest quest, State state, String npc, String next,
            List<Objective> objectives) {
        return new QuestProgress(quest.getName(), state, npc, next, objectives);
    }
}
