/* SPDX-License-Identifier: GPL-2.0-or-later */
package games.stendhal.client.gui.dialogue;

import games.stendhal.client.gui.dialogue.DialoguePage.Part;
import games.stendhal.client.gui.dialogue.DialoguePage.Reply;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Offline editorial preview. No server connection, quest saves or reward actions. */
public final class DialoguePreview implements NpcDialoguePanel.Listener {
    public static final String[] SCENARIOS = {"Carmen - propozycja pomocy", "Carmen - lista i pytania",
            "Carmen - oddawanie składników", "Sedro - propozycja pomocy", "Sedro - postęp polowania",
            "Ouchit - drewno", "Ouchit - cięciwa", "Karl - wiadomość od Ouchita"};
    private static final String[] NAMES = {"Carmen", "Sedro", "Ouchit", "Karl"};
    private static final String[] DESCRIPTIONS = {
        "Widzisz przyjazną Carmen. Wygląda ona na kogoś, kogo możesz poprosić o pomoc.",
        "Widzisz starca Sedro. Zapytaj go o zadanie.",
        "Oto Ouchit. Łucznictwo jest jego pasją.", "Oto Karl miły starszy rolnik."};
    private static final String[] SPRITES = {"welcomernpc", "Sedronpc", "weaponsellernpc", "beardmannpc"};
    public final NpcDialoguePanel panel = new NpcDialoguePanel(this);
    private final Map<String, String> keywords = new LinkedHashMap<String, String>();
    private int scenario;
    private int npc;
    private String topic;
    private JDialog window;
    private JTextArea chat;
    private JLabel submitted;
    private boolean active = true;
    private String lastKeyword;

    public DialoguePreview() { start(0); }

    private List<Reply> options(String... triples) {
        keywords.clear(); List<Reply> result = new ArrayList<Reply>();
        for (int i = 0; i < triples.length; i += 3) {
            result.add(new Reply(triples[i], triples[i+1])); keywords.put(triples[i], triples[i+2]);
        }
        return result;
    }
    private List<Reply> carmenTopics() {
        return options("supplies", "Chcę przekazać składniki.", "zapasy",
                "arandula", "Gdzie rośnie arandula?", "arandula",
                "mushrooms", "Gdzie zbiorę grzyby?", "borowik",
                "apple", "Gdzie znajdę jabłka?", "jabłko",
                "wood", "Skąd wziąć polana?", "polano",
                "healing", "Potrzebuję leczenia.", "ulecz",
                "trade", "Pokaż mi swoje towary.", "oferta",
                "job", "Opowiedz mi o swojej pracy.", "praca",
                "goodbye", "Do zobaczenia, Carmen.", "bye");
    }
    private void show(String narration, List<Part> words, List<Reply> choices) {
        DialoguePage page = new DialoguePage(NAMES[npc], DESCRIPTIONS[npc],
                "data/sprites/npc/" + SPRITES[npc] + ".png", topic, narration, words, choices, active);
        panel.display(page);
        if (chat != null) { chat.append(NAMES[npc] + ": " + page.plainText() + "\n"); }
    }
    private void show(String narration, String words, List<Reply> choices) {
        show(narration, Arrays.asList(Part.text(words)), choices);
    }

