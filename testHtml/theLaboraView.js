// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// helper functions and variables
//
// flag indicating that popup of BT devices is hidden
// (the popup is used when scanning for BT devices)
var peripheralPopupHidden = true;
//
// helper DOM object for escaping HTML text
var escape = document.createElement('textarea');
// converting text with e.g. '<' to text with '&lt;'
function escapeHTML(text) {
    escape.textContent = text;
    return escape.innerHTML;
}
// converting text with e.g. '&lt;' to text with '<'
function unescapeHTML(html) {
    escape.innerHTML = html;
    return escape.textContent;
}
//
// show (error) messages to the user
var nMessageCount = -1;
function showMessage(text, bIsError) {
    if (text == null) return;
    nMessageCount++;
    var msgTxt;
    if (bIsError)
        msgTxt = "<font color=red><b>Msg " + nMessageCount + " ERROR: " + text + "</b></font>";
    else 
        msgTxt = "Msg " + nMessageCount + ": " + text;
    document.getElementById("message").innerHTML = msgTxt;
}
//
// maintain the device list in the popup of BT devices during scanning
function setPeripheral(name, appears) {
    // if entry already exists, only adjust the color
    var theChild = document.getElementById("deviceDropdown").firstChild.nextSibling;
    var theHtmlName = escapeHTML(name).replace(/\s/g, "&nbsp;");
    while (theChild) {
        if (theChild.innerHTML == theHtmlName) {
            theChild.style.color=(appears ? "black" : "lightGrey");
            theChild.disabled = (appears ? false : true);
            return;
        }
        theChild = theChild.nextSibling;
    }
    // not found, create a new entry to the popup
    var linkElement = document.createElement("A");
    linkElement.href="#";
    linkElement.style.padding="6px 16px";
    linkElement.style.display="block";
    linkElement.innerHTML = theHtmlName;
    linkElement.onclick=function() {connectDevice(theHtmlName, name);}
    linkElement.style.color=(appears ? "black" : "lightGrey");
    linkElement.disabled = (appears ? false : true);
    document.getElementById("deviceDropdown").appendChild(linkElement);
}



// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// +++++++++++++++ CALLS FROM THE NATIVE APP TO JS ++++++++++++++++
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
function appToJsInterface(theText) {
    // parse the JSON text sent from the native app
    var obj = JSON.parse(theText);
    //
    // -------------------- PERIPHERAL STUFF --------------------------
    if (obj.target == "peripheral") {
        if (obj.op == "message") {
            showMessage(obj.val1, (obj.val2 != "0"));
            return;
        }
        if (obj.op == "scan") {
            if (obj.val1=="started") {
                // note: obj.val2 may be "" when "start scanning" failed
                if (obj.val2=="")
                    return;
                showScanningPopup(obj.val2);
                return;
            }
            if (obj.val1=="stopped") {
                // note: obj.val2 may be "" when "stop scanning" failed
                if (obj.val2=="")
                    return;
                hideScanningPopup(obj.val2);
                return;
            }
            if    (peripheralPopupHidden) 
                return;
            setPeripheral(obj.val2, (obj.val1=="detected"));
            return;
        }
        if (obj.op == "name") {
            // note obj.val1 may be "" denoting "disconnecting"
            if (obj.val1 == "") {
                var domObj;
                document.getElementById("device_name").innerHTML = "(No Device)";
                domObj = document.getElementById("motorIndex");
                while (domObj.options.length > 0) 
                    { domObj.remove(0); }
                document.getElementById("motorNum").innerHTML = "(-)";
                domObj = document.getElementById("lightIndex");
                while (domObj.options.length > 0) 
                    { domObj.remove(0); }
                document.getElementById("lightNum").innerHTML = "(-)";
                domObj = document.getElementById("lightIndex");
                while (domObj.options.length > 0) 
                    { domObj.remove(0); }
                document.getElementById("lightNum").innerHTML = "(-)";
                domObj = document.getElementById("piezoIndex");
                while (domObj.options.length > 0) 
                    { domObj.remove(0); }
                document.getElementById("piezoNum").innerHTML = "(-)";
                domObj = document.getElementById("orientationIndex");
                while (domObj.options.length > 0) 
                    { domObj.remove(0); }
                document.getElementById("orientationNum").innerHTML = "(-)";
                domObj = document.getElementById("motionIndex");
                while (domObj.options.length > 0) 
                    { domObj.remove(0); }
                document.getElementById("motionNum").innerHTML = "(-)";
                domObj = document.getElementById("voltageIndex");
                while (domObj.options.length > 0) 
                    { domObj.remove(0); }
                document.getElementById("voltageNum").innerHTML = "(-)";
                domObj = document.getElementById("currentIndex");
                while (domObj.options.length > 0) 
                    { domObj.remove(0); }
                document.getElementById("currentNum").innerHTML = "(-)";
                return;
                }
            // obj.val1 != ""
            document.getElementById("device_name").innerHTML = obj.val1;
            if ( !peripheralPopupHidden ) {
                document.getElementById("deviceDropdown").style.display = "none";
                peripheralPopupHidden = true;
                // clear all entries made
                var thePopup = document.getElementById("deviceDropdown");
                while (thePopup.firstChild) { 
                    thePopup.removeChild(thePopup.firstChild);
                }
            }
            return;
        }
        if (obj.op == "manufacturer") {
            document.getElementById("manufacturer_name").innerHTML = obj.val1;
            return;
        }
        if (obj.op == "firmware") {
            document.getElementById("firmware_version").innerHTML = obj.val1;
            return;
        }
        if (obj.op == "hardware") {
            document.getElementById("hardware_version").innerHTML = obj.val1;
            return;
        }
        if (obj.op == "software") {
            if (obj.val2 == "lib")
                document.getElementById("software_version_lib").innerHTML = obj.val1;
            else // (obj.val2 == "dev")
                document.getElementById("software_version_dev").innerHTML = obj.val1;
            return;
        }
        if (obj.op == "battery") {
            document.getElementById("battery_level").innerHTML = obj.val1;
            return;
        }
        if (obj.op == "services") {
            var data = obj.val1.split(" ");

            for (i = 0; i < data.length; i+=2) {
                var domObjSel = null;
                var domObjInf = null;
                if (data[i] == "motor") {
                    domObjSel = document.getElementById("motorIndex");
                    domObjInf = document.getElementById("motorNum");
                } else if (data[i] == "light") {
                    domObjSel = document.getElementById("lightIndex");
                    domObjInf = document.getElementById("lightNum");
                } else if (data[i] == "piezo") {
                    domObjSel = document.getElementById("piezoIndex");
                    domObjInf = document.getElementById("piezoNum");
                } else if (data[i] == "orientation") {
                    domObjSel = document.getElementById("orientationIndex");
                    domObjInf = document.getElementById("orientationNum");
                } else if (data[i] == "motion") {
                    domObjSel = document.getElementById("motionIndex");
                    domObjInf = document.getElementById("motionNum");
                } else if (data[i] == "voltage") {
                    domObjSel = document.getElementById("voltageIndex");
                    domObjInf = document.getElementById("voltageNum");
                } else if (data[i] == "current") {
                    domObjSel = document.getElementById("currentIndex");
                    domObjInf = document.getElementById("currentNum");
                } else {
                    showMessage("(at Javascript) Unsupported Bluetooth LE device service (sensor/actor) type '" + data[i] + "'", true);
                    continue;
                }
                 if (domObjSel == null || domObjInf == null) {
                    showMessage("(at Javascript) Cannot find HTML element for Bluetooth LE device service (sensor/actor) type '" + data[i] + "'", true);
                    continue;
                }
                var nNum = parseInt(data[i+1]);
                for (j = 0; j < nNum; j++) {
                    var option = document.createElement("option");
                    option.text  = j.toString();
                    option.value = j.toString();
                    domObjSel.add(option);
                }
                domObjInf.innerHTML = "(" + data[i+1] + ")";
            }
            return;    
        }
        showMessage("(at Javascript) INVALID COMMAND: " + 
                obj.target + ", " + obj.op + " (=invalid): " + 
                obj.val1 + ", " + obj.val2 + ", " + obj.val3, true);
        return;
    }
    //
    // ------------------------ MOTOR STUFF ---------------------------
    if (obj.target == "motor") {
        if (obj.id != document.getElementById("motorIndex").value) {
            showMessage("COMMAND: " + obj.target + "/" + obj.id + "/" + obj.op + "/" + obj.val1, false);
            return;
        }
        if (obj.op == "speed") {
            var nSpeed = parseInt(obj.val1);
            if (nSpeed < 0) {
                document.getElementById("motor_value").innerHTML = "<font style=\"font-size: 1.5em;\"><b>left:&nbsp;" + (-nSpeed) + "</b></font>";
            } else if (nSpeed > 0) {
                document.getElementById("motor_value").innerHTML = "<font style=\"font-size: 1.5em;\"><b>right:&nbsp;" + (nSpeed) + "</b></font>";
            } else {
                document.getElementById("motor_value").innerHTML = "<font style=\"font-size: 1.5em;\"><b>no&nbsp;speed&nbsp;(0)" + "</b></font>";
            }
            return;
        }
        if (obj.op == "lock") {
            if (obj.val1 == "brake") {
                document.getElementById("motor_value").innerHTML = "<font style=\"font-size: 1.5em;\"><b>BRAKE</b></font>";
            } else {
                document.getElementById("motor_value").innerHTML = "<font style=\"font-size: 1.5em;\"><b>DRIFT</b></font>";
            }
            return;
        }
        showMessage("(at Javascript) INVALID COMMAND: " + 
                obj.target + ", " + obj.op + " (=invalid): " + 
                obj.val1 + ", " + obj.val2 + ", " + obj.val3, true);
        return;
    }
    //
    // ------------------------ LIGHT STUFF ---------------------------
    if (obj.target == "light") {
        if (obj.id != document.getElementById("lightIndex").value) {
            showMessage("COMMAND: " + obj.target + "/" + obj.id + "/" + obj.op + "/" + obj.val1, false);
            return;
        }
        if (obj.op == "mode") {
            if (obj.val1 == "discrete") {
                document.getElementById("lightMode").value = "discrete";
                document.getElementById("light_value").innerHTML = "index:&nbsp;--";
            } else {
                document.getElementById("lightMode").value = "absolute";
                document.getElementById("light_value").innerHTML = "value&nbsp;--";
            }
            return;
        }
        if (obj.op == "discrete") {
            document.getElementById("light_value").innerHTML = "<font style=\"font-size: 1.5em;\"><b>index:&nbsp;" + obj.val1 + "</b></font>";
            return;
        }
        if (obj.op == "crash") {
            document.getElementById("light_value").innerHTML = "<font style=\"font-size: 1.5em;\"><b>value:&nbsp;" + obj.val1 + "</b></font>";
            return;
        }
        showMessage("(at Javascript) INVALID COMMAND: " + 
                obj.target + ", " + obj.op + " (=invalid): " + 
                obj.val1 + ", " + obj.val2 + ", " + obj.val3, true);
        return;
    }
    //
    // --------------- ORIENTATION SENSOR STUFF --------------------
    if (obj.target == "orientation") {
        if (obj.id != document.getElementById("orientationIndex").value) {
            showMessage("COMMAND: " + obj.target + "/" + obj.id + "/" + obj.op + "/" + obj.val1, false);
            return;
        }
        if (obj.op == "mode") {
            if (obj.val1 == "off") {
                document.getElementById("orientationMode").value = "off";
                document.getElementById("orientation_sensor_value").innerHTML = "OFF";
            } else if (obj.val1 == "angle") {
                document.getElementById("orientationMode").value = "angle";
                document.getElementById("orientation_sensor_value").innerHTML = "--&nbsp;angle";
            } else if (obj.val1 == "tilt") {
                document.getElementById("orientationMode").value = "tilt";
                document.getElementById("orientation_sensor_value").innerHTML = "--&nbsp;tilt";
            } else {
                document.getElementById("orientationMode").value = "crash";
                document.getElementById("orientation_sensor_value").innerHTML = "--&nbsp;crash";
            }
            return;
        }
        if (obj.op == "angle") {
            document.getElementById("orientation_sensor_value").innerHTML = "<font style=\"font-size: 1.5em;\"><b>x: " + obj.val1 + ", y: " + obj.val2 + "&nbsp;angle</b></font>";
            return;
        }
        if (obj.op == "tilt") {
            document.getElementById("orientation_sensor_value").innerHTML = "<font style=\"font-size: 1.5em;\"><b>" + obj.val1 + "&nbsp;tilt</b></font>";
            return;
        }
        if (obj.op == "crash") {
            document.getElementById("orientation_sensor_value").innerHTML = 
                "<font style=\"font-size: 1.5em;\"><b>x: " + obj.val1 + ", y: " + obj.val2 + ", z: " + obj.val3 + "&nbsp;crash</b></font>";
            return;
        }
        showMessage("(at Javascript) INVALID COMMAND: " + 
                obj.target + ", " + obj.op + " (=invalid): " + 
                obj.val1 + ", " + obj.val2 + ", " + obj.val3, true);
        return;
    }
    //
    // --------------- MOTION SENSOR STUFF --------------------
    if (obj.target == "motion") {
        if (obj.id != document.getElementById("motionIndex").value) {
            showMessage("COMMAND: " + obj.target + "/" + obj.id + "/" + obj.op + "/" + obj.val1, false);
            return;
        }
        if (obj.op == "mode") {
            if (obj.val1 == "off") {
                document.getElementById("motionMode").value = "off";
                document.getElementById("motion_sensor_value").innerHTML = "OFF";
            } else if (obj.val1 == "distance") {
                document.getElementById("motionMode").value = "distance";
                document.getElementById("motion_sensor_value").innerHTML = "--&nbsp;distance";
            } else {
                document.getElementById("motionMode").value = "count";
                document.getElementById("motion_sensor_value").innerHTML = "--&nbsp;times";
            }
            return;
        }
        if (obj.op == "distance") {
            document.getElementById("motion_sensor_value").innerHTML = "<font style=\"font-size: 1.5em;\"><b>" + obj.val1 + "&nbsp;distance</b></font>"; 
            return;
        }
        if (obj.op == "count") {
            document.getElementById("motion_sensor_value").innerHTML = "<font style=\"font-size: 1.5em;\"><b>" + obj.val1 + "&nbsp;times</b></font>"; 
            return;
        }
        showMessage("(at Javascript) INVALID COMMAND: " + 
                obj.target + ", " + obj.op + " (=invalid): " + 
                obj.val1 + ", " + obj.val2 + ", " + obj.val3, true);
        return;
    }
    //
    // --------------- VOLTAGE SENSOR STUFF --------------------
    if (obj.target == "voltage") {
        if (obj.op == "value") {
            if (obj.id != document.getElementById("voltageIndex").value) {
                showMessage("COMMAND: " + obj.target + "/" + obj.id + "/" + obj.op + "/" + obj.val1, false);
                return;
            }
            document.getElementById("voltage_sensor_value").innerHTML = "<font style=\"font-size: 1.5em;\"><b>" + obj.val1 + "&nbsp;mV</b></font>"; 
            return;
        } 
        if (obj.op == "mode") {
            if (obj.val1 == "off") {
                document.getElementById("voltageMode").value = "off";
                document.getElementById("voltage_sensor_value").innerHTML = "OFF";
            } else {
                document.getElementById("voltageMode").value = "on";
                document.getElementById("voltage_sensor_value").innerHTML = "--&nbsp;mV";
            }
            return;
        }
        showMessage("(at Javascript) INVALID COMMAND: " + 
                obj.target + ", " + obj.op + " (=invalid): " + 
                obj.val1 + ", " + obj.val2 + ", " + obj.val3, true);
        return;
    }
    //
    // --------------- CURRENT SENSOR STUFF --------------------
    if (obj.target == "current") {
        if (obj.op == "value") {
            if (obj.id != document.getElementById("currentIndex").value) {
                showMessage("COMMAND: " + obj.target + "/" + obj.id + "/" + obj.op + "/" + obj.val1, false);
                return;
            }
            document.getElementById("current_sensor_value").innerHTML = "<font style=\"font-size: 1.5em;\"><b>" + obj.val1 + "&nbsp;mA</b></font>"; 
            return;
        }
        if (obj.op == "mode") {
            if (obj.val1 == "off") {
                document.getElementById("currentMode").value = "off";
                document.getElementById("current_sensor_value").innerHTML = "OFF";
            } else {
                document.getElementById("currentMode").value = "on";
                document.getElementById("current_sensor_value").innerHTML = "--&nbsp;mA";
            }
            return;
        }
        showMessage("(at Javascript) INVALID COMMAND: " + 
                obj.target + ", " + obj.op + " (=invalid): " + 
                obj.val1 + ", " + obj.val2 + ", " + obj.val3, true);
        return;
    }
    // --------------- ERROR invalid target --------------------
    showMessage("(at Javascript) UNKNOWN COMMAND: " + 
            obj.target + " (=invalid), " + obj.op + ": " + 
            obj.val1 + ", " + obj.val2 + ", " + obj.val3, true);
}



// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// +++++++++++++++ CALLS FROM JS TO THE NATIVE APP ++++++++++++++++
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// 'jsToAppInterface(jsonString)' is defined in file jsToAppLink.js, 
// Eventually use <string>.replace("\'", "\\\'") to escape '.
//
// -------------------- PERIPHERAL STUFF --------------------------
//
function selectSystem(e) { document.getElementById("libSelect").style.display = "block"; }
function setLib(what)    { document.getElementById("libSelect").style.display = "none";
                           if (what == null) 
                               return;
                           showMessage("Set Library to '" + what + "'", false);
                           showMessage(jsToAppInterface('{"target" : "peripheral", "op" : "library", "val1" : "' + what + '"}'), true);
                           if      (what == '')       { document.getElementById("system_name").innerHTML = '(No System Selected)';
                                                        document.getElementById("software_version_lib").innerHTML = ''; }
                           else if (what == 'dummy')    document.getElementById("system_name").innerHTML = 'Roberta Test Dummy';
                           else if (what == 'lego_2.0') document.getElementById("system_name").innerHTML = 'Lego WeDo 2.0';
                           else                         document.getElementById("system_name").innerHTML = '(unknown system type: ' + what + ')'; }
function startScan(e)    { // display popup
                           showMessage(jsToAppInterface('{"target" : "peripheral", "op" : "startScan"}'), true); }
