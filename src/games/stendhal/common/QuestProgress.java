package games.stendhal.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable, player-visible quest snapshot. Never contains raw quest slots. */
public final class QuestProgress {
    public enum State { ACTIVE, READY_TO_DELIVER, COMPLETED, DECLINED, UNKNOWN }

    public static final class Objective {
        private final String id;
        private final String label;
        private final int current;
        private final int required;
        private final int carried;

        /** carried is -1 when an inventory counter is not applicable. */
        public Objective(String id, String label, int current, int required, int carried) {
            if (id == null || label == null || required <= 0 || current < 0
                    || current > required || carried < -1) {
                throw new IllegalArgumentException("Invalid quest objective");
            }
            this.id = id;
            this.label = label;
            this.current = current;
            this.required = required;
            this.carried = carried;
        }

        public String getId() { return id; }
        public String getLabel() { return label; }
        public int getCurrent() { return current; }
        public int getRequired() { return required; }
        public int getCarried() { return carried; }
    }

    private final String id;
    private final State state;
    private final String nextNPC;
    private final String nextStep;
    private final List<Objective> objectives;

    public QuestProgress(String id, State state, String nextNPC, String nextStep,
            List<Objective> objectives) {
        if (id == null || state == null || nextNPC == null || nextStep == null
                || objectives == null || objectives.size() > 32 || objectives.contains(null)) {
            throw new IllegalArgumentException("Invalid quest progress");
        }
        this.id = id;
        this.state = state;
        this.nextNPC = nextNPC;
        this.nextStep = nextStep;
        this.objectives = Collections.unmodifiableList(new ArrayList<Objective>(objectives));
    }

    public String getId() { return id; }
    public State getState() { return state; }
    public String getNextNPC() { return nextNPC; }
    public String getNextStep() { return nextStep; }
    public List<Objective> getObjectives() { return objectives; }

    /** Versioned list transported by Marauroa's existing list encoding. */
    public List<String> toWire() {
        List<String> data = new ArrayList<String>();
        Collections.addAll(data, "1", id, state.name(), nextNPC, nextStep,
                Integer.toString(objectives.size()));
        for (Objective objective : objectives) {
            Collections.addAll(data, objective.id, objective.label,
                    Integer.toString(objective.current), Integer.toString(objective.required),
                    Integer.toString(objective.carried));
        }
        return data;
    }

    /** Unknown versions or malformed optional data fall back to the legacy journal. */
    public static QuestProgress fromWire(List<String> data) {
        if (data == null || data.size() < 6 || data.size() > 166 || !"1".equals(data.get(0))) {
            return null;
        }
        try {
            int count = Integer.parseInt(data.get(5));
            if (count < 0 || count > 32 || data.size() != 6 + count * 5) {
                return null;
            }
            List<Objective> objectives = new ArrayList<Objective>();
            for (int i = 6; i < data.size(); i += 5) {
                objectives.add(new Objective(data.get(i), data.get(i + 1),
                        Integer.parseInt(data.get(i + 2)), Integer.parseInt(data.get(i + 3)),
                        Integer.parseInt(data.get(i + 4))));
            }
            return new QuestProgress(data.get(1), State.valueOf(data.get(2)),
                    data.get(3), data.get(4), objectives);
        } catch (IllegalArgumentException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }
}
