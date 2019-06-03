package com.rapleaf.cascading_ext.workflow2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.flow.FlowDef;
import cascading.flow.FlowListener;
import cascading.pipe.Pipe;
import cascading.tap.Tap;

public class FlowBuilder {

  private final FlowConnector connector;
  private final String userClassName;

  protected FlowBuilder(FlowConnector connector, Class userCodeClass) {
    this.connector = connector;
    this.userClassName = userCodeClass.getSimpleName();
  }

  //  these methods wrap those found in FlowConnector

  public FlowClosure connect(Tap source, Tap sink, Pipe tail) {
    return connect(userClassName, source, sink, tail);
  }

  public FlowClosure connect(String name, Tap source, Tap sink, Pipe tail) {
    Map<String, Tap> sources = new HashMap<String, Tap>();
    sources.put(tail.getHeads()[0].getName(), source);
    return connect(name, sources, sink, tail);
  }

  public FlowClosure connect(String name, Tap source, Tap sink, Tap trap, Pipe tail) {
    Map<String, Tap> sources = new HashMap<String, Tap>();
    sources.put(tail.getHeads()[0].getName(), source);
    Map<String, Tap> traps = new HashMap<String, Tap>();
    traps.put(tail.getHeads()[0].getName(), trap);
    return connect(name, sources, sink, traps, tail);
  }

  public FlowClosure connect(Map<String, Tap> sources, Tap sink, Pipe tail) {
    return connect(userClassName, sources, sink, tail);
  }

  public FlowClosure connect(String name, Map<String, Tap> sources, Tap sink, Pipe tail) {
    Map<String, Tap> sinks = new HashMap<String, Tap>();
    sinks.put(tail.getName(), sink);
    return connect(name, sources, sinks, tail);
  }

  public FlowClosure connect(String name, Map<String, Tap> sources, Tap sink, Map<String, Tap> traps, Pipe tail) {
    Map<String, Tap> sinks = new HashMap<String, Tap>();
    sinks.put(tail.getName(), sink);
    return connect(name, sources, sinks, traps, tail);
  }

  public FlowClosure connect(String name, Tap source, Tap sink, Map<String, Tap> traps, Pipe tail) {
    Map<String, Tap> sources = new HashMap<String, Tap>();
    sources.put(tail.getHeads()[0].getName(), source);
    Map<String, Tap> sinks = new HashMap<String, Tap>();
    sinks.put(tail.getName(), sink);
    return connect(name, sources, sinks, traps, tail);
  }

  public FlowClosure connect(Tap source, Map<String, Tap> sinks, Collection<Pipe> tails) {
    return connect(userClassName, source, sinks, tails.toArray(new Pipe[tails.size()]));
  }

  public FlowClosure connect(String name, Tap source, Map<String, Tap> sinks, Collection<Pipe> tails) {
    return connect(name, source, sinks, tails.toArray(new Pipe[tails.size()]));
  }

  public FlowClosure connect(Tap source, Map<String, Tap> sinks, Pipe... tails) {
    return connect(userClassName, source, sinks, tails);
  }

  public FlowClosure connect(String name, Tap source, Map<String, Tap> sinks, Pipe... tails) {
    Set<Pipe> heads = new HashSet<Pipe>();

    for (Pipe pipe : tails) {
      Collections.addAll(heads, pipe.getHeads());
    }

    if (heads.isEmpty()) {
      throw new IllegalArgumentException("no pipe instance found");
    }

    if (heads.size() != 1) {
      throw new IllegalArgumentException("there may be only 1 head pipe instance, found " + heads.size());
    }

    Map<String, Tap> sources = new HashMap<String, Tap>();

    for (Pipe pipe : heads) {
      sources.put(pipe.getName(), source);
    }

    return connect(name, sources, sinks, tails);
  }

  public FlowClosure connect(Map<String, Tap> sources, Map<String, Tap> sinks, Pipe... tails) {
    return connect(userClassName, sources, sinks, tails);
  }

  public FlowClosure connect(String name, Map<String, Tap> sources, Map<String, Tap> sinks, Pipe... tails) {
    return connect(name, sources, sinks, new HashMap<String, Tap>(), tails);
  }

  public FlowClosure connect(String name, Map<String, Tap> sources, Map<String, Tap> sinks, Map<String, Tap> traps, Pipe... tails) {
    return new FlowClosure(name, sources, sinks, traps, tails);
  }

  public FlowClosure connect(FlowDef def) {
    return new FlowClosure(def.getName(), def.getSources(), def.getSinks(), def.getTraps(), def.getTailsArray());
  }

  protected interface IFlowClosure {
    public Flow buildFlow();
  }

  public class FlowClosure implements IFlowClosure {

    private final String name;
    private final Map<String, Tap> sources;
    private final Map<String, Tap> sinks;
    private final Map<String, Tap> traps;
    private final Pipe[] tails;
    private List<FlowListener> listeners = Lists.newArrayList();

    private FlowClosure(String name, Map<String, Tap> sources, Map<String, Tap> sinks, Map<String, Tap> traps, Pipe[] tails) {
      this.name = name;
      this.sources = sources;
      this.sinks = sinks;
      this.traps = traps;
      this.tails = tails;
    }

    public FlowClosure addListener(FlowListener flowListener) {
      listeners.add(flowListener);
      return this;
    }

    public Flow buildFlow() {
      Flow connect = connector.connect(name, sources, sinks, traps, tails);
      for (FlowListener listener : listeners) {
        connect.addListener(listener);
      }
      return connect;
    }

  }

}
