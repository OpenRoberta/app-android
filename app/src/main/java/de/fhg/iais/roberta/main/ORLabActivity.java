//*******************************************************************
/*!
\file   ORLabActivity.java
\author
\date
\brief
*/

//*************************************************************************************************
package de.fhg.iais.roberta.main;

//*************************************************************************************************
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.PrintWriter;
import java.net.URLDecoder;

import de.fhg.iais.roberta.robot.ORB.ORB_Communicator;
import de.fhg.iais.roberta.robot.RobotCommunicator;

/**
 * <h1>Open Roberta Mobile</h1>
 * Only activity for the Open Roberta Mobile app.
 * <p>
 * The activity holds a connection the https://lab.open-roberta.org via a web view. With this connection
 * it is possible to communicate between the "Lab" and different robot systems like WeDo 2.0. Depending
 * on the users selection in the "Lab" this activity instantiates the corresponding robot communicator,
 * if supported.
 *
 * @author Beate Jost
 * @version 1.0
 * @since 2018-07-15
 */
public class ORLabActivity extends AppCompatActivity
{
    private static final int REQUEST_WRITE_PERMISSION = 1;
    private static final int REQUEST_READ_PERMISSION = 2;
    RobotCommunicator robotCommunicator;
    private WebView orView;
    private ValueCallback<Uri[]> orFilePathCallback;
    private ProgressBar progressBar;
    private String TAG = ORLabActivity.class.getSimpleName();

    //---------------------------------------------------------------------------------------------
    @SuppressLint("AddJavascriptInterface")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main ); // muylayout is your layout.xml

        PreferenceManager.setDefaultValues( this, R.xml.preferences, false );
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( this );
        String orUrl = sharedPreferences.getString( "prefUrl", "" );

  /*
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (wifi == null || !wifi.isConnected()) {
            Util.showCloseNoWifiAlert(ORLabActivity.this, R.string.wifi_not_connected);
        }
*/
        this.orView = findViewById( R.id.orView );
        this.progressBar = findViewById( R.id.progressBar );

        this.robotCommunicator = new RobotCommunicator();

        this.orView.getSettings().setJavaScriptEnabled( true );
        this.orView.getSettings().setDomStorageEnabled( true );
        this.orView.getSettings().setLoadWithOverviewMode( true );
        this.orView.getSettings().setUseWideViewPort( true );
        this.orView.getSettings().setAppCacheEnabled( false );
      
