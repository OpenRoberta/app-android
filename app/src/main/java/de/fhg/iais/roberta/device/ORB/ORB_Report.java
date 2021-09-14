package de.fhg.iais.roberta.device.orb;

public interface ORB_Report
{
    void reportDisconnect();

    void reportConnect( String brickId, String brickName );

    void reportScan( String brickId, String brickName ) ;

    void reportORB();

    void reportDownload( String str );
}
