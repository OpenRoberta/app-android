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
import android.content.Intent;
import android.hardware.usb.UsbManager;

import com.ORB_App.ORB.ORB_RemoteBT;
import com.ORB_App.ORB.ORB_RemoteHandler;
import com.ORB_App.ORB.ORB_RemoteUSB;

//*******************************************************************
public class ORB_Manager extends ORB_RemoteHandler implements Runnable
{
    public  ORB_RemoteUSB orb_USB;
   public ORB_RemoteBT orb_BT;

    Thread mainThread;
  public  boolean runMainThread = false;
    //---------------------------------------------------------------
    public ORB_Manager(Activity activity)
    {
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
    public void init()
    {
        mainThread = new Thread(this);


        if( mainThread.getState() == Thread.State.NEW )
        {
            runMainThread = true;
            mainThread.start();
            mainThread.setPriority(2);
        }
    }

    //---------------------------------------------------------------
    public void open( )
    {
       // orb_USB.open();
    }

    //---------------------------------------------------------------
    public void close()
    {
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
        return( super.update() );
    }

    //-----------------------------------------------------------------
    @Override
    public
    void run()
    {
        while ( runMainThread )
        {
            //read from / write to board ======================================================================
            update();

            try
            {
                //Thread.sleep(1);
                Thread.sleep(5);
            }
            catch (InterruptedException e)
            {
            }
        }
    }

    //---------------------------------------------------------------
    // Motor
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    public void  configMotor( int   id,
                              int   ticsPerRotation,
                              int   acc,
                              int   Kp,
                              int   Ki )
    {
        configToORB.configMotor(id,ticsPerRotation,acc,Kp,Ki);
    }

    //---------------------------------------------------------------
    public void setMotor( int id,
                          int mode,
                          int speed,
                          int pos )
    {
        propToORB.setMotor(id, mode, speed, pos);
    }

    //---------------------------------------------------------------
    public short getMotorPwr( byte id )
    {
        return( propFromORB.getMotorPwr(id) );
    }

    //---------------------------------------------------------------
    public short getMotorSpeed( byte id )
    {
        return( propFromORB.getMotorSpeed(id) );
    }

    //---------------------------------------------------------------
    public int getMotorPos( byte id )
    {
        return( propFromORB.getMotorPos(id) );
    }

    //---------------------------------------------------------------
    // ModellServo
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    public void setModelServo( int id,
                               int  speed,
                               int  angle )
    {
        propToORB.setModelServo(id,speed,angle);
    }

    //---------------------------------------------------------------
    // Sensor
    //---------------------------------------------------------------
    public void configSensor( byte id,
                              byte type,
                              byte mode,
                              byte option )
    {
        configToORB.configSensor(id,type,mode,option);
    }

    //---------------------------------------------------------------
    public boolean getSensorValid( byte id )
    {
        return( propFromORB.getSensorValid(id) );
    }

    //---------------------------------------------------------------
    public int getSensorValue( byte id )
    {
        return( propFromORB.getSensorValue(id) );
    }

    //---------------------------------------------------------------
    public int getSensorValueExt( byte id, byte ch )
    {
        return( propFromORB.getSensorValueAnalog(id, ch));
    }

    //---------------------------------------------------------------
    public boolean getSensorValueDigital( byte id, byte ch )
    {
        return( propFromORB.getSensorValueDigital(id, ch));
    }

    //---------------------------------------------------------------
    public boolean getSensorDigital( byte id )
    {
        return( propFromORB.getSensorDigital(id));
    }

    //---------------------------------------------------------------
    // Miscellaneous
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    public float getVcc()
    {
        return( propFromORB.getVcc());
    }

    //---------------------------------------------------------------
    public byte getStatus()
    {
        return( propFromORB.getStatus());
    }

    //-----------------------------------------------------------------
}
