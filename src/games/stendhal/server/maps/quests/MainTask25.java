package games.stendhal.server.maps.quests;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.quest.*;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.player.Player;
import java.util.*;
import static games.stendhal.server.entity.npc.ConversationStates.*;

/** Preparation, three individually recorded discoveries, report, then a single fang. */
public class MainTask25 extends AbstractQuest implements MultiStepMasterQuest {
    public String getSlotName(){return ExpeditionXXV.MASTER;}
    public String getName(){return "Główne zadanie XXV - Ślady ostatniej wyprawy";}
    public String getNPCName(){return MasterStage.NPC_NAME;}
    public int getMinLevel(){return 320;}
    public List<String> getHistory(Player p){return p.hasQuest(getSlotName())?Arrays.asList(masterSummary(p)):Collections.<String>emptyList();}
    public String masterItem(Player p){return ExpeditionXXV.state(p,getSlotName()).equals("fang")?ExpeditionXXV.FANG:"";}
    public int masterItemAmount(Player p){return masterItem(p).isEmpty()?0:1;}
    public String masterSummary(Player p){
        String state=ExpeditionXXV.state(p,getSlotName());
        if(p.isQuestCompleted(getSlotName()))return "Próba XXV jest ukończona. Dzięki twoim meldunkom znamy zagrożenia na trasie, a okucia pomogą przygotować następną wyprawę.";
        if(!ExpeditionXXV.eligible(p))return "Próba XXV jest dostępna po ukończeniu XXIV i osiągnięciu poziomu 320. Wróć, gdy spełnisz oba warunki.";
        if(ExpeditionXXV.newQuest(p,getSlotName()))return "Etap XXV - Ślady ostatniej wyprawy. Najpierw przygotujesz sprzęt z Thyronem i Hogartem oraz wykonasz krótki zwiad Faga. Potem zbadamy trzy ślady, a na koniec przyniesiesz 1 kieł złotej kostuchy. Nagroda: odznaka XXV (2 ATK / 34 DEF) i "+MasterStage.rewardXP(320)+" EXP.";
        if(state.equals("preparing"))return "Zanim wskażę miejsca do zbadania, potrzebuję dwóch potwierdzeń. Zwiad Faga: "+(p.isQuestCompleted(ExpeditionXXV.HUNT)?"ukończony":"do wykonania")+". Okucia dostarczone Thyronowi: "+(p.isQuestCompleted(ExpeditionXXV.FITTINGS)?"tak":"jeszcze nie")+". Fag czeka przy pustynnej piramidzie, Thyron przy barierze w Semos; Hogart pracuje w kuźni kopalni krasnoludów Orril.";
        if(state.matches("survey;[0-7]"))return "Rozpoznanie trasy: "+ExpeditionXXV.surveyed(p)+"/3. "+ExpeditionXXV.route()+" Po trzech odkryciach wróć i zdaj mi raport.";
        if(state.equals("fang"))return "Raport jest kompletny. Przynieś 1 kieł złotej kostuchy, aby zamknąć próbę; może pochodzić z wcześniejszej wyprawy albo handlu. W zamian otrzymasz odznakę XXV (2 ATK / 34 DEF) i "+MasterStage.rewardXP(320)+" EXP; zostaw jedno wolne miejsce w plecaku.";
        return "Nie można teraz kontynuować próby XXV. Zachowaj przedmioty i zgłoś problem administracji.";
    }
    public void addToWorld(){
        fillQuestInfo(getName(),"Przygotuj wyprawę, zbadaj trzy ślady i zamknij próbę kłem złotej kostuchy.",false);
        MasterStage n=MasterStage.register(25,this);
        n.add(ATTENDING,ConversationPhrases.QUEST_MESSAGES,(p,s,e)->ExpeditionXXV.eligible(p)&&ExpeditionXXV.newQuest(p,getSlotName()),QUEST_OFFERED,
            "Sama siła nie wystarczy, kiedy wyprawa zostawia po sobie zniszczony sprzęt i urwane ślady. Najpierw pomożesz Fagowi oraz Thyronowi, potem sprawdzisz drogę, a kieł złotej kostuchy domknie próbę. Podejmujesz się tego zadania?",null);
        n.add(QUEST_OFFERED,ConversationPhrases.YES_MESSAGES,(p,s,e)->ExpeditionXXV.eligible(p)&&ExpeditionXXV.newQuest(p,getSlotName()),ATTENDING,null,(p,s,e)->{p.setQuest(getSlotName(),"preparing");e.say(masterSummary(p));});
        n.add(QUEST_OFFERED,ConversationPhrases.NO_MESSAGES,null,ATTENDING,"Przygotuj się i wróć, gdy zechcesz podjąć tę próbę.",(p,s,e)->p.setQuest(getSlotName(),"rejected"));
        n.add(ATTENDING,Arrays.asList("zadanie","meldunek wyprawy"),(p,s,e)->ExpeditionXXV.eligible(p)&&!ExpeditionXXV.newQuest(p,getSlotName()),ATTENDING,null,(p,s,e)->{
            String state=ExpeditionXXV.state(p,getSlotName());
            if(state.equals("preparing")&&ExpeditionXXV.prepared(p)){
                p.setQuest(getSlotName(),"survey;0");e.say("Fag potwierdził zwiad, a Thyron odebrał sprzęt. Teraz zbadaj trzy ślady poprzedniej wyprawy. Możesz odwiedzić je w dowolnej kolejności. "+ExpeditionXXV.route());
            }else if(ExpeditionXXV.surveying(p)&&ExpeditionXXV.surveyed(p)==3){
                p.setQuest(getSlotName(),"fang");e.say("Wypalona lampa wyjaśnia utratę światła, zarysowana tablica wskazuje uszkodzone oznaczenie drogi, a porzucone kotwy pokazują słabe mocowania. Wiemy już, gdzie wykorzystać okucia przygotowane przez Hogarta. Rozpoznanie jest zakończone. "+masterSummary(p));
            }else e.say(masterSummary(p));
        });
        n.add(ATTENDING,"oddaj kieł",(p,s,e)->ExpeditionXXV.eligible(p)&&ExpeditionXXV.prepared(p)&&ExpeditionXXV.state(p,getSlotName()).equals("fang"),ATTENDING,null,(p,s,e)->{
            if(!p.isEquipped(ExpeditionXXV.FANG,1)){e.say("Nie masz przy sobie kła złotej kostuchy. Przygotowania są zakończone, a raport mam już u siebie. Wróć z jednym kłem, a zakończymy próbę.");return;}
            Item badge=SingletonRepository.getEntityManager().getItem(ExpeditionXXV.BADGE);
            if(badge==null){e.say("Nie mogę teraz wydać odznaki. Zachowaj kieł i zgłoś problem administracji.");return;}
            badge.setBoundTo(p.getName());
            if(!p.equipToInventoryOnly(badge)){e.say("Zwolnij jedno miejsce w plecaku na odznakę. Zachowujesz kieł; nagroda zaczeka do kolejnego odbioru.");return;}
            if(!p.drop(ExpeditionXXV.FANG,1)){p.drop(badge);return;}
            p.setQuest(getSlotName(),"done");e.say("Znasz przeciwników, zadbałeś o przygotowanie wyprawy i sprawdziłeś jej drogę. Ten kieł domyka próbę. Otrzymujesz odznakę wojownika XXV (2 ATK / 34 DEF) oraz "+MasterStage.rewardXP(320)+" EXP.");
            // MasterStage awards the fixed XP once, only on this transition to done.
            p.notifyWorldAboutChanges();
        });
    }
}
