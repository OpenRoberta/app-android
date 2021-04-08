//*******************************************************************
/*!
\file   cPropToORB.java
\author Thomas Breuer
\date   21.02.2020
\brief
*/

//*******************************************************************
package com.ORB;

//*******************************************************************
import java.nio.ByteBuffer;

//*******************************************************************
class cPropToORB
{
    private class Motor
    {
        int  mode  = 0;
        int  speed = 0;
        int  pos   = 0;
    }

    private class ModellServo
    {
        int  mode = 0;
        int  pos  = 0;
    }

    private Motor[]       motor = new Motor[4];
    private ModellServo[] servo = new ModellServo[2];

    public boolean isNew = true;

    //---------------------------------------------------------------
    public cPropToORB()
    {
        for( int i = 0; i < 4; i++ )
        {
            motor[i] = new Motor();
        }

        for( int i = 0; i < 2; i++ )
        {
            servo[i] = new ModellServo();
        }

        isNew = true;
    }

    //---------------------------------------------------------------
    public int fill( ByteBuffer buffer )
    {
        int idx = 4;
        synchronized( this )
        {
            for( int i = 0; i < 4; i++ )
            {
                buffer.put( idx++, (byte)((motor[i].mode      )       ) );

                buffer.put( idx++, (byte)((motor[i].speed     ) & 0xFF) ); // LSB
                buffer.put( idx++, (byte)((motor[i].speed >> 8) & 0xFF) ); // MSB

                buffer.put( idx++, (byte)((motor[i].pos      ) & 0xFF) ); // LSB
                buffer.put( idx++, (byte)((motor[i].pos >>  8) & 0xFF) ); //
                buffer.put( idx++, (byte)((motor[i].pos >> 16) & 0xFF) ); //
                buffer.put( idx++, (byte)((motor[i].pos >> 24) & 0xFF) ); // MSB
            }

            for( int i = 0; i < 2; i++ )
            {
                buffer.put( idx++, (byte)((servo[i].mode)) );
                buffer.put( idx++, (byte)((servo[i].pos )) );
            }
        } // synchronized

        ORB_Remote.addDataFrame( buffer, (byte)1, idx );

        return( idx );
    }

    //---------------------------------------------------------------
    public void setMotor( int idx,
                          int mode,
                          int speed,
                          int pos )
    {
        synchronized( this )
        {
            if( 0 <= idx && idx < 4 )
            {
                motor[idx].mode  = mode;
                motor[idx].speed = speed;
                motor[idx].pos   = pos;

                isNew=true;
            }
        } // synchronized
    }

    //---------------------------------------------------------------
    public void setModelServo( int idx,
                               int mode,
                               int pos )
    {
        synchronized( this )
        {
            if( 0 <= idx && idx < 2 )
            {
                servo[idx].mode = mode;
                servo[idx].pos  = pos;

                isNew = true;
            }
        } // synchronized
    }
} // end of class
