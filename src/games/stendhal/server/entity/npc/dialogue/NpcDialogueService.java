package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.server.actions.validator.StandardActionValidations;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.NpcDialogueEvent;
import games.stendhal.server.entity.npc.dialogue.PilotDialogueReplies.Choice;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import marauroa.common.game.RPAction;

/** Game-thread adapter around the existing single-attending NPC engine. */
public final class NpcDialogueService {
    private static final Map<SpeakerNPC, Session> sessions = new IdentityHashMap<SpeakerNPC, Session>();
    private NpcDialogueService() { }

    /** Read the actual published reply label for this player's guide, without advancing dialogue. */
    public static String guideReply(SpeakerNPC npc, Player player, String... preferredIds) {
        Session session=sessions.get(npc);
        if(session==null||session.player!=player||npc.getAttending()!=player)return "";
        for(String id:preferredIds){Choice choice=session.choices.get(id);if(choice!=null)return choice.reply.getLabel();}
        return "";
    }

    /** The tutorial reads the seller's actual quote, never guesses from the player's words. */
    public static final class GuidePurchase {
        public final String item;
        public final int amount,price;
        private GuidePurchase(String item,int amount,int price){this.item=item;this.amount=amount;this.price=price;}
    }
    public static void quotedPurchase(SpeakerNPC npc,Player player,String item,int amount,int price){
        Session session=sessions.get(npc);
        if(session!=null&&session.player==player&&session.processing)session.guidePurchase=new GuidePurchase(item,amount,price);
    }
    public static GuidePurchase guidePurchase(SpeakerNPC npc,Player player){
        Session session=sessions.get(npc);
        return session!=null&&session.player==player&&npc.getAttending()==player
            &&npc.getEngine().getCurrentState()==ConversationStates.BUY_PRICE_OFFERED?session.guidePurchase:null;
    }
    public static boolean guideShopOpen(SpeakerNPC npc,Player player){
        Session session=sessions.get(npc);
        return session!=null&&session.player==player&&npc.getAttending()==player&&session.shopping
            &&npc.getEngine().getCurrentState()==ConversationStates.ATTENDING;
    }

    private static final class Session {
        final SpeakerNPC npc;
        final Player player;
        final String id = UUID.randomUUID().toString();
        long revision;
        String speech = "";
        String captured = "";
        String narration = "";
        String capturedNarration = "";
        String hintContext = "";
        String nextHintContext = "";
        String echo = "";
        String pendingEcho;
        String questStamp = "";
        boolean processing;
        boolean published;
        ConversationStates state;
        Map<String, Choice> choices = Collections.emptyMap();
        List<NpcDialogueSnapshot.Item> items = Collections.emptyList();
        final List<NpcDialogueSnapshot.Reward> rewards = new ArrayList<NpcDialogueSnapshot.Reward>();
        String command = "", lastCommand = "", itemContext = "";
        String branch = "root";
        ConversationStates previousState;
        JourneyRewardObserver journeyRewards;
        boolean carmenIntroduced, shopping, nextShopping;
        GuidePurchase guidePurchase;
        Session(SpeakerNPC npc, Player player) { this.npc = npc; this.player = player; }
    }

    /** Called immediately before a real engine step, only for its actual attending player. */
    public static void beforeStep(SpeakerNPC npc, Player player, String text) {
        if (!PilotDialogueReplies.supports(npc) || npc.getAttending() != player) { return; }
        Session session = sessions.get(npc);
        if (session == null || session.player != player) {
            end(npc, "");
            session = new Session(npc, player);
            sessions.put(npc, session);
        }
        session.processing = true;
        session.previousState = npc.getEngine().getCurrentState();
        session.command = text.trim().toLowerCase(Locale.ROOT);
        session.nextShopping = session.shopping && npc.getEngine().getCurrentState() == ConversationStates.BUY_PRICE_OFFERED;
        session.captured = "";
        session.capturedNarration = "";
        session.rewards.clear();
        session.journeyRewards=(ExpeditionDialogueReplies.supports(npc)||MasterDialogueReplies.supports(npc)||JourneyDialogueReplies.supports(npc)||HuntDialogueReplies.supports(npc)||CollectionDialogueReplies.supports(npc))?new JourneyRewardObserver(player):null;
        session.nextHintContext = "";
        session.echo = limit(session.pendingEcho == null ? text : session.pendingEcho, NpcDialogueSnapshot.MAX_INPUT);
        session.pendingEcho = null;
    }

