package games.stendhal.server.entity.npc.dialogue;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.maps.quests.*;
import games.stendhal.server.core.rp.quest.*;
import java.util.*;
public class CollectionQuestFixture extends HuntQuestFixture {
    @Override public void createWorld(){
        super.createWorld();Map<String,String> attrs=Collections.emptyMap();
        originals.put("Urzędnik Ados",NPCList.get().remove("Urzędnik Ados"));
        new games.stendhal.server.maps.ados.townhall.UrzednikAdosNPC().configureZone(zone,attrs);
        npcs.put("Urzędnik Ados",NPCList.get().get("Urzędnik Ados"));
        originals.put("Kolekcjoner Andrzej",NPCList.get().remove("Kolekcjoner Andrzej"));
        new games.stendhal.server.maps.fado.hotel.KolekcjonerAndrzejNPC().configureZone(zone,attrs);
        npcs.put("Kolekcjoner Andrzej",NPCList.get().get("Kolekcjoner Andrzej"));
        originals.put("Szaberka",NPCList.get().remove("Szaberka"));
        new games.stendhal.server.maps.bandit.tavern.SzaberkaNPC().configureZone(zone,attrs);
        npcs.put("Szaberka",NPCList.get().get("Szaberka"));
        originals.put("Josephine",NPCList.get().remove("Josephine"));
        new games.stendhal.server.maps.fado.house.WomanNPC().configureZone(zone,attrs);
        npcs.put("Josephine",NPCList.get().get("Josephine"));
        originals.put("Bario",NPCList.get().remove("Bario"));
        new games.stendhal.server.maps.ados.outside.CloaksCollectorNPC().configureZone(zone,attrs);
        npcs.put("Bario",NPCList.get().get("Bario"));
        originals.put("Generał Jan",NPCList.get().remove("Generał Jan"));
        new games.stendhal.server.maps.ados.stronghold.General().configureZone(zone,attrs);
        npcs.put("Generał Jan",NPCList.get().get("Generał Jan"));
        originals.put("Mayor Chalmers",NPCList.get().remove("Mayor Chalmers"));
        new games.stendhal.server.maps.ados.townhall.MayorNPC().configureZone(zone,attrs);
        npcs.put("Mayor Chalmers",NPCList.get().get("Mayor Chalmers"));
        originals.put("Mayor Sakhs",NPCList.get().remove("Mayor Sakhs"));
        new games.stendhal.server.maps.semos.townhall.MayorNPC().configureZone(zone,attrs);
        npcs.put("Mayor Sakhs",NPCList.get().get("Mayor Sakhs"));
        originals.put("Urzędnik Hans",NPCList.get().remove("Urzędnik Hans"));
        new games.stendhal.server.maps.semos.townhall.Urzednik1NPC().configureZone(zone,attrs);
        npcs.put("Urzędnik Hans",NPCList.get().get("Urzędnik Hans"));
        originals.put("Urzędnik Bill",NPCList.get().remove("Urzędnik Bill"));
        new games.stendhal.server.maps.semos.townhall.Urzednik2NPC().configureZone(zone,attrs);
        npcs.put("Urzędnik Bill",NPCList.get().get("Urzędnik Bill"));
        originals.put("Terry",NPCList.get().remove("Terry"));
        new games.stendhal.server.maps.semos.caves.BabyDragonSellerNPC().configureZone(zone,attrs);
        npcs.put("Terry",NPCList.get().get("Terry"));
        originals.put("Urzędnik Fado",NPCList.get().remove("Urzędnik Fado"));
        new games.stendhal.server.maps.fado.tavern.UrzednikFadoNPC().configureZone(zone,attrs);
        npcs.put("Urzędnik Fado",NPCList.get().get("Urzędnik Fado"));
        originals.put("Urzędnik Kirdneh",NPCList.get().remove("Urzędnik Kirdneh"));
        new games.stendhal.server.maps.kirdneh.townhall.UrzednikKirdnehNPC().configureZone(zone,attrs);
        npcs.put("Urzędnik Kirdneh",NPCList.get().get("Urzędnik Kirdneh"));
        originals.put("Urzędnik Nalwor",NPCList.get().remove("Urzędnik Nalwor"));
        new games.stendhal.server.maps.nalwor.royal.UrzednikNalworNPC().configureZone(zone,attrs);
        npcs.put("Urzędnik Nalwor",NPCList.get().get("Urzędnik Nalwor"));
        originals.put("Urzędnik Semos",NPCList.get().remove("Urzędnik Semos"));
        new games.stendhal.server.maps.semos.townhall.UrzednikSemosNPC().configureZone(zone,attrs);
        npcs.put("Urzędnik Semos",NPCList.get().get("Urzędnik Semos"));
        originals.put("Baldemar",NPCList.get().remove("Baldemar"));
        new games.stendhal.server.maps.magic.theater.MithrilShieldForgerNPC().configureZone(zone,attrs);
        npcs.put("Baldemar",NPCList.get().get("Baldemar"));
        originals.put("Vulcanus",NPCList.get().remove("Vulcanus"));
        new games.stendhal.server.maps.kotoch.SmithNPC().configureZone(zone,attrs);
        npcs.put("Vulcanus",NPCList.get().get("Vulcanus"));
        originals.put("Phalk",NPCList.get().remove("Phalk"));
        new games.stendhal.server.maps.semos.mines.DwarfGuardianNPC().configureZone(zone,attrs);
        npcs.put("Phalk",NPCList.get().get("Phalk"));
        originals.put("Wrvil",NPCList.get().remove("Wrvil"));
        new games.stendhal.server.maps.wofol.house4.TraderNPC().configureZone(zone,attrs);
        npcs.put("Wrvil",NPCList.get().get("Wrvil"));
        originals.put("Mrotho",NPCList.get().remove("Mrotho"));
        new games.stendhal.server.maps.ados.barracks.BuyerNPC().configureZone(zone,attrs);
        npcs.put("Mrotho",NPCList.get().get("Mrotho"));
        originals.put("Grafindle",NPCList.get().remove("Grafindle"));
        originals.put("Nnyddion",NPCList.get().remove("Nnyddion"));
        new games.stendhal.server.maps.nalwor.bank.BankNPC().configureZone(zone,attrs);
        npcs.put("Grafindle",NPCList.get().get("Grafindle"));
        npcs.put("Nnyddion",NPCList.get().get("Nnyddion"));
        originals.put("Jens",NPCList.get().remove("Jens"));
        originals.put("George",NPCList.get().remove("George"));
        originals.put("Anna",NPCList.get().remove("Anna"));
        new games.stendhal.server.maps.ados.city.KidsNPCs().configureZone(zone,attrs);
        npcs.put("Jens",NPCList.get().get("Jens"));
        npcs.put("George",NPCList.get().get("George"));
        npcs.put("Anna",NPCList.get().get("Anna"));
        originals.put("Wandy",NPCList.get().remove("Wandy"));
        new games.stendhal.server.maps.ados.collector.WandyNPC().configureZone(zone,attrs);
        npcs.put("Wandy",NPCList.get().get("Wandy"));
        originals.put("Hazel",NPCList.get().remove("Hazel"));
        new games.stendhal.server.maps.kirdneh.museum.CuratorNPC().configureZone(zone,attrs);
        npcs.put("Hazel",NPCList.get().get("Hazel"));
        originals.put("Trener Rycerzy",NPCList.get().remove("Trener Rycerzy"));
        new games.stendhal.server.maps.ados.stronghold.Warrior().configureZone(zone,attrs);
        npcs.put("Trener Rycerzy",NPCList.get().get("Trener Rycerzy"));
        originals.put("Major Klykh",NPCList.get().remove("Major Klykh"));
        new games.stendhal.server.maps.ados.stronghold.Best().configureZone(zone,attrs);
        npcs.put("Major Klykh",NPCList.get().get("Major Klykh"));
        originals.put("Trener magów",NPCList.get().remove("Trener magów"));
        new games.stendhal.server.maps.ados.stronghold.Mage().configureZone(zone,attrs);
        npcs.put("Trener magów",NPCList.get().get("Trener magów"));
        originals.put("Trener łuczników",NPCList.get().remove("Trener łuczników"));
        new games.stendhal.server.maps.ados.stronghold.Archer().configureZone(zone,attrs);
        npcs.put("Trener łuczników",NPCList.get().get("Trener łuczników"));
        // Daily hunts use creatures instantiated by map spawns. This small test world
        // has no map loader; seed real definitions instead of relying on earlier tests.
        for(String name:Arrays.asList("szczur","szczur jaskiniowy","gnom","panna gnom")){
            org.junit.Assert.assertNotNull(name, games.stendhal.server.core.engine.SingletonRepository.getEntityManager().getCreature(name));
        }
        for(IQuest q:CollectionQuestProgress.all()){q.addToWorld();quests.put(q.getClass().getSimpleName(),q);}
    }
    String npcName(IQuest q){for(String name:npcs.keySet())if(name.equalsIgnoreCase(q.getNPCName()))return name;throw new AssertionError(q.getNPCName());}
    @Override void capture(String name){
        try{
            java.nio.file.Path dir=java.nio.file.Paths.get(System.getProperty("quest.pilot.output","."),"collection-scenes");java.nio.file.Files.createDirectories(dir);
            try(java.io.DataOutputStream out=new java.io.DataOutputStream(java.nio.file.Files.newOutputStream(dir.resolve(name+".wire")))){java.util.List<String> values=latest().toWire();out.writeInt(values.size());for(String v:values)out.writeUTF(v);}
        }catch(java.io.IOException e){throw new AssertionError(e);}
    }
}
