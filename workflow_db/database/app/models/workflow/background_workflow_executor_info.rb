class Workflow::BackgroundWorkflowExecutorInfo < Workflow::BaseModel
  has_many :background_step_attempt_info, :dependent => :nullify
end