    /** Capture only speech made by this step, never a busy reply addressed to another player. */
    public static void spoken(SpeakerNPC npc, String text) {
        Session session = sessions.get(npc);
        if (session != null && session.processing && session.player == npc.getAttending() && text != null) {
            session.captured = limit(session.captured.isEmpty() ? text : session.captured + "\n\n" + text, 16000);
        }
    }

    /** Pure presentation actions are accepted only inside this player's real engine step. */
    public static void narrate(SpeakerNPC npc, Player player, String text) {
        Session session = sessions.get(npc);
        if (session != null && session.processing && session.player == player) {
            session.capturedNarration = limit(text, 2000);
        }
    }
    public static void setHintContext(SpeakerNPC npc, Player player, String context) {
        Session session = sessions.get(npc);
        if (session != null && session.processing && session.player == player) {
            session.nextHintContext = context;
        }
    }
    /** Called only after an existing reward action has finished successfully. */
    public static void reward(SpeakerNPC npc, Player player, NpcDialogueSnapshot.Reward reward) {
        Session session = sessions.get(npc);
        if (session != null && session.processing && session.player == player && session.rewards.size() < 32) {
            session.rewards.add(reward);
        }
    }
    static String hintContext(SpeakerNPC npc, Player player) {
        Session session = sessions.get(npc);
        return session != null && session.player == player ? session.hintContext : "";
    }

    static boolean hasCarmenIntroduction(SpeakerNPC npc, Player player) {
        Session session = sessions.get(npc);
        return session != null && session.player == player && session.carmenIntroduced;
    }
    static String itemContext(SpeakerNPC npc, Player player) {
        Session session = sessions.get(npc);
        return session != null && session.player == player ? session.itemContext : "";
    }
    static String branch(SpeakerNPC npc, Player player) {
        Session session = sessions.get(npc);
        return session != null && session.player == player ? session.branch : "root";
    }
    static String lastCommand(SpeakerNPC npc, Player player) {
        Session session=sessions.get(npc);
        return session!=null && session.player==player ? session.lastCommand : "";
    }
    static boolean repeatsTopic(SpeakerNPC npc, Player player, String command) {
        Session session = sessions.get(npc);
        if (session == null || session.player != player) { return false; }
        if (command.equalsIgnoreCase(session.lastCommand)) { return true; }
        String context = session.itemContext;
        return ("carmen_arandula".equals(context) && "arandula".equals(command))
                || ("carmen_mushrooms".equals(context) && ("borowik".equals(command) || "pieczarka".equals(command)))
                || ("carmen_apples".equals(context) && "jabłko".equals(command))
                || ("carmen_wood".equals(context) && "polano".equals(command))
                || ("sedro_rats".equals(context) && "szczury".equals(command))
                || ("sedro_gnomes".equals(context) && "gnomy".equals(command))
                || ("ouchit_karl".equals(context) && "karl".equalsIgnoreCase(command));
    }

