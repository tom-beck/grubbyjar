require 'bundler'
require 'pathname'

def determine_gem_files(gemfile, gemfile_lock)
  definition = Bundler::Definition.build(gemfile, gemfile_lock, nil)

  definition.specs_for([:default]).map do |spec|
    ret = {
        "name" => spec.name,
        "version" => spec.version.to_s,
        "gemspec" => spec.loaded_from,
        "full_name" => spec.full_name,
        "install_path" => spec.full_gem_path,
        "spec_class_name" => spec.class.to_s,
    }
    if spec.executable
      bin_dir = Pathname.new(spec.bin_dir)
      full_gem_path = Pathname.new(spec.full_gem_path)

      ret["executable"] = (bin_dir.relative_path_from(full_gem_path) /
          spec.executable).to_s
    end
    ret
  end.to_a
end