function showScanningPopup(btID) {
                           if (!peripheralPopupHidden)
                               return;
                           // set title
                           var thePopup = document.getElementById("deviceDropdown");
                           var textnode = document.createElement("P");
                           textnode.innerHTML = "<table style=\"margin: 12px 16px; width: calc(100% - 32px);\">" + 
                                                "<tr><td>Device&nbsp;" + escapeHTML(btID).replace(" ", "&nbsp;") + "</td>" + 
                                                "<td align=\"right\"><b><a href=\"#\" onClick=\"stopScan(null);\">" + 
                                                "X</a></b></td></tr></table>";
                           textnode.style.color="black"; 
                           //  textnode.style.padding = "12px 16px"; 
                           textnode.style.display = "inline";
                           textnode.style.fontWeight = "bold";
                           thePopup.appendChild(textnode);
                           // display popup
                           document.getElementById("deviceDropdown").style.display = "block";
                           peripheralPopupHidden = false; }
function stopScan(e)   { showMessage(jsToAppInterface('{"target" : "peripheral", "op" : "stopScan"}'), true); }
function hideScanningPopup(btID) {
                           document.getElementById("deviceDropdown").style.display = "none";
                           peripheralPopupHidden = true;
                           // clear all entries made
                           var thePopup = document.getElementById("deviceDropdown");
                           while (thePopup.firstChild)
                              thePopup.removeChild(thePopup.firstChild); }