    /** State, inventory and reward actions have finished before this is called. */
    public static void afterStep(SpeakerNPC npc, Player player, boolean handled) {
        Session session = sessions.get(npc);
        if (session == null || session.player != player || !session.processing) { return; }
        if(handled && session.journeyRewards!=null)session.journeyRewards.finish(npc,player,session.rewards);
        session.journeyRewards=null;
        session.processing = false;
        if (handled) {
            updateBranch(session);
            session.lastCommand = session.command;
            if ("carmen_intro".equals(session.nextHintContext)) { session.carmenIntroduced = true; }
            if (!session.nextHintContext.isEmpty()) { session.itemContext = session.nextHintContext; }
            else if (!java.util.Arrays.asList("rycina", "grzyby", "sad", "drewno", "podziemia", "wioska", "farma").contains(session.command)) {
                session.itemContext = "";
            }
            session.shopping = (npc.getEngine().getCurrentState() == ConversationStates.ATTENDING
                    && NpcDialogueItems.opensShop(npc, session.command)) || session.nextShopping || (session.shopping
                    && npc.getEngine().getCurrentState() == ConversationStates.BUY_PRICE_OFFERED);
            session.narration = session.capturedNarration;
            session.hintContext = session.nextHintContext;
        }
        if (!session.captured.isEmpty()) { session.speech = session.captured; }
        if (!session.rewards.isEmpty()) {
            StringBuilder receipt = new StringBuilder("Otrzymujesz: ");
            for (NpcDialogueSnapshot.Reward reward : session.rewards) {
                if (receipt.length() > 13) { receipt.append(", "); }
                receipt.append(reward.describe());
            }
            player.sendPrivateText(receipt.toString() + ".");
        }
        if (!npc.isTalking()) {
            end(npc, "");
            return;
        }
        String feedback = !handled ? "Brak odpowiedzi na ten tekst. Spróbuj inaczej lub wybierz temat z listy."
                : session.captured.isEmpty() ? "Przyjęto odpowiedź." : "";
        publish(session, true, feedback, session.echo);
    }

    private static void updateBranch(Session session) {
        String command = session.command;
        if ("Miter".equals(session.npc.getName()) && ("skup".equals(command)
                || session.previousState == ConversationStates.SELL_PRICE_OFFERED)) {
            session.branch = "sell"; return;
        }
        if ("tematy".equals(command) || "wątki".equals(command)) { session.branch = "root"; return; }
        if (AncientCloakDialogue.supports(session.npc) && java.util.Arrays.asList("pradawny", "pradawny płaszcz", "płaszcz pradawnych smoków", "pradawnych smoków").contains(command)) { session.branch="ancient"; return; }
        if (AdditionalDialogueReplies.supports(session.npc) || ResidentDialogueReplies.supports(session.npc) || JourneyDialogueReplies.supports(session.npc) || HuntDialogueReplies.supports(session.npc) || CollectionDialogueReplies.supports(session.npc)) {
            if (games.stendhal.server.entity.npc.ConversationPhrases.OFFER_MESSAGES.contains(command)) { session.branch="shop"; return; }
            if (java.util.Arrays.asList("wskazówki","piwnica","wiadomość","bielikrasa").contains(command)) { session.branch="hints"; return; }
            if (java.util.Arrays.asList("jedzenie","joshua","jef","cała","postęp","rynsztunku","używać","miś").contains(command)) { session.branch="quest"; return; }
        }
        if (NpcDialogueItems.opensShop(session.npc, command)) { session.branch = "shop"; return; }
        if (session.previousState == ConversationStates.BUY_PRICE_OFFERED) { session.branch = "shop"; return; }
        if ("carmen_intro".equals(session.nextHintContext)) { session.branch = "intro"; return; }
        if (!session.nextHintContext.isEmpty()) { session.branch = "hints"; return; }
        if (session.previousState == ConversationStates.QUEST_OFFERED
                && games.stendhal.server.entity.npc.ConversationPhrases.NO_MESSAGES.contains(command)) {
            session.branch = "declined"; return;
        }
        if (session.previousState == ConversationStates.QUEST_OFFERED
                || session.previousState == ConversationStates.QUESTION_1
                || session.previousState == ConversationStates.QUESTION_2
                || games.stendhal.server.entity.npc.ConversationPhrases.QUEST_MESSAGES.contains(command)
                || java.util.Arrays.asList("zapasy", "polano", "włosie", "włosie końskie", "koński włos", "ouchit").contains(command)) {
            session.branch = "quest"; return;
        }
        if ("praca".equals(command) || "job".equals(command)) { session.branch = "about"; }
        else if ("pomoc".equals(command) || "help".equals(command)) { session.branch = "help"; }
        else if (java.util.Arrays.asList("ulecz", "heal", "wylecz").contains(command)) { session.branch = "healed"; }
    }

