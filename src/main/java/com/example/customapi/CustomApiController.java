package com.example.customapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class CustomApiController {

    RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/custom")
    public List<Map<String, Object>> custom (
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        return callAPI(limit);
    }

    private List<Map<String, Object>> callAPI (Integer limit) {
        List<Map<String, Object>> maps = new ArrayList<>();

        // Get data from 3rd party
        ResponseEntity<JsonNode> resAll = restTemplate.exchange(
                limit == null ? "https://pokeapi.co/api/v2/pokemon/?limit=250" : "https://pokeapi.co/api/v2/pokemon/?limit=" + limit,
                HttpMethod.GET,
                null,
                JsonNode.class
        );
        ArrayNode resultAlls = (ArrayNode) resAll.getBody().get("results");

        // Custom data
        for (int i = 0; i < resultAlls.size(); i++) {
            int finalI = i;
            String url = resultAlls.get(finalI).get("url").asText();
            String no = url.split("/")[6];

            // Detail
            ResponseEntity<JsonNode> resDetail = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    JsonNode.class
            );

            String imgDefault = resDetail.getBody().get("sprites").get("front_default").asText();
            String imgShiny = resDetail.getBody().get("sprites").get("front_default").asText();
            int height = resDetail.getBody().get("height").asInt();
            int weight = resDetail.getBody().get("weight").asInt();

            // Form
            List<String> types = new ArrayList<>();
            String urlForm = resDetail.getBody().get("forms").get(0).get("url").asText();
            ResponseEntity<JsonNode> resForm = restTemplate.exchange(
                    urlForm,
                    HttpMethod.GET,
                    null,
                    JsonNode.class
            );

            ArrayNode forms = (ArrayNode) resForm.getBody().get("types");
            for (int j = 0; j < forms.size(); j++) {
                types.add(forms.get(j).get("type").get("name").asText());
            }

            // Set result
            maps.add(
                    new HashMap<String, Object>() {{
                        put("no", String.format("%3s", no).replace(' ', '0'));
                        put("name", resultAlls.get(finalI).get("name"));
                        put("image_default", imgDefault);
                        put("image_shiny", imgShiny);
                        put("height", height);
                        put("weight", weight);
                        put("elements", types);
                    }}
            );
        }
        return maps;
    }

}
