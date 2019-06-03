require 'workflow_constants'

class Workflow::WorkflowAttempt < Workflow::BaseModel

  belongs_to :workflow_execution
  has_many :step_attempt, :dependent => :destroy
  has_many :workflow_attempt_datastore, :dependent => :destroy
  has_many :workflow_attempt_configured_notification, :dependent => :destroy
  has_one :background_attempt_info, :dependent => :destroy

end
