package de.fhg.iais.roberta.device.Monitor;

import org.json.JSONObject;

import de.fhg.iais.roberta.robot.orb.ORB_Communicator;

public class Monitor_Manager
{
    ORB_Communicator orb_report;

    //--------------------------------------------------------------------------------------------
    public Monitor_Manager(ORB_Communicator orb_report)
    {
        this.orb_report = orb_report;
    }

    //--------------------------------------------------------------------------------------------
    public void close()
    {
    }

    //--------------------------------------------------------------------------------------------
    public void update()
    {
        orb_report.reportMonitor();
    }

    //--------------------------------------------------------------------------------------------
    public void setText( String str )
    {
        MonitorActivity.DataHolder.setData( str );
    }

    //--------------------------------------------------------------------------------------------
    public void setLayout(JSONObject layout)
    {
        MonitorActivity.DataHolder.setLayout( layout );
    }

    //--------------------------------------------------------------------------------------------
    public byte getKey()
    {
        return( MonitorActivity.DataHolder.getKey() );
    }
}
