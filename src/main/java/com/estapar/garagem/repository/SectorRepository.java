package com.estapar.garagem.repository;
import com.estapar.garagem.domain.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface SectorRepository extends JpaRepository<Sector, Long> {
    Optional<Sector> findByName(String name);
}
