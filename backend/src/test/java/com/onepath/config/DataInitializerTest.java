package com.onepath.config;

import com.onepath.repository.CollegeRepository;
import com.onepath.repository.ServiceCenterRepository;
import com.onepath.repository.ServiceRepository;
import com.onepath.repository.UpdateRepository;
import com.onepath.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CollegeRepository collegeRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ServiceCenterRepository serviceCenterRepository;

    @Mock
    private UpdateRepository updateRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    @DisplayName("migrateSchemaIfNeeded should alter fees and timeline columns to TEXT")
    void testMigrateSchemaIfNeeded() {
        dataInitializer.migrateSchemaIfNeeded();

        verify(jdbcTemplate).execute("ALTER TABLE services ALTER COLUMN fees TYPE TEXT");
        verify(jdbcTemplate).execute("ALTER TABLE services ALTER COLUMN timeline TYPE TEXT");
        verify(jdbcTemplate).execute("ALTER TABLE college_processes ALTER COLUMN fees TYPE TEXT");
        verify(jdbcTemplate).execute("ALTER TABLE college_processes ALTER COLUMN timeline TYPE TEXT");
        verify(jdbcTemplate).execute(contains("CREATE TABLE IF NOT EXISTS mentor_requests"));
    }

    @Test
    @DisplayName("seedVitApDataIfNeeded skips execution when all VIT-AP records already exist")
    void testSeedVitApData_WhenAlreadyExists_DoesNotExecuteScript() {
        when(jdbcTemplate.queryForObject(contains("colleges WHERE short_name = 'VIT'"), eq(Integer.class)))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(contains("college_processes cp JOIN colleges c"), eq(Integer.class)))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(contains("services WHERE slug = 'vit-ap-reporting'"), eq(Integer.class)))
                .thenReturn(1);

        dataInitializer.seedVitApDataIfNeeded();

        // Should NOT execute the seed script because all records exist
        verify(jdbcTemplate, never()).execute(contains("DO $$"));
    }

    @Test
    @DisplayName("seedVitApDataIfNeeded executes seed script when VIT-AP records are missing")
    void testSeedVitApData_WhenMissing_ExecutesSeedScript() {
        when(jdbcTemplate.queryForObject(contains("colleges WHERE short_name = 'VIT'"), eq(Integer.class)))
                .thenReturn(0);
        when(jdbcTemplate.queryForObject(contains("college_processes cp JOIN colleges c"), eq(Integer.class)))
                .thenReturn(0);
        when(jdbcTemplate.queryForObject(contains("services WHERE slug = 'vit-ap-reporting'"), eq(Integer.class)))
                .thenReturn(0);

        dataInitializer.seedVitApDataIfNeeded();

        // Should execute the seed script
        verify(jdbcTemplate).execute(anyString());
    }
}
