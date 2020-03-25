//*******************************************************************
/*!
\file   ORB_Remote.java
\author Thomas Breuer
\date   21.02.2020
\brief
*/

//*******************************************************************
package com.ORB_App.ORB;

//*******************************************************************
import java.nio.ByteBuffer;

//*******************************************************************
public abstract class ORB_Remote
{
    //---------------------------------------------------------------
    ORB_RemoteHandler handler;

    //---------------------------------------------------------------
    ORB_Remote( ORB_RemoteHandler handler )
    {
        this.handler = handler;
    }

    //---------------------------------------------------------------
    public void init(  )
    {
    }

    //---------------------------------------------------------------
    public abstract boolean update();

    //-----------------------------------------------------------------
    //-----------------------------------------------------------------
    static public short CRC( ByteBuffer data, int start, int anz )
    {
        int crc  = 0xFFFF;
        int temp = 0;

        for( int i = start; i < start+anz; i++ )
        {
            int idx = ((short)data.array()[i]&0xFF) ^ crc;

            temp = 0;
            for( byte bit = 0; bit < 8; bit++ )
            {
                int x = (temp^idx) & 0x01;
                if( x != 0 )
                {
                    temp = (temp>>1) ^ 0xA001; //generatorPolynom;
                }
                else
                {
                    temp = (temp>>1);
                }
                idx = (idx>>1);
            }
            crc = (crc>>8) ^ temp;
        }
        return( (short)(crc&0xFFFF)  );
    }
} // end of class
