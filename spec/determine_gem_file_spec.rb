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
end
