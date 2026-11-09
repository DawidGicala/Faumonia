package games.stendhal.server.core.rp.quest;
import games.stendhal.common.QuestProgress;
import games.stendhal.common.QuestProgress.*;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.*;
import java.util.*;

/** Journal projection over the same saved phases used by dialogue and survey objects. */
public final class ExpeditionQuestProgress {
    private ExpeditionQuestProgress(){}
    public static boolean supports(IQuest q){return q instanceof MainTask25||q instanceof FagDeathScout||q instanceof ThyronExpeditionFittings;}
    public static QuestProgress read(IQuest q,Player p){
        List<Objective> rows=new ArrayList<Objective>();String next=q.getNPCName(),hint="";State status=State.ACTIVE;
        String s=ExpeditionXXV.state(p,q.getSlotName());
        if(q.isCompleted(p))return new QuestProgress(q.getName(),State.COMPLETED,"","Zadanie ukończone.",rows);
        if(s.equals("rejected"))return new QuestProgress(q.getName(),State.DECLINED,next,"Wróć do zleceniodawcy, jeśli zechcesz podjąć próbę.",rows);
        if(q instanceof FagDeathScout){
            Map<String,int[]> kills=ExpeditionXXV.kills(p);boolean complete=kills.size()==6;
            if(kills.size()!=6)status=State.UNKNOWN;
            for(Map.Entry<String,int[]> e:kills.entrySet()){int[] v=e.getValue();rows.add(new Objective("kill:"+e.getKey(),e.getKey(),v[0],v[1],-1));complete&=v[0]>=v[1];}
            if(complete)status=State.READY_TO_DELIVER;hint="Zdaj meldunek Fagowi przy pustynnej piramidzie. Ten krótki zwiad jest częścią próby XXV; Fag zleca również osobne, dłuższe polowanie.";
        }else if(q instanceof ThyronExpeditionFittings){
            int[] left=ExpeditionXXV.remaining(p);hint=ExpeditionXXV.fittingsSummary(p);
            if(left!=null){next="Hogart";for(int i=0;i<3;i++)rows.add(new Objective(ExpeditionXXV.MATERIALS[i],"Oddano: "+ExpeditionXXV.MATERIALS[i],ExpeditionXXV.AMOUNTS[i]-left[i],ExpeditionXXV.AMOUNTS[i],p.getNumberOfEquipped(ExpeditionXXV.MATERIALS[i])));}
            else if(s.equals("ready")){next="Hogart";status=State.READY_TO_DELIVER;}
            else if(s.equals("issued")){next="Thyron";int carried=ExpeditionXXV.carriedPackage(p)==null?0:1;rows.add(new Objective(ExpeditionXXV.PACKAGE,"Dostarcz paczkę Thyronowi",0,1,carried));if(carried==1)status=State.READY_TO_DELIVER;}
            else status=State.UNKNOWN;
        }else{
            hint=((MainTask25)q).masterSummary(p);
            if(s.equals("preparing")){rows.add(new Objective("fag","Ukończ krótki zwiad Faga",p.isQuestCompleted(ExpeditionXXV.HUNT)?1:0,1,-1));rows.add(new Objective("thyron","Dostarcz okucia Thyronowi",p.isQuestCompleted(ExpeditionXXV.FITTINGS)?1:0,1,-1));if(ExpeditionXXV.prepared(p))status=State.READY_TO_DELIVER;}
            else if(s.matches("survey;[0-7]")){for(int i=0;i<3;i++)rows.add(new Objective("survey_"+i,ExpeditionXXV.PLACES[i],(ExpeditionXXV.surveyMask(p)&(1<<i))!=0?1:0,1,-1));if(ExpeditionXXV.surveyed(p)==3)status=State.READY_TO_DELIVER;}
            else if(s.equals("fang")){int carried=p.getNumberOfEquipped(ExpeditionXXV.FANG);rows.add(new Objective(ExpeditionXXV.FANG,"Dostarcz kieł złotej kostuchy",0,1,carried));if(carried>0)status=State.READY_TO_DELIVER;}
            else status=State.UNKNOWN;
        }
        return new QuestProgress(q.getName(),status,next,hint,rows);
    }
}
