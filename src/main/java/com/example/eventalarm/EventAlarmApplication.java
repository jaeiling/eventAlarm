package com.example.eventalarm;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EventAlarmApplication {

    public static void main(String[] args) {
        // .env 파일 로드 → System Property 로 주입 (Railway 배포 시엔 환경변수가 우선됨)
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            dotenv.entries().forEach(e ->
                System.setProperty(e.getKey(), e.getValue())
            );
        } catch (Exception ignored) {}

        SpringApplication.run(EventAlarmApplication.class, args);
    }

}
