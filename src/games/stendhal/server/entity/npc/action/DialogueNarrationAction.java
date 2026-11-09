package games.stendhal.server.entity.npc.action;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.dialogue.NpcDialogueService;
import games.stendhal.server.entity.player.Player;

/** Optional source-authored stage direction. Does not write quest or world state. */
public final class DialogueNarrationAction implements ChatAction {
    private final String text;
    public DialogueNarrationAction(String text) {
        if (text == null || text.length() > 2000) { throw new IllegalArgumentException("Invalid narration"); }
        this.text = text;
    }
    @Override public void fire(Player player, Sentence sentence, EventRaiser raiser) {
        if (raiser.getEntity() instanceof SpeakerNPC) {
            NpcDialogueService.narrate((SpeakerNPC) raiser.getEntity(), player, text);
        }
    }
    @Override public boolean equals(Object other) {
        return other instanceof DialogueNarrationAction && text.equals(((DialogueNarrationAction) other).text);
    }
    @Override public int hashCode() { return text.hashCode(); }
}
