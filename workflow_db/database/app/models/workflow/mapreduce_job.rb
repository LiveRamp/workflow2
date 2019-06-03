class Workflow::MapreduceJob < Workflow::BaseModel
  belongs_to :step_attempt
  has_many :mapreduce_counters, :dependent => :destroy
  has_many :mapreduce_job_task_exceptions, :dependent => :destroy
  has_many :workflow_alert_mapreduce_job, :dependent => :destroy
end
