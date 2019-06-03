package com.liveramp.cascading_ext.megadesk;

public interface ILockManager {
  public ILockManager lockProcessStart();
  public ILockManager lockConsumeStart();
  public ILockManager lock();
  public void release();
}