this.orView.getSettings().setAllowContentAccess(true);
this.orView.getSettings().setAllowFileAccess(true);
this.orView.getSettings().setAllowFileAccessFromFileURLs( true );
this.orView.getSettings().setAllowUniversalAccessFromFileURLs( true );
this.orView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        this.orView.requestFocus( View.FOCUS_DOWN );
        this.orView.addJavascriptInterface( this, "OpenRoberta" );

        //-----------------------------------------------------------------------------------------
        this.orView.setDownloadListener( new DownloadListener()
        {
            //-------------------------------------------------------------------------------------
            @Override
            public void onDownloadStart( String url, String userAgent,
                                         String contentDisposition,
                                         String mimeType,
                                         long contentLength )
            {
                if( !Util.isWriteStoragePermissionGranted( ORLabActivity.this,
                                                           REQUEST_WRITE_PERMISSION ) )
                {
                    return;
                }
                DownloadManager downloadManager = (DownloadManager) ORLabActivity.this.getSystemService(
                        DOWNLOAD_SERVICE );
                String content = "";
                File file;
                try
                {
                    content = URLDecoder.decode( url, "UTF-8" );
                    int start = content.indexOf( '/' ) + 1;
                    int end = content.indexOf( ';' );
                    String fileType = content.substring( start, end );
                    String fileName = "NepoProg." + fileType;
                    file = new File( Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS ), fileName );
                    PrintWriter printWriter = new PrintWriter( file );
                    printWriter.println( content.substring( content.indexOf( "," ) + 1 ) );
                    printWriter.close();
                    Log.d( TAG, file.toString() );
                    downloadManager.addCompletedDownload( file.getName(), file.getName(), true,
                                                          "text/xml", file.getAbsolutePath(),
                                                          file.length(), true );
                } catch( java.io.IOException e )
                {
                    Log.e( TAG, "download failed: " + e.getMessage() );
                }
            }
        } );
        this.orView.loadUrl( orUrl );
        this.orView.setWebChromeClient( new orWebViewClient() );
        this.orView.setWebViewClient( new myWebViewClient() );
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT )
        {
            WebView.setWebContentsDebuggingEnabled( true );
        }
    }

    //---------------------------------------------------------------------------------------------
    @Override
    protected void onDestroy()
    {
        robotCommunicator.close();
        super.onDestroy();
    }

    //---------------------------------------------------------------------------------------------
    @Override
    protected void onResume()
    {
        super.onResume();
    }

    //---------------------------------------------------------------------------------------------
    @Override
    protected void onPause()
    {
        super.onPause();
    }

    //---------------------------------------------------------------------------------------------
    public void onActivityResult( int requestCode, int resultCode, Intent intent )
    {
        switch( requestCode )
        {
            case REQUEST_WRITE_PERMISSION:
                Uri[] result;
                if( orFilePathCallback == null )
                {
                    super.onActivityResult( requestCode, resultCode, intent );
                    return;
                }
                if( resultCode == Activity.RESULT_OK )
                {
                    if( intent != null )
                    {
                        String file = intent.getDataString();
                        if( file != null )
                        {
                            result = new Uri[] {Uri.parse( file )};
                            orFilePathCallback.onReceiveValue( result );
                        }
                    }
                }
                orFilePathCallback = null;
                break;
            default:
                robotCommunicator.handleActivityResult( requestCode, resultCode, intent );
        }
        super.onActivityResult( requestCode, resultCode, intent );
    }

    //---------------------------------------------------------------------------------------------
    @Override
    public void onRequestPermissionsResult( int requestCode, String[] permissions,
                                            int[] grantResults )
    {
        switch( requestCode )
        {
            case REQUEST_WRITE_PERMISSION:
                // nothing to do here
                break;
            default:
                robotCommunicator.handleRequestPermissionsResult( requestCode, permissions,
                                                                  grantResults );
        }
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );
    }

    //---------------------------------------------------------------------------------------------
    @JavascriptInterface
    public String jsToAppInterface( String msg )
    {
        try
        {
            JSONObject newMsg = new JSONObject( msg );
            String target = newMsg.getString( "target" );
            String type = newMsg.getString( "type" );
            if( target == null || type == null )
            {
                throw new IllegalArgumentException( "Min. 2 parameters required !" );
            }
            if( target.equals( "internal" ) )
            {
                if( type.equals( "identify" ) )
                {
                    this.identify( newMsg );
                }
                else if( type.equals( "setRobot" ) )
                {
                    this.setRobot( newMsg );
                }
            }
            else if( target.equals( this.robotCommunicator.ROBOT ) )
            {
                this.robotCommunicator.jsToRobot( newMsg );
            }
        } catch( final JSONException e )
        {
            // ignore invalid messages
            Log.e( TAG, "Json parsing error: " + e.getMessage() + " processing: " + msg );
        }
        return ("");
    }

    //---------------------------------------------------------------------------------------------
    public void identify( JSONObject msg )
    {
        try
        {
            msg.put( "name", "OpenRoberta" );
            msg.put( "app_version", BuildConfig.VERSION_NAME );
            msg.put( "device_version", Build.VERSION.SDK_INT );
            msg.put( "model", Build.MODEL );
        } catch( JSONException e )
        {
            Log.e( TAG, "Json parsing error: " + e.getMessage() );
            // nothing to do, msg/answer is save but empty
        }
        final JSONObject answer = msg;
        this.orView.post( new Runnable()
        {
            @Override
            public void run()
            {
                orView.loadUrl(
                        "javascript:webviewController.appToJsInterface('" + answer.toString() + "')" );
            }
        } );
    }

    //---------------------------------------------------------------------------------------------
    public void setRobot( JSONObject msg )
    {
        try
        {
            this.robotCommunicator.close();
            String robot = msg.getString( "robot" );
            switch( robot )
            {
                case "orb":

                    this.robotCommunicator = new ORB_Communicator( this, this.orView );
                    //TODO inform webview
                    break;

                default:
                    this.robotCommunicator = new RobotCommunicator();
                    //TODO inform webview
                    break;
            }
        } catch( JSONException e )
        {
            Log.e( TAG, e.getMessage() );
        }
    }

    //---------------------------------------------------------------------------------------------
    public void openSettings( View v )
    {
        PopupMenu popup = new PopupMenu( getBaseContext(), v );
        popup.getMenuInflater().inflate( R.menu.popup_menu, popup.getMenu() );
        popup.setOnMenuItemClickListener( new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick( MenuItem item )
            {
                switch( item.getItemId() )
                {
                    case R.id.settings:
                        AlertDialog prefDialog = Util.createSettingsDialog( ORLabActivity.this,
                                                                            orView );
                        prefDialog.show();
                        break;
                    case R.id.about:
                        AlertDialog aboutDialog = Util.createAboutDialog( ORLabActivity.this,
                                orView );
                        aboutDialog.show();
                        break;

                    case R.id.service:
                        orView.loadUrl("file:///android_asset/HTML/Service/index.html");
                        break;

                    case R.id.exit:
                        ORLabActivity.this.finish();
                        System.exit( 0 );
                }
                return true;
            }
        } );
        popup.show();
    }

    //---------------------------------------------------------------------------------------------
    public WebView getOrView()
    {
        return this.orView;
    }

    //*********************************************************************************************
    private class orWebViewClient extends WebChromeClient
    {
        //-----------------------------------------------------------------------------------------
        // TODO check if we need this
        public boolean publicbooleanonJsAlert( WebView view, String url, String message,
                                               final JsResult result )
        {
            return true;
        }

        //-----------------------------------------------------------------------------------------
        // TODO check if we need this
        public boolean publicbooleanonJsConfirm( WebView view, String url, String message,
                                                 final JsResult result )
        {
            return true;
        }

        //-----------------------------------------------------------------------------------------
        /**
         * This method is called in blockly "window.prompt(Blockly.Msg.CHANGE_VALUE_TITLE, this.text_)" to allow Android/IOS users to make inputs.
         */
        public boolean publicbooleanonJsPrompt( WebView view, String url, String message,
                                                String defaultValue, final JsPromptResult result )
        {
            return true;
        }

        //-----------------------------------------------------------------------------------------
        /**
         * Overwrite this method to enable the import functions from the Open Roberta Lab.
         */
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        public boolean onShowFileChooser( WebView webView,
                                          ValueCallback<Uri[]> filePathCallback,
                                          FileChooserParams fileChooserParams )
        {
            if( !Util.isReadStoragePermissionGranted( ORLabActivity.this,
                                                      REQUEST_READ_PERMISSION ) )
            {
                return false;
            }
            if( orFilePathCallback != null )
            {
                orFilePathCallback.onReceiveValue( null );
            }
            orFilePathCallback = filePathCallback;

            Intent contentSelectionIntent = new Intent( Intent.ACTION_GET_CONTENT );
            contentSelectionIntent.addCategory( Intent.CATEGORY_OPENABLE );
            // TODO check if we want to allow source code uploading as well. Then we have to determine the file type.
            contentSelectionIntent.setTypeAndNormalize( "text/xml" );

            startActivityForResult( contentSelectionIntent, REQUEST_WRITE_PERMISSION );
            return true;
        }

        //-----------------------------------------------------------------------------------------
        public boolean onCreateWindow( WebView view,
                                       boolean isDialog,
                                       boolean isUserGesture,
                                       Message resultMsg )
        {
            return true;
        }

        //-----------------------------------------------------------------------------------------
        public void onProgressChanged( WebView view, int progress )
        {
            ORLabActivity.this.progressBar.setProgress( progress );
            if( progress == 100 )
            {
                progressBar.setVisibility( View.GONE );
            }
            else
            {
                progressBar.setVisibility( View.VISIBLE );
            }
        }
    }

    //*********************************************************************************************
    public class myWebViewClient extends WebViewClient
    {
        /**
         * Overwrite this method to open the system browser for external links, e.g. data protection declaration
         */
/*        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public boolean shouldOverrideUrlLoading( WebView view, WebResourceRequest request )
        {
            Intent browserIntent = new Intent( Intent.ACTION_VIEW,
                                               Uri.parse( request.getUrl().toString() ) );
            startActivity( browserIntent );
            return true;
        }

 */
    }

    //---------------------------------------------------------------------------------------------
    public void show_Toast( String t )
    {
        Toast.makeText( this, t, Toast.LENGTH_LONG ).show();
    }
}