/* SPDX-License-Identifier: GPL-2.0-or-later */
package games.stendhal.client.gui.dialogue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Presentation data only. Availability and effects must be supplied by a controller. */
public final class DialoguePage {
    public static final class Reply {
        public final String id;
        public final String label;
        public Reply(String id, String label) {
            if (id == null || !id.matches("[a-zA-Z0-9_-]+") || label == null) {
                throw new IllegalArgumentException("Invalid reply");
            }
            this.id = id;
            this.label = label;
        }
    }

    public static final class Part {
        public final String text;
        public final String replyId;
        private Part(String text, String replyId) { this.text = text; this.replyId = replyId; }
        public static Part text(String text) { return new Part(text, null); }
        public static Part topic(String text, String replyId) { return new Part(text, replyId); }
    }

    public final String npcName;
    public final String description;
    public final String sprite;
    public final String topic;
    public final String narration;
    public final List<Part> words;
    public final List<Reply> replies;
    public final boolean active;
    public final List<games.stendhal.common.NpcDialogueSnapshot.Item> items;
    public final boolean shop;
    public final int money;
    public final List<games.stendhal.common.NpcDialogueSnapshot.Reward> rewards;

    public DialoguePage(String npcName, String description, String sprite, String topic,
            String narration, List<Part> words, List<Reply> replies, boolean active) {
        this(npcName, description, sprite, topic, narration, words, replies, active,
                Collections.<games.stendhal.common.NpcDialogueSnapshot.Item>emptyList(), false);
    }
    public DialoguePage(String npcName, String description, String sprite, String topic,
            String narration, List<Part> words, List<Reply> replies, boolean active,
            List<games.stendhal.common.NpcDialogueSnapshot.Item> items, boolean shop) {
        this(npcName, description, sprite, topic, narration, words, replies, active, items, shop, -1);
    }
    public DialoguePage(String npcName, String description, String sprite, String topic,
            String narration, List<Part> words, List<Reply> replies, boolean active,
            List<games.stendhal.common.NpcDialogueSnapshot.Item> items, boolean shop, int money) {
        this(npcName, description, sprite, topic, narration, words, replies, active, items, shop, money,
                Collections.<games.stendhal.common.NpcDialogueSnapshot.Reward>emptyList());
    }
    public DialoguePage(String npcName, String description, String sprite, String topic,
            String narration, List<Part> words, List<Reply> replies, boolean active,
            List<games.stendhal.common.NpcDialogueSnapshot.Item> items, boolean shop, int money,
            List<games.stendhal.common.NpcDialogueSnapshot.Reward> rewards) {
        if (npcName == null || description == null || sprite == null || topic == null
                || narration == null || words == null || replies == null) {
            throw new IllegalArgumentException("Incomplete dialogue page");
        }
        Set<String> ids = new HashSet<String>();
        for (Reply reply : replies) {
            if (reply == null || !ids.add(reply.id)) { throw new IllegalArgumentException("Duplicate reply"); }
        }
        for (Part part : words) {
            if (part == null || part.text == null || (part.replyId != null && !ids.contains(part.replyId))) {
                throw new IllegalArgumentException("Topic must reference an available reply");
            }
        }
        this.npcName = npcName; this.description = description; this.sprite = sprite;
        this.topic = topic; this.narration = narration; this.active = active;
        this.words = Collections.unmodifiableList(new ArrayList<Part>(words));
        this.replies = Collections.unmodifiableList(new ArrayList<Reply>(replies));
        this.items = Collections.unmodifiableList(new ArrayList<games.stendhal.common.NpcDialogueSnapshot.Item>(items));
        this.shop = shop;
        this.money = money;
        this.rewards = Collections.unmodifiableList(new ArrayList<games.stendhal.common.NpcDialogueSnapshot.Reward>(rewards));
    }

    public String plainText() {
        StringBuilder text = new StringBuilder();
        for (Part part : words) { text.append(part.text); }
        return text.toString();
    }
}
