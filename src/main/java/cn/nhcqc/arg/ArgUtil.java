package cn.nhcqc.arg;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 参数相关例程库
 * @author 陈庆灿
 */
public class ArgUtil {
    public static final int KB = 1024, BUFSIZ = 4 * KB;

    /** 如主项目有引用依赖项 org.slf4j:slf4j-api 令 org.slf4j.Logger 类存在，LOG 为该类对象，否则为空指针。 */
    public static final Object LOG;
    static {
        Object a = null;
        try {
            a = Class.forName ("org.slf4j.LoggerFactory")
                     .getMethod ("getLogger", Class.class)
                     .invoke (null, ArgUtil.class);
        } catch (Exception ignore) {/* ignore */}
        LOG = a;
    }

    //------------------------------------------------------------------------
    /**
     * 在 args 里找到任意一个 name 后返回下一个 args 元素；
     * 或在 args 里找到任意一个 name=... 返回等号后面文字.
     * 如 args 为空、或 name 为空、或 name 有一个元素为空指针、或全都找不到，返回缺省值。
     * @param def 缺省值
     */
    public static String getArg (final String def, final String[] args, final String... name) {
        if (args == null || name == null) return def;
        if (args.length <= 0 || name.length <= 0) return def;

        String[] equ = new String [name.length];
        for (int in = 0 ; in < name.length ; in++) {
            if (name[in] == null) return def;
            equ[in] = name[in] + '=';
        }

        for (int ia = 0 ; ia < args.length ; ia++) {
            for (int in = 0 ; in < name.length ; in++) {
                if (args[ia].equals (name[in]) && ia + 1 < args.length) return args[ia + 1];
                if (args[ia].startsWith (equ[in])) return args[ia].substring (equ[in].length ());
            }
        }
        return def;
    }

    /**
     * 在 args 里找到任意一个 name 后返回下一个 args 元素；
     * 或在 args 里找到任意一个 name=... 返回等号后面文字.
     * 如 args 为空、或 name 为空、或 name 有一个元素为空指针、或全都找不到、或文字不能转成整数，返回缺省值。
     * @param def 缺省值
     */
    public static int getIntArg (final int def, final String[] args, final String... name) {
        String value = getArg (Integer.toString (def), args, name);
        try { return Integer.parseInt (value); }
        catch (Exception e) { return def; }
    }

    /**
     * 在 args 里找到任意一个 name 后返回下一个 args 元素；
     * 或在 args 里找到任意一个 name=... 返回等号后面文字.
     * 如 args 为空、或 name 为空、或 name 有一个元素为空指针、或全都找不到、或文字不能转成长整数，返回缺省值。
     * @param def 缺省值
     */
    public static long getLongArg (final long def, final String[] args, final String... name) {
        String value = getArg (Long.toString (def), args, name);
        try { return Long.parseLong (value); }
        catch (Exception e) { return def; }
    }

    /**
     * 在 args 里找到任意一个 name 后返回下一个 args 元素；
     * 或在 args 里找到任意一个 name=... 返回等号后面文字.
     * 如 args 为空、或 name 为空、或 name 有一个元素为空指针、或全都找不到、或文字不能转成浮点数，返回缺省值。
     * @param def 缺省值
     */
    public static float getFloatArg (final float def, final String[] args, final String... name) {
        String value = getArg (Float.toString (def), args, name);
        try { return Float.parseFloat (value); }
        catch (Exception e) { return def; }
    }

    /**
     * 在 args 里找到任意一个 name 后返回下一个 args 元素；
     * 或在 args 里找到任意一个 name=... 返回等号后面文字.
     * 如 args 为空、或 name 为空、或 name 有一个元素为空指针、或全都找不到、或文字不能转成双精度浮点数，返回缺省值。
     * @param def 缺省值
     */
    public static double getFloatArg (final double def, final String[] args, final String... name) {
        String value = getArg (Double.toString (def), args, name);
        try { return Double.parseDouble (value); }
        catch (Exception e) { return def; }
    }

