//shared helper methods


var ATTEMPT_WINDOW = 2 * 60 * 60 * 1000; // couple hours
var STOPPED_WINDOW = 24 * 60 * 60 * 1000; // day
var RUNNING_WINDOW = 30 * 24 * 60 * 60 * 1000; // month

function getPrettyDate(millis) {
  if (millis == 0) {
    return "N/A";
  } else {
    return moment(new Date(millis)).format('MM/D/YYYY h:mm:ss a');
  }
}

function clearObj(object) {
  for (var variableKey in object) {
    if (object.hasOwnProperty(variableKey)) {
      delete object[variableKey];
    }
  }
}

var formatTime = function (d) {

  var years = Math.floor(d / 31536000000);
  var rem0 = d - (31536000000 * years);
  var days = Math.floor(rem0 / 86400000);
  var rem = rem0 - (86400000 * days);
  var hours = Math.floor(rem / 3600000);
  var rem2 = rem - (3600000 * hours);
  var mins = Math.floor(rem2 / 60000);
  var rem3 = rem2 - (60000 * mins);
  var secs = Math.floor(rem3 / 1000);

  var res = [];
  if (years) {
    res.push(years + 'yr');
  }
  if (days) {
    res.push(days + 'd');
  }
  if (hours) {
    res.push(hours + 'hr');
  }
  if (mins) {
    res.push(mins + 'min');
  }
  if (secs) {
    res.push(secs + 's');
  }

  return res.slice(0, 2).join(' ');
};

function getDaysName(durationMillis) {
  return getDAys(durationMillis) + " days"
}

function getDays(durationMillis) {
  return Math.floor(durationMillis / 86400000);
}


function getElapsed(start, end) {
  if (start == 0 || end == 0) {
    return "N/A";
  }

  return getDuration(end - start);
}


function getDuration(durationMillis) {
  var hours = Math.floor(durationMillis / 3600000);
  var rem = durationMillis - hours * 3600000;

  var minutes = Math.floor(rem / 60000);
  rem = rem - minutes * 60000;

  var seconds = Math.floor(rem / 1000);

  var res = "";
  if (hours != 0) {
    res = res + hours + "h ";
  }

  if (minutes != 0) {
    res = res + minutes + "min ";
  }

  return res + seconds + "s";
}

function getURLParams() {
  return new URI().query(true);
}

function attemptCommon(attempt) {
  return [
    $('<td>' + attempt.host + '</td>'),
    $('<td>' + attempt.system_user + '</td>'),
    $('<td>' + truncateDir(attempt.launch_dir) + '</td>'),
    $('<td>' + truncateJar(attempt.launch_jar) + '</td>'),
    $('<td>' + commitLink(attempt) + '</td>')
  ];
}

function trimHost(hostname) {
  return hostname.split(/\./)[0];
}

function getDisplayName(name) {
  return name.split(/\./).pop();
}

function truncateDir(dir) {
  return truncateTo(dir, 52);
}

function truncateScope(scope_identifier) {
  return truncateTo(scope_identifier, 25);
}

function truncateJar(jar) {
  return truncateTo(jar, 30);
}

function truncateTo(string, length) {
  if (string.length < length) {
    return string;
  }
  return string.substring(0, length - 3) + "...";
}

function commitLink(attempt) {
  if (attempt.scm_remote) {

    //attempt.scm_remote assumed to be of the form git@X:Y/../Z/git or a http link
    var re = /(git@(\S+?):(\S+?\/\S+?)\.git)|(https?:\/\/\S+)/;
    var found = attempt.scm_remote.match(re);
    if (!found) {
      return "";
    }
    else if (found[4]) {
      return ('<a href="' + found[4] + '/commits/' + attempt.commit_revision + '">' + abbreviate(attempt.commit_revision) + '</a>');
    }
    else if (found[1]) {
      return ('<a href="https://' + found[2] + '/' + found[3] + '/commits/' + attempt.commit_revision + '">' + abbreviate(attempt.commit_revision) + '</a>');
    }
    else {
      return "";
    }
  }
  else {
    return "";
  }
}

function abbreviate(commit) {
  if (commit && commit.length > 7) {
    return commit.substring(0, 7);
  }
  return commit;
}

function getApplicationLink(execution) {
  return $('<td><a href="application.html?name=' + encodeURIComponent(execution.name) + '">' + getDisplayName(execution.name) + '</a>' + +'</td>');
}

function getScopeLink(execution) {
  return $('<td>' + getScopeHref(execution) + '</td>')
}

function getScopeHref(execution) {
  if (execution.scope_identifier != "") {
    return '<a href="application.html?name=' + encodeURIComponent(execution.name) + '&scope_identifier=' + execution.scope_identifier + '">' + truncateScope(execution.scope_identifier) + '</a>';
  } else {
    return '<a href="application.html?name=' + encodeURIComponent(execution.name) + '&scope_identifier=__NULL">NULL</a>';
  }
}

