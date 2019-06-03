class Workflow::UserDashboard < Workflow::BaseModel
  belongs_to :user
  belongs_to :dashboard
end