function connectDevice(dev, devPure) { // check if item is disabled
                           var theChild = document.getElementById("deviceDropdown").firstChild.nextSibling;
                           while (theChild) {
                              if (theChild.innerHTML == dev) {
                                  if (theChild.disabled)
                                      return;
                                  break;
                              }
                              theChild = theChild.nextSibling;
                           }
                           showMessage(jsToAppInterface('{"target" : "peripheral", "op" : "connect", "val1" : "' + devPure + '"}'), true); }             
function disconnect(e) { showMessage(jsToAppInterface('{"target" : "peripheral", "op" : "disconnect"}'), true); }
//
// ------------------------ MOTOR STUFF ---------------------------
//
function left(e)  { var nIdx = document.getElementById("motorIndex").value;
                    if (nIdx == "") 
                        { showMessage("No motor actor available"); return; }
                    showMessage(jsToAppInterface('{"target" : "motor", "id" : ' + nIdx + ', "op" : "speed", "val1" : -50}'), true); }
function right(e) { var nIdx = document.getElementById("motorIndex").value;
                    if (nIdx == "") 
                        { showMessage("No motor actor available"); return; }
                    showMessage(jsToAppInterface('{"target" : "motor", "id" : ' + nIdx + ', "op" : "speed", "val1" : 50}'), true); }
