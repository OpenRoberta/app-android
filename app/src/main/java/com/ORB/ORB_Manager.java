//*******************************************************************
/*!
\file   ORB_Manager.java
\author Thomas Breuer
\date   21.02.2020
\brief
*/

//*******************************************************************
package com.ORB;

//*******************************************************************
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import java.nio.ByteBuffer;

import static java.lang.Thread.MAX_PRIORITY;
import static java.lang.Thread.NORM_PRIORITY;

//*******************************************************************
public class ORB_Manager  implements Runnable
{
    //---------------------------------------------------------------
    private cConfigToORB configToORB;
    private cPropToORB propToORB;
    private cPropFromORB propFromORB;
    private cDownloadToORB downloadToORB;
    private ORB_Report orb_report;
    private Thread mainThread;
    private boolean runMainThread = false;
    private int updateTimeout = 0;

    //---------------------------------------------------------------
    private ORB_RemoteUSB orb_USB;
    private ORB_RemoteBT orb_BT;

    //---------------------------------------------------------------
    public ORB_Manager( Activity activity, ORB_Report orb_report)
    {
        this.orb_report = orb_report;

        configToORB   = new cConfigToORB();
        propToORB     = new cPropToORB();
        propFromORB   = new cPropFromORB();
        downloadToORB = new cDownloadToORB();

        orb_USB = new ORB_RemoteUSB(activity, this);
        orb_BT  = new ORB_RemoteBT(this);

        mainThread = new Thread(this);
        if( mainThread.getState() == Thread.State.NEW )
        {
            runMainThread = true;
            mainThread.start();
            mainThread.setPriority( MAX_PRIORITY );
        }
    }

    //---------------------------------------------------------------
    public void open( )
    {
    }

    //---------------------------------------------------------------
    public void close()
    {
        runMainThread = false;
        orb_USB.close();
        orb_BT.close();
    }

    //---------------------------------------------------------------------------------------------
    public void scan()
    {
        if( orb_USB.isAvailable() )
        {
            orb_report.reportScan( "USB", "ORB via USB" );
        }
        for( BluetoothDevice device : orb_BT.getPairedDevices() )
        {
            orb_report.reportScan( device.getAddress(), device.getName() );
        }
    }

    //---------------------------------------------------------------
    public void connect(String addr)
    {
        orb_USB.close();
        orb_BT.close();

        if( addr.length() > 0 )
        {
            if( addr.equals( "USB" ) )
            {
                orb_USB.open();
                orb_report.reportConnect( "USB", "ORB via USB" );
            }
            else
            {
                BluetoothDevice BT_Device = orb_BT.open( addr );
                orb_report.reportConnect( BT_Device.getAddress(), BT_Device.getName() );
            }
        }
    }

    //-----------------------------------------------------------------
    @Override
    public void run()
    {
        while ( runMainThread )
        {
            ORB_Remote orbRemote = orb_USB.isConnected() ? orb_USB : orb_BT;

            //---------------------------------------------------------------
            if( orbRemote.update() )
            {
                orb_report.report();
                updateTimeout = 0;
            }
            else
            {
                switch (downloadToORB.getStatus())
                {
                    case "OK":
                        orb_report.reportDownload("OK");
                        break;
                    case "ERR":
                        orb_report.reportDownload("error");
                        break;
                }

                if( (orb_USB.isConnected() || orb_BT.isConnected()) && !downloadToORB.isRunning )
                {
                    if (updateTimeout++ > 100)
                    {
                        orb_report.reportDisconnect();
                        orb_USB.close();
                        orb_BT.close();
                    }
                }
                else
                {
                    updateTimeout = 0;
                }
            }
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
            }
        }
    }

    //---------------------------------------------------------------
    // An der Schnittstelle wurden Daten empfangen, diese jetzt
    // verarbeiten
    void process( ByteBuffer data )
    {
        if( propFromORB.get(data) )
        {
            return;
        }
        if( downloadToORB.get(data) )
        {
            return;
        }
    }

    //---------------------------------------------------------------
    // Die Schnittstelle ist bereit, neue Daten zu versenden,
    // diese ggf. jetzt eintragen
    int fill(  ByteBuffer data )
    {
        int size = 0;

        // Hier: Download-Manager fragen, ob es etwas zu tun gibt ....
        if( downloadToORB.isNew )
        {
            size = downloadToORB.fill(data);
            downloadToORB.isNew = false;
        }
        else if( configToORB.isNew ) // TODO: erst senden, wenn letztes status-bit in propToORB == 0
        {
            size = configToORB.fill(data);
            configToORB.isNew = false; // TODO: reset erst, wenn status-bit in propToORB == 1
        }
        else // propToORB auch dann senden, wenn sich nichts geändert hat
        {
            size = propToORB.fill(data);
            propToORB.isNew = false;
        }
        return( size );
    }


    //---------------------------------------------------------------
    // Download
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    public boolean download( byte memoryID, ByteBuffer payload )
    {
        return( downloadToORB.start(memoryID, payload) );
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
    public cPropFromORB.Motor getMotor( byte id )
    {
        return( propFromORB.getMotor(id) );
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
                              short option )
    {
        configToORB.configSensor(id,type,mode,option);
    }

    //---------------------------------------------------------------
    public cPropFromORB.Sensor getSensor(int id )
    {
        return( propFromORB.getSensor(id) );
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
    public double getVcc()
    {
        return( propFromORB.getVcc() );
    }

    //---------------------------------------------------------------
    public byte getStatus()
    {
        return( propFromORB.getStatus() );
    }

    //-----------------------------------------------------------------
}
