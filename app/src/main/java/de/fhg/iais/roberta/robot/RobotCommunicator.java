package de.fhg.iais.roberta.robot;

import android.content.Intent;
import android.util.Log;

import org.json.JSONObject;

//*************************************************************************************************
public class RobotCommunicator implements RobotCommunicatorInterface
{
    //---------------------------------------------------------------------------------------------
    public String TAG = RobotCommunicator.class.getSimpleName();
    public String ROBOT = "OpenRoberta";

    //---------------------------------------------------------------------------------------------
    public RobotCommunicator()
    {
        Log.d( TAG, "initialized" );
    }

    //---------------------------------------------------------------------------------------------
    @Override
    public void jsToRobot( JSONObject msg )
    {
        Log.d( TAG, "jsToRobot: " + msg.toString() );
    }

    //---------------------------------------------------------------------------------------------
    @Override
    public void handleActivityResult( int requestCode,
                                      int resultCode,
                                      Intent intent )
    {
    }

    //---------------------------------------------------------------------------------------------
    public void handleRequestPermissionsResult( int requestCode,
                                                String[] permissions,
                                                int[] grantResults )
    {
    }

    //---------------------------------------------------------------------------------------------
    @Override
    public void open()
    {
        Log.d( TAG, "open" );
    }

   //---------------------------------------------------------------------------------------------
    @Override
    public void close()
    {
        Log.d( TAG, "close" );
    }
}
