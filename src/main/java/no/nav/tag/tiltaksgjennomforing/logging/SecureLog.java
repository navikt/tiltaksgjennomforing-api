package no.nav.tag.tiltaksgjennomforing.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class SecureLog implements Logger {
    private static final Marker SECURE_LOG = MarkerFactory.getMarker("SECURE_LOG");

    private final Logger log;

    private SecureLog(Logger log) {
        this.log = log;
    }

    public static SecureLog getLogger(Logger log) {
        return new SecureLog(log);
    }

    public static SecureLog getLogger(Class<?> clazz) {
        return new SecureLog(LoggerFactory.getLogger(clazz));
    }

    @Override
    public String getName() {
        return log.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        log.trace(SECURE_LOG, msg);
    }

    @Override
    public void trace(String format, Object arg) {
        log.trace(SECURE_LOG, format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        log.trace(SECURE_LOG, format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        log.trace(SECURE_LOG, format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        log.trace(SECURE_LOG, msg, t);
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public boolean isTraceEnabled(Marker marker) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void trace(Marker marker, String msg) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void trace(Marker marker, String format, Object arg) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void trace(Marker marker, String format, Object... argArray) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void trace(Marker marker, String msg, Throwable t) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        log.debug(SECURE_LOG, msg);
    }

    @Override
    public void debug(String format, Object arg) {
        log.debug(SECURE_LOG, format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        log.debug(SECURE_LOG, format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        log.debug(SECURE_LOG, format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        log.debug(SECURE_LOG, msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return false;
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void debug(Marker marker, String msg) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void debug(Marker marker, String format, Object arg) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void debug(Marker marker, String format, Object... arguments) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void debug(Marker marker, String msg, Throwable t) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        log.info(SECURE_LOG, msg);
    }

    @Override
    public void info(String format, Object arg) {
        log.info(SECURE_LOG, format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        log.info(SECURE_LOG, format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        log.info(SECURE_LOG, format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        log.info(SECURE_LOG, msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return log.isInfoEnabled();
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void info(Marker marker, String msg) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void info(Marker marker, String format, Object arg) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void info(Marker marker, String format, Object... arguments) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void info(Marker marker, String msg, Throwable t) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        log.warn(SECURE_LOG, msg);
    }

    @Override
    public void warn(String format, Object arg) {
        log.warn(SECURE_LOG, format, arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        log.warn(SECURE_LOG, format, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        log.warn(SECURE_LOG, format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        log.warn(SECURE_LOG, msg, t);
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public boolean isWarnEnabled(Marker marker) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void warn(Marker marker, String msg) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void warn(Marker marker, String format, Object arg) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void warn(Marker marker, String format, Object... arguments) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void warn(Marker marker, String msg, Throwable t) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void error(String msg) {
        log.error(SECURE_LOG, msg);
    }

    @Override
    public void error(String format, Object arg) {
        log.error(SECURE_LOG, format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        log.error(SECURE_LOG, format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        log.error(SECURE_LOG, format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        log.error(SECURE_LOG, msg, t);
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public boolean isErrorEnabled(Marker marker) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void error(Marker marker, String msg) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void error(Marker marker, String format, Object arg) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void error(Marker marker, String format, Object... arguments) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Secure logs kan ikke brukes med marker")
    public void error(Marker marker, String msg, Throwable t) {
        throw new UnsupportedOperationException("Secure logs kan ikke brukes med marker");
    }
}
