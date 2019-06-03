function renderAlertsTab(execution_data, $table, description_data, $description_table) {
  var triggeredAlerts = {};
  execution_data.forEach(function (e) {
    var row = $('<tr class="alerts_row"></tr>');

    var stepNameCell = $('<td></td>');
    stepNameCell.append($('<a href="' + window.location.href.split('#')[0] + '&step_id=' + e.stepName + '">' + e.stepName + '</a>'));
    row.append(stepNameCell);

    var alertNameCell = $('<td></td>');
    alertNameCell.append(e.alertName);
    row.append(alertNameCell);
    triggeredAlerts[e.alertName] = description_data[e.alertName];

    var alertCountCell = $('<td></td>');
    alertCountCell.append(e.alertCount);
    row.append(alertCountCell);

    var alertsPerJobCell = $('<td></td>');
    alertsPerJobCell.append(e.alertsPerJob);
    row.append(alertsPerJobCell);

    $table.append(row);
  });

  $table.trigger("update");

  for (var alert in triggeredAlerts) {
    var descriptionRow = $('<tr class="alert_descriptions_row"></tr>');

    var alertCell = $('<td></td>').append(alert);
    var descriptionCell = $('<td></td>').append(description_data[alert]);

    descriptionRow.append(alertCell);
    descriptionRow.append(descriptionCell);
    $description_table.append(descriptionRow);
  }
  $description_table.trigger("update");
}
