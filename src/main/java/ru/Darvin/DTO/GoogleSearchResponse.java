package ru.Darvin.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleSearchResponse {
    @JsonProperty("items")
    private List<SearchItem> items;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchItem {
        @JsonProperty("link")
        private String link; // URL изображения
    }
}
