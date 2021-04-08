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
class cDownloadToORB
{
    final int blockSize = 32;

    short index;
    short option;
    int   numOfBlocks;
    int   blockId;
    ByteBuffer payload;

    short error;
    short crcReceived;
    int   length;

    public boolean isNew = true;

    public boolean isRunning = false;
    public boolean isRunningPrev = false;
    public boolean isLastTransferOK = true;

    //---------------------------------------------------------------
    public cDownloadToORB()
    {
        isNew = false;
    }

    //---------------------------------------------------------------
    public int fill( ByteBuffer buffer )
    {
        int idx = 4;
        synchronized( this )
        {
         //   buffer.put( idx++, (byte)254 ); // id
         //   buffer.put( idx++, (byte)0 ); // reserved

            buffer.put( idx++, (byte)((index      ) & 0xFF) ); // LSB
            buffer.put( idx++, (byte)((index >>  8) & 0xFF) ); //

            buffer.put( idx++, (byte)((option      ) & 0xFF) ); // LSB
            buffer.put( idx++, (byte)((option >>  8) & 0xFF) ); //

            buffer.put( idx++, (byte)((numOfBlocks      ) & 0xFF) ); // LSB
            buffer.put( idx++, (byte)((numOfBlocks >>  8) & 0xFF) ); //
            buffer.put( idx++, (byte)((numOfBlocks >> 16) & 0xFF) ); //
            buffer.put( idx++, (byte)((numOfBlocks >> 24) & 0xFF) ); // MSB

            buffer.put( idx++, (byte)((blockId      ) & 0xFF) ); // LSB
            buffer.put( idx++, (byte)((blockId >>  8) & 0xFF) ); //
            buffer.put( idx++, (byte)((blockId >> 16) & 0xFF) ); //
            buffer.put( idx++, (byte)((blockId >> 24) & 0xFF) ); // MSB

            for( int i = 0; i < 32; i++ )
            {
                int n = 32*blockId+i;
                byte x = (n+1 < payload.array().length)?payload.get(n):0;
                buffer.put( idx++, x); // LSB
            }
        } // synchronized

        ORB_Remote.addDataFrame( buffer, (byte)254, idx );

        return( idx );
    }

    //---------------------------------------------------------------
    public boolean get( ByteBuffer data )
    {
        if( !ORB_Remote.checkDataFrame( data, (byte)255, 40 ) )
        {
            return( false );
        }

        int idx = 4;

        synchronized( this )
        {
            error = (short)( ((short)data.get(idx++) & 0xFF)
                    |((short)data.get(idx++) & 0xFF) << 8);

            crcReceived = (short)( ((short)data.get(idx++) & 0xFF)
                    |((short)data.get(idx++) & 0xFF) << 8);

            length   = (int)  (  ((int)data.get(idx++) & 0xFF)
                    | ((int)data.get(idx++) & 0xFF) <<  8
                    | ((int)data.get(idx++) & 0xFF) << 16
                    | ((int)data.get(idx++) & 0xFF) << 24);


            // ignore payload!
            //            for( int i = 0; i < 32; i++ )
            //            {
            //                payload[i] = data.get(idx++);
            //            }

            if( blockId == numOfBlocks-1 ) // ready?
            {
                isRunning = false;
            }
            else
            {
                blockId++;
                isNew =true;
            }

            // check last data transfer
            if(    error != 0
                || (    !isRunning   // letzter block wurde gesendet
                     && crcReceived != ORB_Remote.CRC( payload, 0, payload.array().length )) )
            {
                // this is an error
                isLastTransferOK = false;
            }
            else
                isLastTransferOK = true;

        }
        return true;
    }

    //---------------------------------------------------------------

    //---------------------------------------------------------------
    // Methode um nächstes paket zu setzen ??
    public boolean start( byte memoryID, ByteBuffer data )
    {
        if( !isRunning )
        {
            isRunning = true;
            index       = memoryID;
            option      = 0;
            numOfBlocks = (data.array().length + 31) / 32;
            blockId     = 0;

            payload     = data;
            // zero padding
            for(int i=data.array().length;i<32*numOfBlocks;i++) {
                payload.put( i, (byte)0 );
            }
            isNew       = true;
            return(true);
        }
        return( false );
    }

    //---------------------------------------------------------------
    // Methode um nächstes paket zu setzen ??
    public String getStatus( )
    {
        if( !isRunning && isRunningPrev )
        {
            isRunningPrev = isRunning;
            if( isLastTransferOK )
                return("OK");
            else
                return("ERR");
        }
        else
        {
            isRunningPrev = isRunning;
            return("");
        }

    }

} // end of class
