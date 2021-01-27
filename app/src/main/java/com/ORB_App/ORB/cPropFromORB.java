//*******************************************************************
/*!
\file   cPropFromORB.java
\author Thomas Breuer
\date   21.02.2020
\brief
*/

//*******************************************************************
package com.ORB_App.ORB;

//*******************************************************************
import android.util.Log;

import java.nio.ByteBuffer;

//*******************************************************************
public class cPropFromORB
{
    //---------------------------------------------------------------
    private class Motor
    {
        short pwr   = 0;
        short speed = 0;
        int   pos   = 0;
    }

    //---------------------------------------------------------------
    private class Sensor
    {
        boolean isValid    = false;
        int     value0     = 0;
        int     value1     = 0;
        byte    type       = 0;
        byte    descriptor = 0;
        byte    option     = 0;
    }

    //---------------------------------------------------------------
    private Motor[]  motor  = new Motor[4];
    private Sensor[] sensor = new Sensor[4];

    private int     Vcc      = 0;
    private byte    Status   = 0;
    private boolean D1       = true;
    private boolean D2       = true;
    private byte    reserved = 0;


    //---------------------------------------------------------------
    public cPropFromORB()
    {
        for( int i = 0; i < 4; i++ )
        {
            motor[i] = new Motor();
        }

        for( int i = 0; i < 4; i++ )
        {
            sensor[i] = new Sensor();
        }
    }

    //---------------------------------------------------------------
    public boolean get( ByteBuffer data )
    {
        int idx = 0;

        short crc =  (short)( ( data.get(idx++) & 0xFF )
                |( data.get(idx++) & 0xFF ) << 8);

        byte id =  (byte)( ( data.get(idx++) & 0xFF) );

        // check ID
        if( id != 2 )
            return false;

        // check CRC
        if( ORB_Remote.CRC( data, 2, 62 ) != crc )
        //                        |   |----- size of payload + offset
        //                        +--------- offset
        {
            Log.e("cPropFromORB", "CRC error");
            return false;
        }

        idx++; // skip reserved

        synchronized( this )
        {
            for( int i = 0; i < 4; i++ )
            {
                motor[i].pwr   = (short)( ((byte)data.get(idx++) & (byte)0xFF) );

                motor[i].speed = (short)( ((short)data.get(idx++) & 0xFF)
                        |((short)data.get(idx++) & 0xFF) << 8);

                motor[i].pos   = (int)  (  ((int)data.get(idx++) & 0xFF)
                        | ((int)data.get(idx++) & 0xFF) <<  8
                        | ((int)data.get(idx++) & 0xFF) << 16
                        | ((int)data.get(idx++) & 0xFF) << 24);
            }

            for( int i = 0; i < 4; i++ )
            {
                sensor[i].value0 = (int)  (  ((int) data.get(idx++) & 0xFF)
                        | ((int) data.get(idx++) & 0xFF) <<  8
                        | ((int) data.get(idx++) & 0xFF) << 16
                        | ((int) data.get(idx++) & 0xFF) << 24);


                byte type            = data.get(idx++);
                sensor[i].descriptor = data.get(idx++);
                sensor[i].option     = data.get(idx++);

                sensor[i].isValid = ((type & 0x80)==0x80)? true : false;
                sensor[i].type    = (byte)(( type & 0x07));
            }

            Byte digital = data.array()[idx++];
            D1 = (digital & (byte)0x40) != 0;
            D2 = (digital & (byte)0x80) != 0;

            Vcc      = data.get(idx++) & 0xFF;
            Status   = data.get(idx++);
            reserved = data.get(idx++);
        }
        return true;
    }

    //---------------------------------------------------------------
    public short getMotorPwr( int idx )
    {
        if( 0 <= idx && idx < 4 )
        {
            return( motor[idx].pwr );
        }
        return( 0 );
    }

    //---------------------------------------------------------------
    public short getMotorSpeed( int idx )
    {
        if( 0 <= idx && idx < 4 )
        {
            return( motor[idx].speed );
        }
        return( 0 );
    }

    //---------------------------------------------------------------
    public int getMotorPos( int idx )
    {
        if( 0 <= idx && idx < 4 )
        {
            return( motor[idx].pos );
        }
        return( 0 );
    }

    //---------------------------------------------------------------
    public boolean getSensorValid( int idx )
    {
        if( 0 <= idx && idx < 4 )
        {
            return( sensor[idx].isValid );
        }
        return( false );
    }

    //---------------------------------------------------------------
    public byte getSensorType( int idx )
    {
        if( 0 <= idx && idx < 4 )
        {
            return( sensor[idx].type );
        }
        return( 0 );
    }

    //---------------------------------------------------------------
    public byte getSensorOption( int idx )
    {
        if( 0 <= idx && idx < 4 )
        {
            return( sensor[idx].option );
        }
        return( 0 );
    }

    //---------------------------------------------------------------
    public int getSensorValue( int idx )
    {
        if( 0 <= idx && idx < 4 )
        {
            return( sensor[idx].value0 );
        }
        return( 0 );
    }

    //---------------------------------------------------------------
    public boolean getSensorDigital( int idx )
    {
        if( idx == 0 )  return( D1 );
        else            return( D2 );
    }

    //---------------------------------------------------------------
    public int getVcc()
    {
        return( Vcc );
    }

    //---------------------------------------------------------------
    public byte getStatus()
    {
        return( Status );
    }
} // end of class
