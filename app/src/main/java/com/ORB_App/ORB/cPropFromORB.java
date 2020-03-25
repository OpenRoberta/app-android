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
        boolean isValid = false;
        int     value   = 0;
        int     analog1 = 0;
        int     analog2 = 0;
        boolean pin5    = true;
        boolean pin6    = true;
    }

    //---------------------------------------------------------------
    private Motor[]  motor  = new Motor[4];
    private Sensor[] sensor = new Sensor[4];

    private int     Vcc    = 0;
    private boolean D1     = true;
    private boolean D2     = true;
    private byte    Status = 0;

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
                sensor[i].value = (int)  (  ((int) data.get(idx++) & 0xFF)
                                          | ((int) data.get(idx++) & 0xFF) <<  8
                                          | ((int) data.get(idx++) & 0xFF) << 16
                                          | ((int) data.get(idx++) & 0xFF) << 24);

                byte a = data.get(idx++);
                byte b = data.get(idx++);
                byte c = data.get(idx++);

                sensor[i].analog1 = (int)( ((int)a & 0xFF)    	| ((int)b & 0x0F) << 8 );
                sensor[i].analog2 = (int)( ((int)c & 0xFF)<<4 	| ((int)b & 0xF0) >> 4 );
            }

            Byte isValid = data.array()[idx++];
            Byte digital = data.array()[idx++];

            Vcc    = ( (int)data.array()[idx++] & 0xFF );
            Status = data.array()[idx++];

            for( int i = 0; i < 4; i++ )
            {
                sensor[i].isValid = ((isValid & (1 << (  i  ) )) != 0) ? true : false;
                sensor[i].pin5    = ((digital & (1 << (2*i  ) )) != 0) ? true : false;
                sensor[i].pin6    = ((digital & (1 << (2*i+1) )) != 0) ? true : false;
            }

            D1 = (isValid & (byte)0x40) != 0;
            D2 = (isValid & (byte)0x80) != 0;
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
    public int getSensorValue( int idx )
    {
        if( 0 <= idx && idx < 4 )
        {
            return( sensor[idx].value );
        }
        return( 0 );
    }

    //---------------------------------------------------------------
    public int getSensorValueAnalog( int idx, byte ch )
    {
        if( 0 <= idx && idx < 4 )
        {
            if( ch == 0)	return( sensor[idx].analog1 );
            else        	return( sensor[idx].analog2 );
        }
        return( 0 );
    }

    //---------------------------------------------------------------
    public boolean getSensorValueDigital( int idx, byte ch )
    {
        if( 0 <= idx && idx < 4 )
        {
            if( ch == 0)	return( sensor[idx].pin5 );
            else        	return( sensor[idx].pin6 );
        }
        return( false );
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
