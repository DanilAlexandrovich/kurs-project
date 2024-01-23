package DD.Test_Kurs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import DD.Test_Kurs.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}