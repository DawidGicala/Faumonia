package games.stendhal.client.gui.progress;

import games.stendhal.common.Constants;
import games.stendhal.common.QuestProgress;
import games.stendhal.server.core.engine.RPClassGenerator;
import games.stendhal.server.core.rp.quest.PilotQuestProgress;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.AbstractQuest;
import games.stendhal.server.maps.quests.BowsForOuchit;
import games.stendhal.server.maps.quests.HelpSedro;
import games.stendhal.server.maps.quests.HerbsForCarmen;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Component;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.imageio.ImageIO;
import javax.swing.*;
import marauroa.common.game.RPObject;

/** Offline demo: actual journal component and server adapters, synthetic local characters. */
public final class PilotJournalDemo {
    private static final String[] SCENARIOS = {"Carmen - część oddana", "Carmen - gotowe składniki",
            "Sedro - polowanie", "Sedro - powrót", "Ouchit - drewno", "Ouchit - szukanie Karla"};

    private static ProgressLog.Page page(int scenario) {
        new RPClassGenerator().createRPClasses();
        Player player = new Player(new RPObject());
        player.put("name", "PodgladLocal");
        player.addSlot("!quests"); player.addSlot("!kills");
        player.getSlot("!quests").add(new RPObject()); player.getSlot("!kills").add(new RPObject());
        for (String slot : Constants.CARRYING_SLOTS) { player.addSlot(slot); }
        AbstractQuest quest;
        String title;
        if (scenario < 2) {
            quest = new HerbsForCarmen(); title = "Zioła dla Carmen";
            player.setKeyedSlot("!quests", quest.getSlotName(), "arandula=3;jabłko=3");
            carry(player, "arandula", scenario == 0 ? 1 : 3);
            if (scenario == 1) { carry(player, "jabłko", 3); }
        } else if (scenario < 4) {
            quest = new HelpSedro(); title = "Pomóż Sedro";
            player.setKeyedSlot("!quests", quest.getSlotName(),
                    "start;gnom,0,1,0,0,panna gnom,0,1,0,0,szczur,0,5,0,0,szczur jaskiniowy,0,3,0,0");
            player.setSoloKill("gnom"); player.setSoloKill("szczur"); player.setSharedKill("szczur");
            if (scenario == 3) {
                player.setSoloKill("panna gnom");
                for (int i = 0; i < 3; i++) { player.setSoloKill("szczur"); player.setSharedKill("szczur jaskiniowy"); }
            }
        } else {
            quest = new BowsForOuchit(); title = "Łuki dla Ouchita";
            player.setKeyedSlot("!quests", quest.getSlotName(), scenario == 4 ? "polano" : "hair");
            if (scenario == 4) { carry(player, "polano", 10); }
        }
        QuestProgress progress = PilotQuestProgress.read(quest, player);
        ProgressLog.Page page = new ProgressLog.Page();
        page.setFont(new Font("Dialog", Font.PLAIN, 12)); page.setFontName("Serif");
        page.setIndex(new ArrayList<String>(Arrays.asList("Zioła dla Carmen", "Pomóż Sedro", "Łuki dla Ouchita")), null);
        page.setContent(title, "", "", quest.getHistory(player), progress, false);
        page.setSize(760, 520);
        return page;
    }

    private static void carry(Player player, String name, int count) {
        StackableItem item = new StackableItem(name, "test", "test", Collections.<String, String>emptyMap());
        item.setQuantity(count); player.getSlot("bag").add(item);
    }

    private static void layout(Container component) {
        component.doLayout();
        for (Component child : component.getComponents()) {
            if (child instanceof Container) { layout((Container) child); }
        }
    }

    public static void renderExamples() throws Exception {
        for (int i = 0; i < SCENARIOS.length; i++) {
            final int scenario = i;
            final ProgressLog.Page[] holder = new ProgressLog.Page[1];
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() { holder[0] = page(scenario); layout(holder[0]); }
            });
            // Drain deferred Swing layout/scroll work before rendering.
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    layout(holder[0]);
                    BufferedImage image = new BufferedImage(760, 520, BufferedImage.TYPE_INT_RGB);
                    java.awt.Graphics2D graphics = image.createGraphics();
                    graphics.setColor(UIManager.getColor("Panel.background")); graphics.fillRect(0, 0, 760, 520);
                    holder[0].printAll(graphics); graphics.dispose();
                    try { ImageIO.write(image, "png", new File(System.getProperty("quest.pilot.output"), "journal-" + scenario + ".png")); }
                    catch (java.io.IOException e) { throw new RuntimeException(e); }
                }
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final JFrame frame = new JFrame("Faumonia - lokalny podgląd dziennika (dane przykładowe)");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                final JPanel content = new JPanel(new BorderLayout());
                final JComboBox<String> scenarios = new JComboBox<String>(SCENARIOS);
                content.add(page(0), BorderLayout.CENTER);
                scenarios.addActionListener(event -> {
                    content.removeAll(); content.add(page(scenarios.getSelectedIndex()), BorderLayout.CENTER);
                    content.revalidate(); content.repaint();
                });
                frame.add(scenarios, BorderLayout.NORTH); frame.add(content, BorderLayout.CENTER);
                frame.setSize(800, 620); frame.setLocationRelativeTo(null); frame.setVisible(true);
            }
        });
    }
}
