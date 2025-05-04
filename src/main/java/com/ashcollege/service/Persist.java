package com.ashcollege.service;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Component
@Transactional


public class Persist {

    private static final Logger LOGGER = LoggerFactory.getLogger(Persist.class);

    @PersistenceContext
    private EntityManager em;

    private Session session() {
        return em.unwrap(Session.class);
    }

    public <T> void saveAll(List<T> objects) {
        for (T object : objects) {
            session().saveOrUpdate(object);
        }
    }

    public void save(Object object) {
        session().saveOrUpdate(object);
    }

    public void remove(Object o) {
        session().remove(o);
    }

    public <T> T loadObject(Class<T> clazz, int id) {
        return session().get(clazz, id);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> loadList(Class<T> clazz) {
        return session().createQuery("FROM " + clazz.getSimpleName()).list();
    }

    public Session getQuerySession() {
        return session();
    }
}
