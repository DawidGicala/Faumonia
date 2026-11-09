package games.stendhal.client.gui.dialogue;

import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.common.NpcDialogueSnapshot.Item;
import java.awt.Component;
import java.awt.Container;
import java.util.*;
import javax.swing.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NpcDialogueItemsTest {
    private NpcDialogueSnapshot snapshot(boolean shop) {
        return new NpcDialogueSnapshot("shop-test", 1, "Carmen", "Opis", "npc/welcomernpc", "Rozmowa", "",
                "Moja oferta.", "", "", true, true,
                Collections.singletonList(new NpcDialogueSnapshot.Reply("give", "Oddaj składnik", "")),
                Collections.singletonList(new Item("test", "arandula", "items/herb/arandula", 5, 2,
                        shop ? 7 : -1, 150, shop ? 1000 : 0, "give")), shop);
    }
    private static <T extends Component> List<T> components(Container root, Class<T> type) {
        List<T> result = new ArrayList<T>();
        for (Component child : root.getComponents()) {
            if (type.isInstance(child)) { result.add(type.cast(child)); }
            if (child instanceof Container) { result.addAll(components((Container) child, type)); }
        }
        return result;
    }
    @Test public void extendedSnapshotPreservesQuantitiesPricesAndRejectsMalformedRows() {
        NpcDialogueSnapshot value = snapshot(true), decoded = NpcDialogueSnapshot.fromWire(value.toWire());
        assertNotNull(decoded); assertTrue(decoded.isShop()); assertEquals(value.toWire(), decoded.toWire());
        assertEquals(31, decoded.getItems().get(0).priceFor(3));
        List<String> wire = new ArrayList<String>(value.toWire());
        wire.remove(wire.size() - 1); assertNull(NpcDialogueSnapshot.fromWire(wire));
        wire = new ArrayList<String>(value.toWire()); wire.set(18, "65"); assertNull(NpcDialogueSnapshot.fromWire(wire));
        wire = new ArrayList<String>(value.toWire()); wire.set(wire.size() - 2, "1001"); assertNull(NpcDialogueSnapshot.fromWire(wire));
        wire = new ArrayList<String>(value.toWire()); wire.set(11, "0"); assertNull(NpcDialogueSnapshot.fromWire(wire));
    }
    @Test public void imagesCannotReadArbitraryPathsOrRemoteAddresses() {
        assertNull(DialogueItemIcon.resource("https://example.invalid/x"));
        assertNull(DialogueItemIcon.resource("items/../../config"));
        assertNull(DialogueItemIcon.resource("C:/private"));
    }
    @Test public void expeditionSpritesUseBundledFramesAndKeepPathRestrictions() {
        for(String name:Arrays.asList("lamp","tablet","anchors")) {
            String path="useable/expedition/"+name;assertNotNull(path,DialogueItemIcon.resource(path));
            java.awt.image.BufferedImage image=new java.awt.image.BufferedImage(32,32,java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g=image.createGraphics();new DialogueItemIcon(path).paintIcon(null,g,0,0);g.dispose();
            boolean visible=false;for(int y=0;y<32;y++)for(int x=0;x<32;x++)visible|=(image.getRGB(x,y)>>>24)!=0;
            assertTrue(name,visible);
        }
        assertNull(DialogueItemIcon.resource("useable/expedition/../../private"));
        assertNull(DialogueItemIcon.resource("useable/other/anything"));
    }
    private NpcDialogueSnapshot shop(int owned, int money) {
        NpcDialogueSnapshot old = snapshot(true);
        return new NpcDialogueSnapshot(old.getSession(), 2, old.getNpcName(), old.getDescription(), old.getSprite(),
                old.getTopic(), "", old.getSpeech(), "", "", true, false, old.getReplies(),
                Collections.singletonList(new Item("test", "mały eliksir", "items/drink/potion", 0, owned,
                        50, 100, 1000, "")), true, money);
    }
    @Test public void balanceWireSupportsZeroAndRejectsMissingNegativeOrOverflowingBalances() {
        assertEquals(-1, NpcDialogueSnapshot.fromWire(snapshot(true).toWire()).getMoney());
        for (int balance : new int[] {0, 1000, Integer.MAX_VALUE}) {
            NpcDialogueSnapshot value = shop(54, balance);
            NpcDialogueSnapshot decoded = NpcDialogueSnapshot.fromWire(value.toWire());
            assertNotNull(decoded); assertEquals(balance, decoded.getMoney());
            assertEquals(value.toWire(), decoded.toWire());
        }
        for (String invalid : Arrays.asList("-1", "2147483648", "coins")) {
            List<String> wire = new ArrayList<String>(shop(54, 1000).toWire());
            wire.set(wire.size() - 1, invalid); assertNull(NpcDialogueSnapshot.fromWire(wire));
        }
        List<String> wire = new ArrayList<String>(shop(54, 1000).toWire());
        wire.remove(wire.size() - 1); assertNull(NpcDialogueSnapshot.fromWire(wire));
    }
    @Test public void shopShowsCurrentOwnedStockAndWalletAfterPurchaseAndHidesOutsideShop() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            NpcDialoguePanel panel = new NpcDialoguePanel(new NpcDialoguePanel.Listener() {
                public void replyChosen(String id) { }
                public void textSubmitted(String text) { }
                public void hideRequested() { }
            }, () -> true);
            NpcDialoguePresenter presenter = new NpcDialoguePresenter(panel);
            presenter.display(shop(54, 1000));
            JLabel wallet = null;
            for (JLabel label : components(panel, JLabel.class)) {
                if ("shop-wallet".equals(label.getName())) { wallet = label; }
            }
            assertNotNull(wallet); assertTrue(wallet.isVisible()); assertEquals("Twoje monety: 1000", wallet.getText());
            assertTrue(components(panel, JLabel.class).stream().anyMatch(l -> l.getText() != null && l.getText().contains("Masz: 54 szt.")));
            presenter.display(shop(56, 900));
            assertEquals("Twoje monety: 900", wallet.getText());
            assertTrue(components(panel, JLabel.class).stream().anyMatch(l -> l.getText() != null && l.getText().contains("Masz: 56 szt.")));
            presenter.display(snapshot(false)); assertFalse(wallet.isVisible());
        });
    }
    @Test public void shopSettingQuantityConfirmationAndItemButtonsWorkInSwing() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            List<String> requests = new ArrayList<String>();
            NpcDialoguePanel.Listener listener = new NpcDialoguePanel.Listener() {
                public void replyChosen(String id) { requests.add(id); }
                public void textSubmitted(String text) { }
                public void hideRequested() { }
                public void purchaseRequested(String id, int amount) { requests.add(id + ":" + amount); }
            };
            NpcDialoguePanel panel = new NpcDialoguePanel(listener, () -> true);
            new NpcDialoguePresenter(panel).display(snapshot(true));
            JSpinner amount = components(panel, JSpinner.class).get(0); amount.setValue(3);
            JButton buy = null;
            for (JButton button : components(panel, JButton.class)) { if ("buy-test".equals(button.getName())) { buy = button; } }
            assertNotNull(buy); assertTrue(buy.isEnabled()); buy.doClick(); buy.doClick();
            assertEquals(Collections.singletonList("test:3"), requests);
            NpcDialoguePanel disabled = new NpcDialoguePanel(listener, () -> false);
            new NpcDialoguePresenter(disabled).display(snapshot(true));
            for (JButton button : components(disabled, JButton.class)) {
                if ("buy-test".equals(button.getName())) {
                    boolean visible = true;
                    for (Component node = button; node != disabled; node = node.getParent()) { visible &= node.isVisible(); }
                    assertFalse(visible);
                }
            }
            new NpcDialoguePresenter(panel).display(snapshot(false));
            JButton give = null;
            for (JButton button : components(panel, JButton.class)) { if ("give".equals(button.getActionCommand())) { give = button; } }
            assertNotNull(give); assertNotNull(give.getIcon());
        });
    }

    private void layout(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) { if (child instanceof Container) { layout((Container) child); } }
    }
    @Test public void minimumWindowWithHistoryKeepsAtLeastOneCompleteAnswerVisible() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            NpcDialoguePanel panel = new NpcDialoguePanel(new NpcDialoguePanel.Listener() {
                public void replyChosen(String id) { }
                public void textSubmitted(String text) { }
                public void hideRequested() { }
            });
            new NpcDialoguePresenter(panel).display(snapshot(false));
            panel.setSize(540, 560); panel.setHistoryVisible(true); layout(panel);
            for (JScrollPane scroll : components(panel, JScrollPane.class)) {
                if ("dialogue-replies-scroll".equals(scroll.getName())) { assertTrue(scroll.getViewport().getHeight() >= 46); }
            }
        });
    }
}
