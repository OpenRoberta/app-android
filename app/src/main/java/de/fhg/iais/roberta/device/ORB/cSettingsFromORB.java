//*******************************************************************
/*!
\file   cSettingFromORB.java
\author Thomas Breuer
\date   21.02.2020
\brief
*/

//*******************************************************************
package de.fhg.iais.roberta.device.orb;

//*******************************************************************
import java.nio.ByteBuffer;

//*******************************************************************
public class cSettingsFromORB
{
    //---------------------------------------------------------------
    private int[] version  = new int[2];
    private int[] board    = new int[2];
    private String name    = "---";
    private double Vcc_ok  = 0;
    private double Vcc_low = 0;

    public boolean isNew = false;

    //---------------------------------------------------------------
    cSettingsFromORB()
    {
    }

    //---------------------------------------------------------------
    boolean get( ByteBuffer data )
    {
        if( !ORB_Remote.checkDataFrame( data, (byte)5, 31 ) )
        {
            return( false );
        }

        int idx = 4;

        synchronized( this )
        {
            version[0] =   (short)( ((short)data.get(idx++) & 0xFF)
                                   |((short)data.get(idx++) & 0xFF) << 8);
            version[1] =   (short)( ((short)data.get(idx++) & 0xFF)
                                   |((short)data.get(idx++) & 0xFF) << 8);
            board[0] =     (short)( ((short)data.get(idx++) & 0xFF)
                                   |((short)data.get(idx++) & 0xFF) << 8);
            board[1] =     (short)( ((short)data.get(idx++) & 0xFF)
                                   |((short)data.get(idx++) & 0xFF) << 8);

            name = "";
            for( int i = 0; i < 20; i++ )
            {
                char c =  (char)data.get(idx++);
                if( c != 0 )
                    name = name.concat( String.valueOf( c ) );
            }
            idx++; // skip terminating zero

            Vcc_ok  = (double)data.get(idx++)/10;
            Vcc_low = (double)data.get(idx++)/10;

            isNew = true;
        }
        return true;
    }

    //---------------------------------------------------------------
    public int[] getVersion()
    {
        return( version );
    }

    //---------------------------------------------------------------
    public int[] getBoard()
    {
        return( board );
    }

    //---------------------------------------------------------------
    public double getVcc_ok()
    {
        return( Vcc_ok );
    }

    //---------------------------------------------------------------
    public double getVcc_low()
    {
        return( Vcc_low );
    }

    //---------------------------------------------------------------
    public String getNamel( )
    {
        return( name );
    }

} // end of class
