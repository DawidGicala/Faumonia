package games.stendhal.server.entity.npc.action;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.dialogue.NpcDialogueService;
import games.stendhal.server.entity.player.Player;

/** Enables a follow-up question for this encounter only; no saved hint or quest flags. */
public final class DialogueHintContextAction implements ChatAction {
    private final String context;
    public DialogueHintContextAction(String context) {
        if (context == null || !context.matches("[a-z_]{0,64}")) {
            throw new IllegalArgumentException("Invalid hint context");
        }
        this.context = context;
    }
    @Override public void fire(Player player, Sentence sentence, EventRaiser raiser) {
        if (raiser.getEntity() instanceof SpeakerNPC) {
            NpcDialogueService.setHintContext((SpeakerNPC) raiser.getEntity(), player, context);
        }
    }
    @Override public boolean equals(Object other) {
        return other instanceof DialogueHintContextAction && context.equals(((DialogueHintContextAction) other).context);
    }
    @Override public int hashCode() { return context.hashCode(); }
}