    //------------------------------------------------------------------------
    /**
     * 在 args 里找到任意一个 name 返回 true;
     * 或在 args 里找到任意一个 name=... 返回 true.
     * 如 args 为空、或 name 为空、或 name 有一个元素为空指针、或全都找不到，返回 false.
     */
    public static boolean hasArg (final String[] args, final String... name) {
        if (args == null || name == null) return false;
        if (args.length <= 0 || name.length <= 0) return false;

        String[] equ = new String [name.length];
        for (int in = 0 ; in < name.length ; in++) {
            if (name[in] == null) return false;
            equ[in] = name[in] + '=';
        }

        for (int ia = 0 ; ia < args.length ; ia++) {
            for (int in = 0 ; in < name.length ; in++) {
                if (args[ia].equals (name[in])) return true;
                if (args[ia].startsWith (equ[in])) return true;
            }
        }
        return false;
    }

    //------------------------------------------------------------------------
    /** parse URL query string */
    public static Map <String, String> parseQueryString (final String query) {
        var r = new HashMap <String, String> ();
        if (query == null) return r;

        String[] split_and = query.split ("&");
        for (String split_and_1 : split_and) {

            String[] split_equ = split_and_1.split ("=");
            if (split_equ.length != 2) continue;
            r.put (split_equ[0], split_equ[1]);
        }
        return r;
    }

    //------------------------------------------------------------------------
    /** 如入参为空指针则返回空字串，否则原样返回。 */
    public static String strNullToEmpty (String s) {
        return s != null ? s : "";
    }

    //------------------------------------------------------------------------
    /**
     * 用第一个找到的分隔符，切割原字串，返回切割后的两个字串。如找不到分隔符，返回空集。
     * @param str 原字串
     * @param sep 分隔符
     * @return 切割后的两个字串。如找不到分隔符，返回空集。
     */
    public static List<String> split2 (final String str, final Character sep) {
        var list = new ArrayList<String> (2);
        if (str == null) return list;
        int i = str.indexOf (sep);
        if (i < 0) return list;
        list.add (str.substring (0, i));
        list.add (str.substring (i + 1));
        return list;
    }

    /**
     * 用最后一个找到的分隔符，切割原字串，返回切割后的两个字串。如找不到分隔符，返回空集。
     * @param str 原字串
     * @param sep 分隔符
     * @return 切割后的两个字串。如找不到分隔符，返回空集。
     */
    public static List<String> split2last (final String str, final Character sep) {
        var list = new ArrayList<String> (2);
        if (str == null) return list;
        int i = str.lastIndexOf (sep);
        if (i < 0) return list;
        list.add (str.substring (0, i));
        list.add (str.substring (i + 1));
        return list;
    }

    //------------------------------------------------------------------------
    /** 如主项目有引用依赖项 cn.nhcqc.tabfile:tabfile-parser 按空白字符分解文本行成列，返回指定列内容。如输入为空或列号超限，返回空字串 "". */
    public static String getLineCol (final String line, final int col) {
        if (line == null || line.isBlank()) return "";

        try {
            var tab = (cn.nhcqc.tabfile.TabFileParser)
                Class.forName ("cn.nhcqc.tabfile.TabFileParser")
                     .getConstructor ()
                     .newInstance ();
            return strNullToEmpty (
                tab
                .parse (line)
                .stream ()
                .findFirst ()
                .get ()
                .get (col)
            );

        } catch (Exception e) {
            if (LOG != null && LOG instanceof org.slf4j.Logger slf4j) {
                slf4j.warn ("按列分解文本行出错: " + line, e);
            }
            return "";
        }
    }

    //------------------------------------------------------------------------
    /** Sleep ignores InterruptedException. */
    public static void sleepEx (final Duration duration) {
        try { Thread.sleep (duration.toMillis ()); }
        catch (InterruptedException e) {/* ignore */}
    }

    //------------------------------------------------------------------------
    /** 写错误级日志（如主项目有引用依赖项 org.slf4j:slf4j-api）、写输出流后，结束进程。 */
    public static void die (final String message, final Exception e, final PrintStream out) {
        if (LOG != null && LOG instanceof org.slf4j.Logger slf4j) {
            slf4j.error (message, e);
        }
        if (out != null) {
            out.println (message);
        }
        e.printStackTrace ();
        System.exit (1);
    }

    /** 写错误级日志（如主项目有引用依赖项 org.slf4j:slf4j-api）、写标准错误输出后，结束进程。 */
    public static void die (final String message, final Exception e) {
        die (message, e, System.err);
    }

