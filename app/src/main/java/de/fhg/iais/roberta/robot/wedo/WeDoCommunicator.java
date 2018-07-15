package de.fhg.iais.roberta.robot.wedo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.fhg.iais.roberta.main.ORLabActivity;
import de.fhg.iais.roberta.main.R;
import de.fhg.iais.roberta.main.Util;
import de.fhg.iais.roberta.robot.RobotCommunicator;
import dk.lego.devicesdk.LDSDKError;
import dk.lego.devicesdk.bluetooth.LegoBluetoothDevice;
import dk.lego.devicesdk.bluetooth.LegoBluetoothDeviceManagerCallbackListener;
import dk.lego.devicesdk.bluetooth.LegoBluetoothDeviceManagerImpl;
import dk.lego.devicesdk.bluetooth.wrappers.AndroidBluetoothAdapterWrapperImpl;
import dk.lego.devicesdk.device.DeviceCallbackListener;
import dk.lego.devicesdk.device.DeviceInfo;
import dk.lego.devicesdk.device.LegoDevice;
import dk.lego.devicesdk.input_output.InputFormat;
import dk.lego.devicesdk.services.LegoService;
import dk.lego.devicesdk.services.MotionSensor;
import dk.lego.devicesdk.services.MotionSensorCallbackListener;
import dk.lego.devicesdk.services.Motor;
import dk.lego.devicesdk.services.PiezoTonePlayer;
import dk.lego.devicesdk.services.RGBLight;
import dk.lego.devicesdk.services.ServiceCallbackListener;
import dk.lego.devicesdk.services.TiltSensor;
import dk.lego.devicesdk.services.TiltSensorCallbackListener;

import static java.lang.Integer.parseInt;

public class WeDoCommunicator extends RobotCommunicator implements LegoBluetoothDeviceManagerCallbackListener, DeviceCallbackListener, ServiceCallbackListener {
    private static final String TAG = WeDoCommunicator.class.getSimpleName();
    private static final int REQUEST_BT_ENABLE = 10;
    private static final int REQUEST_GPS_ENABLE = 11;
    private static final int REQUEST_LOC_PERMISSION = 12;
    private ORLabActivity orLabActivity;
    private WebView mainView;
    private LegoBluetoothDeviceManagerImpl legoDeviceManager;
    private Map<String, LegoBluetoothDevice> scannedDevices = new HashMap<>();
    private Map<String, LegoBluetoothDevice> connectedDevices = new HashMap<>();
    private Map<String, LegoService> actuators = new HashMap<>();

    public WeDoCommunicator(ORLabActivity orLabActivity, WebView mainView) {

        this.orLabActivity = orLabActivity;
        this.mainView = mainView;
        this.legoDeviceManager = new LegoBluetoothDeviceManagerImpl(new AndroidBluetoothAdapterWrapperImpl(orLabActivity));
        this.legoDeviceManager.updateAdvertisingDevicesInterval(10);
        this.legoDeviceManager.registerCallbackListener(this);
        this.ROBOT = "wedo";
        Log.d(TAG, "initialized");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startScan() {
        // check if Bluetooth adapter is available
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            reportScanError("no Bluetooth adapter available");
            Util.showAlert(this.orLabActivity, R.string.blue_adapter_missing);
            return;
        }
        // check if Bluetooth LE is available
        if (!this.orLabActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            reportScanError("no Bluetooth LE available");
            Util.showAlert(this.orLabActivity, R.string.blue_adapter_missing);
            return;
        }
        // check if Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled()) {
            Util.enableBluetooth(orLabActivity, REQUEST_BT_ENABLE);
            return;
        }
        // check bluetooth version
        int bluetoothVersion = parseInt(Build.VERSION.SDK);
        switch (bluetoothVersion) {
            case Build.VERSION_CODES.BASE:
            case Build.VERSION_CODES.BASE_1_1:
            case Build.VERSION_CODES.CUPCAKE:
            case Build.VERSION_CODES.DONUT:
                reportScanError("wrong Bluetooth version");
                Util.showAlert(this.orLabActivity, R.string.msg_blue_old_version);
                return;
            default:
                break;
        }
        if (orLabActivity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Util.checkForLocationPermission(orLabActivity, REQUEST_LOC_PERMISSION);
        }
        // for Bluetooth LE, we need at least "Battery Saving" permission at location mode
        final LocationManager manager = (LocationManager) this.orLabActivity.getSystemService(Context.LOCATION_SERVICE);
        if (manager != null) {
            if (!manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Util.enableLocationService(this.orLabActivity, REQUEST_GPS_ENABLE);
                return;
            }
        }else{
            Log.e(TAG, "no location manager");
            Util.showAlert(this.orLabActivity,R.string.location_problem);
            reportScanError("no location service");
            return;
        }

