package com.liveramp.workflow.test;

import com.google.common.base.Joiner;

public class StepNameBuilder {

  private final String outerStep;
  private final String[] innerSteps;

  public StepNameBuilder(String outerStep, String... innerSteps) {
    this.outerStep = outerStep;
    this.innerSteps = innerSteps;
  }

  public String getCompositeStepName() {
    return outerStep + "__" + Joiner.on("__").join(innerSteps);
  }
}
