package com.liveramp.workflow_ui.util.prime;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.TimerTask;

import com.google.common.collect.Multimap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Prime extends TimerTask {
  private static final Logger LOG = LoggerFactory.getLogger(Prime.class);

  private Multimap<String, RequestBuilder> requestsToBuild;
  private final String rootUrl;

  public Prime(String rootUrl, Multimap<String, RequestBuilder> builders) {
    this.rootUrl = rootUrl;
    this.requestsToBuild = builders;
  }

  @Override
  public void run() {
    try {
      prime();
    } catch (URISyntaxException e) {
      LOG.error("Could not prime!", e);
    }
  }

  private void prime() throws URISyntaxException {

    for (Map.Entry<String, RequestBuilder> entry : requestsToBuild.entries()) {
      String key = entry.getKey();
      RequestBuilder builder = entry.getValue();

      try {
        URIBuilder uriBuilder = new URIBuilder().setScheme("http").setHost(rootUrl).setPath(key);
        for (Map.Entry<String, String> param : builder.getRequestParams().entrySet()) {
          uriBuilder.addParameter(param.getKey(), param.getValue());
        }

        URL url = uriBuilder.build().toURL();
        LOG.info("Priming URL " + url);

        IOUtils.copy(url.openStream(), new NullOutputStream());
      } catch (IOException e) {
        LOG.error("Error priming " + key, e);
      }
    }

  }

}
