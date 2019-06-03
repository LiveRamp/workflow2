class Workflow::WorkflowAlertMapreduceJob < Workflow::BaseModel
  belongs_to :mapreduce_job
  belongs_to :workflow_alert
end
