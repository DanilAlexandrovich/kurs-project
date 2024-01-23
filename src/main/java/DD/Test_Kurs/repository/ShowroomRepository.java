package DD.Test_Kurs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import DD.Test_Kurs.entity.Showroom;

@Repository
public interface ShowroomRepository extends JpaRepository<Showroom, Long> {
}
