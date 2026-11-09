/* SPDX-License-Identifier: GPL-2.0-or-later */
package games.stendhal.client.gui.dialogue;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.gui.j2DClient;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.NpcDialogueSnapshot;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;
import javax.swing.JDialog;
import javax.swing.Timer;
import marauroa.common.game.RPAction;
import org.apache.log4j.Logger;

/** Coordinates a private NPC conversation. All instance methods run on the EDT. */
public final class NpcDialogueController implements NpcDialoguePanel.Listener {
    public static final String AUTO_OPEN_PROPERTY = "npc_dialogue.auto_open";
    public static final String SHOP_ENABLED_PROPERTY = "npc_dialogue.shop_enabled";
    public static final String AUTO_SIZE_PROPERTY = "npc_dialogue.auto_size";
    public interface Transport { void send(RPAction action); }
    public interface View {
        void display(NpcDialogueSnapshot snapshot);
        void reset();
        void showWindow();
        void hideWindow();
        void showFeedback(String message);
        void markEnded(String message);
    }

    private static final Logger LOGGER = Logger.getLogger(NpcDialogueController.class);
    private static NpcDialogueController instance;
    private final Transport transport;
    private final View view;
    private final BooleanSupplier autoOpen;
    private final Timer replyTimeout;
    private final Set<String> retiredSessions = new LinkedHashSet<String>();
    private NpcDialogueSnapshot current;
    private boolean active;
    private boolean pending;
    private boolean connected = true;
    private boolean closing;

    public static synchronized NpcDialogueController get() {
        if (instance == null) {
            instance = new NpcDialogueController(action -> {
                StendhalClient client = StendhalClient.get();
                if (client == null || !client.getConnectionState()) {
                    throw new IllegalStateException("No server connection");
                }
                client.send(action);
            });
        }
        return instance;
    }

    private NpcDialogueController(Transport transport) {
        this.transport = transport;
        view = new DialogView(this);
        autoOpen = () -> Boolean.parseBoolean(WtWindowManager.getInstance().getProperty(AUTO_OPEN_PROPERTY, "true"));
        replyTimeout = createTimeout();
    }

    /** Dependency injection also lets the offline test use the real server Engine. */
    public NpcDialogueController(Transport transport, View view) {
        this(transport, view, () -> true);
    }

    NpcDialogueController(Transport transport, View view, BooleanSupplier autoOpen) {
        if (transport == null || view == null || autoOpen == null) { throw new IllegalArgumentException("Missing dialogue dependency"); }
        this.transport = transport;
        this.view = view;
        this.autoOpen = autoOpen;
        replyTimeout = createTimeout();
    }

    private Timer createTimeout() {
        Timer timer = new Timer(8000, event -> recoverWaiting());
        timer.setRepeats(false);
        return timer;
    }

    public void receive(NpcDialogueSnapshot snapshot) {
        if (!connected || snapshot == null || retiredSessions.contains(snapshot.getSession())) { return; }
        boolean sameSession = current != null && current.getSession().equals(snapshot.getSession());
        if (sameSession && (snapshot.getRevision() <= current.getRevision() || (!active && snapshot.isActive()))) { return; }
        if (!sameSession && (!snapshot.isOpened() || !snapshot.isActive())) { return; }
        if (!sameSession) {
            if (current != null) { retire(current.getSession()); }
            closing = false;
            view.reset();
        }
        current = snapshot;
        active = snapshot.isActive();
        pending = false;
        replyTimeout.stop();
        view.display(snapshot);
        if (!active) { closing = false; view.hideWindow(); return; }
        if (closing) { pending = true; replyTimeout.restart(); }
        // An update to a hidden encounter never takes the focus back from chat.
        if (!sameSession && autoOpen.getAsBoolean()) { view.showWindow(); }
    }

    @Override public void replyChosen(String id) {
        if (current == null || !active || pending || closing) { return; }
        for (NpcDialogueSnapshot.Reply reply : current.getReplies()) {
            if (reply.getId().equals(id)) { send("reply", id); return; }
        }
        view.showFeedback("Ta odpowiedź nie jest już dostępna. Wybierz jedną z widocznych możliwości.");
    }

