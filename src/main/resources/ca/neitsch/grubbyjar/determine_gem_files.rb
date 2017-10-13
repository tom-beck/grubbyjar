require 'bundler'
require 'pathname'

def determine_gem_files(gemfile, gemfile_lock)
  gemClass = Java::CaNeitschGrubbyjar::Gem

  definition = Bundler::Definition.build(gemfile, gemfile_lock, nil)

  definition.specs_for([:default]).map do |spec|
    ret = {
        gemClass::NAME => spec.name,
        gemClass::VERSION => spec.version.to_s,
        gemClass::GEMSPEC => spec.loaded_from,
        gemClass::FULL_NAME => spec.full_name,
        gemClass::INSTALL_PATH => spec.full_gem_path
    }

    # Most specifications are stubs with incomplete metadata, but gems with
    # gemspecs have complete metadata, and need to be converted into packable
    # form.
    if spec.is_a? Gem::Specification
      ret[gemClass::FILES] = spec.files
      ret[gemClass::SPEC_TEXT] = spec.to_ruby
    end
    if spec.executable
      bin_dir = Pathname.new(spec.bin_dir)
      full_gem_path = Pathname.new(spec.full_gem_path)

      ret[gemClass::EXECUTABLE] = (bin_dir.relative_path_from(full_gem_path) /
          spec.executable).to_s
    end
    ret
  end.to_a
end
