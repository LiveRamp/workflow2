class Workflow::StepDependency < Workflow::BaseModel
  belongs_to :step_attempt, :class_name => "StepAttempt"
  belongs_to :dependency_attempt, :class_name => "StepAttempt", :foreign_key => :dependency_attempt_id
end