    /** Existing timeout, departure, disconnect or attending change closes only this UI session. */
    public static void end(SpeakerNPC npc, String goodbye) {
        Session session = sessions.remove(npc);
        if (session == null || !session.published) { return; }
        session.narration = "";
        session.hintContext = "";
        session.rewards.clear();
        if (goodbye != null && !goodbye.isEmpty()) { session.speech = limit(goodbye, 16000); }
        publish(session, false, "Rozmowa się zakończyła.", session.processing ? "" : session.echo);
    }

    /** Called by the action listener; nothing in the packet selects a quest or a parser command. */
    public static void handle(Player player, RPAction action) {
        Session own = null;
        Session selected = null;
        for (Session session : new ArrayList<Session>(sessions.values())) {
            if (session.player == player) {
                own = session;
                if (action.has("session") && session.id.equals(action.get("session"))) { selected = session; }
            }
        }
        if (selected == null) {
            if (own != null) { refresh(own, "Ta odpowiedź pochodzi ze starszej rozmowy. Wybierz aktualny temat."); }
            return;
        }
        Session session = selected;
        if (!session.npc.isDialogueCurrent(player)) {
            end(session.npc, "");
            return;
        }
        // Closing a bound encounter is safe even when a preceding reply changed its revision.
        // It must never confirm a purchase or be blocked by the ordinary chat rate limit.
        if (action.has("close")) {
            if (!"1".equals(action.get("close")) || action.has("reply") || action.has("text")
                    || action.has("buy") || action.has("amount")) {
                refresh(session, "Nieprawidłowe zakończenie rozmowy."); return;
            }
            String goodbye = "Do zobaczenia.";
            RPAction chat = new RPAction(); chat.put("text", goodbye);
            if (StandardActionValidations.CHAT.validate(player, chat) == null) { echoPublicly(session, goodbye); }
            session.pendingEcho = goodbye;
            session.npc.listenTo(player, "bye");
            return;
        }
        // Remaining in a conversation (8 tiles) is distinct from being heard (normally 5).
        int hearingRange = session.npc.getPerceptionRange();
        if (Math.abs(player.getX() - session.npc.getX()) >= hearingRange
                || Math.abs(player.getY() - session.npc.getY()) >= hearingRange) {
            refresh(session, "Podejdź bliżej, aby odpowiedzieć.");
            return;
        }
        long revision = -1;
        try { if (action.has("revision")) { revision = Long.parseLong(action.get("revision")); } }
        catch (NumberFormatException ignored) { /* malformed input gets a fresh snapshot below */ }
        if (revision != session.revision || session.state != session.npc.getEngine().getCurrentState()
                || !session.questStamp.equals(questStamp(session))) {
            refresh(session, "Rozmowa się zmieniła. Wybierz odpowiedź z aktualnej listy.");
            return;
        }
        int modes = (action.has("reply") ? 1 : 0) + (action.has("text") ? 1 : 0) + (action.has("buy") ? 1 : 0);
        if (modes != 1 || (!action.has("buy") && action.has("amount"))) {
            refresh(session, "Wybierz jedną odpowiedź albo wpisz własny tekst.");
            return;
        }
        if (action.has("reply")) {
            String id = action.get("reply");
            Choice offered = session.choices.get(id);
            Choice current = PilotDialogueReplies.read(session.npc, player).get(id);
            if (offered == null || current == null || !offered.command.equals(current.command)
                    || !offered.reply.getLabel().equals(current.reply.getLabel())) {
                refresh(session, "Ta odpowiedź nie jest już dostępna. Wybierz aktualny temat.");
                return;
            }
            if (validateChat(session, current.reply.getLabel())) {
                echoPublicly(session, current.reply.getLabel());
                session.pendingEcho = current.reply.getLabel();
                session.npc.listenTo(player, current.command);
            }
        } else if (action.has("buy")) {
            requestPurchase(session, action);
        } else {
            String text = action.get("text").trim();
            if (text.isEmpty() || text.length() > NpcDialogueSnapshot.MAX_INPUT) {
                refresh(session, "Wpisz wiadomość od 1 do 500 znaków.");
                return;
            }
            if (validateChat(session, text)) { echoPublicly(session, text); session.npc.listenTo(player, text); }
        }
    }

