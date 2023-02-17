package junit;

public interface UserDao {
    int countById(String id) ;
    void updateEmail(String id, String email) ;
}
