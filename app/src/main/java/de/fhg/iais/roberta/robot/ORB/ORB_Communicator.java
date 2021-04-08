//*******************************************************************
/*!
\file   ORB_Communicator.java
\author Thomas Breuer
\date   21.02.2019
\brief
*/

//*******************************************************************
package de.fhg.iais.roberta.robot.ORB;

//*******************************************************************
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.webkit.WebView;

import com.ORB.ORB_Manager;
import com.ORB.ORB_Report;
import com.ORB.cPropFromORB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;

import de.fhg.iais.roberta.main.ORLabActivity;
import de.fhg.iais.roberta.robot.RobotCommunicator;

//*******************************************************************
public class ORB_Communicator extends RobotCommunicator implements ORB_Report
{
    //---------------------------------------------------------------
    private static final String TAG = ORB_Communicator.class.getSimpleName();
  //  private ORLabActivity orLabActivity;
    private WebView webView;
    private ORB_Manager orbManager;

    //---------------------------------------------------------------
    public ORB_Communicator(ORLabActivity orLabActivity, WebView webView)
    {
        this.ROBOT = "orb";
     //   this.orLabActivity = orLabActivity;
        this.webView = webView;

        orbManager = new ORB_Manager( orLabActivity, this );

        Log.d( TAG, "OrbCommunicator instantiated" );
    }
    //---------------------------------------------------------------
    @Override
    public void open()
    {
        orbManager.open();
    }
    //---------------------------------------------------------------
    @Override
    public void close()
    {
        orbManager.close();
    }
    //---------------------------------------------------------------
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void jsToRobot( JSONObject msg )
    {
        //Log.d( TAG, "jsToRobot: " + msg.toString() );
        try
        {
            String target = msg.getString( "target" );
            String type = msg.getString( "type" );

            if( !target.equals( ROBOT ) )
            {
                Log.e( TAG, "jsToRobot: " + "wrong target" );
                return; // wrong robot
            }

            switch( type )
            {
                case "startScan":
                    handle_startScan( msg );
                    break;
                case "stopScan":
                    handle_stopScan( msg );
                    break;
                case "connect":
                    handle_connect( msg );
                    break;
                case "disconnect":
                    handle_disconnect( msg );
                    break;
                case "data":
                    handle_data( msg );
                    break;
                case "download":
                    handle_download( msg );
                    break;
                default:
                    Log.e( TAG, "Not supported msg: " + msg );
                    break;
            }
        }
        catch (final JSONException e)
        {
            // ignore invalid messages
            Log.e( TAG, "Json parsing error: " + e.getMessage() + " processing: " + msg );
        }
        catch (final NullPointerException e)
        {
            Log.e( TAG, "Command parsing error: " + e.getMessage() + " processing: " + msg );
        }
        catch (final Exception e)
        {
            Log.e( TAG, "Exception: " + e.getMessage() + " processing: " + msg );
        }
    }
    //---------------------------------------------------------------
    private void handle_startScan( JSONObject msg )
    {
        orbManager.scan();
    }
    //---------------------------------------------------------------
    private void handle_stopScan( JSONObject msg )
    {
        Log.e( TAG, "stop scan is not implemented for orb robot" );
    }
    //---------------------------------------------------------------
    private void handle_connect( JSONObject msg )
    {
        try
        {
            String addr = msg.getString( "robot" );
            orbManager.connect( addr );
        }
        catch (final JSONException e)
        {
            // ignore invalid messages
            Log.e( TAG, "Json parsing error: " + e.getMessage() + " processing: " + msg );
        }
    }
    //---------------------------------------------------------------
    private void handle_disconnect( JSONObject msg )
    {
        Log.d( TAG, "disconnect is not implemented for orb robot" );
    }
    //---------------------------------------------------------------
    private void handle_data(JSONObject msg )
    {
        try
        {
            if(msg.has("propToORB"))
            {
                JSONObject propToORB = msg.getJSONObject( "propToORB" );
                JSONArray motor = propToORB.getJSONArray( "Motor" );
                synchronized (orbManager)
                {
                    for( int i = 0; i < motor.length(); i++ )
                    {
                        JSONObject m = motor.getJSONObject( i );
                        orbManager.setMotor( i, m.getInt( "mode" ), m.getInt( "speed" ),
                                             m.getInt( "pos" ) );
                    }
                }
                JSONArray servo = propToORB.getJSONArray( "Servo" );
                synchronized (orbManager)
                {
                    for( int i = 0; i < servo.length(); i++ )
                    {
                        JSONObject s = servo.getJSONObject( i );
                        orbManager.setModelServo( i, s.getInt( "mode" ), s.getInt( "pos" ) );
                    }
                }
            }
            else if(msg.has("configToORB"))
            {
                JSONObject configToORB = msg.getJSONObject( "configToORB" );
                JSONArray motor = configToORB.getJSONArray( "Motor" );
                synchronized (orbManager)
                {
                    for( byte i = 0; i < motor.length(); i++ )
                    {
                        JSONObject m = motor.getJSONObject( i );
                        orbManager.configMotor( i, m.getInt( "tics" ), m.getInt( "acc" ), m.getInt( "Kp" ),
                                                m.getInt( "Ki" ) );
                    }
                }
                JSONArray sensor = configToORB.getJSONArray( "Sensor" );
                synchronized (orbManager)
                {
                    for( byte i = 0; i < sensor.length(); i++ )
                    {
                        JSONObject s = sensor.getJSONObject( i );
                        orbManager.configSensor( i, (byte) s.getInt( "type" ), (byte) s.getInt( "mode" ),
                                                 (short) s.getInt( "option" ) );
                    }
                }
            }
        }
        catch (final JSONException e)
        {
            // ignore invalid messages
            Log.e( TAG, "Json parsing error: " + e.getMessage() + " processing: " + msg );
        }
    }

