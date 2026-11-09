/* SPDX-License-Identifier: GPL-2.0-or-later */
package games.stendhal.client.gui.dialogue;

import static org.junit.Assert.*;
import games.stendhal.client.gui.dialogue.DialoguePage.Part;
import games.stendhal.client.gui.dialogue.DialoguePage.Reply;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.junit.Test;

public class NpcDialoguePanelTest {
    private static final class Requests implements NpcDialoguePanel.Listener {
        final List<String> replies = new ArrayList<String>();
        final List<String> texts = new ArrayList<String>();
        int hides;
        public void replyChosen(String id) { replies.add(id); }
        public void textSubmitted(String text) { texts.add(text); }
        public void hideRequested() { hides++; }
    }

    private DialoguePage page(String npc, boolean active) {
        return new DialoguePage(npc, "Opis <img src='x'>", "data/sprites/npc/welcomernpc.png",
                "Przykładowe zadanie", "", Arrays.asList(Part.text("Zapytaj o "), Part.topic("arandulę", "herb")),
                Arrays.asList(new Reply("herb", "Gdzie rośnie arandula?"), new Reply("accept", "Pomogę ci.")), active);
    }

    private static <T extends Component> List<T> components(Container root, Class<T> type) {
        List<T> result = new ArrayList<T>();
        for (Component child : root.getComponents()) {
            if (type.isInstance(child)) { result.add(type.cast(child)); }
            if (child instanceof Container) { result.addAll(components((Container) child, type)); }
        }
        return result;
    }
    private static JEditorPane speech(NpcDialoguePanel panel) {
        for (JEditorPane pane : components(panel, JEditorPane.class)) {
            if (pane.getText().contains("reply:herb")) { return pane; }
        }
        throw new AssertionError("No topic link");
    }
    private static void clickLink(JEditorPane pane, String target) {
        HyperlinkEvent event = new HyperlinkEvent(pane, HyperlinkEvent.EventType.ACTIVATED, null, target);
        for (HyperlinkListener listener : pane.getHyperlinkListeners()) { listener.hyperlinkUpdate(event); }
    }
    private static String allHtml(NpcDialoguePanel panel) {
        StringBuilder html = new StringBuilder();
        for (JEditorPane pane : components(panel, JEditorPane.class)) { html.append(pane.getText()); }
        return html.toString();
    }
    private static String allText(NpcDialoguePanel panel) {
        StringBuilder text = new StringBuilder();
        try {
            for (JEditorPane pane : components(panel, JEditorPane.class)) {
                text.append(pane.getDocument().getText(0, pane.getDocument().getLength()));
            }
        } catch (javax.swing.text.BadLocationException e) { throw new AssertionError(e); }
        return text.toString();
    }

