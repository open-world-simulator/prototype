package com.openworldsimulator.simulation;

import java.io.IOException;

public class SimulationLog {
    private String simulationId;
    private boolean muted = false;
    private boolean debug = false;

    public SimulationLog(Simulation simulation, String logFile) throws IOException {
        this.simulationId = simulation.getSimulationId();

    }

    public void init() {

    }

    public void logDebug(String text) {
        if( debug ) {
            log(text);
        }
    }

    public void log(String text) {
        if( muted ) return;
        System.out.println("[" + simulationId + "] " + text);
    }

    public void log(String format, Object ... args) {
        log(String.format(format, args));
    }

    public void logDebug(String format, Object ... args) {
        if( debug) {
            log(String.format(format, args));
        }
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
