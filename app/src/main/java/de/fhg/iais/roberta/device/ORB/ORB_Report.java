package de.fhg.iais.roberta.device.ORB;

public interface ORB_Report
{
    void reportDisconnect();

    void reportConnect( String brickId, String brickName );

    void reportScan( String brickId, String brickName ) ;

    void reportORB();

    void reportDownload( String str );
}
