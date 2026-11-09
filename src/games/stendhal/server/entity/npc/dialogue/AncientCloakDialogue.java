package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.AncientDragonCloak;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Presents the existing Terry/Josephine recipe without implementing a second hand-in path. */
final class AncientCloakDialogue {
    static final String SLOT = "ancient_dragon_cloak";
    static boolean supports(SpeakerNPC npc) {
        return "Terry".equals(npc.getName()) || "Josephine".equals(npc.getName());
    }
    static boolean active(SpeakerNPC npc, Player player) {
        return supports(npc) && "ancient".equals(NpcDialogueService.branch(npc, player))
                && npc.getEngine().getCurrentState() == ConversationStates.ATTENDING;
    }
    static void decorate(Map<String, PilotDialogueReplies.Choice> choices, SpeakerNPC npc, Player player) {
        if (!supports(npc) || npc.getEngine().getCurrentState() != ConversationStates.ATTENDING
                || !npc.getEngine().canHandle(player, "pradawny płaszcz")) { return; }
        String stage = player.getQuest(SLOT, 0);
        String label = "Chcę omówić płaszcz pradawnych smoków.";
        Map<String, Integer> needs = AncientDragonCloak.currentRequirements(player, npc.getName());
        boolean all = !needs.isEmpty();
        for (Map.Entry<String, Integer> need : needs.entrySet()) {
            all &= player.isEquipped(need.getKey(), need.getValue());
        }
        if (all) {
            label = "Przekazuję materiały na płaszcz pradawnych smoków.";
        } else if ("sewing".equals(stage) && "Josephine".equals(npc.getName())) {
            label = "Chcę odebrać płaszcz lub sprawdzić czas szycia.";
        } else if ("done".equals(stage)) {
            label = "Porozmawiajmy o ukończonym pradawnym płaszczu.";
        }
        choices.put("ancient_cloak", new PilotDialogueReplies.Choice("ancient_cloak", label,
                "pradawny płaszcz", "płaszcz pradawnych smoków"));
    }
    static Map<String, PilotDialogueReplies.Choice> read(SpeakerNPC npc, Player player) {
        Map<String, PilotDialogueReplies.Choice> result = new LinkedHashMap<String, PilotDialogueReplies.Choice>();
        decorate(result, npc, player);
        if (npc.getEngine().canHandle(player, "wątki")) {
            result.put("back", new PilotDialogueReplies.Choice("back", "Porozmawiajmy o czymś innym.", "wątki", "back"));
        }
        result.put("goodbye", new PilotDialogueReplies.Choice("goodbye", "Do zobaczenia.", "bye", "goodbye"));
        return result;
    }
    static void items(List<NpcDialogueSnapshot.Item> result, SpeakerNPC npc, Player player) {
        for (Map.Entry<String, Integer> entry : AncientDragonCloak.currentRequirements(player, npc.getName()).entrySet()) {
            Item item = SingletonRepository.getEntityManager().getItem(entry.getKey());
            if (item != null) {
                result.add(new NpcDialogueSnapshot.Item("ancient_" + result.size(), entry.getKey(),
                        "items/" + item.getItemClass() + "/" + item.getItemSubclass(), entry.getValue(),
                        player.getNumberOfEquipped(entry.getKey()), -1, 100, 0, "ancient_cloak"));
            }
        }
    }
}
