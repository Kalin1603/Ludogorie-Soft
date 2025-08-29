package com.ludogoriesoft.repository;
import com.ludogoriesoft.entity.StockData;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
@ApplicationScoped
public class StockDataRepository implements PanacheRepository<StockData> {
    /**
     * This is our caching method. It finds the most recent stock data entry for a given company
     * that was fetched today.
     *
     * @param companyId The ID of the company.
     * @return An Optional containing the StockData if found, otherwise an empty Optional.
     */
    public Optional<StockData> findLatestByCompanyIdForToday(Long companyId) {
        // Get the timestamp for the beginning of today (midnight UTC).
        Instant startOfDay = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);

        // Panache Query:
        // "company.id = ?1" -> matches the company ID.
        // "and fetchedAt >= ?2" -> ensures the data was fetched on or after the start of today.
        // "order by fetchedAt desc" -> gets the newest entry first.
        // .firstResultOptional() -> returns only the first result, wrapped in an Optional.
        return find("company.id = ?1 and fetchedAt >= ?2 order by fetchedAt desc",
                companyId,
                startOfDay)
                .firstResultOptional();
    }
}