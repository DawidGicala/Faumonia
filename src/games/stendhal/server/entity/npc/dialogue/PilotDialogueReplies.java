package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.NpcDialogueSnapshot.Reply;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.HerbsForCarmen;
import games.stendhal.server.maps.quests.HelpSedro;
import games.stendhal.server.maps.quests.BowsForOuchit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Explicit pilot labels; the existing engine still decides whether each trigger works. */
final class PilotDialogueReplies {
    static final class Choice {
        final Reply reply;
        final String command;
        Choice(String id, String label, String command) {
            this(id, label, command, command);
        }
        Choice(String id, String label, String command, String topic) {
            this.reply = new Reply(id, label, topic);
            this.command = command;
        }
    }
    private final SpeakerNPC npc;
    private final Player player;
    private final Map<String, Choice> choices = new LinkedHashMap<String, Choice>();

    private PilotDialogueReplies(SpeakerNPC npc, Player player) {
        this.npc = npc;
        this.player = player;
    }
    static boolean supports(SpeakerNPC npc) {
        return StarterDialogueReplies.supports(npc) || MasterDialogueReplies.supports(npc) || "Carmen".equals(npc.getName()) || "Sedro".equals(npc.getName())
                || "Ouchit".equals(npc.getName()) || "Karl".equals(npc.getName()) || AdditionalDialogueReplies.supports(npc) || ResidentDialogueReplies.supports(npc) || JourneyDialogueReplies.supports(npc) || HuntDialogueReplies.supports(npc) || CollectionDialogueReplies.supports(npc);
    }
    static String questSlot(SpeakerNPC npc) {
        if(MasterDialogueReplies.supports(npc))return "main_task_1";
        if(StarterDialogueReplies.supports(npc))return "task_hayunn";
        games.stendhal.server.maps.quests.IQuest hunt=games.stendhal.server.core.rp.quest.HuntQuestProgress.forNpc(npc.getName());
        if(hunt!=null)return hunt.getSlotName();
        java.util.List<games.stendhal.server.maps.quests.IQuest> journeys=games.stendhal.server.core.rp.quest.JourneyQuestProgress.forNpc(npc.getName());
        if(!journeys.isEmpty())return journeys.get(0).getSlotName();
        games.stendhal.server.maps.quests.IQuest resident=games.stendhal.server.core.rp.quest.ResidentQuestProgress.forNpc(npc.getName());
        if (resident!=null && !"Ilisa".equals(npc.getName())) { return resident.getSlotName(); }
        if (AdditionalDialogueReplies.supports(npc)) { return AdditionalDialogueReplies.quest(npc).getSlotName(); }
        if ("Carmen".equals(npc.getName())) { return HerbsForCarmen.QUEST_SLOT; }
        if ("Sedro".equals(npc.getName())) { return "help_sedro"; }
        return BowsForOuchit.QUEST_SLOT;
    }
    static Map<String, Choice> read(SpeakerNPC npc, Player player) {
        return QuestFeeDialogue.decorate(readBase(npc,player),npc,player);
    }
    private static Map<String, Choice> readBase(SpeakerNPC npc, Player player) {
        Map<String,Choice> result;
        if(AncientCloakDialogue.active(npc,player))return AncientCloakDialogue.read(npc,player);
        if(MasterDialogueReplies.supports(npc))return MasterDialogueReplies.read(npc,player);
        if(ExpeditionDialogueReplies.active(npc))return ExpeditionDialogueReplies.read(npc,player);
        if(StarterDialogueReplies.supports(npc)){result=StarterDialogueReplies.read(npc,player);ExpeditionDialogueReplies.decorate(result,npc,player);return result;}
        if (CollectionDialogueReplies.supports(npc)) {
            result=CollectionDialogueReplies.read(npc,player);
        } else if (HuntDialogueReplies.supports(npc)) {
            result=HuntDialogueReplies.read(npc,player);
        } else if (JourneyDialogueReplies.supports(npc)) {
            result=JourneyDialogueReplies.read(npc,player);
        } else if (games.stendhal.server.core.rp.quest.ResidentQuestProgress.forNpc(npc.getName())!=null) {
            result=ResidentDialogueReplies.read(npc,player);
        } else if (AdditionalDialogueReplies.supports(npc)) { result=AdditionalDialogueReplies.read(npc,player); }
        else if ("Carmen".equals(npc.getName())||"Sedro".equals(npc.getName())||"Ouchit".equals(npc.getName())||"Karl".equals(npc.getName())) {
            PilotDialogueReplies builder=new PilotDialogueReplies(npc,player);builder.build();result=builder.choices;
        } else { result=ResidentDialogueReplies.read(npc,player); }
        AncientCloakDialogue.decorate(result,npc,player);
        ResidentDialogueReplies.addPizza(npc,player,result);
        ExpeditionDialogueReplies.decorate(result,npc,player);
        return result;
    }
    static List<Reply> replies(Map<String, Choice> choices) {
        List<Reply> result = new ArrayList<Reply>();
        for (Choice choice : choices.values()) { result.add(choice.reply); }
        // Stable ordering keeps the current task first, then useful services, then small talk.
        result.sort(java.util.Comparator.comparingInt(reply -> priority(reply.getId())));
        return result;
    }
    private static int priority(String id) {
        if(id.startsWith("xxv_"))return 0;
        if (id.startsWith("give_") || java.util.Arrays.asList("accept", "confirm", "know", "interest", "ouchit").contains(id)) { return 0; }
        if (java.util.Arrays.asList("quest", "supplies", "ingredients", "wood", "hair", "hints", "karl").contains(id)) { return 1; }
        if ("heal".equals(id) || "offer".equals(id)) { return 2; }
        if ("job".equals(id) || "help".equals(id)) { return 4; }
        if ("back".equals(id) || "later".equals(id)) { return 5; }
        if ("goodbye".equals(id)) { return 6; }
        return 3;
    }
    private void add(String id, String label, String command) {
        add(id, label, command, command);
    }
    private void add(String id, String label, String command, String topic) {
        boolean informational = !topic.isEmpty() && !java.util.Arrays.asList(
                "quest", "supplies", "offer", "heal", "accept", "decline", "know", "introduce",
                "interest", "later", "confirm", "cancel", "goodbye", "ouchit", "back").contains(id);
        if (informational && NpcDialogueService.repeatsTopic(npc, player, command)) { return; }
        if (npc.getEngine().canHandle(player, command)) {
            choices.put(id, new Choice(id, label, command, topic));
        }
    }
    private void build() {
        ConversationStates state = npc.getEngine().getCurrentState();
        boolean carmen = "Carmen".equals(npc.getName());
        boolean sedro = "Sedro".equals(npc.getName());
        boolean ouchit = "Ouchit".equals(npc.getName());
        if (state == ConversationStates.IDLE) { return; }
        if (state == ConversationStates.QUEST_OFFERED) {
            // A quest may be changed outside this dialogue. Do not turn the engine's
            // unconditional yes/no transitions into buttons that restart completed work.
            if (canStillAccept()) {
                add("accept", carmen ? "Przyniosę ci składniki." : ouchit
                        ? "Przyniosę ci dziesięć polan." : "Pomogę ci uporać się z potworami.", "tak");
                add("decline", "Nie mogę się teraz tego podjąć.", "nie");
            }
            if (ouchit) { add("wood_hint", "Gdzie zebrać dziesięć polan?", "polano", "polan"); }
        } else if (carmen && state == ConversationStates.QUESTION_1) {
            add("know", "Tak, wiem, że zajmujesz się leczeniem.", "tak");
            add("introduce", "Jeszcze się nie znamy. Opowiedz mi o sobie.", "nie");
        } else if (ouchit && state == ConversationStates.QUESTION_1) {
            add("interest", "Tak, powiedz, czego potrzebujesz.", "tak");
            add("later", "Może innym razem. Do zobaczenia.", "nie");
        } else if (carmen && state == ConversationStates.QUESTION_2) {
            // Do not offer already-delivered ingredients or infer a different quest phase.
            String remaining = player.getQuest(HerbsForCarmen.QUEST_SLOT);
            if (remaining != null) {
                for (String item : HerbsForCarmen.getRequiredItems().keySet()) {
                    if (containsRemaining(remaining, item) && player.getNumberOfEquipped(item) > 0) {
                        add("give_" + itemId(item), "Chcę oddać " + deliveryName(item)
                                + " (mam: " + player.getNumberOfEquipped(item) + ").", item, "");
                    }
                }
            }
            add("later", "Na razie to wszystko. Wrócę z kolejnymi składnikami.", "nie");
        } else if (state == ConversationStates.ATTENDING) {
            if (carmen) {
                String quest = player.getQuest(HerbsForCarmen.QUEST_SLOT);
                boolean active = quest != null && !"rejected".equals(quest) && !"done".equals(quest);
                if (active) { add("supplies", "Chcę przekazać składniki albo przypomnieć sobie listę.", "zapasy"); }
                else if (canStillAccept() && ("rejected".equals(quest) || !NpcDialogueService.hasCarmenIntroduction(npc, player))) {
                    add("quest", "Czy mogę ci w czymś pomóc?", "zadanie");
                }
                add("ingredients", "Opowiedz mi o brakujących składnikach.", "leczniczych");
                add("hints", "Gdzie szukać potrzebnych składników?", "wskazówki");
                add("arandula", "Gdzie znajdę arandulę?", "arandula");
                add("mushrooms", "Gdzie szukać borowików?", "borowik");
                add("champignons", "Gdzie znajdę pieczarki?", "pieczarka");
                add("apples", "Skąd wziąć jabłka?", "jabłko");
                add("wood", "Gdzie zbiorę drewno?", "polano");
                add("heal", "Potrzebuję leczenia.", "ulecz");
                add("help", "Co warto wiedzieć przed wyruszeniem w drogę?", "pomoc", "pomóc");
            } else if (sedro) {
                String quest = player.getQuest("help_sedro");
                add("quest", quest != null && quest.startsWith("start;")
                        ? "Jak mi idzie polowanie?" : "Czy potrzebujesz mojej pomocy?", "zadanie");
                add("hints", "Gdzie znajdę te potwory?", "wskazówki");
                add("items", "Jak mogę obejrzeć swój ekwipunek?", "przedmioty");
            } else if (ouchit) {
                String quest = player.getQuest(BowsForOuchit.QUEST_SLOT);
                add("quest", "polano".equals(quest) || "hair".equals(quest)
                        ? "Przypomnij mi, czego jeszcze potrzebujesz." : "Czy mogę ci w czymś pomóc?", "zadanie");
                if ("polano".equals(quest)) {
                    int carried = player.getNumberOfEquipped("polano");
                    add("wood", carried >= 10 ? "Chcę oddać dziesięć polan (mam: " + carried + ")."
                            : "Gdzie zebrać dziesięć polan?", "polano", carried >= 10 ? "" : "polano");
                }
                if ("hair".equals(quest)) {
                    add("hair", player.isEquipped("koński włos") ? "Przynoszę koński włos od Karla."
                            : "Przypomnij mi, skąd wziąć koński włos.", "włosie",
                            player.isEquipped("koński włos") ? "" : "włosie końskie");
                }
                if ("hair".equals(quest) || "done".equals(quest)) {
                    add("karl", "Kim jest Karl i gdzie go znajdę?", "Karl");
                }
                add("help", "Co możesz mi polecić?", "pomoc");
            } else {
                // The real condition also requires the hair stage and no carried horse hair.
                add("ouchit", "Przysyła mnie Ouchit. Potrzebuje końskiego włosa.", "Ouchit");
                add("quest", "Czy przyda ci się pomoc w gospodarstwie?", "zadanie");
                add("help", "Co warto wiedzieć o okolicy?", "pomoc");
                add("neighborhood", "Opowiedz mi o sąsiedztwie.", "sąsiedztwie.", "sąsiedztwie");
                add("philomena", "Gdzie znajdę Philomenę?", "Philomena");
                add("sacks", "Opowiedz mi o workach, które sprzedajesz.", "puste worki");
            }
            add("job", "Opowiedz mi o sobie.", "praca");
            add("offer", sedro ? "Masz coś na sprzedaż?" : "Pokaż mi swoją ofertę.", "oferta");
        } else {
            // Existing services (for example a purchase confirmation) retain their parser.
            add("confirm", state == ConversationStates.BUY_PRICE_OFFERED ? "Tak, kupuję." : "Tak, potwierdzam.", "tak");
            add("cancel", "Nie, rezygnuję.", "nie");
        }
        if (state == ConversationStates.ATTENDING) { contextualChoices(carmen, sedro, ouchit); }
        add("goodbye", "Do zobaczenia.", "bye");
    }

