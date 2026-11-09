package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.HerbsForCarmen;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.lang.ref.WeakReference;
import java.util.Iterator;

/** Reads real NPC offers and item definitions; never creates a second transaction system. */
public final class NpcDialogueItems {
    // RPObject hashCode changes as its attributes change. Compare the actual NPC identity.
    private static final class Seller {
        final WeakReference<SpeakerNPC> npc;
        final SellerBehaviour behaviour;
        Seller(SpeakerNPC npc, SellerBehaviour behaviour) {
            this.npc = new WeakReference<SpeakerNPC>(npc); this.behaviour = behaviour;
        }
    }
    private static final List<Seller> sellers = new ArrayList<Seller>();
    private NpcDialogueItems() { }

    public static boolean registerSeller(SpeakerNPC npc, SellerBehaviour seller) {
        if (!PilotDialogueReplies.supports(npc)) { return false; }
        sellers.add(new Seller(npc, seller)); return true;
    }

    private static SellerBehaviour sellerFor(SpeakerNPC npc) {
        SellerBehaviour result = null;
        for (Iterator<Seller> iterator = sellers.iterator(); iterator.hasNext();) {
            Seller seller = iterator.next(); SpeakerNPC owner = seller.npc.get();
            if (owner == null) { iterator.remove(); }
            else if (owner == npc) { result = seller.behaviour; }
        }
        return result;
    }
    static boolean opensShop(SpeakerNPC npc, String command) {
        return sellerFor(npc) != null && ("sklep".equals(command) || "shop".equals(command)
                || games.stendhal.server.entity.npc.ConversationPhrases.OFFER_MESSAGES.contains(command));
    }

    static List<NpcDialogueSnapshot.Item> read(SpeakerNPC npc, Player player, boolean shop) {
        List<NpcDialogueSnapshot.Item> result = new ArrayList<NpcDialogueSnapshot.Item>();
        if(MasterDialogueReplies.supports(npc)){MasterDialogueReplies.items(result,npc,player);return result;}
        if(ExpeditionDialogueReplies.active(npc)){ExpeditionDialogueReplies.items(result,npc,player);return result;}
        if (AncientCloakDialogue.active(npc,player) && !shop) {
            AncientCloakDialogue.items(result,npc,player); QuestFeeDialogue.append(result,npc,player); return result;
        }
        if (shop) {
            SellerBehaviour seller = sellerFor(npc);
            // Pilot NPCs use ordinary fixed-price sellers. Specialized services retain their own dialogue.
            if (!fixedItemShopAvailable(npc, player, seller)) { return result; }
            List<String> names = new ArrayList<String>(seller.dealtItems());
            Collections.sort(names);
            if ("Miter".equals(npc.getName()) && names.remove("woda")) { names.add(0, "woda"); }
            for (int i = 0; i < names.size() && i < 64; i++) {
                String name = names.get(i);
                Item item = SingletonRepository.getEntityManager().getItem(name);
                if (item == null) { continue; }
                int price = seller.getUnitPrice(name);
                if (price < 0) { continue; }
                int maximum = npc.getEngine().getCurrentState() == ConversationStates.ATTENDING
                        ? item instanceof StackableItem ? 1000 : 1 : 0;
                result.add(new NpcDialogueSnapshot.Item("shop_" + i, name, sprite(item), 0,
                        player.getNumberOfEquipped(name), price, player.isBadBoy() ? 150 : 100, maximum, ""));
            }
            return result;
        }
        if ("Sedro".equals(npc.getName()) || "Eonna".equals(npc.getName())) {
            games.stendhal.common.QuestProgress progress = games.stendhal.server.core.rp.quest.PilotQuestProgress.read(
                    "Sedro".equals(npc.getName()) ? new games.stendhal.server.maps.quests.HelpSedro()
                    : new games.stendhal.server.maps.quests.CleanStorageSpace(), player);
            if (progress != null) {
                for (games.stendhal.common.QuestProgress.Objective goal : progress.getObjectives()) {
                    String name = goal.getId().split(":")[0];
                    games.stendhal.server.entity.creature.Creature creature = SingletonRepository.getEntityManager().getCreature(name);
                    if (creature != null) {
                        result.add(new NpcDialogueSnapshot.Item("kill_" + result.size(), name,
                                "monsters/" + creature.get("class") + "/" + creature.get("subclass"),
                                goal.getRequired(), goal.getCurrent(), -1, 100, 0, ""));
                    }
                }
            }
        } else if ("Carmen".equals(npc.getName())) {
            String state = player.getQuest(HerbsForCarmen.QUEST_SLOT);
            boolean active = state != null && !"done".equals(state) && !"rejected".equals(state);
            boolean offered = npc.getEngine().getCurrentState() == ConversationStates.QUEST_OFFERED;
            String context = NpcDialogueService.itemContext(npc, player);
            for (Map.Entry<String, Integer> entry : HerbsForCarmen.getRequiredItems().entrySet()) {
                String name = entry.getKey();
                if (active || offered || contextMatches(context, name)) {
                    int needed = active ? remaining(state, name) : entry.getValue();
                    if (needed > 0) { addQuest(result, player, name, needed, "give_" + ("jabłko".equals(name) ? "apple" : name)); }
                }
            }
        } else if ("Ouchit".equals(npc.getName()) || "Karl".equals(npc.getName())) {
            String state = player.getQuest("bows_ouchit");
            if ("polano".equals(state) || npc.getEngine().getCurrentState() == ConversationStates.QUEST_OFFERED) {
                addQuest(result, player, "polano", 10, "wood");
            } else if ("hair".equals(state)) { addQuest(result, player, "koński włos", 1, "hair"); }
        } else if (AdditionalDialogueReplies.supports(npc)) {
            games.stendhal.server.maps.quests.IQuest quest = AdditionalDialogueReplies.quest(npc);
            String state = player.getQuest(quest.getSlotName());
            boolean offered = npc.getEngine().getCurrentState() == ConversationStates.QUEST_OFFERED;
            if ("Plink".equals(npc.getName()) && (offered || "start".equals(state))) {
                addQuest(result, player, "pluszowy miś", 1, "give_teddy");
            } else if (quest instanceof games.stendhal.server.maps.quests.HungryJoshua && (offered || "start".equals(state))) {
                addQuest(result, player, "kanapka", 5, "give_food");
            } else if (quest instanceof games.stendhal.server.maps.quests.FindJefsMom
                    && "found_mom".equals(player.getQuest(quest.getSlotName(), 0))) {
                addQuest(result, player, "bielikrasa", 1, "give_flower");
            } else if ("Sally".equals(npc.getName()) && (offered || "start".equals(state))) {
                addQuest(result, player, "polano", 10, "give_wood");
            } else if ("Lupos".equals(npc.getName())) {
                if (offered || (state != null && !quest.isCompleted(player))) {
                    List<String> required = ((games.stendhal.server.maps.quests.ElvishArmor) quest).getNeededItems();
                    List<String> done = java.util.Arrays.asList(state == null ? new String[0] : state.split(";"));
                    for (int i = 0; i < required.size(); i++) {
                        if (!done.contains(required.get(i))) { addQuest(result, player, required.get(i), 1, "give_" + i); }
                    }
                } else if (quest.isCompleted(player)) {
                    int i = 0;
                    for (String name : new java.util.TreeSet<String>(SingletonRepository.getShopList().get("buyelvish").keySet())) {
                        // Zero required: icon metadata for a sale reply, not a quest requirement.
                        if (player.isEquipped(name)) { addQuest(result, player, name, 0, "sell_" + i); }
                        i++;
                    }
                }
            }
        }
        if ("Miter".equals(npc.getName()) && "sell".equals(NpcDialogueService.branch(npc,player))) {
            int i=0;
            for(String name:SingletonRepository.getShopList().get("miterbuyback").keySet()) {
                if(player.isEquipped(name))addQuest(result,player,name,0,"sell_"+i);
                i++;
            }
        }
        ResidentDialogueItems.append(result,npc,player);
        JourneyDialogueItems.append(result,npc,player);
        HuntDialogueItems.append(result,npc,player);
        CollectionDialogueItems.append(result,npc,player);
        QuestFeeDialogue.append(result,npc,player);
        return result;
    }

