package games.stendhal.server.actions;

import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.server.entity.npc.dialogue.NpcDialogueService;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;

/** A reply is an offered opaque id, never a client-chosen quest command. */
public final class NpcDialogueAction implements ActionListener {
    public static void register() {
        CommandCenter.register(NpcDialogueSnapshot.ACTION, new NpcDialogueAction());
    }
    @Override public void onAction(Player player, RPAction action) {
        NpcDialogueService.handle(player, action);
    }
}
