package games.stendhal.server.maps.quests;

import static org.junit.Assert.*;
import games.stendhal.common.Constants;
import games.stendhal.server.core.engine.RPClassGenerator;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import java.util.Collections;
import marauroa.common.game.RPObject;
import org.junit.Test;

/** Separate regression for the existing possession-versus-delivery history bug. */
public class BowsForOuchitHistoryTest {
    @Test public void historyDoesNotMistakePossessionForDelivery() {
        new RPClassGenerator().createRPClasses();
        Player player = new Player(new RPObject());
        player.addSlot("!quests"); player.getSlot("!quests").add(new RPObject());
        for (String slot : Constants.CARRYING_SLOTS) { player.addSlot(slot); }
        StackableItem wood = new StackableItem("polano", "test", "test", Collections.<String, String>emptyMap());
        wood.setQuantity(10); player.getSlot("bag").add(wood);
        BowsForOuchit quest = new BowsForOuchit();
        player.setKeyedSlot("!quests", "bows_ouchit", "polano");
        String history = quest.getHistory(player).toString();
        assertTrue(history.contains("Muszę je jeszcze oddać"));
        assertFalse(history.contains("Ouchit przyjął drewno"));
        player.setKeyedSlot("!quests", "bows_ouchit", "hair");
        history = quest.getHistory(player).toString();
        assertTrue(history.contains("Ouchit przyjął drewno"));
        assertFalse(history.contains("Muszę zebrać 10 polan"));
    }
}
