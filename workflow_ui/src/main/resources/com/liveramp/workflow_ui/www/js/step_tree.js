function renderStepTree($expanded_step_body, $collapsed_step_body, step_data, render_info) {

  var countersForSteps = {};
  step_data.forEach(function (e) {
    var step = e.step;
    e.counters.forEach(function (e) {
      var group = e.group;
      e.names.forEach(function (e) {
        var name = e.name;
        var value = e.value;
        var counter = group + "." + name;
        if (!countersForSteps[counter]) {
          countersForSteps[counter] = [];
        }
        countersForSteps[counter].push({
          step_token: step,
          value: value
        });
      });
    });
  });

  $expanded_step_body.empty();
  $collapsed_step_body.empty();

  if (countersForSteps["Duration.Total"]) {
    renderTreeGraph($expanded_step_body, $collapsed_step_body, countersForSteps, "Duration.Total", 0, render_info);
  }

  Object.keys(countersForSteps).sort().forEach(function (key, count) {
    if (key !== "Duration.Total") {
      renderTreeGraph($expanded_step_body, $collapsed_step_body, countersForSteps, key, count + 1, render_info);
    }
  });
}


function renderTreeGraph($expanded_body, $collapsed_body, countersForSteps, key, count, render_info) {

  var w = 1600,
      h = 800,
      x = d3.scale.linear().range([0, w]),
      y = d3.scale.linear().range([0, h]);

  var dataParsed = asTree(countersForSteps[key], function (e) {
    return e.value;
  });

  var id = "tree-graph-" + count;

  var treeDiv = $('<div></div>');
  var label = $('<h4 class="trend-chart">' + key + '</h4>');

  var treeContainer = $('<div id="' + id + '"></div>');


  var build = buildTree(treeContainer, w, h, dataParsed, x, y);
  label.click(build);

  var treeBodyDiv = treeDiv
      .append(label)
      .append(treeContainer);

  if (render_info[key]) {
    $expanded_body.append(treeBodyDiv);
    build();
  } else {
    $collapsed_body.append(treeBodyDiv);
  }

}

function buildTree(child, w, h, dataParsed, x, y) {
  return function () {

    if (child.children().length == 0) {
      renderGraph("#" + child.attr('id'), w, h, dataParsed, x, y, params.name);
    } else {
      child.slideToggle(100);
    }

  }
}


function renderGraph(selector, w, h, tree, x, y, wfName) {

  var totalSize = tree.size;

  var vis = d3.select(selector).append("div")
      .attr("class", "chart")
      .style("width", w + "px")
      .style("height", h + "px")
      .append("svg:svg")
      .attr("width", w)
      .attr("height", h);

  var partition = d3.layout.partition()
      .value(function (d) {
        return d.size;
      });

  var root = tree;

  var g = vis.selectAll("g")
      .data(partition.nodes(root))
      .enter().append("svg:g")
      .attr("transform", function (d) {
        return "translate(" + x(d.y) + "," + y(d.x) + ")";
      })
      .on("click", click);

  var kx = w / root.dx,
      ky = h;

  var opacity = function (d) {
    return d.dx * ky > 30 ? 1 : 0;
  };

  g.append("svg:rect")
      .attr("width", root.dy * kx)
      .attr("height", function (d) {
        return d.dx * ky;
      })
      .attr("class", function (d) {
        return d.children ? "parent tree-node" : "child tree-node";
      });

  g.append("svg:foreignObject")
      .attr("transform", function (d) {
        return transform(d, ky);
      })
      .attr("dy", "0.35em")
      .attr("width", (root.dy * kx) * .9)
      .attr("height", 30)
      .append("xhtml:body")
      .attr("class", function (d) {
        return d.children ? "parent step-label" : "child step-label";
      })
      .style("opacity", opacity)
      .html(function (d) {

        var name = truncate(d.name);
        if (!d.children) {
          name = '<a class="tree-link" href="application.html?name=' + wfName + '&step_id=' + d.fullName + '#trends">' + name + '</a>';
        }

        return "<b>" + Math.round(100 * d.size / totalSize) + "% </b>" + name;
      });

  function truncate(name) {
    if (name.length > 28) {
      return name.substring(0, 25) + "...";
    } else {
      return name;
    }
  }

  d3.select(selector)
      .on("click", function () {
        click(root);
      });

  function click(d) {
    //if (!d.children) return;


    kx = (d.y ? w - 40 : w) / (1 - d.y);
    ky = h / d.dx;
    x.domain([d.y, 1]).range([d.y ? 40 : 0, w]);
    y.domain([d.x, d.x + d.dx]);

    var t = g.transition()
        .duration(d3.event.altKey ? 7500 : 750)
        .attr("transform", function (d) {
          return "translate(" + x(d.y) + "," + y(d.x) + ")";
        });

    t.select("rect")
        .attr("width", d.dy * kx)
        .attr("height", function (d) {
          return d.dx * ky;
        });

    t.select(".step-label")
        .attr("transform", transform)
        .style("opacity", opacity);

    d3.event.stopPropagation();

  }

  function transform(d) {
    return "translate(8," + ((d.dx * ky / 2) - 8) + ")";
  }

}

function asTree(data, extractor) {

  var tree = {children: {}};

  data.forEach(function (e) {
    var token = e.step_token;
    var parts = token.split("__");
    var value = extractor(e);
    insert(parts, token, tree, value);
  });

  var collapsed = collapseChildren("Workflow", tree);

  //  skip root node if only one child
  if (collapsed.children.length == 1) {
    return collapsed.children[0];
  }

  return collapsed;
}

function collapse(name, data) {

  if (Object.keys(data.children).length == 0) {
    return {
      name: name,
      fullName: data.fullName,
      size: data.size
    }
  }

  return collapseChildren(name, data);
}

function collapseChildren(name, data) {

  var children = [];
  for (var key in data.children) {
    children.push(collapse(key, data.children[key]));
  }

  var size = 0;
  children.forEach(function (e) {
    size += e.size;
  });

  var sortedChildren =
      _.sortBy(children, function (e) {
        return -e.size;
      });

  var limited = sortedChildren.slice(0, 20);

  return {
    name: name,
    fullName: data.fullName,
    children: limited,
    size: size
  }

}

function insert(parts, fullName, data, value) {

  if (parts.length) {
    var token = parts.shift();

    if (!data.children[token]) {
      data.children[token] = {
        children: {}
      };
    }

    insert(parts, fullName, data.children[token], value);

  }
  else {
    data.fullName = fullName;
    data.size = value;
  }
}