function stop(e)  { var nIdx = document.getElementById("motorIndex").value;
                    if (nIdx == "") 
                        { showMessage("No motor actor available"); return; }
                    showMessage(jsToAppInterface('{"target" : "motor", "id" : ' + nIdx + ', "op" : "speed", "val1" : 0}'), true); }
function brake(e) { var nIdx = document.getElementById("motorIndex").value;
                    if (nIdx == "") 
                        { showMessage("No motor actor available"); return; }
                    showMessage(jsToAppInterface('{"target" : "motor", "id" : ' + nIdx + ', "op" : "lock", "val1" : "brake"}'), true); }
function drift(e) { var nIdx = document.getElementById("motorIndex").value;
                    if (nIdx == "") 
                        { showMessage("No motor actor available"); return; }
                    showMessage(jsToAppInterface('{"target" : "motor", "id" : ' + nIdx + ', "op" : "lock", "val1" : "drift"}'), true); }
//
// ------------------------ LIGHT STUFF ---------------------------
//
function lightMode(e) { var htmlTxt = "";
                        var jsonVal = "";
                        if   ($( this ).val() == "discrete") { htmlTxt = "index&nbsp;??"; jsonVal = "discrete"; }
                        else                                 { htmlTxt = "value&nbsp;??"; jsonVal = "absolute"; }
                        document.getElementById("light_value").innerHTML = htmlTxt; 
                        var nIdx = document.getElementById("lightIndex").value;
                        if (nIdx == "") 
                            { showMessage("No light actor available"); return; }
                        showMessage(jsToAppInterface('{"target" : "light", "id" : ' + nIdx + ', "op" : "mode", "val1" : "' + jsonVal + '"}'), true); }
