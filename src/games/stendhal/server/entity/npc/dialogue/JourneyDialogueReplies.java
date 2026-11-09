package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.server.core.rp.quest.JourneyQuestProgress;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.*;
import games.stendhal.server.entity.npc.dialogue.PilotDialogueReplies.Choice;
import java.util.*;

/** Stage-specific continuations. Every displayed action is also checked by the native engine. */
final class JourneyDialogueReplies {
    final SpeakerNPC npc;final Player p;final String name,last;final Map<String,Choice> choices=new LinkedHashMap<String,Choice>();
    JourneyDialogueReplies(SpeakerNPC n,Player player){npc=n;p=player;name=n.getName();last=NpcDialogueService.lastCommand(n,p);}
    static boolean supports(SpeakerNPC n){return !JourneyQuestProgress.forNpc(n.getName()).isEmpty();}
    static Map<String,Choice> read(SpeakerNPC n,Player p){JourneyDialogueReplies b=new JourneyDialogueReplies(n,p);b.build();return b.choices;}
    void add(String id,String label,String command){id=ResidentDialogueReplies.id(id);if(npc.getEngine().canHandle(p,command))choices.put(id,new Choice(id,label,command,command));}
    void info(String id,String label,String command){if(!command.equalsIgnoreCase(last))add(id,label,command);}
    String value(String slot){String v=p.getQuest(slot);return v==null?"":v;}
    boolean available(IQuest q){return !p.hasQuest(q.getSlotName())||"rejected".equals(value(q.getSlotName()))||q.isRepeatable(p);}
    void build(){
        ConversationStates state=npc.getEngine().getCurrentState();if(state==ConversationStates.IDLE)return;
        if("Leander".equals(name)){
            choices.putAll(ResidentDialogueReplies.read(npc,p));
            if(state==ConversationStates.ATTENDING&&Arrays.asList("first","first1").contains(value("magicstaff")))add("give_athor","Chcę przygotować jedzenie dla mieszkańców Athor.","jedzenie");return;
        }
        if(state==ConversationStates.QUEST_ITEM_QUESTION&&"Alrak".equals(name)){
            add("supplies","Jakich zapasów potrzebujesz do pracy w kuźni?","zapasów");
        }else if(state==ConversationStates.QUESTION_2&&"Pequod".equals(name)){
            // A quiz answer belongs in the free-text field; do not disclose it as a button.
        }else if(state==ConversationStates.QUESTION_1&&"Santiago".equals(name)){
            // The actual fish is on the table. No preselected correct answer.
        }else if(state==ConversationStates.QUESTION_1&&"Ceryl".equals(name)&&"seeking_book".equals(value("obsidian_knife"))){
            add("give_message","Książka o kamieniach jest dla Alraka, kowala z Wofol.","Alrak");
        }else if(state==ConversationStates.QUESTION_1&&"Carena".equals(name)){
            String[] parts=value("find_ghosts").toLowerCase(Locale.ROOT).split(":",-1);
            if(parts.length==2)for(String n:Arrays.asList("mary","ben","zak","goran"))if(Arrays.asList(parts[0].split(";")).contains(n)&&!Arrays.asList(parts[1].split(";")).contains(n))add("give_"+n,"Mam wieści od ducha: "+Character.toUpperCase(n.charAt(0))+n.substring(1)+".",n);
            add("later","Na razie to wszystkie wiadomości.","nie");
        }else if((state==ConversationStates.QUESTION_1&&Arrays.asList("Ivan Abe","Wrviliza").contains(name))||(state==ConversationStates.QUESTION_2&&Arrays.asList("Adalbert","Santiago").contains(name))){
            IQuest q="Ivan Abe".equals(name)?new CrownForTheWannaBeKing():"Wrviliza".equals(name)?new KoboldishTorcibud():"Adalbert".equals(name)?new DeadWife():new FishermansLicenseCollector();
            try{for(String item:JourneyQuestProgress.remaining(q,p).keySet())if(p.isEquipped(item))add("give_"+item,"Przekazuję: "+item+" (mam: "+p.getNumberOfEquipped(item)+").",item);}catch(IllegalArgumentException e){ }
            add("later","Pozostałe rzeczy przyniosę później.","nie");
        }else if(state==ConversationStates.QUEST_2_OFFERED&&"Alrak".equals(name)){
            info("knife","Chcę zamówić obsydianowy saks. Jakie są warunki?","nóż");
        }else if(state==ConversationStates.ATTENDING){
            attending();
            for(IQuest q:JourneyQuestProgress.forNpc(name))if(p.hasQuest(q.getSlotName())&&!q.isCompleted(p)&&!"rejected".equals(value(q.getSlotName()))){info("hints","Przypomnij mi następny krok i potrzebne rzeczy.","wskazówki wyprawy");break;}
            info("job","Opowiedz mi o sobie.","praca");info("offer","Pokaż mi swoją ofertę.","oferta");
            if(!"root".equals(NpcDialogueService.branch(npc,p)))add("back","Porozmawiajmy o czymś innym.","wątki");
        }else{
            String yes="Tak, potwierdzam.";
            if(state==ConversationStates.QUEST_OFFERED)yes="Tak, podejmę się tej prośby.";
            if(state==ConversationStates.QUEST_2_OFFERED&&"Santiago".equals(name))yes="Tak, przyniosę po jednej rybie każdego gatunku.";
            if(state==ConversationStates.QUESTION_1&&"Pequod".equals(name))yes="Znam powiedzonko. Chcę je przytoczyć.";
            if(state==ConversationStates.QUESTION_1&&"Hogart".equals(name))yes="Tak, potrzebuję nowej pustej czary.";
            if(state==ConversationStates.QUEST_ITEM_BROUGHT&&"Ognir".equals(name))yes="Tak, oddaję uszkodzony pierścień i płacę za naprawę.";
            if(state==ConversationStates.QUEST_ITEM_BROUGHT&&"Alrak".equals(name))yes="Tak, przyniosę ci książkę od Ceryla.";
            if(state==ConversationStates.BUY_PRICE_OFFERED)yes="Tak, kupuję.";
            if(state==ConversationStates.SELL_PRICE_OFFERED)yes="Tak, sprzedaję.";
            add(state==ConversationStates.QUEST_OFFERED?"accept":"confirm",yes,"tak");add("cancel","Nie teraz. Wrócę do tego później.","nie");
        }
        add("goodbye","Do zobaczenia.","bye");
    }
    void quest(IQuest q,String label,String command){if(available(q))info("quest",label,command);}
    void attending(){
        String v;
        switch(name){
            case "Ceryl":
                v=value("ceryl_book");if(v.isEmpty()||"rejected".equals(v))quest(new LookBookforCeryl(),"Czy mogę pomóc odzyskać wypożyczoną księgę?","book");
                if("start".equals(v))info("jynath","Gdzie znajdę Jynath z twoją księgą?","jynath");
                if("jynath".equals(v))add("give_book","Przychodzę w sprawie księgi od Jynath.","księga ceryla");
                if("seeking_book".equals(value("obsidian_knife")))info("alrak","Szukam książki o kamieniach dla Alraka.","książka o kamieniach");break;
            case "Jynath":if(Arrays.asList("start","jynath").contains(value("ceryl_book")))add("give_book","Ceryl prosi o zwrot czarnej księgi.","księga ceryla");break;
            case "Pequod":if(!p.isQuestCompleted("get_fishing_rod"))info("quest",p.hasQuest("get_fishing_rod")?"Chcę przytoczyć poszukiwane powiedzonko.":"Czy mogę ci w czymś pomóc?","zadanie");break;
            case "Samaron":v=value("magicstaff");
                if(v.isEmpty()||"rejected".equals(v))quest(new MagicStaff(),"Opowiedz mi o magicznej różdżce.","zadanie");
                else if(Arrays.asList("powrot","killing").contains(v))add("give_staff","Przynoszę części i składniki różdżki.","różdżka");break;
            case "Woy'tyk":v=value("magicstaff");
                if("start".equals(v))info("quest","Samaron przysyła mnie w sprawie różdżki.","różdżka");
                if(Arrays.asList("first","first1").contains(v))add("give_food",p.isEquipped("paczka jedzenia")?"Przynoszę jedzenie od Leandra.":"Przypomnij, kto przygotuje jedzenie dla Athor.","jedzenie");
                if("first2".equals(v))add("give_magic","Przynoszę różdżki i magię do nauki.","gotowe");break;
            case "Ylrik":if(Arrays.asList("gotfirstpiece","ptaszory").contains(value("magicstaff")))add("give_staff","Co z różdżką, o której rozmawialiśmy?","różdżka");break;
            case "Alrak":v=value("obsidian_knife");
                if(v.isEmpty()||"rejected".equals(v))info("quest","Jakich zapasów potrzebujesz w kuźni?","zadanie");
                if(v.isEmpty()||"rejected".equals(v))info("supplies","Jakie jedzenie mam przynieść?","zapasów");
                if(Arrays.asList("szynka","mięso","ser").contains(v))add("give_"+v,"Przynoszę zamówione jedzenie: "+v+".",v);
                if("food_brought".equals(v))info("book","Zdobędę dla ciebie książkę o kamieniach.","zadanie");
                if(Arrays.asList("got_book","book_read","knife_offered").contains(v)||v.startsWith("reading;")||v.startsWith("forging;"))add("supplies",v.startsWith("reading;")?"Czy udało ci się już przeczytać książkę?":v.startsWith("forging;")?"Czy mój saks jest gotowy?":"Co dalej z obsydianowym saksem?","praca alraka");break;
            case "Princess Esclara":
                if("capture".equals(value("jailedbarb")))add("lorenz","Dlaczego Lorenz został uwięziony?","Lorenz");
                if("start".equals(value("amazon_princess")))add("give_drink","Przynoszę napój z oliwką.","napój");
                else quest(new AmazonPrincess(),"Czy mogę coś dla ciebie przynieść?","zadanie");break;
            case "Tywysoga":if("got_flower".equals(p.getQuest("elf_princess",0)))add("give_flower","Przynoszę orchideę od Róży Kwiaciarki.","kwiatek");else quest(new ElfPrincess(),"Czy przyda ci się świeża orchidea?","zadanie");break;
            case "Róża Kwiaciarka":if(p.hasQuest("elf_princess")&&!new ElfPrincess().isCompleted(p))add("give_flower","Przychodzę po orchideę dla Tywysogi.","kwiat księżniczki");break;
            case "Princess Ylflia":
                if("jailed".equals(value("jailedbarb")))add("lorenz","Lorenz jest uwięziony na wyspie Amazon.","uwięziony");
                v=value("imperial_princess");if(v.isEmpty()||"rejected".equals(v))info("quest","Chcę pomóc zdobyć lekarstwa dla jeńców.","zioła");
                else if(!Arrays.asList("done","recommended").contains(v))add("give_herbs","Przynoszę zioła i lekarstwa dla jeńców.","zioła");break;
            case "King Cozart":if("recommended".equals(value("imperial_princess")))add("give_message","Przychodzę z polecenia księżniczki Ylflii.","polecenie księżniczki");break;
            case "Lorenz":v=value("jailedbarb");
                quest(new JailedBarbarian(),"Czy mogę ci pomóc wydostać się z więzienia?","zadanie");
                if("start".equals(v))add("give_kosa","Przynoszę kosę do wycięcia kwiatów.","kosa");
                if("princess".equals(v))add("tunnel","Esclara opowiedziała mi o tunelu. Co się stało?","tunel");
                if("jajo".equals(v))add("give_jajo","Przynoszę jajo, o które prosiłeś.","jajo");
                if("spoken".equals(v))add("give_message","Ylflia przesyła ci pozdrowienia.","pozdrowienia");
                if("armor".equals(v))add("give_armor","Przynoszę zbroję barbarzyńcy do ucieczki.","armor");break;
            case "Hunel":if(!p.isQuestCompleted("jailed_dwarf"))add("give_key","Chcę pomóc otworzyć twoją celę.","klucz hunela");break;
            case "Ivan Abe":v=value("crown_for_the_wannabe_king");
                if(v.isEmpty()||"rejected".equals(v))info("quest","Jak ma wyglądać twoja korona?","korony");
                else if(!Arrays.asList("done","reward").contains(v))add("supplies","Chcę przekazać materiały na koronę.","przedmioty");break;
            case "Kendra Mattori":if("reward".equals(value("crown_for_the_wannabe_king")))add("give_message","Ivan Abe przysyła mnie po obiecaną nagrodę.","nagroda");break;
            case "Henry":quest(new KanmararnSoldiers(),"Pomogę odszukać twoich towarzyszy.","zadanie");
                if(Arrays.asList("start","map").contains(value("soldier_henry")))add("supplies","Przychodzę z wieściami o twojej drużynie.","wieści żołnierzy");break;
            case "Sergeant James":if("map".equals(value("soldier_henry")))add("give_map","Przynoszę mapę od Henry'ego.","mapa");else if(!p.hasQuest("soldier_henry"))info("quest","Co stało się z twoją drużyną?","zadanie");break;
            case "Carena":quest(new FindGhosts(),"Pomogę ci odnaleźć inne duchy.","zadanie");if(value("find_ghosts").contains(":"))add("supplies","Chcę opowiedzieć o napotkanych duchach.","wieści duchów");break;
            case "Wrviliza":quest(new KoboldishTorcibud(),"Czy potrzebujesz składników do kolejnej nalewki?","zadanie");if(value("koboldish_torcibud").contains("="))add("supplies","Przynoszę składniki do nalewki.","składnik");break;
            case "Hogart":quest(new VampireSword(),"Chcę dowiedzieć się, jak zdobyć krwiopijcę.","zadanie");if("start".equals(value("vs_quest"))||value("vs_quest").startsWith("forging;"))add("supplies",value("vs_quest").startsWith("forging;")?"Czy krwiopijca jest gotowy?":"Co jeszcze muszę przygotować do wykucia krwiopijcy?","praca hogarta");break;
            case "Markovich":if(p.hasQuest("sicky_fill_goblet")&&!p.isQuestCompleted("sicky_fill_goblet"))add("supplies","Chcę odebrać napełnioną czarę.","odbiór czary");else if("start".equals(value("vs_quest"))&&!p.isEquipped("czara"))info("fill","Chcę zlecić napełnienie czary dla Hogarta.","napełnij");break;
            case "Ognir":add("give_ring","Chcę porozmawiać o naprawie pierścienia szmaragdowego.","szmaragd");break;
            case "Orc Saman":quest(new ClubOfThorns(),"Jak mogę pomóc orkom Kotoch?","zadanie");if(value("club_thorns").startsWith("start"))add("supplies","Przychodzę w sprawie królowej pająków.","zabity");break;
            case "Żywioł Ognia":craft(new ElementalDagger1(),"materiały ognia");break;
            case "Żywioł Wody":craft(new ElementalDagger2(),"materiały wody");break;
            case "Ludwik":craft(new Ciupagaz1Wasem(),"materiały ludwika");break;
            case "Tajemniczy Wędrowiec":craft(new Podroznik(),"materiały wędrowca");break;
            case "Adalbert":quest(new DeadWife(),"Chcę wysłuchać twojej historii.","zadanie");if(value("dead_wife").contains("="))add("supplies","Przynoszę pamiątki po twojej żonie.","prosiłem");else if(available(new DeadWife())&&!"root".equals(NpcDialogueService.branch(npc,p)))info("help_wife","Pomogę odzyskać rzeczy twojej żony.","pomóc");break;
            case "Santiago":if(value("fishermans_license2").isEmpty()&&p.hasQuest("fishermans_license2")||value("fishermans_license2").contains(";"))add("supplies","Chcę przekazać ryby do części praktycznej.","rodzaju");else if(!p.isQuestCompleted("fishermans_license2"))info("quest","Chcę omówić egzamin na kartę wędkarską.","egzamin");break;
            default:break;
        }
    }
    void craft(IQuest q,String command){
        if(available(q))quest(q,"Jakie materiały będą potrzebne?","zadanie");
        else if(!q.isCompleted(p))add("supplies",value(q.getSlotName()).startsWith("forging;")?"Czy moje zamówienie jest już gotowe?":"Przyniosłem materiały. Sprawdzisz, czego jeszcze brakuje?",command);
    }
}
