package cn.nhcqc.arg;

import org.junit.jupiter.api.*;

public class ArgUtilTest {

    //------------------------------------------------------------------------
    @Test
    public void testGetArg () {
        String get = null;

        get = ArgUtil.getArg (null, new String[] {"-i", "pub.pem", "-o", "pub.txt"}, "-i", "--input");
        Assertions.assertEquals ("pub.pem", get);

        get = ArgUtil.getArg (null, new String[] {"--input", "pub.pem", "--output", "pub.txt"}, "-o", "--output");
        Assertions.assertEquals ("pub.txt", get);
    }

    //------------------------------------------------------------------------
    @Test
    public void testGetIntArg () {
        int get = 0;

        get = ArgUtil.getIntArg (0, new String[] {"-h", "99"}, "-h", "--head");
        Assertions.assertEquals (99, get);

        get = ArgUtil.getIntArg (0, new String[] {"--head", "99"}, "-h", "--head");
        Assertions.assertEquals (99, get);
    }

    //------------------------------------------------------------------------
    @Test
    public void testHasArg () {
        boolean has = false;

        has = ArgUtil.hasArg (new String[] {"-version"}, "-version", "--version");
        Assertions.assertTrue (has);

        has = ArgUtil.hasArg (new String[] {"--version"}, "-version", "--version");
        Assertions.assertTrue (has);
    }

    //------------------------------------------------------------------------
    @Test
    public void testParseQueryString () {
        var plain = ArgUtil.parseQueryString ("mode=PLAIN&dek=");
        Assertions.assertTrue (plain.get ("mode").equals ("PLAIN"));

        var xor   = ArgUtil.parseQueryString ("mode=XOR&dek=123");
        Assertions.assertTrue (xor.get ("mode").equals ("XOR"));
        Assertions.assertTrue (xor.get ("dek" ).equals ("123"));
    }

}
