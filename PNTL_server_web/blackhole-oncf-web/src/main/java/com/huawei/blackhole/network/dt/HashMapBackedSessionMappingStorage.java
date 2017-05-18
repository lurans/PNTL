package com.huawei.blackhole.network.dt;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.jasig.cas.client.session.SessionMappingStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashMapBackedSessionMappingStorage implements SessionMappingStorage {
    /**
     * Maps the ID from the CAS server to the Session.
     */
    private final Map<String, HttpSession> MANAGED_SESSIONS = new HashMap<String, HttpSession>();

    /**
     * Maps the Session ID to the key from the CAS Server.
     */
    private final Map<String, String> ID_TO_SESSION_KEY_MAPPING = new HashMap<String, String>();

    private final Map<String, Long> SESSIONS_ACCESSTIME = new HashMap<String, Long>();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public synchronized HttpSession removeSessionByMappingId(String mappingId) {
        final HttpSession session = MANAGED_SESSIONS.get(mappingId);

        if (session != null) {
            removeBySessionById(session.getId());
        }

        return session;
    }

    @Override
    public synchronized void removeBySessionById(String sessionId) {
        final String key = ID_TO_SESSION_KEY_MAPPING.get(sessionId);
        if (logger.isDebugEnabled()) {
            if (key != null) {
                logger.debug("Found mapping for session.  Session Removed.");
            } else {
                logger.debug("No mapping for session found.  Ignoring.");
            }
        }
        MANAGED_SESSIONS.remove(key);
        ID_TO_SESSION_KEY_MAPPING.remove(sessionId);
        // 在删除SessionID函数中删除SessionID与访问时间关系
        SESSIONS_ACCESSTIME.remove(sessionId);

    }

    @Override
    public synchronized void addSessionById(String mappingId, HttpSession session) {
        ID_TO_SESSION_KEY_MAPPING.put(session.getId(), mappingId);
        MANAGED_SESSIONS.put(mappingId, session);
        // 在添加SessionID函数中存储SessionID与访问时间关系
        SESSIONS_ACCESSTIME.put(session.getId(), System.currentTimeMillis());
    }

    // 新增通过SessionID读取Session上次TGT续期时间接口
    public synchronized Long getAccessTimeBySessionId(HttpSession session) {
        if (session == null) {
            return null;
        }
        return SESSIONS_ACCESSTIME.get(session.getId());
    }

    // 新增通过SessionID读取Ticket接口
    public synchronized String getMappingIdBySessionId(HttpSession session) {
        if (session == null) {
            return null;
        }
        return ID_TO_SESSION_KEY_MAPPING.get(session.getId());
    }

    // 新增通过SessionID更新本次TGT续期接口
    public synchronized void updateAccessTimeBySessionId(HttpSession session) {
        if (session == null) {
            return;
        }
        SESSIONS_ACCESSTIME.put(session.getId(), System.currentTimeMillis());
    }

}
