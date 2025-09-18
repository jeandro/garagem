package com.estapar.garagem.repository;
import com.estapar.garagem.domain.Spot;
import com.estapar.garagem.domain.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SpotRepository extends JpaRepository<Spot, Long> {
    List<Spot> findBySectorAndOccupiedFalse(Sector sector);
    long countBySectorAndOccupiedTrue(Sector sector);
}
