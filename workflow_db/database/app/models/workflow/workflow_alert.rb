class Workflow::WorkflowAlert < Workflow::BaseModel
  has_one :workflow_alert_mapreduce_job, :dependent => :destroy
  has_one :workflow_alert_workflow_execution, :dependent => :destroy
end
