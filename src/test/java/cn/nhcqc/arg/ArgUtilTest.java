package cn.nhcqc.arg;

import java.util.*;
import org.junit.jupiter.api.*;
import static java.lang.System.out;

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
        Map<String, String> plain = ArgUtil.parseQueryString ("mode=PLAIN&dek=");
        Assertions.assertTrue (plain.get ("mode").equals ("PLAIN"));

        Map<String, String> xor   = ArgUtil.parseQueryString ("mode=XOR&dek=123");
        Assertions.assertTrue (xor.get ("mode").equals ("XOR"));
        Assertions.assertTrue (xor.get ("dek" ).equals ("123"));
    }

    //------------------------------------------------------------------------
    @Test
    public void testBase16 () {
        String base16 = "0123456789ABCDEF";
        byte[] unbase16 = ArgUtil.unbase16 (base16);
        String base16_2 = ArgUtil.base16 (unbase16);
        final int HEAD_N = 4;
        String head16 = ArgUtil.head16 (unbase16, HEAD_N);
        out.printf ("Base16 转换再转换: %s%n", base16_2);
        out.printf ("Base16 转换再转换等于原值: %b%n", base16.equalsIgnoreCase (base16_2));
        out.printf ("Base16 预览%d字节: %s%n", HEAD_N, head16);
    }

}
