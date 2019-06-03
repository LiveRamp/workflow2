package com.liveramp.workflow_core.runner;

import java.util.Set;

public interface ExecutionNode<Config> {
  Set<? extends ExecutionNode<Config>> getDependencies();
}
