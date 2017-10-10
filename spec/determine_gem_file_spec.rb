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
        include(GGem::NAME => "bundler"),
        include(GGem::NAME => "concurrent-ruby", "version" => "1.0.5",
            GGem::INSTALL_PATH => match('/gems/concurrent-ruby-1.0.5')))
  end

  it 'gets executables from gemspecs' do
    gemfile = test_file('gemspec1/Gemfile')
    gemfile_lock = test_file('gemspec1/Gemfile.lock')

    expect(determine_gem_files(gemfile, gemfile_lock)).to contain_exactly(
        include(GGem::NAME => "bundler",
            GGem::SPEC_CLASS_NAME => "Bundler::StubSpecification"),
        include(GGem::NAME => "concurrent-ruby", "version" => "1.0.5",
            GGem::INSTALL_PATH => match('/gems/concurrent-ruby-1.0.5')),
        include(GGem::NAME => "gemspec1", GGem::EXECUTABLE => "bin/gemspec1",
            GGem::SPEC_CLASS_NAME => "Gem::Specification"))
  end
end
