package com.rapleaf.cascading_ext.workflow2.state;

import java.io.IOException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.liveramp.workflow.state.WorkflowDbPersistenceFactory;
import org.junit.Test;

import com.liveramp.java_support.functional.Fn;
import com.rapleaf.cascading_ext.workflow2.Action;
import com.rapleaf.cascading_ext.workflow2.Step;
import com.rapleaf.cascading_ext.workflow2.WorkflowRunner;
import com.rapleaf.cascading_ext.workflow2.WorkflowTestCase;
import com.rapleaf.cascading_ext.workflow2.options.HadoopWorkflowOptions;
import com.liveramp.workflow.test.FailingPersistenceFactory;
import com.liveramp.workflow.test.StepNameBuilder;
import com.liveramp.workflow2.workflow_hadoop.HadoopMultiStepAction;

import static com.liveramp.commons.test.TestUtils.assertCollectionEquivalent;
import static org.junit.Assert.assertTrue;

public class FailingPersistenceIT extends WorkflowTestCase {

  private static final String WORKFLOW_CHKPT_TOKEN = "test-workflow";
  private static final String SHOULD_FAIL_CHKPT_TOKEN = "should-fail-but-complete";
  private static final String NOT_REACHED_CHKPT_TOKEN = "should-not-be-reached";
  private static final String SHOULDN_NOT_FAIL_MULTISTEP_CHKPT_TOKEN = "should-not-fail-multi-step";
  private static final Set<StepNameBuilder> STEPS_THAT_SHOULD_COMPLETE = Sets.newHashSet(
      new StepNameBuilder(WORKFLOW_CHKPT_TOKEN, SHOULDN_NOT_FAIL_MULTISTEP_CHKPT_TOKEN, SHOULD_FAIL_CHKPT_TOKEN),
      new StepNameBuilder(WORKFLOW_CHKPT_TOKEN, SHOULD_FAIL_CHKPT_TOKEN)
  );

  @Test
  public void testFailingPersistence() throws IOException {
    Set<String> stepsThatCompleted = Sets.newHashSet();

    boolean failedIntentionally = false;
    try {
      new WorkflowRunner(
          TestWorkflow.class,
          new FailingPersistenceFactory(new WorkflowDbPersistenceFactory(), new StepNameBuilder(WORKFLOW_CHKPT_TOKEN, SHOULD_FAIL_CHKPT_TOKEN)),
          HadoopWorkflowOptions.test(),
          new Step(new TestWorkflow(
              WORKFLOW_CHKPT_TOKEN,
              getTestRoot(),
              stepsThatCompleted
          ))
      ).run();
    } catch (RuntimeException e) {
      failedIntentionally = e.getMessage().contains(FailingPersistenceFactory.IntentionallyFailedStepException.class.getName());
    }
    assertTrue(failedIntentionally);

    assertCollectionEquivalent(
        STEPS_THAT_SHOULD_COMPLETE.stream().map(StepNameBuilder::getCompositeStepName).collect(Collectors.toSet()), stepsThatCompleted);
  }

  private static class TestWorkflow extends HadoopMultiStepAction {

    public TestWorkflow(String checkpointToken, String tmpRoot, Set<String> stepsThatSucceeded) {
      super(checkpointToken, tmpRoot);

      Step shouldNotFailMultiStep = new Step(new ShouldNotFailMultiStep(
          checkpointToken,
          SHOULDN_NOT_FAIL_MULTISTEP_CHKPT_TOKEN,
          tmpRoot,
          stepsThatSucceeded
      ));

      Step shouldFail = new Step(new TestStep(
          checkpointToken,
          SHOULD_FAIL_CHKPT_TOKEN,
          stepsThatSucceeded
      ),
           shouldNotFailMultiStep);

      Step shouldNotFail = new Step(new TestStep(
          checkpointToken,
          NOT_REACHED_CHKPT_TOKEN,
          stepsThatSucceeded
      ), shouldFail);

      setSubStepsFromTail(shouldNotFail);
    }
  }

  private static class ShouldNotFailMultiStep extends HadoopMultiStepAction {

    public ShouldNotFailMultiStep(String superStepsCheckPointTokens, String checkpointToken, String tmpRoot, Set<String> stepsThatSucceeded) {
      super(checkpointToken, tmpRoot);

      Step shouldNotFailButHasSameNameAsStepThatFails = new Step(new TestStep(
          new StepNameBuilder(superStepsCheckPointTokens, checkpointToken).getCompositeStepName(),
          SHOULD_FAIL_CHKPT_TOKEN,
          stepsThatSucceeded
      ));

      setSubStepsFromTail(shouldNotFailButHasSameNameAsStepThatFails);
    }
  }

  private static class TestStep extends Action {

    private final String superStepsCheckPointTokens;
    private final String checkpointToken;
    private final Set<String> stepsThatSucceeded;

    public TestStep(String superStepsCheckPointTokens, String checkpointToken, Set<String> stepsThatSucceeded) {
      super(checkpointToken);
      this.superStepsCheckPointTokens = superStepsCheckPointTokens;
      this.checkpointToken = checkpointToken;
      this.stepsThatSucceeded = stepsThatSucceeded;
    }

    @Override
    protected void execute() throws Exception {
      stepsThatSucceeded.add(new StepNameBuilder(superStepsCheckPointTokens, checkpointToken).getCompositeStepName());
    }
  }
}
