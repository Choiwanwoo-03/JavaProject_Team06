package CommunicateClient;

import database.DataReader;

 // 로그인 검증 전용 클래스
public class LoginManager {

    private final DataReader reader = new DataReader();

     // 실패 시 null 반환, 로그인 성공 시 닉네임 반환
    public String verify(String userId, String userPwd) {
        return reader.login(userId, userPwd);
    }
}