    @Override public void textSubmitted(String text) {
        if (current == null || !active || pending || closing) { return; }
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) { view.showFeedback("Napisz odpowiedź albo wybierz jedną z podpowiedzi."); return; }
        if (trimmed.length() > NpcDialogueSnapshot.MAX_INPUT) { view.showFeedback("Odpowiedź może mieć najwyżej 500 znaków."); return; }
        send("text", trimmed);
    }

    private void send(String key, String value) {
        send(key, value, null);
    }

    @Override public void purchaseRequested(String id, int amount) {
        if (current == null || !active || pending || closing || !current.isShop()) { return; }
        for (NpcDialogueSnapshot.Item item : current.getItems()) {
            if (item.getId().equals(id) && item.isForSale() && amount >= 1 && amount <= item.getMaxAmount()) {
                send("buy", id, amount); return;
            }
        }
        view.showFeedback("Wybierz przedmiot i ilość z aktualnej oferty.");
    }

    private void send(String key, String value, Integer amount) {
        RPAction action = new RPAction();
        action.put("type", NpcDialogueSnapshot.ACTION);
        action.put("session", current.getSession());
        action.put("revision", Long.toString(current.getRevision()));
        action.put(key, value);
        if (amount != null) { action.put("amount", Integer.toString(amount)); }
        pending = true;
        replyTimeout.restart();
        try { transport.send(action); }
        catch (RuntimeException e) {
            pending = false;
            if (closing) { closing = false; view.showWindow(); }
            replyTimeout.stop();
            view.showFeedback("Nie udało się wysłać odpowiedzi. Sprawdź połączenie i spróbuj ponownie.");
            LOGGER.warn("Unable to send NPC dialogue reply", e);
        }
    }

    /** X and Escape use the NPC's ordinary farewell, including while another reply is in flight. */
    @Override public void hideRequested() {
        view.hideWindow();
        if (current != null && active && connected && !closing) {
            closing = true; send("close", "1");
        }
    }

    public boolean reopen() {
        if (current == null) { return false; }
        view.showWindow();
        return true;
    }

    public void disconnected() {
        games.stendhal.client.gui.tutorial.TutorialGuideController.get().reset();
        replyTimeout.stop(); pending = false; active = false; connected = false; closing = false;
        if (current != null) {
            retire(current.getSession());
            view.markEnded("Utracono połączenie. Rozpocznij rozmowę ponownie po powrocie do gry.");
        }
    }

    /** A reconnect needs a fresh server encounter; it never reactivates cached actions. */
    public void connected() { connected = true; }

    public void reset() {
        games.stendhal.client.gui.tutorial.TutorialGuideController.get().reset();
        replyTimeout.stop(); pending = false; active = false; closing = false;
        current = null; connected = true; retiredSessions.clear(); view.reset(); view.hideWindow();
    }

    /** Recover controls, but never retry a quest-changing request automatically. */
    void recoverWaiting() {
        if (!pending) { return; }
        pending = false; replyTimeout.stop();
        if (closing) { closing = false; view.showWindow(); }
        view.showFeedback("Odpowiedź jeszcze nie dotarła. Możesz zaczekać lub spróbować ponownie.");
    }

    private void retire(String session) {
        retiredSessions.add(session);
        if (retiredSessions.size() > 256) { retiredSessions.remove(retiredSessions.iterator().next()); }
    }

    /** Keeps saved bounds usable when a monitor disappears or changes resolution. */
    static Rectangle fitBounds(Rectangle requested, Rectangle screen) {
        int width = Math.min(Math.max(540, requested.width), screen.width);
        int height = Math.min(Math.max(400, requested.height), screen.height);
        int x = Math.max(screen.x, Math.min(requested.x, screen.x + screen.width - width));
        int y = Math.max(screen.y, Math.min(requested.y, screen.y + screen.height - height));
        return new Rectangle(x, y, width, height);
    }

    /** Centre on the game's monitor, including after changing monitors or restoring an old size. */
    static Rectangle centeredBounds(Dimension requested, Rectangle screen) {
        Rectangle bounds = fitBounds(new Rectangle(screen.x, screen.y, requested.width, requested.height), screen);
        bounds.x = screen.x + (screen.width - bounds.width) / 2;
        bounds.y = screen.y + (screen.height - bounds.height) / 2;
        return bounds;
    }

    /** Centre relative to the game window, then keep the dialogue on its usable monitor. */
    static Rectangle centeredBounds(Dimension requested, Rectangle owner, Rectangle screen) {
        Rectangle bounds = fitBounds(new Rectangle(screen.x, screen.y, requested.width, requested.height), screen);
        bounds.x = owner.x + (owner.width - bounds.width) / 2;
        bounds.y = owner.y + (owner.height - bounds.height) / 2;
        return fitBounds(bounds, screen);
    }

    /** Automatic height follows the page; manual dimensions survive page changes. */
    static Dimension dialogueSize(Dimension current, int contentHeight, int mapHeight, boolean automatic) {
        return new Dimension(current.width, automatic
                ? Math.min(Math.max(400, mapHeight - 16), Math.max(400, contentHeight)) : current.height);
    }

    private static final class DialogView implements View {
        private final NpcDialogueController listener;
        private JDialog window;
        private NpcDialoguePanel panel;
        private NpcDialoguePresenter presenter;
        private boolean geometryReady;
        private boolean resizeQueued;

        DialogView(NpcDialogueController listener) { this.listener = listener; }

        private void ensureWindow() {
            if (window != null) { return; }
            j2DClient ui = j2DClient.get();
            window = new JDialog(ui.getMainFrame(), "Rozmowa");
            games.stendhal.client.gui.tutorial.TutorialGuideController.get().setConversationWindow(window);
            window.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            panel = new NpcDialoguePanel(listener);
            panel.addPropertyChangeListener("dialogue.contentSize", event -> queueContentResize());
            presenter = new NpcDialoguePresenter(panel);
            window.setContentPane(panel);
            window.pack();
            window.addWindowListener(new WindowAdapter() {
                @Override public void windowClosing(WindowEvent event) { listener.hideRequested(); }
            });
            WtWindowManager settings = WtWindowManager.getInstance();
            int width = settings.getPropertyInt("npc_dialogue.width", window.getWidth());
            int height = automaticSize() ? window.getHeight()
                    : settings.getPropertyInt("npc_dialogue.height", window.getHeight());
            settings.setProperty("npc_dialogue.layout_version", "3");
            window.setSize(Math.max(1, width), Math.max(1, height));
            positionAtCenter();
            geometryReady = true;
            window.addComponentListener(new ComponentAdapter() {
                @Override public void componentResized(ComponentEvent event) { rememberBounds(); }
            });
        }

        private void positionAtCenter() {
            GraphicsConfiguration graphics = j2DClient.get().getMainFrame().getGraphicsConfiguration();
            Rectangle usable = new Rectangle(graphics.getBounds());
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(graphics);
            usable.x += insets.left; usable.y += insets.top;
            usable.width -= insets.left + insets.right; usable.height -= insets.top + insets.bottom;
            window.setMinimumSize(new Dimension(Math.min(540, usable.width), Math.min(400, usable.height)));
            Rectangle map = j2DClient.get().getGameAreaBoundsOnScreen();
            Dimension size = dialogueSize(window.getSize(), panel.getPreferredSize().height
                    + window.getInsets().top + window.getInsets().bottom, map.height, automaticSize());
            window.setBounds(centeredBounds(size, map, usable));
        }

        private boolean automaticSize() {
            return Boolean.parseBoolean(WtWindowManager.getInstance().getProperty(AUTO_SIZE_PROPERTY, "true"));
        }

        private void queueContentResize() {
            if (resizeQueued) { return; }
            resizeQueued = true;
            javax.swing.SwingUtilities.invokeLater(() -> {
                resizeQueued = false;
                if (window != null && geometryReady && automaticSize()) { positionAtCenter(); }
            });
        }

        private void rememberBounds() {
            if (!geometryReady || window == null) { return; }
            Rectangle bounds = window.getBounds();
            WtWindowManager settings = WtWindowManager.getInstance();
            settings.setProperty("npc_dialogue.width", Integer.toString(bounds.width));
            // Keep the manual height even if automatic sizing is temporarily enabled.
            if (!automaticSize()) { settings.setProperty("npc_dialogue.height", Integer.toString(bounds.height)); }
        }

        public void display(NpcDialogueSnapshot snapshot) { ensureWindow(); presenter.display(snapshot); }
        public void reset() { if (presenter != null) { presenter.reset(); } }
        public void showWindow() {
            ensureWindow();
            if (!window.isVisible()) {
                if (!automaticSize()) {
                    window.setSize(window.getWidth(), Math.max(1, WtWindowManager.getInstance()
                            .getPropertyInt("npc_dialogue.height", window.getHeight())));
                }
                positionAtCenter();
            }
            window.setVisible(true);
        }
        public void hideWindow() { if (window != null) { rememberBounds(); window.setVisible(false); } }
        public void showFeedback(String message) { if (panel != null) { panel.showFeedback(message); } }
        public void markEnded(String message) { if (panel != null) { panel.markEnded(message); } }
    }
}
