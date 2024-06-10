package plm.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import plm.model.Document;

@Repository
@Transactional
public class DocumentDao {

	@Autowired
	private SessionFactory sessionFactory;

	public void create(Document document) {

		Session currentSession = sessionFactory.getCurrentSession();
		currentSession.save(document);
		currentSession.flush();
	}

	public Document get(String reference, String version, int iteration) {
		//
		// Implementation and returned value are not relevant for this exercise
		//
		return null;
	}

	public void update(Document document) {
		Session currentSession = sessionFactory.getCurrentSession();
		currentSession.update(document);
		currentSession.flush();
	}

}
