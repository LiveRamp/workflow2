class Workflow::StepAttemptDatastore < Workflow::BaseModel
  belongs_to :step_attempt
  belongs_to :workflow_attempt_datastore
end
