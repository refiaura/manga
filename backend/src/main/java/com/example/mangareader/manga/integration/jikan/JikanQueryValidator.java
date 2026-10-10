package com.example.mangareader.manga.integration.jikan;

import java.util.List;
import java.util.Set;

/** Allowlist filter/sort yang dipetakan ke parameter Jikan yang benar-benar tersedia. */
public final class JikanQueryValidator {

    // GET /manga supports: type=manga|onehit|manhua|manhwaga|lightnovel|novel|oneshot|doujin|roleplay|manga (per docs "Manga Type")
    public static final Set<String> TYPES = Set.of(
            "manga", "onehit", "manhua", "manhwa", "novel", "lightnovel", "oneshot", "doujin", "roleplay");

    // GET /manga supports: status=publishing|complete|hiatus|discontinued|upcoming
    public static final Set<String> STATUSES = Set.of("publishing", "complete", "hiatus", "discontinued", "upcoming");

    // GET /manga supports: order_by on documented fields
    public static final Set<String> ORDER_BY = Set.of(
            "title", "start_date", "end_date", "score", "scored_by", "rank",
            "popularity", "members", "favorites");

    public static final Set<String> SORTS = Set.of("asc", "desc");

    private JikanQueryValidator() {}

    public static void validate(String type, String status, String orderBy, String sort) {
        if (type != null && !TYPES.contains(type)) throw new IllegalArgumentException("type tidak didukung: " + type);
        if (status != null && !STATUSES.contains(status)) throw new IllegalArgumentException("status tidak didukung: " + status);
        if (orderBy != null && !ORDER_BY.contains(orderBy)) throw new IllegalArgumentException("orderBy tidak didukung: " + orderBy);
        if (sort != null && !SORTS.contains(sort)) throw new IllegalArgumentException("sort tidak didukung: " + sort);
    }
}
