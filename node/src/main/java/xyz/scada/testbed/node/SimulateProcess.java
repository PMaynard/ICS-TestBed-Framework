package xyz.scada.testbed.node;


import java.text.SimpleDateFormat;
import java.util.Date;

public class SimulateProcess {
    public static String getProcess() {
        Date date = new Date();

        SimpleDateFormat format = new SimpleDateFormat("HH.SS");
        return format.format(date);
    }

    public static Float getProcessFloat() {
        Date date = new Date();

        SimpleDateFormat format = new SimpleDateFormat("HH.SS");
        return Float.parseFloat(format.format(date));
    }

    public static Integer getProcessInt() {
        Date date = new Date();

        SimpleDateFormat format = new SimpleDateFormat("HHSS");
        return Integer.parseInt(format.format(date));
    }
}