    //------------------------------------------------------------------------
    /**
     * 移除 map 里面在 lookup 找不到对应 key 的元素
     */
    public static <K,V> void removeIfNotIn (final Map<K,V> map, final Collection<K> lookup) {
        Set<K> rm = map.keySet().stream()
            .filter (k -> ! lookup.contains (k))
            .collect(Collectors.toSet ());
        rm.forEach  (k -> map.remove (k));
    }

    //------------------------------------------------------------------------
    /**
     * 如主项目有引用依赖项 org.yaml:snakeyaml 打开 YAML 文件并返回指定节点。
     * 逐级节点名逐个参数值提供。
     * 如无指定节点则返回 null.
     */
    public static Map<?, ?> yamlGetNode (final Path file, final String... node)
    throws IOException {
        if (file == null || node == null || node.length <= 0) return null;
        Object aYaml;
        try {
            aYaml = Class.forName ("org.yaml.snakeyaml.Yaml").getConstructor ().newInstance ();
        } catch (Exception e) {
            return null;
        }
        var yaml = (org.yaml.snakeyaml.Yaml) aYaml;

        try(var in = Files.newInputStream (file)) {
            var root = yaml.load (in);
            if (root instanceof Map<?, ?> mapRoot) {
                Map<?, ?> mapEnd = mapRoot;
                for(var node1 : node) {
                    if (mapEnd.get (node1) instanceof Map<?, ?> mapNext) mapEnd = mapNext;
                    else return null;
                }
                return mapEnd;
            }
        }
        return null;
    }

    //------------------------------------------------------------------------
    private static final HexFormat BASE16 = HexFormat.of ().withUpperCase ();

    /** 字节数组转成 Base16 (Hex) 字符串 */
    public static String base16 (final byte[] b) {
        return BASE16.formatHex (b);
    }
    /** 字节数组转成 Base16 (Hex) 字符串 */
    public static String base16 (final byte[] b, final int len) {
        return BASE16.formatHex (b, 0, len);
    }

    /** Base16 (Hex) 字符串转成字节数组 */
    public static byte[] unbase16 (final String s) {
        return BASE16.parseHex (s);
    }

    //------------------------------------------------------------------------
    public static final Base64.Encoder BASE64ENC     = Base64.getEncoder ().withoutPadding ();
    public static final Base64.Encoder BASE64URLENC  = Base64.getUrlEncoder ().withoutPadding ();
    public static final Base64.Encoder BASE64MIMEENC = Base64.getMimeEncoder ().withoutPadding ();
    public static final Base64.Decoder BASE64DEC     = Base64.getDecoder ();
    public static final Base64.Decoder BASE64URLDEC  = Base64.getUrlDecoder ();
    public static final Base64.Decoder BASE64MIMEDEC = Base64.getMimeDecoder ();

    /** 字节数组转成 Base64 字符串 */
    public static String base64 (final byte[] b) {
        return BASE64ENC.encodeToString (b);
    }
    /** 字节数组转成 URL 安全的 Base64 字符串 */
    public static String base64url (final byte[] b) {
        return BASE64URLENC.encodeToString (b);
    }
    /** 字节数组转成 MIME 样式 Base64 字符串 */
    public static String base64mime (final byte[] b) {
        return BASE64MIMEENC.encodeToString (b);
    }

    /** 字节数组转成 Base64 字符串 */
    public static String base64 (final byte[] b, final int len) {
        return len == b.length ?
            BASE64ENC.encodeToString (b) :
            BASE64ENC.encodeToString (Arrays.copyOf (b, len)) ;
    }
    /** 字节数组转成 URL 安全的 Base64 字符串 */
    public static String base64url (final byte[] b, final int len) {
        return len == b.length ?
            BASE64URLENC.encodeToString (b) :
            BASE64URLENC.encodeToString (Arrays.copyOf (b, len)) ;
    }
    /** 字节数组转成 MIME 样式 Base64 字符串 */
    public static String base64mime (final byte[] b, final int len) {
        return len == b.length ?
            BASE64MIMEENC.encodeToString (b) :
            BASE64MIMEENC.encodeToString (Arrays.copyOf (b, len)) ;
    }

