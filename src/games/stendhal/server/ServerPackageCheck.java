/* SPDX-License-Identifier: GPL-2.0-or-later */
package games.stendhal.server;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/** Checks the unpacked distribution without starting a world or opening a database. */
public final class ServerPackageCheck {
    private ServerPackageCheck() { }

    public static void main(String[] args) throws Exception {
        ClassLoader loader = ServerPackageCheck.class.getClassLoader();
        String[] classes = {
            "games.stendhal.server.StendhalServer",
            "games.stendhal.server.actions.NpcDialogueAction",
            "games.stendhal.common.NpcDialogueSnapshot",
            "games.stendhal.common.TutorialGuideSnapshot",
            "games.stendhal.server.maps.semos.masterhouse.MasterNPC",
            "games.stendhal.server.entity.mapstuff.portal.NewcomerPortalFactory",
            "games.stendhal.server.maps.quests.HerbsForCarmen",
            "games.stendhal.server.maps.quests.HelpSedro",
            "games.stendhal.server.maps.quests.BowsForOuchit",
            "marauroa.server.marauroad", "org.apache.log4j.Logger",
            "org.apache.commons.lang.StringUtils", "groovy.lang.GroovyShell",
            "simple.http.Request", "org.h2.Driver", "com.mysql.jdbc.Driver"
        };
        for (String name : classes) {
            // No class initialization: this is a packaging check, not server startup.
            Class<?> type = Class.forName(name, false, loader);
            URL origin = type.getProtectionDomain().getCodeSource().getLocation();
            if (!origin.getPath().endsWith(".jar")) {
                throw new IllegalStateException("Class loaded outside a JAR: " + name + " from " + origin);
            }
            System.out.println("OK " + name + " : " + origin);
        }
        String[] resources = {
            "data/conf/zones.xml", "data/conf/items.xml", "data/conf/creatures.xml",
            "data/maps/Level 0/semos/city.tmx",
            "games/stendhal/server/stendhal_init.sql"
        };
        for (String name : resources) {
            URL resource = loader.getResource(name);
            if (resource == null || !"jar".equals(resource.getProtocol())) {
                throw new IllegalStateException("Missing packaged resource: " + name + " : " + resource);
            }
            System.out.println("OK " + name);
        }
        Class<?> snapshot = Class.forName("games.stendhal.common.NpcDialogueSnapshot", false, loader);
        Object action = snapshot.getField("ACTION").get(null);
        Object event = snapshot.getField("EVENT").get(null);
        if (action.equals(event)) {
            throw new IllegalStateException("NPC action collides with event: " + action);
        }
        System.out.println("NPC action: " + action + ", event: " + event);
        verifyPlayerRegistration(loader);
        System.out.println("Package check OK. No server or database was started.");
    }

    /** Player.generateRPClass logs and swallows SyntaxException; class loading alone cannot detect it. */
    private static void verifyPlayerRegistration(ClassLoader loader) throws Exception {
        Class<?> generator = Class.forName("games.stendhal.server.core.engine.RPClassGenerator", true, loader);
        generator.getMethod("createRPClasses").invoke(generator.getConstructor().newInstance());
        Class<?> rpClass = Class.forName("marauroa.common.game.RPClass", true, loader);
        Object player = rpClass.getMethod("getRPClass", String.class).invoke(null, "player");
        Set<String> names = new HashSet<String>();
        for (Object definition : (Iterable<?>) rpClass.getMethod("getDefinitions").invoke(player)) {
            names.add((String) definition.getClass().getMethod("getName").invoke(definition));
        }
        for (String required : new String[] {"source_usage", "looted_items", "buddies", "outfit_colors", "features", "menu", "text", "player_dialogue_text", "tutorial_guide"}) {
            if (!names.contains(required)) {
                throw new IllegalStateException("Incomplete player registration: missing " + required);
            }
        }
        System.out.println("OK player definitions: saved character maps and public dialogue chat");
    }
}
