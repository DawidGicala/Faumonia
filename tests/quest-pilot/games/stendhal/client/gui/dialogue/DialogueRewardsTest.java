package games.stendhal.client.gui.dialogue;

import static org.junit.Assert.*;
import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.common.NpcDialogueSnapshot.Reward;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.Test;

public class DialogueRewardsTest {
    private static final String[] ICONS = {"greeting", "farewell", "conversation", "back", "confirm", "decline",
            "quest", "hint", "handover", "complete", "xp", "attack_xp", "defense_xp", "karma", "health", "unlock"};
    private static final String[] LABELS = {"Powitanie", "Pożegnanie", "Rozmowa", "Powrót", "Potwierdzenie", "Rezygnacja",
            "Zadanie", "Wskazówka", "Przekazanie", "Ukończenie", "XP postaci", "XP ataku", "XP obrony", "Karma", "Zdrowie / leczenie", "Dostęp"};
    private NpcDialogueSnapshot snapshot(int revision, String feedback, List<Reward> rewards) {
        return new NpcDialogueSnapshot("receipt", revision, "Carmen", "Uzdrowicielka.", "npc/welcomernpc", "", "",
                "Dziękuję za pomoc.", feedback, "", true, revision == 1,
                Arrays.asList(new NpcDialogueSnapshot.Reply("back", "Porozmawiajmy.", "")),
                Collections.<NpcDialogueSnapshot.Item>emptyList(), false, -1, rewards);
    }
    private List<Reward> rewards() {
        return Arrays.asList(new Reward("mały eliksir", "items/drink/minor_potion", 50, true),
                new Reward("XP postaci", "gui/dialogue/xp", 500, false), new Reward("Karma", "gui/dialogue/karma", 10, false));
    }
    @Test public void receiptsRoundTripAndOldVersionsStillDecode() {
        NpcDialogueSnapshot receipt = snapshot(1, "", rewards());
        assertEquals("4", receipt.toWire().get(0));
        assertEquals(receipt.toWire(), NpcDialogueSnapshot.fromWire(receipt.toWire()).toWire());
        assertEquals(3, NpcDialoguePresenter.toPage(receipt).rewards.size());
        assertTrue(NpcDialogueSnapshot.fromWire(receipt.toWire()).getRewards().get(0).isOnGround());
        NpcDialogueSnapshot old = snapshot(2, "", Collections.<Reward>emptyList());
        assertEquals("1", old.toWire().get(0)); assertTrue(NpcDialogueSnapshot.fromWire(old.toWire()).getRewards().isEmpty());
    }
    @Test public void malformedReceiptCannotCrashTheConversation() {
        List<String> data = snapshot(1, "", rewards()).toWire();
        for (String amount : Arrays.asList("NaN", "Infinity", "-1", "0", "not a number")) {
            List<String> broken = new ArrayList<String>(data); broken.set(broken.size() - 2, amount);
            assertNull(amount, NpcDialogueSnapshot.fromWire(broken));
        }
        data.remove(data.size() - 1); assertNull(NpcDialogueSnapshot.fromWire(data));
    }
    private static <T extends Component> List<T> find(Container root, Class<T> type) {
        List<T> result = new ArrayList<T>();
        for (Component child : root.getComponents()) {
            if (type.isInstance(child)) { result.add(type.cast(child)); }
            if (child instanceof Container) { result.addAll(find((Container) child, type)); }
        }
        return result;
    }
    @Test public void rewardsHaveIconsSurviveInHistoryAndFeedbackDoesNotDuplicateReceipt() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            NpcDialoguePanel panel = new NpcDialoguePanel(new NpcDialoguePanel.Listener() {
                public void replyChosen(String id) { }
                public void textSubmitted(String text) { }
                public void hideRequested() { }
            });
            NpcDialoguePresenter presenter = new NpcDialoguePresenter(panel);
            presenter.display(snapshot(1, "", rewards()));
            int count = 0;
            for (JLabel label : find(panel, JLabel.class)) {
                if (label.getName() != null && label.getName().startsWith("reward-")) { count++; assertNotNull(label.getIcon()); }
            }
            assertEquals(3, count);
            presenter.display(snapshot(2, "Rozmowa się zmieniła.", rewards()));
            presenter.display(snapshot(3, "", Collections.<Reward>emptyList()));
            try {
                String history = "";
                for (JEditorPane pane : find(panel, JEditorPane.class)) {
                    String text = pane.getDocument().getText(0, pane.getDocument().getLength());
                    if (text.contains("Carmen:")) { history = text; }
                }
                assertEquals(1, history.split("Otrzymujesz", -1).length - 1);
                assertTrue(history.contains("mały eliksir +50 (na ziemi)"));
            } catch (javax.swing.text.BadLocationException e) { throw new AssertionError(e); }
        });
    }
    @Test public void allDedicatedIconsAreBundledTransparentAndRenderAtGameSize() throws Exception {
        BufferedImage sheet = new BufferedImage(720, 380, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new Color(38, 43, 48)); g.fillRect(0, 0, 720, 380);
        g.setColor(new Color(212, 215, 218)); g.setFont(new Font("Dialog", Font.BOLD, 16));
        g.drawString("Faumonia - ikony rozmów i nagród", 20, 28);
        g.setFont(new Font("Dialog", Font.PLAIN, 12));
        for (int i = 0; i < ICONS.length; i++) {
            String sprite = "gui/dialogue/" + ICONS[i];
            assertNotNull(sprite, DialogueItemIcon.resource(sprite));
            BufferedImage asset = ImageIO.read(DialogueItemIcon.resource(sprite));
            assertTrue(asset.getColorModel().hasAlpha()); assertEquals(0, asset.getRGB(0, 0) >>> 24);
            int x = 22 + i % 4 * 176, y = 54 + i / 4 * 78;
            new DialogueItemIcon(sprite, 32).paintIcon(null, g, x, y);
            g.drawString(LABELS[i], x, y + 51);
        }
        g.dispose();
        File directory = new File(System.getProperty("quest.pilot.output")); directory.mkdirs();
        ImageIO.write(sheet, "png", new File(directory, "dialogue-icon-catalog.png"));
    }
}
