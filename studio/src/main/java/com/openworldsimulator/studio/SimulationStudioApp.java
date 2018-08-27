package com.openworldsimulator.studio;

import com.openworldsimulator.demographics.DemographicsModel;
import com.openworldsimulator.economics.EconomyModel;
import com.openworldsimulator.experiments.Experiment;
import com.openworldsimulator.experiments.ExperimentsManager;
import com.openworldsimulator.simulation.ModelParameters;
import com.openworldsimulator.simulation.Simulation;
import com.openworldsimulator.tools.ConfigTools;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SimulationStudioApp extends AbstractVerticle {

    private static FreeMarkerTemplateEngine engine;

    private static ExperimentsManager experimentsManager = null;

    private static ExperimentsManager getExperimentsManager() {
        if (experimentsManager == null) {
            System.out.println("Creating experiment manager");
            try {
                experimentsManager = new ExperimentsManager(getOutputDir());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return experimentsManager;
    }

    private static File getOutputDir() {
        try {
            String dir = ConfigTools.getConfig("OPEN_WORLD_SIM_OUTPUT_DIR", "./output");
            File f = new File(dir);

            if (!f.exists() || !f.canWrite()) {
                throw new FileNotFoundException(f.getCanonicalPath());
            }

            return f.getCanonicalFile();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Experiment getExperiment(RoutingContext ctx, boolean createNew) throws IOException {

        Experiment experiment = null;

        String id = ctx.queryParams().get("id");
        if (id != null && !id.equals("")) {
            experiment = getExperimentsManager().loadExperiment(id);
        }
        if (experiment == null && createNew) {
            experiment = getExperimentsManager().newExperiment();
            if (id == null || id.length() > 3) {
                experiment.setExperimentId(id);
            }
        }

        return experiment;
    }

    private static void render(String template, RoutingContext ctx) {
        ctx.response().putHeader("content-type", "text/html");
        // and now delegate to the engine to render it.
        ctx.put("template", template);

        engine.render(ctx, template, res -> {
            if (res.succeeded()) {
                ctx.response().end(res.result());
            } else {
                ctx.fail(res.cause());
            }
        });
    }

    //------------------------------------------------------------------

    private static void pageConfiguration(RoutingContext ctx) {

        try {
            Experiment experiment = getExperiment(ctx, true);

            ctx.put("experiments", getExperimentsManager().listExperiments());
            ctx.put("experiment", experiment);
            ctx.put("configs", Arrays.asList("blank.defaults", "Spain.defaults"));
        } catch (IOException e) {
            ctx.fail(e);
            return;
        }

        render("templates/configuration.ftl", ctx);
    }

    private static void actionSaveConfiguration(RoutingContext ctx) {
        try {
            Experiment experiment = getExperiment(ctx, true);

            // Parse request
            boolean validated = true;

            String baseSettings = ctx.queryParams().get("baseConfiguration");

            int nMonths = 0;
            try {
                nMonths = NumberFormat.getInstance().parse(ctx.queryParams().get("nMonths")).intValue();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            validated = experiment.isValidId();

            if (validated) {
                experiment.setBaseSimulationConfig(baseSettings);
                experiment.setMonths(nMonths);

                getExperimentsManager().saveExperiment(experiment);

                ctx.put("success", Boolean.TRUE);

                pageConfiguration(ctx);
            } else {
                ctx.put("success", Boolean.FALSE);
                pageConfiguration(ctx);
            }
        } catch (Exception e) {
            ctx.fail(e);
        }
    }


    //-------------------------------------------------


    private static void pageParameters(RoutingContext ctx) {
        try {
            Experiment experiment = getExperiment(ctx, false);
            if (experiment == null) {
                ctx.fail(404);
                return;
            }

            ctx.put("experiments", getExperimentsManager().listExperiments());
            ctx.put("experiment", experiment);

            ModelParameters demographyParams = experiment.getSimulation().getModel(DemographicsModel.MODEL_ID).getParams();

            // Pass all model parameters
            ctx.put("INITIAL_DEMOGRAPHY_DATA_COUNTRY", demographyParams.getParameterValueString("INITIAL_DEMOGRAPHY_DATA_COUNTRY"));
            ctx.put("INITIAL_DEMOGRAPHY_DATA_YEAR", demographyParams.getParameterValueDouble("INITIAL_DEMOGRAPHY_DATA_YEAR"));

            ctx.put("demography", demographyParams.getParameterMapForDouble());
            ctx.put("economy", experiment.getSimulation().getModel(EconomyModel.MODEL_ID).getParams().getParameterMapForDouble());

        } catch (Exception e) {
            ctx.fail(e);
        }
        render("templates/parameters.ftl", ctx);
    }

    private static void actionSaveParameters(RoutingContext ctx) {

        try {
            Experiment experiment = getExperiment(ctx, false);
            if (experiment == null) {
                ctx.fail(404);
                return;
            }

            Map optionalParameters = new HashMap();
            optionalParameters.clear();
            ctx.queryParams().forEach(
                    entry -> {
                        if (entry.getValue() != null) {
                            //System.out.println(entry.getKey() + "=" + entry.getValue());
                            optionalParameters.put(entry.getKey(), entry.getValue());
                        }
                    });

            // Non Double parameters
            int year = 0;
            String country = null;
            try {
                country = ctx.queryParams().get("INITIAL_DEMOGRAPHY_DATA_COUNTRY");
                if (country != null) {
                    optionalParameters.put("INITIAL_DEMOGRAPHY_DATA_COUNTRY", country);
                }
                String yearParam = ctx.queryParams().get("INITIAL_DEMOGRAPHY_DATA_YEAR");
                if (yearParam != null) {
                    year = NumberFormat.getInstance().parse(yearParam).intValue();
                    optionalParameters.put("INITIAL_DEMOGRAPHY_DATA_YEAR", year);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (country != null && country.length() > 3 && year > 1900) {
                experiment.setOptionalProperties(optionalParameters);
                getExperimentsManager().saveExperiment(experiment);

                ctx.put("success", Boolean.TRUE);
                pageParameters(ctx);
            } else {
                ctx.put("success", Boolean.FALSE);
                pageParameters(ctx);
            }
        } catch (Exception e) {
            ctx.fail(e);
        }
    }

    //------------------------------------------------------------------

    private static void pageExperimentResults(RoutingContext ctx) {

        /*
        ctx.vertx().executeBlocking(
                future -> {
                    // EXECUTE SIMULATION
                    try {
                        //experiment.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    future.complete();
                },
                result -> {
                }
        );
        */
        render("templates/results.ftl", ctx);
    }

    private static void pageExperimentStatus(RoutingContext ctx) {
        try {
            Experiment experiment = getExperiment(ctx, false);
            if (experiment == null) {
                ctx.fail(404);
                return;
            }

            ctx.put("experiments", getExperimentsManager().listExperiments());
            ctx.put("experiment", experiment);
            Simulation simulation = experiment.getSimulation();
            ctx.put("simulation", simulation);

            render("templates/status.ftl", ctx);
        } catch (Exception e) {
            ctx.fail(e);
        }
    }

    private static void actionExecute(RoutingContext ctx) {
        try {
            Experiment experiment = getExperiment(ctx, false);
            if (experiment == null) {
                ctx.fail(404);
                return;
            }

            ctx.put("experiments", getExperimentsManager().listExperiments());
            ctx.put("experiment", experiment);
            Simulation simulation = experiment.getSimulation();
            ctx.put("simulation", simulation);

            if( !simulation.isRunning()) {
                ctx.vertx().executeBlocking(
                        future -> {
                            // EXECUTE SIMULATION
                            try {
                                experiment.run();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            future.complete();
                        },
                        result -> {
                            // Do nothing
                            System.out.println("ENDED!");
                        }
                );
            }

            render("templates/status.ftl", ctx);
        } catch (Exception e) {
            ctx.fail(e);
        }
    }


    @Override
    public void start() throws Exception {

        // In order to use a template we first need to create an engine
        engine = FreeMarkerTemplateEngine.create();

        Router router = Router.router(vertx);

        router.route("/").handler(SimulationStudioApp::pageConfiguration);
        router.route("/configuration").handler(SimulationStudioApp::pageConfiguration);
        router.route("/save-configuration").handler(SimulationStudioApp::actionSaveConfiguration);
        router.route("/parameters").handler(SimulationStudioApp::pageParameters);
        router.route("/save-parameters").handler(SimulationStudioApp::actionSaveParameters);
        router.route("/status").handler(SimulationStudioApp::pageExperimentStatus);
        router.route("/execute").handler(SimulationStudioApp::actionExecute);


        router.route("/results").handler(SimulationStudioApp::pageExperimentResults);
        router.route("/static/*").handler(StaticHandler.create().setCachingEnabled(false));

        File outputDir = getOutputDir();
        if (!outputDir.exists()) {
            System.out.println("# Creating output dir: " + getOutputDir().getCanonicalPath());
            outputDir.mkdirs();
        }

        File markerFile = new File(outputDir, ExperimentsManager.MARKER_FILE);
        if (!markerFile.exists()) {
            System.out.println("# Creating experiments marker file: " + markerFile.getCanonicalPath());
            FileUtils.write(markerFile, "", Charset.defaultCharset());
        }
        System.out.println("# Found " + getExperimentsManager().listExperiments().size() + " experiments");
        System.out.println("# Listening at 8080");

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8080);
    }

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Consumer<Vertx> runner = vertx -> {
            vertx.deployVerticle(new SimulationStudioApp());
        };

        VertxOptions vertxOptions = new VertxOptions();

        Vertx vertx = Vertx.vertx(vertxOptions);

        runner.accept(vertx);
    }
}
