/*
 * Copyright 2006-2009 the original author or authors.
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

import java.util.Date;

/**
 * Data Access Object Interface for getting and storing weather records
 */
public interface WeatherDao {

  /**
   * Returns the WeatherData for a date, or null if there is none 
   * @param date the date to search on 
   */
  WeatherData find(Date date);

  /**
   * Saves the WeatherData for a date
   */
  WeatherData save(Date date);

  WeatherData update(Date date);
}