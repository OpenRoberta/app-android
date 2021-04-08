package com.ORB;

public interface ORB_Report
{
    void reportDisconnect();

    void reportConnect( String brickId, String brickName );

    void reportScan( String brickId, String brickName );

    void reportDownload( String str );

    void report();
}