function setColor(nIndex, nHexVal) {
                        var nIdx = document.getElementById("lightIndex").value;
                        if (nIdx == "") 
                            { showMessage("No light actor available"); return; }
                        var modeSel  = "";
                        var colorVal = 0;
                        var htmlTxt  = "";
                        if ( document.getElementById("lightMode").value == "discrete") {
                            modeSel  = "discrete";
                            colorVal = nIndex;
                            htmlTxt  = "index&nbsp;" + nIndex;
                        } else {
                            modeSel = "absolute";
                            colorVal = nHexVal;
                            htmlTxt  = "value&nbsp;" + nHexVal; }
                        document.getElementById("light_value").innerHTML= htmlTxt;
                        showMessage(jsToAppInterface('{"target" : "light", "id" : ' + nIdx + ', "op" : "' + modeSel + '", "val1" : ' + colorVal + '}'), true); }
function color0(e)    { setColor(0, 0x000000); }
function color1(e)    { setColor(1, 0xAA00AA); }
function color2(e)    { setColor(2, 0x800080); }
function color3(e)    { setColor(3, 0x0000AA); }
function color4(e)    { setColor(4, 0x00AAAA); }
function color5(e)    { setColor(5, 0x00CC00); }
function color6(e)    { setColor(6, 0x009900); }
function color7(e)    { setColor(7, 0xAAAA00); }
function color8(e)    { setColor(8, 0xFFA500); }
function color9(e)    { setColor(9, 0xAA0000); }
//
// ------------------------ PIEZO STUFF ---------------------------
//
function piezoPlay(e) { var nIdx = document.getElementById("piezoIndex").value;
                        if (nIdx == "") 
                            { showMessage("No piezo actor available"); return; }
                        var sMode = (document.getElementById("piezoMode").value == "note" ? "note" : "frequency");
                        var sVal  = document.getElementById("piezoValue").value;
                        var sDur  = document.getElementById("piezoDur").value;
                        showMessage(jsToAppInterface('{"target" : "piezo", "id" : ' + nIdx + 
                                                       ', "op" : "' + sMode + '"' + 
                                                       ', "val1" : ' + (isNaN(sVal) ? 0 : parseInt(sVal)) + 
                                                       ', "val2" : ' + (isNaN(sDur) ? 0 : parseInt(sDur)) + 
                                                       '}'), true); }
function piezoStop(e) { var nIdx = document.getElementById("piezoIndex").value;
                        if (nIdx == "") 
                            { showMessage("No piezo actor available"); return; }
                        showMessage(jsToAppInterface('{"target" : "piezo", "id" : ' + nIdx + ', "op" : "stop"}'), true); }
//
// ------------------------ ORIENTATION SENSOR STUFF ---------------------------
//
function orientationMode(e) { var htmlTxt = "";
                              var jsonVal = "";
                              if      ($( this ).val() == "off")   { htmlTxt = "OFF";           jsonVal = "off"; }
                              else if ($( this ).val() == "angle") { htmlTxt = "??&nbsp;angle"; jsonVal = "angle"; }
                              else if ($( this ).val() == "tilt")  { htmlTxt = "??&nbsp;tilt";  jsonVal = "tilt";  }
                              else                                 { htmlTxt = "??&nbsp;crash"; jsonVal = "crash"; }
                              document.getElementById("orientation_sensor_value").innerHTML = htmlTxt; 
                              var nIdx = document.getElementById("orientationIndex").value;
                              if (nIdx == "") 
                                  { showMessage("No orientation sensor available"); return; }
                              showMessage(jsToAppInterface('{"target" : "orientation", "id" : ' + nIdx + ', "op" : "mode", "val1" : "' + jsonVal + '"}'), true); }
