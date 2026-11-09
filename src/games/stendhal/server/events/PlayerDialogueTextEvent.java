package games.stendhal.server.events;

import games.stendhal.common.constants.Events;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;

/** Public chat echo without reusing the player's legacy text attribute or re-running NPC input. */
public final class PlayerDialogueTextEvent extends RPEvent {
    public static void generateRPClass() {
        RPClass rpclass = new RPClass(Events.PLAYER_DIALOGUE_TEXT);
        rpclass.addAttribute("text", Type.LONG_STRING);
    }
    public PlayerDialogueTextEvent(String text) {
        super(Events.PLAYER_DIALOGUE_TEXT);
        put("text", text);
    }
}
