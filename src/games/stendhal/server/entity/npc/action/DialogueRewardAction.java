package games.stendhal.server.entity.npc.action;

import games.stendhal.common.NpcDialogueSnapshot.Reward;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.dialogue.NpcDialogueService;
import games.stendhal.server.entity.player.Player;
import java.util.function.ToDoubleFunction;

/** Presents the result of existing reward actions without changing their mechanics. */
public final class DialogueRewardAction implements ChatAction {
    private final ChatAction action;
    private final ToDoubleFunction<Player> counter;
    private final String name, sprite;
    private final int itemAmount;

    private DialogueRewardAction(ChatAction action, ToDoubleFunction<Player> counter,
            String name, String sprite, int itemAmount) {
        this.action = action; this.counter = counter; this.name = name;
        this.sprite = sprite; this.itemAmount = itemAmount;
    }
    public static ChatAction item(String name) { return item(name, 1, false); }
    public static ChatAction item(String name, int amount) { return item(name, amount, false); }
    public static ChatAction randomItem(String name, int min, int max) {
        // Same inclusive random draw as EquipRandomAmountOfItemAction's default increment (1).
        return (player, sentence, npc) -> item(name, games.stendhal.common.Rand.randUniform(min, max)).fire(player, sentence, npc);
    }
    public static ChatAction item(String name, int amount, boolean bound) {
        return new DialogueRewardAction(new EquipItemAction(name, amount, bound),
                player -> player.getNumberOfEquipped(name), name, "", amount);
    }
    public static ChatAction xp(int amount) {
        return new DialogueRewardAction(new IncreaseXPAction(amount), player -> player.getXP(),
                "XP postaci", "gui/dialogue/xp", 0);
    }
    public static ChatAction attackXP(int amount) {
        return new DialogueRewardAction(new IncreaseAtkXPAction(amount), player -> player.getInt("atk_xp"),
                "XP ataku", "gui/dialogue/attack_xp", 0);
    }
    public static ChatAction defenseXP(int amount) {
        return new DialogueRewardAction(new IncreaseDefXPAction(amount), player -> player.getInt("def_xp"),
                "XP obrony", "gui/dialogue/defense_xp", 0);
    }
    public static ChatAction karma(ChatAction action) {
        return new DialogueRewardAction(action, player -> player.getKarma(), "Karma", "gui/dialogue/karma", 0);
    }
    public static ChatAction maximumHealth(ChatAction action) {
        return new DialogueRewardAction(action, player -> player.getBaseHP(), "Maksymalne zdrowie", "gui/dialogue/health", 0);
    }
    public static ChatAction healing(ChatAction action) {
        return new DialogueRewardAction(action, player -> player.getHP(), "Leczenie", "gui/dialogue/health", 0);
    }
    public static ChatAction unlock(String feature, String label) {
        return new DialogueRewardAction(new EnableFeatureAction(feature),
                player -> player.hasFeature(feature) ? 1 : 0, label, "gui/dialogue/unlock", 0);
    }
    /** Announce access already granted by the quest's own state, without adding another unlock. */
    public static ChatAction access(String label) {
        return (player, sentence, raiser) -> {
            if (raiser.getEntity() instanceof SpeakerNPC) {
                NpcDialogueService.reward((SpeakerNPC)raiser.getEntity(), player,
                        new Reward(label, "gui/dialogue/unlock", 1, false));
            }
        };
    }
    /** Observe an existing (including random) item grant without changing how it is equipped. */
    public static void recordItem(Player player, EventRaiser raiser, Item item, int before, int amount) {
        if (!(raiser.getEntity() instanceof SpeakerNPC)) { return; }
        SpeakerNPC npc = (SpeakerNPC) raiser.getEntity();
        int carried = Math.max(0, Math.min(amount, player.getNumberOfEquipped(item.getName()) - before));
        String image = "items/" + item.getItemClass() + "/" + item.getItemSubclass();
        if (carried > 0) { NpcDialogueService.reward(npc, player, new Reward(item.getName(), image, carried, false)); }
        if (carried < amount) { NpcDialogueService.reward(npc, player, new Reward(item.getName(), image, amount - carried, true)); }
    }
    @Override public void fire(Player player, Sentence sentence, EventRaiser raiser) {
        double before = counter.applyAsDouble(player);
        action.fire(player, sentence, raiser);
        if (!(raiser.getEntity() instanceof SpeakerNPC)) { return; }
        SpeakerNPC npc = (SpeakerNPC) raiser.getEntity();
        double change = Math.round((counter.applyAsDouble(player) - before) * 1000000.0) / 1000000.0;
        if (itemAmount > 0) {
            Item item = SingletonRepository.getEntityManager().getItem(name);
            if (item == null) { return; } // EquipItemAction did not award an unknown item.
            int amount = item instanceof StackableItem ? itemAmount : 1;
            String image = "items/" + item.getItemClass() + "/" + item.getItemSubclass();
            int carried = Math.max(0, Math.min(amount, (int) change));
            if (carried > 0) { NpcDialogueService.reward(npc, player, new Reward(name, image, carried, false)); }
            if (carried < amount) { NpcDialogueService.reward(npc, player, new Reward(name, image, amount - carried, true)); }
        } else if (change > 0) {
            NpcDialogueService.reward(npc, player, new Reward(name, sprite, change, false));
        }
    }
}
