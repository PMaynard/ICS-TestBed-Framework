package xyz.scada.testbed.node;

import org.openmuc.j60870.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HMIConnectInterrogate extends Thread {

    private static Logger LOGGER = null;

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        LOGGER = Logger.getLogger(HMIConnectInterrogate.class.getName());
    }

    private String host;
    private int port;
    private int commonAddress;
    private int time_sleep;

    private volatile Connection clientConnection;
    private BufferedReader is;

    HMIConnectInterrogate(String host, int port, int commonAddress, int time_sleep) {
        this.host = host;
        this.port = port;
        this.commonAddress = commonAddress;
        this.time_sleep = time_sleep;
    }

    private class ClientEventListener implements ConnectionEventListener {

        @Override
        public void newASdu(ASdu aSdu) {
            LOGGER.log(Level.INFO, "[TID{0}] {1}:{2} ASDU: {3}|{4}\n\t{5}", new Object[]{Thread.currentThread().getId(), host, port, aSdu.getTypeIdentification(), aSdu.getCauseOfTransmission(), aSdu});

//            for(InformationObject ob : aSdu.getInformationObjects()){
//                for(InformationElement[] ie : ob.getInformationElements()){
//                    LOGGER.log(Level.INFO, ie.toString());
//                }
//            }
        }

        @Override
        public void connectionClosed(IOException e) {
            String reason = "Unknown";
            if (!e.getMessage().isEmpty()) {
                reason = e.getMessage();
            }

            LOGGER.log(Level.INFO, " Received connection closed signal. Reason: {0}", reason);

//            try {
//                is.close();
//            } catch (IOException e1) {
//                LOGGER.log(Level.SEVERE, "Closing connection: {0}", e1);
//            }
        }
    }

    public void run() {
//            for (int i = 1000; i > 1; i += 1000) {

        LOGGER.log(Level.INFO, "[TID{0}] Connecting to: {1}:{2}", new Object[]{Thread.currentThread().getId(), host, port});
        System.out.println("Connecting to: " + host + ":" + port + " (IEC104)");

        InetAddress address;
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            LOGGER.log(Level.WARNING, "Unknown host: {0}", host);
            return;
        }
//                TODO: Reconnection attempts.
//            ClientConnectionBuilder clientConnectionBuilder = new ClientConnectionBuilder(address).setPort(port).setMaxUnconfirmedIPdusReceived(2);
//            int count = 3000;
//            int maxTries = -1;
//            while(true) {
//                try {
//                    clientConnection = clientConnectionBuilder.connect();
//                } catch (IOException e) {
//                    LOGGER.log(Level.SEVERE, "{0} {1}:{2}", new Object[]{e.toString(), host, port});
//                    LOGGER.log(Level.INFO, "Retrying connection in {0}s.\n", ((count/1000)%60));
//                    try {
//                        Thread.sleep(count);
//                    }catch (InterruptedException ez) {
//                        LOGGER.log(Level.INFO, "[TID{0}] Got interrupted!",Thread.currentThread().getId() );
//                    }
//                    count += 3000;
//                    if (count == maxTries) break;
//                }
//            }

        ClientConnectionBuilder clientConnectionBuilder = new ClientConnectionBuilder(address).setPort(port).setMaxUnconfirmedIPdusReceived(2);

        try {
            clientConnection = clientConnectionBuilder.connect();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to connect to remote host: {0}:{1}", new Object[]{host, port});
//            LOGGER.log(Level.SEVERE, e.toString(), e);
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                clientConnection.close();
            }
        });

        try {
            try {
                clientConnection.startDataTransfer(new ClientEventListener(), 5000);
            } catch (TimeoutException e2) {
                throw new IOException(" Starting data transfer timed out.");
            }

            LOGGER.log(Level.INFO, "[TID{0}] Successfully connected", Thread.currentThread().getId());

            while (true) {
                LOGGER.log(Level.INFO, " Sent Interrogation Command.");

                clientConnection.interrogation(commonAddress, CauseOfTransmission.ACTIVATION, new IeQualifierOfInterrogation(20));

                try {
                    Thread.sleep(time_sleep);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.INFO, " Got interrupted!");
                }
                clientConnection.synchronizeClocks(commonAddress, new IeTime56(System.currentTimeMillis()));
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Connection closed for the following reason: {0}", e.getMessage());
            return;
        } finally {
            clientConnection.close();
        }

//                try {
//                    LOGGER.log(Level.INFO, "Retrying connection in {0}ms.\n", i);
//                    Thread.sleep(i);
//                } catch (InterruptedException e) {
//                    LOGGER.log(Level.INFO, "[TID{0}] Got interrupted!",Thread.currentThread().getId() );
//                }
//            }

    }
}
