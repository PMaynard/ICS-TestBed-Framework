package xyz.scada.testbed.node;

import java.util.logging.Logger;


public final class HMI {

    // Default Values
    private String[] hosts = new String[]{"127.0.0.1"};
    private String hostsStr = "127.0.0.1";
    private int port = 2404;
    private int commonAddress = 0;
    private int time_sleep = 10000;

    private static Logger LOGGER = null;

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%n");
        LOGGER = Logger.getLogger(HMI.class.getName());
    }

    public void start() {
        for (String host : hosts) {
            HMIConnectInterrogate rtuN = new HMIConnectInterrogate(host, port, commonAddress, time_sleep);
            rtuN.start();
        }
    }

    public void setHosts(String[] hosts) {
        this.hosts = hosts;
        for(String host : hosts) this.hostsStr +=" " + host;
    }

    public void setCommonAddress(int commonAddress) {
        this.commonAddress = commonAddress;
    }

    public void setTime_sleep(int time_sleep) {
        this.time_sleep = time_sleep;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void show() {
        System.out.println("Hosts:\t " + this.hostsStr);
        System.out.println("IEC104 Common Address: " + this.commonAddress);
        System.out.println("Interval:\t " + this.time_sleep);
        System.out.println("IEC104 Port: " + this.port);
    }
}
