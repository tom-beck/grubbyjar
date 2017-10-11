require 'concurrent'
require 'jardep1_jars'

import com.google.common.base.Strings

module Jardep1
  class Main
    def run
      puts Concurrent::Event.new
      puts Strings::repeat("hello", 3)
    end
  end
end
