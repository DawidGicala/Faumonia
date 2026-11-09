/* SPDX-License-Identifier: GPL-2.0-or-later */
package games.stendhal.client.gui.dialogue;

import games.stendhal.common.Constants;
import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.client.gui.styled.StyledLookAndFeel;
import games.stendhal.client.gui.styled.WoodStyle;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.core.engine.RPClassGenerator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPRuleProcessor;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.HelpSedro;
import games.stendhal.server.maps.quests.HerbsForCarmen;
import games.stendhal.server.maps.quests.BowsForOuchit;
import games.stendhal.server.maps.ados.forest.FarmerNPC;
import games.stendhal.server.maps.semos.city.HealerNPC;
import games.stendhal.server.maps.semos.tavern.BowAndArrowSellerNPC;
import games.stendhal.server.maps.semos.village.SedroNPC;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.server.db.TransactionPool;
import marauroa.server.db.command.DBCommandQueue;

/**
 * Local review harness: real map NPCs, quest actions, private events and client UI.
 * It has no network connection, persistent player, database or world simulation.
 */
public final class IntegratedDialogueDemo implements NpcDialogueController.View, AutoCloseable {
    private final LocalRules rules;
    private final LocalZone zone = new LocalZone();
    private final Map<String, SpeakerNPC> previousNPCs = new LinkedHashMap<String, SpeakerNPC>();
    private final Map<String, SpeakerNPC> npcs = new LinkedHashMap<String, SpeakerNPC>();
    private final NpcDialogueController controller;
    private final DialogueWireClient wire;
    public final NpcDialoguePanel panel;
    private final NpcDialoguePresenter presenter;
    private Player player;
    private SpeakerNPC npc;
    private NpcDialogueSnapshot latest;
    private JFrame frame;
    private JDialog window;
    private JLabel testStatus;
    private JTextArea chatLog;
    private Component chatAnchor;
    private boolean visible;
    private boolean closed;
    private int deliveredSnapshots;
    private RPAction lastSent;

    private static final class LocalRules extends StendhalRPRuleProcessor {
        private final StendhalRPRuleProcessor previous;
        private final long started = System.nanoTime();
        private int advancedTurns;
        private boolean rendering;
        LocalRules() { previous = instance; instance = this; }
        @Override public int getTurn() { return advancedTurns + (int) ((System.nanoTime() - started) / 300000000L); }
        // Scripted screenshots cannot wait for the normal chat bucket to refill.
        // Interactive testing uses elapsed time and the actual throttle remains visible.
        void tick() { advancedTurns += rendering ? 34 : 1; }
        void restore() { instance = previous; }
    }

    /** No RPWorld bootstrap, login, achievements, database or map sync. */
    private static final class LocalZone extends StendhalRPZone {
        private int nextId;
        LocalZone() { super("dialogue_preview", 128, 128); }
        @Override public synchronized void add(RPObject object) {
            object.setID(new RPObject.ID(++nextId, getID()));
            if (object instanceof Entity) { ((Entity) object).onAdded(this); }
            if (object instanceof SpeakerNPC) {
                NPCList.get().add((SpeakerNPC) object);
                getNPCList().add((SpeakerNPC) object);
            }
            if (object instanceof Player) {
                getPlayers().add((Player) object);
                getPlayerAndFriends().add((Player) object);
            }
        }
        @Override public synchronized void modify(RPObject object) { /* In-memory snapshot only. */ }
    }