    private static void echoPublicly(Session session, String text) {
        session.player.addEvent(new games.stendhal.server.events.PlayerDialogueTextEvent(text));
        new games.stendhal.server.core.engine.GameEvent(session.player.getName(), "chat", null,
                Integer.toString(text.length()), text).raise();
        session.player.notifyWorldAboutChanges();
    }

    private static void requestPurchase(Session session, RPAction action) {
        int amount;
        try { amount = Integer.parseInt(action.get("amount")); }
        catch (RuntimeException e) { refresh(session, "Wybierz poprawną ilość."); return; }
        if (!session.shopping || session.npc.getEngine().getCurrentState() != ConversationStates.ATTENDING) {
            refresh(session, "Najpierw zapytaj o aktualną ofertę."); return;
        }
        List<NpcDialogueSnapshot.Item> current = NpcDialogueItems.read(session.npc, session.player, true);
        for (NpcDialogueSnapshot.Item offered : session.items) {
            if (!offered.isForSale() || !offered.getId().equals(action.get("buy"))) { continue; }
            for (NpcDialogueSnapshot.Item item : current) {
                if (!item.getId().equals(offered.getId()) || !item.getName().equals(offered.getName())) { continue; }
                if (item.getUnitPrice() != offered.getUnitPrice() || item.getPricePercent() != offered.getPricePercent()) {
                    refresh(session, "Cena się zmieniła. Sprawdź aktualną ofertę."); return;
                }
                if (amount < 1 || amount > item.getMaxAmount() || item.priceFor(amount) > Integer.MAX_VALUE) {
                    refresh(session, "Wybierz ilość od 1 do " + item.getMaxAmount() + "."); return;
                }
                String echo = "Chcę kupić: " + item.getName() + " (" + amount + " szt.).";
                if (validateChat(session, echo)) {
                    echoPublicly(session, echo); session.pendingEcho = echo;
                    // The existing seller parser quotes the price and requires its normal yes/no confirmation.
                    session.npc.listenTo(session.player, "buy " + amount + " " + item.getName());
                }
                return;
            }
        }
        refresh(session, "Ten przedmiot nie jest dostępny w bieżącej ofercie.");
    }

    /** The panel shares the existing chat bucket and mute rules with ordinary chat. */
    private static boolean validateChat(Session session, String command) {
        RPAction chat = new RPAction();
        chat.put("text", command);
        String error = StandardActionValidations.CHAT.validate(session.player, chat);
        if (error == null) { return true; }
        refresh(session, error.trim().isEmpty() ? "Zwolnij na chwilę, zanim wyślesz kolejną odpowiedź."
                : limit(error, 2000));
        return false;
    }

