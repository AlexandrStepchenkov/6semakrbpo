package com.example.rbpo2.service;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.rbpo2.model.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
@Transactional
public class DB {

    @PersistenceContext
    private EntityManager em;

    public User saveUser(int id, String name, String email, String phone) {
        User u = em.find(User.class, id);
        if (u == null) {
            u = new User();
            u.setUserId(id);
        }
        u.setName(name);
        u.setEmail(email);
        u.setPhone(phone);
        return em.merge(u);
    }

    public List<User> users() {
        return em.createQuery("from User", User.class).getResultList();
    }

    public void deleteUser(int id) {
        User u = em.find(User.class, id);
        if (u != null) em.remove(u);
    }
}