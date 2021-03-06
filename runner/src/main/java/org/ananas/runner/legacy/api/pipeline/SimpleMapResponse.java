package org.ananas.runner.legacy.api.pipeline;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpleMapResponse {
  public SimpleMapResponse() {}

  public Integer code;
  public SimpleData data;
}