    public void start(int next) {
        scenario = next; active = true; panel.resetHistory(); displayScenario();
        if (window != null) { window.setVisible(true); }
    }
    private void displayScenario() {
        if (scenario == 0) {
            npc = 0; topic = "Zioła dla Carmen";
            show("Carmen odkłada pęk ziół i zwraca się do ciebie.",
                    "Zapasy szybko znikają, a rannych w Semos nie brakuje. Pomożesz mi zebrać kilka składników?",
                    options("accept", "Przyniosę ci składniki.", "tak", "refuse", "Nie mogę teraz pomóc.", "nie"));
        } else if (scenario == 1) {
            npc = 0; topic = "Zioła dla Carmen";
            show("Carmen wylicza potrzebne składniki.", Arrays.asList(
                    Part.text("Potrzebuję: "), Part.topic("arandula", "arandula"), Part.text(" - 5, "),
                    Part.topic("borowik", "mushrooms"), Part.text(" - 1, "), Part.topic("jabłko", "apple"),
                    Part.text(" - 3, "), Part.topic("polano", "wood"), Part.text(" - 2 i "),
                    Part.topic("pieczarka", "mushrooms"), Part.text(" - 1. Nie musisz przynosić wszystkiego naraz. Gdy wrócisz, porozmawiamy o zapasach.")), carmenTopics());
        } else if (scenario == 2) {
            npc = 0; topic = "Zioła dla Carmen · oddawanie składników";
            show("Carmen robi miejsce obok swoich zapasów.",
                    "Co udało ci się przynieść? Możesz oddać część składników, a po resztę wrócić później.",
                    options("give_arandula", "Przynoszę arandulę.", "arandula",
                            "give_borowik", "Przynoszę borowiki.", "borowik", "give_apple", "Przynoszę jabłka.", "jabłko",
                            "give_wood", "Przynoszę polana.", "polano", "give_pieczarka", "Przynoszę pieczarki.", "pieczarka",
                            "nothing", "Na razie to wszystko.", "nie"));
        } else if (scenario == 3) {
            npc = 1; topic = "Pomóż Sedro";
            show("Sedro opiera dłonie na kolanach.",
                    "Szczury i gnomy dają nam się we znaki. Sam bym się nimi zajął, ale nogi nie nadążają już za dobrymi chęciami. Pomożesz mi zaprowadzić trochę spokoju?",
                    options("accept", "Pomogę ci uporać się z potworami.", "tak", "refuse", "Nie podejmę się tego teraz.", "nie"));
        } else if (scenario == 4) {
            npc = 1; topic = "Pomóż Sedro · trwające zadanie";
            show("Sedro słucha twojej relacji.",
                    "Gnom już nie będzie przeszkadzał, ale zostały jeszcze inne potwory. Szczury: 2 z 5, szczury jaskiniowe: 0 z 3, gnom: 1 z 1, panna gnom: 0 z 1. Możesz poprosić innych wędrowców o pomoc.",
                    options("progress", "Przypomnij mi, co jeszcze zostało.", "zadanie",
                            "hint", "Podpowiesz mi, gdzie szukać?", null,
                            "job", "Opowiedz mi o sobie.", "praca", "goodbye", "Wrócę, gdy uporam się z resztą.", "bye"));
        } else if (scenario == 5) {
            npc = 2; topic = "Łuki dla Ouchita · drewno";
            show("Ouchit odkłada pracę i spogląda na ciebie.",
                    "Do nowych strzał potrzebuję dziesięciu polan. Drewno znajdziesz w lesie. Masz już wszystko przy sobie?",
                    options("give_wood", "Przynoszę dziesięć polan.", "polano",
                            "progress", "Przypomnij mi, czego potrzebujesz.", "zadanie",
                            "trade", "Pokaż mi swoje łuki i strzały.", "oferta", "goodbye", "Jeszcze poszukam drewna.", "bye"));
        } else if (scenario == 6) {
            npc = 2; topic = "Łuki dla Ouchita · cięciwa";
            show("Ouchit przygląda się przygotowywanemu łukowi.", Arrays.asList(
                    Part.text("Drewno już mam. Do cięciwy potrzebuję jeszcze końskiego włosa. Odwiedź "),
                    Part.topic("Karla", "karl"), Part.text(" na jego farmie i powiedz, że przysyła cię Ouchit.")),
                    options("karl", "Gdzie znajdę Karla?", "Karl", "give_hair", "Przynoszę koński włos.", "włosie",
                            "progress", "Przypomnij mi, czego teraz potrzebujesz.", "zadanie",
                            "trade", "Pokaż mi swoje towary.", "oferta", "goodbye", "Wyruszę na farmę.", "bye"));
        } else {
            npc = 3; topic = "Wiadomość od Ouchita";
            show("Karl na chwilę przerywa pracę.", "Heja! Miło cię widzieć w naszym gospodarstwie. Co cię sprowadza?",
                    options("ouchit", "Ouchit przysłał mnie po koński włos.", "Ouchit",
                            "job", "Jak idzie praca w gospodarstwie?", "praca",
                            "trade", "Co można u was kupić?", "oferta", "goodbye", "Nie będę przeszkadzać. Do widzenia.", "bye"));
        }
    }