//
// ------------------------ MOTION SENSOR STUFF ---------------------------
//
function motionMode(e) { var htmlTxt = "";
                         var jsonVal = "";
                         if      ($( this ).val() == "off")   { htmlTxt = "OFF";              jsonVal = "off"; }
                         else if ($( this ).val() == "count") { htmlTxt = "??&nbsp;times";    jsonVal = "count"; }
                         else                                 { htmlTxt = "??&nbsp;distance"; jsonVal = "distance"; }
                         document.getElementById("motion_sensor_value").innerHTML = htmlTxt; 
                         var nIdx = document.getElementById("motionIndex").value;
                         if (nIdx == "") 
                             { showMessage("No motion sensor available"); return; }
                         showMessage(jsToAppInterface('{"target" : "motion", "id" : ' + nIdx + ', "op" : "mode", "val1" : "' + jsonVal + '"}'), true); }
//
// ------------------------ VOLTAGE SENSOR STUFF ---------------------------
//
function voltageMode(e) { var htmlTxt = "";
                          var jsonVal = "";
                          if      ($( this ).val() == "off")   { htmlTxt = "OFF";        jsonVal = "off"; }
                          else                                 { htmlTxt = "??&nbsp;mV"; jsonVal = "on"; }
                          document.getElementById("voltage_sensor_value").innerHTML = htmlTxt; 
                          var nIdx = document.getElementById("voltageIndex").value;
                          if (nIdx == "") 
                              { showMessage("No voltage sensor available"); return; }
                          showMessage(jsToAppInterface('{"target" : "voltage", "id" : ' + nIdx + ', "op" : "mode", "val1" : "' + jsonVal + '"}'), true); }
//
// ------------------------ CURRENT SENSOR STUFF ---------------------------
//
function currentMode(e) { var htmlTxt = "";
                          var jsonVal = "";
                          if      ($( this ).val() == "off")   { htmlTxt = "OFF";        jsonVal = "off"; }
                          else                                 { htmlTxt = "??&nbsp;mV"; jsonVal = "on"; }
                          document.getElementById("current_sensor_value").innerHTML = htmlTxt; 
                          var nIdx = document.getElementById("currentIndex").value;
                          if (nIdx == "") 
                              { showMessage("No current sensor available"); return; }
                          showMessage(jsToAppInterface('{"target" : "current", "id" : ' + nIdx + ', "op" : "mode", "val1" : "' + jsonVal + '"}'), true); }






$(document).ready(function() {
    // -------------------- PERIPHERAL STUFF --------------------------
    $('#selectSystem').on('click', selectSystem);
    $('#startScan').on('click', startScan);
    // Stop Scan button is part of the Popup: $('#stopScan').on('click', stopScan);
    $('#disconnect').on('click', disconnect);
    // ------------------------ MOTOR STUFF ---------------------------
    $('#left').on('click', left);
    $('#right').on('click', right);
    $('#stop').on('click', stop);
    $('#brake').on('click', brake);
    $('#drift').on('click', drift);
    // ------------------------ LIGHT STUFF ---------------------------
    $('#lightMode').on('change', lightMode);
    $('#color0').on('click', color0);
    $('#color1').on('click', color1);
    $('#color2').on('click', color2);
    $('#color3').on('click', color3);
    $('#color4').on('click', color4);
    $('#color5').on('click', color5);
    $('#color6').on('click', color6);
    $('#color7').on('click', color7);
    $('#color8').on('click', color8);
    $('#color9').on('click', color9);
    // ------------------------ PIEZO STUFF ---------------------------
    $('#piezoPlay').on('click', piezoPlay);
    $('#piezoStop').on('click', piezoStop);
    // ------------------------ ORIENTATION SENSOR STUFF ---------------------------
    $('#orientationMode').on('change', orientationMode);
    // ------------------------ MOTION SENSOR STUFF ---------------------------
    $('#motionMode').on('change', motionMode);
    // ------------------------ VOLTAGE SENSOR STUFF ---------------------------
    $('#voltageMode').on('change', voltageMode);
    // ------------------------ CURRENT SENSOR STUFF ---------------------------
    $('#currentMode').on('change', currentMode);

});

