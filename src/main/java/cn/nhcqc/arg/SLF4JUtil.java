package cn.nhcqc.arg;

import java.util.function.Supplier;
import org.slf4j.Logger;

/**
 * SLF4J 日志例程库
 * @author 陈庆灿
 */
public class SLF4JUtil {
    private final Logger log;

    //------------------------------------------------------------------------
    /**  SLF4J 日志例程库 */
    public SLF4JUtil (Logger log) {
        this.log = log;
    }

    //------------------------------------------------------------------------
    /** 写 trace 级日志，利用 λ 表达式避免不输出 trace 级的时候计算参数值的开销。 */
    public void trace (final Supplier<String> msg) {
        if (log.isTraceEnabled()) log.trace (msg.get());
    }

    //------------------------------------------------------------------------
    /** 写 trace 级日志，通过 String.format 格式化参数值。 */
    public void tracef (final String format, final Object... args) {
        if (log.isTraceEnabled()) log.trace (String.format (format, args));
    }

    //------------------------------------------------------------------------
    /** 写 debug 级日志，利用 λ 表达式避免不输出 debug 级的时候计算参数值的开销。 */
    public void debug (final Supplier<String> msg) {
        if (log.isDebugEnabled()) log.debug (msg.get());
    }

    //------------------------------------------------------------------------
    /** 写 debug 级日志，通过 String.format 格式化参数值。 */
    public void debugf (final String format, final Object... args) {
        if (log.isDebugEnabled()) log.debug (String.format (format, args));
    }

}