function executionFields(execution, attempt) {

  return [
    getApplicationLink(execution),
    getScopeLink(execution),
    $('<td>' + getPrettyDate(execution.start_time) + '</td>'),
    $('<td>' + getPrettyDate(execution.end_time) + '</td>'),
    $('<td>' + getTrackURL(attempt.id, getTrackURL(attempt.id, displayStatus(execution, attempt))) + '</td>')
  ];
}

function attemptDetails(attempt) {
  return [
    $('<td>' + getPrettyDate(attempt.start_time) + '</td>'),
    $('<td>' + getPrettyDate(attempt.end_time) + '</td>'),
    $('<td>' + getTrackURL(attempt.id, attemptDisplayStatus(attempt)) + '</td>')
  ];
}

function getTrackURL(attemptId, status) {
  return '<a href="workflow.html?id=' + attemptId + '">' + status + '</a>'
}

function attemptDisplayStatus(attempt) {
  if (attempt.process_status === "TIMED_OUT") {
    return "DIED_UNCLEAN";
  }

  return attempt.status;
}

function displayStatus(execution, latestAttempt) {

  if (execution.status == "COMPLETE") {
    return "COMPLETE";
  } else if (execution.status == "CANCELLED") {
    return "CANCELLED";
  } else {
    return attemptDisplayStatus(latestAttempt);
  }
}

function trimClassName(name) {
  var n = name.lastIndexOf(".") + 1;
  return name.substring(n);
}

function getHours(millis) {
  return Math.floor(millis / 3600000);
}

function getCostInTermsOfUsage(workflowTime, totalClusterTime, timeFrame) {
  return Math.round(workflowTime * 173.90 * timeFrame / totalClusterTime);
}

function getExecutionLink(execution, numAttempts) {
  return $('<td><a href="execution.html?id=' + execution.id + '">' + execution.id + ' (' + numAttempts + ')</a></td>');
}

function populateTaskHeader(columns, list) {
  list.empty();

  var header = $('<tr></tr>');
  columns.forEach(function (c) {
    header.append($('<th>' + c + '</th>'));
  });
  list.append(header);
}

function populateException2Hosts(data, list) {
  list.empty();

  var sorted = _.sortBy(data, function (e) {
    return e.exception;
  });

  sorted.forEach(function (e) {
    var row = $('<tr class="task_row"></tr>');
    row.append($('<td>' + e.exception + '</td>'));
    row.append($('<td>' + e.hosts.join("<br />") + '</td>'));
    list.append(row);
  });

}

function populateAppToAggregateExceptions(data, list){
  list.empty();

  var sorted = _.sortBy(data, function (e) {
    return -e.count;
  });

  sorted.forEach(function (e) {
    var row = $('<tr class="host_row"></tr>');
    row.append($('<td>' + e.app + '</td>'));
    row.append($('<td>' + e.count + '</td>'));
    list.append(row);
  })
}


function populateHostToAggregateExceptions(data, list) {
  list.empty();

  var sorted = _.sortBy(data, function (e) {
    return -e.exceptions_count;
  });

  sorted.forEach(function (e) {
    var row = $('<tr class="host_row"></tr>');
    row.append($('<td>' + e.host + '</td>'));
    row.append($('<td>' + e.exceptions_count + '</td>'));
    list.append(row);
  })

}


function populateHost2Exceptions(data, list) {
  list.empty();

  var sorted = _.sortBy(data, function (e) {
    return e.host;
  });

  sorted.forEach(function (e) {
    var row = $('<tr class="host_row"></tr>');
    row.append($('<td>' + e.host + '</td>'));
    row.append($('<td>' + e.exceptions.join("") + '</td>'));
    list.append(row);
  });
}

function populateTaskExceptions(data, list) {
  list.empty();

  var sorted = _.sortBy(data, function (e) {
    return e.mapreduce_job_id;
  });

  sorted.reverse().forEach(function (e) {
    var row = $('<tr class="task_row"></tr>');
    row.append($('<td>' + getPrettyDate(e.time) + '</td>'));
    row.append($('<td>' + e.exception + '</td>'));
    row.append($('<td>' + e.host_url + '</td>'));
    row.append($('<td>' + getTrackURL(e.workflow_attempt_id, e.workflow_attempt_id) + '</td>'));
    row.append($('<td><a href="' + e.tracking_url + '">TRACK</a></td>'));
    row.append($('<td>' + e.job_name + '</td>'));
    row.append($('<td>' + e.step_token + '</td>'));

    list.append(row);
  });
}

