var durationNames = {
  "org.apache.hadoop.mapreduce.JobCounter.SLOTS_MILLIS_MAPS": true,
  "org.apache.hadoop.mapreduce.JobCounter.SLOTS_MILLIS_REDUCES": true,
  "org.apache.hadoop.mapreduce.JobCounter.VCORES_MILLIS_MAPS": true,
  "org.apache.hadoop.mapreduce.JobCounter.VCORES_MILLIS_REDUCES": true,
  "org.apache.hadoop.mapreduce.JobCounter.MB_MILLIS_MAPS": true,
  "org.apache.hadoop.mapreduce.JobCounter.MB_MILLIS_REDUCES": true,
  "Derived.TOTAL_VCORES_MILLIS": true
};


function createTable() {
  return {
    executions: ['x_executions'],
    days: ['x_days'],
    execution_values: ['Per Execution'],
    day_values: ['Per Day'],
    ids: ['Ids']
  }
}

function makeRedirect(points) {
  return function (e) {
    if (e.name === 'Per Execution') {
      window.location.replace("execution.html?id=" + points.ids[e.index+1]);
    }
  };
}

function moreThanDaily(dates, runs, frequency_cutoff) {
  if (dates) {
    return ((runs/dates) > frequency_cutoff);
  }
  else {
    return false;
  }
}

function dayOfDateStr(time) {
  var date = parseInt(time);
  date = new Date(date);
  date.setHours(0,0,0,0);
  return date;
}


function renderTrendTab(expanded_chart_holder, collapsed_chart_holder, counter_data, execution_data, daily_data, render_info) {

  clearObj(counter_data);

  var duration = {
    times: ['x'],
    values: ['Duration'],
    ids: ['Ids']
  };

  var count = {};
  var duration_sums = {};
  var num_runs = 0;
  var num_dates = 0;

  execution_data.forEach(function (e) {

    var time = e.start_time;
    var dur = e.duration;

    duration.times.push(time);
    duration.values.push(dur);
    duration.ids.push(e.execution_id);

    var date = dayOfDateStr(time);
    if (count[date]) {
      count[date] += 1;
      num_runs += 1;
    }
    else {
      count[date] = 1;
      num_runs += 1;
      num_dates += 1;
    }
    duration_sums[date] = (duration_sums[date] || 0) + dur;

    e.counters.forEach(function (c) {
      var group = c.group;

      c.names.forEach(function (n) {
        var name = n.name;
        var value = n.value;

        var combined = group + '.' + name;

        if (!counter_data[combined]) {
          counter_data[combined] = createTable();
        }

        var cell = counter_data[combined];
        cell.executions.push(time);
        cell.execution_values.push(value);
        cell.ids.push(e.execution_id);

      });
    })
  });

  daily_data.forEach(function (e) {
    var day = e.day;

    e.counters.forEach(function (c) {
      var group = c.group;

      c.names.forEach(function (n) {
        var name = n.name;
        var value = n.value;

        var combined = group + '.' + name;

        if (!counter_data[combined]) {
          counter_data[combined] = createTable();
        }

        var cell = counter_data[combined];
        cell.days.push(day);
        cell.day_values.push(value);

      });
    })

  });

  expanded_chart_holder.empty();
  collapsed_chart_holder.empty();

  expanded_chart_holder
      .append('<h4>Duration</h4>')
      .append($('<div id="duration-graph"></div>'));


  if (moreThanDaily(num_dates, num_runs, 2)) {
    var countkeys = ['x'];
    var counts = ['times_run'];
    var duration_col = ['average_duration'];
    Object.keys(count).forEach( function(e) {
      counts.push(count[e]);
      countkeys.push(new Date(e));
      duration_col.push(duration_sums[e]/count[e]);
    });

    c3.generate({
      bindto: '#duration-graph',
      data: {
        x: 'x',
        columns: [
          countkeys,
          counts,
          duration_col
        ],
        axes: {
          times_run: 'y',
          average_duration: 'y2'
        }
      },
      transition: {
        duration: null
      },
      axis: {
        x: {
          type: 'timeseries',
          tick: {
            format: '%Y-%m-%d'
          }
        },
        y2: {
          tick: {
            format: formatTime
          },
          show: true
        }
      }
    });
  }
  else {
    c3.generate({
      bindto: '#duration-graph',
      data: {
        x: 'x',
        columns: [
          duration.times,
          duration.values
        ],
        onclick: function (e) {
          window.location.replace("execution.html?id=" + duration.ids[e.index+1]);
        }
      },
      transition: {
        duration: null
      },
      axis: {
        x: {
          type: 'timeseries',
          tick: {
            format: '%Y-%m-%d'
          }
        },
        y: {
          tick: {
            format: formatTime
          }
        }
      }
    });
  }


  var index = 0;
  for (var value in counter_data) {
    var points = counter_data[value];
    index = index + 1;

    var divName = "graph-" + index;
    var chartDiv = $('<div></div>');
    var chartChild = $('<div></div>');
    var label = $('<h4 class="trend-chart">' + value + '</h4>');

    var displayFunc = buildChild(name, chartChild, divName, points);
    label.click(displayFunc);

    chartDiv
        .append(label)
        .append(chartChild.attr('id', divName));

    if (render_info[value]) {
      expanded_chart_holder.append(chartDiv);
      displayFunc();
    } else {
      collapsed_chart_holder.append(chartDiv)
    }

  }
}

function getFormat(value) {
  if (durationNames[value]) {
    return formatTime;
  } else {
    return d3.format('e');
  }
}

function buildChild(name, child, divName, points) {
  return function () {

    if (child.children().length == 0) {
      var chart = c3.generate({
        bindto: '#' + divName,
        data: {
          xs: {
            'Per Day': 'x_days',
            'Per Execution': 'x_executions'
          },
          columns: [
            points.days,
            points.executions,
            points.day_values,
            points.execution_values
          ],
          onclick: makeRedirect(points)
        },
        transition: {
          duration: null
        },
        axis: {
          x: {
            type: 'timeseries',
            tick: {
              format: '%Y-%m-%d'
            }
          },
          y: {
            tick: {
              format: getFormat(name)
            },
            min: 0
          }
        }
      });

      chart.hide([points.executions[0], points.execution_values[0]]);
    } else {
      child.slideToggle(100);
    }

  }
}