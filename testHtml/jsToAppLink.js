// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// +++++++++ INTERFACE - JS <-> Native App (Android/IOS) ++++++++++
// 
// Simple interface to connect native apps to the roberta Web page.
// 
// A call from Javascript into the Native App may contain a platform 
// specific name prefix and/or postfix. This can be initialized by 
// the native app when calling 'setJsToAppCall(..)'.
//
// The Native App is then expected to implement a function counter part
// to the JS call 'jsToAppInterface(json)' with a JSON object as the 
// respective command. I.e.
// - Call 'setJsToAppCall(prefix, postfix)' once when HTML doc is loaded.
// - Expect JS calls into the Native App via 'jsToAppInterface(json)'.
// 
// For error messages, check the return value of 'jsToAppInterface(json)'
// with return value:
// - null => no error, 
// - <htmlText> => error.
//
// Author        Wolfgang Vonolfen, Fraunhofer IAIS
// Copyright (c) 2018 Fraunhofer IAIS, Germany
//
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

// App Platform specific part of the call string
var jsToAppCallPrefix = null;
var jsToAppCallPostfix = null;

// Initialize the platform specific part of the call to the native App
//     (must be called once by the native App when HTML doc is loaded).
function setJsToAppCall(theCallPrefix, theCallPostfix) 
	{ jsToAppCallPrefix  = theCallPrefix; jsToAppCallPostfix = theCallPostfix; }

// Pass a JSON object from Javascript to the Native App
//      On error, return the HTML error message as text. On success, return null.
function jsToAppInterface(jsonTxtObj) {
	if ((!jsToAppCallPrefix && !jsToAppCallPostfix) || 
	    (0 === jsToAppCallPrefix.length && 0 === jsToAppCallPostfix.length)) {
		return ("Native App hasn't been connected to the WebSite!");
	}
	eval(jsToAppCallPrefix + "jsToAppInterface" + jsToAppCallPostfix + "('" + (jsonTxtObj.replace("\'", "\\\'")) + "')");
        return (null);
}
