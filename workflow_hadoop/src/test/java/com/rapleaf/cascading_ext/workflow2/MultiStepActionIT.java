package com.rapleaf.cascading_ext.workflow2;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;


import com.liveramp.workflow2.workflow_hadoop.HadoopMultiStepAction;
import com.liveramp.workflow_core.runner.BaseAction;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

public class MultiStepActionIT extends WorkflowTestCase {
  private Step a;
  private Step b;
  private Step g;
  private Step d;
  private Step f;
  
  private HadoopMultiStepAction msa;

  public final class NullAction2 extends Action {
    public NullAction2(String checkpoint, String tmpRoot) {
      super(checkpoint, tmpRoot);
    }

    @Override
    public void execute() {}
  }

  @Before
  public void prepare() throws Exception {

    msa = new HadoopMultiStepAction("msa", getTestRoot());

    b = new Step(new NullAction2("b", msa.getTmpRoot()));
    a = new Step(new NullAction2("a", msa.getTmpRoot()));
    Step c = new Step(new NullAction2("c", msa.getTmpRoot()), a);
    d = new Step(new NullAction2("d", msa.getTmpRoot()), a, b);
    Step e = new Step(new NullAction2("e", msa.getTmpRoot()), b);
    f = new Step(new NullAction2("f", msa.getTmpRoot()), c, e);
    g = new Step(new NullAction2("g", msa.getTmpRoot()));

    msa.setSubSteps(Arrays.asList(a, b, c, d, e, f, g));
  }

  @Test
  public void testGetHeadSteps() throws Exception {

    //  assert that the tmp root is set
    assertEquals(getTestRoot()+"/msa-tmp-stores", msa.getTmpRoot());
    BaseAction action = a.getAction();
    assertEquals(getTestRoot()+"/msa-tmp-stores/a-tmp-stores", ((Action)action).getTmpRoot());

    assertEquals(new HashSet<Step>(Arrays.asList(a, b, g)), msa.getHeadSteps());
  }

  @Test
  public void testGetTailSteps() throws Exception {
    assertEquals(new HashSet<Step>(Arrays.asList(d, f, g)), msa.getTailSteps());
  }

  @Test
  public void testNoDuplicateTokens() throws Exception {
    try {
      new HadoopMultiStepAction("blah", getTestRoot(), Arrays.asList(a, a));
      fail();
    } catch (Exception e) {
      // cool!
    }
  }
}
