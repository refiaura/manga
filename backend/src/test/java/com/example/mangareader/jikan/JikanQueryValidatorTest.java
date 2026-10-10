package com.example.mangareader.jikan;

import com.example.mangareader.manga.integration.jikan.JikanQueryValidator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Validasi filter/sort terhadap allowlist parameter Jikan yang benar-benar tersedia. */
class JikanQueryValidatorTest {

    @Test
    void acceptsDocumentedParams() {
        assertThatCode(() -> JikanQueryValidator.validate("manga", "publishing", "score", "desc"))
                .doesNotThrowAnyException();
        assertThatCode(() -> JikanQueryValidator.validate(null, null, null, null))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsUnsupportedType() {
        assertThatThrownBy(() -> JikanQueryValidator.validate("anime", null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsUnsupportedStatus() {
        assertThatThrownBy(() -> JikanQueryValidator.validate(null, "airing", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsUnsupportedOrderByAndSort() {
        assertThatThrownBy(() -> JikanQueryValidator.validate(null, null, "views", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> JikanQueryValidator.validate(null, null, "score", "random"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
