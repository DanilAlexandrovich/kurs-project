package DD.Test_Kurs.service;



import DD.Test_Kurs.dto.UserDto;
import DD.Test_Kurs.entity.User;

import java.util.List;

public interface UserService {
    void saveUser(UserDto userDto);

    User findUserByUsername(String username);

    List<UserDto> findAllUsers();

    void addRoleToUser(String username, String roleName);

}