    public IntegratedDialogueDemo() {
        if (TransactionPool.get() != null) { throw new IllegalStateException("This demo must never use a database"); }
        rules = new LocalRules();
        new RPClassGenerator().createRPClasses();
        for (String name : new String[] {"Carmen", "Sedro", "Ouchit", "Karl"}) {
            previousNPCs.put(name, NPCList.get().remove(name));
        }
        new HealerNPC().configureZone(zone, Collections.<String, String>emptyMap());
        new SedroNPC().configureZone(zone, Collections.<String, String>emptyMap());
        new BowAndArrowSellerNPC().configureZone(zone, Collections.<String, String>emptyMap());
        new FarmerNPC().configureZone(zone, Collections.<String, String>emptyMap());
        for (String name : previousNPCs.keySet()) { npcs.put(name, NPCList.get().get(name)); }
        new HerbsForCarmen().addToWorld();
        new HelpSedro().addToWorld();
        new BowsForOuchit().addToWorld();
        wire = new DialogueWireClient();
        controller = new NpcDialogueController(action -> {
            lastSent = wire.roundTrip(action);
            rules.tick();
            if (!CommandCenter.execute(player, lastSent)) { throw new IllegalStateException("Dialogue dispatch failed"); }
            drain();
        }, this);
        panel = new NpcDialoguePanel(controller);
        presenter = new NpcDialoguePresenter(panel);
        newPlayer();
        start("Carmen");
    }

    public void newPlayer() {
        if (player != null) {
            for (SpeakerNPC existing : npcs.values()) {
                existing.getEngine().setCurrentState(ConversationStates.IDLE);
                existing.setAttending(null);
            }
            zone.getPlayers().remove(player); zone.getPlayerAndFriends().remove(player);
            player.onRemoved(zone);
        }
        controller.reset(); latest = null;
        player = new Player(new RPObject());
        player.put("name", "WędrowiecTestowy");
        player.put("base_hp", 100); player.put("hp", 100);
        player.put("atk", 10); player.put("def", 10); player.put("level", 3);
        player.put("atk_xp", 0); player.put("def_xp", 0);
        for (String slot : Constants.CARRYING_SLOTS) { player.addSlot(slot); }
        for (String slot : new String[] {"!quests", "!kills", "!tutorial", "!visited"}) {
            player.addSlot(slot); player.getSlot(slot).add(new RPObject());
        }
        player.update(); zone.add(player); updateStatus();
    }

    public void start(String name) {
        if (npc != null && npc.isTalking()) { depart(); }
        npc = npcs.get(name);
        player.setPosition(npc.getX(), npc.getY() + 1);
        npc.getEngine().setCurrentState(ConversationStates.IDLE); npc.setAttending(null);
        say("hi");
    }

    public void say(String text) {
        rules.tick(); npc.listenTo(player, text); drain();
    }

    public void choose(String id) {
        boolean offered = false;
        if (latest != null) {
            for (NpcDialogueSnapshot.Reply reply : latest.getReplies()) {
                if (reply.getId().equals(id)) { offered = true; break; }
            }
        }
        if (!offered) { throw new IllegalStateException("Unavailable test choice: " + id); }
        controller.replyChosen(id);
    }

    public void depart() {
        if (npc == null || !npc.isTalking()) { return; }
        player.setPosition(npc.getX() + 20, npc.getY() + 20);
        rules.tick(); npc.preLogic(); drain();
    }

    public void addIngredients() {
        for (Map.Entry<String, Integer> required : HerbsForCarmen.getRequiredItems().entrySet()) {
            int missing = required.getValue() - player.getNumberOfEquipped(required.getKey());
            if (missing > 0) {
                Item item = SingletonRepository.getEntityManager().getItem(required.getKey());
                if (!(item instanceof StackableItem)) { throw new IllegalStateException("Missing ingredient: " + required.getKey()); }
                ((StackableItem) item).setQuantity(missing); player.equipToInventoryOnly(item);
            }
        }
        updateStatus("Dodano składniki do plecaka postaci testowej. Przekaż je Carmen podczas rozmowy.");
    }

    public void addWood() {
        int missing = 10 - player.getNumberOfEquipped("polano");
        if (missing > 0) {
            Item item = SingletonRepository.getEntityManager().getItem("polano");
            ((StackableItem) item).setQuantity(missing); player.equipToInventoryOnly(item);
        }
        updateStatus("Dodano 10 polan do testowego plecaka. Ouchit przyjmie je po przyjęciu jego zadania.");
    }

    public void loseHair() {
        if (player.getNumberOfEquipped("koński włos") > 0) {
            player.drop("koński włos", 1);
            updateStatus("Testowa postać zgubiła koński włos. Wróć do Karla po kolejny.");
        } else { updateStatus("Postać nie ma końskiego włosa. Najpierw odbierz go od Karla."); }
    }

