package com.youkeda.hashopping.session;

import com.youkeda.hashopping.Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import static org.junit.jupiter.api.Assertions.*;

/** 验证真实 Redis 中的会话保存、读取与注销，不修改业务表。 */
@SpringBootTest(classes = Application.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class RedisSessionStorageTest {
    @Autowired
    private SessionRepository sessions;

    @Test
    void savesLoadsAndDeletesSession() {
        Session session = sessions.createSession();
        session.setAttribute("userId", 1L);
        String id = session.getId();
        try {
            sessions.save(session);
            Session loaded = sessions.findById(id);
            assertNotNull(loaded);
            assertEquals(Long.valueOf(1), loaded.getAttribute("userId"));
            assertEquals(1800, loaded.getMaxInactiveInterval().getSeconds());
            sessions.deleteById(id);
            assertNull(sessions.findById(id));
        } finally {
            sessions.deleteById(id);
        }
    }
}

