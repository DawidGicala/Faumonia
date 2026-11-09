package games.stendhal.client.gui.dialogue;

import games.stendhal.common.NpcDialogueSnapshot;
import java.lang.reflect.Field;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.*;

public class NpcDialogueGoodbyeTest {
    @Test public void normalChatButtonAndTypedGoodbyeCloseAllFourPilotNpcs() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (String name : new String[] {"Carmen", "Sedro", "Ouchit", "Karl"}) {
                try (IntegratedDialogueDemo demo = new IntegratedDialogueDemo()) {
                    for (int mode = 0; mode < 4; mode++) {
                        demo.start(name);
                        assertTrue(name, (Boolean) field(demo, "visible"));
                        if (mode == 0) {
                            // Same NPC entry point as speech from the normal chat.
                            demo.say("bye");
                        } else if (mode == 1) {
                            demo.choose("goodbye");
                        } else if (mode == 2) {
                            ((NpcDialogueController) field(demo, "controller")).textSubmitted("bye");
                        } else {
                            ((NpcDialogueController) field(demo, "controller")).hideRequested();
                        }
                        NpcDialogueSnapshot ended = (NpcDialogueSnapshot) field(demo, "latest");
                        assertFalse(name + " mode " + mode, ended.isActive());
                        assertFalse(name + " mode " + mode, (Boolean) field(demo, "visible"));
                    }
                }
            }
        });
    }

    @Test public void naturalPolishFarewellsCloseNormalChatAndReplyInput() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (String name : new String[] {"Carmen", "Sedro", "Ouchit", "Karl"}) {
                try (IntegratedDialogueDemo demo = new IntegratedDialogueDemo()) {
                    for (String phrase : new String[] {"do widzenia", "Do widzenia.",
                            "do zobaczenia", "Do zobaczenia!", "dowidzenia", "bye"}) {
                        for (boolean fromPanel : new boolean[] {false, true}) {
                            demo.start(name);
                            assertTrue(name, (Boolean) field(demo, "visible"));
                            if (fromPanel) {
                                ((NpcDialogueController) field(demo, "controller")).textSubmitted(phrase);
                            } else {
                                demo.say(phrase);
                            }
                            String context = name + " / " + phrase + " / panel=" + fromPanel;
                            assertFalse(context, ((NpcDialogueSnapshot) field(demo, "latest")).isActive());
                            assertFalse(context, (Boolean) field(demo, "visible"));
                        }
                    }
                }
            }
        });
    }

    @Test public void polishFarewellCancelsPendingPurchaseWithoutTakingGold() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (String phrase : new String[] {"do widzenia", "do zobaczenia"}) {
                for (boolean fromPanel : new boolean[] {false, true}) {
                    try (IntegratedDialogueDemo demo = new IntegratedDialogueDemo()) {
                        games.stendhal.server.entity.player.Player player =
                                (games.stendhal.server.entity.player.Player) field(demo, "player");
                        games.stendhal.server.entity.item.StackableItem money =
                                (games.stendhal.server.entity.item.StackableItem)
                                games.stendhal.server.core.engine.SingletonRepository.getEntityManager().getItem("money");
                        money.setQuantity(1000);
                        assertTrue(player.equipToInventoryOnly(money));
                        demo.start("Carmen"); demo.say("kupię 2 antidotum");
                        NpcDialogueSnapshot offer = (NpcDialogueSnapshot) field(demo, "latest");
                        assertTrue(offer.getReplies().stream().anyMatch(reply -> "confirm".equals(reply.getId())));
                        if (fromPanel) {
                            ((NpcDialogueController) field(demo, "controller")).textSubmitted(phrase);
                        } else {
                            demo.say(phrase);
                        }
                        assertFalse(((NpcDialogueSnapshot) field(demo, "latest")).isActive());
                        assertFalse((Boolean) field(demo, "visible"));
                        demo.start("Carmen"); demo.say("tak");
                        assertEquals(1000, player.getNumberOfEquipped("money"));
                        assertEquals(0, player.getNumberOfEquipped("antidotum"));
                    }
                }
            }
        });
    }

    private static Object field(IntegratedDialogueDemo demo, String name) {
        try {
            Field field = IntegratedDialogueDemo.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(demo);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
