package skhu.ht.hotthink.api.user.service;

import skhu.ht.hotthink.api.MessageState;
import skhu.ht.hotthink.api.domain.User;
import skhu.ht.hotthink.api.user.model.NewUserDTO;
import skhu.ht.hotthink.api.user.model.UserInfoDTO;
import skhu.ht.hotthink.api.user.model.UserModificationDTO;
import skhu.ht.hotthink.security.model.dto.UserAuthenticationModel;

import java.util.List;

public interface UserService {
    public List<User> findAll();
    public MessageState setUser(NewUserDTO newUserDTO, int initPoint);
    public boolean saveUser(UserModificationDTO user);
    public User findByEmail(String email);
    public UserAuthenticationModel findUserAuthByEmailAndPw(String email, String pw);
    public UserInfoDTO findUserInfo();
}
