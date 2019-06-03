package com.rapleaf.cascading_ext.workflow2.util;

import com.liveramp.java_support.functional.Fn;
import com.liveramp.workflow_core.runner.BaseAction;

public interface IdActionFactory<CONTEXT> extends Fn<CONTEXT, BaseAction> {
}
