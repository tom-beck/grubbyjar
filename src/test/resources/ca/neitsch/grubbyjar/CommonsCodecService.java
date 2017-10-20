package ext;

import org.jruby.Ruby;
import org.jruby.runtime.load.BasicLibraryService;

import java.io.IOException;

public class CommonsCodecService
        implements BasicLibraryService
{
    @Override
    public boolean basicLoad(Ruby runtime) throws IOException {
        System.out.println("Hello from CommonsCodecService");
        return true;
    }
}
