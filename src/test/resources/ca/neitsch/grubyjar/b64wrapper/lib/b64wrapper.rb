require_relative 'ext/commons-codec'

module B64Wrapper
  import org.apache.commons.codec.binary.Base64

  def self.decode(s)
    Base64::decode_base64(s)
  end
end

if __FILE__ == $0
  puts B64Wrapper.decode('aGVsbG8gYmFzZTY0Cg==')
end
