/* SPDX-License-Identifier: GPL-2.0-or-later */
package games.stendhal.client.gui.dialogue;

import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.common.NpcDialogueSnapshot.Reply;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import marauroa.common.game.RPAction;
import org.junit.Test;
import static org.junit.Assert.*;

public class NpcDialogueControllerTest {
    private static final class View implements NpcDialogueController.View {
        final List<NpcDialogueSnapshot> snapshots = new ArrayList<NpcDialogueSnapshot>();
        boolean visible;
        int resets;
        String feedback = "";
        String ended = "";
        public void display(NpcDialogueSnapshot snapshot) { snapshots.add(snapshot); }
        public void reset() { resets++; }
        public void showWindow() { visible = true; }
        public void hideWindow() { visible = false; }
        public void showFeedback(String message) { feedback = message; }
        public void markEnded(String message) { ended = message; }
    }

    private static NpcDialogueSnapshot page(String session, long revision, boolean opened, boolean active) {
        return new NpcDialogueSnapshot(session, revision, "Carmen", "Opis Carmen", "npc/welcomernpc", "Zadanie",
                "", "Pomożesz?", "", "", active, opened, active
                ? Arrays.asList(new Reply("accept", "Przyniosę ci składniki.", "tak")) : Collections.<Reply>emptyList());
    }

