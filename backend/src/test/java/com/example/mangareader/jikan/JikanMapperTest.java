package com.example.mangareader.jikan;

import com.example.mangareader.manga.integration.jikan.JikanDto;
import com.example.mangareader.manga.integration.jikan.JikanMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit test pemetaan response Jikan → domain internal, termasuk field null/kosong. */
class JikanMapperTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void mapsFullSearchResponse() throws Exception {
        String json = """
        {
          "data": [
            {
              "mal_id": 1,
              "title": "One Piece",
              "title_english": "ONE PIECE",
              "type": "Manga",
              "status": "Publishing",
              "score": 8.95,
              "scored_by": 1000,
              "rank": 1,
              "popularity": 2,
              "chapters": 1100,
              "volumes": 107,
              "url": "https://myanimelist.net/manga/1/One_Piece",
              "images": { "jpg": { "image_url": "https://cdn/1.jpg" } },
              "genres": [ {"mal_id": 1, "name": "Action"}, {"mal_id": 2, "name": "Adventure"} ],
              "authors": [ {"entry": {"mal_id": 52, "name": "Oda, Eiichiro"}} ]
            }
          ],
          "pagination": { "page": 1, "per_page": 25, "total_pages": 400, "items_count": 10000, "has_next_page": true }
        }
        """;
        JikanDto.PageResponse resp = mapper.readValue(json, JikanDto.PageResponse.class);
        List<JikanDto.MangaItem> items = JikanMapper.itemsOf(resp);
        assertThat(items).hasSize(1);
        JikanDto.MangaItem item = items.get(0);
        assertThat(JikanMapper.coverUrl(item)).isEqualTo("https://cdn/1.jpg");
        assertThat(JikanMapper.normalizeScore(item.score())).isEqualTo(8.95);
        assertThat(JikanMapper.genreNames(item)).containsExactly("Action", "Adventure");
        assertThat(JikanMapper.roleNames(item.authors())).containsExactly("Oda, Eiichiro");
        assertThat(JikanMapper.alternativeTitle(item)).isEqualTo("ONE PIECE");
        assertThat(resp.pagination().has_next_page()).isTrue();
    }

    @Test
    void handlesNullAndEmptyFields() throws Exception {
        String json = """
        { "data": [ { "mal_id": 99, "title": "Unknown", "score": null, "synopsis": null,
                      "images": {}, "genres": [], "title_english": "", "title_japanese": null } ] }
        """;
        JikanDto.PageResponse resp = mapper.readValue(json, JikanDto.PageResponse.class);
        JikanDto.MangaItem item = JikanMapper.itemsOf(resp).get(0);
        assertThat(JikanMapper.coverUrl(item)).isNull();
        assertThat(JikanMapper.normalizeScore(item.score())).isNull();
        assertThat(JikanMapper.genreNames(item)).isEmpty();
        // title_english kosong → fallback ke synonyms (kosong) → title_japanese (null) → null
        assertThat(JikanMapper.alternativeTitle(item)).isNull();
        assertThat(resp.pagination()).isNull();
    }

    @Test
    void scoreZeroTreatedAsNoScore() {
        assertThat(JikanMapper.normalizeScore(0.0)).isNull();
        assertThat(JikanMapper.normalizeScore(null)).isNull();
        assertThat(JikanMapper.normalizeScore(7.5)).isEqualTo(7.5);
    }

    @Test
    void ignoresUnknownFieldsGracefully() throws Exception {
        String json = """
        { "data": [ { "mal_id": 1, "title": "X", "brand_new_field": {"deep": [1,2,3]} } ],
          "pagination": { "page": 2, "unexpected": true } }
        """;
        JikanDto.PageResponse resp = mapper.readValue(json, JikanDto.PageResponse.class);
        assertThat(JikanMapper.itemsOf(resp)).hasSize(1);
        assertThat(resp.pagination().page()).isEqualTo(2);
    }

    @Test
    void itemsOfHandlesNullResponse() {
        assertThat(JikanMapper.itemsOf(null)).isEmpty();
        assertThat(JikanMapper.itemOf((JikanDto.SingleResponse) null)).isNull();
    }
}
