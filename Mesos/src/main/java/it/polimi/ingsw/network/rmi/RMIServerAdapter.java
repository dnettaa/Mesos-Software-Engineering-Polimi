package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.network.message.ClientMessage;
import it.polimi.ingsw.network.message.ErrorMessage;
import it.polimi.ingsw.network.message.ServerMessage;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Server-side RMI adapter.
 * <p>
 * This object is registered in the RMI registry and receives remote calls
 * from RMI clients. For each connected client it creates an internal
 * {@link VirtualView}, so the controller can interact with RMI clients
 * exactly as it does with Socket clients.
 */
public class RMIServerAdapter extends UnicastRemoteObject implements ServerRMI {

    private static final int OUTBOX_CAPACITY = 100;

    private final GameController controller;
    private final Map<ClientRMI, RMIClientConnection> connections;

    /**
     * Creates a new RMI server adapter.
     *
     * @param controller the game controller used to handle client messages
     * @throws RemoteException if the remote object cannot be exported
     */
    public RMIServerAdapter(GameController controller) throws RemoteException {
        super();
        this.controller = controller;
        this.connections = new ConcurrentHashMap<>();
    }

    @Override
    public void connect(ClientRMI client) throws RemoteException {
        connections.putIfAbsent(client, new RMIClientConnection(client));
    }

    @Override
    public void sendMessage(ClientMessage message, ClientRMI sender) throws RemoteException {
        RMIClientConnection connection = connections.get(sender);

        if (connection == null || !connection.isConnected()) {
            throw new RemoteException("Unknown or disconnected RMI client.");
        }

        if (connection.getNickname() != null && !connection.getNickname().equals(message.getNickname())){
            connection.send(new ErrorMessage("INVALID_SENDER", "This connection is not associated with nickname " + message.getNickname()));
            return;
        }

        message.execute(controller, connection);
    }

    @Override
    public void disconnect(ClientRMI client) throws RemoteException {
        RMIClientConnection connection = connections.remove(client);

        if (connection != null) {
            String nickname = connection.getNickname();
            connection.disconnect();

            if (nickname != null) {
                controller.onDisconnect(nickname);
            }
        }
    }

    /**
     * Internal server-side representation of one RMI client.
     * <p>
     * This class implements {@link VirtualView}, so it can be registered
     * inside the controller like any other connected client.
     */
    private class RMIClientConnection implements VirtualView {

        private final ClientRMI clientStub;
        private final BlockingQueue<ServerMessage> outbox;
        private final Thread writerThread;

        private volatile boolean connected;
        private volatile String nickname;

        private RMIClientConnection(ClientRMI clientStub) {
            this.clientStub = clientStub;
            this.connected = true;
            this.outbox = new LinkedBlockingQueue<>(OUTBOX_CAPACITY);
            this.writerThread = new Thread(this::writerLoop, "RMIWriter");
            this.writerThread.start();
        }

        @Override
        public String getNickname() {
            return nickname;
        }

        @Override
        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        @Override
        public void send(ServerMessage message) {
            if (!connected) {
                return;
            }

            boolean accepted = outbox.offer(message);

            if (!accepted) {
                connected = false;

                if (nickname != null) {
                    controller.onDisconnect(nickname);
                }

                disconnect();
            }
        }

        @Override
        public void disconnect() {
            connected = false;
            writerThread.interrupt();
        }

        @Override
        public boolean isConnected() {
            return connected;
        }

        /**
         * Sends queued server messages to the remote client.
         * <p>
         * The blocking RMI call is performed here instead of inside
         * {@link #send(ServerMessage)}, so the controller is never blocked
         * by a slow or unreachable client.
         */
        private void writerLoop() {
            try {
                while (connected && !Thread.currentThread().isInterrupted()) {
                    ServerMessage message = outbox.take();
                    clientStub.receiveMessage(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (RemoteException e) {
                if (connected) {
                    connected = false;

                    if (nickname != null) {
                        controller.onDisconnect(nickname);
                    }
                }
            }
        }
    }
}