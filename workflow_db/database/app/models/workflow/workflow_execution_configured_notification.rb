class Workflow::WorkflowExecutionConfiguredNotification < Workflow::BaseModel
  belongs_to :configured_notification
  belongs_to :workflow_execution
end
