//*******************************************************************
/*!
\file   ORB_Remote.java
\author Thomas Breuer
\date   21.02.2020
\brief
*/

//*******************************************************************
package com.ORB;

//*******************************************************************
import android.util.Log;
import java.nio.ByteBuffer;

//*******************************************************************
abstract class ORB_Remote
{
    //---------------------------------------------------------------
    ORB_Manager orb_manager;

    //---------------------------------------------------------------
    ORB_Remote(  ORB_Manager orb_manager )
    {
        this.orb_manager = orb_manager;
    }

    //---------------------------------------------------------------
    public void init(  )
    {
    }

    //---------------------------------------------------------------
    public abstract boolean update();

    //-----------------------------------------------------------------
    //-----------------------------------------------------------------
    static boolean checkDataFrame( ByteBuffer data, byte id, int size )
    {
        // check ID
        if( (byte)( ( data.get(2) & 0xFF) ) != id )
            return false;

        // read CRC
        short crc =  (short)( ( data.get(0) & 0xFF )
                             |( data.get(1) & 0xFF ) << 8);

        // check CRC
        if( ORB_Remote.CRC( data, 2, size+2 ) != crc )
        //                        |   |----- size of payload + offset
        //                        +--------- offset
        {
            Log.e("ORB_Remote", "CRC error");
            return false;
        }
        return( true );
    }
    //-----------------------------------------------------------------
    static public void addDataFrame( ByteBuffer data, byte id, int size)
    {
        data.put( 2, (byte)((id      )       ) );
        data.put( 3, (byte)((0       )       ) );

        short crc = CRC( data, 2, size-2 );

        data.put( 0, (byte)((crc     ) & 0xFF) );
        data.put( 1, (byte)((crc >> 8) & 0xFF) );
    }

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
