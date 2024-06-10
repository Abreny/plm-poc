package plm.repositories;

import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Repository;

import plm.model.Document;
import plm.model.Part;

@Repository
public interface DocumentRepository {
	Set<Document> findByPart(Part part);

	Optional<Part> findPartLinkedTo(Document document);
}
