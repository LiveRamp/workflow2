class Workflow::WorkflowExecutionResourceRoot < Workflow::BaseModel
  belongs_to :workflow_execution
  belongs_to :resource_root
end
