package games.stendhal.server.entity.mapstuff.useable;
import games.stendhal.server.core.rp.quest.ExpeditionXXV;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;

/** A shared world object with per-player, server-validated discovery flags. */
public class ExpeditionSurveySpot extends UseableEntity {
    private final int site;
    public ExpeditionSurveySpot(int site){
        if(site<0||site>=3)throw new IllegalArgumentException("Unknown expedition site");this.site=site;
        put("class","expedition");put("name",ExpeditionXXV.SPRITES[site]);put("state",0);
        setMenu("Zbadaj|use");setDescription(ExpeditionXXV.PLACES[site]+". Podejdź blisko i wybierz Zbadaj, aby sprawdzić ślad wyprawy.");
        setPosition(ExpeditionXXV.COORDS[site][0],ExpeditionXXV.COORDS[site][1]);setResistance(0);
    }
    public String getName(){return ExpeditionXXV.PLACES[site];}
    public boolean onUsed(RPEntity entity){
        if(!(entity instanceof Player))return false;Player p=(Player)entity;
        if(getZone()==null||p.getZone()!=getZone()||!ExpeditionXXV.ZONES[site].equals(getZone().getName())
            ||Math.abs(p.getX()-getX())+Math.abs(p.getY()-getY())>1){p.sendPrivateText("Podejdź bezpośrednio do śladu wyprawy, żeby go zbadać.");return false;}
        if(!ExpeditionXXV.surveying(p)){p.sendPrivateText("Najpierw ukończ zwiad Faga i dostawę Thyrona, a potem odbierz od Mistrza zlecenie rozpoznania trasy XXV.");return false;}
        int mask=ExpeditionXXV.surveyMask(p),bit=1<<site;
        if((mask&bit)!=0){p.sendPrivateText("To miejsce jest już zapisane w twoim raporcie. Rozpoznanie: "+Integer.bitCount(mask)+"/3.");return true;}
        p.setQuest(ExpeditionXXV.MASTER,"survey;"+(mask|bit));
        p.sendPrivateText(ExpeditionXXV.PLACES[site]+": "+ExpeditionXXV.FINDINGS[site]+" Rozpoznanie: "+Integer.bitCount(mask|bit)+"/3."+(Integer.bitCount(mask|bit)==3?" Wróć do Mistrza i złóż raport.":""));
        p.notifyWorldAboutChanges();return true;
    }
}
