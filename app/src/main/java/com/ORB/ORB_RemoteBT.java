//*******************************************************************
/*!
\file   ORB_RemoteBT.java
\author Thomas Breuer
\date   21.02.2020
\brief
*/

//*******************************************************************
package com.ORB;

//*******************************************************************
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.UUID;

//*******************************************************************
class ORB_RemoteBT extends ORB_Remote
{
    //---------------------------------------------------------------
    private BluetoothSocket BT_Socket;
    private OutputStream BT_OutStream;
    private InputStream BT_InStream;
    private boolean isConnected = false;

    //---------------------------------------------------------------
    private ByteBuffer bufferIN;
    private ByteBuffer bufferOUT;

    //---------------------------------------------------------------
    private boolean ready = false;
    private int pos = 0;
    private byte temp = 0;

    private static final String TAG = "ORB_BT";

    private static final UUID MY_UUID = UUID.fromString( "00001101-0000-1000-8000-00805F9B34FB" );

    //---------------------------------------------------------------
    public ORB_RemoteBT( ORB_Manager orb_manager )
    {
        super( orb_manager );

        bufferIN = ByteBuffer.allocate( 256 );
        bufferOUT = ByteBuffer.allocate( 256 );

        isConnected = false;
    }

    //---------------------------------------------------------------
    public BluetoothDevice open( String addr )
    {
        close();

        BluetoothDevice BT_Device
                = BluetoothAdapter.getDefaultAdapter().getRemoteDevice( addr );
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        try
        {
            synchronized( this )
            {
                BT_Socket = BT_Device.createRfcommSocketToServiceRecord( MY_UUID );
                BT_Socket.connect();

                BT_OutStream = BT_Socket.getOutputStream();
                BT_InStream = BT_Socket.getInputStream();
                isConnected = true;
            }
        } catch( IOException e )
        {
            isConnected = false;
        }
        return (BT_Device);
    }

    //---------------------------------------------------------------
    public void close()
    {
        isConnected = false;
        try
        {
            if(  BT_Socket != null )
            {
                if( BT_InStream != null )
                {
                    BT_InStream.close();
                }
                if( BT_OutStream != null )
                {
                    BT_OutStream.close();
                }

                BT_Socket.close();
                BT_Socket = null;

            }
        } catch(IOException e)
        {
            Log.e( TAG, "BT close error" + e.toString() );
        }
    }
    //---------------------------------------------------------------
    public Set<BluetoothDevice> getPairedDevices()
    {
        BluetoothAdapter BT_Adapter;
        Set<BluetoothDevice> BT_PairedDevices;

        BT_Adapter = BluetoothAdapter.getDefaultAdapter();
        BT_PairedDevices = BT_Adapter.getBondedDevices();
        return (BT_PairedDevices);
    }


    //---------------------------------------------------------------
    public boolean isConnected()
    {
        return (isConnected);
    }

    //---------------------------------------------------------------
    private void updateOut()
    {
        if( !isConnected )
        {
            return;
        }

        short crc;
        int size;

        size = orb_manager.fill( bufferOUT );

        byte data[] = new byte[1024];
        short len = 0;
        short idx = 0;

        data[len++] = (byte) (0x20 | ((bufferOUT.get( idx ) >> 4) & 0x0F));
        data[len++] = (byte) (0x30 | ((bufferOUT.get( idx++ )) & 0x0F));

        while( idx < size - 1 )
        {
            data[len++] = (byte) (0x40 | ((bufferOUT.get( idx ) >> 4) & 0x0F));
            data[len++] = (byte) (0x50 | ((bufferOUT.get( idx++ )) & 0x0F));
        }
        data[len++] = (byte) (0x80 | ((bufferOUT.get( idx ) >> 4) & 0x0F));
        data[len++] = (byte) (0x90 | ((bufferOUT.get( idx++ )) & 0x0F));

        try
        {
            BT_OutStream.write( data, 0, len );
            BT_OutStream.flush();
        } catch( IOException e )
        {
        }
    }

    //---------------------------------------------------------------
    private boolean updateIn()
    {
        if( !isConnected )
        {
            return (false);
        }

        int r;
        try
        {
            while( !ready && BT_InStream.available() > 0 && (r = BT_InStream.read()) >= 0 )
            {
                if( (r & 0xF0) == 0x20 || pos > 255 )
                {
                    pos = 0;
                    ready = false;
                }

                if( (r & 0xF0) == 0x90 )
                {
                    ready = true;
                }

                if( (r & 0x10) == 0x00 )
                {
                    temp = (byte) ((r << 4) & 0xf0);
                }
                else
                {
                    temp |= (r & 0x0f);
                    bufferIN.put( pos, temp );
                    pos++;
                }
            }
        } catch( IOException e )
        {
            Log.e( TAG, "error read " );
        }

        if( ready )
        {
            orb_manager.process( bufferIN );
            ready = false;
            return (true);
        }
        return (false);
    }

    //---------------------------------------------------------------
    public boolean update()
    {
        boolean ret = false;
        synchronized( this )
        {
            if( updateIn() )
            {
                ret = true;
            }

            updateOut();
        }
        return (ret);
    }

} // end of class
