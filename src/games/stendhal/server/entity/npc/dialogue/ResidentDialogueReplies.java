package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.server.core.rp.quest.ResidentQuestProgress;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.*;
import games.stendhal.server.entity.npc.dialogue.PilotDialogueReplies.Choice;
import java.util.*;

/** Explicit continuations for residents, with every command checked by the native FSM. */
final class ResidentDialogueReplies {
    final SpeakerNPC npc; final Player player; final String name,last,branch,value;
    final IQuest quest; final Map<String,Choice> choices=new LinkedHashMap<String,Choice>();
    ResidentDialogueReplies(SpeakerNPC npc,Player player){
        this.npc=npc;this.player=player;name=npc.getName();quest=ResidentQuestProgress.forNpc(name);
        value=quest==null?null:player.getQuest(quest.getSlotName());last=NpcDialogueService.lastCommand(npc,player);branch=NpcDialogueService.branch(npc,player);
    }
    static boolean supports(SpeakerNPC npc){return ResidentQuestProgress.forNpc(npc.getName())!=null||PizzaDelivery.isCustomer(npc.getName());}
    static Map<String,Choice> read(SpeakerNPC npc,Player player){ResidentDialogueReplies b=new ResidentDialogueReplies(npc,player);b.build();return b.choices;}
    static String id(String value){
        return java.text.Normalizer.normalize(value,java.text.Normalizer.Form.NFD).replaceAll("\\p{M}","").replace('ł','l').replace('Ł','L').replaceAll("[^a-zA-Z0-9_-]","_");
    }
    void add(String id,String label,String command){
        id=id(id);
        if(npc.getEngine().canHandle(player,command))choices.put(id,new Choice(id,label,command,command));
    }
    void info(String id,String label,String command){if(!last.equalsIgnoreCase(command))add(id,label,command);}
    boolean active(){return value!=null&&!"rejected".equals(value)&&!quest.isCompleted(player);}
    boolean available(){return quest!=null&&(value==null||"rejected".equals(value)||quest.isRepeatable(player));}
    boolean root(){return "root".equals(branch)||"about".equals(branch)||"help".equals(branch);}
    void build(){
        ConversationStates state=npc.getEngine().getCurrentState();if(state==ConversationStates.IDLE)return;
        if(quest instanceof CheeseForSoro && "ser=0".equals(value)){
            add("claim","To już wszystkie sery. Masz coś dla mnie?","odbiór");
            add("goodbye","Do zobaczenia.","bye");return;
        }
        if("Ilisa".equals(name)) {
            choices.putAll(AdditionalDialogueReplies.read(npc,player));
            if(state==ConversationStates.ATTENDING){
                if("ilisa".equals(value)&&player.isEquipped("flasza"))add("give_flask","Przynoszę flaszę na lekarstwo dla Tada.","lekarstwo tada");
                if("corpse&herbs".equals(value)&&player.isEquipped("arandula"))add("give_arandula","Przekazuję arandulę do lekarstwa dla Tada.","lekarstwo tada");
            }
            return;
        }
        if(state==ConversationStates.QUEST_OFFERED){
            // Native repeat offers may remain valid for asked sheep and expired pizza assignments.
            if(available()||"asked".equals(value)||quest instanceof PizzaDelivery){
                add("accept",acceptLabel(),"tak");add("decline","Nie podejmę się tego teraz.","nie");
            }
            if(quest instanceof Soup||quest instanceof FishSoup)info("ingredients","Przypomnij mi cały przepis.","składniki");
            if(quest instanceof MedicineForTad)info("hints","Gdzie kupię pustą flaszę?","flasza");
        }else if(state==ConversationStates.INFORMATION_1&&(quest instanceof Soup||quest instanceof FishSoup)){
            add("recipe","Chcę zamówić twoją zupę. Jak ją przygotowujesz?",quest instanceof Soup?"ożywić":"zobaczyć");
        }else if(state==ConversationStates.QUESTION_1&&(quest instanceof TaskHayunn||quest instanceof CheeseForSoro||quest instanceof ResourceForDiehelm)){
            add("know",quest instanceof TaskHayunn?"Jasne. Jak to zrobić?":quest instanceof CheeseForSoro?"Pewnie, mogę przynieść ci ser.":"Tak, opowiedz mi, czego potrzebujesz.","tak");
            add("introduce",quest instanceof TaskHayunn?"Może później.":"Jeszcze się nie znamy. Opowiedz mi o sobie.","nie");
        }else if(state==ConversationStates.QUESTION_2&&quest!=null&&ResidentQuestProgress.collection(quest)){
            deliveries();add("later","Na razie to wszystko. Resztę przyniosę później.","nie");
        }else if(state==ConversationStates.QUESTION_1&&(quest instanceof Soup||quest instanceof FishSoup)){
            deliveries();add("later","Wrócę z brakującymi składnikami.","nie");
        }else if(state==ConversationStates.QUESTION_1&&quest instanceof FruitsForCoralia){
            add("supplies","Chcę przekazać owoce z twojej listy.","przekaż");fruitHints();
        }else if(state==ConversationStates.QUESTION_1&&quest instanceof FindRatChildren){
            children();add("later","Wrócę, gdy dowiem się czegoś więcej.","nie");
        }else if(state==ConversationStates.QUESTION_1&&quest instanceof LearnAboutKarma){
            add("confirm","Tak, powiedz mi, jaka jest moja karma.","tak");add("cancel","Na razie dziękuję za wyjaśnienie.","nie");
        }else if(state==ConversationStates.ATTENDING){
            attending();pizza();
            if(root()){
                info("job","Opowiedz mi o sobie.","praca");
                if(!"Tad".equals(name))info("help","Co warto wiedzieć w tym miejscu?","pomoc");
                info("offer","Pokaż mi swoją ofertę.","oferta");
                if("Salva Mattori".equals(name)||"Dr. Feelgood".equals(name))info("heal","Salva Mattori".equals(name)?"Potrzebuję leczenia.":"Czy możesz uleczyć moje zwierzęta?","ulecz");
            }
            if(!"root".equals(branch))add("back","Porozmawiajmy o czymś innym.","tematy");
        }else{
            String label=state==ConversationStates.BUY_PRICE_OFFERED?"Tak, kupuję.":"Tak, potwierdzam.";
            if(state==ConversationStates.SELL_PRICE_OFFERED)label="Tak, sprzedaję.";
            if(state==ConversationStates.SERVICE_OFFERED&&"Katinka".equals(name))label="Tak, przekazuję moje zwierzę do schroniska.";
            if(quest instanceof SheepGrowing&&(state==ConversationStates.QUEST_ITEM_BROUGHT||state==ConversationStates.QUEST_ITEM_QUESTION))label="Sato".equals(name)?"Tak, przekazuję tę owcę jako prezent od Nishiyi.":"Tak, Sato otrzymał już owcę.";
            if(quest instanceof ChocolateForElisabeth&&state==ConversationStates.QUESTION_1)label="Tak, ta tabliczka czekolady jest dla ciebie.";
            if(quest instanceof IcecreamForAnnie&&state==ConversationStates.QUESTION_1)label="Tak, te lody są dla ciebie.";
            if(quest instanceof FishSoupForHughie&&state==ConversationStates.QUEST_ITEM_BROUGHT)label="Tak, przekazuję porcję zupy dla Hughiego.";
            if(quest instanceof SuntanCreamForZara&&state==ConversationStates.QUEST_ITEM_BROUGHT)label="Tak, oddaję ci olejek do opalania.";
            if(quest instanceof ZooFood&&state==ConversationStates.QUEST_ITEM_BROUGHT)label="Tak, przekazuję całą zamówioną karmę.";
            add("confirm",label,"tak");add("cancel","Jeszcze nie. Wrócę do tego później.","nie");
        }
        add("goodbye","Do zobaczenia.","bye");
    }
    String acceptLabel(){
        if(quest instanceof TaskHayunn)return "Jasne, przyniosę ci wodę.";
        if(quest instanceof CheeseForSoro)return "Przyniosę ci trzy kawałki sera.";
        if(quest instanceof MedicineForTad)return "Pomogę ci zdobyć lekarstwo.";
        if(quest instanceof NewsFromHackim)return "Przekażę Xinowi wiadomość o dostawie.";
        if(quest instanceof LearnAboutKarma)return "Tak, staram się pomagać innym.";
        if(quest instanceof SheepGrowing)return "Zaopiekuję się owcą i przyprowadzę ją Sato.";
        if(quest instanceof ResourceForDiehelm)return "Dostarczę surowce z twojej listy.";
        if(quest instanceof CoalForHaunchy)return "Zdobędę dwadzieścia pięć sztuk węgla.";
        if(quest instanceof ChocolateForElisabeth)return "Porozmawiam z twoją mamą i przyniosę czekoladę.";
        if(quest instanceof IcecreamForAnnie)return "Zapytam twoją mamę o zgodę i przyniosę lody.";
        if(quest instanceof Soup||quest instanceof FishSoup)return "Przyniosę składniki. Zacznijmy przygotowania.";
        if(quest instanceof FishSoupForHughie)return "Przyniosę zupę rybną dla twojego syna.";
        if(quest instanceof FruitsForCoralia)return "Zbiorę owoce do twojego kapelusza.";
        if(quest instanceof SuntanCreamForZara)return "Przyniosę ci olejek do opalania.";
        if(quest instanceof ZooFood)return "Przyniosę zamówioną karmę dla zwierząt.";
        if(quest instanceof FindRatChildren)return "Odszukam twoje dzieci i przyniosę wieści.";
        if(quest instanceof HelpMrsYeti)return "Dowiem się, kto może przygotować eliksir miłości.";
        if(quest instanceof PizzaDelivery)return "Podejmę się dostawy. Daj mi pizzę i adres.";
        return "Pomogę ci.";
    }
    void attending(){
        if(quest==null)return;
        if(quest instanceof TaskHayunn||quest instanceof CheeseForSoro||quest instanceof ResourceForDiehelm){
            if(active())add("supplies",quest instanceof TaskHayunn?"Wróciłem w sprawie wody.":quest instanceof CheeseForSoro?"Porozmawiajmy o serze.":"Co jeszcze mam ci przynieść?","zadanie");
            else if(available()){
                if(!root()&&"quest".equals(branch))add("ingredients",quest instanceof TaskHayunn?"Dobrze. Co mam kupić?":quest instanceof CheeseForSoro?"Ile sera mam przynieść?":"Jakich surowców potrzebujesz?",quest instanceof TaskHayunn?"ok":quest instanceof CheeseForSoro?"ser":"surowce");
                else info("quest","Czy mogę ci w czymś pomóc?","zadanie");
            }
            if(active())info("hints","Gdzie zdobędę to, czego potrzebujesz?","wskazówki");
        }else if(quest instanceof MedicineForTad){
            if("Tad".equals(name)){
                if(available()&&root())info("quest","Wyglądasz na chorego. Jak mogę pomóc?","zadanie");
                if("start".equals(value)){
                    if(player.isEquipped("flasza"))add("give_flask","Mam pustą flaszę. Co mam z nią zrobić?","flasza");
                    else info("hints","Gdzie kupię flaszę na twoje lekarstwo?","flasza");
                }else if("ilisa".equals(value)||"corpse&herbs".equals(value))info("hints","Przypomnij, z kim mam porozmawiać o lekarstwie.","Ilisa");
                else if("eliksir".equals(value))add("give_message","Ilisa przygotowała lekarstwo. Prosi, żebyś do niej przyszedł.","lekarstwo tada");
            }
        }else if(quest instanceof NewsFromHackim){
            if("Hackim Easso".equals(name)){
                if(available()&&root())info("quest","Masz dla mnie jakąś wiadomość do przekazania?","zadanie");
                if(active())info("hints","Przypomnij mi, gdzie znajdę Xina Blankę.","Xin Blanca");
                yeti();
            }else if("start".equals(value))add("give_message","Hackim prosił przekazać, że dostawa broni jest gotowa.","wiadomość hackima");
        }else if(quest instanceof LearnAboutKarma){
            if(available()&&root())info("quest","Czy jest coś, czego możesz mnie nauczyć?","zadanie");
            info("karma","Wyjaśnij mi, jak działa karma.","karma");
        }else if(quest instanceof SheepGrowing){
            if("Nishiya".equals(name)){
                if(available()&&root()&&!player.hasSheep())info("quest","Czy potrzebujesz pomocy przy owcach?","zadanie");
                if("handed_over".equals(value))add("give_message","Sato dostał już wyhodowaną owcę.","owca nishiyi");
                if("start".equals(value)){info("hints","Jak nakarmić owcę i bezpiecznie z nią podróżować?","wskazówki");info("sato","Gdzie znajdę Sato i jak przekazać mu owcę?","Sato");}
            }else if("start".equals(value))add("give_sheep",ResidentQuestProgress.sheepWeight(player)>=100?"Przyprowadzam wyhodowaną owcę od Nishiyi.":"Przysyła mnie Nishiya. Jakiej owcy oczekujesz?","owca nishiyi");
        }else if(quest instanceof Soup||quest instanceof FishSoup){
            if(active())add("supplies","Chcę przekazać składniki do zupy.",quest instanceof Soup?"zupa":"zupa rybna");
            else if(available()&&root())info("quest","Chcę zamówić porcję twojej zupy.",quest instanceof Soup?"zupa":"zupa rybna");
            else if(!available()&&root())info("cooldown","Kiedy znów możesz ugotować dla mnie zupę?",quest instanceof Soup?"zupa":"zupa rybna");
            if(active())info("hints","Gdzie szukać składników do twojego przepisu?","wskazówki");
        }else if(quest instanceof FruitsForCoralia){
            if(active())add("supplies","Chcę sprawdzić brakujące owoce i przekazać dostawę.","zadanie");
            else if(available()&&root())info("quest","Czy mogę pomóc uzupełnić owoce w twoim kapeluszu?","zadanie");
            else if(root())info("cooldown","Kiedy znów będziesz potrzebować świeżych owoców?","zadanie");
        }else if(quest instanceof FindRatChildren){
            if(!"Agnus".equals(name)){info("mother","Gdzie znajdę twoją mamę?","mama");return;}
            if(active())add("supplies","Porozmawiajmy o odnalezionych dzieciach.","dzieci");
            else if(available()&&root())info("quest","Czy mogę ci jakoś pomóc?","zadanie");
            else if(root())info("cooldown","Czy masz nowe wieści od dzieci?","dzieci");
            if(active())info("hints","Gdzie mam szukać twoich dzieci?","wskazówki");
        }else if(quest instanceof HelpMrsYeti){yeti();}
        else if(quest instanceof HelpTomi){
            if(player.isEquipped("miecz lodowy"))add("give_sword","Oddaję ci miecz lodowy, żebyś mógł się ochłodzić.","miecz lodowy");
            else info("hints","Powiedz, czego potrzebujesz i gdzie tego szukać.","zadanie");
        }else if(quest instanceof PizzaDelivery){
            if(active()){
                String customer=value.split(";")[0];info("hints","Przypomnij mi, gdzie czeka odbiorca: "+customer+".",customer);
                info("delivery","Chcę omówić obecną dostawę pizzy.","zadanie");
            }else if(root())info("quest","Czy masz pizzę do dostarczenia?","zadanie");
        }else{
            boolean main=name.equals(quest.getNPCName());
            if(main&&available()&&root())info("quest","Czy mogę ci w czymś pomóc?","zadanie");
            else if(main&&active()){
                if(quest instanceof CoalForHaunchy)add("give_coal",player.isEquipped("węgiel",25)?"Przynoszę dwadzieścia pięć sztuk węgla do grilla.":"Przypomnij mi, ile węgla potrzeba do grilla.","węgiel");
                else if(quest instanceof ChocolateForElisabeth||quest instanceof IcecreamForAnnie){
                    boolean chocolate=quest instanceof ChocolateForElisabeth;
                    if("start".equals(value))info("hints","Gdzie znajdę twoją mamę i kupię słodycze?","wskazówki");
                    else if("mummy".equals(value)){
                        String item=chocolate?"tabliczka czekolady":"lody";
                        if(player.isEquipped(item))add("give_sweet","Mama się zgodziła. Przynoszę twój podarunek.",chocolate?"czekolada":"lody");
                        else info("hints","Gdzie mogę kupić słodycze?","wskazówki");
                    }
                }else if(quest instanceof FishSoupForHughie)add("give_soup",player.isEquipped("zupa rybna")?"Przynoszę zupę rybną dla Hughiego.":"Jak przygotować zupę dla Hughiego?","zupa dla hughiego");
                else if(quest instanceof SuntanCreamForZara)add("give_cream",player.isEquipped("olejek do opalania")?"Przynoszę ci olejek do opalania.":"Przypomnij, gdzie zamówić olejek do opalania.","olejek");
                else if(quest instanceof ZooFood)add("supplies","Chcę omówić dostawę karmy dla zwierząt.","karma dla zwierząt");
            }else if(main&&root())info("cooldown","Kiedy znów mogę ci pomóc?","zadanie");
            if(main&&active()&&!(quest instanceof ChocolateForElisabeth)&&!(quest instanceof IcecreamForAnnie))info("hints","Gdzie zdobędę potrzebne rzeczy?","wskazówki");
        }
    }
    void deliveries(){
        if(!active())return;
        try{for(Map.Entry<String,Integer> e:ResidentQuestProgress.remaining(quest,player).entrySet())if(player.isEquipped(e.getKey()))
            add("give_"+e.getKey(),"Przekazuję: "+e.getKey()+" (mam: "+player.getNumberOfEquipped(e.getKey())+").",e.getKey());
        }catch(IllegalArgumentException e){ /* Unknown saves have no guessed hand-in buttons. */ }
    }
    void fruitHints(){try{for(String item:ResidentQuestProgress.remaining(quest,player).keySet())info("hint_"+item,"Gdzie znajdę: "+item+"?",item);}catch(IllegalArgumentException e){}}
    void children(){
        if(value==null||!value.contains(":"))return;String[] sides=value.split(":",-1);if(sides.length!=2)return;
        Set<String> seen=new HashSet<String>(Arrays.asList(sides[0].split(";"))), said=new HashSet<String>(Arrays.asList(sides[1].split(";")));
        for(String child:Arrays.asList("avalon","cody","mariel","opal"))if(seen.contains(child)&&!said.contains(child))add("give_"+child,"Mam wiadomość od dziecka: "+child+".",child);
    }
    void yeti(){
        String step=player.getQuest("mrsyeti",0);if(step==null)step="";
        if("Mrs. Yeti".equals(name)){
            if(available()&&root())info("quest","Co cię trapi? Może zdołam pomóc.","zadanie");
            else if("gotpotion".equals(step))add("give_potion",player.isEquipped("eliksir miłości")?"Przynoszę eliksir miłości od Salvy.":"Przypomnij, jaki eliksir mam dostarczyć.","eliksir");
            else if("dragon".equals(step))add("give_dragon",player.getPet()!=null&&"baby_dragon".equals(player.getPet().get("type"))?"Przekazuję ci małego smoka.":"Powiedz, skąd wziąć małego smoka.","smoki");
            else if("reward".equals(step))info("reward","Przychodzę zapytać o obiecaną nagrodę.","nagroda");
            else if(active())info("hints","Przypomnij, z kim mam porozmawiać o eliksirze.","zadanie");
        }else if("Salva Mattori".equals(name)){
            if("start".equals(step))info("yeti","Mrs. Yeti potrzebuje eliksiru miłości. Czy możesz go przygotować?","eliksir");
            else if("mieczyk".equals(step))add("give_knife",player.isEquipped("mieczyk")?"Przynoszę mieczyk, o który prosiłaś.":"Przypomnij mi, jakiego noża potrzebujesz.","mieczyk");
            else if("eliksir".equals(step))add("give_ingredients","Chcę omówić składniki do eliksiru miłości.","eliksir");
            else if("hackim".equals(step)||"pies".equals(step))info("hints","Przypomnij, kto ma twój nóż.","mieczyk");
        }else if("Hackim Easso".equals(name)){
            if("hackim".equals(step))info("yeti","Salva Mattori przysyła mnie po pożyczony nóż.","salva");
            else if("pies".equals(step))add("give_tarts",player.isEquipped("tarta",5)?"Przynoszę pięć tart. Powiedz, co z nożem Salvy.":"Przypomnij, czego chcesz za pomoc w sprawie Salvy.","salva");
        }
    }
    void pizza(){
        if(name.equals(player.getQuest("pizza_delivery",0)))add("give_pizza","Przynoszę zamówioną pizzę od Leandra.","pizza");
    }
    static void addPizza(SpeakerNPC npc,Player player,Map<String,Choice> choices){
        if(npc.getEngine().getCurrentState()==ConversationStates.ATTENDING&&npc.getName().equals(player.getQuest("pizza_delivery",0))&&npc.getEngine().canHandle(player,"pizza"))
            choices.put("give_pizza",new Choice("give_pizza","Przynoszę zamówioną pizzę od Leandra.","pizza",""));
    }
}
