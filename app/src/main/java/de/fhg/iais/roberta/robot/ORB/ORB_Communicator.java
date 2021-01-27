//*******************************************************************
/*!
\file   ORB_Remote_Handler.java
\author Thomas Breuer
\date   21.02.2019
\brief
*/

//*******************************************************************
package de.fhg.iais.roberta.robot.ORB;

//*******************************************************************
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.webkit.WebView;

import com.ORB_App.ORB_Manager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Set;

import de.fhg.iais.roberta.main.ORLabActivity;
import de.fhg.iais.roberta.robot.RobotCommunicator;

//*******************************************************************
public class ORB_Communicator extends RobotCommunicator
{
    //---------------------------------------------------------------
    private static final String TAG = ORB_Communicator.class.getSimpleName();

    private ORLabActivity orLabActivity;
    private WebView       mainView;
    private ORB_Manager   orbManager;

    //---------------------------------------------------------------
    public ORB_Communicator(ORLabActivity orLabActivity, WebView mainView )
    {
        this.orLabActivity = orLabActivity;
        this.mainView      = mainView;

        orbManager = new ORB_Manager(orLabActivity);
        orbManager.init();

        this.ROBOT = "orb";

        Log.d(TAG, "OrbCommunicator instantiated");
    }

    //---------------------------------------------------------------
    @Override
    public void open()
    {
        orbManager.open( );
    }

    //---------------------------------------------------------------
    @Override
    public void close()
    {
        orbManager.close();
    }

    //---------------------------------------------------------------
//TODO: function required?
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startScan()
    {
        if(orbManager.isConnectionReady())
        {
            reportStateChanged("connect", "connected", "0" /*BT_Device.getAddress()*/ ,"brickname", /*BT_Device.getName()+*/"ORB via USB");
//            reportStateChanged("scan", "appeared", "ORB Device ID", "brickname", "ORB Device Name");
        }
        else
        {
            //   reportScanError("USB device not connected");
            //   Util.showAlert(this.orLabActivity, "USB device not connected");
        }
    }

