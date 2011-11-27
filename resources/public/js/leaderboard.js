  var chart = new SmoothieChart({millisPerPixel: 500});
  var colourTable = {};
  var timeSeries = {};

  function createTimeline() {
    chart.streamTo(document.getElementById("chart"), 5000);
  }
  function randomRgbValue() {
    return Math.floor(Math.random() * 156 + 99);
  }
  function randomColour() {
    return 'rgb(' + [randomRgbValue(), randomRgbValue(), randomRgbValue()].join(',') + ')';
  }
  function updateChart(players) {
    for (var i=0; i<players.length; i++) {
      var player = players[i];
      var series = timeSeries[player.id];
      if (!series) {
        series = new TimeSeries();
        timeSeries[player.id] = series;
        colourTable[player.id] = randomColour();
        chart.addTimeSeries(series, { strokeStyle : colourTable[player.id], lineWidth: 3 });
      }
      series.append(new Date().getTime(), player.score);
    }
  }
  function updateScoreBoard(players) {
    var body = $("<tbody id='scores'></tbody>");
    var id;
    for (var i=0; i<players.length; i++) {
      var player = players[i];
      var colour = colourTable[player.id];

      (function(id) {
	var completed = (player.completed == player.total);
        body.append(
          $("<tr/>")
            .append("<td><a href='/player/" + player.id + "'>" + player.name + "</a>"
		    + "<span class='pull-right'>" + player.url + "</span></td>")
            .append("<td><div style='width: 20px; background-color: " + colour + "'>&nbsp;</div></td>")
            .append("<td>" + player.score + "</td>")
            .append("<td>" + player.completed + " / " + player.total +
		    (completed ? " <span class='label success'>OK</span>" : "") +
		    (player.bonus ? " <span class='label warning'>Bonus</span>" : "") + "</td>")
            .append($("<td/>")
              .append($("<a>Kick</a>")
                .attr("href", "#")
                .click(function () { var _i = id; return kick(_i); })))
        );
        })(player.id);
    }
    $("#scores").replaceWith(body);
  }
  function update() {
    $.ajax({
      url: '/scores',
      dataType: 'json',
      success: function(data) {
        var players = data;
        updateChart(players);
        updateScoreBoard(players);
      }
    });
  }
  function kick(playerId) {
    if(confirm("Sure?")) {
      $.post("/admin/kick", 
        { id: playerId }, 
        function (data) { update(); } );
    }
    return false;
  }
  $(function() {
    createTimeline();
    setInterval(update, 5000);
  });
  update();

