/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.histogram;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.json.Json;
import javax.json.JsonObject;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.StringReader;

import java.net.URL;
import java.util.stream.Stream;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class WeatherServiceTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(WeatherService.class)
                .addAsWebInfResource(new StringAsset("<beans/>"), "beans.xml");
        return webArchive;
    }

    @ArquillianResource
    private URL base;

    private Client client;

    @Before
    public void before() {
        this.client = ClientBuilder.newClient();
    }

    @After
    public void after() {
        this.client.close();
    }

    @Test
    public void testHistogramMetric() {
        WebTarget webTarget = this.client.target(this.base.toExternalForm());
        final String message =  webTarget.path("/weather/histogram")
                .request()
                .get(String.class);
        final String metricPath = "/metrics/application";
        assertPrometheusFormat(metricPath);
        assertJsonFormat(metricPath);
    }

    private void assertPrometheusFormat(final String metricPath) {
        WebTarget webTarget = this.client.target(this.base.toExternalForm());
        final String metric = webTarget.path(metricPath)
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .get(String.class);
        final String expected = "# TYPE application:temperatures_degrees F summary histogram\n" +
                "# TYPE application:temperatures_degrees F_count histogram\n" +
                "application:temperatures_degrees F_count 15.0\n" +
                "# TYPE application:temperatures_min_degrees F histogram\n" +
                "application:temperatures_min_degrees F 27.0\n" +
                "# TYPE application:temperatures_max_degrees F histogram\n" +
                "application:temperatures_max_degrees F 55.0\n" +
                "# TYPE application:temperatures_mean_degrees F histogram\n" +
                "application:temperatures_mean_degrees F 44.4\n" +
                "# TYPE application:temperatures_stddev_degrees F histogram\n" +
                "application:temperatures_stddev_degrees F 7.0710678118654755\n" +
                "# TYPE application:temperatures_degrees F histogram\n" +
                "application:temperatures_degrees F{quantile=\"0.5\"} 45.0\n" +
                "# TYPE application:temperatures_degrees F histogram\n" +
                "application:temperatures_degrees F{quantile=\"0.75\"} 46.0\n" +
                "# TYPE application:temperatures_degrees F histogram\n" +
                "application:temperatures_degrees F{quantile=\"0.95\"} 54.0\n" +
                "# TYPE application:temperatures_degrees F histogram\n" +
                "application:temperatures_degrees F{quantile=\"0.98\"} 54.0\n" +
                "# TYPE application:temperatures_degrees F histogram\n" +
                "application:temperatures_degrees F{quantile=\"0.99\"} 54.0\n" +
                "# TYPE application:temperatures_degrees F histogram\n" +
                "application:temperatures_degrees F{quantile=\"0.999\"} 54.0\n" +
                "# TYPE application:org_superbiz_histogram_weather_service_temperatures summary histogram\n" +
                "# TYPE application:org_superbiz_histogram_weather_service_temperatures_count histogram\n" +
                "application:org_superbiz_histogram_weather_service_temperatures_count 0.0\n" +
                "# TYPE application:org_superbiz_histogram_weather_service_temperatures_min histogram\n" +
                "application:org_superbiz_histogram_weather_service_temperatures_min 0.0\n" +
                "# TYPE application:org_superbiz_histogram_weather_service_temperatures_max histogram\n" +
                "application:org_superbiz_histogram_weather_service_temperatures_max 0.0\n" +
                "# TYPE application:org_superbiz_histogram_weather_service_temperatures_mean histogram\n" +
                "application:org_superbiz_histogram_weather_service_temperatures_mean NaN\n" +
                "# TYPE application:org_superbiz_histogram_weather_service_temperatures_stddev histogram\n" +
                "application:org_superbiz_histogram_weather_service_temperatures_stddev 0.0\n" +
                "# TYPE application:org_superbiz_histogram_weather_service_temperatures histogram\n" +
                "application:org_superbiz_histogram_weather_service_temperatures{quantile=\"0.5\"} 0.0\n" +
                "# TYPE application:org_superbiz_histogram_weather_service_temperatures histogram\n" +
                "application:org_superbiz_histogram_weather_service_temperatures{quantile=\"0.75\"} 0.0\n" +
                "# TYPE application:org_superbiz_histogram_weather_service_temperatures histogram\n" +
                "application:org_superbiz_histogram_weather_service_temperatures{quantile=\"0.95\"} 0.0\n" +
                "# TYPE application:org_superbiz_histogram_weather_service_temperatures histogram\n" +
                "application:org_superbiz_histogram_weather_service_temperatures{quantile=\"0.98\"} 0.0\n" +
                "# TYPE application:org_superbiz_histogram_weather_service_temperatures histogram\n" +
                "application:org_superbiz_histogram_weather_service_temperatures{quantile=\"0.99\"} 0.0\n" +
                "# TYPE application:org_superbiz_histogram_weather_service_temperatures histogram\n" +
                "application:org_superbiz_histogram_weather_service_temperatures{quantile=\"0.999\"} 0.0\n";
        assertEquals(expected, metric);
    }

    private void assertJsonFormat(final String metricPath) {
        WebTarget webTarget = this.client.target(this.base.toExternalForm());
        final String metric = webTarget.path(metricPath)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);
        final String expected = "{\"temperatures\":{\"count\":15,\"max\":55,\"mean\":44.4,\"min\":27,\"p50\":45.0,\"p75\":46.0,\"p95\":54.0,\"p98\":54.0,\"p99\":54.0,\"p999\":54.0,\"stddev\":7.0710678118654755,\"unit\":\"degrees F\"},\"org.superbiz.histogram.WeatherService.temperatures\":{\"count\":0,\"max\":0,\"min\":0,\"p50\":0.0,\"p75\":0.0,\"p95\":0.0,\"p98\":0.0,\"p99\":0.0,\"p999\":0.0,\"stddev\":0.0,\"unit\":\"none\"}}";
        assertEquals(expected, metric);
    }

    @Test
    public void testHistogramMetricMetadata() {
        WebTarget webTarget = this.client.target(this.base.toExternalForm());
        final Response response = webTarget
                .path("/metrics/application")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .options();
        final String metaData = response.readEntity(String.class);

        JsonObject metadataJson = Json.createReader(new StringReader(metaData)).readObject();
        final String expected = "{\"temperatures\":{\"description\":\"A histogram of recent New York temperatures.\",\"displayName\":\"temperatures\",\"name\":\"temperatures\",\"reusable\":false,\"tags\":\"\",\"type\":\"histogram\",\"typeRaw\":\"HISTOGRAM\",\"unit\":\"degrees F\"},\"org.superbiz.histogram.WeatherService.temperatures\":{\"description\":\"A histogram metrics example.\",\"displayName\":\"Histogram of Recent New York Temperatures\",\"name\":\"org.superbiz.histogram.WeatherService.temperatures\",\"reusable\":false,\"tags\":\"\",\"type\":\"histogram\",\"typeRaw\":\"HISTOGRAM\",\"unit\":\"none\"}}";

        JsonObject expectedJson = Json.createReader(new StringReader(expected)).readObject();
        assertEquals(expectedJson, metadataJson);
        assertEquals(expectedJson.keySet().size(), metadataJson.keySet().size());
    }
}
