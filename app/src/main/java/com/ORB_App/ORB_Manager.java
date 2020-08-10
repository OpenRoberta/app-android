//*******************************************************************
/*!
\file   ORBManager.java
\author Thomas Breuer
\date   21.02.2020
\brief
*/

//*******************************************************************
package com.ORB_App;

//*******************************************************************

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbManager;

import com.ORB_App.ORB.ORB_RemoteBT;
import com.ORB_App.ORB.ORB_RemoteHandler;
import com.ORB_App.ORB.ORB_RemoteUSB;

import de.fhg.iais.roberta.main.ORLabActivity;


//*******************************************************************
public class ORB_Manager extends ORB_RemoteHandler implements Runnable {
    private final int trackWidth = 18;

    public ORB_RemoteUSB orb_USB;
    public ORB_RemoteBT orb_BT;
    private ORLabActivity orLabActivity;


    private static final int POWER_MODE = 0;
    private static final int BRAKE_MODE = 1;
    private static final int SPEED_MODE = 2;
    private static final int MOVETO_MODE = 3;

    private static final int FORWARD = 0;
    private static final int BACKWARD = 1;

    private static final int EV3_MOTOR = 72;

    Thread mainThread;
    public boolean runMainThread = false;

    //---------------------------------------------------------------
    public ORB_Manager(Activity activity, ORLabActivity orLabActivity) {
        this.orLabActivity = orLabActivity;
        orb_USB = new ORB_RemoteUSB(this);
        orb_BT = new ORB_RemoteBT(this);

        orb_USB.init((UsbManager) activity.getSystemService(Context.USB_SERVICE));
        orb_BT.init();
    }

    //---------------------------------------------------------------
    public boolean isConnectionReady() {
        // TODO: check and return connection
        //
        return orb_USB.isConnected();
        // return true;
    }

    //---------------------------------------------------------------
    public void init() {
        mainThread = new Thread(this);


        if (mainThread.getState() == Thread.State.NEW) {
            runMainThread = true;
            mainThread.start();
            mainThread.setPriority(2);
        }
    }

    //---------------------------------------------------------------
    public void open() {
        // orb_USB.open();
    }

    //---------------------------------------------------------------
    public void close() {
        runMainThread = false;
        orb_USB.close();
        orb_BT.close();
    }

    //---------------------------------------------------------------
    @Override
    public boolean update() {
        if (orb_USB.isConnected()) {
            setORB_Remote(orb_USB);
        } else {
            setORB_Remote(orb_BT);
        }
        return (super.update());
    }

    //-----------------------------------------------------------------
    @Override
    public void run() {
        while (runMainThread) {
            //read from / write to board ======================================================================
            update();

            try {
                //Thread.sleep(1);
                Thread.sleep(5);
            } catch (InterruptedException e) {
            }
        }
    }

    //---------------------------------------------------------------
    // Motor
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    public void configMotor(int id,
                            int ticsPerRotation,
                            int acc,
                            int Kp,
                            int Ki) {
        configToORB.configMotor(id, ticsPerRotation, acc, Kp, Ki);
    }

    //---------------------------------------------------------------
    public void setMotor(int id,
                         int mode,
                         int speed,
                         int pos) {
        propToORB.setMotor(id, mode, speed, pos);
    }

    //---------------------------------------------------------------
    public short getMotorPwr(byte id) {
        return (propFromORB.getMotorPwr(id));
    }

    //---------------------------------------------------------------
    public short getMotorSpeed(byte id) {
        return (propFromORB.getMotorSpeed(id));
    }

    //---------------------------------------------------------------
    public int getMotorPos(byte id) {
        return (propFromORB.getMotorPos(id));
    }

    //TODO Erstmal mit Überschreiben, ob die Funktion in der Form verwendet werden hängt davon ab, wie es Serverseitig gemacht wird
    public void startConfigMotor(int id_1) {
        configMotor(id_1, EV3_MOTOR, 50, 30, 30);
    }

    public void startConfigMotor(int id_1, int id_2) {
        configMotor(id_1, EV3_MOTOR, 50, 30, 30);
        configMotor(id_2, EV3_MOTOR, 50, 30, 30);
    }

    public void startConfigMotor(int id_1, int id_2, int id_3) {
        configMotor(id_1, EV3_MOTOR, 50, 30, 30);
        configMotor(id_2, EV3_MOTOR, 50, 30, 30);
        configMotor(id_3, EV3_MOTOR, 50, 30, 30);
    }

