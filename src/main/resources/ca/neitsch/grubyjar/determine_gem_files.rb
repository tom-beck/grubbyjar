require 'bundler'
require 'pathname'

GGem = Java::CaNeitschGrubyjar::Gem

def determine_gem_files(gemfile, gemfile_lock)
  definition = Bundler::Definition.build(gemfile, gemfile_lock, nil)

  definition.specs_for([:default]).map do |spec|
    # It would be possible to use constants from the Ruby class instead of
    # hardcoding strings here, but the classpath fiddling required to run rspec
    # standalone doesn’t seem worth it.
    ret = {
        GGem::NAME => spec.name,
        GGem::VERSION => spec.version.to_s,
        GGem::GEMSPEC => spec.loaded_from,
        GGem::FULL_NAME => spec.full_name,
        GGem::INSTALL_PATH => spec.full_gem_path,
        GGem::SPEC_CLASS_NAME => spec.class.to_s,
    }
    if spec.executable
      bin_dir = Pathname.new(spec.bin_dir)
      full_gem_path = Pathname.new(spec.full_gem_path)

      ret[GGem::EXECUTABLE] = (bin_dir.relative_path_from(full_gem_path) /
          spec.executable).to_s
    end
    ret
  end.to_a
end
