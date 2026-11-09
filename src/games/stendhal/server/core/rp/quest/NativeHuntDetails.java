package games.stendhal.server.core.rp.quest;

import games.stendhal.server.entity.player.Player;
import marauroa.common.Pair;
import java.util.Map;

/** Read-only access to the same targets used by the native quest actions. */
public interface NativeHuntDetails {
    Map<String, Pair<Integer, Integer>> getDialogueHuntTargets(Player player);
    int getDialogueHuntTotal(Player player);
    int getDialogueKillIndex();
    String getDialogueHuntHint(Player player);
}
