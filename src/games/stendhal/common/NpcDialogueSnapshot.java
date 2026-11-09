package games.stendhal.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Private, versioned presentation of one player's current NPC conversation. */
public final class NpcDialogueSnapshot {
    public static final String EVENT = "npc_dialogue";
    // Must differ from EVENT: StendhalClient.send selects an RPClass by action type.
    public static final String ACTION = "npc_dialogue_reply";
    public static final int MAX_INPUT = 500;
    private static final int MAX_REPLIES = 64;
    private static final int MAX_ITEMS = 64;
    private static final int MAX_REWARDS = 32;

    /** Receipt of an award already applied by the server, never a client-side grant. */
    public static final class Reward {
        private final String name, sprite;
        private final double amount;
        private final boolean onGround;
        public Reward(String name, String sprite, double amount, boolean onGround) {
            this.name = checked(name, 100); this.sprite = checked(sprite, 200);
            if (name.isEmpty() || !Double.isFinite(amount) || amount <= 0 || amount > 1.0e12) {
                throw new IllegalArgumentException("Invalid awarded amount");
            }
            this.amount = amount; this.onGround = onGround;
        }
        public String getName() { return name; }
        public String getSprite() { return sprite; }
        public double getAmount() { return amount; }
        public boolean isOnGround() { return onGround; }
        public String formatAmount() { return java.math.BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString(); }
        public String describe() { return "gui/dialogue/unlock".equals(sprite) ? "Odblokowano: " + name
                : name + " +" + formatAmount() + (onGround ? " (na ziemi)" : ""); }
    }

    /** Server-owned item presentation. Prices are informational; the NPC confirms each purchase. */
    public static final class Item {
        private final String id, name, sprite, replyId;
        private final int required, owned, unitPrice, pricePercent, maxAmount;
        public Item(String id, String name, String sprite, int required, int owned,
                int unitPrice, int pricePercent, int maxAmount, String replyId) {
            if (id == null || !id.matches("[a-zA-Z0-9_-]{1,64}") || required < 0 || owned < 0
                    || unitPrice < -1 || pricePercent < 100 || pricePercent > 1000
                    || maxAmount < 0 || maxAmount > 1000) {
                throw new IllegalArgumentException("Invalid dialogue item");
            }
            this.id = id; this.name = checked(name, 100); this.sprite = checked(sprite, 200);
            this.required = required; this.owned = owned; this.unitPrice = unitPrice;
            this.pricePercent = pricePercent; this.maxAmount = maxAmount;
            this.replyId = checked(replyId, 64);
        }
        public String getId() { return id; }
        public String getName() { return name; }
        public String getSprite() { return sprite; }
        public String getReplyId() { return replyId; }
        public int getRequired() { return required; }
        public int getOwned() { return owned; }
        public int getUnitPrice() { return unitPrice; }
        public int getPricePercent() { return pricePercent; }
        public int getMaxAmount() { return maxAmount; }
        public boolean isForSale() { return unitPrice >= 0; }
        public long priceFor(int amount) { return (long) unitPrice * amount * pricePercent / 100; }
    }

    public static final class Reply {
        private final String id;
        private final String label;
        private final String topic;

        public Reply(String id, String label, String topic) {
            if (id == null || !id.matches("[a-zA-Z0-9_-]{1,64}")) {
                throw new IllegalArgumentException("Invalid reply id");
            }
            this.id = id;
            this.label = checked(label, 500);
            this.topic = checked(topic, 100);
        }
        public String getId() { return id; }
        public String getLabel() { return label; }
        /** Optional keyword for linking existing #topics, never client authority. */
        public String getTopic() { return topic; }
    }

    private final String session;
    private final long revision;
    private final String npcName;
    private final String description;
    private final String sprite;
    private final String topic;
    private final String narration;
    private final String speech;
    private final String feedback;
    private final String playerText;
    private final boolean active;
    private final boolean opened;
    private final List<Reply> replies;
    private final List<Item> items;
    private final boolean shop;
    private final int money;
    private final List<Reward> rewards;

    public NpcDialogueSnapshot(String session, long revision, String npcName,
            String description, String sprite, String topic, String narration,
            String speech, String feedback, String playerText, boolean active,
            boolean opened, List<Reply> replies) {
        this(session, revision, npcName, description, sprite, topic, narration, speech, feedback,
                playerText, active, opened, replies, Collections.<Item>emptyList(), false);
    }

