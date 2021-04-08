package de.fhg.iais.roberta.main;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class Util {

    public static void showCloseNoWifiAlert(final ORLabActivity orLabActivity, int msg) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(orLabActivity);
        builder.setMessage(orLabActivity.getString(msg))
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        orLabActivity.finish();
                        System.exit(0);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public static void enableBluetooth(ORLabActivity orLabActivity, int requestCode) {
        BluetoothAdapter oBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (oBluetoothAdapter.isEnabled())
            return;
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        orLabActivity.startActivityForResult(enableBtIntent, requestCode);
    }

    public static void enableLocationService(final ORLabActivity orLabActivity, final int requestCode) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(orLabActivity);
        builder.setMessage(orLabActivity.getString(R.string.location_activation))
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        orLabActivity.startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), requestCode);
                    }
                })
                .setNegativeButton(R.string.msg_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    public static AlertDialog createSettingsDialog(final Activity orLabActivity, final WebView orView) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(orLabActivity);
        final String defaultValue = sharedPreferences.getString("prefUrl", "https://lab.open-roberta.org");

        LayoutInflater prefLI = LayoutInflater.from(orLabActivity);
        View prefView = prefLI.inflate(R.layout.dialog_url, null);
        final EditText userInput = prefView.findViewById(R.id.dialog_url);
        userInput.setText(defaultValue);
        AlertDialog.Builder prefDialogBuilder = new AlertDialog.Builder(orLabActivity);
        prefDialogBuilder.setView(prefView);
        prefDialogBuilder.setMessage(R.string.pref_change);
        prefDialogBuilder.setTitle(R.string.pref_title_url);
        prefDialogBuilder.setCancelable(false);
        prefDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       // if (defaultValue.equals(userInput.getText().toString())) {
                       //     dialog.dismiss();
                       // } else {
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(orLabActivity);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("prefUrl", userInput.getText().toString());
                            editor.apply();
                            orView.loadUrl(userInput.getText().toString());
                            dialog.dismiss();
                       // }
                    }
                });
        prefDialogBuilder.setNegativeButton(R.string.msg_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        return prefDialogBuilder.create();
    }

    public static AlertDialog createAboutDialog(Activity orLabActivity, final WebView orView) {
        LayoutInflater aboutLI = LayoutInflater.from(orLabActivity);
        View aboutView = aboutLI.inflate(R.layout.dialog_about, null);
        AlertDialog.Builder aboutDialogBuilder = new AlertDialog.Builder(orLabActivity);
        aboutDialogBuilder.setTitle(R.string.pref_title_about);
        aboutDialogBuilder.setView(aboutView);
        TextView textAppAbout = aboutView.findViewById(R.id.textAppAbout);
        textAppAbout.setText(R.string.about_app_content);
        ImageView imageAppAbout = aboutView.findViewById(R.id.imageAppAbout);
        imageAppAbout.setImageResource(R.drawable.logo_open_roberta);
        TextView textFHGAbout = aboutView.findViewById(R.id.textFHGAbout);
        textFHGAbout.setMovementMethod(LinkMovementMethod.getInstance());
        textFHGAbout.setText(R.string.about_fhg_content);
        ImageView imageFHGAbout = aboutView.findViewById(R.id.imageFHGAbout);
        imageFHGAbout.setImageResource(R.drawable.iais_logo);
        String versionName = BuildConfig.VERSION_NAME;
        TextView textVersion = aboutView.findViewById(R.id.textVersion);
        textVersion.setText(orLabActivity.getString(R.string.version, versionName));
        TextView textPublish = aboutView.findViewById(R.id.textPublish);
        textPublish.setMovementMethod(LinkMovementMethod.getInstance());
        textPublish.setText(R.string.about_publish);
        aboutDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        return aboutDialogBuilder.create();
    }

    public static boolean isWriteStoragePermissionGranted(ORLabActivity mainActivity, int reqestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (mainActivity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, reqestCode);
                return false;
            }
        } else {
            return true;
        }
    }

    public static void showAlert(ORLabActivity orLabActivity, String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(orLabActivity);
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setTitle(R.string.alert_title);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static void showAlert(ORLabActivity orLabActivity, int msg) {
        showAlert(orLabActivity, orLabActivity.getResources().getString(msg));
    }

    public static boolean isReadStoragePermissionGranted(ORLabActivity orLabActivity, int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (orLabActivity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(orLabActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
                return false;
            }
        } else {
            return true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void checkForLocationPermission(ORLabActivity orLabActivity, int requestCode) {
        final String[] permissionList = new String[1];
        permissionList[0] = Manifest.permission.ACCESS_COARSE_LOCATION;
        orLabActivity.requestPermissions(permissionList, requestCode);
    }
}