    /** Base64 字符串转成字节数组 */
    public static byte[] unbase64 (final String s) {
        return s != null ? BASE64DEC.decode (s) : new byte[0];
    }
    /** Base64 字符串转成字节数组后转成字符串 */
    public static String unbase64 (final String s, final Charset cs) {
        return new String (unbase64 (s), cs);
    }

    /** URL 安全的 Base64 字符串转成字节数组 */
    public static byte[] unbase64url (final String s) {
        return s != null ? BASE64URLDEC.decode (s) : new byte[0];
    }
    /** URL 安全的 Base64 字符串转成字节数组后转成字符串 */
    public static String unbase64url (final String s, final Charset cs) {
        return new String (unbase64url (s), cs);
    }

    /** MIME 样式 Base64 字符串转成字节数组 */
    public static byte[] unbase64mime (final String s) {
        return s != null ? BASE64MIMEDEC.decode (s) : new byte[0];
    }
    /** MIME 样式 Base64 字符串转成字节数组后转成字符串 */
    public static String unbase64mime (final String s, final Charset cs) {
        return new String (unbase64mime (s), cs);
    }

    /** URL 形式 Base64 编码成字符串，无填充等号，以便用于表单值。 */
    public static String formBase64 (final byte[] b) {
        return BASE64URLENC.withoutPadding ().encodeToString (b);
    }

    //------------------------------------------------------------------------
    /** 关闭且忽略异常 */
    public static void closeIgEx (final AutoCloseable c) {
        try { c.close (); }
        catch (Exception e) { /* ignore */ }
    }

    //------------------------------------------------------------------------
    /** 把输入流全部读了然后关闭 */
    public static byte[] closeReadAll (final InputStream in)
    throws IOException {
        try { return in.readAllBytes (); }
        finally { in.close (); }
    }

    /** 把输入流全部读了然后关闭 */
    public static String closeReadAll (final InputStream in, final Charset cs)
    throws IOException {
        return new String (closeReadAll (in), cs);
    }

    //------------------------------------------------------------------------
    /** 把输入流全部读了然后关闭，多行内容拼接成一长行。 */
    public static String closeReadAllConcat (final InputStream in, final Charset cs)
    throws IOException {
        var s = new StringBuilder ();
        try(var reader =
            new BufferedReader (
            new InputStreamReader (
            in, cs))) {
                reader.lines ().forEach (s::append);
        }
        return s.toString ();
    }

    //------------------------------------------------------------------------
    /** 读文件一部分，如偏移量超出范围返回空数组。 */
    public static byte[] readNBytes (final File f, final long pos, final int len)
    throws IOException {
        try (var in = new RandomAccessFile (f, "r")) {
            byte[] buf = new byte [len];
            in.seek (pos);
            int read = in.read (buf);
            return noMoreThan (buf, read);
        }
    }

    //------------------------------------------------------------------------
    /** 如数组现有长度不超出指定值则原样返回，否则返回截短后副本。如传入空指针或负数，则返回空数组。 */
    public static byte[] noMoreThan (final byte[] b, final int len) {
        if (b != null && len > 0) {
            return b.length <= len ? b : Arrays.copyOf (b, len);
        } else {
            return new byte[0];
        }
    }

    //------------------------------------------------------------------------
    /** 在文件指定偏移量处，写入整个字节数组。 */
    public static void write (final File f, final byte[] write, final long pos)
    throws IOException {
        try (var out = new RandomAccessFile (f, "rw")) {
            out.seek (pos);
            out.write (write);
        }
    }

    //------------------------------------------------------------------------
    /** StringBuilder append format */
    public static StringBuilder appendf
    (final StringBuilder s, final String fmt, final Object... args) {
        return s.append (String.format (fmt, args));
    }

    /** String format then to bytes */
    public static byte[] bsprintf
    (final Charset cs, final String fmt, final Object... args) {
        return String.format (fmt, args).getBytes (cs);
    }

    /** String format then to bytes to OutputStream */
    public static OutputStream bsprintf
    (final OutputStream out, final Charset cs, final String f, final Object... args)
    throws IOException {
        out.write (bsprintf (cs, f, args));
        return out;
    }

