class Workflow::WorkflowAttemptDatastore < Workflow::BaseModel
  has_many :step_attempt_datastores, :dependent => :destroy
  belongs_to :workflow_attempt
end