    private static boolean contextMatches(String context, String name) {
        return ("carmen_arandula".equals(context) && "arandula".equals(name))
                || ("carmen_mushrooms".equals(context) && ("borowik".equals(name) || "pieczarka".equals(name)))
                || ("carmen_apples".equals(context) && "jabłko".equals(name))
                || ("carmen_wood".equals(context) && "polano".equals(name));
    }

    private static boolean fixedItemShopAvailable(SpeakerNPC npc, Player player, SellerBehaviour seller) {
        if(seller==null)return false;
        if(seller.getClass()==SellerBehaviour.class)return true;
        // Both keep the ordinary item transaction. Only their access or unit price differs.
        if(seller instanceof games.stendhal.server.entity.npc.behaviour.impl.QuestCompletedSellerBehaviour)
            return seller.getTransactionCondition().fire(player,null,npc);
        if("Dr. Feelgood".equals(npc.getName()))return seller.getTransactionCondition().fire(player,null,npc);
        return false;
    }

    private static int remaining(String state, String name) {
        for (String part : state.split(";")) {
            if (part.startsWith(name + "=")) {
                try { return Math.max(0, Integer.parseInt(part.substring(name.length() + 1))); }
                catch (NumberFormatException e) { return 0; }
            }
        }
        return 0;
    }

    private static void addQuest(List<NpcDialogueSnapshot.Item> rows, Player player, String name, int required, String replyId) {
        Item item = SingletonRepository.getEntityManager().getItem(name);
        if (item != null) {
            rows.add(new NpcDialogueSnapshot.Item("item_" + rows.size(), name, sprite(item), required,
                    player.getNumberOfEquipped(name), -1, 100, 0, replyId));
        }
    }

    private static String sprite(Item item) { return "items/" + item.getItemClass() + "/" + item.getItemSubclass(); }
}
