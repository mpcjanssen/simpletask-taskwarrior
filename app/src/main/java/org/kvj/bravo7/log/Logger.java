package org.kvj.bravo7.log;

import android.util.Log;

/**
 * Created by vorobyev on 6/12/14.
 */
public class Logger {

    public enum LoggerLevel {Debug, Info, Warning, level, Error};

    public static interface LoggerOutput {
        public boolean output(Logger logger, LoggerLevel level, Throwable e, String line);
    }

    public static class NullLogger implements LoggerOutput {

        @Override
        public boolean output(Logger logger, LoggerLevel level, Throwable e, String line) {
            return true;
        }
    }

    private static LoggerOutput output = new NullLogger();

    public static void setOutput(LoggerOutput output) {
        Logger.output = output;
    }

    private String LOG = null;

    public Logger(String title) {
        this.LOG = title;
    }

    public static Logger forClass(Class cl) {
        return new Logger(cl.getSimpleName());
    }

    public static Logger forInstance(Object obj) {
        return new Logger(obj.getClass().getSimpleName());
    }

    public static Logger forString(String title) {
        return new Logger(title);
    }

    private String log(Object[] objects, int from) {
        StringBuilder sb = new StringBuilder();
        if (null != objects) {
            for (int i = from; i < objects.length; i++) {
                if (i>0) {
                    sb.append(' ');
                }
                if (null == objects[i]) {
                    sb.append("[NULL]");
                } else {
                    sb.append(objects[i].toString());
                }
            }
        }
        return sb.toString();
    }

    public String log(LoggerLevel level, Object... data) {
        String str = log(data, 0);
        if (null != output) {
            output.output(this, level, null, str);
        }
        return str;
    }

    public String d(Object... data) {
        String str = log(data, 0);
        if (null != output) {
            output.output(this, LoggerLevel.Debug, null, str);
        }
        return str;
    }

    public String i(Object... data) {
        String str = log(data, 0);
        if (null != output) {
            output.output(this, LoggerLevel.Info, null, str);
        }
        return str;
    }

    public String w(Object... data) {
        String str = log(data, 0);
        if (null != output) {
            output.output(this, LoggerLevel.Warning, null, str);
        }
        return str;
    }

    public String e(Object... data) {
        int from = 0;
        Throwable e = null;
        if (null != data && data.length>0 && data[0] instanceof Throwable) {
            e = (Throwable) data[0];
            from = 1;
        }
        String str = log(data, from);
        if (null != output) {
            output.output(this, LoggerLevel.Error, e, str);
        }
        return str;
    }

    public String getTitle() {
        return LOG;
    }
}
