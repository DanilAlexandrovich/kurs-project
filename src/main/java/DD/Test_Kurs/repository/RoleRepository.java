package DD.Test_Kurs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import DD.Test_Kurs.entity.Role;


public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);

}