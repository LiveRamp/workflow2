class AddTasksSampledToMapreduceJob < ActiveRecord::Migration
  def self.up
    add_column :mapreduce_jobs, :tasks_sampled, :integer, :null => true
    add_column :mapreduce_jobs, :tasks_failed_in_sample, :integer, :null => true
  end

  def self.down
    remove_column :mapreduce_jobs, :tasks_failed_in_sample
    remove_column :mapreduce_jobs, :tasks_sampled
  end
end
