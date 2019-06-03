class Workflow::WorkflowAttemptConfiguredNotification < Workflow::BaseModel
  belongs_to :configured_notification
  belongs_to :workflow_attempt
end
