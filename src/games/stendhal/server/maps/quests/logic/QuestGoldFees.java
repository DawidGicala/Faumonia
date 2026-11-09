package games.stendhal.server.maps.quests.logic;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.*;
import java.util.*;

/** Explicit, saved one-time payments. Progress and delivered materials stay in native quest slots. */
public final class QuestGoldFees {
    public static final class Fee {
        public final int id, amount;
        public final String slot, npc, purpose;
        private Fee(int id, IQuest quest, int amount, String purpose) {
            this.id=id;this.slot=quest.getSlotName();this.npc=quest.getNPCName();
            this.amount=amount;this.purpose=purpose;
        }
        public int price(Player player) {
            return id==22 ? (int)(5000000L * Math.max(0,Math.min(player.getLevel(),597)) / 597) : amount;
        }
        public String command() { return "oplata"+id; }
        public String quoteKey() { return "fee_quote_v1_"+id; }
        public String paidKey() { return "fee_paid_v1_"+id; }
        public boolean completed(Player p) {
            String state=p.getQuest(slot);
            // The old second cloak quest uses "done" for delivery before reward pickup.
            if(id==10)return "done;rewarded".equals(state);
            return p.isQuestCompleted(slot);
        }
        public boolean paid(Player p) { return p.hasQuest(paidKey()); }
        public boolean pending(Player p) { return p.hasQuest(quoteKey())&&!paid(p)&&!completed(p); }
    }
    private static final Map<String,Fee> FEES=new LinkedHashMap<String,Fee>();
    private static final Map<Engine,Set<Integer>> INSTALLED=new WeakHashMap<Engine,Set<Integer>>();
    static {
        add(4,new CloaksForBario(),50000,"dopasowanie rękawic i przygotowanie nagrody");
        add(5,new VampireSword(),100000,"obróbkę ostrza i przygotowanie kuźni");
        add(6,new ElementalDagger1(),100000,"przygotowanie sztyletu żywiołów");
        add(7,new ElementalDagger2(),250000,"wzmocnienie sztyletu żywiołów");
        add(8,new ObsidianKnife(),500000,"obróbkę obsydianu i wykończenie saksu");
        add(9,new CloakCollector(),400000,"materiały do wykończenia czarnego płaszcza");
        add(10,new CloakCollector2(),800000,"przygotowanie i dopasowanie butów zabójcy");
        add(12,new StuffForVulcanus(),1000000,"pracę kuźni nad mieczem nieśmiertelnych");
        add(13,new MagicStaff(),2500000,"odczynniki i złożenie różdżki Mokoszy");
        add(14,new StuffForBaldemar(),4000000,"obróbkę mithrilu i wykończenie tarczy");
        add(16,new AncientDragonCloak(),300000,"nici i szycie płaszcza pradawnych smoków");
        add(17,new WeaponsCollector(),100000,"przygotowanie pary mieczy");
        add(18,new WeaponsCollector2(),100000,"odnowienie lodowego ekwipunku");
        add(19,new SuppliesForPhalk(),10000,"dopasowanie zbroi Phalka");
        add(20,new AndrzejCollector(),500000,"odczynniki do trwałego wzmocnienia zdrowia");
        add(21,new ResourceForDiehelm(),200000,"obróbkę surowców i przygotowanie klucza");
        add(22,new CrownForTheWannaBeKing(),5000000,"pracę złotników przy koronie");
        add(23,new CollectChaosThorgal(),100000,"przygotowanie zwojów i zaopatrzenie wypraw");
        add(24,new FishermansLicenseQuiz(),500,"wydanie potwierdzenia zdanego egzaminu");
        add(25,new FishermansLicenseCollector(),2500,"szkolenie i wystawienie karty wędkarskiej");
        add(26,new SemosBadgeQuest(),500,"wpis do rejestru mieszkańców Semos");
        add(27,new FadoBadgeQuest(),1500,"wpis do rejestru mieszkańców Fado");
        add(28,new AdosBadgeQuest(),5000,"wpis do rejestru mieszkańców stolicy Ados");
        add(29,new KirdnehBadgeQuest(),2500,"wpis do rejestru mieszkańców Kirdneh");
        add(30,new NalworBadgeQuest(),2500,"wpis do rejestru mieszkańców Nalwor");
        add(31,new TakeGoldforGrafindle(),10000,"przygotowanie klucza do banku");
    }
    private QuestGoldFees() { }
    private static void add(int id,IQuest quest,int amount,String purpose) { FEES.put(quest.getSlotName(),new Fee(id,quest,amount,purpose)); }
    public static Fee forSlot(String slot) { return FEES.get(slot); }
    public static Collection<Fee> all() { return Collections.unmodifiableCollection(FEES.values()); }
    public static String catalogueDescription(String slot) {
        Fee fee = forSlot(slot);
        if (fee == null) { return ""; }
        String price = fee.id == 22 ? "5000000 monet przy 597 poziomie; poniżej proporcjonalnie do poziomu"
                : fee.amount + " monet";
        return " Opłata za " + fee.purpose + ": " + price + ". Płatna raz przed końcową nagrodą.";
    }

