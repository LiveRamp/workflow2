package com.rapleaf.cascading_ext.workflow2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import com.liveramp.workflow.state.WorkflowDbPersistenceFactory;
import com.liveramp.workflow2.workflow_hadoop.HadoopMultiStepAction;
import com.rapleaf.cascading_ext.workflow2.options.HadoopWorkflowOptions;

public class Example {
  
  public static class SlightlyLessComplex extends HadoopMultiStepAction {
    
    public SlightlyLessComplex(String checkpointToken, String tmpRoot) {
      super(checkpointToken, tmpRoot, steps());
    }
    
    private static Collection<Step> steps() {
      Step s1 = new Step(new PrintAction("1"));
      Step s2 = new Step(new PrintAction("2"), s1);
      Step s3 = new Step(new PrintAction("3"), s2);
      return Arrays.asList(s1, s2, s3);
    }
    
  }
  
  public static class ComplexAction extends HadoopMultiStepAction {
    public ComplexAction(String tmpRoot) {
      super("complex", tmpRoot);

      setSubStepsFromTails(steps());
    }
    
    private Collection<Step> steps() {
      Collection<Step> steps = new ArrayList<Step>();
      Step s = new Step(new PrintAction("1"));
      steps.add(s);
      s = new Step(new PrintAction("2"), s);
      steps.add(s);
      steps.add(new Step(new PrintAction("3"), s));
      steps.add(new Step(new SlightlyLessComplex("4", getTmpRoot()), s));
      return steps;
    }
  }
  
  public static class PrintAction extends Action {
    private String message;
    
    public PrintAction(String string) {
      super(string);
      this.message = string;
    }
    
    @Override
    public void execute() {
      System.out.println(message);
    }
  }
  
  public static void main(String[] args) throws IOException {

    final String tmpRoot = "/tmp/examples/";

    Step s = new Step(new PrintAction("first"));
    new Step(new PrintAction("not dependent on second"), s);
    s = new Step(new PrintAction("second"), s);
    s = new Step(new ComplexAction(tmpRoot), s);
    s = new Step(new PrintAction("last"), s);
    
    new WorkflowRunner(
        Example.class,
        new WorkflowDbPersistenceFactory(),
        HadoopWorkflowOptions.test().setMaxConcurrentSteps(1),
        new HashSet<Step>(Arrays.asList(s))
    ).run();
  }
}
