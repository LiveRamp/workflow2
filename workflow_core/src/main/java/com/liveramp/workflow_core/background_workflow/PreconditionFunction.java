package com.liveramp.workflow_core.background_workflow;

import java.io.Serializable;
import java.util.function.Function;

public interface PreconditionFunction<Context extends Serializable> extends Function<Context, Boolean> {}
