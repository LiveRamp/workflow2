function renderCostTab(execution_data, $table, graphHolder){
  var totalCost = 0;

  $table.empty();

  var costs = {
    times: ['x'],
    values: ['Cost']
  };

  execution_data.forEach(function (e) {

    var row = $('<tr class="cost_row"></tr>');

    var executionId = e.execution_id;
    var cost = e.estimated_cost;

    var cell = $('<td></td>');

    var link = $('<a href=' + 'execution.html?id=' + executionId + '/></a>');
    link.append(executionId);

    cell.append(link);
    row.append(cell);

    var startCell = $('<td></td>');
    startCell.append(getPrettyDate(e.start_time));
    row.append(startCell);

    var durationCell = $('<td></td>');
    durationCell.append(getDuration(e.duration));
    row.append(durationCell);

    var costCell = $('<td></td>');
    totalCost += cost;
    costCell.append("$" + cost.toFixed(3));
    row.append(costCell);

    $table.append(row);

    costs.times.push(e.start_time);
    costs.values.push(cost);

  });

  $table.trigger("update");

  $(graphHolder).empty();

  c3.generate({
    bindto: graphHolder,
    data: {
      x: 'x',
      columns: [
        costs.times,
        costs.values
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