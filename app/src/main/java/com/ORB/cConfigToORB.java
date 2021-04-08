//*******************************************************************
/*!
\file   cConfigToORB.java
\author Thomas Breuer
\date   21.02.2020
\brief
*/

//*******************************************************************
package com.ORB;

//*******************************************************************
import java.nio.ByteBuffer;

//*******************************************************************
class cConfigToORB
{
    //---------------------------------------------------------------
    private class Sensor
    {
        byte type     = 0;
        byte mode     = 0;
        short option   = 0;
    }

    //---------------------------------------------------------------
    private class Motor
    {
        byte   acceleration    = 0;
        short  ticsPerRotation = 0;
        byte   Regler_Kp       = 0;
        byte   Regler_Ki       = 0;
        byte   Regler_Kd       = 0;
    }

    //---------------------------------------------------------------
    private Sensor[] sensor   = new Sensor[4];
    private Motor[]  motor    = new Motor[4];

    public boolean isNew = true;

    //---------------------------------------------------------------
    public cConfigToORB()
    {
        for( int i = 0; i < 4; i++ )
        {
            sensor[i] = new Sensor();
            motor[i]  = new Motor();

            isNew     = true;
        }
    }

    //---------------------------------------------------------------
    public int fill( ByteBuffer buffer )
    {
        int idx = 4;

        synchronized( this )
        {
            for( int i = 0; i < 4; i++ )
            {
                buffer.put( idx++, sensor[i].type   );
                buffer.put( idx++, sensor[i].mode   );
                buffer.put( idx++, (byte)(  sensor[i].option    &0xFF) );
                buffer.put( idx++, (byte)( (sensor[i].option>>8)&0xFF) );
            }

            for( int i = 0; i < 4; i++ )
            {
                buffer.put( idx++, (byte)( motor[i].ticsPerRotation    &0xFF) );
                buffer.put( idx++, (byte)((motor[i].ticsPerRotation>>8)&0xFF) );
                buffer.put( idx++, motor[i].acceleration );
                buffer.put( idx++, motor[i].Regler_Kp    );
                buffer.put( idx++, motor[i].Regler_Ki    );
                buffer.put( idx++, motor[i].Regler_Kd    );
                buffer.put( idx++, (byte)0               ); // reserved
                buffer.put( idx++, (byte)0               ); // reserved
            }

            for( int i = 0; i < 12; i++ )
            {
                buffer.put( idx++, (byte)0 ); // reserved
            }
        } // synchronized

        ORB_Remote.addDataFrame( buffer, (byte)0, idx );

        return( idx );
    }

    //---------------------------------------------------------------
    public void configMotor( int  idx,
                             int   tics,
                             int   acc,
                             int   Kp,
                             int   Ki )
    {
        synchronized( this )
        {
            if( 0 <= idx && idx < 4 )
            {
                motor[idx].ticsPerRotation = (short)tics;
                motor[idx].acceleration    = (byte)acc;
                motor[idx].Regler_Kp       = (byte)Kp;
                motor[idx].Regler_Ki       = (byte)Ki;

                isNew = true;
            }
        }
    }

    //---------------------------------------------------------------
    public void configSensor( int  idx,
                              byte type,
                              byte mode,
                              short option )
    {
        synchronized( this )
        {
            if( 0 <= idx && idx < 4 )
            {
                sensor[idx].type   = type;
                sensor[idx].mode   = mode;
                sensor[idx].option = option;

                isNew = true;
            }
        }
    }
} // end of class