    @Test public void naturalReplyMapsToKeywordAndAllNineTopicsRemainAvailable() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            DialoguePreview preview = new DialoguePreview();
            assertTrue(DialoguePreview.clickReply(preview.panel, "accept"));
            assertEquals("tak", preview.getLastKeyword());
            List<String> ids = new ArrayList<String>();
            for (JButton button : components(preview.panel, JButton.class)) {
                if (button.getText().startsWith("<html>")) { ids.add(button.getActionCommand()); }
            }
            assertEquals(Arrays.asList("supplies", "arandula", "mushrooms", "apple", "wood", "healing", "trade", "job", "goodbye"), ids);
            assertTrue(allText(preview.panel).contains("Przyniosę ci składniki."));
            assertTrue(DialoguePreview.clickReply(preview.panel, "arandula"));
            assertEquals("arandula", preview.getLastKeyword());
            assertTrue(DialoguePreview.clickReply(preview.panel, "hint_detail"));
            assertNull(preview.getLastKeyword()); // New hint is not falsely advertised as an existing keyword.
        });
    }

    @Test public void topicLinksRejectUnknownAndRepeatedActionsUntilReplyArrives() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Requests requests = new Requests(); NpcDialoguePanel panel = new NpcDialoguePanel(requests);
            panel.display(page("Carmen", true)); JEditorPane words = speech(panel);
            clickLink(words, "reply:missing"); clickLink(words, "https://example.invalid/");
            assertTrue(requests.replies.isEmpty());
            clickLink(words, "reply:herb"); clickLink(words, "reply:herb");
            DialoguePreview.clickReply(panel, "accept");
            assertEquals(Arrays.asList("herb"), requests.replies);
            panel.display(page("Carmen", true)); DialoguePreview.clickReply(panel, "accept");
            assertEquals(Arrays.asList("herb", "accept"), requests.replies);
            assertFalse(allHtml(panel).contains("<img"));
        });
    }

    @Test public void closeAndEscapeOnlyHideAndDoNotEndOrAnswer() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Requests requests = new Requests(); NpcDialoguePanel panel = new NpcDialoguePanel(requests);
            panel.display(page("Carmen", true));
            assertTrue(DialoguePreview.clickReply(panel, "×"));
            Object binding = panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(KeyStroke.getKeyStroke("ESCAPE"));
            panel.getActionMap().get(binding).actionPerformed(new ActionEvent(panel, 0, ""));
            assertEquals(2, requests.hides); assertTrue(requests.replies.isEmpty()); assertTrue(requests.texts.isEmpty());
            DialoguePreview.clickReply(panel, "accept"); assertEquals(Arrays.asList("accept"), requests.replies);
            DialoguePreview preview = new DialoguePreview();
            DialoguePreview.clickReply(preview.panel, "×"); assertTrue(preview.isActive());
            assertNull(preview.getLastKeyword());
            preview.depart(); assertFalse(preview.isActive());
        });
    }

    @Test public void manualTextIsTrimmedAndCannotRepeatWhileWaitingOrAfterDeparture() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Requests requests = new Requests(); NpcDialoguePanel panel = new NpcDialoguePanel(requests);
            panel.display(page("Carmen", true));
            JTextField input = components(panel, JTextField.class).get(0);
            input.setText("   "); input.postActionEvent(); assertTrue(requests.texts.isEmpty());
            input.setText("  dzień dobry  "); input.postActionEvent(); input.postActionEvent();
            assertEquals(Arrays.asList("dzień dobry"), requests.texts); assertFalse(input.isEnabled());
            panel.display(page("Carmen", false)); input.setText("tak"); input.postActionEvent();
            DialoguePreview.clickReply(panel, "accept");
            assertEquals(1, requests.texts.size()); assertTrue(requests.replies.isEmpty());
            assertFalse(allHtml(panel).contains("reply:herb"));
        });
    }

    @Test public void historySurvivesCollapsingButResetsForNewEncounter() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Requests requests = new Requests(); NpcDialoguePanel panel = new NpcDialoguePanel(requests);
            panel.display(page("Carmen", true)); DialoguePreview.clickReply(panel, "accept");
            panel.setHistoryVisible(true); panel.setHistoryVisible(false); panel.setHistoryVisible(true);
            assertTrue(allText(panel).contains("Pomogę ci."));
            panel.resetHistory(); assertFalse(allText(panel).contains("Pomogę ci."));
            panel.display(page("Carmen", true)); DialoguePreview.clickReply(panel, "accept");
            panel.display(page("Sedro", true)); assertFalse(allText(panel).contains("Pomogę ci."));
            assertFalse(allText(panel).contains("Carmen:"));
        });
    }

    @Test(expected = IllegalArgumentException.class) public void undisplayedTopicCannotBeMadeClickable() {
        new DialoguePage("Carmen", "", "", "", "", Arrays.asList(Part.topic("tajny temat", "hidden")),
                Arrays.asList(new Reply("shown", "Jawny temat")), true);
    }

    @Test public void latestPlayerReplyStaysVisibleWithoutOpeningHistoryAndResetsBetweenEncounters() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            NpcDialoguePanel panel = new NpcDialoguePanel(new Requests());
            panel.display(page("Carmen", true));
            JLabel last = null;
            for (JLabel label : components(panel, JLabel.class)) {
                if ("last-player-reply".equals(label.getName())) { last = label; }
            }
            assertNotNull(last); assertFalse(last.isVisible());
            DialoguePreview.clickReply(panel, "accept");
            assertTrue(last.isVisible()); assertEquals("Ty: Pomogę ci.", last.getText());
            panel.recordPlayerReply("Pomogę ci."); panel.display(page("Carmen", true));
            assertEquals("Ty: Pomogę ci.", last.getText());
            JTextField input = components(panel, JTextField.class).get(0);
            input.setText("gdzie rośnie arandula?"); input.postActionEvent();
            assertEquals("Ty: gdzie rośnie arandula?", last.getText());
            panel.recordPlayerReply("wiadomość ze zwykłego czatu");
            assertEquals("Ty: wiadomość ze zwykłego czatu", last.getText());
            panel.setHistoryVisible(true); panel.setHistoryVisible(false); assertTrue(last.isVisible());
            panel.resetHistory(); assertFalse(last.isVisible()); assertEquals("", last.getText());
            panel.recordPlayerReply("poprzednia odpowiedź"); panel.display(page("Sedro", true));
            assertFalse(last.isVisible());
        });
    }

    @Test public void rendersActualSwingDialoguePages() throws Exception { DialoguePreview.renderExamples(); }

    private static void layoutAll(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container) { layoutAll((Container) child); }
        }
    }

    @Test public void smallWindowWithHistoryAndIngredientsKeepsACompleteReplyVisible() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            NpcDialoguePanel panel = new NpcDialoguePanel(new Requests());
            List<Reply> choices = new ArrayList<Reply>();
            List<games.stendhal.common.NpcDialogueSnapshot.Item> ingredients = new ArrayList<>();
            for (int i = 0; i < 7; i++) { choices.add(new Reply("choice" + i, "Chcę oddać składniki.")); }
            for (int i = 0; i < 5; i++) {
                ingredients.add(new games.stendhal.common.NpcDialogueSnapshot.Item("item" + i, "arandula",
                        "items/herb/arandula", 5, 5, -1, 100, 0, ""));
            }
            panel.setSize(540, 560);
            panel.display(new DialoguePage("Carmen", "Uzdrowicielka z Semos.", "data/sprites/npc/welcomernpc.png", "", "",
                    Arrays.asList(Part.text("Sprawdźmy, czego brakuje w moich zapasach: arandula - 5, borowik - 1, jabłko - 3, "
                    + "pieczarka - 1, polano - 2. Nie musisz mieć wszystkiego naraz, przyjmę to, co udało ci się zebrać. Masz coś ze sobą?")),
                    choices, true, ingredients, false));
            panel.recordPlayerReply("Przynoszę składniki."); panel.setHistoryVisible(true);
            panel.dispatchEvent(new java.awt.event.ComponentEvent(panel, java.awt.event.ComponentEvent.COMPONENT_RESIZED));
            layoutAll(panel); layoutAll(panel);
            JScrollPane answers = null;
            for (JScrollPane scroll : components(panel, JScrollPane.class)) {
                if ("dialogue-replies-scroll".equals(scroll.getName())) { answers = scroll; }
            }
            assertNotNull(answers);
            assertTrue("A whole reply must remain reachable without closing history: " + answers.getViewport().getHeight(),
                    answers.getViewport().getHeight() >= 44);
            assertTrue(DialoguePreview.clickReply(panel, "choice0"));
        });
    }

    @Test public void longIngredientListKeepsNamesAndCountsReadableAndLastItemReachable() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            NpcDialoguePanel panel = new NpcDialoguePanel(new Requests());
            List<games.stendhal.common.NpcDialogueSnapshot.Item> items = new ArrayList<>();
            for (int i = 0; i < 14; i++) {
                items.add(new games.stendhal.common.NpcDialogueSnapshot.Item("item" + i, "sztabka mithrilu",
                        "items/resource/mithril_bar", 10000, 1234, -1, 100, 0, ""));
            }
            for (int width : new int[] {540, 690}) {
                panel.setSize(width, 620);
                panel.display(new DialoguePage("Baldemar", "Kowal.", "", "", "",
                        Arrays.asList(Part.text("Przynieś materiały.")),
                        Arrays.asList(new Reply("goodbye", "Do zobaczenia.")), true, items, false));
                layoutAll(panel); layoutAll(panel);
                JScrollPane strip = null;
                for (JScrollPane scroll : components(panel, JScrollPane.class)) {
                    if ("dialogue-ingredients-scroll".equals(scroll.getName())) { strip = scroll; }
                }
                assertNotNull(strip); assertTrue(strip.isVisible());
                assertTrue(strip.getHorizontalScrollBar().isVisible());
                Container row = (Container) strip.getViewport().getView();
                assertEquals(14, row.getComponentCount());
                for (Component component : row.getComponents()) {
                    JLabel label = (JLabel) component;
                    assertTrue(label.getText().contains("sztabka mithrilu"));
                    assertTrue(label.getText().contains("1234 / 10000"));
                    assertTrue("Ingredient must not be squeezed below its readable width", label.getWidth() >= label.getPreferredSize().width);
                    assertTrue("The whole caption must fit", label.getHeight() >= label.getPreferredSize().height);
                }
                strip.getHorizontalScrollBar().setValue(strip.getHorizontalScrollBar().getMaximum());
                layoutAll(strip);
                assertTrue("Last requirement must be fully reachable", strip.getViewport().getViewRect().contains(row.getComponent(13).getBounds()));
                assertTrue("One strip must not make the dialogue tall", strip.getHeight() <= 120);
                panel.display(page("Carmen", true));
                assertFalse("Empty requirements must not leave a blank strip", strip.isVisible());
            }
        });
    }

    @Test public void responsesCollapseToFiveWithIconsAndExpandWithoutSendingAnything() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Requests requests = new Requests(); NpcDialoguePanel panel = new NpcDialoguePanel(requests);
            List<Reply> choices = new ArrayList<Reply>();
            for (String id : Arrays.asList("quest", "wood", "offer", "help", "job", "back", "goodbye")) {
                choices.add(new Reply(id, "Odpowiedź " + id));
            }
            panel.setSize(690, 900);
            panel.display(new DialoguePage("Ouchit", "Opis NPC.", "data/sprites/npc/youngsoldiernpc.png", "", "",
                    Arrays.asList(Part.text("W czym pomóc?")), choices, true));
            JButton more = null; int visible = 0;
            for (JButton button : components(panel, JButton.class)) {
                if ("dialogue-more-replies".equals(button.getName())) { more = button; }
                if (choices.stream().anyMatch(c -> c.id.equals(button.getActionCommand()))) {
                    assertNotNull(button.getIcon()); if (button.isVisible()) { visible++; }
                }
            }
            assertEquals(5, visible); assertNotNull(more); assertTrue(more.isVisible());
            assertTrue("Old stretched size must not be preferred", panel.getPreferredSize().height < 650);
            more.doClick(); visible = 0;
            for (JButton button : components(panel, JButton.class)) {
                if (choices.stream().anyMatch(c -> c.id.equals(button.getActionCommand())) && button.isVisible()) { visible++; }
            }
            assertEquals(7, visible); assertTrue(requests.replies.isEmpty()); assertTrue(requests.texts.isEmpty());
            more.doClick(); assertTrue(DialoguePreview.clickReply(panel, "quest")); assertEquals(Arrays.asList("quest"), requests.replies);
        });
    }
}
