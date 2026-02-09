package com.team.coin_simulator;

import java.awt.Desktop;
import java.net.URI;

public class SocialLoginService {

    // 구글 로그인창 열기
    public void loginWithGoogle() {
        try {
            // 실제로는 클라이언트 ID와 Redirect URI가 포함된 URL이어야 합니다.
            String url = "https://accounts.google.com/o/oauth2/auth?client_id=YOUR_CLIENT_ID&redirect_uri=http://localhost:8080&response_type=code&scope=email profile";
            Desktop.getDesktop().browse(new URI(url));
            
            // 이후 로컬 서버(localhost:8080)를 임시로 실행하여 인증 코드를 수신하는 로직 필요
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 카카오 로그인창 열기
    public void loginWithKakao() {
        try {
            String url = "https://kauth.kakao.com/oauth/authorize?client_id=YOUR_REST_API_KEY&redirect_uri=http://localhost:8080&response_type=code";
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}