package games.stendhal.client.gui.progress;

import static org.junit.Assert.*;
import games.stendhal.common.QuestProgress;
import games.stendhal.common.QuestProgress.Objective;
import games.stendhal.common.QuestProgress.State;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import org.junit.Test;

public class QuestProgressViewTest {
    private QuestProgress example() {
        return new QuestProgress("Carmen", State.ACTIVE, "Carmen", "Porozmawiaj o zapasach.",
                Arrays.asList(new Objective("arandula", "Oddano: arandula", 2, 5, 1)));
    }

    @Test public void unknownWireVersionsAndMalformedCountersFallBack() {
        List<String> data = new ArrayList<String>(example().toWire());
        data.set(0, "2"); assertNull(QuestProgress.fromWire(data));
        data.set(0, "1"); data.set(5, "1000000000"); assertNull(QuestProgress.fromWire(data));
        data.set(5, "1"); data.set(8, "-1"); assertNull(QuestProgress.fromWire(data));
        assertNull(QuestProgress.fromWire(Arrays.asList("1")));
        assertNull(QuestProgress.fromWire(null));
    }

    @Test public void typedProgressRetainsPolishTextAndEscapesMarkup() {
        QuestProgress value = new QuestProgress("test", State.ACTIVE, "<Karl>", "A & B",
                Arrays.asList(new Objective("x", "koński włos <img src='x'>", 0, 1, -1)));
        assertEquals(value.toWire(), QuestProgress.fromWire(value.toWire()).toWire());
        String html = QuestProgressFormatter.render(value);
        assertTrue(html.contains("koński włos"));
        assertTrue(html.contains("&lt;Karl&gt;"));
        assertFalse(html.contains("<img"));
    }

    @Test public void pageRefreshOnlyRequestsSelectedActivePilot() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                final int[] refreshes = {0};
                ProgressLog.Page page = new ProgressLog.Page();
                page.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
                ProgressStatusQuery query = new ProgressStatusQuery() {
                    public void fire(String value) { }
                    public void refresh(String value) { refreshes[0]++; }
                };
                page.setIndex(new ArrayList<String>(Arrays.asList("Zioła dla Carmen")), query);
                page.hyperlinkUpdate(new HyperlinkEvent(page, HyperlinkEvent.EventType.ACTIVATED, null, "Zioła dla Carmen"));
                page.setContent("Zioła dla Carmen", "", "", Arrays.<String>asList(), example(), false);
                page.refreshProgress(); assertEquals(1, refreshes[0]);
                page.hyperlinkUpdate(new HyperlinkEvent(page, HyperlinkEvent.EventType.ACTIVATED, null, "Inne zadanie"));
                page.refreshProgress(); assertEquals(1, refreshes[0]);
                page.setContent("Inne zadanie", "", "", Arrays.<String>asList(), null, false);
                page.refreshProgress(); assertEquals(1, refreshes[0]);
                QuestProgress done = new QuestProgress("test", State.COMPLETED, "", "Ukończone.", Arrays.<Objective>asList());
                page.setContent("Inne zadanie", "", "", Arrays.<String>asList(), done, false);
                page.refreshProgress(); assertEquals(1, refreshes[0]);
            }
        });
    }

    @Test public void rendersActualSwingJournalPage() throws Exception {
        PilotJournalDemo.renderExamples();
    }
}