    //---------------------------------------------------------------
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void jsToRobot(JSONObject msg)
    {
        Log.d(TAG, "jsToRobot: " + msg.toString());
        try {
            String target = msg.getString("target");
            String type = msg.getString("type");
            if (target.equals(ROBOT))
            {
                switch (type)
                {
                    case "startScan":
                        //startScan();
                        if(orbManager.isConnectionReady())
                        {
                            reportStateChanged("connect", "connected", "0" // device address()
                                    , "brickname", "ORB via USB");
                        }
                        else
                        {
                            if( orbManager.orb_USB.isAvailable()  )
                            {
                                reportStateChanged("scan", "appeared", "USB" // device address()
                                        , "brickname", "ORB via USB");
                            }
                            else // check BT
                            {
//TODO: move BT stuff to ORB_RemoteBT
                                BluetoothAdapter BT_Adapter;
                                Set<BluetoothDevice> BT_PairedDevices;

                                BT_Adapter = BluetoothAdapter.getDefaultAdapter();
                                BT_PairedDevices = BT_Adapter.getBondedDevices();

                                ArrayList<String> list = new ArrayList<String>();

                                for (BluetoothDevice lBT_Device : BT_PairedDevices) {
                                    reportStateChanged("scan", "appeared", lBT_Device.getAddress(), "brickname", lBT_Device.getName());
                                }
                            }
                        }
                        break;

                    case "stopScan":

                        // TODO:
                        // diese Funktion sollte eigentlich an anderer Stelle aufgerufen werden,
                        // aber "connect" wird nicht ausgefuehrt. Fehler im JS ?
                        // ****************************************
                        // orbManager.orb_USB.open(orLabActivity);
                        // ****************************************

                        Log.d(TAG, "stop scan is not implemented for orb robot");
                        break;

                    case "connect":
                    {
                        String addr = msg.getString("robot");
                        orbManager.orb_USB.close();
                        orbManager.orb_BT.close();

                        if (addr.length() > 0) {
                            if (addr.equals("USB")) {
                                orbManager.orb_USB.open(orLabActivity);
                                reportStateChanged("connect", "connected", "USB", "brickname", "ORB via USB");
                            }
                            else
                            {
                                BluetoothDevice BT_Device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(addr);
                                orbManager.orb_BT.open(BT_Device);
                                reportStateChanged("connect", "connected", BT_Device.getAddress(), "brickname", BT_Device.getName());
                            }
                        }
                    }
                    break;

                    case "disconnect":
                        Log.d(TAG, "disconnect is not implemented for orb robot");
                        break;

                    case "data":
                        if( msg.has("propToORB") )
                        {
                            JSONObject propToORB = msg.getJSONObject("propToORB");

                            JSONArray motor = propToORB.getJSONArray("Motor");

                            synchronized (orbManager.propToORB )
                            {
                                for (int i = 0; i < motor.length(); i++)
                                {
                                    JSONObject m = motor.getJSONObject(i);
                                    orbManager.setMotor(i, m.getInt("mode"), m.getInt("speed"), m.getInt("pos"));
                                }
                            }

                            JSONArray servo= propToORB.getJSONArray("Servo");

                            synchronized (orbManager.propToORB )
                            {
                                for (int i = 0; i < servo.length(); i++)
                                {
                                    JSONObject s = servo.getJSONObject(i);
                                    orbManager.setModelServo(i, s.getInt("mode"), s.getInt("pos"));
                                }
                            }

                            /// reply
                            //    JSONObject out = new JSONObject();
                            //    robotToJs( out );

                            //    final JSONObject answer = out;
                            //    sendToJS( answer.toString() );
                        }
                        else if( msg.has("configToORB") )
                        {
                            JSONObject configToORB = msg.getJSONObject("configToORB");
                            JSONArray motor= configToORB.getJSONArray("Motor");

                            synchronized (orbManager.configToORB )
                            {
                                for(byte i=0;i<motor.length();i++)
                                {
                                    JSONObject m = motor.getJSONObject(i);
                                    orbManager.configMotor(i,m.getInt("tics"),m.getInt("acc"),m.getInt("Kp"),m.getInt("Ki"));
                                }
                            }

                            JSONArray sensor= configToORB.getJSONArray("Sensor");

                            synchronized (orbManager.configToORB )
                            {
                                for(byte i=0;i<sensor.length();i++)
                                {
                                    JSONObject s = sensor.getJSONObject(i);
                                    orbManager.configSensor(i,(byte)s.getInt("type"),(byte)s.getInt("mode"),(short)s.getInt("option"));
                                }
                            }
                        }
                        break;

                    default:
                        Log.e(TAG, "Not supported msg: " + msg);
                        break;
                } //  switch (type)
            }

        } catch (final JSONException e) {
            // ignore invalid messages
            Log.e(TAG, "Json parsing error: " + e.getMessage() + " processing: " + msg);
        } catch (final NullPointerException e) {
            Log.e(TAG, "Command parsing error: " + e.getMessage() + " processing: " + msg);
        }catch (final Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage() + " processing: " + msg);
        }
    }

    //---------------------------------------------------------------
    public void report()
    {
        JSONObject out = new JSONObject();
        robotToJs(out);
        final JSONObject answer = out;
        sendToJS(answer.toString());
    }

    //---------------------------------------------------------------
    public void reportDisconnect()
    {
        reportStateChanged("connect", "disconnected", "orb");
    }



    //---------------------------------------------------------------
    public void robotToJs( JSONObject out )
    {
        try
        {
            out.put("target", "orb");
            out.put("type", "data");

            JSONObject prop = new JSONObject();

            JSONArray motor = new JSONArray();
            for (byte i = 0; i < 4; i++)
            {
                JSONObject m = new JSONObject();
                m.put("pwr", orbManager.getMotorPwr(i));
                m.put("speed", orbManager.getMotorSpeed(i));
                m.put("pos", orbManager.getMotorPos(i));
                motor.put(m);
            }
            prop.put("Motor", motor);

            JSONArray sensor = new JSONArray();
            for (byte i = 0; i < 4; i++)
            {
                JSONObject s = new JSONObject();
                s.put("valid",  orbManager.getSensorValid(i));
                s.put("type",   orbManager.getSensorType(i));
                s.put("option", orbManager.getSensorOption(i));
                s.put("value",  orbManager.getSensorValue(i));
                sensor.put(s);
            }
            prop.put("Sensor", sensor);

            prop.put("Vcc", orbManager.getVcc());

            JSONArray b = new JSONArray();
            b.put(orbManager.getSensorDigital((byte)0));
            b.put(orbManager.getSensorDigital((byte)1));

            prop.put("Digital", b);

            prop.put("Status",orbManager.getStatus());  // TODO: get status

            out.put("propFromORB", prop);


        } catch (JSONException e) {
            Log.e(TAG, "Json parsing error: " + e.getMessage());
            // nothing to do, msg/answer is save but empty
        }
    }

    // communication with webview ------------------------------------------------------------------
    /**
     * Report to the webview new state information.
     *
     * @param strg, min. type, state and brickid in this order are reqired (3 arguments).
     *  All next arguments have to appear in pairs, key <-> value
     */
    @Override
    public void reportStateChanged(String type, String state, String brickid, String... strg)
    {
        try {
            if (type != null && state != null && brickid != null) {
                JSONObject newMsg = new JSONObject();
                newMsg.put("target", ROBOT);
                newMsg.put("type", type);
                newMsg.put("state", state);
                newMsg.put("brickid", brickid);
                if (strg != null) {
                    for (int i = 0; i < strg.length; i += 2) {
                        newMsg.put(strg[i], strg[i + 1]);
                    }
                }
                sendToJS( newMsg.toString() );

            } else {
                throw new IllegalArgumentException("Min. 3 parameters required + additional parameters in pairs!");
            }
        } catch (JSONException | IllegalArgumentException | IndexOutOfBoundsException e) {
            Log.e(TAG, e.getMessage() + "caused by: " + type + state + brickid + strg);
        }
    }

    //---------------------------------------------------------------
    private void reportScanError(String msg)
    {
        try {
            final JSONObject newMsg = new JSONObject();
            newMsg.put("target", ROBOT);
            newMsg.put("type", "scan");
            newMsg.put("state", "error");
            newMsg.put("message", msg);
            sendToJS( newMsg.toString() );
        } catch (JSONException | IllegalArgumentException e) {
            Log.e(TAG, e.getMessage() + "caused by: scan " + msg);
        }
    }

    //---------------------------------------------------------------
    private void sendToJS( String str )
    {
        this.mainView.post(new Runnable()
        {
            @Override
            public void run()
            {
                mainView.loadUrl("javascript:webviewController.appToJsInterface('" + str + "')");
            }
        });
        Log.e(TAG, str );
    }

}  // end of class
