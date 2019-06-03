
class Workflow::StepAttempt < Workflow::BaseModel
  belongs_to :workflow_attempt
  has_many :step_dependencies, :class_name => "StepDependency", :dependent => :destroy
  has_many :dependent_steps, :class_name => "StepDependency", :foreign_key => :dependency_attempt_id, :dependent => :destroy
  has_many :step_attempt_datastores, :dependent => :destroy
  has_many :mapreduce_jobs, :dependent => :destroy
  has_many :step_statistics, :dependent => :destroy

  has_one :background_step_attempt_info, :dependent => :destroy
end
