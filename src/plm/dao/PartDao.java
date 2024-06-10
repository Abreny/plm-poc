package plm.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import plm.model.Part;

@Repository
@Transactional
public class PartDao {

	@Autowired
	private SessionFactory sessionFactory;

	public void create(Part part) {
		Session currentSession = sessionFactory.getCurrentSession();
		currentSession.save(part);
		currentSession.flush();
	}

	public Part get(String reference, String version, int iteration) {
		//
		// Implementation and returned value are not relevant for this exercise
		//
		return null;
	}

	public void update(Part part) {

		Session currentSession = sessionFactory.getCurrentSession();
		currentSession.update(part);
		currentSession.flush();
	}

}
