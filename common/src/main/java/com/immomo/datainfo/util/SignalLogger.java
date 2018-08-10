package com.immomo.datainfo.util;

import org.apache.commons.logging.Log;
import sun.misc.Signal;
import sun.misc.SignalHandler;
import com.immomo.datainfo.annotations.classification.InterfaceAudience;
import com.immomo.datainfo.annotations.classification.InterfaceStability;

/**
 * This class logs a message whenever we're about to exit on a UNIX signal.
 * This is helpful for determining the root cause of a process' exit.
 * For example, if the process exited because the system administrator
 * ran a standard "kill," you would see 'EXITING ON SIGNAL SIGTERM' in the log.
 */
@InterfaceAudience.Private
@InterfaceStability.Unstable
public enum SignalLogger {
    INSTANCE;

    private boolean registered = false;

    /**
     * Our signal handler.
     */
    private static class Handler implements SignalHandler {
        final private org.apache.commons.logging.Log LOG;
        final private SignalHandler prevHandler;

        Handler(String name, Log LOG) {
            this.LOG = LOG;
            prevHandler = Signal.handle(new Signal(name), this);
        }

        /**
         * Handle an incoming signal.
         *
         * @param signal    The incoming signal
         */
        @Override
        public void handle(Signal signal) {
            LOG.error("RECEIVED SIGNAL " + signal.getNumber() +
                    ": SIG" + signal.getName());
            prevHandler.handle(signal);
        }
    }

    /**
     * Register some signal handlers.
     *
     * @param LOG        The log4j logfile to use in the signal handlers.
     */
    public void register(final Log LOG) {
        if (registered) {
            throw new IllegalStateException("Can't re-install the signal handlers.");
        }
        registered = true;
        StringBuilder bld = new StringBuilder();
        bld.append("registered UNIX signal handlers for [");
        final String SIGNALS[] = { "TERM", "HUP", "INT" };
        String separator = "";
        for (String signalName : SIGNALS) {
            try {
                new Handler(signalName, LOG);
                bld.append(separator);
                bld.append(signalName);
                separator = ", ";
            } catch (Exception e) {
                LOG.debug(e);
            }
        }
        bld.append("]");
        LOG.info(bld.toString());
    }
}
