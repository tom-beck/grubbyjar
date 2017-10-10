require 'pathname'

require_relative '../src/main/resources/ca/neitsch/grubyjar/determine_gem_files'

def test_file(filename)
  Pathname.new(__FILE__).parent.parent / 'src' / 'test' / 'resources' /
      'ca' / 'neitsch' / 'grubyjar' / filename
end

RSpec.describe '#determine_gem_files' do
  it 'works' do
    gemfile = test_file('concurrent-ruby.Gemfile')
    gemfile_lock = test_file('concurrent-ruby.Gemfile.lock')

    expect(determine_gem_files(gemfile, gemfile_lock)).to contain_exactly(
        include("name" => "bundler"),
        include("name" => "concurrent-ruby", "version" => "1.0.5",
            "install_path" => match('/gems/concurrent-ruby-1.0.5')))
  end

  it 'gets executables from gemspecs' do
    gemfile = test_file('gemspec1/Gemfile')
    gemfile_lock = test_file('gemspec1/Gemfile.lock')

    expect(determine_gem_files(gemfile, gemfile_lock)).to contain_exactly(
        include("name" => "bundler",
          "spec_class_name" => "Bundler::StubSpecification"),
        include("name" => "concurrent-ruby", "version" => "1.0.5",
            "install_path" => match('/gems/concurrent-ruby-1.0.5')),
        include("name" => "gemspec1", "executable" => "bin/gemspec1",
            "spec_class_name" => "Gem::Specification"))
  end
end
