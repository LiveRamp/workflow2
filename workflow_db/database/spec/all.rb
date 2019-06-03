
# Run all unit tests
specs = Dir[File.dirname(__FILE__) + "/**/*_spec.rb"]
specs.each do |f|
  require f
end
