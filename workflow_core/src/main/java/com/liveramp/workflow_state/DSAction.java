package com.liveramp.workflow_state;

import java.util.Set;

import com.google.common.collect.Sets;

public enum DSAction {

  //  input
  READS_FROM,
  CONSUMES,

  //  output
  CREATES,           // auto sweep store
  CREATES_TEMPORARY, // auto sweep store
  WRITES_TO;

  public static final Set<DSAction> INPUT = Sets.newHashSet(
    READS_FROM, CONSUMES
  );

  public static final Set<DSAction> OUTPUT = Sets.newHashSet(
    CREATES, CREATES_TEMPORARY, WRITES_TO
  );

  @Override
  public String toString() {
    return super.toString().toLowerCase();
  }

  public static DSAction findByValue(int val){
    return values()[val];
  }

}
