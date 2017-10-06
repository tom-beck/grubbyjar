require 'bundler'

def determine_gem_files(gemfile, gemfile_lock)
  definition = Bundler::Definition.build(gemfile, gemfile_lock, nil)

  definition.specs_for([:default]).map do |spec|
    {
        "name" => spec.name,
        "version" => spec.version.to_s,
        "gemspec" => spec.loaded_from,
        "full_name" => spec.full_name,
        "install_path" => spec.full_gem_path
    }
  end.to_a
end
