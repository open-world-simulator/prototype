package com.openworldsimulator.experiments;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ExperimentsManager {
    public static final String EXPERIMENTS_MARKER_FILE = ".experiments";
    private File baseDirectory;

    public ExperimentsManager(File baseDirectory) throws IOException {
        System.out.println("Experiments stored at " + baseDirectory);
        if (!new File(baseDirectory, EXPERIMENTS_MARKER_FILE).exists()) {
            throw new IOException(EXPERIMENTS_MARKER_FILE + " : File must be created in experiments dir " + baseDirectory.getCanonicalPath());
        }
        if (baseDirectory == null || !baseDirectory.isDirectory() || !baseDirectory.canRead() || !baseDirectory.canWrite()) {
            throw new IOException("Experiments Manager : directory " + baseDirectory + " is not a valid directory");
        }
        this.baseDirectory = baseDirectory;
    }

    public Experiment newExperiment() throws IOException {
        return new Experiment(this, null, null, null, 1200);
    }

    public void deleteAllExperiments() throws IOException {
        System.out.println("Deleting all experiments");
        for (Experiment e : listExperiments()) {
            deleteExperiment(e.getExperimentId());
        }
    }

    public void deleteExperiment(String id) throws IOException {
        File dir = getExperimentDirectory(id);

        if (dir != null && dir.canWrite()) {
            System.out.println("Deleting experiment " + id + " at " + dir);

            FileUtils.deleteDirectory(dir);
        }
    }

    public List<Experiment> listExperiments() {
        List<Experiment> experiments = new ArrayList<>();
        for (File f : FileUtils.listFilesAndDirs(getBaseDirectory(), FalseFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
            Experiment e = null;
            try {
                if (!f.equals(getBaseDirectory())) {
                    e = loadExperiment(f.getName());
                }
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
            if (e != null) {
                experiments.add(e);
            }
        }

        return experiments;
    }

    public Experiment loadExperiment(String experimentId) throws FileNotFoundException {
        File dir = getExperimentDirectory(experimentId);
        if (dir != null) {
            // load experiment
            return loadExperiment(dir);
        }
        return null;
    }

    private Experiment loadExperiment(File dir) throws FileNotFoundException {

        // load experiment
        File descriptor = new File(dir, getExperimentFileName());
        if (!descriptor.exists()) {
            System.out.println("File descriptor does not exist " + descriptor);
            return null;
        }

        System.out.println("Loading experiment from " + descriptor);
        Gson gson = new Gson();
        Experiment e = gson.fromJson(
                new FileReader(descriptor),
                Experiment.class
        );
        e.setExperimentsManager(this);

        return e;
    }

    public void saveExperiment(Experiment experiment) throws IOException {
        File dir = getExperimentDirectory(experiment.getExperimentId());
        if (dir == null || !dir.canWrite()) {
            throw new IOException("Can't write experiment " + experiment + " at directory " + dir);
        }
        File descriptor = new File(dir, getExperimentFileName());
        FileUtils.write(descriptor,
                new Gson().toJson(experiment),
                Charset.forName("UTF-8"));
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    private String getExperimentFileName() {
        // TODO: Filter out non letter-number;
        return "experiment.json";

    }

    public File getExperimentDirectory(String experimentId) {
        if( experimentId != null) {
            File dir = new File(getBaseDirectory(), experimentId);
            if (dir.exists() && dir.canRead()) {
                return dir;
            }
            if (!dir.exists()) {
                dir.mkdirs();
                return dir;
            }
        }
        return null;
    }
}
