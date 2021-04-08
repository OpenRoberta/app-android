//*******************************************************************
/*!
\file   cPropFromORB.java
\author Thomas Breuer
\date   21.02.2020
\brief
*/

//*******************************************************************
package com.ORB;

//*******************************************************************
import java.nio.ByteBuffer;

//*******************************************************************
public class cPropFromORB
{
    //---------------------------------------------------------------
    public class Motor
    {
        public short pwr   = 0;
        public short speed = 0;
        public int   pos   = 0;
    }

    //---------------------------------------------------------------
    public class Sensor
    {
        public boolean isValid    = false;
        public int[]   value      = {0,0};
        public byte    type       = 0;
        public byte    option     = 0;
    }

    private class Sensor_temp
    {
        int[]     valueTemp = {0,0};
    }

    //---------------------------------------------------------------
    private Motor[]  motor  = new Motor[4];
    private Sensor[] sensor = new Sensor[4];
    private Sensor_temp[] sensor_temp = new Sensor_temp[4];

    private int     Vcc      = 0;
    private byte    Status   = 0;
    private boolean D1       = true;
    private boolean D2       = true;

    //---------------------------------------------------------------
    cPropFromORB()
    {
        for( int i = 0; i < 4; i++ )
        {
            motor[i] = new Motor();
        }

        for( int i = 0; i < 4; i++ )
        {
            sensor[i] = new Sensor();
            sensor_temp[i] = new Sensor_temp();
        }
    }

    //---------------------------------------------------------------
    boolean get( ByteBuffer data )
    {
        if( !ORB_Remote.checkDataFrame( data, (byte)2, 60 ) )
        {
            return( false );
        }

        int idx = 4;

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
                int value    = (int)  (  ((int) data.get(idx++) & 0xFF)
                                       | ((int) data.get(idx++) & 0xFF) <<  8
                                       | ((int) data.get(idx++) & 0xFF) << 16
                                       | ((int) data.get(idx++) & 0xFF) << 24);

				byte type            = data.get(idx++);
                byte descriptor      = data.get(idx++);
                byte option          = data.get(idx++);


                int lenExp = (descriptor >> 5) & 0xFF;
                int id =  (descriptor) & 0x1F;

                if( lenExp <= 2) // 1,2 oder 4 Byte
                {
                    sensor[i].value[0] = value;
                    sensor[i].value[1] = 0;
                    sensor[i].isValid = ((type & 0x80)==0x80)? true : false;
                    sensor[i].type    = (byte)(( type & 0x7F));
                    sensor[i].option  = option;
                }
                else if( lenExp == 3 && id == 0 ) // 1. von 2 Packeten
                {
                    sensor_temp[i].valueTemp[0] = value;
                }
                else if( lenExp == 3 && id == 1 ) // 2. von 2 Packeten
                {
                    sensor[i].value[0] = sensor_temp[i].valueTemp[0];
                    sensor[i].value[1] = value;
                    sensor[i].isValid = ((type & 0x80)==0x80)? true : false;
                    sensor[i].type    = (byte)(( type & 0x7F));
                    sensor[i].option  = option;
                }
                else {
                    sensor[i].isValid = false;
                    // ignore!
                }
            }

            Byte digital = data.array()[idx++];
            D1 = (digital & (byte)0x01) != 0;
            D2 = (digital & (byte)0x02) != 0;

            Vcc      = data.get(idx++) & 0xFF;
            Status   = data.get(idx++);
        }
        return true;
    }

    //---------------------------------------------------------------
    Motor getMotor( int idx )
    {
        if( 0 <= idx && idx < 4 )
        {
            return( motor[idx] );
        }
        return( motor[0] );
    }


    //---------------------------------------------------------------
    Sensor getSensor( int idx )
    {
        if( 0 <= idx && idx < 4 )
        {
            return( sensor[idx] );
        }
        return(  sensor[0]  );
    }

    //---------------------------------------------------------------
    public boolean getSensorDigital( int idx )
    {
        if( idx == 0 )  return( D1 );
        else            return( D2 );
    }

    //---------------------------------------------------------------
    double getVcc()
    {
        return ((double)Vcc/10);
    }

    //---------------------------------------------------------------
    byte getStatus()
    {
        return( Status );
    }

} // end of class
