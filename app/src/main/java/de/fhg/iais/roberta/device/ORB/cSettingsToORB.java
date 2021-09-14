//*******************************************************************
/*!
\file   cSettingsToORB.java
\author Thomas Breuer
\date   21.02.2020
\brief
*/

//*******************************************************************
package de.fhg.iais.roberta.device.orb;

//*******************************************************************
import java.nio.ByteBuffer;

//*******************************************************************
class cSettingsToORB
{
    //---------------------------------------------------------------
    public byte    command = 0; // 0: read settings, 1: set settings, 2: clear memory
    public String  name = "";
    public double  VCC_ok = 7.6;
    public double  VCC_low = 7.2;

    public boolean isNew = true;

    //---------------------------------------------------------------
    public cSettingsToORB()
    {
        isNew = true;
    }

    //---------------------------------------------------------------
    public int fill( ByteBuffer buffer )
    {
        int idx = 4;
        synchronized( this )
        {
            buffer.put( idx++, command );
            for( int i = 0; i < 20; i++ )
            {
                if( i < name.length() )
                    buffer.put( idx++, (byte)name.charAt( i ) );
                else
                    buffer.put( idx++, (byte)0 );
            }
            buffer.put( idx++, (byte)0 );
            buffer.put( idx++, (byte)(10.0*VCC_ok ) );
            buffer.put( idx++, (byte)(10.0*VCC_low) );

        } // synchronized

        ORB_Remote.addDataFrame( buffer, (byte)6, idx );

        return( idx );
    }

    //---------------------------------------------------------------
    public void sendRequest(  )
    {
        synchronized( this )
        {
            command = 0;
            isNew=true;
        } // synchronized
    }

    //---------------------------------------------------------------
    public void sendData( String name, double VCC_ok, double VCC_low )
    {
        synchronized( this )
        {
            command = 1;
            this.name = name;
            this.VCC_ok = VCC_ok;
            this.VCC_low = VCC_low;

            isNew=true;
        } // synchronized
    }

} // end of class
