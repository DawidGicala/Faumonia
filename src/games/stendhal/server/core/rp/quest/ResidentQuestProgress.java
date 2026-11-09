package games.stendhal.server.core.rp.quest;

import games.stendhal.common.QuestProgress;
import games.stendhal.common.QuestProgress.Objective;
import games.stendhal.common.QuestProgress.State;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.*;
import java.util.*;

/** Read-only views of the residents' original quest records. No save migration. */
public final class ResidentQuestProgress {
    private ResidentQuestProgress() { }
    public static int sheepWeight(Player player) {
        games.stendhal.server.entity.creature.Sheep sheep = player.getSheep();
        return sheep == null ? 0 : sheep.getWeight();
    }
    public static IQuest forNpc(String npc) {
        switch(npc) {
            case "Hayunn": return new TaskHayunn();
            case "Soro": return new CheeseForSoro();
            case "Tad": case "Ilisa": case "Ketteh Wehoh": return new MedicineForTad();
            case "Hackim Easso": case "Xin Blanca": return new NewsFromHackim();
            case "Sarzina": return new LearnAboutKarma();
            case "Nishiya": case "Sato": return new SheepGrowing();
            case "Diehelm Brui": return new ResourceForDiehelm();
            case "Haunchy Meatoch": return new CoalForHaunchy();
            case "Elisabeth": case "Carey": return new ChocolateForElisabeth();
            case "Annie Jones": case "Mrs Jones": return new IcecreamForAnnie();
            case "Old Mother Helena": return new Soup();
            case "Florence Boullabaisse": return new FishSoup();
            case "Anastasia": return new FishSoupForHughie();
            case "Coralia": return new FruitsForCoralia();
            case "Zara": return new SuntanCreamForZara();
            case "Katinka": case "Dr. Feelgood": return new ZooFood();
            case "Agnus": case "Avalon": case "Cody": case "Mariel": case "Opal": return new FindRatChildren();
            case "Mrs. Yeti": case "Salva Mattori": return new HelpMrsYeti();
            case "tomi": return new HelpTomi();
            case "Leander": return new PizzaDelivery();
            default: return null;
        }
    }
    public static boolean supports(IQuest q) {
        return q instanceof TaskHayunn || q instanceof CheeseForSoro || q instanceof MedicineForTad
            || q instanceof NewsFromHackim || q instanceof LearnAboutKarma || q instanceof SheepGrowing
            || q instanceof ResourceForDiehelm || q instanceof CoalForHaunchy || q instanceof ChocolateForElisabeth
            || q instanceof IcecreamForAnnie || q instanceof Soup || q instanceof FishSoup || q instanceof FishSoupForHughie
            || q instanceof FruitsForCoralia || q instanceof SuntanCreamForZara || q instanceof ZooFood
            || q instanceof FindRatChildren || q instanceof HelpMrsYeti || q instanceof HelpTomi || q instanceof PizzaDelivery;
    }
    public static boolean collection(IQuest q) {
        return q instanceof TaskHayunn || q instanceof CheeseForSoro || q instanceof ResourceForDiehelm || q instanceof FruitsForCoralia;
    }
    public static Map<String,Integer> requirements(IQuest q) {
        if (q instanceof TaskHayunn) return TaskHayunn.getRequiredItems();
        if (q instanceof CheeseForSoro) return CheeseForSoro.getRequiredItems();
        if (q instanceof ResourceForDiehelm) return ResourceForDiehelm.getRequiredItems();
        if (q instanceof FruitsForCoralia) return FruitsForCoralia.getRequiredItems();
        Map<String,Integer> result=new LinkedHashMap<String,Integer>();
        if (q instanceof Soup) for(String n:Soup.getRequiredItems()) result.put(n,1);
        if (q instanceof FishSoup) for(String n:FishSoup.getRequiredItems()) result.put(n,1);
        return result;
    }
    public static Map<String,Integer> remaining(IQuest q,Player p) {
        Map<String,Integer> required=requirements(q), result=new LinkedHashMap<String,Integer>();
        String raw=p.getQuest(q.getSlotName());
        if(raw==null || raw.equals("rejected")) return required;
        if(q.isCompleted(p)) return result;
        if(collection(q)) {
            for(String part:raw.split(";",-1)) {
                String[] v=part.split("=",-1);
                if(v.length!=2 || !required.containsKey(v[0]) || result.containsKey(v[0])) throw new IllegalArgumentException("Unknown collection");
                int count=Integer.parseInt(v[1]);
                if(count<=0 || count>required.get(v[0])) throw new IllegalArgumentException("Invalid collection count");
                result.put(v[0],count);
            }
        } else if(q instanceof Soup || q instanceof FishSoup) {
            Set<String> delivered=new HashSet<String>(Arrays.asList(raw.split(";")));
            for(String name:delivered) if(!name.isEmpty()&&!required.containsKey(name)) throw new IllegalArgumentException("Unknown ingredient");
            for(String name:required.keySet()) if(!delivered.contains(name)) result.put(name,1);
        }
        return result;
    }
    public static QuestProgress read(IQuest q,Player p) {
        if(!supports(q)||!q.isStarted(p)||!q.isVisibleOnQuestStatus())return null;
        String v=p.getQuest(q.getSlotName());
        if(q.isCompleted(p)) return snap(q,State.COMPLETED,"","Zadanie ukończone."+(q.isRepeatable(p)?" Możesz ponownie zapytać o pomoc.":""),Collections.<Objective>emptyList());
        if("rejected".equals(v)) return snap(q,State.DECLINED,q.getNPCName(),"Jeśli zmienisz zdanie, wróć do rozmowy.",Collections.<Objective>emptyList());
        try {
            if(collection(q)||q instanceof Soup||q instanceof FishSoup) {
                List<Objective> goals=new ArrayList<Objective>();Map<String,Integer> left=remaining(q,p);boolean ready=true;
                for(Map.Entry<String,Integer> e:requirements(q).entrySet()) {
                    int n=left.containsKey(e.getKey())?left.get(e.getKey()):0, carried=p.getNumberOfEquipped(e.getKey());
                    goals.add(new Objective(e.getKey(),"Oddano: "+e.getKey(),e.getValue()-n,e.getValue(),carried));ready&=carried>=n;
                }
                return snap(q,ready?State.READY_TO_DELIVER:State.ACTIVE,q.getNPCName(),"Przekaż brakujące składniki. Możesz donosić je partiami; rozmówca pamięta wcześniejsze dostawy.",goals);
            }
            if(q instanceof MedicineForTad) {
                if("start".equals(v))return item(q,p,"Tad","flasza",1,"Kup pustą flaszę od Margaret w tawernie Semos i pokaż ją Tadowi.");
                if("ilisa".equals(v))return item(q,p,"Ilisa","flasza",1,"Zanieś flaszę do Ilisy w świątyni Semos.");
                if("corpse&herbs".equals(v))return item(q,p,"Ilisa","arandula",1,"Zbierz arandulę na północ od Semos i zanieś ją Ilisie.");
                if("eliksir".equals(v))return next(q,"Tad","Przekaż Tadowi w hostelu, że Ilisa przygotowała lekarstwo.");
            }
            if(q instanceof NewsFromHackim && "start".equals(v))return next(q,"Xin Blanca","Przekaż Xinowi Blance w tawernie Semos, że dostawa broni jest gotowa.");
            if(q instanceof SheepGrowing) {
                if("asked".equals(v))return next(q,"Nishiya","Zdecyduj, czy zaopiekujesz się owcą dla Sato.");
                if("start".equals(v))return snap(q,State.ACTIVE,"Sato","Nakarm owcę do wagi 100 i przyprowadź ją do Sato. Przekaż ją jako prezent od Nishiyi, zamiast sprzedawać.",Collections.singletonList(new Objective("sheep","Waga owcy",sheepWeight(p),100,-1)));
                if("handed_over".equals(v))return next(q,"Nishiya","Wróć do Nishiyi i potwierdź, że Sato otrzymał owcę.");
            }
            if(q instanceof CoalForHaunchy) {
                if("start".equals(v))return item(q,p,q.getNPCName(),"węgiel",25,"Zanieś Haunchy'emu 25 sztuk węgla na rynek w Ados.");
                if(v.startsWith("waiting;"))return snap(q,State.COMPLETED,"Haunchy Meatoch","Węgiel został dostarczony. Następna dostawa jest możliwa po dwóch dniach.",Collections.<Objective>emptyList());
            }
            if(q instanceof ChocolateForElisabeth || q instanceof IcecreamForAnnie) {
                boolean chocolate=q instanceof ChocolateForElisabeth;
                if("start".equals(v))return next(q,chocolate?"Carey":"Mrs Jones","Najpierw porozmawiaj z mamą dziewczynki. Bez jej zgody nie przekazuj podarunku.");
                if("mummy".equals(v))return item(q,p,q.getNPCName(),chocolate?"tabliczka czekolady":"lody",1,"Mama wyraziła zgodę. Zanieś słodycze dziewczynce i potwierdź podarunek.");
            }
            if(q instanceof FishSoupForHughie && "start".equals(v))return item(q,p,"Anastasia","zupa rybna",1,"Florence gotuje zupę na rynku Ados. Zanieś gotową porcję Anastasii dla Hughiego.");
            if(q instanceof SuntanCreamForZara && "start".equals(v))return item(q,p,"Zara","olejek do opalania",1,"Poproś Pam lub Davida w przebieralni na Athor o przygotowanie olejku i zanieś go Zarze.");
            if(q instanceof ZooFood && (v.equals("start")||v.startsWith("start;"))) {
                if(v.equals("start"))return item(q,p,"Katinka","szynka",10,"Katinka potrzebuje dziesięciu szynek. Przynieś całą dostawę.");
                String n=p.getRequiredItemName(q.getSlotName(),1);int count=p.getRequiredItemQuantity(q.getSlotName(),1);
                if(count<=0||!ZooFood.getRequiredItems().containsKey(n)||ZooFood.getRequiredItems().get(n)!=count)throw new IllegalArgumentException("Unknown zoo delivery");
                return item(q,p,"Katinka",n,count,"Przynieś Katince całą zamówioną ilość karmy i potwierdź przekazanie.");
            }
            if(q instanceof FindRatChildren && v.startsWith("looking")&&v.contains(":")) {
                String[] sides=v.split(":",-1);if(sides.length!=2||!sides[1].startsWith("said"))throw new IllegalArgumentException("Unknown children record");
                Set<String> seen=new HashSet<String>(Arrays.asList(sides[0].split(";"))), reported=new HashSet<String>(Arrays.asList(sides[1].split(";")));
                List<Objective> goals=new ArrayList<Objective>();for(String name:Arrays.asList("avalon","cody","mariel","opal"))goals.add(new Objective(name,"Wiadomość: "+name,reported.contains(name)?1:0,1,seen.contains(name)?1:0));
                return snap(q,State.ACTIVE,"Agnus","Odszukaj dzieci w lochach Orril, przywitaj się z każdym i przekaż Agnus wiadomość o każdym z nich.",goals);
            }
            if(q instanceof HelpMrsYeti) {
                switch(v.split(";")[0]) {
                    case "start":return next(q,"Salva Mattori","Porozmawiaj z Salvą Mattori w podziemnym mieście magów o eliksirze miłości.");
                    case "hackim":return next(q,"Hackim Easso","Zapytaj Hackima w kuźni Semos o Salvę i pożyczony nóż.");
                    case "pies":return item(q,p,"Hackim Easso","tarta",5,"Przynieś Hackimowi pięć tart. Arlindo w piekarni Ados potrafi je przygotować.");
                    case "mieczyk":return item(q,p,"Salva Mattori","mieczyk",1,"Kup mieczyk u Xina Blanki w Semos i zanieś go Salvie.");
                    case "eliksir":return items(q,p,"Salva Mattori",new String[]{"lilia","kokuda","napój z winogron","czarna perła"},new int[]{3,1,1,1},"Przynieś Salvie cały zestaw do sporządzenia eliksiru.");
                    case "gotpotion":return item(q,p,"Mrs. Yeti","eliksir miłości",1,"Zanieś gotowy eliksir Mrs. Yeti w jaskini pod Semos.");
                    case "dragon":return next(q,"Mrs. Yeti","Mrs. Yeti chce małego smoka. Porozmawiaj o smoczym jaju z Morgrinem, a o wykluciu z Terrym.");
                    case "reward":return next(q,"Mrs. Yeti","Mrs. Yeti potrzebuje doby. Potem zapytaj o nagrodę.");
                    default:break;
                }
            }
            if(q instanceof PizzaDelivery && v.contains(";")) {
                String customer=v.split(";")[0];String hint=PizzaDelivery.customerHint(customer);
                if(hint!=null)return item(q,p,customer,"pizza",1,hint+" Powiedz odbiorcy: pizza. Dostarcz ją gorącą, aby otrzymać napiwek.");
            }
        }catch(IllegalArgumentException e) { /* Never repair unknown records from a UI reader. */ }
        return snap(q,State.UNKNOWN,q.getNPCName(),"Nie można wyświetlić postępu tego zadania. Zgłoś problem administracji, podając nazwę zadania.",Collections.<Objective>emptyList());
    }
    private static QuestProgress next(IQuest q,String npc,String hint){return snap(q,State.ACTIVE,npc,hint,Collections.<Objective>emptyList());}
    private static QuestProgress item(IQuest q,Player p,String npc,String item,int count,String hint){return items(q,p,npc,new String[]{item},new int[]{count},hint);}
    private static QuestProgress items(IQuest q,Player p,String npc,String[] names,int[] counts,String hint){
        List<Objective> goals=new ArrayList<Objective>();boolean ready=true;
        for(int i=0;i<names.length;i++){int have=p.getNumberOfEquipped(names[i]);goals.add(new Objective(names[i],"Masz: "+names[i],Math.min(have,counts[i]),counts[i],-1));ready&=have>=counts[i];}
        return snap(q,ready?State.READY_TO_DELIVER:State.ACTIVE,npc,hint,goals);
    }
    private static QuestProgress snap(IQuest q,State s,String npc,String next,List<Objective> goals){return new QuestProgress(q.getName(),s,npc,next,goals);}
}