    public void startConfigMotor(int id_1, int id_2, int id_3, int id_4) {
        configMotor(id_1, EV3_MOTOR, 50, 30, 30);
        configMotor(id_2, EV3_MOTOR, 50, 30, 30);
        configMotor(id_3, EV3_MOTOR, 50, 30, 30);
        configMotor(id_4, EV3_MOTOR, 50, 30, 30);
    }


    //Stop-----------------------------------------------------------
    public void MotorStop(int id) {
        setMotor(id, POWER_MODE, 0, 0);
    }

    //MotorOn--------------------------------------------------------
    public void motorOn(int id, int speed) {
        setMotor(id, SPEED_MODE, speed, 0);
    }

    //Drive-------------------------------------------------------------
    // Alles Int weil setMotor  braucht speed in int
    // Erst Annahme 1 cm = 72 ticsPerRotation in alle Funktionen, es muss noch geändert werden
    public void drive(int id_1, int id_2, int speed) {
        setMotor(id_1, SPEED_MODE, speed, 0);
        setMotor(id_2, SPEED_MODE, speed, 0);
    }

    //Drive_Dis-------------------------------------------------------------
    public void driveDis(int id_1, int id_2, int speed, int cm) throws InterruptedException {
        int move_to = cm * 72;//ticsPerCm 12,86 = 1cm, TODO noch ungenau, muss angepasst werden, bei alle Funktionen.
        int akutelle_pos_1 = getMotorPos((byte) id_1);
        int akutelle_pos_2 = getMotorPos((byte) id_2);
        //orLabActivity.show_Toast("getMotorPos((byte)id_1):" + getMotorPos((byte) id_1) + "getMotorPos((byte)id_2)" + getMotorPos((byte) id_2));
        while (true) {
            //orLabActivity.show_Toast(" getMotorPos((byte)id_1):" + getMotorPos((byte) id_1) + " move_to:" + move_to);
            setMotor(id_1, MOVETO_MODE, speed, akutelle_pos_1 + move_to);
            setMotor(id_2, MOVETO_MODE, speed, akutelle_pos_2 + move_to);
            if (getMotorPos((byte) id_1) + 13 > (akutelle_pos_1 + move_to)) {
                break;
            }
        }
    }

    //Turn-----------------------------------------------------------
    public void turn(int id_1, int id_2, int speed, String RorL) {
        if (RorL == "right") {
            setMotor(id_1, SPEED_MODE, speed, 0);
        }
        if (RorL == "left") {
            setMotor(id_2, SPEED_MODE, speed, 0);
        }
    }

    //Turn-----------------------------------------------------------
    public void turnDegree(int id_1, int id_2, int speed, String RorL, int degree) {
        int move_to = degree * 72;//ticsPerDegree, erst ein Degree = 72
        if (RorL == "right") {
            int akutelle_pos = getMotorPos((byte) id_1);
            while (akutelle_pos != move_to) {
                setMotor(id_1, SPEED_MODE, speed, 0);
                akutelle_pos = getMotorPos((byte) id_1);
            }
        }
        if (RorL == "left") {
            int akutelle_pos = getMotorPos((byte) id_2);
            while (akutelle_pos != move_to) {
                setMotor(id_2, SPEED_MODE, speed, 0);
                akutelle_pos = getMotorPos((byte) id_2);
            }
        }
    }

    //Steer----------------------------------------------------------
    public void steer(int id_1, int id_2, int speed_1, int speed_2, int direction) {
        //TODO set motor speed int, somit alle speed in Funktionen int, veraendern ?
        if (direction == BACKWARD) {
            int radius = calculateRadius(speed_1, speed_2);
            int Lspeed = calculateSpeedDriveInCurve(speed_1, speed_2);
            int Aspeed = (int) (Lspeed / radius * 180.0 / Math.PI);
            setMotor(id_1, SPEED_MODE, -1 * Lspeed, 0);
            setMotor(id_2, SPEED_MODE, -1 * Aspeed, 0);
        }
        if (direction == FORWARD) {
            int radius = calculateRadius(speed_1, speed_2);
            int Lspeed = calculateSpeedDriveInCurve(speed_1, speed_2);
            int Aspeed = (int) (Lspeed / radius * 180.0 / Math.PI);
            setMotor(id_1, SPEED_MODE, 1 * Lspeed, 0);
            setMotor(id_2, SPEED_MODE, 1 * Aspeed, 0);
        }
    }

