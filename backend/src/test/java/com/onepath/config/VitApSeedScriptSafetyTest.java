package com.onepath.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class VitApSeedScriptSafetyTest {

    @Test
    @DisplayName("seed_vit_ap_reporting.sql must exist on classpath and be non-destructive")
    void testSeedScriptSafetyAndIdempotency() throws Exception {
        ClassPathResource resource = new ClassPathResource("seed_vit_ap_reporting.sql");
        assertTrue(resource.exists(), "seed_vit_ap_reporting.sql must be packaged in resources");

        String sql;
        try (InputStream is = resource.getInputStream()) {
            sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertFalse(sql.isBlank(), "seed_vit_ap_reporting.sql must not be empty");

        // Verify strictly non-destructive: NO DELETE, DROP, or TRUNCATE
        assertFalse(
                Pattern.compile("\\bDELETE\\s+FROM\\b", Pattern.CASE_INSENSITIVE).matcher(sql).find(),
                "Seed script must NEVER contain DELETE FROM statements to protect production data"
        );
        assertFalse(
                Pattern.compile("\\bDROP\\s+TABLE\\b", Pattern.CASE_INSENSITIVE).matcher(sql).find(),
                "Seed script must NEVER contain DROP TABLE statements"
        );
        assertFalse(
                Pattern.compile("\\bTRUNCATE\\b", Pattern.CASE_INSENSITIVE).matcher(sql).find(),
                "Seed script must NEVER contain TRUNCATE statements"
        );

        // Verify no hardcoded id = numeric
        assertFalse(
                Pattern.compile("\\bid\\s*=\\s*\\d+", Pattern.CASE_INSENSITIVE).matcher(sql).find(),
                "Seed script must NOT contain hardcoded numeric IDs like 'id = 3' which collide with production primary keys"
        );

        // Verify presence of protective idempotency guards
        assertTrue(sql.contains("IF v_process_id IS NULL THEN"), "Must check whether process exists before inserting");
        assertTrue(sql.contains("IF v_service_id IS NULL THEN"), "Must check whether service exists before inserting");
        assertTrue(sql.contains("IF v_college_id IS NULL THEN"), "Must check whether college exists before inserting");

        // Verify valid PL/pgSQL block termination
        assertTrue(sql.trim().endsWith("END $$;"), "Script must properly close PL/pgSQL block with 'END $$;'");
    }
}
