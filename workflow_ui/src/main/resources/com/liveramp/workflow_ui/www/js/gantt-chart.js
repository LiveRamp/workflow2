
function renderGantt(selector, executions){

  $(selector).empty();

  var tasks = [];
  var scopes = {};

  executions.forEach(function(e){
    var scope = e.execution.scope_identifier;
    scopes[scope] = true;

    e.attempts.forEach(function(attempt){

      var startTime = attempt.start_time;
      var endTime = attempt.end_time;

      if(!endTime || endTime == 0){
        endTime = new Date().getTime();
      }

      tasks.push({
        "startDate":new Date(startTime),
        "endDate": new Date(endTime),
        "taskName": scope,
        "status":attempt.status,
        "attempt":attempt.id
      });

    });

  });

  tasks.sort(function(a, b) {
    return a.startDate - b.startDate;
  });

  var format = "%Y-%m-%d";
  var gantt = d3.gantt(selector, Object.keys(scopes)).tickFormat(format);

  gantt(tasks);

}

/**
 * @author Dimitry Kudrayvtsev
 * @version 2.0
 */

d3.gantt = function (selector, inputTaskTypes) {

  var margin = {
    top: 20,
    right: 40,
    bottom: 20,
    left: 150
  };
  var timeDomainStart = d3.time.day.offset(new Date(), -3);
  var timeDomainEnd = d3.time.hour.offset(new Date(), +3);
  var taskTypes = inputTaskTypes;
  var height = 200 + 50 * taskTypes.length + margin.top + margin.bottom;
  var width = document.body.clientWidth - margin.left - margin.right - 5;

  var tickFormat = "%H:%M";

  var keyFunction = function (d) {
    return d.startDate + d.taskName + d.endDate;
  };

  var rectTransform = function (d) {
    return "translate(" + x(d.startDate) + "," + y(d.taskName) + ")";
  };

  var x = d3.time.scale().domain([timeDomainStart, timeDomainEnd]).range([0, width]).clamp(true);

  var y = d3.scale.ordinal().domain(taskTypes).rangeRoundBands([0, height - margin.top - margin.bottom], .1);

  var yAxis = d3.svg.axis().scale(y).orient("left").tickSize(0);

  var xAxis;

  var initTimeDomain = function (tasks) {
    tasks.sort(function (a, b) {
      return a.endDate - b.endDate;
    });
    timeDomainEnd = tasks[tasks.length - 1].endDate;
    tasks.sort(function (a, b) {
      return a.startDate - b.startDate;
    });
    timeDomainStart = tasks[0].startDate;
  };

  var initAxis = function () {
    x = d3.time.scale().domain([timeDomainStart, timeDomainEnd]).range([0, width]).clamp(true);
    y = d3.scale.ordinal().domain(taskTypes).rangeRoundBands([0, height - margin.top - margin.bottom], .1);
    xAxis = d3.svg.axis().scale(x).orient("bottom").tickFormat(d3.time.format(tickFormat)).tickSubdivide(true)
        .tickSize(8).tickPadding(8).ticks(8);

    yAxis = d3.svg.axis().scale(y).orient("left").tickSize(0);
  };

  function gantt(tasks) {

    initTimeDomain(tasks);
    initAxis();

    var svg = d3.select(selector)
        .append("svg")
        .attr("class", "attempt-gantt")
        .attr("height", height+20)
        .append("g")
        .attr("class", "gantt-chart")
        .attr("width", "100%")
        .attr("height", height)
        .attr("transform", "translate(" + margin.left + ", " + margin.top + ")");

    var tip = d3.tip()
        .attr('class', 'd3-tip')
        .offset([-10, 0])
        .html(function (d) {
          return "<strong>Start:</strong>" + getPrettyDate(d.startDate.getTime()) + "<br><strong>End:</strong>" + getPrettyDate(d.endDate.getTime());
        });

    svg.selectAll(".chart")
        .data(tasks, keyFunction).enter()
        .append("rect")
        .attr("rx", 5)
        .attr("ry", 5)
        .attr("class", function (d) {
          return "gantt-element workflow_row " + d.status;
        })
        .attr("y", 0)
        .attr("transform", rectTransform)
        .attr("height", function (d) {
          return y.rangeBand();
        })
        .attr("width", function (d) {
          return (x(d.endDate) - x(d.startDate));
        })
        .on('click', function (d, i) {
          window.location = "workflow.html?id=" + d.attempt;
        })
        .on('mouseover', tip.show)
        .on('mouseout', tip.hide);

    svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0, " + (height - margin.top - margin.bottom) + ")")
        .transition()
        .call(xAxis);

    svg.append("g").attr("class", "y axis").transition().call(yAxis);


    svg.call(tip);


    return gantt;

  }

  gantt.margin = function (value) {
    if (!arguments.length)
      return margin;
    margin = value;
    return gantt;
  };

  gantt.timeDomain = function (value) {
    if (!arguments.length)
      return [timeDomainStart, timeDomainEnd];
    timeDomainStart = +value[0], timeDomainEnd = +value[1];
    return gantt;
  };

  gantt.tickFormat = function (value) {
    if (!arguments.length)
      return tickFormat;
    tickFormat = value;
    return gantt;
  };


  return gantt;
};