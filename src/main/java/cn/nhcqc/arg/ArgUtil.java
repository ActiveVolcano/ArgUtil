package cn.nhcqc.arg;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 参数相关例程库
 * @author 陈庆灿
 */
public class ArgUtil {
    public static final int BYTES_INT = 4, KB = 1024, BUFSIZ = 4 * KB;

    //------------------------------------------------------------------------
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
    /** 读入 .properties 文件，相当于无 [] 分节的 .ini 文件。 */
    public static Properties loadProperties (final Path path)
    throws IOException {
        Properties prop = new Properties ();
        try (InputStream fin = Files.newInputStream (path)) {
            prop.load (fin);
        }
        return prop;
    }

    //------------------------------------------------------------------------
    /** 先试读 Java 环境变量，再试读操作系统环境变量，都找不到则返回缺省值。 */
    public static String getEnv2 (final String name, final String default_) {
        String value = System.getProperty (name);
        if (value != null) return value;
        value = System.getenv (name);
        return value != null ? value : default_;
    }

    //------------------------------------------------------------------------
    /** 先试读 Java 环境变量，再试读操作系统环境变量，再试读 Properties 参数，都找不到则返回缺省值。 */
    public static String getEnvProperty (final Properties prop, final String name, final String default_) {
        String value = getEnv2 (name, null);
        if (value != null) return value;
        return prop.getProperty (name, default_);
    }

    //------------------------------------------------------------------------
    /** parse URL query string */
    public static Map <String, String> parseQueryString (final String query) {
        Map <String, String> r = new HashMap <String, String> ();
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
        List<String> list = new ArrayList<String> (2);
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
        List<String> list = new ArrayList<String> (2);
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
        if (line == null || line.trim().isEmpty()) return "";

        try {
            cn.nhcqc.tabfile.TabFileParser tab = (cn.nhcqc.tabfile.TabFileParser)
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
            if (LOG != null && LOG instanceof org.slf4j.Logger) {
                ((org.slf4j.Logger) LOG).warn ("按列分解文本行出错: " + line, e);
            }
            return "";
        }
    }

    //------------------------------------------------------------------------
    /** 暂停且忽略异常。和 sleepIgEx 等效，只是命名风格不同。 */
    public static void sleepex (final Duration duration) {
        try { Thread.sleep (duration.toMillis ()); }
        catch (InterruptedException e) {/* ignore */}
    }

    /** 暂停且忽略异常。和 sleepex 等效，只是命名风格不同。 */
    public static void sleepIgEx (final Duration duration) {
        sleepex (duration);
    }

