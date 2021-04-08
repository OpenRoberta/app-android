var devices = [];


function setRobot()
{
  var cmd = { target:"internal", type:"setRobot", robot:"orb" };
  send(JSON.stringify(cmd) );
}

function scanRobot( )
{
var cmd = { target:"orb", type:"startScan" };
send(JSON.stringify(cmd) );
}

function connect()
{
var cmd  = { target:"orb", type:"connect", robot: "" };

var list = document.getElementById("selectDevice");
var nr = list.selectedIndex;
cmd.robot = devices[nr].brickid;
send(JSON.stringify(cmd) );
}

function scanReply(list, res)
{
  if( res.target === "orb" && res.type ==="scan" && res.state ==="appeared")
  {
    devices[devices.length] = {brickid:res.brickid,brickname:res.brickname}; //e.target.responseText;
    var opt  = document.createElement("option");
    opt.text = res.brickname;
    list.add(opt);
  }
}

function initConnect()
{
  window.setTimeout(setRobot,50);
  window.setTimeout(scanRobot,500);
}