    @Test public void closeSendsFarewellOnceAndKeepsHiddenOnUpdatesAndEnd() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            View view = new View(); List<RPAction> actions = new ArrayList<RPAction>();
            NpcDialogueController controller = new NpcDialogueController(actions::add, view);
            controller.receive(page("first", 1, true, true)); assertTrue(view.visible);
            controller.hideRequested(); assertFalse(view.visible); assertEquals(1, actions.size());
            assertEquals("1", actions.get(0).get("close"));
            controller.receive(page("first", 2, false, true)); assertFalse(view.visible);
            assertTrue(controller.reopen()); assertTrue(view.visible);
            controller.hideRequested(); controller.receive(page("first", 3, false, false));
            assertFalse(view.visible); controller.replyChosen("accept"); assertEquals(1, actions.size());
            controller.reset();
        });
    }

    @Test public void repliesCarrySessionRevisionAndOpaqueIdAndCannotDoubleSubmit() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            View view = new View(); List<RPAction> actions = new ArrayList<RPAction>();
            NpcDialogueController controller = new NpcDialogueController(actions::add, view);
            controller.receive(page("first", 3, true, true)); controller.replyChosen("accept");
            controller.replyChosen("accept"); controller.textSubmitted("tak"); assertEquals(1, actions.size());
            RPAction action = actions.get(0); assertEquals("npc_dialogue_reply", action.get("type"));
            assertEquals("first", action.get("session")); assertEquals("3", action.get("revision"));
            assertEquals("accept", action.get("reply")); assertFalse(action.has("text"));
            controller.receive(page("first", 4, false, true)); controller.textSubmitted("  moje pytanie  ");
            assertEquals(2, actions.size()); assertEquals("moje pytanie", actions.get(1).get("text"));
            assertFalse(actions.get(1).has("reply")); controller.reset();
        });
    }

    @Test public void disablingAutoOpenKeepsChatSessionAndManualOpeningAvailable() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            View view = new View(); List<RPAction> actions = new ArrayList<RPAction>();
            final boolean[] autoOpen = {false};
            NpcDialogueController controller = new NpcDialogueController(actions::add, view, () -> autoOpen[0]);
            controller.receive(page("first", 1, true, true));
            assertFalse(view.visible); assertEquals(1, view.snapshots.size()); assertTrue(actions.isEmpty());
            controller.receive(page("first", 2, false, true)); assertFalse(view.visible);
            assertTrue(controller.reopen()); assertTrue(view.visible);
            controller.replyChosen("accept"); assertEquals(1, actions.size());
            controller.receive(page("first", 3, false, true)); assertTrue(view.visible);
            controller.hideRequested();
            autoOpen[0] = true;
            controller.receive(page("first", 4, false, true)); assertFalse(view.visible);
            controller.receive(page("second", 1, true, true)); assertTrue(view.visible);
            controller.hideRequested(); autoOpen[0] = false;
            controller.receive(page("third", 1, true, true)); assertFalse(view.visible);
            assertEquals(3, actions.size()); controller.reset();
        });
    }

    @Test public void staleAndForeignSessionUpdatesCannotReopenOrReplaceCurrentConversation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            View view = new View(); NpcDialogueController controller = new NpcDialogueController(action -> {}, view);
            controller.receive(page("first", 1, true, true)); controller.receive(page("first", 3, false, true));
            controller.receive(page("first", 2, true, true)); controller.receive(page("first", 3, false, true));
            controller.receive(page("other", 1, false, true)); assertEquals(2, view.snapshots.size());
            controller.receive(page("second", 1, true, true)); controller.hideRequested();
            controller.receive(page("first", 20, true, true)); assertFalse(view.visible); assertEquals(3, view.snapshots.size());
            controller.reset();
        });
    }

    @Test public void timeoutRecoversControlsWithoutAutomaticallyRepeatingQuestAction() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            View view = new View(); List<RPAction> actions = new ArrayList<RPAction>();
            NpcDialogueController controller = new NpcDialogueController(actions::add, view);
            controller.receive(page("first", 1, true, true)); controller.replyChosen("accept");
            controller.recoverWaiting(); assertEquals(1, actions.size()); assertTrue(view.visible);
            assertTrue(view.feedback.contains("nie dotarła"));
            controller.receive(page("first", 2, false, true)); controller.textSubmitted("dziękuję");
            assertEquals(2, actions.size()); controller.reset();
        });
    }

    @Test public void invalidInputOrSendFailureReleasesWaitingAndReportsNeutralFeedback() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            View view = new View(); List<RPAction> actions = new ArrayList<RPAction>();
            NpcDialogueController controller = new NpcDialogueController(actions::add, view);
            controller.receive(page("first", 1, true, true)); controller.replyChosen("invented");
            assertTrue(view.feedback.contains("nie jest już dostępna")); controller.textSubmitted(" ");
            assertTrue(actions.isEmpty()); controller.textSubmitted(String.join("", Collections.nCopies(501, "a")));
            assertTrue(view.feedback.contains("500")); assertTrue(actions.isEmpty()); controller.reset();
            NpcDialogueController broken = new NpcDialogueController(action -> { throw new IllegalStateException("Test failure"); }, view);
            broken.receive(page("second", 1, true, true)); broken.replyChosen("accept");
            assertTrue(view.feedback.contains("Nie udało się wysłać")); broken.reset();
        });
    }

    @Test public void disconnectInvalidatesControlsAndIgnoresLateMessagesUntilReconnect() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            View view = new View(); List<RPAction> actions = new ArrayList<RPAction>();
            NpcDialogueController controller = new NpcDialogueController(actions::add, view);
            controller.receive(page("first", 1, true, true)); controller.hideRequested(); controller.disconnected();
            controller.replyChosen("accept"); controller.receive(page("first", 2, true, true));
            controller.receive(page("second", 1, true, true)); assertFalse(view.visible);
            assertEquals(1, actions.size()); assertEquals("1", actions.get(0).get("close"));
            assertEquals(1, view.snapshots.size()); assertFalse(view.ended.isEmpty());
            controller.connected(); controller.receive(page("first", 3, true, true)); assertFalse(view.visible);
            controller.receive(page("fresh", 1, true, true)); assertTrue(view.visible); controller.reset();
            assertFalse(controller.reopen());
        });
    }

    @Test public void serverFarewellHidesVisibleWindowAndKeepsHistoryAvailableManually() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            View view = new View(); List<RPAction> actions = new ArrayList<RPAction>();
            NpcDialogueController controller = new NpcDialogueController(actions::add, view);
            controller.receive(page("first", 1, true, true)); assertTrue(view.visible);
            controller.receive(page("first", 2, false, false)); assertFalse(view.visible);
            assertEquals(2, view.snapshots.size()); assertEquals(1, view.resets);
            assertTrue(controller.reopen()); assertTrue(view.visible);
            controller.replyChosen("accept"); controller.textSubmitted("tak"); assertTrue(actions.isEmpty());
            controller.receive(page("first", 3, false, true)); assertEquals(2, view.snapshots.size());
            controller.receive(page("second", 1, true, true)); assertTrue(view.visible);
            assertEquals(2, view.resets); controller.reset();
        });
    }

    @Test public void windowIsCenteredOnUsableGameMonitorAndFitsSmallScreens() {
        assertEquals(new Rectangle(370, 165, 700, 600), NpcDialogueController.centeredBounds(
                new Dimension(700, 600), new Rectangle(0, 30, 1440, 870)));
        assertEquals(new Rectangle(-990, 60, 700, 600), NpcDialogueController.centeredBounds(
                new Dimension(700, 600), new Rectangle(-1280, 0, 1280, 720)));
        assertEquals(new Rectangle(0, 24, 480, 400), NpcDialogueController.centeredBounds(
                new Dimension(700, 600), new Rectangle(0, 24, 480, 400)));
    }

    @Test public void automaticHeightReplacesStretchedSizeWhileManualModeKeepsItAcrossPages() {
        Dimension resized = new Dimension(760, 900);
        assertEquals(new Dimension(760, 550), NpcDialogueController.dialogueSize(resized, 550, 800, true));
        assertEquals(new Dimension(760, 784), NpcDialogueController.dialogueSize(resized, 1100, 800, true));
        Dimension manual = NpcDialogueController.dialogueSize(resized, 550, 800, false);
        assertEquals(resized, manual);
        assertEquals(resized, NpcDialogueController.dialogueSize(manual, 1100, 800, false));
        assertEquals(new Rectangle(0, 0, 640, 480), NpcDialogueController.centeredBounds(
                manual, new Rectangle(0, 0, 640, 480)));
    }

    @Test public void rememberedWindowBoundsStayUsableAfterMonitorChanges() {
        assertEquals(new Rectangle(740, 30, 700, 600), NpcDialogueController.fitBounds(
                new Rectangle(2000, -100, 700, 600), new Rectangle(0, 30, 1440, 870)));
        assertEquals(new Rectangle(-1280, 0, 1280, 720), NpcDialogueController.fitBounds(
                new Rectangle(-2000, 80, 2000, 1200), new Rectangle(-1280, 0, 1280, 720)));
        assertEquals(new Rectangle(20, 30, 540, 400), NpcDialogueController.fitBounds(
                new Rectangle(20, 30, -1, 0), new Rectangle(0, 0, 1440, 900)));
        assertEquals(new Rectangle(0, 24, 480, 400), NpcDialogueController.fitBounds(
                new Rectangle(2000, 80, 700, 600), new Rectangle(0, 24, 480, 400)));
    }

    @Test public void centerUsesGameWindowEvenWhenItIsNotMaximized() {
        assertEquals(new Rectangle(250, 200, 700, 600), NpcDialogueController.centeredBounds(
                new Dimension(700, 600), new Rectangle(100, 100, 1000, 800), new Rectangle(0, 0, 1920, 1080)));
        assertEquals(new Rectangle(0, 0, 700, 600), NpcDialogueController.centeredBounds(
                new Dimension(700, 600), new Rectangle(0, 0, 400, 300), new Rectangle(0, 0, 1920, 1080)));
    }

    @Test public void closeWhileReplyIsPendingSendsOnlyOneBoundCloseAndIgnoresFurtherClicks() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            View view = new View(); List<RPAction> actions = new ArrayList<RPAction>();
            NpcDialogueController controller = new NpcDialogueController(actions::add, view);
            controller.receive(page("first", 1, true, true)); controller.replyChosen("accept");
            controller.hideRequested(); controller.hideRequested(); controller.replyChosen("accept");
            assertEquals(2, actions.size()); assertEquals("1", actions.get(1).get("close"));
            assertEquals("first", actions.get(1).get("session")); assertFalse(view.visible);
            controller.receive(page("first", 2, false, true)); assertFalse(view.visible);
            controller.receive(page("first", 3, false, false)); assertFalse(view.visible);
            controller.hideRequested(); assertEquals(2, actions.size()); controller.reset();
        });
    }
}
