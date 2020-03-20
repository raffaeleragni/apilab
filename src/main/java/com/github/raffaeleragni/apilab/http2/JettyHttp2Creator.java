/*
 * Copyright 2019 Raffaele Ragni.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.raffaeleragni.apilab.http2;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.jetty.JettyStatisticsCollector;
import java.io.IOException;
import static java.util.Optional.ofNullable;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * Creates a http2 server.
 * @author Raffaele Ragni
 */
public class JettyHttp2Creator {

  private JettyHttp2Creator() {
  }

  static HTTPServer metricServer;
  
  /**
   * Starts the metrics server.
   * Port is set via env JAVALIN_PROMETHEUS_PORT.
   * @throws IOException exception on prometheus port
   */
  public static void startMetrics() throws IOException {
    if (metricServer != null) {
      metricServer.stop();
    }
    metricServer = new HTTPServer(getPrometheusPort());
  }
  
  /**
   * Stops the metrics server.
   */
  public static void stopMetrics() {
    if (metricServer != null) {
      metricServer.stop();
    }
  }

  /**
   * Creates a HTTP2 server including configuration for the https with certificate.
   * By default loads "/keystore.jks" with password "password"
   * Ports are: 8443 for https, 8080 for http.
   *
   * This addons library already contains a default "keystore.jks" for "localhost" for debug or internal modes.
   * If your load balancer already deals with correct certificates and protects the api internally,
   * you can use this local certificate internally and still support http/2: it's not worse than using plain http.
   *
   * These addons are made to be used inside containers, so doesn't matter much which port is the default.
   *
   * These variables can be changed through environment variables, defaults are:
   * - JAVALIN_HTTP2_PORT: 8080 (system property is javalinHttp2Port)
   * - JAVALIN_HTTPS2_PORT: 8443 (system property is javalinHttps2Port)
   * - JAVALIN_HTTPS2_CERT_CLASSPATH: "/keystore.jks" (system property is javalinHttps2CertClasspath)
   * - JAVALIN_HTTPS2_CERT_PASSWORD: "password" (system property is getHttps2CertPassword)
   *
   * System properties take over environment variables.
   *
   * @return Jetty server
   */
  public static Server createHttp2Server() {
    
    StatisticsHandler statisticsHandler = new StatisticsHandler();
    CollectorRegistry.defaultRegistry.clear();
    DefaultExports.initialize();
    new JettyStatisticsCollector(statisticsHandler).register();
    
    Server server = new Server();

    server.setHandler(statisticsHandler);
    
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(getHttp2Port());
    server.addConnector(connector);

    // HTTP Configuration
    HttpConfiguration httpConfig = new HttpConfiguration();
    httpConfig.setSendServerVersion(false);
    httpConfig.setSecureScheme("https");
    httpConfig.setSecurePort(getHttps2Port());

    // SSL Context Factory for HTTPS and HTTP/2
    SslContextFactory sslContextFactory = new SslContextFactory.Server();
    // replace with your real keystore
    sslContextFactory.setKeyStorePath(JettyHttp2Creator.class
      .getResource(getHttps2CertClasspath()).toExternalForm());
    // replace with your real password
    sslContextFactory.setKeyStorePassword(getHttps2CertPassword());
    sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);
    sslContextFactory.setProvider("Conscrypt");

    // HTTPS Configuration
    HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
    httpsConfig.addCustomizer(new SecureRequestCustomizer());

    // HTTP/2 Connection Factory
    HTTP2ServerConnectionFactory h2 = new HTTP2ServerConnectionFactory(httpsConfig);
    ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
    alpn.setDefaultProtocol("h2");

    // SSL Connection Factory
    SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory, alpn.getProtocol());

    // HTTP/2 Connector
    ServerConnector http2Connector = new ServerConnector(server, ssl, alpn, h2, new HttpConnectionFactory(httpsConfig));
    http2Connector.setPort(getHttps2Port());
    server.addConnector(http2Connector);

    return server;
  }

  private static int getPrometheusPort() {
    return ofNullable(System.getProperty("javalinPrometheusPort"))
          .map(Integer::valueOf)
          .orElseGet(() ->
            ofNullable(System.getenv("JAVALIN_PROMETHEUS_PORT"))
              .map(Integer::valueOf)
              .orElse(7080)
          );
  }
  
  private static int getHttp2Port() {
    return ofNullable(System.getProperty("javalinHttp2Port"))
          .map(Integer::valueOf)
          .orElseGet(() ->
            ofNullable(System.getenv("JAVALIN_HTTP2_PORT"))
              .map(Integer::valueOf)
              .orElse(8080)
          );
  }

  private static int getHttps2Port() {
    return ofNullable(System.getProperty("javalinHttps2Port"))
          .map(Integer::valueOf)
          .orElseGet(() ->
            ofNullable(System.getenv("JAVALIN_HTTPS2_PORT"))
              .map(Integer::valueOf)
              .orElse(8443)
          );
  }

  private static String getHttps2CertClasspath() {
    return ofNullable(System.getProperty("javalinHttps2CertClasspath"))
          .orElseGet(() ->
            ofNullable(System.getenv("JAVALIN_HTTPS2_CERT_CLASSPATH"))
              .orElse("/keystore.jks")
          );
  }

  private static String getHttps2CertPassword() {
    return ofNullable(System.getProperty("javalinHttps2CertPassword"))
          .orElseGet(() ->
            ofNullable(System.getenv("JAVALIN_HTTPS2_CERT_PASSWORD"))
              .orElse("password")
          );
  }
}
