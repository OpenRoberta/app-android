package de.fhg.iais.roberta.robot.wedo;

import android.content.Intent;

import org.json.JSONObject;

public interface RobotCommunicatorInterface {

    void jsToRobot(JSONObject msg);

    void handleActivityResult(int requestCode, int resultCode, Intent intent);

    void reportStateChanged(String type, String state, String brickid, String... strg);

    void close();
}
