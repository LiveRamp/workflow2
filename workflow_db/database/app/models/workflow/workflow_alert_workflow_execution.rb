class Workflow::WorkflowAlertWorkflowExecution < Workflow::BaseModel
  belongs_to :workflow_execution
  belongs_to :workflow_alert
end
