package games.stendhal.server.core.rp.quest;

import games.stendhal.server.core.engine.*;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.player.Player;
import java.util.*;
import marauroa.common.Pair;
import marauroa.common.game.*;

/** Authoritative saved phases of the expedition. Reading never accepts or completes it. */
public final class ExpeditionXXV {
    public static final String MASTER="main_task_25", HUNT="fag_death_scout", FITTINGS="thyron_expedition_fittings";
    public static final String PACKAGE="paczka okuć Thyrona", FANG="kieł złotej kostuchy", BADGE="odznaka wojownika XXV";
    public static final String[] MATERIALS={"żelazo","polano","sztabka mithrilu"};
    public static final int[] AMOUNTS={100,200,30};
    public static final String[] ZONES={"-1_nalwor_caves_w","-1_ados_caves_e","-1_orril_river_cave"};
    public static final String[] PLACES={"Pęknięta lampa zwiadowcy","Zarysowana tablica drogi","Porzucone kotwy wyprawy"};
    public static final String[] SPRITES={"lamp","tablet","anchors"};
    public static final int[][] COORDS={{64,56},{64,78},{71,49}};
    public static final String[] FINDINGS={
        "W osłonie lampy zachowała się sadza, a knot jest wypalony do końca. Ktoś próbował utrzymać światło mimo walki z czarnymi śmierciami. Zapisujesz stan przejścia dla Mistrza.",
        "Na tablicy pozostały ślady ostrza i urwany znak kierunku. Wyprawa musiała zwolnić tutaj przed wejściem w rejon srebrnych śmierci. Odtwarzasz oznaczenie drogi w swoim raporcie.",
        "Kotwy leżą obok wejścia do jaskini wielkich kostuch. Część mocowań puściła, lecz skała przy schodach nadal nadaje się do zabezpieczenia. Raport wyjaśni, gdzie wykorzystać przygotowane okucia."
    };
    private ExpeditionXXV(){}
    public static boolean eligible(Player p){return p.getLevel()>=320&&p.isQuestCompleted("main_task_24");}
    public static String state(Player p,String slot){String v=p.getQuest(slot);return v==null?"":v;}
    public static boolean newQuest(Player p,String slot){String v=state(p,slot);return v.isEmpty()||v.equals("rejected");}
    public static boolean prepared(Player p){return p.isQuestCompleted(HUNT)&&p.isQuestCompleted(FITTINGS);}
    public static boolean surveying(Player p){return eligible(p)&&prepared(p)&&state(p,MASTER).matches("survey;[0-7]");}
    public static int surveyMask(Player p){String s=state(p,MASTER);return s.matches("survey;[0-7]")?Integer.parseInt(s.substring(7)):0;}
    public static int surveyed(Player p){return Integer.bitCount(surveyMask(p));}
    public static String route(){return "Lampa: zachodnie jaskinie Nalwor, pole 64,56. Tablica: wschodnie jaskinie Ados, pole 64,78; wejście schodami na polu 56,95 pod północno-zachodnią górą Ados. Kotwy: jaskinia rzeki Orril, przy schodach na polu 71,49. Podejdź do każdego śladu i wybierz Zbadaj.";}
    public static Map<String,Pair<Integer,Integer>> targets(){
        Map<String,Pair<Integer,Integer>> m=new LinkedHashMap<String,Pair<Integer,Integer>>();
        String[] names={"czarna śmierć","srebrna śmierć","złota śmierć","kostucha wielka","kostucha różowa wielka","kostucha złota wielka"};
        int[] amounts={5,3,1,3,3,1};for(int i=0;i<names.length;i++)m.put(names[i],new Pair<Integer,Integer>(0,amounts[i]));
        return m;
    }
    public static Map<String,int[]> kills(Player p){
        Map<String,int[]> r=new LinkedHashMap<String,int[]>();String v=p.getQuest(HUNT,1);
        if(!state(p,HUNT).startsWith("start;")||v==null)return r;
        String[] a=v.split(",");if(a.length!=30)return r;
        try{for(int i=0;i<a.length;i+=5){
            Pair<Integer,Integer> expected=targets().get(a[i]);int required=Integer.parseInt(a[i+2]);
            int soloBase=Integer.parseInt(a[i+3]),sharedBase=Integer.parseInt(a[i+4]);
            if(expected==null||r.containsKey(a[i])||Integer.parseInt(a[i+1])!=0||required!=expected.second()||soloBase<0||sharedBase<0)throw new IllegalArgumentException("Invalid scout baseline");
            long got=Math.max(0L,(long)p.getSoloKill(a[i])+p.getSharedKill(a[i])-soloBase-sharedBase);
            r.put(a[i],new int[]{(int)Math.min(required,got),required});
        }}catch(RuntimeException e){r.clear();}
        return r;
    }
    public static String huntSummary(Player p){
        if(p.isQuestCompleted(HUNT))return "Krótki zwiad masz ukończony. Twój meldunek trafi do Mistrza; nie musisz ponownie pokonywać tych przeciwników.";
        StringBuilder b=new StringBuilder("Zwiad przed wyprawą: ");Map<String,int[]> progress=kills(p);
        if(!newQuest(p,HUNT)&&progress.size()!=6)return "Nie można teraz ustalić postępu zwiadu. Zgłoś problem administracji.";
        for(Map.Entry<String,Pair<Integer,Integer>> e:targets().entrySet()){int[] count=progress.get(e.getKey());b.append(e.getKey()).append(" ").append(count==null?0:count[0]).append('/').append(e.getValue().second()).append("; ");}
        return b.append("Liczą się walki po przyjęciu tego zwiadu. Moja wcześniejsza, większa próba pozostaje osobnym zadaniem.").toString();
    }
    public static int[] remaining(Player p){
        String s=state(p,FITTINGS);if(!s.matches("collect;[0-9]+;[0-9]+;[0-9]+"))return null;
        String[] a=s.split(";");int[] r=new int[3];try{for(int i=0;i<3;i++){r[i]=Integer.parseInt(a[i+1]);if(r[i]<0||r[i]>AMOUNTS[i])return null;}}catch(NumberFormatException e){return null;}return r;
    }
    public static String fittingsSummary(Player p){
        String s=state(p,FITTINGS);int[] left=remaining(p);
        if(left!=null){StringBuilder b=new StringBuilder("Hogart potrzebuje jeszcze: ");for(int i=0;i<3;i++)b.append(MATERIALS[i]).append(" - ").append(left[i]).append(" (masz ").append(p.getNumberOfEquipped(MATERIALS[i])).append("); ");return b.append("Dostawy przyjmie partiami. Po komplecie odbierz paczkę i dostarcz ją Thyronowi w Semos.").toString();}
        if(s.equals("ready"))return "Hogart ma komplet materiałów i przygotował okucia. Zostaw jedno wolne miejsce w plecaku, odbierz paczkę i zanieś ją Thyronowi.";
        if(s.equals("issued"))return "Paczka okuć została wydana. Zanieś ją Thyronowi przy barierze w Semos; samo dostarczenie materiałów do Hogarta nie kończy tego zadania.";
        if(p.isQuestCompleted(FITTINGS))return "Thyron odebrał okucia. Przygotowanie wyprawy jest zakończone.";
        if(!newQuest(p,FITTINGS))return "Nie można teraz sprawdzić zamówienia okuć. Zachowaj pozostałe materiały i zgłoś problem administracji.";
        return "Thyron zamawia okucia u Hogarta, kowala w kopalniach krasnoludów Orril. Potrzeba 100 żelaza, 200 polan i 30 sztabek mithrilu. Przyjmij zamówienie u Thyrona, a materiały możesz dostarczać Hogartowi partiami.";
    }
    public static String fittingsDialogue(Player p,String speaker){
        String s=state(p,FITTINGS);String text=fittingsSummary(p);
        if("Hogart".equals(speaker)){
            if(remaining(p)!=null)return text.replace("Hogart potrzebuje jeszcze:","Do wykucia okuć potrzebuję jeszcze:").replace("Dostawy przyjmie partiami.","Surowce możesz przynosić partiami.");
            if(s.equals("ready"))return "Mam już komplet materiałów i przygotowałem okucia. Zostaw jedno wolne miejsce w plecaku, odbierz ode mnie paczkę i zanieś ją Thyronowi w Semos.";
            if(s.equals("issued"))return "Wydałem ci oznaczoną paczkę okuć. Zanieś ją Thyronowi przy barierze w Semos, a on potwierdzi odbiór; jeśli ją utracisz, sprawdzę możliwość odtworzenia zamówienia.";
            if(p.isQuestCompleted(FITTINGS))return "Thyron potwierdził odbiór moich okuć. Oby dobrze trzymały w skale; z solidnym mocowaniem wyprawa ma znacznie większą szansę wrócić w komplecie.";
            if(newQuest(p,FITTINGS))return "Thyron potrzebuje okuć do zabezpieczenia starych przejść. Mogę je wykuć ze 100 żelaza, 200 polan i 30 sztabek mithrilu, ale najpierw uzgodnij z nim zamówienie przy barierze w Semos.";
        }else if("Thyron".equals(speaker)){
            if(s.equals("issued"))return "Hogart potwierdził, że wydał ci paczkę. Przekaż mi oznaczone okucia, a sprawdzę komplet i zamknę przygotowania dla Mistrza.";
            if(p.isQuestCompleted(FITTINGS))return "Okucia dotarły i są gotowe na wyprawę. Mistrz ma już moje potwierdzenie; nie musisz ponownie przynosić materiałów ani paczki.";
            if(newQuest(p,FITTINGS))return "Stare przejścia potrzebują nowych mocowań, zanim wyruszy kolejna wyprawa. Hogart w kuźni kopalni krasnoludów Orril wykona je ze 100 żelaza, 200 polan i 30 sztabek mithrilu. Zależy mi na dostarczeniu gotowych okuć, a nie tylko zebraniu surowców.";
        }
        return text;
    }
    public static void deliverMaterial(Player p,int index,EventRaiser n){
        int[] left=remaining(p);if(!eligible(p)||left==null||index<0||index>=3)return;
        int take=Math.min(left[index],p.getNumberOfEquipped(MATERIALS[index]));
        if(take==0){n.say("Nie mam czego dopisać do tej dostawy. "+fittingsSummary(p));return;}
        if(!p.drop(MATERIALS[index],take)){n.say("Nie udało się przekazać materiału. Sprawdź plecak i spróbuj ponownie.");return;}
        left[index]-=take;p.setQuest(FITTINGS,left[0]+left[1]+left[2]==0?"ready":"collect;"+left[0]+";"+left[1]+";"+left[2]);
        n.say("Przyjmuję: "+MATERIALS[index]+" - "+take+". "+fittingsDialogue(p,"Hogart"));p.notifyWorldAboutChanges();
    }
    public static String packageMark(Player p){return FITTINGS+":"+p.getName();}
    private static boolean marked(RPObject o,Player p){return o instanceof Item&&PACKAGE.equals(((Item)o).getName())&&p.getName().equals(((Item)o).getBoundTo())&&packageMark(p).equals(((Item)o).getInfoString());}
    private static boolean containsPackage(RPObject o,Player p){if(marked(o,p))return true;for(RPSlot s:o.slots())for(RPObject child:s)if(containsPackage(child,p))return true;return false;}
    public static boolean packageExists(Player p){
        if(containsPackage(p,p))return true;
        // Bound parcels can be put down, or stored inside a ground container. Do not clone them.
        for(IRPZone z:StendhalRPWorld.get())for(RPObject o:z)if(containsPackage(o,p))return true;
        return false;
    }
    public static Item carriedPackage(Player p){for(Item i:p.getAllEquipped(PACKAGE))if(marked(i,p))return i;return null;}
    public static void claimPackage(Player p,EventRaiser n){
        String s=state(p,FITTINGS);if(!eligible(p)||!(s.equals("ready")||s.equals("issued")))return;
        if(packageExists(p)){n.say("Twoja paczka już istnieje. Sprawdź plecak, magazyny i miejsce, gdzie została odłożona; nie wydam drugiego egzemplarza.");return;}
        Item item=SingletonRepository.getEntityManager().getItem(PACKAGE);
        if(item==null){n.say("Nie mogę teraz wydać paczki. Nie przynoś ponownie materiałów; zgłoś problem administracji.");return;}
        item.setBoundTo(p.getName());item.setInfoString(packageMark(p));item.put("undroppableondeath",1);
        if(!p.equipToInventoryOnly(item)){n.say("Zwolnij jedno miejsce w plecaku. Gotowe okucia zaczekają u mnie i nie trafią na ziemię.");return;}
        p.setQuest(FITTINGS,"issued");n.say("Oto gotowa paczka okuć. Zanieś ją Thyronowi przy barierze w Semos; on sprawdzi dostawę i powiadomi Mistrza.");p.notifyWorldAboutChanges();
    }
    public static void handOverPackage(Player p,EventRaiser n){
        if(!eligible(p)||!state(p,FITTINGS).equals("issued"))return;Item item=carriedPackage(p);
        if(item==null){n.say("Potrzebuję paczki okuć, którą Hogart przygotował właśnie dla ciebie. Odbierz ją od kowala i przynieś w plecaku.");return;}
        if(!p.drop(item))return;p.setQuest(FITTINGS,"done");
        n.say("Okucia dotarły w komplecie. Dzięki nim następna wyprawa zabezpieczy stare przejścia, zamiast ponownie tracić sprzęt. Twoja część przygotowań jest ukończona; Mistrz sprawdzi jeszcze meldunek Faga.");p.notifyWorldAboutChanges();
    }
}
