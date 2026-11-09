package games.stendhal.server.events;

import games.stendhal.common.NpcDialogueSnapshot;
import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;

/** Attached only to the attending player's PRIVATE event slot. */
public final class NpcDialogueEvent extends RPEvent {
    public static void generateRPClass() {
        RPClass rpclass = new RPClass(NpcDialogueSnapshot.EVENT);
        rpclass.add(DefinitionClass.ATTRIBUTE, "data", Type.VERY_LONG_STRING);
    }
    public NpcDialogueEvent(NpcDialogueSnapshot snapshot) {
        super(NpcDialogueSnapshot.EVENT);
        put("data", snapshot.toWire());
    }
}
