package com.openworldsimulator.simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SimulationLog {
    private Simulation simulation;
    private FileWriter outputWriter;
    private String logFile;
    private boolean muted = false;
    private boolean debug = true;

    public SimulationLog(Simulation simulation, String logFile) throws IOException {
        this.simulation = simulation;
        this.logFile = logFile;
        outputWriter = new FileWriter(
                getOutputFile(), false);

    }

    private File getOutputFile() {
        return new File(simulation.getSimulationOutputPath(), logFile + ".log");
    }

    public void init() {
        System.out.println("Simulation output: " + getOutputFile().getPath());
    }

    public void logDebug(String text) {
        if( debug ) {
            log(text);
        }
    }

    public void log(String text) {
        if( muted ) return;

        if (outputWriter != null) {
            try {
                outputWriter.write(text + "\n");
                outputWriter.flush();
            } catch (IOException e) {
                System.out.println(text);
                e.printStackTrace();
            }
        }
    }

    public void logOut(String text) {
        if( muted ) return;
        log(text);
        System.out.println(text);
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

    public void close() throws IOException {
        if (outputWriter != null) {
            outputWriter.close();
        }
    }
}
