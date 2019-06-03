
function identity(e) {
  return e;
}

function identityDiv(e) {
  return $('<td></td>')
      .append(e);
}

function costFormat(e) {
  return Math.round(e);
}

function costDiv(e) {
  return $('<td></td>').
      append(costFormat(e));
}

function makeTimeCell(time, clusterTime){
  return makeCell(time, clusterTime, formatTime);
}

function makeIdentityCell(time, clusterTime){
  return makeCell(time, clusterTime, identity);
}

function makeCell(time, clusterTime, format) {
  var cell = $('<td></td>');
  cell.hover(
      function () {
        $(this).children().each(function () {
          if ($(this).hasClass("time")) {
            $(this).show();
          } else {
            $(this).hide();
          }
        });
      },
      function () {
        $(this).children().each(function () {
          if ($(this).hasClass("percent")) {
            $(this).show();
          } else {
            $(this).hide();
          }
        });
      }
  );
  var timeElement = $('<div class="time"></div>');
  var percentElement = $('<div class="percent"></div>');

  percentElement.append((time * 100 / clusterTime).toFixed(3));
  timeElement.hide();
  timeElement.append(format(time));
  cell.append(percentElement);
  cell.append(timeElement);
  return cell;
}

function renderComparison(currentStats, targetServlet){

  //  this code is dark magic from stack overflow

  // Javascript to enable link to tab
  var hash = document.location.hash;
  var prefix = "tab_";
  if (hash) {
    $('.nav-tabs a[href=' + hash.replace(prefix, "") + ']').tab('show');
  }

  $(document).ready(function () {
    // add a hash to the URL when the user clicks on a tab
    $('a[data-toggle="tab"]').on('click', function (e) {
      history.pushState(null, null, $(this).attr('href'));
    });
    // navigate to a tab when the history changes
    window.addEventListener("popstate", function (e) {
      var activeTab = $('[href=' + location.hash + ']');
      if (activeTab.length) {
        activeTab.tab('show');
      } else {
        $('.nav-tabs a:first').tab('show');
      }
    });
  });


  var $absTable = $('#absolute-tr');
  var $relTable = $('#relative-tr');

  currentStats.forEach(function(statistic){

    $absTable.append($('<th></th>')
        .append(statistic.absDisplay));

    $relTable.append($('<th></th>')
        .append(statistic.relDisplay+' \u0394'));

  });

  $.tablesorter.addParser({
    id: 'days',
    is: function () {
      return false;
    },
    format: function (s) {
      return s.split(' ')[0];
    },
    type: 'numeric'
  });

  $.tablesorter.addParser({
    id: 'floats',
    is: function () {
      return false;
    },
    format: function (s) {
      return parseFloat(s);
    },
    type: 'numeric'
  });

  var absoluteStart = $("#absolute-start-date");
  absoluteStart.datepicker({
    onSelect: refreshAbsoluteTable
  });

  var absoluteEnd = $("#absolute-end-date");
  absoluteEnd.datepicker({
    onSelect: refreshAbsoluteTable
  });

  absoluteEnd.datepicker("setDate", new Date());
  absoluteStart.datepicker("setDate", moment().subtract(30, 'days').toDate());

  var trendsEnd1 = $("#trends-end1-date");
  trendsEnd1.datepicker({
    onSelect: refreshTrendsTable
  });

  var trendsEnd2 = $("#trends-end2-date");
  trendsEnd2.datepicker({
    onSelect: refreshTrendsTable
  });

  var $trends = $("#trends-time");
  $trends.change(refreshTrendsTable);

  trendsEnd1.datepicker("setDate", moment().subtract(30, 'days').toDate());
  trendsEnd2.datepicker("setDate", new Date());

  function refreshTrendsTable() {

    var date1 = moment(trendsEnd1.datepicker('getDate'));
    var date2 = moment(trendsEnd2.datepicker('getDate'));

    var date1Start = moment(date1)
        .subtract($trends.val(), 'days')
        .toDate().getTime();

    var date1End = moment(date1)
        .toDate().getTime();

    var date2Start = moment(date2)
        .subtract($trends.val(), 'days')
        .toDate().getTime();

    var date2End = moment(date2)
        .toDate().getTime();

    $.when(
        $.ajax({
          type: 'GET',
          dataType: 'html',
          url: targetServlet,
          data: {
            started_after: date1Start,
            started_before: date1End
          }
        }),
        $.ajax({
          type: 'GET',
          dataType: 'html',
          url: targetServlet,
          data: {
            started_after: date2Start,
            started_before: date2End
          }
        })
    ).done(function (period1, period2) {
          console.log("Trends:");
          console.log(period1);
          console.log(period2);
          refreshTrendsSuccess(period1[0], period2[0]);
        });
  }

  function getCellClass(value) {
    if (value <= -1) {
      return 'negative';
    } else if (value >= 1) {
      return 'positive';
    } else {
      return 'neutral';
    }
  }

  function safeVal(obj, func) {
    if (obj) {
      return func(obj);
    }
    return 0;
  }

  function refreshTrendsSuccess(dataStr1, dataStr2) {
    var parse1 = JSON.parse(dataStr1);
    var parse2 = JSON.parse(dataStr2);

    var table = $('#cost-trends');
    table.empty();

    Object.keys(parse2).forEach(function (name) {
      var row = $('<tr class="cost_row"></tr>');

      row.append($('<td></td>')
          .append($('<a href="application.html?name=' + encodeURIComponent(name) + '#cost-tab"/></a>')
              .append(name)));

      var data2 = parse2[name];
      var data1 = parse1[name];

      currentStats.forEach(function (statistic) {

        var value = safeVal(data2, statistic.calc) - safeVal(data1, statistic.calc);

        row.append($('<td></td>')
            .addClass(getCellClass(value))
            .append(statistic.relFormat(value)));
      });

      table.append(row);
    });

    $(".workflow-delta").trigger("update");

  }

  function refreshAbsoluteTable() {
    $.ajax({
      type: 'GET',
      dataType: 'html',
      url: targetServlet,
      data: {
        started_after: absoluteStart.datepicker('getDate').getTime(),
        started_before: moment(absoluteEnd.datepicker('getDate')).toDate().getTime()
      },
      success: function (dataStr) {
        console.log("Absolute:");
        console.log(dataStr);
        refreshAbsoluteSuccess(dataStr)
      }
    });
  }

  function refreshAbsoluteSuccess(dataStr) {

    var parse = JSON.parse(dataStr);

    var table = $('#costs');
    table.empty();

    var clusterData = {};
    var clusterAppData = parse["CLUSTER_TOTAL"];
    currentStats.forEach(function (statistic) {
      clusterData[statistic.name] = safeVal(clusterAppData, statistic.calc);
    });

    Object.keys(parse).forEach(function (app) {
      var appData = parse[app];

      if (app != "CLUSTER_TOTAL") {

        var appRow = $('<tr class="cost_row"></tr>')
            .append($('<td></td>')
                .append($('<a href="application.html?name=' + encodeURIComponent(app) + '#cost-tab"/></a>')
                    .append(trimClassName(app))));

        currentStats.forEach(function (statistic) {
          appRow.append(statistic.absCell(
              statistic.calc(appData),
              clusterData[statistic.name]
          ));
        });

        table.append(appRow);
      }
    });

    $(".absolute-costs").trigger("update");

  }

  refreshAbsoluteTable();
  refreshTrendsTable();

  var absHeaders = {};
  var relHeaders = {};

  currentStats.forEach(function (statistic, index) {
    absHeaders[index] = {
      sorter: statistic.absSorter
    };
    relHeaders[index] = {
      sorter: statistic.relSorter
    };
  });

  $('.absolute-costs').tablesorter({
    headers: absHeaders,
    usNumberFormat: true
  });

  $(".workflow-delta").tablesorter({
    headers: relHeaders
  });

}