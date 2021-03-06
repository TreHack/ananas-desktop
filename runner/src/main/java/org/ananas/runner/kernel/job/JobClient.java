package org.ananas.runner.kernel.job;

import java.io.IOException;
import org.ananas.runner.kernel.model.DagRequest;

public interface JobClient {

  String createJob(String projectId, String token, DagRequest dagRequest) throws IOException;

  void updateJobState(String jobId) throws IOException;
}
