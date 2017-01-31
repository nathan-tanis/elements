/*
Copyright 2015 Futeh Kao

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package net.e6tech.elements.common.logging.jul;



import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;


/**
 * Created by futeh.
 */
public class LoggerAdapter extends java.util.logging.Logger {

    net.e6tech.elements.common.logging.Logger logger;

    public LoggerAdapter(String name) {
        super(name, null);
        logger = net.e6tech.elements.common.logging.Logger.getLogger(name);
    }

    @Override
    public void log(final LogRecord record) {
        if (isFiltered(record)) {
            return;
        }
        Level level = record.getLevel();
        String message = record.getMessage();
        Object[] parameters = record.getParameters();
        final Throwable thrown = record.getThrown();
        log(level, message, parameters, thrown);
    }

    boolean isFiltered(final LogRecord logRecord) {
        final Filter filter = getFilter();
        return filter != null && !filter.isLoggable(logRecord);
    }

    public void log(Level level, String message, Object[] parameters, Throwable th) {
        log(level, message, th);
    }

    @Override
    public boolean isLoggable(final Level level) {
        if (Level.ALL.equals(level)) return true;
        else if (Level.CONFIG.equals(level)) logger.isInfoEnabled();
        return true;
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public void setLevel(final Level newLevel) throws SecurityException {
        throw new UnsupportedOperationException("Cannot set level through log4j-api");
    }

    @Override
    public void log(final Level level, final String msg) {
        if (Level.ALL.equals(level)) logger.info(msg);
        else if (Level.CONFIG.equals(level)) logger.info(msg);
        else if (Level.FINE.equals(level)) logger.debug(msg);
        else if (Level.FINER.equals(level)) logger.debug(msg);
        else if (Level.FINEST.equals(level)) logger.trace(msg);
        else if (Level.INFO.equals(level)) logger.info(msg);
        else if (Level.SEVERE.equals(level)) logger.error(msg);
        else if (Level.WARNING.equals(level)) logger.warn(msg);
    }

    @Override
    public void log(final Level level, final String msg, final Object param1) {
        if (Level.ALL.equals(level)) logger.info(msg, param1);
        else if (Level.CONFIG.equals(level)) logger.info(msg, param1);
        else if (Level.FINE.equals(level)) logger.debug(msg, param1);
        else if (Level.FINER.equals(level)) logger.debug(msg, param1);
        else if (Level.FINEST.equals(level)) logger.trace(msg, param1);
        else if (Level.INFO.equals(level)) logger.info(msg, param1);
        else if (Level.SEVERE.equals(level)) logger.error(msg, param1);
        else if (Level.WARNING.equals(level)) logger.warn(msg, param1);
    }

    @Override
    public void log(final Level level, final String msg, final Object[] params) {
        if (Level.ALL.equals(level)) logger.info(msg, params);
        else if (Level.CONFIG.equals(level)) logger.info(msg, params);
        else if (Level.FINE.equals(level)) logger.debug(msg, params);
        else if (Level.FINER.equals(level)) logger.debug(msg, params);
        else if (Level.FINEST.equals(level)) logger.trace(msg, params);
        else if (Level.INFO.equals(level)) logger.info(msg, params);
        else if (Level.SEVERE.equals(level)) logger.error(msg, params);
        else if (Level.WARNING.equals(level)) logger.warn(msg, params);
    }

    @Override
    public void log(final Level level, final String msg, final Throwable thrown) {
        if (Level.ALL.equals(level)) logger.info(msg, thrown);
        else if (Level.CONFIG.equals(level)) logger.info(msg, thrown);
        else if (Level.FINE.equals(level)) logger.debug(msg, thrown);
        else if (Level.FINER.equals(level)) logger.debug(msg, thrown);
        else if (Level.FINEST.equals(level)) logger.trace(msg, thrown);
        else if (Level.INFO.equals(level)) logger.info(msg, thrown);
        else if (Level.SEVERE.equals(level)) logger.error(msg, thrown);
        else if (Level.WARNING.equals(level)) logger.warn(msg, thrown);
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg) {
        log(level, msg);
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg,
                     final Object param1) {
        log(level, msg, param1);
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg,
                     final Object[] params) {
        log(level, msg, params);
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg,
                     final Throwable thrown) {
        log(level, msg, thrown);
    }

    /* Deprecated
    @Override
    public void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName,
                      final String msg) {
        log(level, msg);
    }

    @Override
    public void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName,
                      final String msg, final Object param1) {
        log(level, msg, param1);
    }

    @Override
    public void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName,
                      final String msg, final Object[] params) {
        log(level, msg, params);
    }

    @Override
    public void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName,
                      final String msg, final Throwable thrown) {
        log(level, msg, thrown);
    }*/

    @Override
    public void entering(final String sourceClass, final String sourceMethod) {

    }

    @Override
    public void entering(final String sourceClass, final String sourceMethod, final Object param1) {
    }

    @Override
    public void entering(final String sourceClass, final String sourceMethod, final Object[] params) {
    }

    @Override
    public void exiting(final String sourceClass, final String sourceMethod) {
    }

    @Override
    public void exiting(final String sourceClass, final String sourceMethod, final Object result) {
    }

    @Override
    public void throwing(final String sourceClass, final String sourceMethod, final Throwable thrown) {
    }

    @Override
    public void severe(final String msg) {
        logger.error(msg);
    }

    @Override
    public void warning(final String msg) {
        logger.warn(msg);
    }

    @Override
    public void info(final String msg) {
        logger.info(msg);
    }

    @Override
    public void config(final String msg) {
        logger.info(msg);
    }

    @Override
    public void fine(final String msg) {
        logger.debug(msg);
    }

    @Override
    public void finer(final String msg) {
        logger.debug(msg);
    }

    @Override
    public void finest(final String msg) {
        logger.trace(msg);
    }
}