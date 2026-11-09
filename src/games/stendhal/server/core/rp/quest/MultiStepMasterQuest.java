package games.stendhal.server.core.rp.quest;
import games.stendhal.server.entity.player.Player;
/** Read-only presentation for a Master trial with several distinct phases. */
public interface MultiStepMasterQuest {
    String masterSummary(Player player);
    String masterItem(Player player);
    int masterItemAmount(Player player);
}
