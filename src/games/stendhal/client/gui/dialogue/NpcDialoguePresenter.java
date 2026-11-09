/* SPDX-License-Identifier: GPL-2.0-or-later */
package games.stendhal.client.gui.dialogue;

import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.client.gui.dialogue.DialoguePage.Part;
import games.stendhal.client.gui.dialogue.DialoguePage.Reply;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Converts private server data to a view, without parsing player commands or changing quest state. */
public final class NpcDialoguePresenter {
    private static final Pattern TOPIC = Pattern.compile("#(?:'([^']+)'|\"([^\"]+)\"|([\\p{L}\\p{N}_-]+))");
    private final NpcDialoguePanel panel;
    private String session;
    private String previousSpeech;

    public NpcDialoguePresenter(NpcDialoguePanel panel) { this.panel = panel; }

    public void reset() {
        session = null; previousSpeech = null; panel.resetHistory();
    }

    public void display(NpcDialogueSnapshot snapshot) {
        boolean fresh = !snapshot.getSession().equals(session);
        if (fresh) { reset(); session = snapshot.getSession(); }
        DialoguePage page = toPage(snapshot);
        if (fresh) { panel.display(page, false); }
        panel.recordPlayerReply(snapshot.getPlayerText());
        boolean appendSpeech = fresh || snapshot.getFeedback().isEmpty()
                || !snapshot.getSpeech().equals(previousSpeech);
        panel.display(page, appendSpeech);
        previousSpeech = snapshot.getSpeech();
        if (!snapshot.getFeedback().isEmpty()) { panel.showFeedback(snapshot.getFeedback()); }
    }

    public static DialoguePage toPage(NpcDialogueSnapshot snapshot) {
        List<Reply> replies = new ArrayList<Reply>();
        for (NpcDialogueSnapshot.Reply reply : snapshot.getReplies()) {
            if (snapshot.isActive()) { replies.add(new Reply(reply.getId(), reply.getLabel())); }
        }
        List<Part> words = new ArrayList<Part>();
        Matcher matcher = TOPIC.matcher(snapshot.getSpeech());
        int previous = 0;
        while (matcher.find()) {
            if (matcher.start() > previous) { words.add(Part.text(snapshot.getSpeech().substring(previous, matcher.start()))); }
            String word = matcher.group(1) != null ? matcher.group(1)
                    : matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
            String id = null;
            if (snapshot.isActive()) {
                for (NpcDialogueSnapshot.Reply reply : snapshot.getReplies()) {
                    if (!reply.getTopic().isEmpty() && normalize(word).equals(normalize(reply.getTopic()))) {
                        id = reply.getId(); break;
                    }
                }
            }
            words.add(id == null ? Part.text(word) : Part.topic(word, id));
            previous = matcher.end();
        }
        if (previous < snapshot.getSpeech().length()) { words.add(Part.text(snapshot.getSpeech().substring(previous))); }
        String sprite = snapshot.getSprite();
        // Sprite names are data, not arbitrary filesystem paths or external URLs.
        String resource = sprite.matches("npc/[A-Za-z0-9_/-]+") && !sprite.contains("..")
                ? "data/sprites/" + sprite + ".png" : "";
        return new DialoguePage(snapshot.getNpcName(), snapshot.getDescription(), resource,
                snapshot.getTopic(), snapshot.getNarration(), words, replies, snapshot.isActive(),
                snapshot.getItems(), snapshot.isShop(), snapshot.getMoney(), snapshot.getRewards());
    }

    private static String normalize(String word) { return word.trim().toLowerCase(Locale.ROOT); }
}
