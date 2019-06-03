class Workflow::ExecutionTag < Workflow::BaseModel
  belongs_to :workflow_execution
end
