package de.fhg.iais.roberta.device.AndroidSensor;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.function.Consumer;

import de.fhg.iais.roberta.robot.orb.ORB_Communicator;

public class AndroidSensor_Manager
{
    //--------------------------------------------------------------------------------------------
    private SensorManager sensorManager;
    private ArrayList<AndroidSensor_Sensor> list;

    ORB_Communicator orb_report;

    public int type = -1;

    private String TAG = AndroidSensor_Manager.class.getSimpleName();

    //--------------------------------------------------------------------------------------------
    public AndroidSensor_Manager( ORB_Communicator orb_report, Activity activity)
    {
        this.orb_report = orb_report;
        sensorManager = (SensorManager) activity.getSystemService( Context.SENSOR_SERVICE);
        list = new ArrayList<AndroidSensor_Sensor>();
    }

    //--------------------------------------------------------------------------------------------
    public void configSensor(int type, String name )
    {
        AndroidSensor_Sensor s = null;
        // Sensor mit gewuenschtem type in Liste?
        for( AndroidSensor_Sensor sTmp : list )
        {
            if( sTmp.getType() == type ) {
                s = sTmp;
                break;
            }
        }
        if( s == null )         // wenn nicht, erzeuge Sensor
        {
            AndroidSensor_Sensor sensor = new AndroidSensor_Sensor(sensorManager, type, name);
            if( sensor != null )
            {
                list.add(sensor);
            }
        }
        else                    // ... sonst setze Namen
        {
            s.setName( name );
        }
    }

    //--------------------------------------------------------------------------------------------
    public void reset()
    {
        list.clear();
    }

    //--------------------------------------------------------------------------------------------
    public void close()
    {
        for( AndroidSensor_Sensor sTmp : list )
        {
            sTmp.unregister();
        }
    }

    //--------------------------------------------------------------------------------------------
    public void update()
    {
        orb_report.reportAndroidSensor();
    }

    //--------------------------------------------------------------------------------------------
    public ArrayList<AndroidSensor_Sensor> getList()
    {
        return( list );
    }
}
