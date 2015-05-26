package com.genesys.gms.mobile.callback.demo.legacy.data.retrofit;

import android.util.Log;
import hugo.weaving.DebugLog;
import retrofit.Endpoint;

import javax.inject.Singleton;

/**
 * Custom Retrofit Endpoint for dynamic redefinition of endpoint address
 * without having to perform an expensive rebuild of the RestAdapter
 * See answer from JakeWharton: http://stackoverflow.com/a/23279628
 */
@Singleton
public class GmsEndpoint implements Endpoint {
  private static final String ENDPOINT_HOST = "endpoint_host";
  private static final String ENDPOINT_PORT = "endpoint_port";
  private static final String ENDPOINT_API_VERSION = "endpoint_api_version";
  private String m_strUrl;

  @DebugLog
  public GmsEndpoint() {
    super();
  }

  @Override
  public String getName() {
    return "default";
  }

  @DebugLog
  public void setUrl(String p_strHost, Integer p_nPort, Integer p_nApiVersion) {
    if (p_strHost == null || p_strHost.isEmpty() || p_nPort == null || p_nApiVersion == null) {
      m_strUrl = null;
    } else {
      m_strUrl = String.format("http://%s:%d/genesys/%d", p_strHost, p_nPort, p_nApiVersion);
    }
    Log.d("GmsEndpoint", "Endpoint URL: " + m_strUrl);
    //Timber.d("Endpoint URL: ", m_strUrl);
  }

  public boolean isUrlSet() {
    return m_strUrl != null;
  }

  @Override
  public String getUrl() {
    if (m_strUrl == null) throw new IllegalStateException("setUrl() has not been called");
    return m_strUrl;
  }
}