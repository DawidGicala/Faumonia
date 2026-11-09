package games.stendhal.client.gui.dialogue;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.PerceptionDispatcher;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.UserContext;
import games.stendhal.client.stendhal;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.List;
import marauroa.client.net.INetworkClientManagerInterface;
import marauroa.common.game.RPAction;
import marauroa.common.net.Decoder;
import marauroa.common.net.Encoder;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2SAction;

/** Real client send and binary protocol, with only the socket replaced by a byte loopback. */
public final class DialogueWireClient implements AutoCloseable {
    private final Network network = new Network();
    private final Client client;

    public DialogueWireClient() {
        if (StendhalClient.get() != null) { throw new IllegalStateException("An isolated client JVM is required"); }
        try {
            // Cache.init must never create or alter the owner's game directory during tests.
            Field folder = stendhal.class.getDeclaredField("gameFolder");
            folder.setAccessible(true);
            Object previous = folder.get(null);
            try {
                File scratch = new File(System.getProperty("quest.pilot.output", "."), "wire-client-cache");
                folder.set(null, scratch.getAbsolutePath() + File.separator);
                client = new Client(network);
            } finally { folder.set(null, previous); }
        } catch (ReflectiveOperationException error) { throw new IllegalStateException(error); }
    }

    public RPAction roundTrip(RPAction action) {
        network.received = null;
        client.send(action);
        if (network.received == null) { throw new IllegalStateException("No action survived the wire"); }
        return network.received;
    }

    @Override public void close() {
        Decoder.get().clear(null);
        StendhalClient.resetClient();
        ClientSingletonRepository.setClientFramework(null);
    }

    private static final class Client extends StendhalClient {
        Client(Network network) {
            super(new UserContext(), new PerceptionDispatcher());
            netMan = network;
        }
    }

    private static final class Network implements INetworkClientManagerInterface {
        RPAction received;
        public void finish() { }
        public InetSocketAddress getAddress() { return new InetSocketAddress("127.0.0.1", 0); }
        public Message getMessage(int timeout) { return null; }
        public boolean getConnectionState() { return true; }
        public void addMessage(Message message) {
            try {
                byte[] bytes = Encoder.get().encode(message);
                List<Message> decoded = Decoder.get().decode(null, bytes);
                if (decoded.size() != 1 || !(decoded.get(0) instanceof MessageC2SAction)) {
                    throw new IllegalStateException("Unexpected protocol response");
                }
                received = ((MessageC2SAction) decoded.get(0)).getRPAction();
            } catch (IOException | InvalidVersionException error) { throw new IllegalStateException(error); }
        }
    }
}
