class AddHostUrlToMapreduceJobTaskExceptions < ActiveRecord::Migration
  def self.up
    add_column :mapreduce_job_task_exceptions, :host_url, :string
  end

  def self.down
    remove_column :mapreduce_job_task_exceptions, :host_url
  end
end
