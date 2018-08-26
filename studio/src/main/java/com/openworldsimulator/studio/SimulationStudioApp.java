package com.openworldsimulator.studio;

import com.openworldsimulator.demographics.DemographicsModel;
import com.openworldsimulator.economics.EconomyModel;
import com.openworldsimulator.experiments.Experiment;
import com.openworldsimulator.experiments.ExperimentsManager;
import com.openworldsimulator.simulation.ModelParameters;
import com.openworldsimulator.tools.ConfigTools;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SimulationStudioApp extends AbstractVerticle {

    private static FreeMarkerTemplateEngine engine;
    private static File outputDir;

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
            // TODO: Make this optionally configurable by system properties
            String dir = ConfigTools.getConfig("OPEN_WORLD_SIM_OUTPUT_DIR", "./output");

            File f = new File(dir);
            System.out.println("Output Dir is " + f.getCanonicalPath());

            if (!f.exists() || !f.canWrite()) {
                throw new FileNotFoundException(f.getCanonicalPath());
            }

            return f.getCanonicalFile();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void pageConfigureExperiment(RoutingContext ctx) {

        String id = ctx.queryParams().get("id");
        Experiment experiment = null;
        try {
            if (id != null && !id.equals("")) {
                experiment = getExperimentsManager().loadExperiment(id);
            }

            if (experiment == null) {
                // Todo: pass parameters
                experiment = getExperimentsManager().newExperiment();
            }
        } catch (IOException e) {
            e.printStackTrace();
            ctx.fail(e);
        }

        try {
            ctx.put("experiments", getExperimentsManager().listExperiments());
            ctx.put("configs", getExperimentsManager().getAvailableBaseConfigurations());

            ctx.put("experiment", experiment);

            ModelParameters demographyParams = experiment.getSimulation().getModel(DemographicsModel.MODEL_ID).getParams();

            // Pass all model parameters
            ctx.put("INITIAL_DEMOGRAPHY_DATA_COUNTRY", demographyParams.getParameterValueString("INITIAL_DEMOGRAPHY_DATA_COUNTRY"));
            ctx.put("INITIAL_DEMOGRAPHY_DATA_YEAR", demographyParams.getParameterValueDouble("INITIAL_DEMOGRAPHY_DATA_YEAR"));

            ctx.put("demography", demographyParams.getParameterMapForDouble());
            ctx.put("economy", experiment.getSimulation().getModel(EconomyModel.MODEL_ID).getParams().getParameterMapForDouble());

        } catch (Exception e) {
            e.printStackTrace();
            ctx.fail(e);
        }
        render("templates/configure.ftl", ctx);
    }

    private static void actionSaveExperiment(RoutingContext ctx) {

        // Parse request
        boolean validated = true;

        String baseSettings = ctx.queryParams().get("baseConfiguration");

        int nMonths = 0;
        try {
            nMonths = NumberFormat.getInstance().parse(ctx.queryParams().get("nMonths")).intValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String experimentId = ctx.queryParams().get("id");

        int year = 0;
        try {
            year = NumberFormat.getInstance().parse(ctx.queryParams().get("INITIAL_DEMOGRAPHY_DATA_COUNTRY")).intValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        Map optionalParameters = new HashMap();
        optionalParameters.clear();
        ctx.queryParams().forEach(
                entry -> {
                    if (entry.getValue() != null) {
                        System.out.println(entry.getKey() + "=" + entry.getValue());
                        optionalParameters.put(entry.getKey(), entry.getValue());
                    }
                });


        System.out.println("EXPERIMENT ID: " + experimentId);

        validated = experimentId != null && year > 1900 && nMonths > 0;

        if (validated) {

            try {
                Experiment existingExperiment = getExperimentsManager().loadExperiment(experimentId);
                Experiment experiment = existingExperiment == null ? getExperimentsManager().newExperiment() : existingExperiment;

                experiment.setExperimentId(experimentId);
                experiment.setBaseSimulationConfig(baseSettings);
                experiment.setOptionalProperties(optionalParameters);
                experiment.setMonths(nMonths);

                getExperimentsManager().saveExperiment(experiment);

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

            } catch (Exception e) {
                e.printStackTrace();
                ctx.fail(e);
            }
            ctx.put("success", Boolean.TRUE);
            pageConfigureExperiment(ctx);
        } else {
            ctx.put("success", Boolean.FALSE);
            pageConfigureExperiment(ctx);
        }
    }

    private static void pageExperimentResults(RoutingContext ctx) {

        render("templates/results.ftl", ctx);
    }

    private static void pageExperimentStatus(RoutingContext ctx) {

        render("templates/status.ftl", ctx);
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

    @Override
    public void start() throws Exception {

        // In order to use a template we first need to create an engine
        engine = FreeMarkerTemplateEngine.create();
        System.out.println(engine.isCachingEnabled());


        // TODO: Make this automatically configurable
        File f = new File(".", "output");
        if (!f.exists() || !f.canWrite()) {
            throw new FileNotFoundException(f.getCanonicalPath());
        }

        outputDir = f;
        System.out.println("Output path is " + outputDir.getCanonicalPath());

        Router router = Router.router(vertx);

        router.route("/").handler(SimulationStudioApp::pageConfigureExperiment);
        router.route("/configure").handler(SimulationStudioApp::pageConfigureExperiment);
        router.route("/save").handler(SimulationStudioApp::actionSaveExperiment);
        router.route("/status").handler(SimulationStudioApp::pageExperimentStatus);
        router.route("/results").handler(SimulationStudioApp::pageExperimentResults);
        router.route("/static/*").handler(StaticHandler.create().setCachingEnabled(false));

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