    public NpcDialogueSnapshot(String session, long revision, String npcName,
            String description, String sprite, String topic, String narration,
            String speech, String feedback, String playerText, boolean active,
            boolean opened, List<Reply> replies, List<Item> items, boolean shop) {
        this(session, revision, npcName, description, sprite, topic, narration, speech, feedback,
                playerText, active, opened, replies, items, shop, -1);
    }

    public NpcDialogueSnapshot(String session, long revision, String npcName,
            String description, String sprite, String topic, String narration,
            String speech, String feedback, String playerText, boolean active,
            boolean opened, List<Reply> replies, List<Item> items, boolean shop, int money) {
        this(session, revision, npcName, description, sprite, topic, narration, speech, feedback,
                playerText, active, opened, replies, items, shop, money, Collections.<Reward>emptyList());
    }

    public NpcDialogueSnapshot(String session, long revision, String npcName,
            String description, String sprite, String topic, String narration,
            String speech, String feedback, String playerText, boolean active,
            boolean opened, List<Reply> replies, List<Item> items, boolean shop, int money, List<Reward> rewards) {
        this.session = checked(session, 80);
        if (session.isEmpty() || revision < 1 || replies == null || replies.size() > MAX_REPLIES) {
            throw new IllegalArgumentException("Invalid dialogue snapshot");
        }
        this.revision = revision;
        this.npcName = checked(npcName, 100);
        this.description = checked(description, 2000);
        this.sprite = checked(sprite, 200);
        this.topic = checked(topic, 200);
        this.narration = checked(narration, 2000);
        this.speech = checked(speech, 16000);
        this.feedback = checked(feedback, 2000);
        this.playerText = checked(playerText, MAX_INPUT);
        this.active = active;
        this.opened = opened;
        Set<String> ids = new HashSet<String>();
        for (Reply reply : replies) {
            if (reply == null || !ids.add(reply.id)) {
                throw new IllegalArgumentException("Invalid or duplicate reply");
            }
        }
        if (!active && !replies.isEmpty()) {
            throw new IllegalArgumentException("Closed dialogue cannot offer replies");
        }
        this.replies = Collections.unmodifiableList(new ArrayList<Reply>(replies));
        if (items == null || items.size() > MAX_ITEMS || (!active && (!items.isEmpty() || shop))) {
            throw new IllegalArgumentException("Invalid dialogue items");
        }
        ids.clear();
        for (Item item : items) {
            if (item == null || !ids.add(item.id)) { throw new IllegalArgumentException("Duplicate item"); }
        }
        this.items = Collections.unmodifiableList(new ArrayList<Item>(items));
        this.shop = shop;
        if (money < -1 || (money >= 0 && !shop)) { throw new IllegalArgumentException("Invalid shop balance"); }
        this.money = money;
        if (rewards == null || rewards.size() > MAX_REWARDS || rewards.contains(null)) {
            throw new IllegalArgumentException("Invalid reward receipt");
        }
        this.rewards = Collections.unmodifiableList(new ArrayList<Reward>(rewards));
    }

    private static String checked(String text, int max) {
        if (text == null || text.length() > max) { throw new IllegalArgumentException("Invalid dialogue text"); }
        return text;
    }
    public String getSession() { return session; }
    public long getRevision() { return revision; }
    public String getNpcName() { return npcName; }
    public String getDescription() { return description; }
    public String getSprite() { return sprite; }
    public String getTopic() { return topic; }
    public String getNarration() { return narration; }
    public String getSpeech() { return speech; }
    public String getFeedback() { return feedback; }
    public String getPlayerText() { return playerText; }
    public boolean isActive() { return active; }
    public boolean isOpened() { return opened; }
    public List<Reply> getReplies() { return replies; }
    public List<Item> getItems() { return items; }
    public boolean isShop() { return shop; }
    /** Spendable carried coins; -1 means an older server supplied no balance. */
    public int getMoney() { return money; }
    public List<Reward> getRewards() { return rewards; }

