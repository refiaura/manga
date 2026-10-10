package com.example.mangareader.manga.integration.jikan;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/** Mapper murni (tanpa Spring) dari DTO Jikan ke domain internal. Nullable-safe. */
public final class JikanMapper {

    /** ObjectMapper khusus konversi node "data" yang bentuknya beragam antar endpoint. */
    private static final ObjectMapper CONVERTER = new ObjectMapper();

    private JikanMapper() {}

    /**
     * Normalisasi field "data" pada list-endpoint Jikan (/manga, /top/manga).
     * Bentuk aktual: array objek manga. Didukung juga bentuk [{data:{...}}] untuk ketahanan,
     * serta objek tunggal {..} (dianggap satu item). Nilai null/bukan-list → empty.
     */
    public static List<JikanDto.MangaItem> itemsOf(JikanDto.PageResponse response) {
        if (response == null || response.data() == null) return List.of();
        Object raw = response.data();
        if (raw instanceof List<?> list) {
            return list.stream()
                    .map(JikanMapper::coerceItem)
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }
        // Fallback defensif: data berupa objek tunggal.
        JikanDto.MangaItem single = coerceItem(raw);
        return single == null ? List.of() : List.of(single);
    }

    /** Konversi node "data" menjadi MangaItem; mengembalikan null bila tidak valid. */
    private static JikanDto.MangaItem coerceItem(Object node) {
        if (node == null) return null;
        try {
            if (node instanceof JikanDto.MangaItem item) return item;
            // Bentuk bungkus { "data": {...} } jika upstream berubah.
            if (node instanceof java.util.Map<?, ?> map && map.containsKey("data") && !(map.containsKey("mal_id"))) {
                return CONVERTER.convertValue(map.get("data"), JikanDto.MangaItem.class);
            }
            return CONVERTER.convertValue(node, JikanDto.MangaItem.class);
        } catch (IllegalArgumentException ex) {
            return null; // item rusak/inkompatibel → lewati, jangan simpan sebagai metadata valid
        }
    }

    public static JikanDto.MangaItem itemOf(JikanDto.SingleResponse response) {
        return response == null ? null : response.data();
    }

    public static String coverUrl(JikanDto.MangaItem item) {
        if (item == null || item.images() == null) return null;
        if (item.images().jpg() != null && isHttpUrl(item.images().jpg().image_url())) {
            return item.images().jpg().image_url();
        }
        if (item.images().webp() != null && isHttpUrl(item.images().webp().image_url())) {
            return item.images().webp().image_url();
        }
        return null;
    }

    static boolean isHttpUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    /** Score 0 atau null dianggap "belum ada skor" sesuai kontrak Jikan/MAL. */
    public static Double normalizeScore(Double score) {
        if (score == null || score <= 0) return null;
        return score;
    }

    public static String alternativeTitle(JikanDto.MangaItem item) {
        if (item == null) return null;
        if (item.titleEnglish() != null && !item.titleEnglish().isBlank()) return item.titleEnglish();
        if (item.synonyms() != null && !item.synonyms().isEmpty()) return item.synonyms().get(0);
        return item.titleJapanese();
    }

    public static List<String> genreNames(JikanDto.MangaItem item) {
        return names(item == null ? null : item.genres());
    }

    public static List<JikanDto.NamedRef> genreRefs(JikanDto.MangaItem item) {
        return item == null || item.genres() == null ? List.of() : item.genres();
    }

    public static List<String> names(List<JikanDto.NamedRef> refs) {
        if (refs == null) return List.of();
        return refs.stream()
                .filter(r -> r != null && r.name() != null && !r.name().isBlank())
                .map(JikanDto.NamedRef::name)
                .toList();
    }

    public static List<String> roleNames(List<JikanDto.RoleEntry> entries) {
        if (entries == null) return List.of();
        return entries.stream()
                .filter(e -> e != null && e.entry() != null && e.entry().name() != null)
                .map(e -> e.entry().name())
                .toList();
    }
}
