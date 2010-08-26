/*
 * Copyright 2006-2008 the original author or authors.
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
 *
 */
package org.springframework.osgi.samples.weather.test;

import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.springframework.osgi.samples.weather.service.WeatherService;

/**
 */
public class WeatherServiceTest extends TestCase {

  private WeatherService ws;

  public void testSample2() throws Exception {

    Double high = ws.getHistoricalHigh(new GregorianCalendar(2004, 0, 1).getTime());
    //  ... do more validation of returned value here, this test is not realistic
    System.out.println("High was: " + high);
  }

  public void setWeatherService(WeatherService ws) {
    this.ws = ws;
  }
}