    //---------------------------------------------------------------
    private void handle_download( JSONObject msg )
    {
        try
        {
            byte memoryID = -1;
            switch( msg.getString( "id" ) )
            {
                case "Prog":
                    memoryID = 0;
                    break;
                case "Flash":
                    memoryID = 1;
                    break;
                case "RAM":
                    memoryID = 2;
                    break;
            }

            JSONArray data = msg.getJSONArray( "data" );
            ByteBuffer payload = ByteBuffer.allocate(
                    32 * ((4 * data.length() + 31) / 32) ); // = new byte[4*data.length()];
            int k = 0;
            for(
                    int i = 0; i < data.length(); i++ )
            {
                payload.put( k++, (byte) ((data.getInt( i )) & 0xFF) );
                payload.put( k++, (byte) ((data.getInt( i ) >> 8) & 0xFF) );
                payload.put( k++, (byte) ((data.getInt( i ) >> 16) & 0xFF) );
                payload.put( k++, (byte) ((data.getInt( i ) >> 24) & 0xFF) );

            }
            if( !orbManager.download( memoryID, payload ) )
            {
                // Hier Fehlermeldung: Download laeuft bereits !!!
            }

        } catch( JSONException e )
        {
            Log.e( TAG, "Json parsing error: " + e.getMessage() + " processing: " + msg );
            e.printStackTrace();
        }
    }

    /**
     * Report to the webview new state information.
     *
     * @param strg, min. type, state and brickid in this order are reqired (3 arguments).
     *              All next arguments have to appear in pairs, key <-> value
     */
    //@Override
    private void reportStateChanged( String type, String state, String brickid, String... strg )
    {
        try
        {
            if( type != null && state != null && brickid != null )
            {
                JSONObject newMsg = new JSONObject();
                newMsg.put( "target", ROBOT );
                newMsg.put( "type", type );
                newMsg.put( "state", state );
                newMsg.put( "brickid", brickid );
                if( strg != null )
                {
                    for( int i = 0; i < strg.length; i += 2 )
                    {
                        newMsg.put( strg[i], strg[i + 1] );
                    }
                }
                sendToJS( newMsg.toString() );

            }
            else
            {
                throw new IllegalArgumentException(
                        "Min. 3 parameters required + additional parameters in pairs!" );
            }
        } catch( JSONException | IllegalArgumentException | IndexOutOfBoundsException e )
        {
            Log.e( TAG, e.getMessage() + "caused by: " + type + state + brickid + strg );
        }
    }

    //---------------------------------------------------------------
    @Override
    public void reportDisconnect()
    {
        reportStateChanged( "connect", "disconnected", "orb" );
    }

    //---------------------------------------------------------------
    @Override
    public void reportConnect(String brickId, String brickName)
    {
        reportStateChanged( "connect","connected",brickId, "brickname", brickName);
    }

    //---------------------------------------------------------------
    @Override
    public void reportScan(String brickId, String brickName)
    {
        reportStateChanged( "scan", "appeared", brickId, "brickname", brickName );
    }

    //---------------------------------------------------------------
    @Override
    public void reportDownload(String str)
    {
        reportStateChanged( "x", "y", "z", "download", str );
    }

    //---------------------------------------------------------------
    @Override
    public void report()
    {
        JSONObject msg = new JSONObject();
        robotToJs( msg );
        sendToJS( msg.toString() );
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
                cPropFromORB.Motor value = orbManager.getMotor(i);
                m.put("pwr", value.pwr);
                m.put("speed", value.speed);
                m.put("pos", value.pos);
                motor.put(m);
            }
            prop.put("Motor", motor);

            JSONArray sensor = new JSONArray();
            for (byte i = 0; i < 4; i++)
            {
                JSONObject s = new JSONObject();
                cPropFromORB.Sensor value = orbManager.getSensor(i);
                s.put("valid",  value.isValid);
                s.put("type",   value.type);
                s.put("option", value.option);
                JSONArray v = new JSONArray(  );
                  v.put( value.value[0] );
                  v.put( value.value[1]);
                s.put("value",v);
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

    //---------------------------------------------------------------
    private void sendToJS( String str )
    {
        this.webView.post( new Runnable()
        {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run()
            {
                webView.loadUrl( "javascript:webviewController.appToJsInterface('" + str + "')" );
            }
        } );

      //  Log.e( TAG, str );
    }

}  // end of class