    @Override public void replyChosen(String id) {
        if (!active || !keywords.containsKey(id)) { return; }
        lastKeyword = keywords.get(id);
        if (submitted != null) {
            submitted.setText(lastKeyword == null ? "Opcja podglądu - wymaga osobnego podłączenia." : "Odpowiadające hasło czatu: „" + lastKeyword + "” (symulacja).");
        }
        if ("goodbye".equals(id)) { depart(); return; }
        if ("accept".equals(id)) { scenario = npc == 0 ? 1 : 4; displayScenario(); return; }
        if ("refuse".equals(id)) {
            show("", "Rozumiem. Jeśli zmienisz zdanie, wróć do mnie.", options("again", "Porozmawiajmy jeszcze o zadaniu.", "zadanie", "goodbye", "Do widzenia.", "bye")); return;
        }
        if ("again".equals(id)) { scenario = npc == 0 ? 0 : 3; displayScenario(); return; }
        if ("supplies".equals(id)) { scenario = 2; displayScenario(); return; }
        if ("nothing".equals(id)) { scenario = 1; displayScenario(); return; }
        if (id.startsWith("give_") && npc == 0) {
            // Example data only; does not take an item from a real character.
            show("Carmen odkłada przyniesione składniki.", "Dziękuję. Masz jeszcze coś z listy?",
                    options("more", "Chcę oddać kolejny składnik.", "tak", "nothing", "Na razie to wszystko.", "nie")); return;
        }
        if ("more".equals(id)) { displayScenario(); return; }
        if ("arandula".equals(id)) {
            show("Carmen zastanawia się przez chwilę.", "Aranduli szukaj na północ od Semos. Rośnie tam poza zabudowaniami.",
                    options("hint_detail", "Podpowiesz dokładniej?", null, "back", "Wróćmy do pozostałych składników.", null)); return;
        }
        if ("hint_detail".equals(id)) {
            show("", "Rozglądaj się niedaleko wilków na północ od miasta. Zachowaj ostrożność podczas zbierania.",
                    options("back", "Wróćmy do zadania.", null)); return;
        }
        if ("mushrooms".equals(id) || "apple".equals(id) || "wood".equals(id)) {
            String words = "mushrooms".equals(id) ? "Szukaj grzybów na polanach przy lesie na południe od miasta."
                    : "apple".equals(id) ? "Jabłonie rosną na wschód od Semos. Jabłka bywają też w tutejszej tawernie."
                    : "Rozglądaj się za drewnem w lesie. Potrzebuję dwóch polan.";
            show("", words, options("back", "Wróćmy do listy składników.", null)); return;
        }
        if ("back".equals(id)) { if (npc == 0) { scenario = 1; } displayScenario(); return; }
        if ("hint".equals(id)) {
            show("", "Szczurów szukaj w mieście i podziemiach. Gnomy spotkasz w jaskini pod domkiem startowym oraz w ich wiosce na północny wschód od Semos.",
                    options("back", "Dziękuję za wskazówki.", null, "goodbye", "Ruszam na poszukiwania.", "bye")); return;
        }
        if ("give_wood".equals(id) && npc == 2) { scenario = 6; displayScenario(); return; }
        if ("karl".equals(id)) {
            show("", "Karl jest rolnikiem. Jego farmę znajdziesz na wschód od Semos. Powiedz mu moje imię.",
                    options("back", "Dobrze, wiem już, kogo szukać.", null, "goodbye", "Pójdę do Karla.", "bye")); return;
        }
        if ("ouchit".equals(id)) {
            show("Karl wyciąga koński włos.", "Ouchit znów potrzebuje włosia do łuku? Proszę, weź. I przekaż mu ode mnie pozdrowienia!",
                    options("goodbye", "Dziękuję, przekażę mu pozdrowienia.", "bye")); return;
        }
        if ("give_hair".equals(id)) {
            show("", "Wspaniale, właśnie tego potrzebowałem. Dziękuję za pomoc - przyjmij mój stary łuk i zapas strzał.",
                    options("goodbye", "Dziękuję. Do zobaczenia!", "bye")); return;
        }
        if ("progress".equals(id)) { displayScenario(); return; }
        if ("job".equals(id)) {
            show("", npc == 0 ? "Pomagam rannym i przygotowuję lecznicze mikstury."
                    : npc == 1 ? "Jestem samotnikiem. Dobrze czasem zamienić kilka słów z wędrowcem."
                    : npc == 2 ? "Robię i sprzedaję łuki oraz strzały." : "W gospodarstwie zawsze jest coś do zrobienia.",
                    options("back", "Wróćmy do naszej rozmowy.", null, "goodbye", "Do zobaczenia.", "bye")); return;
        }
        // Trading/healing remain separate existing services; preview does not perform either.
        show("", "healing".equals(id) ? "Pokaż, gdzie cię boli."
                : npc == 3 ? "Ja sprzedaję puste worki. O mleko zapytaj moją żonę Philomenę."
                : npc == 0 ? "Mam lecznicze mikstury i antidotum." : "Sprzedaję łuki i strzały.",
                options("back", "Wróćmy do rozmowy.", null, "goodbye", "Do widzenia.", "bye"));
    }

