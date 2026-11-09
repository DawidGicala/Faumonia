package games.stendhal.server.core.rp.quest;

import games.stendhal.common.QuestProgress;
import games.stendhal.common.QuestProgress.Objective;
import games.stendhal.common.QuestProgress.State;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.*;
import java.util.*;

/** Read-only interpretation of the original journey records; never migrates a save. */
public final class JourneyQuestProgress {
    private JourneyQuestProgress() { }
    public static List<IQuest> forNpc(String name) {
        List<IQuest> q=new ArrayList<IQuest>();
        switch(name){
            case "Ceryl":q.add(new LookBookforCeryl());q.add(new ObsidianKnife());break;
            case "Jynath":q.add(new LookBookforCeryl());break;
            case "Pequod":q.add(new LookUpQuote());break;
            case "Samaron":case "Woy'tyk":case "Leander":case "Ylrik":q.add(new MagicStaff());break;
            case "Alrak":q.add(new ObsidianKnife());break;
            case "Princess Esclara":q.add(new AmazonPrincess());q.add(new JailedBarbarian());break;
            case "Tywysoga":case "Róża Kwiaciarka":q.add(new ElfPrincess());break;
            case "Princess Ylflia":q.add(new ImperialPrincess());q.add(new JailedBarbarian());break;
            case "King Cozart":q.add(new ImperialPrincess());break;
            case "Lorenz":q.add(new JailedBarbarian());break;
            case "Hunel":q.add(new JailedDwarf());break;
            case "Ivan Abe":case "Kendra Mattori":q.add(new CrownForTheWannaBeKing());break;
            case "Henry":case "Sergeant James":q.add(new KanmararnSoldiers());break;
            case "Carena":case "Mary":case "Ben":case "Zak":case "Goran":q.add(new FindGhosts());break;
            case "Wrviliza":q.add(new KoboldishTorcibud());break;
            case "Hogart":case "Markovich":q.add(new VampireSword());break;
            case "Ognir":q.add(new RingMaker());break;
            case "Orc Saman":q.add(new ClubOfThorns());break;
            case "Żywioł Ognia":q.add(new ElementalDagger1());break;
            case "Żywioł Wody":q.add(new ElementalDagger2());break;
            case "Ludwik":q.add(new Ciupagaz1Wasem());break;
            case "Adalbert":q.add(new DeadWife());break;
            case "Tajemniczy Wędrowiec":q.add(new Podroznik());break;
            case "Santiago":q.add(new FishermansLicenseQuiz());q.add(new FishermansLicenseCollector());break;
            default:
                if(FindDragons.getMeetingNames().contains(name))q.add(new FindDragons());
                if(SevenCherubs.getMeetingNames().contains(name))q.add(new SevenCherubs());
        }
        return q;
    }
    public static boolean supports(IQuest q){
        return q instanceof LookBookforCeryl||q instanceof LookUpQuote||q instanceof MagicStaff||q instanceof ObsidianKnife
            ||q instanceof AmazonPrincess||q instanceof ElfPrincess||q instanceof ImperialPrincess||q instanceof JailedBarbarian
            ||q instanceof JailedDwarf||q instanceof CrownForTheWannaBeKing||q instanceof KanmararnSoldiers||q instanceof FindDragons
            ||q instanceof FindGhosts||q instanceof SevenCherubs||q instanceof KoboldishTorcibud||q instanceof VampireSword
            ||q instanceof RingMaker||q instanceof ClubOfThorns||q instanceof ElementalDagger1||q instanceof ElementalDagger2
            ||q instanceof Ciupagaz1Wasem||q instanceof DeadWife||q instanceof Podroznik||q instanceof FishermansLicenseQuiz||q instanceof FishermansLicenseCollector;
    }
    public static boolean sequential(IQuest q){return q instanceof ElementalDagger1||q instanceof ElementalDagger2||q instanceof Ciupagaz1Wasem||q instanceof Podroznik;}
    public static Map<String,Integer> requirements(IQuest q){
        if(q instanceof ElementalDagger1)return ElementalDagger1.getRequiredItems();
        if(q instanceof ElementalDagger2)return ElementalDagger2.getRequiredItems();
        if(q instanceof Ciupagaz1Wasem)return Ciupagaz1Wasem.getRequiredItems();
        if(q instanceof Podroznik)return Podroznik.getRequiredItems();
        if(q instanceof CrownForTheWannaBeKing)return CrownForTheWannaBeKing.getRequiredItems();
        if(q instanceof DeadWife)return DeadWife.getRequiredItems();
        return Collections.emptyMap();
    }
    public static Map<String,Integer> items(Object...pairs){
        Map<String,Integer> m=new LinkedHashMap<String,Integer>();for(int i=0;i<pairs.length;i+=2)m.put((String)pairs[i],(Integer)pairs[i+1]);return m;
    }
    public static String requirementsText(IQuest q){
        StringBuilder text=new StringBuilder();
        for(Map.Entry<String,Integer> e:requirements(q).entrySet()){
            if(text.length()>0)text.append(", ");
            text.append("money".equals(e.getKey())?"monety":e.getKey()).append(" - ").append(e.getValue());
        }
        return text.toString();
    }
    public static int personalItemCount(Player p,String item,String owner){
        int count=0;for(games.stendhal.server.entity.item.Item i:p.getAllEquipped(item))if(owner.equalsIgnoreCase(i.getInfoString()))count++;return count;
    }
    public static Map<String,Integer> remaining(IQuest q,Player p){
        String v=p.getQuest(q.getSlotName());Map<String,Integer> all=requirements(q),r=new LinkedHashMap<String,Integer>();
        if(v==null||"rejected".equals(v))return all;
        if(q.isCompleted(p)||"reward".equals(v)||v.startsWith("forging"))return r;
        if(sequential(q)){
            String[] parts=v.split(";",-1);if(parts.length!=all.size()+1||!"start".equals(parts[0]))throw new IllegalArgumentException("Unknown sequence");
            int i=1;for(Map.Entry<String,Integer> e:all.entrySet()){
                int n=Integer.parseInt(parts[i++]);if(n<0||n>e.getValue())throw new IllegalArgumentException("Invalid count");
                if(n<e.getValue())r.put(e.getKey(),e.getValue()-n);
            }
        }else if(q instanceof CrownForTheWannaBeKing||q instanceof DeadWife||q instanceof KoboldishTorcibud){
            Set<String> allowed=q instanceof KoboldishTorcibud?new HashSet<String>(Arrays.asList("butla czwórniaczka","wąska butelka","mandragora","karczoch","arandula","sclaria","kekik","ekstrakt litworowy")):all.keySet();
            for(String part:v.split(";")){
                String[] e=part.split("=",-1);if(e.length!=2||!allowed.contains(e[0])||r.containsKey(e[0]))throw new IllegalArgumentException("Unknown collection");
                int n=Integer.parseInt(e[1]);if(n<1||(!all.isEmpty()&&n>all.get(e[0])))throw new IllegalArgumentException("Invalid count");r.put(e[0],n);
            }
        }else if(q instanceof FishermansLicenseCollector){
            Set<String> done=new HashSet<String>(Arrays.asList(v.split(";")));for(String f:FishermansLicenseCollector.getRequiredFish())if(!done.contains(f))r.put(f,1);
        }
        return r;
    }
    public static String kill(IQuest q){
        if(q instanceof ElementalDagger1)return "kobold olbrzymi";if(q instanceof ElementalDagger2)return "błękitny smok";
        if(q instanceof Ciupagaz1Wasem)return "serafin";if(q instanceof Podroznik)return "szczur olbrzymi";return "";
    }
    private static QuestProgress snap(IQuest q,State state,String npc,String hint,List<Objective> goals){return new QuestProgress(q.getName(),state,npc,hint,goals);}
    private static QuestProgress next(IQuest q,String npc,String hint){return snap(q,State.ACTIVE,npc,hint,Collections.<Objective>emptyList());}
    private static QuestProgress carry(IQuest q,Player p,String npc,String hint,Map<String,Integer> items){
        List<Objective> goals=new ArrayList<Objective>();boolean ready=true;
        for(Map.Entry<String,Integer> e:items.entrySet()){int count=p.getNumberOfEquipped(e.getKey());goals.add(new Objective(e.getKey(),e.getKey(),Math.min(count,e.getValue()),e.getValue(),-1));ready&=count>=e.getValue();}
        return snap(q,ready?State.READY_TO_DELIVER:State.ACTIVE,npc,hint,goals);
    }
    public static QuestProgress read(IQuest q,Player p){
        if(!supports(q)||!p.hasQuest(q.getSlotName()))return null;String v=p.getQuest(q.getSlotName()),step=v.split(";",-1)[0];
        if(q.isCompleted(p))return snap(q,State.COMPLETED,"","Zadanie ukończone.",Collections.<Objective>emptyList());
        if("rejected".equals(v))return snap(q,State.DECLINED,q.getNPCName(),"Możesz wrócić do rozmowy, jeśli zechcesz pomóc.",Collections.<Objective>emptyList());
        try{
            if(sequential(q)||q instanceof CrownForTheWannaBeKing||q instanceof DeadWife||q instanceof KoboldishTorcibud||q instanceof FishermansLicenseCollector){
                if("reward".equals(v)&&q instanceof CrownForTheWannaBeKing)return next(q,"Kendra Mattori","Idź do Kendry Mattori w magicznej świątyni i zapytaj o nagrodę za materiały na koronę.");
                if(step.startsWith("forging"))return next(q,q.getNPCName(),"Przygotowanie trwa. Wróć do rozmówcy i zapytaj o odbiór; nie dostarczaj materiałów ponownie.");
                if(q instanceof FishermansLicenseCollector&&!p.isQuestCompleted("fishermans_license1"))throw new IllegalArgumentException("Exam prerequisite");
                Map<String,Integer> left=remaining(q,p);List<Objective> goals=new ArrayList<Objective>();boolean ready=true;
                for(Map.Entry<String,Integer> e:left.entrySet()){int n=p.getNumberOfEquipped(e.getKey());goals.add(new Objective(e.getKey(),e.getKey(),0,e.getValue(),n));ready&=n>=e.getValue();}
                if(sequential(q)){boolean killed=p.hasKilled(kill(q));goals.add(new Objective("kill:"+kill(q),"Pokonaj: "+kill(q),killed?1:0,1,-1));ready&=killed;}
                return snap(q,ready?State.READY_TO_DELIVER:State.ACTIVE,q.getNPCName(),sequential(q)?"Dostarczaj brakujące materiały w podanej kolejności. Rzemieślnik rozpocznie pracę, gdy dostarczysz komplet i pokonasz wskazanego przeciwnika.":"Przekaż brakujące rzeczy. Możesz przynosić je partiami.",goals);
            }
            if(q instanceof LookBookforCeryl){
                if("start".equals(v))return next(q,"Jynath","Poproś Jynath w domu na południe od zamku Orril o zwrot czarnej księgi Ceryla.");
                if("jynath".equals(v))return carry(q,p,"Ceryl","Zanieś czarną księgę do Ceryla w bibliotece Semos.",items("księga czarna",1));
            }
            if(q instanceof LookUpQuote)return next(q,"Pequod","Odszukaj i podaj dokładny cytat rybaka: "+v+". Wpisz powiedzonko we własnej odpowiedzi; nie podawaj samego imienia autora.");
            if(q instanceof MagicStaff){
                if("start".equals(v))return next(q,"Woy'tyk","Porozmawiaj o różdżce z Woy'tykiem na wyspie Athor.");
                if("first".equals(v))return next(q,"Leander","Poproś Leandra w piekarni Semos o jedzenie dla mieszkańców Athor.");
                if("first1".equals(v))return p.isEquipped("paczka jedzenia")?carry(q,p,"Woy'tyk","Zanieś paczkę jedzenia Woy'tykowi na Athor.",items("paczka jedzenia",1)):carry(q,p,"Leander","Przynieś Leandrowi 60 porcji mięsa z kraba i odbierz paczkę jedzenia.",items("mięso z kraba",60));
                if("first2".equals(v))return carry(q,p,"Woy'tyk","Dostarcz komplet pomocy do nauki magii na Athor.",items("różdżka",10,"magia ziemi",2500,"magia płomieni",2000));
                if("gotfirstpiece".equals(v))return carry(q,p,"Ylrik","Pokaż Ylrikowi w Kirdneh uszkodzoną różdżkę.",items("uszkodzona różdżka",1));
                if("ptaszory".equals(v))return carry(q,p,"Ylrik","Dostarcz Ylrikowi 40 muchomorów do rozwiązania problemu z ptakami.",items("muchomor",40));
                if("powrot".equals(v))return carry(q,p,"Samaron","Wróć do Samarona w górach Ados z trzema częściami.",items("uszkodzona różdżka",1,"magiczny katalizator",1,"kryształ mocy",1));
                if("killing".equals(v))return carry(q,p,"Samaron","Zbierz cały zestaw do końcowego połączenia różdżki Mokoszy.",items("magia deszczu",1000,"magia mroku",500,"magia światła",250,"diament",1,"pazur zielonego smoka",1,"pazur czarnego smoka",3,"pazur złotego smoka",4,"różdżka Swaroga",1,"kryształ mocy",1));
            }
            if(q instanceof ObsidianKnife){
                if(Arrays.asList("szynka","mięso","ser").contains(v))return carry(q,p,"Alrak","Przynieś Alrakowi całą zamówioną dostawę jedzenia do kuźni Wofol.",items(v,100));
                if("food_brought".equals(v))return next(q,"Alrak","Zapytaj Alraka o książkę o kamieniach.");
                if("seeking_book".equals(v))return next(q,"Ceryl","W bibliotece Semos poproś o książkę o kamieniach i powiedz, że jest dla Alraka.");
                if("got_book".equals(v))return carry(q,p,"Alrak","Zanieś Alrakowi lazurową księgę.",items("księga lazurowa",1));
                if("reading".equals(step))return next(q,"Alrak","Alrak czyta księgę przez trzy dni. Po tym czasie wróć do jego kuźni.");
                if("book_read".equals(v))return next(q,"Alrak","Porozmawiaj z Alrakiem o obsydianowym nożu. Wymagany jest poziom wyższy niż 50.");
                if("knife_offered".equals(v))return carry(q,p,"Alrak","Pokonaj czarnego smoka i przynieś Alrakowi obsydian oraz dorsza. Wykucie potrwa 10 minut.",items("obsydian",1,"dorsz",1));
                if("forging".equals(step))return next(q,"Alrak","Odbierz obsydianowy saks po 10 minutach pracy.");
            }
            if(q instanceof AmazonPrincess&&"start".equals(v))return carry(q,p,"Princess Esclara","Zanieś jedną porcję napoju z oliwką księżniczce na wyspie Amazon.",items("napój z oliwką",1));
            if(q instanceof ElfPrincess){
                if("start".equals(step))return next(q,"Róża Kwiaciarka","Odszukaj wędrowną Różę Kwiaciarkę i odbierz orchideę dla Tywysogi. Między kolejnymi odbiorami musi minąć pięć minut.");
                if("got_flower".equals(step))return carry(q,p,"Tywysoga","Zanieś orchideę księżniczce na szczycie wieży Nalwor.",items("orchidea",1));
            }
            if(q instanceof ImperialPrincess){
                if("recommended".equals(v))return next(q,"King Cozart","Ylflia poleciła cię królowi. Porozmawiaj z Cozartem w zamku Kalavan i odbierz obywatelstwo.");
                int level=Integer.parseInt(v);if(level<0||level>100000)throw new IllegalArgumentException("Invalid level");
                return carry(q,p,"Princess Ylflia","Dostarcz cały zamówiony komplet. Nawet jeśli awansujesz, potrzebne ilości pozostaną takie jak przy przyjęciu prośby.",items("arandula",1+level/5,"eliksir",1+level/3,"antidotum",1+level/4,"kokuda",1,"sclaria",1,"kekik",1));
            }
            if(q instanceof JailedBarbarian){
                if("start".equals(v))return carry(q,p,"Lorenz","Przynieś Lorenzowi sprawną kosę, nie starą kosę.",items("kosa",1));
                if("capture".equals(v))return next(q,"Princess Esclara","Na wyspie Amazon zapytaj Esclarę o Lorenza i powód jego uwięzienia.");
                if("princess".equals(v))return next(q,"Lorenz","Wróć do Lorenza i zapytaj go o tunel.");
                if("jajo".equals(v))return carry(q,p,"Lorenz","Przynieś Lorenzowi jedno jajo.",items("jajo",1));
                if("jailed".equals(v))return next(q,"Princess Ylflia","Powiedz Ylflii w zamku Kalavan, że Lorenz jest uwięziony na Amazon.");
                if("spoken".equals(v))return next(q,"Lorenz","Przekaż Lorenzowi pozdrowienia od Ylflii.");
                if("armor".equals(v))return carry(q,p,"Lorenz","Dostarcz Lorenzowi zbroję barbarzyńcy do ucieczki.",items("zbroja barbarzyńcy",1));
            }
            if(q instanceof JailedDwarf&&"start".equals(v))return carry(q,p,"Hunel","Zdobądź klucz do więzienia Kanmararn i pokaż go Hunelowi.",items("klucz do więzienia Kanmararn",1));
            if(q instanceof KanmararnSoldiers){
                if("start".equals(v)||"map".equals(v)){
                    String[][] wanted="start".equals(v)?new String[][]{{"skórzane spodnie","tom"},{"zbroja łuskowa","peter"},{"karteczka","charles"}}:new String[][]{{"mapa","henry"}};
                    List<Objective> goals=new ArrayList<Objective>();boolean ready=true;
                    for(String[] item:wanted){int count=Math.min(1,personalItemCount(p,item[0],item[1]));ready&=count==1;goals.add(new Objective(item[0],item[0]+" ("+item[1]+")",count,1,-1));}
                    return snap(q,ready?State.READY_TO_DELIVER:State.ACTIVE,"start".equals(v)?"Henry":"Sergeant James","start".equals(v)?"Odszukaj osobiste rzeczy: spodnie Toma, zbroję Petera i karteczkę Charlesa. Zwykłe przedmioty o tych nazwach nie wystarczą.":"Zanieś mapę od Henry'ego sierżantowi Jamesowi. Karteczka Charlesa pozostaje przy tobie.",goals);
                }
            }
            if(q instanceof FindDragons||q instanceof SevenCherubs){
                Collection<String> names=q instanceof FindDragons?FindDragons.getMeetingNames():SevenCherubs.getMeetingNames();
                Set<String> seen=new HashSet<String>(Arrays.asList(v.split(";")));List<Objective> goals=new ArrayList<Objective>();
                for(String name:names)goals.add(new Objective("npc:"+name,name,seen.contains(name)?1:0,1,-1));
                return snap(q,State.ACTIVE,"",q instanceof FindDragons?FindDragons.getMeetingHint():SevenCherubs.getMeetingHint(),goals);
            }
            if(q instanceof FindGhosts){
                String[] halves=v.toLowerCase(Locale.ROOT).split(":",-1);if(halves.length!=2)throw new IllegalArgumentException("Unknown ghosts");
                Set<String> seen=new HashSet<String>(Arrays.asList(halves[0].split(";"))),said=new HashSet<String>(Arrays.asList(halves[1].split(";")));List<Objective> goals=new ArrayList<Objective>();
                for(String n:Arrays.asList("mary","ben","zak","goran"))goals.add(new Objective("npc:"+n,"Wieści: "+n,said.contains(n)?1:0,1,seen.contains(n)?1:0));
                return snap(q,State.ACTIVE,"Carena","Szukaj duchów w Ados, podziemnym Wofol, jaskini Athor i szczurzym mieście pod Orril. Po rozmowie z każdym przekaż jego imię Carenie.",goals);
            }
            if(q instanceof VampireSword){
                if("forging".equals(step))return next(q,"Hogart","Odbierz krwiopijcę od Hogarta po 10 minutach wykuwania.");
                if("start".equals(v))return carry(q,p,"Hogart","Pokonaj Lorda Wampira. Markovich napełni pustą czarę za 7 trucheł wampira, 7 trucheł nietoperza i pierścień z czaszką w 5 minut; Hogart potrzebuje pełnej czary i dodatkowych 200 trucheł wampira.",items("czara",1,"truchło wampira",200));
            }
            if(q instanceof RingMaker&&step.startsWith("forging"))return next(q,"Ognir","Naprawa trwa 15 minut. Zapytaj Ognira o pierścień szmaragdowy, aby odebrać go z zachowaniem pierwotnego przypisania do właściciela.");
            if(q instanceof ClubOfThorns&&"start".equals(step))return next(q,"Orc Saman","Użyj otrzymanego klucza w więzieniu Kotoch, pokonaj królową pająków po przyjęciu prośby i wróć do Samana z wieścią o zwycięstwie.");
            if(q instanceof FishermansLicenseQuiz){if("passed".equals(v))return next(q,"Santiago","Egzamin zdany. Uiść jednorazowo 500 monet za uprawnienia, a potem zapytaj o egzamin, aby odebrać nagrodę i przejść dalej. Nie musisz rozpoznawać ryb ponownie.");Long.parseLong(v);return next(q,"Santiago","W trakcie egzaminu wpisuj nazwy ryb ze stołu. Po nieudanej lub przerwanej próbie odczekaj dobę od jej rozpoczęcia, następnie zapytaj o egzamin.");}
        }catch(IllegalArgumentException e){ /* Preserve unfamiliar saves and use legacy history. */ }
        return snap(q,State.UNKNOWN,"","Nie można wyświetlić postępu tego zadania. Zgłoś problem administracji, podając nazwę zadania.",Collections.<Objective>emptyList());
    }
}
