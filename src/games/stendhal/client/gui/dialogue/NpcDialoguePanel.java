/* SPDX-License-Identifier: GPL-2.0-or-later */
package games.stendhal.client.gui.dialogue;

import games.stendhal.client.gui.dialogue.DialoguePage.Part;
import games.stendhal.client.gui.dialogue.DialoguePage.Reply;
import games.stendhal.client.gui.progress.QuestProgressFormatter;
import games.stendhal.client.gui.styled.StyleUtil;
import games.stendhal.client.gui.styled.WoodStyle;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import games.stendhal.common.NpcDialogueSnapshot.Item;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicPanelUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import javax.swing.text.DefaultCaret;

/** Swing view: all effects and availability are controlled outside the panel. */
public final class NpcDialoguePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Color INK = new Color(212, 215, 218);
    private static final Color MUTED = new Color(174, 181, 187);
    private static final Color LINE = new Color(88, 98, 107);
    // Muted blue remains recognizable on the matte reading surface.
    private static final Color ACCENT = new Color(130, 174, 214);

    public interface Listener {
        void replyChosen(String id);
        void textSubmitted(String text);
        /** End the active conversation with the NPC's normal farewell. */
        void hideRequested();
        default void purchaseRequested(String id, int amount) { }
    }

    private final Listener listener;
    private final BooleanSupplier shopEnabled;
    private final JLabel name = new JLabel();
    private final JEditorPane description = htmlPane(12);
    private final JLabel topic = new JLabel();
    private final Portrait portrait = new Portrait();
    private final JEditorPane speech = htmlPane(14);
    private final JLabel lastReply = new JLabel();
    private final JScrollPane speechScroll = scroll(speech);
    private final JPanel ingredients = transparent(new GridLayout(1, 0, 6, 0));
    private final JScrollPane ingredientsScroll = scroll(ingredients);
    private final JPanel replies = column();
    private final JScrollPane repliesScroll = scroll(replies);
    private final JPanel catalog = column();
    private final JScrollPane catalogScroll = scroll(catalog);
    private final JPanel replyCards = transparent(new CardLayout());
    private final JLabel answerLabel = new JLabel("TWOJA ODPOWIEDŹ");
    private final JButton shopToggle = new JButton();
    private final JLabel wallet = new JLabel();
    private final JButton moreReplies = new JButton();
    private boolean repliesExpanded;
    private boolean viewingShop;
    private boolean compact;
    private final JPanel historyContainer = transparent(new BorderLayout());
    private final JEditorPane history = htmlPane(13);
    private final JToggleButton historyToggle = new JToggleButton("Historia rozmowy");
    private final JTextField input = new JTextField();
    private final JButton send = new JButton("Powiedz");
    private final JTextArea status = new JTextArea(2, 0);
    private final List<String> transcript = new ArrayList<String>();
    private DialoguePage page;
    private boolean waiting;
    private String feedback = "";
    private String pendingEcho;
    private final JButton hide = new JButton("×");
    private final JPanel rewardsGrid = transparent(new GridLayout(0, 3, 8, 4));
    private final JScrollPane rewardsScroll = scroll(rewardsGrid);
    private final JPanel rewardsSection = transparent(new BorderLayout(0, 5));

    public NpcDialoguePanel(Listener listener) {
        this(listener, () -> Boolean.parseBoolean(WtWindowManager.getInstance().getProperty(
                NpcDialogueController.SHOP_ENABLED_PROPERTY, "true")));
    }

    NpcDialoguePanel(Listener listener, BooleanSupplier shopEnabled) {
        this.listener = listener;
        this.shopEnabled = shopEnabled;
        setUI(new BasicPanelUI());
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createCompoundBorder(WoodStyle.getInstance().getBorder(),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        setMinimumSize(new Dimension(520, 380));
        setOpaque(false);

        JPanel header = transparent(new BorderLayout(16, 0));
        header.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, LINE),
                BorderFactory.createEmptyBorder(0, 0, 12, 0)));
        portrait.setPreferredSize(new Dimension(64, 76)); header.add(portrait, BorderLayout.WEST);
        JPanel identity = transparent(new BorderLayout(0, 5));
        JPanel heading = transparent(new BorderLayout());
        name.setFont(new Font("Dialog", Font.BOLD, 22)); name.setForeground(INK);
        heading.add(name, BorderLayout.CENTER);
        hide.setUI(new BasicButtonUI());
        hide.setFont(new Font("Dialog", Font.PLAIN, 22)); hide.setForeground(MUTED);
        hide.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 2)); hide.setContentAreaFilled(false);
        hide.setToolTipText("Pożegnaj się i zamknij rozmowę (Esc)"); hide.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        hide.addActionListener(event -> listener.hideRequested()); heading.add(hide, BorderLayout.EAST);
        identity.add(heading, BorderLayout.NORTH); identity.add(description, BorderLayout.CENTER);
        description.setPreferredSize(new Dimension(0, 46));
        topic.setFont(new Font("Dialog", Font.BOLD, 12)); topic.setForeground(MUTED);
        topic.setVisible(false); header.add(identity, BorderLayout.CENTER); add(header, BorderLayout.NORTH);

        JPanel center = transparent(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 0; c.fill = GridBagConstraints.BOTH;
        lastReply.setName("last-player-reply");
        lastReply.setFont(new Font("Dialog", Font.PLAIN, 12)); lastReply.setForeground(MUTED);
        lastReply.setMinimumSize(new Dimension(0, lastReply.getFontMetrics(lastReply.getFont()).getHeight()));
        lastReply.setVisible(false); c.insets = new Insets(0, 2, 7, 0); center.add(lastReply, c);
        c.gridy++; c.weighty = 0; c.insets = new Insets(0, 0, 0, 0);
        speechScroll.setMinimumSize(new Dimension(0, 100)); center.add(speechScroll, c);
        ingredientsScroll.setName("dialogue-ingredients-scroll");
        ingredientsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        ingredientsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        ingredientsScroll.getHorizontalScrollBar().setUnitIncrement(126);
        ingredientsScroll.setVisible(false);
        c.gridy++; c.insets = new Insets(5, 0, 0, 0); center.add(ingredientsScroll, c);
        JLabel rewardHeading = new JLabel("OTRZYMUJESZ", new DialogueItemIcon("gui/dialogue/complete", 20), SwingConstants.LEFT);
        rewardHeading.setFont(new Font("Dialog", Font.BOLD, 11)); rewardHeading.setForeground(INK);
        rewardsSection.setName("dialogue-rewards"); rewardsSection.setVisible(false);
        rewardsSection.add(rewardHeading, BorderLayout.NORTH); rewardsSection.add(rewardsScroll, BorderLayout.CENTER);
        c.gridy++; center.add(rewardsSection, c);
        answerLabel.setForeground(MUTED); answerLabel.setFont(new Font("Dialog", Font.BOLD, 11));
        JPanel answerHeading = transparent(new BorderLayout());
        answerHeading.add(answerLabel, BorderLayout.WEST);
        styleButton(shopToggle); shopToggle.setFont(new Font("Dialog", Font.PLAIN, 11));
        shopToggle.setVisible(false);
        shopToggle.addActionListener(event -> { viewingShop = !viewingShop; showReplyCard(); });
        answerHeading.add(shopToggle, BorderLayout.EAST);
        c.gridy++; c.weighty = 0; c.insets = new Insets(10, 2, 6, 0); center.add(answerHeading, c);
        c.gridy++; c.weighty = 1; c.insets = new Insets(0, 0, 0, 0);
        replyCards.add(repliesScroll, "replies"); replyCards.add(catalogScroll, "shop");
        repliesScroll.setName("dialogue-replies-scroll");
        replyCards.setMinimumSize(new Dimension(0, 90)); replyCards.setPreferredSize(new Dimension(0, 90)); center.add(replyCards, c);
        styleButton(moreReplies); moreReplies.setName("dialogue-more-replies"); moreReplies.setActionCommand("more_replies");
        moreReplies.setFont(new Font("Dialog", Font.PLAIN, 12)); moreReplies.setVisible(false);
        moreReplies.addActionListener(event -> {
            repliesExpanded = !repliesExpanded; updateReplyVisibility(); notifyContentSize();
        });
        c.gridy++; c.weighty = 0; c.insets = new Insets(4, 0, 0, 0); center.add(moreReplies, c);
        wallet.setName("shop-wallet"); wallet.setFont(new Font("Dialog", Font.PLAIN, 13));
        wallet.setForeground(INK); wallet.setIcon(new DialogueItemIcon("items/money/gold", 20));
        wallet.setIconTextGap(8); wallet.setVisible(false);
        c.gridy++; c.weighty = 0; c.insets = new Insets(7, 5, 0, 0); center.add(wallet, c);
        add(center, BorderLayout.CENTER);

        JPanel footer = transparent(new BorderLayout(0, 8));
        historyToggle.setUI(new BasicToggleButtonUI());
        historyToggle.setFont(new Font("Dialog", Font.PLAIN, 12)); historyToggle.setForeground(ACCENT);
        historyToggle.setContentAreaFilled(false); historyToggle.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        historyToggle.setHorizontalAlignment(SwingConstants.LEFT);
        historyToggle.addActionListener(event -> setHistoryVisible(historyToggle.isSelected()));
        JPanel historySection = transparent(new BorderLayout(0, 4));
        historySection.add(historyToggle, BorderLayout.NORTH);
        historyContainer.setOpaque(false); historyContainer.setPreferredSize(new Dimension(0, 70));
        historyContainer.add(scroll(history), BorderLayout.CENTER); historyContainer.setVisible(false);
        historySection.add(historyContainer, BorderLayout.CENTER); footer.add(historySection, BorderLayout.NORTH);

        JPanel typing = transparent(new BorderLayout(8, 5));
        JLabel typingLabel = new JLabel("Możesz też napisać własną odpowiedź:");
        typingLabel.setForeground(MUTED); typingLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        typing.add(typingLabel, BorderLayout.NORTH);
        input.setFont(new Font("Dialog", Font.PLAIN, 14)); input.setBackground(new Color(35, 38, 41));
        input.setForeground(INK); input.setCaretColor(INK);
        input.setDisabledTextColor(MUTED); input.setSelectionColor(new Color(82, 100, 116));
        input.setSelectedTextColor(Color.WHITE);
        input.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
        input.getAccessibleContext().setAccessibleName("Własna odpowiedź do NPC");
        input.addActionListener(event -> submitText()); typing.add(input, BorderLayout.CENTER);
        styleButton(send); send.addActionListener(event -> submitText()); typing.add(send, BorderLayout.EAST);
        status.setForeground(MUTED); status.setFont(new Font("Dialog", Font.PLAIN, 11));
        status.setEditable(false); status.setFocusable(false); status.setOpaque(false);
        status.setLineWrap(true); status.setWrapStyleWord(true);
        typing.add(status, BorderLayout.SOUTH); footer.add(typing, BorderLayout.CENTER); add(footer, BorderLayout.SOUTH);

        speech.addHyperlinkListener(event -> {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String target = event.getDescription();
                if (target != null && target.startsWith("reply:")) { choose(target.substring(6)); }
            }
        });
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"), "hide-dialogue");
        getActionMap().put("hide-dialogue", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent event) { listener.hideRequested(); }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent event) { adaptLayout(); }
        });
    }

    public void display(DialoguePage next) {
        display(next, true);
    }

    /** A feedback-only response keeps the last spoken line without repeating it in history. */
    public void display(DialoguePage next, boolean appendSpeech) {
        boolean changedNPC = page == null || !page.npcName.equals(next.npcName);
        boolean wasShop = page != null && page.shop;
        boolean wasConfirmation = page != null && isConfirmation(page);
        page = next; waiting = false; feedback = "";
        repliesExpanded = false;
        boolean shopAvailable = next.shop && shopEnabled.getAsBoolean() && next.active;
        wallet.setVisible(shopAvailable && next.money >= 0);
        wallet.setText(next.money >= 0 ? "Twoje monety: " + next.money : "");
        wallet.setToolTipText("Monety, które masz przy sobie i możesz wydać w sklepie.");
        if (!shopAvailable || isConfirmation(next)) { viewingShop = false; }
        else if (!wasShop || wasConfirmation || changedNPC) { viewingShop = true; }
        shopToggle.setVisible(shopAvailable && !isConfirmation(next));
        if (changedNPC) { transcript.clear(); input.setText(""); pendingEcho = null; setLastReply(""); setHistoryVisible(false); }
        name.setText(next.npcName); topic.setText(next.topic);
        description.setText(html(12, "<p>" + escape(next.description) + "</p>"));
        description.setToolTipText(next.description);
        description.setCaretPosition(0); portrait.load(next.sprite, next.npcName);
        StringBuilder words = new StringBuilder();
        if (!next.narration.isEmpty()) {
            words.append("<p style='color:#aeb5bb; font-size:12px; margin-bottom:10px'><i>")
                    .append(escape(next.narration)).append("</i></p>");
        }
        words.append("<p>");
        for (Part part : next.words) {
            if (part.replyId != null && next.active) {
                words.append("<a href='reply:").append(part.replyId).append("' title='Kliknij, aby zapytać'>")
                        .append(escape(part.text)).append("</a>");
            } else { words.append(escape(part.text)); }
        }
        words.append("</p>"); buildIngredientList(next.items); buildRewards(next.rewards);
        speech.setText(html(14, words.toString())); speech.setCaretPosition(0);
        sizeSpeechToContent();
        replies.removeAll();
        for (Reply reply : next.replies) {
            JButton button = new JButton("<html>" + escape(reply.label) + "</html>");
            styleButton(button); button.setActionCommand(reply.id);
            for (Item item : next.items) {
                if (!item.isForSale() && reply.id.equals(item.getReplyId())) {
                    button.setIcon(new DialogueItemIcon(item.getSprite(), 28)); button.setIconTextGap(9);
                    break;
                }
            }
            if (button.getIcon() == null) {
                String sprite = topicSprite(reply.id);
                button.setIcon(new DialogueItemIcon(sprite, 28)); button.setIconTextGap(9);
            }
            boolean carriedIngredient = false;
            for (Item item : next.items) { carriedIngredient |= !item.isForSale() && item.getOwned() > 0; }
            if (replies.getComponentCount() == 0 && (!"supplies".equals(reply.id) || carriedIngredient)
                    && (java.util.Arrays.asList("quest", "ingredients", "accept", "supplies", "wood", "hair").contains(reply.id)
                    || reply.id.startsWith("give_"))) {
                button.putClientProperty("dialogue.recommended", Boolean.TRUE);
                button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(88, 109, 126)),
                        BorderFactory.createEmptyBorder(4, 9, 4, 9)));
            }
            button.setHorizontalAlignment(SwingConstants.LEFT); button.setAlignmentX(Component.LEFT_ALIGNMENT);
            int height = 42;
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, height)); button.setPreferredSize(new Dimension(350, height));
            button.addActionListener(event -> choose(reply.id)); replies.add(button);
            replies.add(Box.createVerticalStrut(4));
        }
        replies.add(Box.createVerticalGlue());
        buildCatalog(next.items); showReplyCard();
        if (appendSpeech && !next.plainText().isEmpty()) { appendHistory(next.npcName, next.plainText()); }
        if (appendSpeech && !next.rewards.isEmpty()) {
            List<String> receipt = new ArrayList<String>();
            for (games.stendhal.common.NpcDialogueSnapshot.Reward reward : next.rewards) { receipt.add(reward.describe()); }
            appendHistory("Otrzymujesz", String.join(", ", receipt));
        }
        updateEnabled();
        revalidate(); repaint();
        notifyContentSize();
        SwingUtilities.invokeLater(() -> {
            adaptLayout();
            speechScroll.getVerticalScrollBar().setValue(0);
            repliesScroll.getVerticalScrollBar().setValue(0);
            catalogScroll.getVerticalScrollBar().setValue(0);
            if (changedNPC) { ingredientsScroll.getHorizontalScrollBar().setValue(0); }
        });
    }

    /** Explicitly start a fresh encounter, including another encounter with the same NPC. */
    public void resetHistory() {
        transcript.clear(); input.setText(""); pendingEcho = null; feedback = "";
        setLastReply("");
        renderHistory(); setHistoryVisible(false);
        viewingShop = false; page = null; shopToggle.setVisible(false);
    }

    /** Includes ordinary chat replies; avoids duplicating a click/text already shown locally. */
    public void recordPlayerReply(String text) {
        if (text == null || text.isEmpty()) { return; }
        setLastReply(text);
        if (!text.equals(pendingEcho)) { appendHistory("Ty", text); }
        pendingEcho = null;
    }

    /** Release input after a server response or a transport failure; never resend automatically. */
    public void showFeedback(String message) {
        waiting = false; feedback = message == null ? "" : message; updateEnabled();
    }

    /** A disconnect is interface feedback, not an invented NPC farewell. */
    public void markEnded(String message) {
        if (page != null) {
            List<Part> words = new ArrayList<Part>();
            for (Part part : page.words) { words.add(Part.text(part.text)); }
            display(new DialoguePage(page.npcName, page.description, page.sprite, page.topic,
                    page.narration, words, java.util.Collections.<Reply>emptyList(), false), false);
        }
        showFeedback(message);
    }

    public void setHistoryVisible(boolean visible) {
        historyToggle.setSelected(visible); historyContainer.setVisible(visible);
        historyToggle.setText(visible ? "Zwiń historię rozmowy" : "Historia rozmowy"); adaptLayout(); revalidate(); notifyContentSize();
    }

    private void choose(String id) {
        if (page == null || !page.active || waiting) { return; }
        for (Reply reply : page.replies) {
            if (reply.id.equals(id)) {
                appendHistory("Ty", reply.label); pendingEcho = reply.label;
                setLastReply(reply.label);
                feedback = ""; waiting = true; updateEnabled(); listener.replyChosen(id); return;
            }
        }
    }

    private void submitText() {
        if (page == null || !page.active || waiting) { return; }
        String text = input.getText().trim();
        if (text.isEmpty()) { return; }
        if (text.length() > games.stendhal.common.NpcDialogueSnapshot.MAX_INPUT) {
            showFeedback("Odpowiedź jest za długa. Napisz najwyżej 500 znaków."); return;
        }
        input.setText(""); appendHistory("Ty", text); pendingEcho = text;
        setLastReply(text);
        feedback = ""; waiting = true; updateEnabled(); listener.textSubmitted(text);
    }

    private void updateEnabled() {
        boolean enabled = page != null && page.active && !waiting;
        for (Component component : replies.getComponents()) { component.setEnabled(enabled); }
        enableTree(catalog, enabled);
        input.setEnabled(enabled); send.setEnabled(enabled);
        status.setForeground(feedback.isEmpty() ? MUTED : ACCENT);
        status.setText(!feedback.isEmpty() ? feedback : page != null && !page.active ? "Rozmowa dobiegła końca. Możesz przejrzeć jej historię."
                : waiting ? "Czekasz na odpowiedź…" : "Kliknij niebieskie słowo, aby zapytać.");
    }

    private static boolean isConfirmation(DialoguePage page) {
        for (Reply reply : page.replies) { if ("confirm".equals(reply.id)) { return true; } }
        return false;
    }

    private void sizeSpeechToContent() {
        Insets insets = getInsets();
        int width = Math.max(240, (getWidth() > 0 ? getWidth() : getPreferredSize().width) - insets.left - insets.right - 14);
        speech.setSize(width, Short.MAX_VALUE);
        javax.swing.text.View view = speech.getUI().getRootView(speech);
        view.setSize(width, Short.MAX_VALUE);
        int height = Math.min(compact ? 72 : 156, Math.max(34, (int) Math.ceil(view.getPreferredSpan(javax.swing.text.View.Y_AXIS)) + 4));
        speechScroll.setPreferredSize(new Dimension(0, height));
        speechScroll.setMinimumSize(new Dimension(0, Math.min(compact ? 44 : 80, height)));
        revalidate();
    }

    private void adaptLayout() {
        boolean nextCompact = getHeight() > 0 && (getHeight() < 480
                || (historyContainer.isVisible() && getHeight() < 650));
        if (compact != nextCompact) {
            compact = nextCompact;
            historyContainer.setPreferredSize(new Dimension(0, compact ? 40 : 70));
            replyCards.setMinimumSize(new Dimension(0, compact ? 46 : 90));
            if (page != null) { buildIngredientList(page.items); }
        }
        sizeSpeechToContent();
        sizeRewards();
    }

    private String topicSprite(String id) {
        if (id.startsWith("xxv_")) { return "gui/dialogue/quest"; }
        if ("instructions".equals(id)) { return "gui/dialogue/hint"; }
        if ("cooldown".equals(id)) { return "gui/dialogue/hint"; }
        if ("food".equals(id)) { return "items/food/sandwich"; }
        if ("offer".equals(id)) { return "items/money/gold"; }
        if ("heal".equals(id)) { return "gui/dialogue/health"; }
        if ("arandula".equals(id)) { return "items/herb/arandula"; }
        if ("mushrooms".equals(id)) { return "items/food/porcini"; }
        if ("champignons".equals(id)) { return "items/food/button_mushroom"; }
        if ("apples".equals(id)) { return "items/food/apple"; }
        if ("wood".equals(id) || "wood_hint".equals(id)) { return "items/resource/wood"; }
        if ("hair".equals(id) || "ouchit".equals(id)) { return "items/resource/horsehair"; }
        if ("supplies".equals(id)) { return "gui/dialogue/handover"; }
        if ("quest".equals(id) || "ingredients".equals(id)) { return "gui/dialogue/quest"; }
        if ("hints".equals(id) || "karl".equals(id)) { return "gui/dialogue/hint"; }
        if ("journal".equals(id)) { return "items/book/book_blue"; }
        if ("rats".equals(id)) { return "monsters/small_animal/rat"; }
        if ("gnomes".equals(id)) { return "monsters/gnome/gnome"; }
        if ("job".equals(id) || "introduce".equals(id)) {
            return page != null && page.sprite.startsWith("data/sprites/npc/")
                    ? page.sprite.substring("data/sprites/".length()).replace(".png", "") : "gui/dialogue/conversation";
        }
        if ("goodbye".equals(id) || "later".equals(id)) { return "gui/dialogue/farewell"; }
        if ("cancel".equals(id) || "decline".equals(id)) { return "gui/dialogue/decline"; }
        if ("accept".equals(id) || "confirm".equals(id) || "know".equals(id) || "interest".equals(id)) { return "gui/dialogue/confirm"; }
        if ("help".equals(id) || "guide".equals(id) || "neighborhood".equals(id) || "philomena".equals(id)) { return "gui/dialogue/hint"; }
        if ("back".equals(id)) { return "gui/dialogue/back"; }
        if ("greeting".equals(id)) { return "gui/dialogue/greeting"; }
        return "gui/dialogue/conversation";
    }

    private void showReplyCard() {
        ((CardLayout) replyCards.getLayout()).show(replyCards, viewingShop ? "shop" : "replies");
        answerLabel.setText(viewingShop ? "OFERTA" : "TWOJA ODPOWIEDŹ");
        shopToggle.setText(viewingShop ? "Pokaż odpowiedzi" : "Sklep");
        updateReplyVisibility(); notifyContentSize();
    }

    private void updateReplyVisibility() {
        int index = 0;
        for (Component component : replies.getComponents()) {
            if (component instanceof JButton) { component.setVisible(repliesExpanded || index++ < 5); }
            else { component.setVisible(repliesExpanded || index <= 5); }
        }
        int count = page == null ? 0 : page.replies.size();
        moreReplies.setVisible(!viewingShop && count > 5);
        moreReplies.setText(repliesExpanded ? "Zwiń odpowiedzi" : "Więcej odpowiedzi (" + (count - 5) + ")");
        int shopCount = 0;
        if (page != null) { for (Item item : page.items) { if (item.isForSale()) { shopCount++; } } }
        int rows = viewingShop ? Math.min(5, shopCount) : Math.min(repliesExpanded ? 8 : 5, count);
        int height = Math.max(46, rows * (viewingShop ? 54 : 46));
        replyCards.setPreferredSize(new Dimension(0, height));
        revalidate(); repaint();
    }

    private void notifyContentSize() { firePropertyChange("dialogue.contentSize", false, true); }

    @Override public Dimension getPreferredSize() {
        return new Dimension(690, Math.max(400, super.getPreferredSize().height));
    }

    private void buildRewards(List<games.stendhal.common.NpcDialogueSnapshot.Reward> rewards) {
        rewardsGrid.removeAll();
        for (games.stendhal.common.NpcDialogueSnapshot.Reward reward : rewards) {
            JLabel label = new JLabel("<html>" + escape(reward.getName()) + "<br><b>"
                    + ("gui/dialogue/unlock".equals(reward.getSprite()) ? "Odblokowano" : "+" + reward.formatAmount())
                    + "</b>" + (reward.isOnGround() ? " (na ziemi)" : "") + "</html>",
                    new DialogueItemIcon(reward.getSprite(), 28), SwingConstants.LEFT);
            label.setName("reward-" + reward.getName());
            label.setForeground(INK); label.setFont(new Font("Dialog", Font.PLAIN, 11)); label.setIconTextGap(5);
            label.setToolTipText(reward.describe()); rewardsGrid.add(label);
        }
        rewardsSection.setVisible(!rewards.isEmpty()); sizeRewards();
    }

    private void sizeRewards() {
        int columns = getWidth() > 0 && getWidth() < 600 ? 2 : 3;
        ((GridLayout) rewardsGrid.getLayout()).setColumns(columns);
        int rows = (rewardsGrid.getComponentCount() + columns - 1) / columns;
        rewardsGrid.setPreferredSize(new Dimension(0, rows * 42));
        rewardsScroll.setPreferredSize(new Dimension(0, Math.min(compact ? 100 : 170, rows * 42)));
        rewardsScroll.setMinimumSize(new Dimension(0, Math.min(80, rows * 42)));
    }

    private void buildIngredientList(List<Item> items) {
        ingredients.removeAll();
        for (Item item : items) {
            boolean examFish = "exam_fish".equals(item.getId());
            boolean huntMember=item.getId().startsWith("hunt_member_");
            boolean huntTotal="hunt_total".equals(item.getId());
            boolean expeditionGoal=item.getId().startsWith("survey_")||item.getId().startsWith("prepare_");
            if (item.isForSale() || (item.getRequired() == 0 && !examFish && !huntMember)) { continue; }
            boolean creature = item.getSprite().startsWith("monsters/") || "sheep".equals(item.getSprite()) || "baby_dragon".equals(item.getSprite());
            String countLabel = huntTotal ? "Pokonano: " : item.getId().startsWith("journey_visit_") ? "Spotkano: "
                    : item.getId().startsWith("journey_news_") ? "Przekazano: " : item.getId().startsWith("survey_") ? "Zbadano: " : item.getId().startsWith("prepare_") ? "Ukończono: " : creature ? "" : "Masz: ";
            JLabel label = new JLabel("<html><div style='width:108px;text-align:center'>" + escape(item.getName()) + "<br>"
                    + (examFish ? "" : huntMember ? "Wlicza się do sumy" : countLabel + item.getOwned() + " / " + item.getRequired()) + "</div></html>", new DialogueItemIcon(item.getSprite(), 24), SwingConstants.CENTER);
            label.setFont(new Font("Dialog", Font.PLAIN, 11)); label.setForeground(MUTED);
            label.setVerticalTextPosition(SwingConstants.BOTTOM); label.setHorizontalTextPosition(SwingConstants.CENTER);
            label.setIconTextGap(3);
            Dimension content = label.getPreferredSize();
            Dimension cell = new Dimension(Math.max(120, content.width + 8), content.height + 6);
            label.setPreferredSize(cell); label.setMinimumSize(cell);
            label.setToolTipText(huntMember ? item.getName()+": zalicza się do wspólnego celu wyprawy." : huntTotal ? "Łączny wynik wszystkich przeciwników z wykazu: "+item.getOwned()+" / "+item.getRequired() : examFish ? "Rozpoznaj rybę i wpisz nazwę gatunku." : item.getId().startsWith("journey_visit_") || item.getId().startsWith("journey_news_") ? item.getName()+": "+countLabel+item.getOwned()+" / "+item.getRequired() : creature ? item.getName() + ": pokonano " + item.getOwned() + " z " + item.getRequired()
                    : item.getName() + ": masz " + item.getOwned() + ", do przekazania NPC zostało " + item.getRequired() + ".");
            ingredients.add(label);
            if(expeditionGoal)label.setToolTipText(item.getName()+": "+countLabel+item.getOwned()+" / "+item.getRequired());
        }
        boolean visible = ingredients.getComponentCount() > 0;
        ingredientsScroll.setVisible(visible);
        if (visible) {
            int height = ingredients.getPreferredSize().height + 14;
            ingredientsScroll.setPreferredSize(new Dimension(0, height));
            ingredientsScroll.setMinimumSize(new Dimension(0, height));
        }
    }

    private void buildCatalog(List<Item> items) {
        catalog.removeAll();
        for (Item item : items) {
            if (!item.isForSale()) { continue; }
            JPanel row = transparent(new BorderLayout(8, 0));
            row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, LINE),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
            row.setPreferredSize(new Dimension(500, 54)); row.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel icon = new JLabel(new DialogueItemIcon(item.getSprite())); row.add(icon, BorderLayout.WEST);
            JLabel label = new JLabel(); label.setForeground(INK); label.setFont(new Font("Dialog", Font.PLAIN, 13));
            row.add(label, BorderLayout.CENTER);
            JPanel controls = transparent(new FlowLayout(FlowLayout.RIGHT, 5, 3));
            JSpinner amount = new JSpinner(new SpinnerNumberModel(1, 1, Math.max(1, item.getMaxAmount()), 1));
            amount.setName("amount-" + item.getId()); amount.setPreferredSize(new Dimension(60, 30));
            JFormattedTextField editor = ((JSpinner.DefaultEditor) amount.getEditor()).getTextField();
            editor.setBackground(new Color(35, 38, 41)); editor.setForeground(INK); editor.setCaretColor(INK);
            editor.setFont(new Font("Dialog", Font.PLAIN, 13));
            Runnable updatePrice = () -> label.setText("<html>" + escape(item.getName())
                    + "<br><span style='color:#aeb5bb;font-size:11px'>" + item.priceFor((Integer) amount.getValue())
                    + " monet | Masz: " + item.getOwned() + " szt.</span></html>");
            updatePrice.run(); amount.addChangeListener(event -> updatePrice.run());
            controls.add(amount);
            JButton buy = new JButton("Kup"); styleButton(buy); buy.setFont(new Font("Dialog", Font.PLAIN, 12));
            buy.setName("buy-" + item.getId()); buy.setToolTipText("Zapytaj o zakup. NPC poprosi o potwierdzenie ceny.");
            buy.addActionListener(event -> {
                try { amount.commitEdit(); }
                catch (java.text.ParseException e) { showFeedback("Wpisz poprawną ilość."); return; }
                if (page == null || !page.active || waiting) { return; }
                int count = (Integer) amount.getValue();
                String echo = "Chcę kupić: " + item.getName() + " (" + count + " szt.).";
                appendHistory("Ty", echo); pendingEcho = echo; setLastReply(echo);
                waiting = true; feedback = ""; updateEnabled(); listener.purchaseRequested(item.getId(), count);
            });
            controls.add(buy); row.add(controls, BorderLayout.EAST); catalog.add(row);
        }
        catalog.add(Box.createVerticalGlue());
    }

    private static void enableTree(Container parent, boolean enabled) {
        for (Component component : parent.getComponents()) {
            component.setEnabled(enabled);
            if (component instanceof Container) { enableTree((Container) component, enabled); }
        }
    }

    private void appendHistory(String speaker, String text) {
        transcript.add("<p><b>" + escape(speaker) + ":</b> " + escape(text) + "</p>");
        if (transcript.size() > 100) { transcript.remove(0); }
        renderHistory();
    }

    private void setLastReply(String text) {
        // One line keeps the current NPC speech readable; the complete reply remains in history.
        String line = text.isEmpty() ? "" : "Ty: " + text.replaceAll("\\s+", " ").trim();
        lastReply.setText(line);
        lastReply.setToolTipText(line.isEmpty() ? null : "<html><div style='width:320px'>" + escape(line) + "</div></html>");
        lastReply.setVisible(!line.isEmpty());
        revalidate(); repaint();
    }

    private void renderHistory() {
        StringBuilder text = new StringBuilder();
        for (String entry : transcript) { text.append(entry); }
        history.setText(html(13, text.toString())); history.setCaretPosition(0);
    }

    @Override protected void paintComponent(Graphics graphics) {
        // Use the same complete 128px metal tile as the game's other windows.
        StyleUtil.fillBackground(WoodStyle.getInstance(), graphics, 0, 0, getWidth(), getHeight());
        // A continuous matte inset keeps the metal at the edges and quiets every text area.
        Graphics2D g = (Graphics2D) graphics.create();
        g.setColor(new Color(38, 43, 48, 242));
        g.fillRect(12, 12, getWidth() - 24, getHeight() - 24);
        g.setColor(LINE);
        g.drawRect(12, 12, getWidth() - 25, getHeight() - 25);
        g.dispose();
        super.paintComponent(graphics);
    }

    private static JPanel column() {
        JPanel panel = transparent(null); panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); return panel;
    }
    private static JPanel transparent(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        // StyledPanelUI tiles even non-opaque panels; children must expose our shared background.
        panel.setUI(new BasicPanelUI()); panel.setBorder(BorderFactory.createEmptyBorder());
        panel.setOpaque(false); return panel;
    }
    private static JScrollPane scroll(Component component) {
        JScrollPane scroll = new JScrollPane(component, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        for (JScrollBar bar : new JScrollBar[] { scroll.getVerticalScrollBar(), scroll.getHorizontalScrollBar() }) {
        bar.setUnitIncrement(18);
        bar.setPreferredSize(bar.getOrientation() == JScrollBar.VERTICAL ? new Dimension(14, 0) : new Dimension(0, 14));
        bar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                trackColor = new Color(42, 45, 48); thumbColor = new Color(96, 106, 116);
                thumbDarkShadowColor = new Color(47, 51, 55); thumbLightShadowColor = LINE;
                thumbHighlightColor = new Color(124, 134, 144);
            }
            @Override protected JButton createDecreaseButton(int direction) { return arrow(direction); }
            @Override protected JButton createIncreaseButton(int direction) { return arrow(direction); }
            private JButton arrow(int direction) {
                return new BasicArrowButton(direction, new Color(60, 65, 70), LINE, INK, new Color(160, 167, 173));
            }
        });
        }
        return scroll;
    }
    private static JEditorPane htmlPane(int size) {
        JEditorPane pane = new JEditorPane("text/html", ""); pane.setEditable(false); pane.setOpaque(false);
        pane.setBorder(BorderFactory.createEmptyBorder()); pane.setFont(new Font("Dialog", Font.PLAIN, size));
        ((DefaultCaret) pane.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE); return pane;
    }
    private static String html(int size, String content) {
        return "<html><head><style>body{font-family:Dialog;font-size:" + size
                + "px;color:#d4d7da;margin:2px;}p{margin:0 0 8px 0;}a{color:#82aed6;text-decoration:none;}</style></head><body>"
                + content + "</body></html>";
    }
    private static String escape(String text) { return QuestProgressFormatter.escape(text).replace("\n", "<br>"); }
    private static void styleButton(AbstractButton button) {
        button.setUI(new BasicButtonUI() {
            @Override public void paint(Graphics graphics, JComponent component) {
                AbstractButton control = (AbstractButton) component;
                graphics.setColor(!control.isEnabled() || control.getModel().isPressed() ? new Color(33, 39, 45)
                        : control.getModel().isRollover() ? new Color(53, 64, 75)
                        : Boolean.TRUE.equals(control.getClientProperty("dialogue.recommended")) ? new Color(36, 44, 52) : new Color(45, 52, 59));
                graphics.fillRect(0, 0, control.getWidth(), control.getHeight());
                super.paint(graphics, component);
            }
            @Override protected void paintFocus(Graphics graphics, AbstractButton control,
                    Rectangle view, Rectangle text, Rectangle icon) {
                graphics.setColor(ACCENT);
                graphics.drawRect(3, 3, control.getWidth() - 7, control.getHeight() - 7);
            }
        });
        button.setRolloverEnabled(true); button.setContentAreaFilled(false); button.setOpaque(false);
        button.setFont(new Font("Dialog", Font.PLAIN, 14)); button.setForeground(INK);
        button.setBackground(new Color(68, 72, 77)); button.setFocusPainted(true);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static final class Portrait extends JComponent {
        private static final long serialVersionUID = 1L;
        private BufferedImage image;
        private String path;
        void load(String nextPath, String npc) {
            setToolTipText(npc);
            if (nextPath.equals(path)) { return; }
            path = nextPath; image = null;
            try {
                java.net.URL resource = getClass().getClassLoader().getResource(nextPath);
                if (resource != null) { image = ImageIO.read(resource); }
            } catch (IOException e) { /* The name/description remain available without an avatar. */ }
            repaint();
        }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setColor(new Color(12, 16, 20, 175)); g.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
            g.setColor(LINE); g.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
            if (image != null) {
                // Existing NPC sheet: three frames, four directions. Centre frame facing down.
                boolean single = path.startsWith("data/sprites/npc/alternative/");
                int w = single ? image.getWidth() : image.getWidth()/3;
                int h = single ? image.getHeight() : image.getHeight()/4;
                double scale = Math.min((getWidth()-12.0)/w, (getHeight()-12.0)/h);
                int dw = (int) (w*scale), dh = (int) (h*scale);
                int x = (getWidth()-dw)/2, y = (getHeight()-dh)/2;
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.drawImage(image, x, y, x+dw, y+dh, single ? 0 : w, single ? 0 : 2*h, single ? w : 2*w, single ? h : 3*h, null);
            }
            g.dispose();
        }
    }
}
