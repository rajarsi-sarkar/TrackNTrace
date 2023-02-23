package com.hz.infrastructure.logs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppLogger {

    private static final Logger mLog = LogManager.getLogger(AppLogger.class.getName());

    public static void logInfo(String aMessage)
    {
        mLog.info(aMessage);
    }

    public static void logError(String aMessage)
    {
        mLog.error(aMessage);
    }
}
