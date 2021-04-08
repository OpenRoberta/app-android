var webViewType;

function init(  ) {
        var a = {};
        a.target = 'internal';
        a.type = 'identify';
        if ( tryAndroid( a ) ) {
            webViewType = "Android";
      //  } else if ( tryIOS( a ) ) {
      //      webViewType = "IOS";
        } else {
            // Obviously not in an Open Roberta webview
			      webViewType = "LOCALHOST";
            
        }
    }

    function tryAndroid( data ) {
        try {
            OpenRoberta.jsToAppInterface( JSON.stringify( data ) );
            return true;
        } catch ( error ) {
            //LOG.error( "no Android Webview: " + error );
            return false;
        }
    }

function send( txt, res = "json" )
{
	try {
		if ( webViewType === "Android" ) {
			OpenRoberta.jsToAppInterface( txt );
			
		} else if ( webViewType === "IOS" ) {
			window.webkit.messageHandlers.OpenRoberta.postMessage( txt );
		} else if ( webViewType === "LOCALHOST" ) {
			req = new XMLHttpRequest();
			req.open("POST","http://localhost:80/"+res,false);
			req.overrideMimeType("application/json");
			//req.addEventListener("load", onReply);
			//req.timeout = 100;
			req.send( txt );
      if(req.status===200)
			{
			  onReply(req.response);
			}
		} else {
			throw "invalid webview type";
		}
	} catch ( error ) {
		// LOG.error( "jsToAppInterface >" + error + " caused by: " + jsonData );
	}			
}

function onReply(txt)
{
//    if( e.target.status == 200)
//    {
     appToJs(txt); //e.target.response); 
//    }
}

function wvController()
{
  this.appToJsInterface =function( txt )
	{
//  TextFromApp = txt;

  appToJs(txt); 
	}
}

var webviewController = new wvController();