    @Override public void textSubmitted(String text) {
        if (!active) { return; }
        for (Map.Entry<String, String> entry : new ArrayList<Map.Entry<String, String>>(keywords.entrySet())) {
            if (entry.getValue() != null && entry.getValue().equalsIgnoreCase(text.trim())) {
                replyChosen(entry.getKey()); return;
            }
        }
        displayScenario();
        if (submitted != null) { submitted.setText("Podgląd obsługuje przykładowe hasła; pełny parser zostaje do podłączenia."); }
    }
    @Override public void hideRequested() { if (window != null) { window.setVisible(false); } }
    public void depart() {
        active = false;
        show("", npc == 3 ? "Do widzenia, do widzenia. Bądź ostrożny." : "Do zobaczenia. Dziękuję za rozmowę.", Collections.<Reply>emptyList());
    }
    public String getLastKeyword() { return lastKeyword; }
    public boolean isActive() { return active; }

    private void open() {
        JFrame frame = new JFrame("Faumonia - podgląd rozmów, dane przykładowe");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); frame.setSize(1024, 850); frame.setLocationRelativeTo(null);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> cases = new JComboBox<String>(SCENARIOS);
        JButton reopen = new JButton("Pokaż rozmowę"); JButton leave = new JButton("Odejdź od NPC");
        controls.add(cases); controls.add(reopen); controls.add(leave); frame.add(controls, BorderLayout.NORTH);
        JPanel space = new JPanel(); space.setBackground(new Color(53, 61, 49)); frame.add(space, BorderLayout.CENTER);
        JPanel chatPanel = new JPanel(new BorderLayout(4, 4)); chatPanel.setBorder(BorderFactory.createTitledBorder("Czat podglądu - można pisać także po ukryciu rozmowy"));
        chat = new JTextArea(4, 40); chat.setEditable(false); chat.setLineWrap(true); chat.setWrapStyleWord(true);
        chatPanel.add(new JScrollPane(chat), BorderLayout.CENTER);
        JTextField chatInput = new JTextField(); chatInput.addActionListener(event -> {
            if (active && !chatInput.getText().trim().isEmpty()) {
                chat.append("Ty: " + chatInput.getText() + "\n"); textSubmitted(chatInput.getText()); chatInput.setText("");
            }
        });
        JPanel chatBottom = new JPanel(new BorderLayout()); chatBottom.add(chatInput, BorderLayout.CENTER);
        submitted = new JLabel("Podgląd nie łączy się z serwerem i nie zmienia postaci."); chatBottom.add(submitted, BorderLayout.SOUTH);
        chatPanel.add(chatBottom, BorderLayout.SOUTH); frame.add(chatPanel, BorderLayout.SOUTH);
        window = new JDialog(frame, "Rozmowa", false); window.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        window.setContentPane(panel); window.pack(); window.setMinimumSize(new Dimension(540, 500));
        cases.addActionListener(event -> start(cases.getSelectedIndex()));
        reopen.addActionListener(event -> window.setVisible(true)); leave.addActionListener(event -> depart());
        frame.setVisible(true);
        Point anchor = chatPanel.getLocationOnScreen();
        window.setLocation(frame.getX() + (frame.getWidth()-window.getWidth())/2,
                Math.max(frame.getY()+65, anchor.y-window.getHeight()-8));
        window.setVisible(true);
    }

    public static void renderExamples() throws Exception {
        final DialoguePreview[] preview = new DialoguePreview[1];
        SwingUtilities.invokeAndWait(() -> preview[0] = new DialoguePreview());
        for (int i = 0; i < SCENARIOS.length + 3; i++) {
            final int selected = i;
            final int width = selected == 10 ? 540 : 690;
            SwingUtilities.invokeAndWait(() -> {
                preview[0].start(selected < SCENARIOS.length ? selected : selected == 8 ? 0 : 1);
                if (selected == SCENARIOS.length) {
                    clickReply(preview[0].panel, "accept"); preview[0].panel.setHistoryVisible(true);
                }
                preview[0].panel.setSize(width, 600); layout(preview[0].panel);
            });
            SwingUtilities.invokeAndWait(() -> {
                layout(preview[0].panel);
                if (selected == 9) { scrollAnswersToEnd(preview[0].panel); }
                BufferedImage image = new BufferedImage(width, 600, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = image.createGraphics(); preview[0].panel.printAll(graphics); graphics.dispose();
                try { ImageIO.write(image, "png", new File(System.getProperty("quest.pilot.output"), "dialogue-" + selected + ".png")); }
                catch (IOException e) { throw new RuntimeException(e); }
            });
        }
    }
    private static void scrollAnswersToEnd(Container component) {
        for (Component child : component.getComponents()) {
            if (child instanceof JScrollPane && ((JScrollPane) child).getViewport().getView() instanceof JPanel) {
                JScrollBar bar = ((JScrollPane) child).getVerticalScrollBar(); bar.setValue(bar.getMaximum());
            } else if (child instanceof Container) { scrollAnswersToEnd((Container) child); }
        }
    }
    private static void layout(Container component) {
        component.doLayout();
        for (Component child : component.getComponents()) { if (child instanceof Container) { layout((Container) child); } }
    }
    static boolean clickReply(Container component, String id) {
        for (Component child : component.getComponents()) {
            if (child instanceof JButton && id.equals(((JButton) child).getActionCommand())) {
                ((JButton) child).doClick(0); return true;
            }
            if (child instanceof Container && clickReply((Container) child, id)) { return true; }
        }
        return false;
    }
    public static void main(String[] args) throws Exception {
        if (args.length == 1 && "--render".equals(args[0])) { renderExamples(); }
        else { SwingUtilities.invokeLater(() -> new DialoguePreview().open()); }
    }
}
