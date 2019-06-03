class Workflow::DashboardApplication < Workflow::BaseModel
  belongs_to :dashboard
  belongs_to :application
end
