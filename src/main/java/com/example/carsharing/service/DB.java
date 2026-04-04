package com.example.carsharing.service;

import com.example.carsharing.model.*;
import jakarta.persistence.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class DB {

    @PersistenceContext
    private EntityManager em;

    public User saveUser(int id, String name, String email, String phone) {
        User u = em.find(User.class, id);
        if (u == null) u = new User(id, name, email, phone);
        else { u.setName(name); u.setEmail(email); u.setPhone(phone); }
        return em.merge(u);
    }

    public List<User> users() {
        return em.createQuery("from User", User.class).getResultList();
    }

    public void deleteUser(int id) {
        em.createQuery("delete from Ride r where r.userId = :id").setParameter("id", id).executeUpdate();
        User u = em.find(User.class, id);
        if (u != null) em.remove(u);
    }

    public Car saveCar(int id, String model, String plate, double price) {
        Car c = em.find(Car.class, id);
        if (c == null) c = new Car(id, model, plate, price, true);
        else { c.setModel(model); c.setLicensePlate(plate); c.setPricePerKm(price); }
        return em.merge(c);
    }

    public List<Car> cars() {
        return em.createQuery("from Car", Car.class).getResultList();
    }

    public void deleteCar(int id) {
        em.createQuery("update Car c set c.available = false where c.carId = :id")
                .setParameter("id", id).executeUpdate();
    }

    public Ride addRide(int userId, int carId, double dist, double hours) {
        Car car = em.find(Car.class, carId);
        if (car == null || !car.isAvailable()) return null;

        Ride r = new Ride(null, userId, carId, dist, hours, "CREATED");
        em.persist(r);
        return r;
    }

    public List<Ride> rides() {
        return em.createQuery("from Ride", Ride.class).getResultList();
    }

    public Ride startRide(Long id) {
        Ride r = em.find(Ride.class, id);
        if (r == null || !"CREATED".equals(r.getStatus())) return null;
        Car c = em.find(Car.class, r.getCarId());
        if (c == null || !c.isAvailable()) return null;

        c.setAvailable(false);
        r.setStatus("IN_PROGRESS");
        return em.merge(r);
    }

    public Payment completeRide(Long id) {
        Ride r = em.find(Ride.class, id);
        if (r == null || !"IN_PROGRESS".equals(r.getStatus())) return null;
        Car c = em.find(Car.class, r.getCarId());
        if (c != null) c.setAvailable(true);
        r.setStatus("COMPLETED");

        Payment p = new Payment();
        p.setRideId(id);
        p.setAmount(r.getDistanceKm() * c.getPricePerKm());
        em.persist(p);
        return p;
    }

    public List<Car> availableCars() {
        return em.createQuery("from Car c where c.available = true", Car.class).getResultList();
    }

    public List<Ride> ridesByUser(int userId) {
        return em.createQuery("from Ride r where r.userId = :uid", Ride.class)
                .setParameter("uid", userId).getResultList();
    }

    public double totalRevenue() {
        return em.createQuery("select coalesce(sum(p.amount), 0) from Payment p", Double.class)
                .getSingleResult();
    }

    public int activeRidesCount() {
        return em.createQuery("select count(r) from Ride r where r.status = 'IN_PROGRESS'", Long.class)
                .getSingleResult().intValue();
    }

    public List<Car> busyCars() {
        return em.createQuery(
                "select c from Car c where c.carId in (select r.carId from Ride r where r.status = 'IN_PROGRESS')",
                Car.class).getResultList();
    }

    public List<Payment> payments() {
        return em.createQuery("from Payment", Payment.class).getResultList();
    }
}