    //------------------------------------------------------------------------
    /** 写错误级日志（如主项目有引用依赖项 org.slf4j:slf4j-api）、写输出流后，结束进程。 */
    public static void die (final String message, final Exception e, final PrintStream out) {
        if (LOG != null && LOG instanceof org.slf4j.Logger) {
            ((org.slf4j.Logger) LOG).error (message, e);
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
    /** 移除 map 里面在 lookup 找不到对应 key 的元素 */
    public static <K,V> void removeIfNotIn (final Map<K,V> map, final Collection<K> lookup) {
        Set<K> rm = map.keySet().stream()
            .filter (k -> ! lookup.contains (k))
            .collect(Collectors.toSet ());
        rm.forEach  (k -> map.remove (k));
    }

    //------------------------------------------------------------------------
    /** one 通过 equals 方法比较 any 中的每一个元素，如有匹配返回 true 如无匹配返回 false，如 one 或 any 为空指针则返回 false. */
    public static boolean equalsAny (final Object one, final Object... any) {
        if (one == null || any == null) return false;
        for (Object any1 : any) if (one.equals (any1)) return true;
        return false;
    }

    //------------------------------------------------------------------------
    /**
     * YAML 取指定节点值，如无指定节点返回空字串，不会返回空指针。
     * @param root 根节点由 new Yaml().load() 得到
     * @param path 节点路径以 / 分隔
     */
    public static String getYaml (final Map <String, Object> root, final String path) {
        if (root == null || path == null) return "";

        Map <String, Object> value1 = root;
        for (String path1 : path.split ("/")) {
            if (path1.trim().isEmpty()) continue;
            if (value1 == null) return "";
            Object node1 = value1.get (path1);
            if (node1 == null) return "";
            if (node1 instanceof Map) {
                @SuppressWarnings("unchecked")
                Map <String, Object> node1m = (Map <String, Object>) node1;
                value1 = node1m;
            } else {
                return node1.toString();
            }
        }

        return value1 == null ? "" : value1.toString();
    }

    //------------------------------------------------------------------------
    /**
     * YAML 取指定节点值，如无指定节点返回指定缺省值。
     * @param root 根节点由 new Yaml().load() 得到
     * @param path 节点路径以 / 分隔
     */
    public static String getYaml (final Map <String, Object> root, final String path, final String default_) {
        String value = getYaml (root, path);
        return value.isEmpty () ? default_ : value;
    }

    //------------------------------------------------------------------------
    /**
     * 如主项目有引用依赖项 org.yaml:snakeyaml 打开 YAML 文件并返回指定节点。
     * 逐级节点名逐个参数值提供。
     * 如无指定节点则返回 null.
     */
    public static Map<?, ?> getYaml (final Path file, final String... node)
    throws IOException {
        if (file == null || node == null || node.length <= 0) return null;
        Object aYaml;
        try {
            aYaml = Class.forName ("org.yaml.snakeyaml.Yaml").getConstructor ().newInstance ();
        } catch (Exception e) {
            return null;
        }
        org.yaml.snakeyaml.Yaml yaml = (org.yaml.snakeyaml.Yaml) aYaml;

        try(InputStream in = Files.newInputStream (file)) {
            Map<?, ?> root = yaml.load (in);
            if (root instanceof Map<?, ?>) {
                Map<?, ?> mapRoot = (Map<?, ?>) root;
                Map<?, ?> mapEnd = mapRoot;
                for(String node1 : node) {
                    if (mapEnd.get (node1) instanceof Map<?, ?>) mapEnd = (Map<?, ?>) mapEnd.get (node1);
                    else return null;
                }
                return mapEnd;
            }
        }
        return null;
    }

    //------------------------------------------------------------------------
    /** 基于纳秒时间戳生成数据库表 ID 字段值 */
    public static long newDBID () {
        return System.currentTimeMillis () * 1_000_000L + LocalDateTime.now().getNano() % 1_000_000L;
    }

    //------------------------------------------------------------------------
    /** 逐个将数组的元素设为数据库指令的参数 */
    public static PreparedStatement sqlSetArgs (final PreparedStatement sql, final Object... args)
    throws SQLException {
        if (sql == null || args == null) return sql;
        for (int i = 1 ; i <= args.length ; i++) {
            sql.setObject (i, args[i - 1]);
        }
        return sql;
    }

    /** 数据库执行无结果指令 */
    public static int sqlUpdate (final PreparedStatement sql, final Object... args)
    throws SQLException {
        Objects.requireNonNull (sql);
        sqlSetArgs (sql, args);
        return sql.executeUpdate ();
    }

    /**
     * 查询数据库结果是一个整数.
     * @return 如查无结果返回缺省值
     */
    public static int sqlQueryInt (final int default_, final PreparedStatement sql, final Object... args)
    throws SQLException {
        Objects.requireNonNull (sql);
        sqlSetArgs (sql, args);
        try (java.sql.ResultSet r = sql.executeQuery ()) {
            return r.next () ? r.getInt (1) : default_;
        }
    }

    /**
     * 查询数据库结果是一个长整数.
     * @return 如查无结果返回缺省值
     */
    public static long sqlQueryLong (final long default_, final PreparedStatement sql, final Object... args)
    throws SQLException {
        Objects.requireNonNull (sql);
        sqlSetArgs (sql, args);
        try (java.sql.ResultSet r = sql.executeQuery ()) {
            return r.next () ? r.getLong (1) : default_;
        }
    }

    /**
     * 查询数据库结果是一个字符串.
     * @return 如查无结果返回缺省值
     */
    public static String sqlQueryString (final String default_, final PreparedStatement sql, final Object... args)
    throws SQLException {
        Objects.requireNonNull (sql);
        sqlSetArgs (sql, args);
        try (java.sql.ResultSet r = sql.executeQuery ()) {
            return r.next () ? r.getString (1) : default_;
        }
    }

    //------------------------------------------------------------------------
    /** 0x%X */
    public static String hex (final int i) {
        return String.format ("0x%X", i);
    }

    /** 0x%08X */
    public static String hex0 (final int i) {
        return String.format ("0x%08X", i);
    }

    /** 0x%X */
    public static String hex (final long l) {
        return String.format ("0x%X", l);
    }

    /** 0x%016X */
    public static String hex0 (final long l) {
        return String.format ("0x%016X", l);
    }

    //------------------------------------------------------------------------
    // private static final HexFormat BASE16 = HexFormat.of ().withUpperCase ();
    private static final Object BASE16 = initBASE16 ();

    private static Object initBASE16 () {
        Object aBASE16 = null;
        try {
            Class<?> classHexFormat = Class.forName ("java.util.HexFormat");
            Object aHexFormat = classHexFormat.getMethod ("of").invoke(null);
            aBASE16 = classHexFormat.getMethod ("withUpperCase").invoke (aHexFormat);
        } catch (Throwable t) {
            aBASE16 = null;
        }
        return aBASE16;
    }

    /** 字节数组转成 Base16 (Hex) 字符串 */
    public static String base16 (final byte[] b) {
        if (b == null) return "null";
        if (BASE16 == null) return "当前 Java 版本不支持 HexFormat";
        // return BASE16.formatHex (b);

        try {
            return (String) BASE16
                .getClass()
                .getMethod ("formatHex", byte[].class)
                .invoke (BASE16, b);
        } catch (Exception e) {
            return e.getMessage();
        }
    }
    /** 字节数组转成 Base16 (Hex) 字符串 */
    public static String base16 (final byte[] b, final int len) {
        if (b == null) return "null";
        if (len <= 0) return "";
        if (BASE16 == null) return "当前 Java 版本不支持 HexFormat";
        // return BASE16.formatHex (b, 0, Math.min (len, b.length));

        try {
            return (String) BASE16
                .getClass()
                .getMethod ("formatHex", byte[].class, int.class, int.class)
                .invoke (BASE16, b, 0, Math.min (len, b.length));
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /** Base16 (Hex) 字符串转成字节数组 */
    public static byte[] unbase16 (final String s) {
        if (s == null || s.isEmpty ()) return new byte[0];
        if (BASE16 == null) return "当前 Java 版本不支持 HexFormat".getBytes();
        // return BASE16.parseHex (s);

        try {
            return (byte[]) BASE16
                .getClass()
                .getMethod ("parseHex", CharSequence.class)
                .invoke (BASE16, s);
        } catch (Exception e) {
            return e.getMessage().getBytes();
        }
    }

    /** 输出字节数组长度及前面部分字节作为预览，如入参为空指针则返回 "(null)". */
    public static String head16 (final byte[] b, final int len) {
        if (b == null) return "(null)";
        if (len <= 0) return "";
        if (BASE16 == null) return "当前 Java 版本不支持 HexFormat";

        StringBuilder s = new StringBuilder();
        s.append ("(").append (b.length).append (") ");

        // BASE16.formatHex (s, b, 0, Math.min (len, b.length));
        try {
            BASE16
                .getClass()
                .getMethod ("formatHex", Appendable.class, byte[].class, int.class, int.class)
                .invoke (BASE16, s, b, 0, Math.min (len, b.length));
        } catch (Exception e) {
            return e.getMessage();
        }

        return s.toString();
    }

    /** 输出字节数组长度及前面16字节作为预览，如入参为空指针则返回 "(null)". */
    public static String head16 (final byte[] b) {
        return head16 (b, 16);
    }

    /** 输出字符串长度及前面部分字符作为预览，如入参为空指针则返回 "(null)". */
    public static String headchar (final String s, final int len) {
        if (s == null) return "(null)";
        return noMoreThan (s, len);
    }

    /** 输出字符串长度及前面16字符作为预览，如入参为空指针则返回 "(null)". */
    public static String headchar (final String s) {
        return headchar (s, 16);
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
        return b == null ? "null" : BASE64ENC.encodeToString (b);
    }
    /** 字节数组转成 URL 安全的 Base64 字符串 */
    public static String base64url (final byte[] b) {
        return b == null ? "null" : BASE64URLENC.encodeToString (b);
    }
    /** 字节数组转成 MIME 样式 Base64 字符串 */
    public static String base64mime (final byte[] b) {
        return b == null ? "null" : BASE64MIMEENC.encodeToString (b);
    }

    /** 字节数组转成 Base64 字符串 */
    public static String base64 (final byte[] b, final int len) {
        if (b == null || len <= 0) return "";
        return len == b.length ?
            BASE64ENC.encodeToString (b) :
            BASE64ENC.encodeToString (Arrays.copyOf (b, len)) ;
    }
    /** 字节数组转成 URL 安全的 Base64 字符串 */
    public static String base64url (final byte[] b, final int len) {
        if (b == null || len <= 0) return "";
        return len == b.length ?
            BASE64URLENC.encodeToString (b) :
            BASE64URLENC.encodeToString (Arrays.copyOf (b, len)) ;
    }
    /** 字节数组转成 MIME 样式 Base64 字符串 */
    public static String base64mime (final byte[] b, final int len) {
        if (b == null || len <= 0) return "";
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
        return b == null ? "null" : BASE64URLENC.withoutPadding ().encodeToString (b);
    }

    //------------------------------------------------------------------------
    /** 拼接多个字节数组 */
    public static byte[] concat (final byte[] a, final byte[]... b) {
        if (b == null || b.length <= 0) return a;
        int len = a.length +
                  Stream.of (b).mapToInt (b1 -> b1.length).sum ();
        byte[] c = Arrays.copyOf (a, len);

        int pos = a.length;
        for (byte[] b1 : b) {
            System.arraycopy (b1, 0, c, pos, b1.length);
            pos += b1.length;
        }

        return c;
    }

    //------------------------------------------------------------------------
    /** 将整数按本机字节序转成字节数组 */
    public static byte[] toBytes (final int i) {
        byte[] b = new byte [BYTES_INT];
        ByteBuffer.wrap (b).order (ByteOrder.nativeOrder ()).putInt (i);
        return b;
    }

    //------------------------------------------------------------------------
    /** 将4个字节以本机字节序转为整数，入参如为空指针或长度不足4字节则返回0. */
    public static int toInt (final byte[] b) {
        if (b == null || b.length < BYTES_INT) return 0;
        return ByteBuffer.wrap (b).order (ByteOrder.nativeOrder ()).getInt ();
    }

    /** 从指定偏移开始截取4字节以本机字节序转为，入参如为空指针或长度不足4字节则返回0. */
    public static int toInt (final byte[] b, final int offset) {
        if (b == null || b.length < BYTES_INT) return 0;
        return ByteBuffer.wrap (b, offset, BYTES_INT)
            .order (ByteOrder.nativeOrder())
            .getInt ();
    }

    /** 将一个字节按无符号规则转为整数 */
    public static int toUInt (final byte b) {
        byte[] bi = new byte [] { b, 0, 0, 0 };
        return ByteBuffer.wrap (bi).order (ByteOrder.LITTLE_ENDIAN).getInt ();
    }

    //------------------------------------------------------------------------
    /** 返回字节数组长度，如为空指针则返回 "null". */
    public static String strLength (final byte[] b) {
        return b == null ? "null" : Integer.toString (b.length);
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
        try { // return in.readAllBytes ();
            java.lang.reflect.Method readAllBytesMethod = InputStream.class.getMethod("readAllBytes");
            return (byte[]) readAllBytesMethod.invoke (in);
        } catch (NoSuchMethodException e) {
            throw new IOException ("当前 Java 版本不支持 InputStream.readAllBytes()", e);
        } catch (Exception e) {
            throw new IOException (e);
        } finally {
            in.close ();
        }
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
        StringBuilder s = new StringBuilder ();
        try(BufferedReader reader =
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
        try (RandomAccessFile in = new RandomAccessFile (f, "r")) {
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
    /** 如字符串现有长度不超出指定值则原样返回，否则返回截短后副本。如传入空指针或负数，则返回空字符串。 */
    public static String noMoreThan (final String s, final int len) {
        if (s != null && len > 0) {
            return s.length() <= len ? s : s.substring (0, len);
        } else {
            return "";
        }
    }

    //------------------------------------------------------------------------
    /** 在文件指定偏移量处，写入整个字节数组。 */
    public static void write (final File f, final byte[] write, final long pos)
    throws IOException {
        try (RandomAccessFile out = new RandomAccessFile (f, "rw")) {
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
    public static class ExecBytes {
        public final int exitcode;
        public final byte[] output;

        public ExecBytes (final int exitcode, final byte[] output) {
            this.exitcode = exitcode;
            this.output = output;
        }
    }

    public static class ExecString {
        public final int exitcode;
        public final String output;

        public ExecString (final int exitcode, final String output) {
            this.exitcode = exitcode;
            this.output = output;
        }
    }

    /** 在指定路径运行外部程序，等结束，取进程返回码和输出字节串，含错误输出。 */
    public static ExecBytes exec_b (final File cwd, final String... cmd)
    throws IOException, InterruptedException {
        ByteArrayOutputStream output = new ByteArrayOutputStream ();
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
        ExecBytes exec = exec_b (cwd, cmd);
        return new ExecString (exec.exitcode, new String (exec.output, cs));
    }

    /** 在当前路径运行外部程序，等结束，取进程返回码和输出字符串，含错误输出。 */
    public static ExecString exec_s (final Charset cs, final String... cmd)
    throws IOException, InterruptedException {
        ExecBytes exec = exec_b (cmd);
        return new ExecString (exec.exitcode, new String (exec.output, cs));
    }

    /** 在指定路径运行外部程序，等结束，取进程返回码和输出字符串（操作系统当前字符集），含错误输出。 */
    public static ExecString exec_s (final File cwd, final String... cmd)
    throws IOException, InterruptedException {
        Charset cs = Charset.forName (System.getProperty ("native.encoding"));
        return exec_s (cwd, cs, cmd);
    }

    /** 在当前路径运行外部程序，等结束，取进程返回码和输出字符串（操作系统当前字符集），含错误输出。 */
    public static ExecString exec_s (final String... cmd)
    throws IOException, InterruptedException {
        Charset cs = Charset.forName (System.getProperty ("native.encoding"));
        return exec_s (cs, cmd);
    }

    //------------------------------------------------------------------------
    /** 返回第一个找到的文件名，如果都找不到则返回空指针。 */
    public static String existsWhichFile (final String[] files) {
        if (files == null) return null;
        for (String file1 : files) {
            if (Files.exists (Paths.get (file1))) {
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
