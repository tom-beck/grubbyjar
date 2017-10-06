require 'concurrent'

module Gemspec1
  class Main
    def run
      puts Concurrent::Event.new
    end
  end
end
