package games.stendhal.server.entity.npc.dialogue;
import games.stendhal.common.QuestProgress;
import games.stendhal.server.core.rp.quest.*;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.*;
import games.stendhal.server.entity.npc.dialogue.PilotDialogueReplies.Choice;
import java.util.*;

/** Continuations follow the native state and the saved stage, never the full topic pool. */
final class CollectionDialogueReplies {
    final SpeakerNPC npc;final Player p;final String name,last;final Map<String,Choice> choices=new LinkedHashMap<String,Choice>();
    CollectionDialogueReplies(SpeakerNPC n,Player p){npc=n;this.p=p;name=n.getName();last=NpcDialogueService.lastCommand(n,p);}
    static boolean supports(SpeakerNPC n){return !CollectionQuestProgress.forNpc(n.getName()).isEmpty();}
    static Map<String,Choice> read(SpeakerNPC n,Player p){CollectionDialogueReplies b=new CollectionDialogueReplies(n,p);b.build();return b.choices;}
    void add(String id,String label,String command){id=ResidentDialogueReplies.id(id);if(npc.getEngine().canHandle(p,command))choices.put(id,new Choice(id,label,command,command));}
    void info(String id,String label,String command){if(!last.equalsIgnoreCase(command))add(id,label,command);}
    String value(IQuest q){String s=p.getQuest(q.getSlotName());return s==null?"":s;}
    boolean available(IQuest q){return !p.hasQuest(q.getSlotName())||"rejected".equals(value(q));}
    boolean elapsed(IQuest q,int minutes){return new TimePassedCondition(q.getSlotName(),1,minutes).fire(p,null,npc);}
    void build(){
        ConversationStates state=npc.getEngine().getCurrentState();if(state==ConversationStates.IDLE)return;
        List<IQuest> quests=CollectionQuestProgress.forNpc(name);
        if("Lorithien".equals(name)){
            if(state==ConversationStates.ATTENDING&&Arrays.asList("start","lorithien").contains(p.getQuest("grafindle_gold")))info("gold_delivery","Przychodzę w sprawie przesyłki złota dla Grafindla.","przesyłka");
            choices.putAll(HuntDialogueReplies.read(npc,p));return;
        }
        if(state==ConversationStates.ATTENDING){
            for(IQuest q:quests){
                if(!CollectionQuestProgress.missingReward(q).isEmpty())continue;
                String v=value(q),k=CollectionQuestProgress.kind(q);boolean done=CollectionQuestProgress.done(q,p);QuestProgress progress=CollectionQuestProgress.read(q,p);
                if(progress!=null&&progress.getState()==QuestProgress.State.UNKNOWN)continue;
                if("random".equals(k)||"dailykill".equals(k)){
                    NativeCollectionDetails d=CollectionQuestProgress.details(q);
                    if(available(q)||done&&elapsed(q,d.getCollectionWait()))info("quest",done?"Mogę zająć się kolejnym zleceniem?":"Masz dla mnie jakieś zlecenie?","zadanie");
                    else if(done)info("cooldown","Kiedy otrzymam kolejne zlecenie?","zadanie");
                    else{
                        boolean ready=progress!=null&&progress.getState()==QuestProgress.State.READY_TO_DELIVER;
                        if(ready)add("deliver","dailykill".equals(k)?"Pokonałem wskazane potwory.":"Przyniosłem zamówione przedmioty.","zakończone");
                        else info("remind","dailykill".equals(k)?"Które potwory jeszcze zostały?":"Jakich przedmiotów mam szukać?","zadanie");
                        if(elapsed(q,d.getCollectionExpiry()))info("replace","Nie mogę dokończyć tego zlecenia. Proszę o inne.","inny");
                    }
                }else if("badge".equals(k)){
                    if(available(q))info("quest","Co muszę zrobić, żeby zostać mieszkańcem miasta?","zadanie");
                    else if(!done)add("supplies","Przychodzę z dostawą do urzędu.","dostawa");
                }else if("sequence".equals(k)){
                    if(available(q))info("quest","Czego potrzebujesz?","zadanie");
                    else if(!done)add("supplies",v.startsWith("forging;")?(q instanceof AndrzejCollector?"Czy udało ci się już ułożyć kolekcję?":progress!=null&&progress.getState()==QuestProgress.State.READY_TO_DELIVER?"Przychodzę odebrać gotowy wyrób.":"Jak idzie praca nad moim zamówieniem?"):q instanceof AndrzejCollector?"Przyniosłem kolejne trofea.":"Przyniosłem część materiałów.","dostawa");
                }else if("count".equals(k)){
                    if(available(q))info("quest","Co mogę dla ciebie przynieść?","zadanie");else if(!done)add("supplies","Mam kolejną sztukę dla ciebie.","dostawa");
                }else if("list".equals(k)){
                    if(q instanceof CloakCollector2){if(p.isQuestCompleted("cloaks_collector")&&!done)info("collection_two",available(q)?"Jakich płaszczy teraz szukasz?":"done".equals(v)?"Przychodzę odebrać buty zabójcy.":"Przyniosłem płaszcz do drugiej kolekcji.","druga kolekcja");}
                    else if(q instanceof WeaponsCollector2){if(p.isQuestCompleted("weapons_collector")){if(available(q))info("quest","Chcę poznać drugą listę broni.","zadanie");else if(!done)add("supplies","Chcę uzupełnić drugą kolekcję broni.","list");}}
                    else if(available(q))info("quest","Opowiedz mi o swojej kolekcji.","zadanie");
                    else if(!done)add("supplies","Przyniosłem coś do twojej kolekcji.",q instanceof CloakCollector?"płaszcze":q instanceof ToysCollector?"lista":"kolekcja");
                }else if(q instanceof DragonCave){if(available(q))info("quest","Chcę poznać pełną listę smoczych przedmiotów.","przedmiotów");else if(!done)add("supplies","Przynoszę smocze przedmioty do kolekcji.","rzeczy");}
                else if(q instanceof TakeGoldforGrafindle){if(available(q))info("quest","Mogę przewieźć złoto do banku. Jakie są warunki?","złota");else if("start".equals(v))info("gold_where","Gdzie odbiorę przesyłkę złota?","złoto");else if(!done)add("gold_delivery","Dostarczam przesyłkę złota od Lorithien.","przesyłka");}
                else if(q instanceof SuppliesForPhalk){
                    if("Phalk".equals(name)){
                        if(available(q))info("quest","Czy potrzebujesz zapasów na posterunek?","zadanie");
                        else if("start".equals(v))add("supplies","Przynoszę zamówione jedzenie i napoje.","jedzenie");
                        else if(v.startsWith("clothes;"))add("supplies","Przynoszę ubrania oznaczone twoim imieniem.","ubrania");
                    }else if(v.startsWith("clothes;")){
                        int index="Wrvil".equals(name)?1:2;String item=index==1?"płaszcz krasnoludzki":"zbroja krasnoludzka";
                        if("none".equals(p.getQuest(q.getSlotName(),index)))info("phalk","Przychodzę po zamówienie dla Phalka.","zapłatę");
                        else if(JourneyQuestProgress.personalItemCount(p,item,"Phalk")==0)info("phalk_replace","Zgubiłem zamówienie Phalka. Chcę kupić zastępstwo za 10000 monet.","Phalk");
                    }
                }
            }
            if(!last.equals("warunki dostawy"))info("hints","Czego jeszcze brakuje i co mam zrobić dalej?","warunki dostawy");
            info("job","Opowiedz mi o sobie.","praca");info("offer","Pokaż mi swoją ofertę.","oferta");
            if(!"root".equals(NpcDialogueService.branch(npc,p)))add("back","Porozmawiajmy o czymś innym.","wątki");
        }else if(state==ConversationStates.QUESTION_1||state==ConversationStates.QUESTION_2){
            boolean handin=false;
            for(IQuest q:quests){String k=CollectionQuestProgress.kind(q);if(available(q)||CollectionQuestProgress.done(q,p))continue;
                if("list".equals(k)||"collection".equals(k)){
                    handin=true;try{for(String item:CollectionQuestProgress.remaining(q,p).keySet())if(p.isEquipped(item))add("give_"+item,"Przekazuję: "+item+".",item);}catch(IllegalArgumentException ex){ }
                }
                if("count".equals(k)){handin=true;String item=CollectionQuestProgress.details(q).getCollectionItems().keySet().iterator().next();add("give_"+item,"Przekazuję jedną sztukę: "+item+".","tak");}
            }
            if(handin)add("later","Resztę przyniosę później.","nie");else confirmations(state);
        }else confirmations(state);
        add("goodbye","Do zobaczenia.","bye");
    }
    void confirmations(ConversationStates state){
        String yes=state==ConversationStates.QUEST_OFFERED||state==ConversationStates.QUEST_2_OFFERED?"Tak, pomogę ci.":"Tak, przyniosłem potrzebne rzeczy.";
        if(state==ConversationStates.BUY_PRICE_OFFERED)yes="Tak, kupuję.";if(state==ConversationStates.SELL_PRICE_OFFERED)yes="Tak, sprzedaję.";
        add(state==ConversationStates.QUEST_OFFERED||state==ConversationStates.QUEST_2_OFFERED?"accept":"confirm",yes,"tak");
        add("cancel","Nie teraz. Wrócę do tego później.","nie");
    }
}
