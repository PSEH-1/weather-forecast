package com.sapient.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;

@Controller
public class WeatherController {
    @GetMapping(value = "/weather/forecast", produces = "application/json")
    @ResponseBody

    public ResponseEntity<String> forecast(@RequestParam String city) throws JSONException, IOException {
        String url = "http://api.openweathermap.org/data/2.5/forecast?q="+city+",us&mode=json&appid=d2929e9483efc82c82c32ee7e02d563e";
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(url, String.class);
        JSONObject returnJSON = new JSONObject();

        Map<String,Object> resultMap = new ObjectMapper().readValue(result, HashMap.class);
        final List<Map<String,Object>> weatherList = (List<Map<String, Object>>) resultMap.get("list");
        Date today = new Date();
        LocalDate referenceDate = today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        //LocalDate referenceDate = LocalDate.parse("1970-01-18");
        //Days.daysBetween(start, end).getDays()
        weatherList.stream().filter(stringObjectMap -> {
            LocalDate listDate = LocalDate.parse(StringUtils.substringBefore((String)stringObjectMap.get("dt_txt")," "));//Instant.ofEpochMilli(longDate).atZone(ZoneId.of("UTC")).toLocalDate();
            if(Long.valueOf(ChronoUnit.DAYS.between(referenceDate,listDate)).compareTo(Long.valueOf(3)) < 0){
                return true;
            } else {
                return false;
            }
        }).forEach(stringObjectMap -> {
            try {
                LocalDate listDate = LocalDate.parse(StringUtils.substringBefore((String)stringObjectMap.get("dt_txt")," "));
                List<Map<String,Object>> weather = (List<Map<String, Object>>) stringObjectMap.get("weather");
                Map<String,Object> main = (Map<String,Object>) stringObjectMap.get("main");
                if(returnJSON.has(listDate.toString())){
                    JSONObject dateWeather = returnJSON.getJSONObject(listDate.toString());
                    forecastRain(weather, dateWeather);
                    forecastHeat(Double.valueOf(String.valueOf(main.get("temp_max"))),dateWeather);
                    maxMinTemp(Double.valueOf(String.valueOf(main.get("temp_min"))),Double.valueOf(String.valueOf(main.get("temp_min"))),dateWeather);
                }else{
                    JSONObject daysWeather = new JSONObject();
                    forecastRain(weather,daysWeather);
                    forecastHeat(Double.valueOf(String.valueOf(main.get("temp_max"))),daysWeather);
                    maxMinTemp(Double.valueOf(String.valueOf(main.get("temp_min"))),Double.valueOf(String.valueOf(main.get("temp_min"))),daysWeather);
                    returnJSON.put(listDate.toString(),daysWeather);
                }
            } catch (JSONException e) {
                System.out.println("Error while converting to JSON..."+e.getMessage());
            }
        });
        return ResponseEntity.ok(returnJSON.toString());
    }

    private void forecastRain(List<Map<String, Object>> weather, JSONObject dateWeather) throws JSONException {
        if(!dateWeather.has("carryUmbrella") || (dateWeather.has("carryUmbrella") &&!dateWeather.getBoolean("carryUmbrella"))){
            boolean willRain = weather.stream().anyMatch(weatherMap -> {
                if("Rain".equalsIgnoreCase((String)weatherMap.get("main"))) {
                    return true;
                } else {
                    return false;
                }
            });
            dateWeather.put("carryUmbrella",willRain);
        }
    }

    private void forecastHeat(Double maxTemp, JSONObject dateWeather) throws JSONException {
        if(!dateWeather.has("useSunscreen") || (dateWeather.has("useSunscreen") &&!dateWeather.getBoolean("useSunscreen"))){
            boolean useSunscreen = maxTemp.compareTo(Double.valueOf(313.15)) > 0 ? true : false;
            dateWeather.put("useSunscreen",useSunscreen);
        }
    }

    private void maxMinTemp(Double maxTemp,Double minTemp, JSONObject dateWeather) throws JSONException {
        if(!dateWeather.has("maxTemp")){
            dateWeather.put("maxTemp",maxTemp);
        } else {
            double maxAvailTemp = dateWeather.getDouble("maxTemp");
            dateWeather.put("maxTemp",maxTemp.compareTo(maxAvailTemp) >= 0 ? maxTemp : maxAvailTemp);
        }

        if(!dateWeather.has("minTemp")){
            dateWeather.put("minTemp",minTemp);
        } else {
            double minAvailTemp = dateWeather.getDouble("minTemp");
            dateWeather.put("minTemp",minTemp.compareTo(minAvailTemp) <= 0 ? minTemp : minAvailTemp);
        }
    }
}