    public void simulateSedroKills() {
        String record = player.getQuest("help_sedro", 1);
        if (record == null || !player.isQuestInState("help_sedro", 0, "start")) {
            updateStatus("Najpierw przyjmij zadanie Sedro. Ten przycisk nie przyjmuje zadania za gracza."); return;
        }
        String[] values = record.split(",");
        for (int i = 0; i + 4 < values.length; i += 5) {
            String name = values[i];
            int soloRequired = Integer.parseInt(values[i + 1]);
            int sharedRequired = Integer.parseInt(values[i + 2]);
            int initialSolo = Integer.parseInt(values[i + 3]);
            int initialShared = Integer.parseInt(values[i + 4]);
            while (player.getSoloKill(name) - initialSolo < soloRequired
                    || player.getSoloKill(name) + player.getSharedKill(name) - initialSolo - initialShared < soloRequired + sharedRequired) {
                player.setSoloKill(name);
            }
        }
        updateStatus("Zasymulowano wymagane zabicia dla testowej postaci. Porozmawiaj z Sedro o zadaniu.");
    }

    private void drain() {
        List<RPEvent> events = new ArrayList<RPEvent>(player.events()); player.clearEvents();
        for (RPEvent event : events) {
            if (NpcDialogueSnapshot.EVENT.equals(event.getName())) {
                NpcDialogueSnapshot snapshot = NpcDialogueSnapshot.fromWire(event.getList("data"));
                if (snapshot == null) { throw new IllegalStateException("Invalid private dialogue event"); }
                deliveredSnapshots++; controller.receive(snapshot);
            }
        }
        updateStatus();
    }

    public void display(NpcDialogueSnapshot snapshot) {
        latest = snapshot; presenter.display(snapshot);
        if (window != null) { window.setTitle("Rozmowa z " + snapshot.getNpcName()); }
        if (chatLog != null) {
            if (!snapshot.getPlayerText().isEmpty()) { chatLog.append("Ty: " + snapshot.getPlayerText() + "\n"); }
            if (!snapshot.getSpeech().isEmpty() && snapshot.getFeedback().isEmpty()) {
                chatLog.append(snapshot.getNpcName() + ": " + snapshot.getSpeech().replace("#", "") + "\n");
            }
            chatLog.setCaretPosition(chatLog.getDocument().getLength());
        }
    }
    public void reset() { if (presenter != null) { presenter.reset(); } }
    public void showWindow() {
        visible = true;
        if (window != null) {
            if (!window.isVisible()) { centerWindow(); }
            window.setVisible(true);
        }
    }
    public void hideWindow() { visible = false; if (window != null) { window.setVisible(false); } }
    public void showFeedback(String message) { panel.showFeedback(message); }
    public void markEnded(String message) { panel.markEnded(message); }

    private void updateStatus() {
        updateStatus("Postać testowa - Carmen: " + questStatus(HerbsForCarmen.QUEST_SLOT)
                + " · Sedro: " + questStatus("help_sedro") + " · Ouchit: " + questStatus(BowsForOuchit.QUEST_SLOT)
                + " · XP: " + player.getXP());
    }
    private String questStatus(String slot) {
        String state = player.getQuest(slot);
        return state == null ? "nieprzyjęte" : state.startsWith("done") ? "ukończone"
                : "rejected".equals(state) ? "odrzucone" : "w toku";
    }
    private void updateStatus(String text) { if (testStatus != null) { testStatus.setText(text); } }