    /** Information never creates a quote or permits paying for an unavailable reward. */
    public static List<Fee> information(SpeakerNPC npc, Player player) {
        List<Fee> result = new ArrayList<Fee>();
        for (Fee fee : FEES.values()) {
            if (fee.completed(player) || (!fee.npc.equals(npc.getName()) && !paymentNpc(fee, npc.getName()))) { continue; }
            if (fee.id == 10 && !player.isQuestCompleted("cloaks_collector")) { continue; }
            if (fee.id == 18 && !player.isQuestCompleted("weapons_collector")) { continue; }
            if (fee.id == 16 && player.getLevel() < 350 && !player.hasQuest(fee.slot)) { continue; }
            result.add(fee);
        }
        return result;
    }

    public static void installInformation(final SpeakerNPC npc) {
        Set<Integer> registered = INSTALLED.get(npc.getEngine());
        if (registered == null) { registered = new HashSet<Integer>(); INSTALLED.put(npc.getEngine(), registered); }
        if (!registered.add(-1)) { return; }
        for (ConversationStates state : Arrays.asList(ConversationStates.ATTENDING, ConversationStates.QUEST_OFFERED,
                ConversationStates.QUEST_2_OFFERED, ConversationStates.QUESTION_1, ConversationStates.QUESTION_2)) {
            npc.add(state, "koszty zadań", null, state, null, (player, sentence, raiser) -> {
                StringBuilder text = new StringBuilder();
                for (Fee fee : information(npc, player)) {
                    if (text.length() > 0) { text.append("\n\n"); }
                    text.append(fee.paid(player) ? "Opłata za " + fee.purpose + " została już wniesiona." : description(fee.slot, player));
                }
                raiser.say(text.length() == 0 ? "Nie masz u mnie nierozliczonej opłaty za zadanie." : text.toString());
            });
        }
    }
    public static String description(String slot,Player p) {
        Fee f=forSlot(slot);if(f==null)return "";
        return "Potrzebuję "+f.price(p)+" monet na "+f.purpose+". Płacisz raz, zanim wydam ci nagrodę.";
    }

    /** Call before the final material is removed or any completion action runs. Never deducts money. */
    public static boolean requirePaid(Player p,String slot,EventRaiser npc) {
        Fee f=forSlot(slot);
        if(f==null||f.paid(p)||f.completed(p))return true;
        if(!(npc.getEntity() instanceof SpeakerNPC))throw new IllegalArgumentException("Quest fee requires an NPC");
        install((SpeakerNPC)npc.getEntity(),f);
        if(!p.hasQuest(f.quoteKey()))p.setQuest(f.quoteKey(),Integer.toString(f.price(p)));
        npc.say(description(slot,p)+" Aby potwierdzić, wybierz opłatę w oknie rozmowy lub wpisz #"+f.command()+".");
        return false;
    }

    public static List<Fee> pending(SpeakerNPC npc,Player p) {
        List<Fee> result=new ArrayList<Fee>();
        for(Fee f:FEES.values())if(f.pending(p)&&paymentNpc(f,npc.getName())){install(npc,f);result.add(f);}
        return result;
    }
    private static boolean paymentNpc(Fee f,String name) {
        // Payment is requested by the actual reward NPC, not intermediate suppliers.
        if(f.id==16)return "Josephine".equals(name);
        if(f.id==22)return "Kendra Mattori".equals(name);
        return f.npc.equals(name);
    }
    private static void install(final SpeakerNPC npc,final Fee f) {
        Set<Integer> registered=INSTALLED.get(npc.getEngine());
        if(registered==null){registered=new HashSet<Integer>();INSTALLED.put(npc.getEngine(),registered);}
        if(!registered.add(f.id))return;
        // Item-list states have a catch-all transition which would shadow an ANY payment.
        npc.add(new ConversationStates[]{ConversationStates.ATTENDING,ConversationStates.QUESTION_1,ConversationStates.QUESTION_2},f.command(),new ChatCondition(){
            public boolean fire(Player p,Sentence s,Entity e){return f.pending(p)&&paymentNpc(f,npc.getName());}
        },ConversationStates.ATTENDING,null,new ChatAction(){
            public void fire(Player p,Sentence s,EventRaiser raiser){
                int price=f.price(p);
                if(!Integer.toString(price).equals(p.getQuest(f.quoteKey()))){
                    p.setQuest(f.quoteKey(),Integer.toString(price));
                    raiser.say("Po zmianie twojego poziomu opłata wynosi "+price+" monet. Potwierdź ponownie, jeśli chcesz zapłacić.");return;
                }
                if(price>0&&!p.drop("money",price)){
                    raiser.say("Potrzebujesz "+price+" monet. Masz przy sobie "+p.getNumberOfEquipped("money")+". Niczego nie pobieram; wróć z brakującą kwotą.");return;
                }
                p.setQuest(f.paidKey(),Integer.toString(price));
                raiser.say("Przyjmuję "+price+" monet za "+f.purpose+". Dziękuję, nie musisz płacić ponownie. Wróćmy teraz do przekazania ostatnich rzeczy albo odbioru nagrody.");
                p.notifyWorldAboutChanges();
            }
        });
    }
}