    //Steer_dis------------------------------------------------------
    public void steerDis(int id_1, int id_2, int speed_1, int speed_2, int cm, int direction) {
        int move_to = cm * 72;//ticsPerCm
        int akutelle_pos_1 = getMotorPos((byte) id_1);
        int akutelle_pos_2 = getMotorPos((byte) id_2);
        if (direction == BACKWARD) {
            int radius = calculateRadius(speed_1, speed_2);
            int Lspeed = calculateSpeedDriveInCurve(speed_1, speed_2);
            int Aspeed = (int) (Lspeed / radius * 180.0 / Math.PI);
            for (int i = 0; i < cm; i++) {
                setMotor(id_1, SPEED_MODE, -1 * Lspeed, akutelle_pos_1 + move_to);
                setMotor(id_2, SPEED_MODE, -1 * Aspeed, akutelle_pos_2 + move_to);
            }
        }
        if (direction == FORWARD) {
            int radius = calculateRadius(speed_1, speed_2);
            int Lspeed = calculateSpeedDriveInCurve(speed_1, speed_2);
            int Aspeed = (int) (Lspeed / radius * 180.0 / Math.PI);
            for (int i = 0; i < cm; i++) {
                setMotor(id_1, SPEED_MODE, 1 * Lspeed, akutelle_pos_1 + move_to);
                setMotor(id_2, SPEED_MODE, 1 * Aspeed, akutelle_pos_2 + move_to);
            }
        }
    }

    private int calculateRadius(int speedLeft, int speedRight) {
        int radius = this.trackWidth * (speedLeft + speedRight) / (2 * (speedRight - speedLeft));
        return radius;
    }

    private int calculateSpeedDriveInCurve(int speedLeft, int speedRight) {
        return (speedLeft + speedRight) / 2;
    }

    //TODO sleep bauen
    public void testMotors(int id, int speed) throws InterruptedException {
        orLabActivity.show_Toast("motorOn");
        motorOn(id, speed);
        //mainThread.sleep(5000);
        orLabActivity.show_Toast("driveDis");
        driveDis(id, id + 1, speed, 10);
        //mainThread.sleep(5000);
        orLabActivity.show_Toast("turn");
        turn(id, id + 1, speed, "right");
        //mainThread.sleep(5000);
        orLabActivity.show_Toast("turnDegree");
        turnDegree(id, id + 1, speed, "left", 20);
        //mainThread.sleep(5000);
        orLabActivity.show_Toast("steer");
        steer(id, id + 1, speed, 200, 1);
        //mainThread.sleep(5000);
        orLabActivity.show_Toast("steerDis");
        steerDis(id, id + 1, speed, 200, 100, 1);
        //mainThread.sleep(5000);
    }

    //---------------------------------------------------------------
    // ModellServo
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    public void setModelServo(int id,
                              int speed,
                              int angle) {
        propToORB.setModelServo(id, speed, angle);
    }

    //---------------------------------------------------------------
    // Sensor
    //---------------------------------------------------------------
    public void configSensor(byte id,
                             byte type,
                             byte mode,
                             byte option) {
        configToORB.configSensor(id, type, mode, option);
    }

    //---------------------------------------------------------------
    public boolean getSensorValid(byte id) {
        return (propFromORB.getSensorValid(id));
    }

    //---------------------------------------------------------------
    public int getSensorValue(byte id) {
        return (propFromORB.getSensorValue(id));
    }

    //---------------------------------------------------------------
    public int getSensorValueExt(byte id, byte ch) {
        return (propFromORB.getSensorValueAnalog(id, ch));
    }

    //---------------------------------------------------------------
    public boolean getSensorValueDigital(byte id, byte ch) {
        return (propFromORB.getSensorValueDigital(id, ch));
    }

    //---------------------------------------------------------------
    public boolean getSensorDigital(byte id) {
        return (propFromORB.getSensorDigital(id));
    }

    //---------------------------------------------------------------
    public void sensor_test(int ports, int typ, int number_mods) throws InterruptedException {
        mainThread.sleep(5000);
        for (int j = 0; j < number_mods; j++) {
            for (int i = 0; i < ports; i++) {
                configSensor((byte) i, (byte) typ, (byte) j, (byte) 0);
                orLabActivity.show_Toast("Port " + i + " Mod " + j + " Value: " + getSensorValue((byte) i) + " Valid: " + getSensorValid((byte) i));
            }
        }
    }

    //---------------------------------------------------------------
    // Miscellaneous
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    public float getVcc() {
        return (propFromORB.getVcc());
    }

    //---------------------------------------------------------------
    public byte getStatus() {
        return (propFromORB.getStatus());
    }

    //-----------------------------------------------------------------
}
