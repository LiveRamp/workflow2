function renderAppCostHistoryTab(execution_data, graphHolder) {

  var times = ['x'];
  var values = ['Cost'];

  for (var date in execution_data) {
    var object = execution_data[date];
    times.push(JSON.parse(date));
    values.push(object.estimated_cost);
  }

  $(graphHolder).empty();

  c3.generate({
    bindto: graphHolder,
    data: {
      x: 'x',
      columns: [
        times,
        values
      ]
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
          format: function (x) {
            return "$" + x.toFixed(2);
          }
        }
      }
    }
  });


}