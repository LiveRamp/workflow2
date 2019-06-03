package com.rapleaf.cascading_ext.workflow2;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

import cascading.tuple.Fields;

import com.liveramp.workflow2.workflow_hadoop.HadoopMultiStepAction;
import com.rapleaf.cascading_ext.datastore.DataStore;
import com.rapleaf.cascading_ext.datastore.TupleDataStoreImpl;

import static com.rapleaf.cascading_ext.workflow2.test.WorkflowTestUtils.execute;

@Ignore
public class LongRunningWorkflow extends WorkflowTestCase {

  private final String LONG_RUNNING_WORKFLOW_PATH = getTestRoot() + "/LongRunningHadoopWorkflow";

  public static class ExampleAction extends TakeSomeTime {
    public ExampleAction(String token, DataStore[] inputs, DataStore[] outputs) {
      super(5, token, inputs, outputs);
    }
  }

  public static final class ExampleMultistepAction extends HadoopMultiStepAction {
    public ExampleMultistepAction(String checkpointToken, String tmpRoot, Step[] steps) {
      super(checkpointToken, tmpRoot, Arrays.asList(steps));
    }
  }

  public static class FailBang extends Action {
    protected FailBang(DataStore[] inputs, DataStore[] outputs) throws IOException {
      super("destined-to-fail");

      for (DataStore input : inputs) {
        readsFrom(input);
      }
      for (DataStore output : outputs) {
        writesTo(output);
      }
    }

    @Override
    public void execute() {
      try {
        Thread.sleep(100000000L);
      } catch (InterruptedException e) {
      }
      throw new RuntimeException("fail!");
    }
  }

  public static class TakeSomeTime extends Action {
    private int seconds;

    public TakeSomeTime(int seconds, String token, DataStore[] inputs, DataStore[] outputs) {
      super(token);
      this.seconds = seconds;

      for (DataStore input : inputs) {
        readsFrom(input);
      }
      for (DataStore output : outputs) {
        writesTo(output);
      }
    }

    @Override
    public void execute() throws IOException {
      for (int i = 0; i < seconds; i++) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        setStatusMessage("Slept " + (i + 1) + " of " + seconds + " seconds");
      }
    }
  }

  private Step getTailStep() throws IOException {
    DataStore d1 = getFakeDS("d1");
    DataStore d2 = getFakeDS("d2");
    DataStore d3 = getFakeDS("d3");
    DataStore d4 = getFakeDS("d4");
    DataStore d5 = getFakeDS("d5");
    DataStore d6 = getFakeDS("d6");
    DataStore d7 = getFakeDS("d7");
    DataStore id1 = getFakeDS("id1");
    DataStore id2 = getFakeDS("id2");
    DataStore id3 = getFakeDS("id3");
    DataStore id4 = getFakeDS("id4");

    Step s1 = new Step(new TakeSomeTime(5, "Step 1", new DataStore[0], new DataStore[0]));
    Step s2 = new Step(new ExampleAction("Step 2", new DataStore[0], new DataStore[] { d1, d2 }),
        s1);
    Step s3 = new Step(new ExampleAction("Step 3", new DataStore[0], new DataStore[] { d3 }), s1);

    Step s4_1 = new Step(new ExampleAction("sub-step 1", new DataStore[] { d1 }, new DataStore[] {
        d1, id1 }));
    Step s4_2 = new Step(new ExampleAction("sub-step 2", new DataStore[] { d2 },
        new DataStore[] { id2 }));
    Step s4_3 = new Step(new ExampleAction("sub-step 3", new DataStore[] { d1, id1, id2 },
        new DataStore[] { d4 }), s4_1, s4_2);
    Step s4 = new Step(new ExampleMultistepAction("Multistep-1", LONG_RUNNING_WORKFLOW_PATH, new Step[] { s4_1, s4_2, s4_3 }),
        s2);

    Step s5_1_1 = new Step(new ExampleAction("sub-sub-step 1", new DataStore[] { d2, d3 },
        new DataStore[] { d3 }));
    Step s5_1_2 = new Step(new TakeSomeTime(0, "sub-sub-step 2", new DataStore[] { d3 },
        new DataStore[] { id3 }), s5_1_1);
    Step s5_1 = new Step(new ExampleMultistepAction("sub-multistep", LONG_RUNNING_WORKFLOW_PATH, new Step[] { s5_1_1, s5_1_2 }));

    Step s5_2 = new Step(new ExampleAction("sub-step 2", new DataStore[] { d3 },
        new DataStore[] { id4 }), s3);
    Step s5_3 = new Step(new ExampleAction("sub-step 3", new DataStore[] { id4 },
        new DataStore[] { d6 }), s5_2);
    Step s5_4 = new Step(new ExampleAction("sub-step 4", new DataStore[] { id3, id4 },
        new DataStore[] { d5 }), s5_1, s5_2);
    Step s5 = new Step(new ExampleMultistepAction("Multistep-2", LONG_RUNNING_WORKFLOW_PATH, new Step[] { s5_1, s5_2, s5_3,
        s5_4 }), s2, s3);

    Step s6 = new Step(new FailBang(new DataStore[] { d5, d6 }, new DataStore[] { d7 }), s5);
    Step s7 = new Step(new TakeSomeTime(5, "Step 7", new DataStore[] { d1, d4, d7 },
        new DataStore[0]), s4, s6);

    return s7;
  }

  @Test
  public void testIt() throws IOException {
    Step tail = getTailStep();
    execute(tail);
  }

  private static DataStore getFakeDS(String name) throws IOException {
    return new TupleDataStoreImpl("test", "/tmp/", name, new Fields());
  }
}
