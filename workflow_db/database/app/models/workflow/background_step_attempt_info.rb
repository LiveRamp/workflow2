class Workflow::BackgroundStepAttemptInfo < Workflow::BaseModel
  belongs_to :step_attempt
  belongs_to :background_workflow_executor_info
end