    public List<String> toWire() {
        List<String> data = new ArrayList<String>();
        boolean receipt = !rewards.isEmpty();
        boolean extended = receipt || shop || !items.isEmpty();
        Collections.addAll(data, receipt ? "4" : money >= 0 ? "3" : extended ? "2" : "1", session, Long.toString(revision), npcName,
                description, sprite, topic, narration, speech, feedback, playerText,
                active ? "1" : "0", opened ? "1" : "0", Integer.toString(replies.size()));
        for (Reply reply : replies) { Collections.addAll(data, reply.id, reply.label, reply.topic); }
        if (extended) {
            Collections.addAll(data, shop ? "1" : "0", Integer.toString(items.size()));
            for (Item item : items) {
                Collections.addAll(data, item.id, item.name, item.sprite, Integer.toString(item.required),
                        Integer.toString(item.owned), Integer.toString(item.unitPrice),
                        Integer.toString(item.pricePercent), Integer.toString(item.maxAmount),
                        item.replyId.isEmpty() ? "-" : item.replyId);
            }
        }
        if (money >= 0 || receipt) { data.add(Integer.toString(money)); }
        if (receipt) {
            data.add(Integer.toString(rewards.size()));
            for (Reward reward : rewards) {
                Collections.addAll(data, reward.name, reward.sprite, reward.formatAmount(), reward.onGround ? "1" : "0");
            }
        }
        return data;
    }

    /** Malformed or unsupported optional data cannot break the existing chat. */
    public static NpcDialogueSnapshot fromWire(List<String> data) {
        if (data == null || data.size() < 14 || data.size() > 18 + MAX_REPLIES * 3 + MAX_ITEMS * 9 + MAX_REWARDS * 4
                || !("1".equals(data.get(0)) || "2".equals(data.get(0)) || "3".equals(data.get(0)) || "4".equals(data.get(0)))) { return null; }
        try {
            boolean receipt = "4".equals(data.get(0));
            boolean balance = receipt || "3".equals(data.get(0));
            boolean extended = balance || "2".equals(data.get(0));
            int count = Integer.parseInt(data.get(13));
            int end = 14 + count * 3;
            if (count < 0 || count > MAX_REPLIES || (extended ? data.size() < end + 2 : data.size() != end)
                    || !("1".equals(data.get(11)) || "0".equals(data.get(11)))
                    || !("1".equals(data.get(12)) || "0".equals(data.get(12)))) { return null; }
            List<Reply> replies = new ArrayList<Reply>();
            for (int i = 14; i < end; i += 3) {
                replies.add(new Reply(data.get(i), data.get(i + 1), data.get(i + 2)));
            }
            List<Item> items = new ArrayList<Item>();
            boolean shop = false;
            int money = -1;
            List<Reward> rewards = new ArrayList<Reward>();
            if (extended) {
                if (!("0".equals(data.get(end)) || "1".equals(data.get(end)))) { return null; }
                shop = "1".equals(data.get(end));
                int itemCount = Integer.parseInt(data.get(end + 1));
                int itemsEnd = end + 2 + itemCount * 9;
                if (itemCount < 0 || itemCount > MAX_ITEMS || (receipt ? data.size() < itemsEnd + 2
                        : data.size() != itemsEnd + (balance ? 1 : 0))) { return null; }
                for (int i = end + 2; i < itemsEnd; i += 9) {
                    items.add(new Item(data.get(i), data.get(i + 1), data.get(i + 2),
                            Integer.parseInt(data.get(i + 3)), Integer.parseInt(data.get(i + 4)),
                            Integer.parseInt(data.get(i + 5)), Integer.parseInt(data.get(i + 6)),
                            Integer.parseInt(data.get(i + 7)), "-".equals(data.get(i + 8)) ? "" : data.get(i + 8)));
                }
                if (balance) {
                    money = Integer.parseInt(data.get(itemsEnd));
                    if (money < (receipt ? -1 : 0)) { return null; }
                }
                if (receipt) {
                    int rewardCount = Integer.parseInt(data.get(itemsEnd + 1));
                    if (rewardCount < 0 || rewardCount > MAX_REWARDS || data.size() != itemsEnd + 2 + rewardCount * 4) { return null; }
                    for (int i = itemsEnd + 2; i < data.size(); i += 4) {
                        if (!("0".equals(data.get(i + 3)) || "1".equals(data.get(i + 3)))) { return null; }
                        rewards.add(new Reward(data.get(i), data.get(i + 1), Double.parseDouble(data.get(i + 2)), "1".equals(data.get(i + 3))));
                    }
                }
            }
            return new NpcDialogueSnapshot(data.get(1), Long.parseLong(data.get(2)),
                    data.get(3), data.get(4), data.get(5), data.get(6), data.get(7),
                    data.get(8), data.get(9), data.get(10), "1".equals(data.get(11)),
                    "1".equals(data.get(12)), replies, items, shop, money, rewards);
        } catch (IllegalArgumentException e) { return null; }
          catch (NullPointerException e) { return null; }
    }
}
