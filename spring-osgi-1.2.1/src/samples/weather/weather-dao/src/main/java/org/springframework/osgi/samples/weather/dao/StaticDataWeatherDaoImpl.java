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
package org.springframework.osgi.samples.weather.dao;

import java.util.Calendar;
import java.util.Date;

/**
 * Implementation of WeatherDao
 */
public class StaticDataWeatherDaoImpl implements WeatherDao {

  public StaticDataWeatherDaoImpl() {
  }

  public WeatherData find(Date date) {

    WeatherData wd = new WeatherData();
    wd.setDate((Date) date.clone());
    // some bogus values
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    wd.setLow(calendar.get(Calendar.MONTH) + 5);
    wd.setHigh(calendar.get(Calendar.MONTH) + 15);
    return wd;
  }

  public WeatherData save(Date date) {
    throw new UnsupportedOperationException("This class uses static data only");
  }

  /*
   * (non-Javadoc)
   * 
   * @see ch02.sample1.WeatherDao#update(java.util.Date)
   */
  public WeatherData update(Date date) {
    throw new UnsupportedOperationException("This class uses static data only");
  }
}