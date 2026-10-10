package com.example.mangareader.manga.integration.jikan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO eksternal untuk response Jikan v4. Semua field dianggap nullable/tidak tersedia
 * (JsonIgnoreProperties ignoreUnknown) karena kontrak upstream dapat berubah.
 * Bentuk mengikuti dokumentasi resmi https://docs.jikan.moe/ (endpoint /manga, /top/manga, /genres/manga).
 */
public class JikanDto {

    /**
     * Bentuk "data" pada response Jikan v4 berbeda per endpoint:
     * - list (pencarian /manga, /top/manga): data berupa array objek manga langsung.
     * - detail (/manga/{id}): data berupa satu objek manga.
     * Karena itu tipe JSON yang diterima bisa beragam; normalisasi dilakukan di JikanMapper.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PageResponse(
            Object data,
            Pagination pagination
    ) {}

    /** Response detail: { "data": { ...manga... }, ... } */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SingleResponse(
            MangaItem data
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pagination(
            Integer page,
            Integer per_page,
            Integer total_pages,
            Long items_count,
            String next_url,
            Boolean has_next_page
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MangaItem(
            Long mal_id,
            String title,
            @JsonProperty("title_english") String titleEnglish,
            @JsonProperty("title_japanese") String titleJapanese,
            List<String> synonyms,
            Images images,
            String type,
            String status,
            Double score,
            @JsonProperty("scored_by") Integer scoredBy,
            Integer rank,
            Integer popularity,
            Integer chapters,
            Integer volumes,
            String synopsis,
            List<NamedRef> genres,
            List<NamedRef> themes,
            List<NamedRef> demographics,
            List<RoleEntry> authors,
            List<RoleEntry> serializations,
            String url
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Images(
            ImageEntry jpg,
            ImageEntry webp
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ImageEntry(
            String image_url,
            @JsonProperty("small_image_url") String small_image_url,
            @JsonProperty("large_image_url") String large_image_url
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NamedRef(Integer mal_id, String name, String type) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RoleEntry(NamedRef role_ref, NamedRef entry) {}

    /** Response /genres/manga: array langsung dari objek {mal_id,name,type}. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GenreList(List<NamedRef> data) {}
}
