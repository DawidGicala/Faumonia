package games.stendhal.server.entity.player;

import games.stendhal.server.core.engine.RPClassGenerator;
import games.stendhal.common.constants.Events;
import games.stendhal.server.events.PlayerDialogueTextEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;
import marauroa.common.net.Decoder;
import marauroa.common.net.Encoder;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageS2CCharacterList;
import marauroa.server.game.db.RPObjectDAO;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/** Exercises stored characters before PlayerTransformer, as the login list does. */
public class PlayerLoginSerializationTest {
    @BeforeClass public static void initialize() { new RPClassGenerator().createRPClasses(); }

    @Test public void playerRegistrationReachesMapsAndMenuAfterDialogueEvents() {
        RPClass player = RPClass.getRPClass("player");
        for (String name : new String[] {"source_usage", "looted_items", "buddies", "outfit_colors", "features", "menu", "text"}) {
            assertNotNull("Player registration stopped before " + name, player.getDefinition(DefinitionClass.ATTRIBUTE, name));
        }
        Definition echo = player.getDefinition(DefinitionClass.RPEVENT, Events.PLAYER_DIALOGUE_TEXT);
        assertNotNull(echo); assertTrue(echo.isVisible()); assertFalse(echo.isStorable());
    }

    private RPObject character() {
        RPObject player = new RPObject(); player.setRPClass("player");
        player.put("type", "player"); player.put("name", "LoginTest"); player.put("outfit", 12345);
        player.put("source_usage", "well.count", 3);
        player.put("looted_items", "money", 42);
        player.put("buddies", "TestBuddy", false);
        player.put("features", "test", "enabled");
        player.put("outfit_colors", "hair", "123456");
        player.addSlot("!quests"); RPObject quest = new RPObject(); quest.put("id", 0);
        quest.put("herbs_for_carmen", "arandula=4;borowik=1;jabłko=3;pieczarka=1;polano=2");
        player.getSlot("!quests").add(quest);
        return player;
    }

    private void assertStoredState(RPObject player) {
        assertEquals("3", player.get("source_usage", "well.count"));
        assertEquals("42", player.get("looted_items", "money"));
        assertEquals("false", player.get("buddies", "TestBuddy"));
        assertEquals("enabled", player.get("features", "test"));
        assertEquals("arandula=4;borowik=1;jabłko=3;pieczarka=1;polano=2",
                player.getSlot("!quests").getFirst().get("herbs_for_carmen"));
    }

    private void assertLoginList(RPObject restored) throws Exception {
        MessageS2CCharacterList message = new MessageS2CCharacterList(null, Collections.singletonMap("LoginTest", restored));
        try {
            List<Message> decoded = Decoder.get().decode(null, Encoder.get().encode(message));
            assertEquals(1, decoded.size());
            MessageS2CCharacterList response = (MessageS2CCharacterList) decoded.get(0);
            assertEquals("LoginTest", response.getCharacters()[0]);
            RPObject visible = response.getCharacterDetails().get("LoginTest");
            assertEquals(12345, visible.getInt("outfit"));
            assertEquals("123456", visible.get("outfit_colors", "hair"));
            assertFalse(visible.hasMap("source_usage")); assertFalse(visible.hasMap("looted_items"));
            assertFalse(visible.hasMap("buddies")); assertFalse(visible.hasSlot("!quests"));
        } finally { Decoder.get().clear(null); }
        assertStoredState(restored);
    }

    @Test public void storedCharacterWithMapsAndQuestSurvivesRealLoginMessage() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        character().writeObject(new OutputSerializer(bytes), DetailLevel.FULL);
        RPObject restored = new RPObject();
        restored.readObject(new InputSerializer(new ByteArrayInputStream(bytes.toByteArray())));
        assertStoredState(restored); assertLoginList(restored);
    }

    private static final class Reader extends RPObjectDAO { Reader() { super(null); } }

    @Test public void publicDialogueEchoReachesClientOnceAndIsNotPersistedWithCharacter() throws Exception {
        RPObject player = character();
        player.addEvent(new PlayerDialogueTextEvent("Czy mogę ci w czymś pomóc?"));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        player.writeObject(new OutputSerializer(bytes), DetailLevel.NORMAL);
        RPObject nearby = new RPObject(); nearby.readObject(new InputSerializer(new ByteArrayInputStream(bytes.toByteArray())));
        assertEquals(1, nearby.events().size());
        marauroa.common.game.RPEvent event = nearby.events().get(0);
        assertEquals("Czy mogę ci w czymś pomóc?", event.get("text"));
        assertFalse(nearby.has("text"));
        assertTrue(games.stendhal.client.events.EventFactory.create(new games.stendhal.client.entity.Player(), event)
                instanceof games.stendhal.client.events.PublicTextEvent);
        bytes.reset(); player.writeObject(new OutputSerializer(bytes), DetailLevel.FULL);
        RPObject stored = new RPObject(); stored.readObject(new InputSerializer(new ByteArrayInputStream(bytes.toByteArray())));
        assertTrue(stored.events().isEmpty()); assertStoredState(stored); assertLoginList(stored);
    }

    @Test public void h2BlobLoadsThroughRealCharacterDaoAndSerializesForLogin() throws Exception {
        // Dedicated memory database: never reads server.ini or the owner's database.
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DeflaterOutputStream compressed = new DeflaterOutputStream(bytes);
        OutputSerializer serializer = new OutputSerializer(compressed);
        character().writeObject(serializer, DetailLevel.FULL); compressed.close();
        Class.forName("org.h2.Driver");
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:dialogue_login_test", "sa", "")) {
            connection.createStatement().execute("CREATE TABLE login_fixture (data BLOB, protocol_version INT)");
            try (PreparedStatement insert = connection.prepareStatement("INSERT INTO login_fixture VALUES (?, ?)")) {
                insert.setBytes(1, bytes.toByteArray()); insert.setInt(2, serializer.getProtocolVersion()); insert.executeUpdate();
            }
            try (ResultSet row = connection.createStatement().executeQuery("SELECT data, protocol_version FROM login_fixture")) {
                assertTrue(row.next());
                RPObject restored = new Reader().readRPObject(1, row.getBlob(1), row.getInt(2), false);
                assertStoredState(restored); assertLoginList(restored);
            }
        }
    }
}
