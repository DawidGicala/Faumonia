package games.stendhal.server.maps.quests;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.quest.ExpeditionXXV;
import games.stendhal.server.entity.mapstuff.useable.ExpeditionSurveySpot;
import java.util.Map;
public class ExpeditionSurveyConfigurator implements ZoneConfigurator {
    public void configureZone(StendhalRPZone zone,Map<String,String> attributes){
        for(int i=0;i<3;i++)if(ExpeditionXXV.ZONES[i].equals(zone.getName())){zone.add(new ExpeditionSurveySpot(i));return;}
        throw new IllegalArgumentException("Expedition survey configured in the wrong zone: "+zone.getName());
    }
}
