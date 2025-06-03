package no.nav.tag.tiltaksgjennomforing.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class TeamLogs implements Logger {
    private static final Marker TEAM_LOGS = MarkerFactory.getMarker("TEAM_LOGS");

    private final Logger log;

    private TeamLogs(Logger log) {
        this.log = log;
    }

    public static TeamLogs getLogger(Logger log) {
        return new TeamLogs(log);
    }

    public static TeamLogs getLogger(Class<?> clazz) {
        return new TeamLogs(LoggerFactory.getLogger(clazz));
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
        log.trace(TEAM_LOGS, msg);
    }

    @Override
    public void trace(String format, Object arg) {
        log.trace(TEAM_LOGS, format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        log.trace(TEAM_LOGS, format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        log.trace(TEAM_LOGS, format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        log.trace(TEAM_LOGS, msg, t);
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public boolean isTraceEnabled(Marker marker) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void trace(Marker marker, String msg) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void trace(Marker marker, String format, Object arg) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void trace(Marker marker, String format, Object... argArray) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void trace(Marker marker, String msg, Throwable t) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        log.debug(TEAM_LOGS, msg);
    }

    @Override
    public void debug(String format, Object arg) {
        log.debug(TEAM_LOGS, format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        log.debug(TEAM_LOGS, format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        log.debug(TEAM_LOGS, format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        log.debug(TEAM_LOGS, msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return false;
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void debug(Marker marker, String msg) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void debug(Marker marker, String format, Object arg) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void debug(Marker marker, String format, Object... arguments) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void debug(Marker marker, String msg, Throwable t) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        log.info(TEAM_LOGS, msg);
    }

    @Override
    public void info(String format, Object arg) {
        log.info(TEAM_LOGS, format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        log.info(TEAM_LOGS, format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        log.info(TEAM_LOGS, format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        log.info(TEAM_LOGS, msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return log.isInfoEnabled();
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void info(Marker marker, String msg) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void info(Marker marker, String format, Object arg) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void info(Marker marker, String format, Object... arguments) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void info(Marker marker, String msg, Throwable t) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        log.warn(TEAM_LOGS, msg);
    }

    @Override
    public void warn(String format, Object arg) {
        log.warn(TEAM_LOGS, format, arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        log.warn(TEAM_LOGS, format, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        log.warn(TEAM_LOGS, format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        log.warn(TEAM_LOGS, msg, t);
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public boolean isWarnEnabled(Marker marker) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void warn(Marker marker, String msg) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void warn(Marker marker, String format, Object arg) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void warn(Marker marker, String format, Object... arguments) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void warn(Marker marker, String msg, Throwable t) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void error(String msg) {
        log.error(TEAM_LOGS, msg);
    }

    @Override
    public void error(String format, Object arg) {
        log.error(TEAM_LOGS, format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        log.error(TEAM_LOGS, format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        log.error(TEAM_LOGS, format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        log.error(TEAM_LOGS, msg, t);
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public boolean isErrorEnabled(Marker marker) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void error(Marker marker, String msg) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void error(Marker marker, String format, Object arg) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void error(Marker marker, String format, Object... arguments) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }

    @Override
    @Deprecated(since = "Team logs kan ikke brukes med marker")
    public void error(Marker marker, String msg, Throwable t) {
        throw new UnsupportedOperationException("Team logs kan ikke brukes med marker");
    }
}
