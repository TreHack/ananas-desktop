package org.ananas.runner.kernel.pipeline;

import org.ananas.runner.kernel.model.Engine;
import org.apache.beam.runners.flink.FlinkPipelineOptions;
import org.apache.beam.runners.flink.FlinkRunner;
import org.apache.beam.runners.spark.SparkPipelineOptions;
import org.apache.beam.runners.spark.SparkRunner;
import org.apache.beam.sdk.options.PipelineOptions;

public class PipelineOptionsFactory {

  public static PipelineOptions create(boolean isTest, Engine engine) {
    if (isTest) {
      return createFlinkOptions(null);
    }

    switch (engine.type.toLowerCase()) {
      case "flink":
        return createFlinkOptions(engine);
      case "spark":
        return createSparkOptions(engine);
      default:
        return createFlinkOptions(null);
    }
  }

  public static PipelineOptions createFlinkOptions(Engine engine) {
    FlinkPipelineOptions options =
        org.apache.beam.sdk.options.PipelineOptionsFactory.create().as(FlinkPipelineOptions.class);
    if (engine == null) {
      options.setParallelism(10);
      options.setMaxBundleSize(1000 * 1000L);
      options.setObjectReuse(true);
    } else {
      options.setFlinkMaster(engine.getProperty("flinkMaster", "[auto]"));
      options.setTempLocation(engine.getProperty("tempLocation", "/tmp/"));
      options.setParallelism(engine.getProperty("parallelism", Integer.valueOf(10)));
      options.setMaxBundleSize(engine.getProperty("maxBundleSize", Long.valueOf(1000 * 1000L)));
      options.setObjectReuse(engine.getProperty("objectReuse", Boolean.TRUE));
    }

    options.setAppName(engine.getProperty(Engine.APP_NAME, "ananas"));
    options.setRunner(FlinkRunner.class);
    return options;
  }

  public static PipelineOptions createSparkOptions(Engine engine) {
    SparkPipelineOptions options =
        org.apache.beam.sdk.options.PipelineOptionsFactory.create().as(SparkPipelineOptions.class);
    options.setSparkMaster(engine.getProperty("sparkMaster", "spark://localhost:7077"));
    options.setTempLocation(engine.getProperty("tempLocation", "/tmp/"));
    options.setStreaming(engine.getProperty("streaming", Boolean.FALSE));
    options.setEnableSparkMetricSinks(engine.getProperty("enableMetricSinks", Boolean.TRUE));

    options.setAppName(engine.getProperty(Engine.APP_NAME, "ananas"));
    options.setRunner(SparkRunner.class);

    return options;
  }
}