        try {
            this.legoDeviceManager.scan(this.orLabActivity);
            Log.d(TAG, "start scanning");
        } catch (IllegalStateException e) {
            reportScanError( e.getMessage());
            Util.showAlert(this.orLabActivity, e.getMessage());
            this.legoDeviceManager.unregisterCallbackListener(this);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void stopScan() {
        this.legoDeviceManager.stopScanning();
        Log.d(TAG, "stop scanning");
    }

    private void connect(String deviceID) {
        LegoBluetoothDevice legoDevice = this.scannedDevices.get(deviceID);
        if (legoDevice == null) {
            Log.e(TAG, "connect failed: " + deviceID);
        } else {
            connectedDevices.put(deviceID, legoDevice);
            legoDevice.registerCallbackListener(this);
            legoDeviceManager.connectToDevice(orLabActivity, legoDevice);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void disconnect(String deviceID) {
        LegoBluetoothDevice legoDevice = this.connectedDevices.get(deviceID);
        if (legoDevice == null) {
            Log.e(TAG, "disconnect failed: " + deviceID);
        } else {
            // TODO seems to not working function
            this.legoDeviceManager.setAutomaticReconnectOnConnectionLostEnabled(false);
            // BluetoothGatt: Unhandled exception in callback
            this.legoDeviceManager.cancelDeviceConnection(legoDevice);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void jsToRobot(JSONObject msg) {
        Log.d(TAG, "jsToRobot: " + msg.toString());
        try {
            String target = msg.getString("target");
            String type = msg.getString("type");
            if (target.equals(ROBOT)) {
                switch (type) {
                    case "startScan":
                        this.startScan();
                        break;
                    case "stopScan":
                        this.stopScan();
                        break;
                    case "connect":
                        this.connect(msg.getString("robot"));
                        break;
                    case "disconnect":
                        //TODO check if we need more information from webview
                    case "command":
                        switch (msg.getString("actuator")) {
                            case "motor":
                                Motor m = (Motor) actuators.get(msg.getString("brickid") + msg.getInt("id") + "Motor");
                                if (msg.getString("action").equals("on")) {
                                    m.run(Motor.MotorDirection.fromInteger(msg.getInt("direction")), msg.getInt("power"));
                                } else if (msg.getString("action").equals("stop")) {
                                    m.brake();
                                }
                                break;
                            case "piezo":
                                PiezoTonePlayer p = (PiezoTonePlayer) actuators.get(msg.getString("brickid") + "Piezo");
                                p.playFrequency(msg.getInt("frequency"), msg.getInt("duration"));
                                break;
                            case "light":
                                RGBLight rgb = (RGBLight) actuators.get(msg.getString("brickid") + "RGB");
                                rgb.setColorIndex(msg.getInt("color"));
                                break;
                            default:
                                throw new NullPointerException();
                        }
                        break;
                    default:
                        Log.e(TAG, "Not supported msg: " + msg);
                        break;
                }
            }
        } catch (final JSONException e) {
            // ignore invalid messages
            Log.e(TAG, "Json parsing error: " + e.getMessage() + " processing: " + msg);
        } catch (final NullPointerException e) {
            Log.e(TAG, "Command parsing error: " + e.getMessage() + " processing: " + msg);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void close() {
        legoDeviceManager.stopScanning();
        for (String name : connectedDevices.keySet()) {
            this.disconnect(name);
        }
        this.scannedDevices.clear();
    }

    @Override
    public void didUpdateDeviceInfo(LegoDevice legoDevice, DeviceInfo deviceInfo, LDSDKError
            ldsdkError) {
        //TODO check if and what we want to report from deviceInfo
        reportStateChanged("connect", "connected", legoDevice.getDeviceId(), "brickname", legoDevice.getName());
    }

    @Override
    public void didChangeNameFrom(LegoDevice legoDevice, String s, String s1) {
        // not supported in Open Roberta
    }

    @Override
    public void didChangeButtonState(LegoDevice legoDevice, boolean b) {
        // TODO check if we should support this button in the Open Roberta Lab (works reliable)
        reportStateChanged("update", String.valueOf(b), legoDevice.getDeviceId(), "sensor", "button");
    }

    @Override
    public void didUpdateBatteryLevel(LegoDevice legoDevice, int i) {
        reportStateChanged("update", Integer.toString(i), legoDevice.getDeviceId(), "sensor", "batterylevel");
    }

    @Override
    public void didUpdateLowVoltageState(LegoDevice legoDevice, boolean b) {
        // not supported in Open Roberta
    }

    @Override
    public void didAddService(LegoDevice legoDevice, LegoService legoService) {
        String type;
        switch (legoService.getServiceName()) {
            case "Motion Sensor":
                MotionSensor mS = (MotionSensor) legoService;
                mS.setMotionSensorMode(MotionSensor.MotionSensorMode.MOTION_SENSOR_MODE_DETECT);
                mS.registerCallbackListener(new MotionListener(this));
                mS.setDevice(legoDevice);
                type = "sensor";
                //actuators.put(legoDevice.getDeviceId()+legoService.getDefaultInputFormat().getConnectId(),m);
                break;
            case "Tilt Sensor":
                TiltSensor t = (TiltSensor) legoService;
                t.registerCallbackListener(new TiltListener(this));
                t.setTiltSensorMode(TiltSensor.TiltSensorMode.TILT_SENSOR_MODE_TILT);
                t.setDevice(legoDevice);
                type = "sensor";
                //actuators.put(legoDevice.getDeviceId()+legoService.getDefaultInputFormat().getConnectId(),t);
                break;
            case "Piezo":
                PiezoTonePlayer p = (PiezoTonePlayer) legoService;
                this.actuators.put(legoDevice.getDeviceId() + "Piezo", p);
                type = "actuator";
                break;
            case "RGB Light":
                RGBLight rgb = (RGBLight) legoService;
                rgb.setDevice(legoDevice);
                this.actuators.put(legoDevice.getDeviceId() + "RGB", rgb);
                type = "actuator";
                break;
            case "Motor":
                Motor m = (Motor) legoService;
                m.setDevice(legoDevice);
                this.actuators.put(legoDevice.getDeviceId() + legoService.getConnectInfo().getConnectId() + "Motor", m);
                type = "actuator";
                break;
            default:
                reportStateChanged("didAddService", "not supported", legoDevice.getDeviceId(), "sensor|actor", legoService.getServiceName(), "id", Integer.toString(legoService.getConnectInfo().getConnectId()));
                return;
        }
        reportStateChanged("didAddService", "connected", legoDevice.getDeviceId(), type, legoService.getServiceName(), "id", Integer.toString(legoService.getConnectInfo().getConnectId()));
    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void handleActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case REQUEST_BT_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    startScan();
                } else {
                    reportScanError("Bluetooth adapter not enabled");
                }
                break;
            case REQUEST_GPS_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    startScan();
                } else {
                    reportScanError("no location service allowed");
                }
                break;
            default:
                // ignore this request
                break;

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOC_PERMISSION: {
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                            grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        startScan();
                        return;
                    }
                }
                reportScanError("no location service allowed");
            }
            default:
        }
    }

    @Override
    public void didRemoveService(LegoDevice legoDevice, LegoService legoService) {
        String type = "sensor";
        if (legoService.getServiceName().equals("Motor")) {
            this.actuators.remove(legoDevice.getDeviceId() + legoService.getConnectInfo().getConnectId() + "Motor");
            type = "actuator";
        }
        reportStateChanged("didRemoveService", "disconnected", legoDevice.getDeviceId(), type, legoService.getServiceName(), "id", Integer.toString(legoService.getConnectInfo().getConnectId()));
    }

    @Override
    public void didFailToAddServiceWithError(LegoDevice legoDevice, LDSDKError ldsdkError) {
        Log.e(TAG, "didFailToAddServiceWithError:" + ldsdkError.getMessage());
        reportStateChanged("didAddService", "failed", legoDevice.getDeviceId(), "error", ldsdkError.getMessage());
    }

    @Override
    public void onDeviceDidAppear(LegoBluetoothDevice legoBluetoothDevice) {
        if (this.scannedDevices.containsKey(legoBluetoothDevice.getDeviceId())) {
            return;
        }
        this.scannedDevices.put(legoBluetoothDevice.getDeviceId(), legoBluetoothDevice);
        reportStateChanged("scan", "appeared", legoBluetoothDevice.getDeviceId(), "brickname", legoBluetoothDevice.getName());
    }

    @Override
    public void onDeviceDidDisappear(LegoBluetoothDevice legoBluetoothDevice) {
        //TODO check if we want to remove the device from scannedDevices
        Log.d(TAG, "disappeared:" + legoBluetoothDevice.getDeviceId());
    }

    @Override
    public void onWillStartConnectingToDevice(LegoBluetoothDevice legoBluetoothDevice) {

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDidFailToConnectToDevice(LegoBluetoothDevice legoBluetoothDevice,
                                           boolean b, LDSDKError ldsdkError) {
        connectedDevices.remove(legoBluetoothDevice.getDeviceId(), legoBluetoothDevice);
        reportStateChanged("connect", "failed", legoBluetoothDevice.getDeviceId());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDidDisconnectFromDevice(LegoBluetoothDevice legoBluetoothDevice,
                                          boolean b, LDSDKError ldsdkError) {
        this.connectedDevices.remove(legoBluetoothDevice.getDeviceId(), legoBluetoothDevice);
        this.scannedDevices.remove(legoBluetoothDevice.getDeviceId(), legoBluetoothDevice);
        reportStateChanged("connect", "disconnected", legoBluetoothDevice.getDeviceId());
    }

    @Override
    public void onDidStartInterrogatingDevice(LegoBluetoothDevice legoBluetoothDevice) {

    }

    @Override
    public void onDidFinishInterrogatingDevice(LegoBluetoothDevice legoBluetoothDevice) {

    }

    @Override
    public void didUpdateValueData(LegoService legoService, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void didUpdateInputFormat(LegoService legoService, InputFormat
            inputFormat, InputFormat inputFormat1) {

    }

    /**
     * Report to the webview new state information.
     *
     * @param strg, min. type, state and brickid in this order are reqired (3 arguments). All next arguments have to appear in pairs, key <-> value
     */
    @Override
    public void reportStateChanged(String type, String state, String brickid, String... strg) {
        try {
            if (type != null && state != null && brickid != null) {
                JSONObject newMsg = new JSONObject();
                newMsg.put("target", ROBOT);
                newMsg.put("type", type);
                newMsg.put("state", state);
                newMsg.put("brickid", brickid);
                if (strg != null) {
                    for (int i = 0; i < strg.length; i += 2) {
                        newMsg.put(strg[i], strg[i + 1]);
                    }
                }
                mainView.loadUrl("javascript:webviewController.appToJsInterface('" + newMsg.toString() + "')");
                Log.d(TAG, newMsg.toString());
            } else {
                throw new IllegalArgumentException("Min. 3 parameters required + additional parameters in pairs!");
            }
        } catch (JSONException | IllegalArgumentException | IndexOutOfBoundsException e) {
            Log.e(TAG, e.getMessage() + "caused by: " + type + state + brickid + strg);
        }
    }

    private void reportScanError(String msg) {
        try {
            final JSONObject newMsg = new JSONObject();
            newMsg.put("target", ROBOT);
            newMsg.put("type", "scan");
            newMsg.put("state", "error");
            newMsg.put("message", msg);
            this.mainView.post(new Runnable() {
                @Override
                public void run() {
                    mainView.loadUrl("javascript:webviewController.appToJsInterface('" + newMsg.toString() + "')");

                }
            });
            Log.e(TAG, newMsg.toString());
        } catch (JSONException | IllegalArgumentException e) {
            Log.e(TAG, e.getMessage() + "caused by: scan " + msg);
        }
    }

    private class MotionListener implements MotionSensorCallbackListener {
        WeDoCommunicator weDoCommunicator;

        MotionListener(WeDoCommunicator weDoCommunicator) {
            this.weDoCommunicator = weDoCommunicator;
        }

        @Override
        public void didUpdateDistance(MotionSensor motionSensor, float v, float v1) {
            reportStateChanged("update", Float.toString(v1), motionSensor.getDevice().getDeviceId(), "sensor", motionSensor.getServiceName(), "id", Integer.toString(motionSensor.getDefaultInputFormat().getConnectId()));
        }

        @Override
        public void didUpdateCount(MotionSensor motionSensor, int i) {
            // not supported in Open Roberta
        }

        @Override
        public void didUpdateValueData(LegoService legoService, byte[] bytes, byte[] bytes1) {
            // not supported in Open Roberta
        }

        @Override
        public void didUpdateInputFormat(LegoService legoService, InputFormat inputFormat, InputFormat inputFormat1) {
            // not supported in Open Roberta
        }
    }

    private class TiltListener implements TiltSensorCallbackListener {
        WeDoCommunicator weDoCommunicator;

        TiltListener(WeDoCommunicator weDoCommunicator) {
            this.weDoCommunicator = weDoCommunicator;
        }

        @Override
        public void didUpdateValueData(LegoService legoService, byte[] bytes, byte[] bytes1) {
            // not supported in Open Roberta
        }

        @Override
        public void didUpdateInputFormat(LegoService legoService, InputFormat inputFormat, InputFormat inputFormat1) {
            // not supported in Open Roberta
        }

        @Override
        public void didUpdateDirection(TiltSensor tiltSensor, TiltSensor.TiltSensorDirection tiltSensorDirection, TiltSensor.TiltSensorDirection tiltSensorDirection1) {
            reportStateChanged("update", Float.toString(tiltSensorDirection1.getValue()), tiltSensor.getDevice().getDeviceId(), "sensor", tiltSensor.getServiceName(), "id", Integer.toString(tiltSensor.getDefaultInputFormat().getConnectId()));
        }

        @Override
        public void didUpdateAngle(TiltSensor tiltSensor, TiltSensor.TiltSensorAngle tiltSensorAngle, TiltSensor.TiltSensorAngle tiltSensorAngle1) {
            // not supported in Open Roberta
        }

        @Override
        public void didUpdateCrash(TiltSensor tiltSensor, TiltSensor.TiltSensorCrash tiltSensorCrash, TiltSensor.TiltSensorCrash tiltSensorCrash1) {
            // not supported in Open Roberta
        }
    }
}