    /** String format then to bytes to OutputStream without exception */
    public static OutputStream bsprintf_ne
    (final OutputStream out, final Charset cs, final String f, final Object... args) {
        try { bsprintf (out, cs, f, args); }
        catch (IOException e) {/* ignore */}
        return out;
    }

    //------------------------------------------------------------------------
    public static record ExecBytes  (int exitcode, byte[] output) {}
    public static record ExecString (int exitcode, String output) {}

    /** 在指定路径运行外部程序，等结束，取进程返回码和输出字节串，含错误输出。 */
    public static ExecBytes exec_b (final File cwd, final String... cmd)
    throws IOException, InterruptedException {
        var output = new ByteArrayOutputStream ();
        Process proc = new ProcessBuilder (cmd)
            .directory (cwd)
            .redirectErrorStream (true)
            .start ();
        // 不等到 proc.waitFor () 之后再读以避免缓冲区满堵塞进程不结束
        for (byte[] buf = new byte [BUFSIZ];;) {
            int read = proc.getInputStream ().read (buf, 0, buf.length);
            if (read <= 0) break;
            output.write (buf, 0, read);
        }
        return new ExecBytes (proc.waitFor (), output.toByteArray ());
    }

    /** 在当前路径运行外部程序，等结束，取进程返回码和输出字节串，含错误输出。 */
    public static ExecBytes exec_b (final String... cmd)
    throws IOException, InterruptedException {
        return exec_b (null, cmd);
    }

    /** 在指定路径运行外部程序，等结束，取进程返回码和输出字符串，含错误输出。 */
    public static ExecString exec_s (final File cwd, final Charset cs, final String... cmd)
    throws IOException, InterruptedException {
        var exec = exec_b (cwd, cmd);
        return new ExecString (exec.exitcode, new String (exec.output, cs));
    }

    /** 在当前路径运行外部程序，等结束，取进程返回码和输出字符串，含错误输出。 */
    public static ExecString exec_s (final Charset cs, final String... cmd)
    throws IOException, InterruptedException {
        var exec = exec_b (cmd);
        return new ExecString (exec.exitcode, new String (exec.output, cs));
    }

    /** 在指定路径运行外部程序，等结束，取进程返回码和输出字符串（操作系统当前字符集），含错误输出。 */
    public static ExecString exec_s (final File cwd, final String... cmd)
    throws IOException, InterruptedException {
        var cs = Charset.forName (System.getProperty ("native.encoding"));
        return exec_s (cwd, cs, cmd);
    }

    /** 在当前路径运行外部程序，等结束，取进程返回码和输出字符串（操作系统当前字符集），含错误输出。 */
    public static ExecString exec_s (final String... cmd)
    throws IOException, InterruptedException {
        var cs = Charset.forName (System.getProperty ("native.encoding"));
        return exec_s (cs, cmd);
    }

    //------------------------------------------------------------------------
    /** 返回第一个找到的文件名，如果都找不到则返回空指针。 */
    public static String existsWhichFile (final String[] files) {
        if (files == null) return null;
        for (String file1 : files) {
            if (Files.exists (Path.of (file1))) {
                return file1;
            }
        }
        return null;
    }

    /**
     * 在 Linux 约定的几个程序路径找文件，如果都找不到则返回空指针。
     * <ul>
     *     <li> /bin/
     *     <li> /usr/bin/
     *     <li> /sbin/
     *     <li> /usr/sbin/
     * </ul>
     */
    public static String findLinuxExe (final String exe) {
        String[] tofind = new String[] { "/bin/" + exe, "/usr/bin/" + exe, "/sbin/" + exe, "/usr/sbin/" + exe };
        return existsWhichFile (tofind);
    }

    /** 在 Linux 约定的几个程序路径找 bash，返回全路径例如 /bin/bash，如果都找不到则返回空指针。 */
    public static String findBash () {
        return findLinuxExe ("bash");
    }

    private static String bash = null;

    /** 在当前路径通过 bash -c 执行 shell 命令，等结束，取进程返回码和输出字符串（操作系统当前字符集），含错误输出。 */
    public static String bash_c (final String shell)
    throws IOException, InterruptedException {
        if (bash == null) bash = findBash ();
        if (bash == null) throw new IOException ("bash not found");
        return exec_s (new String[] { bash, "-c", shell }).output;
    }

}
