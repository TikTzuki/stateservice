package org.minerva.stateservice;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Quickstart {
    EntityManagerFactory sessionFactory;

    public void setUp() throws Exception {
        sessionFactory = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");
    }

    public void saving(){
        EntityManager entityManager = sessionFactory.createEntityManager();
        entityManager.getTransaction().begin();

        entityManager.getTransaction().commit();
        entityManager.close();
    }

}