    private static void refresh(Session session, String feedback) {
        if (!session.npc.isDialogueCurrent(session.player)) { end(session.npc, ""); }
        else { publish(session, true, feedback, ""); }
    }
    private static void publish(Session session, boolean active, String feedback, String echo) {
        boolean opened = active && !session.published;
        session.revision++;
        session.state = session.npc.getEngine().getCurrentState();
        session.questStamp = questStamp(session);
        session.choices = active ? PilotDialogueReplies.read(session.npc, session.player)
                : Collections.<String, Choice>emptyMap();
        session.items = active ? NpcDialogueItems.read(session.npc, session.player, session.shopping)
                : Collections.<NpcDialogueSnapshot.Item>emptyList();
        String description = session.npc.getDescription();
        String sprite = session.npc.getAlternativeImage()!=null ? "npc/alternative/"+session.npc.getAlternativeImage()
                : session.npc.has("class") ? "npc/" + session.npc.get("class") : "";
        NpcDialogueSnapshot snapshot = new NpcDialogueSnapshot(session.id, session.revision,
                session.npc.getName(), limit(description, 2000), limit(sprite, 200),
                "Carmen".equals(session.npc.getName()) ? "Rozmowa z Carmen"
                        : "Sedro".equals(session.npc.getName()) ? "Rozmowa z Sedro"
                        : "Ouchit".equals(session.npc.getName()) ? "Rozmowa z Ouchitem"
                        : "Karl".equals(session.npc.getName()) ? "Rozmowa z Karlem" : "Rozmówca: " + session.npc.getName(), session.narration,
                session.speech, feedback, limit(echo, NpcDialogueSnapshot.MAX_INPUT), active, opened,
                PilotDialogueReplies.replies(session.choices), session.items, active && session.shopping,
                active && session.shopping ? session.player.getNumberOfEquipped("money") : -1, session.rewards);
        session.published = true;
        session.player.addEvent(new NpcDialogueEvent(snapshot));
        session.player.notifyWorldAboutChanges();
        session.echo = "";
    }
    private static String questStamp(Session session) {
        StringBuilder stamp=new StringBuilder();
        java.util.Set<String> slots=new java.util.TreeSet<String>();
        slots.add(PilotDialogueReplies.questSlot(session.npc));
        if(AncientCloakDialogue.supports(session.npc))java.util.Collections.addAll(slots,"ancient_dragon_cloak","find_dragons");
        if(ExpeditionDialogueReplies.supports(session.npc)||MasterDialogueReplies.supports(session.npc))java.util.Collections.addAll(slots,"main_task_24","main_task_25","fag_death_scout","thyron_expedition_fittings");
        if(MasterDialogueReplies.supports(session.npc))for(int i=1;i<=25;i++)slots.add("main_task_"+i);
        for(games.stendhal.server.maps.quests.IQuest q:games.stendhal.server.core.rp.quest.JourneyQuestProgress.forNpc(session.npc.getName()))slots.add(q.getSlotName());
        for(games.stendhal.server.maps.quests.IQuest q:games.stendhal.server.core.rp.quest.CollectionQuestProgress.forNpc(session.npc.getName())){slots.add(q.getSlotName());slots.addAll(games.stendhal.server.core.rp.quest.CollectionQuestProgress.details(q).getCollectionPrerequisites().keySet());}
        if("Markovich".equals(session.npc.getName()))slots.add("sicky_fill_goblet");
        games.stendhal.server.maps.quests.IQuest resident=games.stendhal.server.core.rp.quest.ResidentQuestProgress.forNpc(session.npc.getName());
        if(resident!=null)slots.add(resident.getSlotName());
        if("Hackim Easso".equals(session.npc.getName()))slots.add("mrsyeti");
        if(games.stendhal.server.maps.quests.PizzaDelivery.isCustomer(session.npc.getName()))slots.add("pizza_delivery");
        for(String slot:slots){String state=session.player.getQuest(slot);stamp.append(slot).append(':').append(state==null?-1:state.length()).append(':').append(state).append(';');}
        return stamp.toString();
    }
    private static String limit(String value, int max) {
        return value == null ? "" : value.length() <= max ? value : value.substring(0, max);
    }
}
