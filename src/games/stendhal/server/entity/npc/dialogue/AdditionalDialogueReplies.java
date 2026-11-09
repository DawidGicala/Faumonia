package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.*;
import games.stendhal.server.entity.npc.dialogue.PilotDialogueReplies.Choice;
import java.util.*;

/** Curated continuations for the second batch, validated again by the real NPC engine. */
final class AdditionalDialogueReplies {
    private final SpeakerNPC npc;
    private final Player player;
    private final String name, value, last, branch;
    private final IQuest quest;
    private final Map<String, Choice> choices = new LinkedHashMap<String, Choice>();
    private AdditionalDialogueReplies(SpeakerNPC npc, Player player) {
        this.npc=npc; this.player=player; name=npc.getName(); quest=quest(npc);
        value=player.getQuest(quest.getSlotName()); last=NpcDialogueService.lastCommand(npc,player);
        branch=NpcDialogueService.branch(npc,player);
    }
    static boolean supports(SpeakerNPC npc) { return quest(npc)!=null; }
    static IQuest quest(SpeakerNPC npc) {
        switch(npc.getName()) {
            case "Plink": return new PlinksToy();
            case "Ilisa": return new LearnAboutOrbs();
            case "Xoderos": case "Joshua": return new HungryJoshua();
            case "Jef": case "Amber": return new FindJefsMom();
            case "Eonna": return new CleanStorageSpace();
            case "Sally": return new Campfire();
            case "Lupos": return new ElvishArmor();
            default: return null;
        }
    }
    static Map<String,Choice> read(SpeakerNPC npc, Player player) {
        AdditionalDialogueReplies b=new AdditionalDialogueReplies(npc,player); b.build(); return b.choices;
    }
    private void add(String id,String label,String command) { add(id,label,command,command,false); }
    private void info(String id,String label,String command) { add(id,label,command,command,true); }
    private void add(String id,String label,String command,String topic,boolean suppressRepeat) {
        if (suppressRepeat && last.equalsIgnoreCase(command)) { return; }
        if (npc.getEngine().canHandle(player,command)) { choices.put(id,new Choice(id,label,command,topic)); }
    }
    private boolean active() { return value!=null && !"rejected".equals(value) && !quest.isCompleted(player); }
    private boolean available() { return value==null || "rejected".equals(player.getQuest(quest.getSlotName(),0)) || quest.isRepeatable(player); }
    private void build() {
        ConversationStates state=npc.getEngine().getCurrentState();
        if (state==ConversationStates.IDLE) { return; }
        if (state==ConversationStates.QUEST_OFFERED) {
            if ("Ilisa".equals(name)) {
                add("instructions","Pokaż mi, jak korzystać z kuli.","używać");
            } else if (available()) {
                String label="Pomogę ci.";
                if ("Plink".equals(name)) { label="Poszukam twojego misia."; }
                if ("Xoderos".equals(name)) { label="Zaniosę Joshui pięć kanapek."; }
                if ("Jef".equals(name)) { label="Poszukam Amber i przekażę ci wiadomość."; }
                if ("Eonna".equals(name)) { label="Zajmę się szczurami i wężem."; }
                if ("Sally".equals(name)) { label="Przyniosę dziesięć polan na ognisko."; }
                if ("Lupos".equals(name)) { label="Zdobędę dla ciebie próbki elfiego rynsztunku."; }
                add("accept",label,"tak"); add("decline","Nie mogę teraz tego obiecać.","nie");
            }
            if ("Plink".equals(name)) { info("hints","Gdzie dokładnie zgubiłeś misia?","wskazówki"); }
            if ("Eonna".equals(name)) { info("hints","Gdzie jest piwnica i na co uważać?","piwnica"); }
            if ("Lupos".equals(name)) { info("supplies","Jakich próbek potrzebujesz?","rynsztunku"); }
        } else if (state==ConversationStates.QUESTION_1 && "Ilisa".equals(name)) {
            add("confirm","Tak, pojawiła się wiadomość.","tak");
            add("cancel","Nie, jeszcze nie udało mi się jej zobaczyć.","nie");
        } else if (state==ConversationStates.QUESTION_1 && "Lupos".equals(name)) {
            if (active()) {
                List<String> required=((ElvishArmor)quest).getNeededItems();
                List<String> done=Arrays.asList(value.split(";"));
                for (int i=0;i<required.size();i++) {
                    String item=required.get(i);
                    if (!done.contains(item) && player.isEquipped(item)) {
                        add("give_"+i,"Przekazuję próbkę: "+item+".",item,"",false);
                    }
                }
                info("supplies","Przypomnij mi, których próbek jeszcze brakuje.","rynsztunku");
            }
            add("later","Na razie to wszystko. Wrócę z kolejnymi próbkami.","nie");
        } else if (state==ConversationStates.QUEST_ITEM_BROUGHT && ("Joshua".equals(name) || "Sally".equals(name))) {
            String item="Joshua".equals(name)?"kanapka":"polano"; int amount="Joshua".equals(name)?5:10;
            if ("start".equals(value) && player.isEquipped(item,amount)) {
                add("confirm","Joshua".equals(name)?"Tak, te kanapki są dla ciebie. Przysyła mnie Xoderos.":"Tak, oddaję dziesięć polan na ognisko.","tak");
            }
            add("cancel","Jeszcze ich nie oddaję.","nie");
        } else if (state==ConversationStates.ATTENDING || (state==ConversationStates.QUEST_STARTED && "Eonna".equals(name))) {
            attending();
        } else {
            add("confirm",state==ConversationStates.BUY_PRICE_OFFERED?"Tak, kupuję.":"Tak, potwierdzam.","tak");
            add("cancel","Nie, rezygnuję.","nie");
        }
        add("goodbye","Do zobaczenia.","bye");
    }
    private void attending() {
        boolean root="root".equals(branch);
        boolean context=!"quest".equals(branch) && !"hints".equals(branch) && !"shop".equals(branch);
        if ("Plink".equals(name)) {
            if (!quest.isCompleted(player)) {
                if (player.isEquipped("pluszowy miś")) { add("give_teddy","Znalazłem misia. Proszę, jest cały.","miś","",false); }
                else { info("hints","Gdzie dokładnie zgubiłeś misia?","wskazówki"); }
            }
        } else if ("Ilisa".equals(name)) {
            if (!quest.isCompleted(player) && context) { info("quest","Chcę się dowiedzieć, jak działają magiczne kule.","zadanie"); }
            if (quest.isCompleted(player) && root) { info("instructions","Przypomnij mi, jak używać kuli.","używać"); }
            info("heal","Potrzebuję leczenia.","ulecz");
            if (player.hasQuest("introduce_players") && !player.isQuestCompleted("introduce_players")) {
                info("tad","Chcę porozmawiać o lekarstwie dla Tada.","tad");
                if (player.isQuestInState("introduce_players","corpse&herbs")) { info("arandula","Gdzie znajdę arandulę dla Tada?","arandula"); }
            }
        } else if ("Xoderos".equals(name)) {
            if ("joshua".equals(value)) { add("give_message","Joshua dostał jedzenie i prosił, żebym cię uspokoił.","Joshua","",false); }
            else if ("start".equals(value)) { info("hints","Gdzie znajdę Joshuę i skąd wziąć jedzenie?","wskazówki"); }
            else if (available() && context) { info("quest","Czy coś cię trapi? Mogę pomóc.","zadanie"); }
            if (available() && "quest".equals(branch)) { add("food","Porozmawiajmy o jedzeniu dla twojego brata.","jedzenie"); }
            if (root || "help".equals(branch)) { info("craft","Chcę porozmawiać o odlaniu żelaza.","odlej"); }
        } else if ("Joshua".equals(name)) {
            if ("start".equals(value)) {
                if (player.isEquipped("kanapka",5)) { add("give_food","Przynoszę pięć kanapek od twojego brata.","jedzenie","",false); }
                else { info("hints","Przysyła mnie Xoderos. Przypomnij, czego potrzebujesz.","wskazówki"); }
            }
            if ("joshua".equals(value)) { info("message","Co mam przekazać Xoderosowi?","wiadomość"); }
            if (root || "help".equals(branch)) { info("craft","Chcę porozmawiać o odlaniu złota.","odlej"); }
        } else if ("Jef".equals(name)) {
            String step=player.getQuest(quest.getSlotName(),0);
            if ("found_mom".equals(step)) {
                if (player.isEquipped("bielikrasa")) { add("give_flower","Amber jest cała i zdrowa. Przynoszę od niej kwiat.","cała","",false); }
                else { info("flower","Przypomnij mi, jaki kwiat mam ci przynieść.","bielikrasa"); }
            } else if ("start".equals(step)) { info("hints","Gdzie szukać Amber?","wskazówki"); }
            else if (available() && context) { info("quest","Czy mogę pomóc ci odnaleźć mamę?","zadanie"); }
            else if (quest.isCompleted(player) && root) { info("cooldown","Czy mam już ponownie odwiedzić Amber?","zadanie"); }
        } else if ("Amber".equals(name)) {
            if ("start".equals(player.getQuest(quest.getSlotName(),0))) { add("give_message","Przysyła mnie Jef. Martwi się o ciebie.","Jef"); }
            if ("found_mom".equals(player.getQuest(quest.getSlotName(),0))) { info("message","Przypomnij, co mam przekazać Jefowi.","wiadomość"); }
        } else if ("Eonna".equals(name)) {
            if (active()) { info("quest","Chcę zdać sprawę z porządków w piwnicy.","postęp"); info("hints","Gdzie jest piwnica i na co uważać?","wskazówki"); }
            else if (available() && context) { info("quest","Wyglądasz na zmartwioną. Potrzebujesz pomocy?","zadanie"); }
        } else if ("Sally".equals(name)) {
            if ("start".equals(value)) {
                if (player.isEquipped("polano",10)) { add("give_wood","Mam dziesięć polan. Chcę ci je przekazać.","polano","",false); }
                info("hints","Gdzie zebrać drewno na ognisko?","wskazówki");
            } else if (available() && context) { info("quest","Przyda ci się pomoc przy ognisku?","zadanie"); }
            else if (quest.isCompleted(player) && root) { info("cooldown","Kiedy znowu przyda ci się drewno?","zadanie"); }
        } else if ("Lupos".equals(name)) {
            if (active()) { add("supplies","Chcę omówić kolekcję i przekazać próbki.","rynsztunku"); }
            else if (available() && context) { info("quest","Jak mogę pomóc w twoich badaniach?","zadanie"); }
            if (quest.isCompleted(player) && "shop".equals(branch)) {
                int i=0;
                for (String item:new TreeSet<String>(games.stendhal.server.core.engine.SingletonRepository.getShopList().get("buyelvish").keySet())) {
                    if (player.isEquipped(item)) { add("sell_"+i,"Chcę sprzedać: "+item+" (1 szt.).","sell 1 "+item,"",false); }
                    i++;
                }
            }
        }
        if (root || context || quest.isCompleted(player)) {
            info("job","Opowiedz mi o sobie.","praca");
            info("help","Co warto wiedzieć o twojej pracy i okolicy?","pomoc");
        }
        if ("Ilisa".equals(name) || "Xoderos".equals(name) || ("Lupos".equals(name) && quest.isCompleted(player))) {
            info("offer","Lupos".equals(name)?"Pokaż, jakie przedmioty teraz skupujesz.":"Pokaż mi swoją ofertę.","oferta");
        }
        if (!root) { add("back","Porozmawiajmy o czymś innym.","tematy", "",false); }
    }
}