    public void open() {
        frame = new JFrame("Faumonia - lokalny test trzech zadań: Carmen, Sedro i Ouchit");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel controls = new JPanel(); controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        JLabel explanation = new JLabel("TEST LOKALNY: rzeczywiste dialogi i zadania, jednorazowa postać. Bez połączenia z serwerem.");
        explanation.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10)); controls.add(explanation);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> selection = new JComboBox<String>(new String[] {"Carmen", "Sedro", "Ouchit", "Karl"});
        selection.addActionListener(event -> start((String) selection.getSelectedItem())); actions.add(selection);
        addButton(actions, "Przywitaj się", () -> start((String) selection.getSelectedItem()));
        addButton(actions, "Pokaż rozmowę", () -> controller.reopen());
        addButton(actions, "Odejdź od NPC", this::depart);
        addButton(actions, "Nowa postać testowa", () -> { newPlayer(); start((String) selection.getSelectedItem()); });
        controls.add(actions);
        JPanel helpers = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton(helpers, "Test: dodaj składniki Carmen", this::addIngredients);
        addButton(helpers, "Test: zalicz cele Sedro", this::simulateSedroKills);
        addButton(helpers, "Test: niezrozumiałe pytanie", () -> controller.textSubmitted("Czy księżyc jest z sera?"));
        controls.add(helpers);
        JPanel woodHelpers = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton(woodHelpers, "Test: dodaj 10 polan Ouchita", this::addWood);
        addButton(woodHelpers, "Test: zgub koński włos", this::loseHair);
        controls.add(woodHelpers);
        testStatus = new JLabel(" "); testStatus.setBorder(BorderFactory.createEmptyBorder(2, 10, 8, 10));
        controls.add(testStatus); frame.add(controls, BorderLayout.NORTH);
        JPanel backdrop = new JPanel(new BorderLayout()); backdrop.setBackground(new Color(44, 61, 42));
        JLabel instructions = new JLabel("<html><center>Okno rozmowy możesz przesuwać i ukrywać.<br>" +
                "Przyciski testowe służą wyłącznie przygotowaniu składników i celów zadania.<br>" +
                "Oddanie przedmiotów, odmowa, nagrody i zakończenie rozmowy korzystają z kodu gry.</center></html>", SwingConstants.CENTER);
        instructions.setForeground(new Color(237, 228, 204)); backdrop.add(instructions); frame.add(backdrop, BorderLayout.CENTER);
        JPanel chat = new JPanel(new BorderLayout(4, 4));
        chat.setBorder(BorderFactory.createTitledBorder("Zwykły czat do NPC - działa również przy ukrytym oknie rozmowy"));
        JTextField input = new JTextField(); chatAnchor = input;
        input.addActionListener(event -> {
            if (!input.getText().trim().isEmpty()) { say(input.getText().trim()); input.setText(""); }
        });
        chat.add(input, BorderLayout.NORTH); chatLog = new JTextArea(4, 40); chatLog.setEditable(false);
        chatLog.setLineWrap(true); chatLog.setWrapStyleWord(true); chat.add(new JScrollPane(chatLog), BorderLayout.CENTER);
        frame.add(chat, BorderLayout.SOUTH); frame.setSize(1080, 880); frame.setLocationRelativeTo(null);
        frame.addWindowListener(new WindowAdapter() { @Override public void windowClosed(WindowEvent event) { close(); } });
        frame.setVisible(true);
        window = new JDialog(frame, "Rozmowa z " + latest.getNpcName());
        window.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent event) { controller.hideRequested(); } });
        window.setContentPane(panel); window.setMinimumSize(new Dimension(540, 560)); window.pack();
        games.stendhal.client.gui.WindowUtils.closeOnEscape(window);
        centerWindow();
        updateStatus(); if (visible) { window.setVisible(true); }
    }

    private static void addButton(JPanel panel, String title, Runnable action) {
        JButton button = new JButton(title); button.addActionListener(event -> action.run()); panel.add(button);
    }

    private void centerWindow() {
        Rectangle screen = new Rectangle(frame.getGraphicsConfiguration().getBounds());
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());
        screen.x += insets.left; screen.y += insets.top;
        screen.width -= insets.left + insets.right; screen.height -= insets.top + insets.bottom;
        window.setBounds(NpcDialogueController.centeredBounds(window.getSize(), frame.getBounds(), screen));
    }

    @Override public void close() {
        if (closed) { return; }
        closed = true; controller.reset();
        if (window != null) { window.dispose(); }
        for (String name : npcs.keySet()) {
            SpeakerNPC current = npcs.get(name);
            current.getEngine().setCurrentState(ConversationStates.IDLE); current.setAttending(null);
            NPCList.get().remove(name);
            if (previousNPCs.get(name) != null) { NPCList.get().add(previousNPCs.get(name)); }
        }
        wire.close(); rules.restore(); DBCommandQueue.get().finish();
        if (TransactionPool.get() != null) { throw new IllegalStateException("Unexpected database in local demo"); }
    }

    private static void layout(Container container) {
        container.doLayout();
        for (Component component : container.getComponents()) {
            if (component instanceof Container) { layout((Container) component); }
        }
    }
    private static void save(IntegratedDialogueDemo demo, File directory, String name, Runnable scene) throws Exception {
        save(demo, directory, name, 690, 0, scene);
    }
    private static void save(IntegratedDialogueDemo demo, File directory, String name, int width, int height, Runnable scene) throws Exception {
        final int[] renderedHeight = {height};
        SwingUtilities.invokeAndWait(() -> {
            scene.run(); demo.panel.setSize(width, 600); layout(demo.panel);
            renderedHeight[0] = height > 0 ? height : demo.panel.getPreferredSize().height;
            demo.panel.setSize(width, renderedHeight[0]); layout(demo.panel);
        });
        SwingUtilities.invokeAndWait(() -> {
            layout(demo.panel);
            BufferedImage image = new BufferedImage(width, renderedHeight[0], BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics(); demo.panel.printAll(graphics); graphics.dispose();
            try { ImageIO.write(image, "png", new File(directory, name + ".png")); }
            catch (IOException e) { throw new IllegalStateException(e); }
        });
    }

    /** Install the same delegates and configured base font as the Java client. */
    private static void installGameLookAndFeel() {
        try {
            StyledLookAndFeel look = new StyledLookAndFeel(WoodStyle.getInstance());
            UIManager.setLookAndFeel(look);
            look.setDefaultFontSize(WtWindowManager.getInstance().getPropertyInt("ui.font_size", 12));
            UIManager.getLookAndFeelDefaults().put("ClassLoader", IntegratedDialogueDemo.class.getClassLoader());
        } catch (UnsupportedLookAndFeelException e) { throw new IllegalStateException("Cannot install the game look and feel", e); }
    }

    /** Focused visual check of the current classpath; no three-quest validation claim. */
    public static void renderStyleExamples() throws Exception {
        File directory = new File(System.getProperty("quest.pilot.output", ".")); directory.mkdirs();
        final IntegratedDialogueDemo[] holder = new IntegratedDialogueDemo[1];
        SwingUtilities.invokeAndWait(() -> {
            installGameLookAndFeel(); holder[0] = new IntegratedDialogueDemo(); holder[0].rules.rendering = true;
        });
        IntegratedDialogueDemo demo = holder[0];
        try {
            save(demo, directory, "metal-0-carmen", () -> {});
            save(demo, directory, "metal-1-carmen-offer", () -> { demo.choose("quest"); demo.choose("know"); demo.choose("ingredients"); });
            save(demo, directory, "metal-2-unknown", () -> {
                demo.choose("accept"); demo.controller.textSubmitted("Czy księżyc jest z sera?");
            });
            if (demo.latest.getFeedback().isEmpty()) { throw new IllegalStateException("Unknown input has no feedback"); }
            save(demo, directory, "metal-3-delivery-history", () -> {
                demo.addIngredients(); demo.choose("supplies"); demo.panel.setHistoryVisible(true);
            });
            save(demo, directory, "metal-4-minimum-width", 540, 560, () -> {});
            Files.write(new File(directory, "metal-style-validation.txt").toPath(),
                    ("Visual-only preview: Java client with game StyledLookAndFeel and the supplied local server classes.\n"
                    + "Actual Carmen dialogue, unknown-input feedback, delivery list, expanded history and minimum window size.\n"
                    + "This run does not validate the final three quests or newest server/editorial changes.\n").getBytes(StandardCharsets.UTF_8));
            System.out.println("Metal dialogue style: 5 actual Carmen states rendered; visual-only preview.");
        } finally { SwingUtilities.invokeAndWait(demo::close); }
    }

    public static void renderExamples() throws Exception {
        File directory = new File(System.getProperty("quest.pilot.output", ".")); directory.mkdirs();
        final IntegratedDialogueDemo[] holder = new IntegratedDialogueDemo[1];
        SwingUtilities.invokeAndWait(() -> {
            installGameLookAndFeel(); holder[0] = new IntegratedDialogueDemo(); holder[0].rules.rendering = true;
        });
        IntegratedDialogueDemo demo = holder[0];
        try {
            save(demo, directory, "integrated-0-carmen", () -> {});
            save(demo, directory, "integrated-1-carmen-offer", () -> { demo.choose("quest"); demo.choose("know"); demo.choose("ingredients"); });
            save(demo, directory, "integrated-2-carmen-active", () -> demo.choose("accept"));
            save(demo, directory, "integrated-3-unknown", () -> demo.controller.textSubmitted("Czy księżyc jest z sera?"));
            if (demo.latest.getFeedback().isEmpty()) { throw new IllegalStateException("Unknown input has no feedback"); }
            save(demo, directory, "integrated-4-carmen-delivery", () -> { demo.addIngredients(); demo.choose("supplies"); });
            save(demo, directory, "integrated-5-carmen-complete", () -> {
                int count = 0;
                while (!"done".equals(demo.player.getQuest(HerbsForCarmen.QUEST_SLOT))) {
                    String choice = null;
                    for (NpcDialogueSnapshot.Reply reply : demo.latest.getReplies()) {
                        if (reply.getId().startsWith("give_")) { choice = reply.getId(); break; }
                    }
                    if (choice == null || ++count > 20) { throw new IllegalStateException("Cannot complete Carmen delivery"); }
                    demo.choose(choice);
                }
            });
            if (demo.latest.getRewards().size() != 5) { throw new IllegalStateException("Carmen reward receipt missing after wire transport"); }
            SwingUtilities.invokeAndWait(() -> demo.verifyNoRewardReplay("Carmen"));
            save(demo, directory, "integrated-6-sedro-offer", () -> { demo.start("Sedro"); demo.choose("quest"); });
            if (demo.latest.getNarration().isEmpty()) { throw new IllegalStateException("Sedro's quest offer has no source narration"); }
            save(demo, directory, "integrated-18-sedro-accepted", () -> demo.choose("accept"));
            save(demo, directory, "integrated-7-sedro-progress", () -> { demo.choose("back"); demo.choose("quest"); });
            save(demo, directory, "integrated-19-sedro-detailed-hint", () -> {
                demo.choose("hints"); demo.choose("rats");
            });
            save(demo, directory, "integrated-8-sedro-complete", () -> { demo.simulateSedroKills(); demo.choose("back"); demo.choose("quest"); });
            if (!demo.player.isQuestInState("help_sedro", 0, "done")) { throw new IllegalStateException("Sedro not completed"); }
            if (demo.latest.getRewards().size() != 12) { throw new IllegalStateException("Sedro reward receipt missing after wire transport"); }
            save(demo, directory, "integrated-9-departed", () -> demo.depart());
            if (demo.latest.isActive()) { throw new IllegalStateException("Departure did not end dialogue"); }
            SwingUtilities.invokeAndWait(() -> { demo.start("Carmen"); demo.controller.hideRequested(); demo.say("praca"); });
            if (demo.visible) { throw new IllegalStateException("Hidden conversation reopened on ordinary chat"); }
            int xp = demo.player.getXP();
            SwingUtilities.invokeAndWait(() -> { demo.start("Sedro"); demo.say("zadanie"); });
            if (demo.player.getXP() != xp) { throw new IllegalStateException("Quest reward duplicated"); }
            save(demo, directory, "integrated-10-ouchit-offer", () -> {
                demo.start("Ouchit"); demo.choose("quest"); demo.choose("interest");
            });
            save(demo, directory, "integrated-11-ouchit-wood", () -> demo.choose("accept"));
            save(demo, directory, "integrated-12-ouchit-karl", () -> {
                demo.addWood(); demo.say("zadanie"); demo.choose("wood");
            });
            if (!demo.player.isQuestInState(BowsForOuchit.QUEST_SLOT, "hair")
                    || demo.player.getNumberOfEquipped("polano") != 0) {
                throw new IllegalStateException("Ouchit did not consume wood and advance to hair");
            }
            save(demo, directory, "integrated-13-karl-hair", () -> { demo.start("Karl"); demo.choose("ouchit"); });
            if (demo.player.getNumberOfEquipped("koński włos") != 1) { throw new IllegalStateException("Karl did not give horse hair"); }
            save(demo, directory, "integrated-14-karl-recovery", () -> {
                demo.loseHair(); demo.start("Karl"); demo.choose("ouchit");
            });
            if (demo.player.getNumberOfEquipped("koński włos") != 1) { throw new IllegalStateException("Lost horse hair was not recoverable"); }
            save(demo, directory, "integrated-15-ouchit-return", () -> demo.start("Ouchit"));
            save(demo, directory, "integrated-16-ouchit-complete", () -> demo.choose("hair"));
            if (demo.latest.getRewards().size() != 5) { throw new IllegalStateException("Ouchit reward receipt missing after wire transport"); }
            if (!demo.player.isQuestInState(BowsForOuchit.QUEST_SLOT, "done")
                    || demo.player.getNumberOfEquipped("koński włos") != 0
                    || demo.player.getNumberOfEquipped("łuk ouchita") != 1
                    || demo.player.getNumberOfEquipped("strzała") != 500
                    || demo.player.getNumberOfEquipped("strzała żelazna") != 300) {
                throw new IllegalStateException("Ouchit's original quest rewards do not match");
            }
            SwingUtilities.invokeAndWait(() -> demo.verifyNoRewardReplay("Ouchit"));
            save(demo, directory, "integrated-17-ouchit-departed", () -> demo.depart());
            if (demo.latest.isActive() || demo.visible) { throw new IllegalStateException("Ouchit departure did not close dialogue"); }
            Files.write(new File(directory, "integrated-demo-validation.txt").toPath(),
                    ("Actual map NPCs: Carmen, Sedro, Ouchit, Karl\nActual StendhalClient.send, binary Encoder/Decoder and CommandCenter dispatch.\nActual Player, quest Engine, action listener and private RPEvent wire round-trip.\n"
                    + "All three quests completed with the same player; reward replay rejected; unknown text acknowledged; hidden chat preserved; departure ended conversation.\n"
                    + "Ouchit: 10 logs consumed, Karl gave horse hair, lost hair recovered, hair consumed, original bow+500 arrows+300 iron arrows received.\n"
                    + "Sedro: source narration, short acceptance and explicitly requested detailed hint rendered.\n"
                    + "Private snapshots delivered: " + demo.deliveredSnapshots + "\nNo network, database, live server, persistent player or combat simulation.\n").getBytes(StandardCharsets.UTF_8));
            System.out.println("Integrated local dialogue demo: 20 rendered states; three quests, recovery, lifecycle, hints and reward replay checks passed.");
        } finally { SwingUtilities.invokeAndWait(demo::close); }
    }

    /** Replay the actual accepted packet; the server revision guard must reject it. */
    private void verifyNoRewardReplay(String name) {
        int xp = player.getXP();
        int potions = player.getNumberOfEquipped("mały eliksir");
        int bows = player.getNumberOfEquipped("łuk ouchita");
        int arrows = player.getNumberOfEquipped("strzała");
        int ironArrows = player.getNumberOfEquipped("strzała żelazna");
        rules.tick(); CommandCenter.execute(player, wire.roundTrip(lastSent)); drain();
        if (xp != player.getXP() || potions != player.getNumberOfEquipped("mały eliksir")
                || bows != player.getNumberOfEquipped("łuk ouchita") || arrows != player.getNumberOfEquipped("strzała")
                || ironArrows != player.getNumberOfEquipped("strzała żelazna")) {
            throw new IllegalStateException(name + " reward replay changed the player");
        }
    }

    public static void renderEnhancements() throws Exception {
        File directory = new File(System.getProperty("quest.pilot.output", ".")); directory.mkdirs();
        final IntegratedDialogueDemo[] holder = new IntegratedDialogueDemo[1];
        SwingUtilities.invokeAndWait(() -> {
            installGameLookAndFeel(); holder[0] = new IntegratedDialogueDemo(); holder[0].rules.rendering = true;
        });
        IntegratedDialogueDemo demo = holder[0];
        try {
            save(demo, directory, "new-01-intro", () -> { demo.choose("quest"); demo.choose("know"); });
            save(demo, directory, "new-02-ingredients", () -> { demo.choose("ingredients"); demo.choose("accept"); });
            save(demo, directory, "new-03-delivery", () -> { demo.addIngredients(); demo.choose("supplies"); });
            save(demo, directory, "new-04-minimum", 540, 560, () -> demo.panel.setHistoryVisible(true));
            save(demo, directory, "new-05-shop", () -> {
                Item money = SingletonRepository.getEntityManager().getItem("money");
                ((StackableItem) money).setQuantity(1000); demo.player.equipToInventoryOnly(money);
                Item potion = SingletonRepository.getEntityManager().getItem("mały eliksir");
                ((StackableItem) potion).setQuantity(54); demo.player.equipToInventoryOnly(potion);
                demo.panel.setHistoryVisible(false); demo.choose("later"); demo.choose("back"); demo.choose("offer");
            });
            save(demo, directory, "new-06-confirm", () -> {
                for (NpcDialogueSnapshot.Item row : demo.latest.getItems()) {
                    if ("mały eliksir".equals(row.getName())) { demo.controller.purchaseRequested(row.getId(), 2); break; }
                }
                if (demo.npc.getEngine().getCurrentState() != ConversationStates.BUY_PRICE_OFFERED) {
                    throw new IllegalStateException("Purchase did not reach confirmation through real client wire");
                }
                if (demo.player.getNumberOfEquipped("money") != 1000) { throw new IllegalStateException("Early charge"); }
            });
            save(demo, directory, "new-07-purchased", () -> demo.choose("confirm"));
            if (demo.player.getNumberOfEquipped("mały eliksir") != 56
                    || demo.latest.getMoney() != 900) { throw new IllegalStateException("Purchase or balance refresh failed"); }
            save(demo, directory, "new-08-ouchit-shop", () -> { demo.start("Ouchit"); demo.choose("offer"); });
            save(demo, directory, "new-09-karl-shop", () -> { demo.start("Karl"); demo.choose("offer"); });
            save(demo, directory, "new-10-sedro-monsters", () -> {
                demo.start("Sedro"); demo.choose("quest"); demo.choose("accept");
                demo.player.setSoloKill("szczur"); demo.player.setSoloKill("szczur");
                demo.choose("back"); demo.choose("quest");
            });
            save(demo, directory, "new-11-ouchit-collapsed", () -> {
                demo.start("Ouchit"); demo.choose("quest"); demo.choose("interest"); demo.choose("accept"); demo.choose("back");
            });
            save(demo, directory, "new-12-ouchit-expanded", () -> {
                if (!DialoguePreview.clickReply(demo.panel, "more_replies")) { throw new IllegalStateException("No expansion control"); }
            });
            System.out.println("Dialogue enhancements: 12 real NPC scenes rendered; purchase through real client serialization and server dispatch passed.");
        } finally { SwingUtilities.invokeAndWait(demo::close); }
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "--render".equals(args[0])) { renderExamples(); }
        else if (args.length > 0 && "--render-style".equals(args[0])) { renderStyleExamples(); }
        else if (args.length > 0 && "--render-enhancements".equals(args[0])) { renderEnhancements(); }
        else { SwingUtilities.invokeLater(() -> { installGameLookAndFeel(); new IntegratedDialogueDemo().open(); }); }
    }
}
