var socket = null;
var isGenerating = false;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    $("#send").prop("disabled", !connected);
    $("#autoGenerate").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
        document.getElementById("isGeneratingCheckbox").checked = isGenerating;
    }
    $("#sequences").html("");
}
function receiveMsg(msg) {
            if (msg.msgType == "sequences") {
                var data = msg.data
                    var perrow = 1,
                          html = "<table><tr>";

                      for (var i=0; i<data.length; i++) {
                        html += "<td>" + data[i] + "</td>";

                        var next = i+1;
                        if (next%perrow==0 && next!=data.length) {
                          html += "</tr><tr>";
                        }
                      }
                      html += "</tr></table>";
                      document.getElementById("sequences").innerHTML = html;
            }
        }
function connect() {
    socket = new SockJS('/websocket');
     socket.onmessage = function(msg) {
        receiveMsg(JSON.parse(msg.data));
     };
     socket.onopen = function() {
         console.log('open');
         setConnected(true)
     };
     socket.onclose = function() {
          console.log('close');
          setConnected(false)
      };

}

function disconnect() {
    if (socket !== null) {
        socket.close();
    }
}

function sendLength() {
    socket.send(JSON.stringify({ 'type': '/generate', 'length': $("#length").val() }));
}
function startGenerating() {
    isGenerating = !isGenerating
    document.getElementById("isGeneratingCheckbox").checked = isGenerating;
    socket.send(JSON.stringify({ 'type': '/autoGenerate', 'isGenerating': isGenerating }));

}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendLength(); });
    $( "#autoGenerate" ).click(function() { startGenerating(); });
});