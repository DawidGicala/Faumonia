package games.stendhal.server.core.rp.quest;
import java.util.*;
/** Read-only metadata backed by the original quest's own requirements. */
public interface NativeCollectionDetails {
    String getCollectionKind();
    Map<String,Integer> getCollectionItems();
    String getCollectionHint();
    default String getCollectionKill(){return "";}
    default boolean getCollectionSolo(){return false;}
    default int getCollectionWait(){return 0;}
    default int getCollectionExpiry(){return 0;}
    default Map<String,String> getCollectionPrerequisites(){return Collections.emptyMap();}
}