function populateExecutions(data, list) {
  list.empty();

  var sorted = _.sortBy(data, function (e) {
    return e.attempts[0].id;
  });

  var unique = _.uniq(sorted, true, function (e) {
    return e.attempts[0].id;
  });

  unique.reverse().forEach(function (e) {

    var execution = e.execution;
    var latestAttempt = e.attempts[0];

    var row = $('<tr class="workflow_row"></tr>').addClass(displayStatus(execution, latestAttempt));

    executionFields(execution, latestAttempt).forEach(function (e) {
      row.append(e);
    });

    attemptCommon(latestAttempt).forEach(function (e) {
      row.append(e);
    });

    row.append(getExecutionLink(execution, e.attempts.length));

    list.append(row);

  });

}

function populateNotifications(notifications, table, idData, callback) {
  var emails = {};
  notifications.forEach(function (e) {
    if (!emails[e.email]) {
      emails[e.email] = [];
    }
    emails[e.email].push(e.workflow_runner_notification);

  });

  Object.keys(emails).forEach(function (e) {

    var button = $("<button></button>")
      .attr('type', 'button')
      .addClass('btn btn-danger')
      .text('Remove');

    button.click(function (event) {
      event.preventDefault();

      $.ajax({
        type: 'POST',
        dataType: 'html',
        url: "notification_configuration",
        data: _.extend({email: e, command: 'remove'}, idData),
        success: function () {
          callback();
        }
      });

    });

    table.append($("<tr></tr>")
      .append($("<td></td>").text(e))
      .append($("<td></td>").text(emails[e].sort().join(" ")))
      .append(button));

  });
}

function cancelButton() {
  return {
    label: 'Cancel',
    cssClass: 'btn',
    hotkey: 27,
    action: function (dialog) {
      dialog.close();
    }
  }
}

function submitButton(action) {
  return {
    label: 'Continue',
    cssClass: 'btn-primary',
    action: action
  }
}

function configureAddNotification(button, idData, callback) {
  button.unbind();
  button.click(function (e) {
    e.preventDefault();

    BootstrapDialog.show({
      title: 'Notification Email',
      message: $('<textarea class="form-control" id="email-to-notify" placeholder="Notification email"></textarea>'),
      buttons: [
        cancelButton(),
        submitButton(function (dialog) {
            var email = $("#email-to-notify").val();
            dialog.close();

            if (email) {
              $.ajax({
                type: 'GET',
                dataType: 'html',
                url: "available_notifications",
                success: function (data) {
                  var options = $("<form></form>");
                  JSON.parse(data).values.sort().forEach(function (e) {
                    options.append($('<input>')
                      .addClass('notification-type')
                      .attr('type', 'checkbox')
                      .attr('name', 'notifications')
                      .attr('value', e))
                      .append(" " + e)
                      .append("<br>")
                  });

                  var all = $('<input>')
                    .attr('type', 'checkbox')
                    .attr('name', 'notifications')
                    .attr('value', 'ALL');

                  all.click(function (e) {

                    var box = $(this);
                    if (box.is(':checked')) {
                      $('.notification-type')
                        .attr('disabled', true)
                        .prop('checked', true);
                    } else {
                      $('.notification-type').removeAttr('disabled');
                    }

                  });

                  options
                    .append('<br>')
                    .append(all)
                    .append(" ALL")
                    .append('<br>');

                  BootstrapDialog.show({
                    title: 'Notifications',
                    message: options,
                    buttons: [
                      cancelButton(),
                      {
                        label: 'Add',
                        cssClass: 'btn-primary',
                        hotkey: 13,
                        action: function (dialog) {

                          var selected = [];
                          $(".notification-type").each(function () {
                            var box = $(this);
                            if (box.is(':checked')) {
                              selected.push(box.attr('value'));
                            }
                          });

                          dialog.close();

                          $.ajax({
                            traditional: true,
                            type: 'POST',
                            dataType: 'html',
                            url: "notification_configuration",
                            data: _.extend({email: email, command: 'add', notification: selected}, idData),
                            success: function () {
                              callback();
                            }
                          });

                        }
                      }
                    ]
                  });

                }
              })
            }
          }
        )
      ]
    });

  })
}

function configureNavbar(intoElement) {
  $.ajaxPrefilter(function (options, originalOptions, jqXHR) {
    jqXHR.setRequestHeader('X-XSRF-Token', $.cookie("XSRF-TOKEN"));
  });


  $.when(
    function () {
      return intoElement.load("navbar.html")
    }(),
    function () {
      return $.ajax({
        type: 'GET',
        dataType: 'html',
        url: 'user'
      })
    }()
  ).done(function (load, user) {

    var userInfo = JSON.parse(user[0]);
    $("#user-menu").text(userInfo.username);

    $("#logout").click(function (event) {
      event.preventDefault();
      $.ajax({
        type: 'POST',
        dataType: 'html',
        url: "logout",
        success: function () {
          location.reload()
        }
      });
    })

  })

}

function getDashURL(name) {
  return $("<a></a>").attr('href', 'dashboard.html?name=' + encodeURIComponent(name)).text(name);
}
