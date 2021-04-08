package de.fhg.iais.roberta.robot;

import android.content.Intent;

import org.json.JSONObject;

//*********************************************************************************************
public interface RobotCommunicatorInterface
{
    //---------------------------------------------------------------------------------------------
    void jsToRobot(JSONObject msg);

    //---------------------------------------------------------------------------------------------
    void handleActivityResult( int requestCode, int resultCode, Intent intent );

    //---------------------------------------------------------------------------------------------
    void open();

    //---------------------------------------------------------------------------------------------
    void close();
}
