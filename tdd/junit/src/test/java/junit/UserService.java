package junit;

public class UserService {
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public void changeEmail(String id, String email){
        if(userDao.countById(id) == 0) throw new IllegalArgumentException("존재하지 않는 아이디 입니다.");
        userDao.updateEmail(id, email);
    }
}
