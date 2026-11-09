package games.stendhal.server.entity.npc.dialogue;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.maps.quests.*;
import java.util.*;

/** Adds genuine hunting NPC definitions to the previous cross-quest world. */
public class HuntQuestFixture extends JourneyQuestFixture {
    @Override public void createWorld() {
        super.createWorld();
        Map<String,String> attrs=Collections.emptyMap();
        originals.put("JJ",NPCList.get().remove("JJ"));
        new games.stendhal.server.maps.ados.barracks.JJNPC().configureZone(zone,attrs);
        originals.put("Andy",NPCList.get().remove("Andy"));
        new games.stendhal.server.maps.ados.city.ManWithHatNPC().configureZone(zone,attrs);
        originals.put("Walt",NPCList.get().remove("Walt"));
        new games.stendhal.server.maps.ados.city.WaltNPC().configureZone(zone,attrs);
        originals.put("Bate",NPCList.get().remove("Bate"));
        new games.stendhal.server.maps.ados.tavern.BateNPC().configureZone(zone,attrs);
        originals.put("Ivar",NPCList.get().remove("Ivar"));
        new games.stendhal.server.maps.ados.townhall.IvarNPC().configureZone(zone,attrs);
        originals.put("Verdan",NPCList.get().remove("Verdan"));
        new games.stendhal.server.maps.ados.townhall.VerdanNPC().configureZone(zone,attrs);
        originals.put("John",NPCList.get().remove("John"));
        new games.stendhal.server.maps.athor.holiday_area.HusbandNPC().configureZone(zone,attrs);
        originals.put("Yance",NPCList.get().remove("Yance"));
        new games.stendhal.server.maps.fado.bank.TellerNPC().configureZone(zone,attrs);
        originals.put("Rade",NPCList.get().remove("Rade"));
        new games.stendhal.server.maps.fado.city.OlbrzymNPC().configureZone(zone,attrs);
        originals.put("Orchiwald",NPCList.get().remove("Orchiwald"));
        new games.stendhal.server.maps.fado.forest.GreeterNPC().configureZone(zone,attrs);
        originals.put("Linda",NPCList.get().remove("Linda"));
        new games.stendhal.server.maps.fado.hotel.GreeterNPC().configureZone(zone,attrs);
        originals.put("Sausid",NPCList.get().remove("Sausid"));
        new games.stendhal.server.maps.fado.tavern.SausidNPC().configureZone(zone,attrs);
        originals.put("Staro",NPCList.get().remove("Staro"));
        new games.stendhal.server.maps.fado.tavern.StaroNPC().configureZone(zone,attrs);
        originals.put("Django",NPCList.get().remove("Django"));
        new games.stendhal.server.maps.faumonia.dragon.DjangoNPC().configureZone(zone,attrs);
        originals.put("Dodo",NPCList.get().remove("Dodo"));
        new games.stendhal.server.maps.kikareukin.cave.DodoNPC().configureZone(zone,attrs);
        originals.put("Cal",NPCList.get().remove("Cal"));
        new games.stendhal.server.maps.kirdneh.museum.CalNPC().configureZone(zone,attrs);
        originals.put("Morgrin",NPCList.get().remove("Morgrin"));
        new games.stendhal.server.maps.magic.school.GroundskeeperNPC().configureZone(zone,attrs);
        originals.put("Lorithien",NPCList.get().remove("Lorithien"));
        new games.stendhal.server.maps.nalwor.postoffice.PostNPC().configureZone(zone,attrs);
        originals.put("Maerion",NPCList.get().remove("Maerion"));
        new games.stendhal.server.maps.nalwor.royal.MayorNPC().configureZone(zone,attrs);
        originals.put("Loretta",NPCList.get().remove("Loretta"));
        new games.stendhal.server.maps.orril.dwarfmine.IronBuyerNPC().configureZone(zone,attrs);
        originals.put("Fag",NPCList.get().remove("Fag"));
        new games.stendhal.server.maps.pol.desert.FagNPC().configureZone(zone,attrs);
        originals.put("Octavio",NPCList.get().remove("Octavio"));
        new games.stendhal.server.maps.semos.city.OctavioNPC().configureZone(zone,attrs);
        originals.put("Wod",NPCList.get().remove("Wod"));
        new games.stendhal.server.maps.semos.city.WodNPC().configureZone(zone,attrs);
        originals.put("Think",NPCList.get().remove("Think"));
        new games.stendhal.server.maps.semos.mines.ThinkNPC().configureZone(zone,attrs);
        originals.put("Thorgal",NPCList.get().remove("Thorgal"));
        new games.stendhal.server.maps.semos.mines.ThorgalNPC().configureZone(zone,attrs);
        originals.put("Luck",NPCList.get().remove("Luck"));
        new games.stendhal.server.maps.semos.plains.LuckNPC().configureZone(zone,attrs);
        originals.put("Natan",NPCList.get().remove("Natan"));
        new games.stendhal.server.maps.semos.roadstatue.NatanNPC().configureZone(zone,attrs);
        originals.put("Kape",NPCList.get().remove("Kape"));
        new games.stendhal.server.maps.semos.tavern.KapeNPC().configureZone(zone,attrs);
        npcs.put("JJ",NPCList.get().get("JJ"));
        npcs.put("Andy",NPCList.get().get("Andy"));
        npcs.put("Walt",NPCList.get().get("Walt"));
        npcs.put("Bate",NPCList.get().get("Bate"));
        npcs.put("Ivar",NPCList.get().get("Ivar"));
        npcs.put("Verdan",NPCList.get().get("Verdan"));
        npcs.put("John",NPCList.get().get("John"));
        npcs.put("Yance",NPCList.get().get("Yance"));
        npcs.put("Rade",NPCList.get().get("Rade"));
        npcs.put("Orchiwald",NPCList.get().get("Orchiwald"));
        npcs.put("Linda",NPCList.get().get("Linda"));
        npcs.put("Sausid",NPCList.get().get("Sausid"));
        npcs.put("Staro",NPCList.get().get("Staro"));
        npcs.put("Django",NPCList.get().get("Django"));
        npcs.put("Dodo",NPCList.get().get("Dodo"));
        npcs.put("Cal",NPCList.get().get("Cal"));
        npcs.put("Morgrin",NPCList.get().get("Morgrin"));
        npcs.put("Lorithien",NPCList.get().get("Lorithien"));
        npcs.put("Maerion",NPCList.get().get("Maerion"));
        npcs.put("Loretta",NPCList.get().get("Loretta"));
        npcs.put("Fag",NPCList.get().get("Fag"));
        npcs.put("Octavio",NPCList.get().get("Octavio"));
        npcs.put("Wod",NPCList.get().get("Wod"));
        npcs.put("Think",NPCList.get().get("Think"));
        npcs.put("Thorgal",NPCList.get().get("Thorgal"));
        npcs.put("Luck",NPCList.get().get("Luck"));
        npcs.put("Natan",NPCList.get().get("Natan"));
        npcs.put("Kape",NPCList.get().get("Kape"));
        for(IQuest q:Arrays.<IQuest>asList(new KillAdosSpiders(),new KillAlbinos(),new KillAmazon(),new KillAngels(),new KillBarbarians(),new KillChaos(),new KillChaosMonsters(),new KillDarkElves(),new KillDragons(),new KillDwarfs(),new KillEasyMonsters(),new KillElfes(),new KillFairy(),new KillForVerdan(),new KillForWod(),new KillGiants(),new KillGnomes(),new KillHigh(),new KillLast(),new KillMadaram(),new KillMag(),new KillMediumMonsters(),new KillMonks(),new KillSausid(),new KillSpiders(),new KillStone(),new KillXeno(),new CleanAthorsUnderground(),new CollectChaosThorgal())){q.addToWorld();quests.put(q.getClass().getSimpleName(),q);}
        player.setXP(games.stendhal.common.Level.getXP(300));player.setLevel(300);
    }
    @Override void capture(String name) {
        try {
            java.nio.file.Path dir=java.nio.file.Paths.get(System.getProperty("quest.pilot.output","."),"hunt-scenes");
            java.nio.file.Files.createDirectories(dir);
            try(java.io.DataOutputStream out=new java.io.DataOutputStream(java.nio.file.Files.newOutputStream(dir.resolve(name+".wire")))) {
                List<String> values=latest().toWire();out.writeInt(values.size());for(String value:values)out.writeUTF(value);
            }
        } catch(java.io.IOException e){throw new AssertionError(e);}
    }
}
