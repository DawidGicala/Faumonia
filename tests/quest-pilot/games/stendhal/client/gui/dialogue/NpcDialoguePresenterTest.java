/* SPDX-License-Identifier: GPL-2.0-or-later */
package games.stendhal.client.gui.dialogue;

import static org.junit.Assert.*;
import games.stendhal.common.NpcDialogueSnapshot;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import org.junit.Test;

public class NpcDialoguePresenterTest {
    private NpcDialogueSnapshot value(long revision, String feedback, String playerText, boolean active) {
        return new NpcDialogueSnapshot("session", revision, "Carmen", "Opis pobrany z NPC", "npc/welcomernpc",
                "Zioła dla Carmen", "", "Zapytaj o #arandula. #Sekret nie jest jeszcze tematem rozmowy.",
                feedback, playerText, active, revision == 1,
                active ? Arrays.asList(new NpcDialogueSnapshot.Reply("herb", "Gdzie rośnie arandula?", "arandula"))
                        : java.util.Collections.<NpcDialogueSnapshot.Reply>emptyList());
    }
    private static final class Sent implements NpcDialoguePanel.Listener {
        final List<String> texts = new ArrayList<String>();
        public void replyChosen(String id) { }
        public void textSubmitted(String text) { texts.add(text); }
        public void hideRequested() { }
    }
    private static <T extends Component> List<T> find(Container root, Class<T> type) {
        List<T> result = new ArrayList<T>();
        for (Component child : root.getComponents()) {
            if (type.isInstance(child)) { result.add(type.cast(child)); }
            if (child instanceof Container) { result.addAll(find((Container) child, type)); }
        }
        return result;
    }
    private static String history(NpcDialoguePanel panel) {
        try {
            for (JEditorPane pane : find(panel, JEditorPane.class)) {
                String text = pane.getDocument().getText(0, pane.getDocument().getLength());
                if (text.contains("Carmen:")) { return text; }
            }
        } catch (javax.swing.text.BadLocationException e) { throw new AssertionError(e); }
        throw new AssertionError("History missing");
    }
    private static int occurrences(String text, String value) {
        return (text.length() - text.replace(value, "").length()) / value.length();
    }

    @Test public void npcDataAndOnlyAvailableTopicsReachThePanel() {
        DialoguePage page = NpcDialoguePresenter.toPage(value(1, "", "", true));
        assertEquals("Opis pobrany z NPC", page.description);
        assertEquals("data/sprites/npc/welcomernpc.png", page.sprite);
        assertFalse(page.plainText().contains("#"));
        int links = 0;
        for (DialoguePage.Part part : page.words) {
            if (part.replyId != null) { links++; assertEquals("herb", part.replyId); }
        }
        assertEquals(1, links);
        DialoguePage ended = NpcDialoguePresenter.toPage(value(2, "", "", false));
        assertTrue(ended.replies.isEmpty());
        for (DialoguePage.Part part : ended.words) { assertNull(part.replyId); }
    }

    @Test public void unknownInputGivesFeedbackAndRestoresTypingWithoutRepeatingNpcSpeech() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Sent sent = new Sent(); NpcDialoguePanel panel = new NpcDialoguePanel(sent);
            NpcDialoguePresenter presenter = new NpcDialoguePresenter(panel);
            presenter.display(value(1, "", "hi", true));
            JTextField input = find(panel, JTextField.class).get(0);
            input.setText("asdasd"); input.postActionEvent(); assertFalse(input.isEnabled());
            presenter.display(value(2, "Nie rozpoznano odpowiedzi. Wybierz temat lub spróbuj inaczej.", "asdasd", true));
            assertTrue(input.isEnabled());
            assertEquals(1, occurrences(history(panel), "asdasd"));
            assertEquals(1, occurrences(history(panel), "Carmen:"));
            assertTrue(history(panel).contains("hi"));
            assertTrue(find(panel, JTextArea.class).get(0).getText().startsWith("Nie rozpoznano"));
            input.setText("arandula"); input.postActionEvent();
            assertEquals(Arrays.asList("asdasd", "arandula"), sent.texts);
        });
    }

    @Test public void disconnectDisablesButtonsLinksAndTextWithoutInventingDialogue() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            NpcDialoguePanel panel = new NpcDialoguePanel(new Sent());
            new NpcDialoguePresenter(panel).display(value(1, "", "", true));
            String before = history(panel);
            panel.markEnded("Połączenie zostało przerwane.");
            assertEquals(before, history(panel));
            assertFalse(find(panel, JTextField.class).get(0).isEnabled());
            for (JEditorPane pane : find(panel, JEditorPane.class)) { assertFalse(pane.getText().contains("reply:herb")); }
            assertEquals("Połączenie zostało przerwane.", find(panel, JTextArea.class).get(0).getText());
        });
    }

    @Test public void longInputStaysEditableWithoutSendingOrLosingDraft() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Sent sent = new Sent(); NpcDialoguePanel panel = new NpcDialoguePanel(sent);
            new NpcDialoguePresenter(panel).display(value(1, "", "", true));
            JTextField input = find(panel, JTextField.class).get(0);
            char[] chars = new char[501]; Arrays.fill(chars, 'a'); String draft = new String(chars);
            input.setText(draft); input.postActionEvent();
            assertEquals(draft, input.getText()); assertTrue(input.isEnabled()); assertTrue(sent.texts.isEmpty());
        });
    }
}
