package games.stendhal.client.gui.dialogue;

import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.server.core.engine.RPClassGenerator;
import java.util.Collections;
import marauroa.common.game.RPAction;
import org.junit.Test;
import static org.junit.Assert.*;

public class NpcDialogueTransportTest {
    @Test public void buttonAndPolishTextSurviveRealClientSendWithServerEventClassesLoaded() {
        new RPClassGenerator().createRPClasses();
        try (DialogueWireClient client = new DialogueWireClient()) {
            RPAction button = action("reply", "ingredients");
            RPAction received = client.roundTrip(button);
            assertEquals(NpcDialogueSnapshot.ACTION, received.get("type"));
            assertEquals("private-session", received.get("session"));
            assertEquals("23", received.get("revision"));
            assertEquals("ingredients", received.get("reply"));
            assertFalse(received.has("text"));

            String text = String.join("", Collections.nCopies(50, "Zażółć gęś"));
            assertEquals(NpcDialogueSnapshot.MAX_INPUT, text.length());
            received = client.roundTrip(action("text", text));
            assertEquals(NpcDialogueSnapshot.ACTION, received.get("type"));
            assertEquals("private-session", received.get("session"));
            assertEquals("23", received.get("revision"));
            assertEquals(text, received.get("text"));
            assertFalse(received.has("reply"));
            RPAction purchase = action("buy", "shop_2"); purchase.put("amount", "42");
            received = client.roundTrip(purchase);
            assertEquals("shop_2", received.get("buy")); assertEquals("42", received.get("amount"));
            received = client.roundTrip(action("close", "1"));
            assertEquals("1", received.get("close")); assertFalse(received.has("text"));
        }
    }

    private static RPAction action(String key, String value) {
        RPAction action = new RPAction();
        action.put("type", NpcDialogueSnapshot.ACTION);
        action.put("session", "private-session");
        action.put("revision", "23");
        action.put(key, value);
        return action;
    }
}
