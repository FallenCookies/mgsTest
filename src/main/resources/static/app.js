var stompClient = null;
var isGenerating = false;
function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/greetings', function (greeting) {
            showGreeting(JSON.parse(greeting.body).content);
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    stompClient.send("/app/generate", {}, JSON.stringify({'length': $("#length").val()}));
}
function startGenerating() {
    stompClient.send("/app/autoGenerate", {}, JSON.stringify({'isGenerating': !isGenerating}));
    isGenerating = !isGenerating
}
function showGreeting(message) {
    var data = message
    var perrow = 1, // 1 cells per row
          html = "<table><tr>";

      // Loop through array and add table cells
      for (var i=0; i<data.length; i++) {
        html += "<td>" + data[i] + "</td>";

        // If you need to click on the cell and do something
        // html += "<td onclick='FUNCTION()'>" + data[i] + "</td>";

        // Break into next row
        var next = i+1;
        if (next%perrow==0 && next!=data.length) {
          html += "</tr><tr>";
        }
      }
      html += "</tr></table>";
      document.getElementById("greetings").innerHTML = html;
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendName(); });
    $( "#autoGenerate" ).click(function() { startGenerating(); });
});