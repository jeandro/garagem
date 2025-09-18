package com.estapar.garagem.repository;
import com.estapar.garagem.domain.ParkingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {
    Optional<ParkingSession> findFirstByLicensePlateAndExitTimeIsNullOrderByEntryTimeDesc(String licensePlate);
    @Query(value = "select s.name as sector, cast(ps.exit_time as date) as dt, sum(ps.total_price) " +
            "from parking_sessions ps join sectors s on ps.sector_id = s.id " +
            "where ps.exit_time is not null and cast(ps.exit_time as date) between :from and :to " +
            "group by s.name, dt order by s.name, dt", nativeQuery = true)
    List<Object[]> aggregateRevenue(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