    /** Each branch is a short continuation of what the NPC just said, not the full parser vocabulary. */
    private void contextualChoices(boolean carmen, boolean sedro, boolean ouchit) {
        String branch = NpcDialogueService.branch(npc, player);
        String quest = player.getQuest(questSlot(npc));
        boolean active = quest != null && !quest.startsWith("done") && !"rejected".equals(quest);
        java.util.Set<String> allowed = new java.util.HashSet<String>();
        if ("root".equals(branch)) {
            java.util.Collections.addAll(allowed, "quest", "job", "help", "heal", "offer", "supplies", "ouchit");
            if (carmen && !active && canStillAccept() && NpcDialogueService.hasCarmenIntroduction(npc, player)) {
                allowed.add("ingredients");
            }
            if (active) { java.util.Collections.addAll(allowed, "hints", "wood", "hair"); }
        } else if ("intro".equals(branch)) {
            allowed.add("ingredients");
        } else if ("quest".equals(branch)) {
            if (active) {
                if (carmen) { java.util.Collections.addAll(allowed, "supplies", "hints"); }
                else if (sedro) { allowed.add("hints"); }
                else if (ouchit) { java.util.Collections.addAll(allowed, "wood", "hair", "karl"); }
            } else if (quest != null && quest.startsWith("done")) { allowed.add("offer"); }
        } else if ("hints".equals(branch)) {
            if (carmen) {
                java.util.Collections.addAll(allowed, "arandula", "mushrooms", "champignons", "apples", "wood",
                        "detail_arandula", "detail_mushrooms", "detail_apples", "detail_wood", "supplies");
                if (active) {
                    if (!containsRemaining(quest, "arandula")) { allowed.remove("arandula"); allowed.remove("detail_arandula"); }
                    if (!containsRemaining(quest, "borowik")) { allowed.remove("mushrooms"); }
                    if (!containsRemaining(quest, "pieczarka")) { allowed.remove("champignons"); }
                    if (!containsRemaining(quest, "jabłko")) { allowed.remove("apples"); allowed.remove("detail_apples"); }
                    if (!containsRemaining(quest, "polano")) { allowed.remove("wood"); allowed.remove("detail_wood"); }
                }
            } else if (sedro) {
                add("gnomes", "Gdzie szukać gnomów?", "gnomy");
                add("guide", "Kto pomoże mi odnaleźć się w Semos?", "Starkad");
                add("journal", "Gdzie mogę sprawdzać postęp zadania?", "dziennik");
                java.util.Collections.addAll(allowed, "gnomes", "guide", "journal", "gnome_detail");
            } else if (ouchit) { java.util.Collections.addAll(allowed, "hair", "karl_detail"); }
        } else if ("shop".equals(branch)) {
            if (carmen) { allowed.add("heal"); }
        } else if ("about".equals(branch) || "help".equals(branch)) {
            java.util.Collections.addAll(allowed, "quest", "supplies", "offer", "heal", "ouchit");
            if (carmen && NpcDialogueService.hasCarmenIntroduction(npc, player)) { allowed.add("ingredients"); }
            if (!carmen && !sedro && !ouchit) { java.util.Collections.addAll(allowed, "neighborhood", "philomena", "sacks"); }
        } else if ("healed".equals(branch)) { allowed.add("offer"); }
        // Completed quests never get an invitation to start the same task again.
        if (!active && !canStillAccept()) { allowed.remove("quest"); allowed.remove("ingredients"); }
        if (!carmen && !sedro && !ouchit) { allowed.remove("quest"); }
        if (sedro) { allowed.remove("offer"); }
        if (ouchit && !"root".equals(branch) && !player.isEquipped("koński włos")) { allowed.remove("hair"); }
        choices.keySet().retainAll(allowed);
        if (!"root".equals(branch)) { add("back", "Porozmawiajmy o czymś innym.", "tematy", ""); }
    }
    private boolean canStillAccept() {
        String state = player.getQuest(questSlot(npc));
        return state == null || "rejected".equals(state)
                || ("Sedro".equals(npc.getName()) && state.startsWith("done") && new HelpSedro().isRepeatable(player));
    }
    private static String deliveryName(String item) {
        if ("arandula".equals(item)) { return "arandulę"; }
        if ("borowik".equals(item)) { return "borowiki"; }
        if ("jabłko".equals(item)) { return "jabłka"; }
        if ("polano".equals(item)) { return "polana"; }
        if ("pieczarka".equals(item)) { return "pieczarki"; }
        return item;
    }
    private static boolean containsRemaining(String state, String item) {
        for (String part : state.split(";")) {
            String[] entry = part.split("=");
            if (entry.length == 2 && item.equals(entry[0])) {
                try { return Integer.parseInt(entry[1]) > 0; }
                catch (NumberFormatException e) { return false; }
            }
        }
        return false;
    }
    private static String itemId(String item) {
        if ("jabłko".equals(item)) { return "apple"; }
        return item;
    }
}
