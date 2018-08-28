package xyz.scada.testbed.node;

import org.openmuc.j60870.*;

import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RTU {
    
    public RTU() { }

    private static Logger LOGGER = null;

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%n");
        LOGGER = Logger.getLogger(RTU.class.getName());
    }

    public class ServerListener implements ServerEventListener {

        public class ConnectionListener implements ConnectionEventListener {

            private final Connection connection;
            private final int connectionId;

            public ConnectionListener(Connection connection, int connectionId) {
                this.connection = connection;
                this.connectionId = connectionId;
            }

            public void newASdu(ASdu aSdu) {
                try {

                    switch (aSdu.getTypeIdentification()) {

                        case C_IC_NA_1:
                            ASdu s = new ASdu(TypeId.M_ME_NB_1, true, CauseOfTransmission.INTERROGATED_BY_STATION, false, false,
                                    0, aSdu.getCommonAddress(),
                                    new InformationObject[]{new InformationObject(1, new InformationElement[][]{
                                            {new IeScaledValue(SimulateProcess.getProcessInt()), new IeQuality(true, true, true, true, true)}})});
                            connection.send(s);
                            System.out.println(s.toString());

                            // When we receive an Activation CoT send
                            if (aSdu.getCauseOfTransmission() == CauseOfTransmission.ACTIVATION) {
                                connection.interrogation(aSdu.getCommonAddress(), CauseOfTransmission.ACTIVATION_TERMINATION, new IeQualifierOfInterrogation(20));
                            }
                            break;
                        case C_CS_NA_1:
                            connection.sendConfirmation(aSdu);
                            break;
                        default:
                            LOGGER.log(Level.INFO, "Got unknown request: " + aSdu + ". Will not confirm it.\n");
                            break;
                    }

                } catch (EOFException e) {
                    LOGGER.log(Level.SEVERE, "Will quit listening for commands on connection (" + connectionId
                            + ") because socket was closed.");
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Will quit listening for commands on connection (" + connectionId
                            + ") because of error: \"" + e.getMessage() + "\".");
                }

            }

            public void connectionClosed(IOException e) {
                LOGGER.log(Level.INFO, " Connection ID:{0} closed. {1}", new Object[]{connectionId, e.getMessage()});
            }

        }

        public void connectionIndication(Connection connection) {

            int myConnectionId = connectionIdCounter++;
            LOGGER.log(Level.INFO, " Client Connected ID:{0}", myConnectionId);

            try {
                connection.waitForStartDT(new ConnectionListener(connection, myConnectionId), 5000);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, " ID:{0} interrupted while waiting for StartDT:{1}", new Object[]{myConnectionId, e.getMessage()});
                return;
            } catch (TimeoutException e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
            }

            LOGGER.log(Level.INFO, " Handshake complete for ID:{0}", myConnectionId);

        }

        public void serverStoppedListeningIndication(IOException e) {
            LOGGER.log(Level.INFO, "Server has stopped listening for new connections : {}", e.getMessage());
        }

        public void connectionAttemptFailed(IOException e) {
            LOGGER.log(Level.INFO, "Connection attempt failed: {0}", e.getMessage());
        }
    }

    private int connectionIdCounter = 1;

    public static void main(String[] args) {
        new RTU().start();
    }

    public void start() {
        Server server = new Server.Builder().build();

        try {
            server.start(new ServerListener());
            LOGGER.log(Level.INFO, " RTU Server started!");
            System.out.println("RTU Started.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to start listening: {0}", e.getMessage());
            return;
        }
    }

}
