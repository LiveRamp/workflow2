package com.liveramp.workflow_ui.servlet;

public class ClusterConstants {

  public static final int DEFAULT_PORT = 8080;
  public static final String MR2_GROUP = "org.apache.hadoop.mapreduce.JobCounter";
  public static final String MB_MAP = "MB_MILLIS_MAPS";
  public static final String MB_RED = "MB_MILLIS_REDUCES";
  public static final String VCORE_MAP = "VCORES_MILLIS_MAPS";
  public static final String VCORE_RED = "VCORES_MILLIS_REDUCES";

  //  these are the on-demand prices from https://cloud.google.com/compute/pricing.  a TODO would be making
  //  these numbers pluggable in the UI config files.
  public static final double CPU_MS_COST = 0.031611 / 60 / 60 / 1000;
  public static final double MB_MS_COST = 0.004237 / 60 / 60 / 1000 / 1000;

}
