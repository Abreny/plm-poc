package plm.model;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;

@Entity
@IdClass(plm.model.Document.DocumentPK.class)
public class Document {

	public static class DocumentPK {
		private int iteration;
		private String reference;
		private String version;

		public DocumentPK() {

		}

		public DocumentPK(String reference, String version, int iteration) {
			this.reference = reference;
			this.version = version;
			this.iteration = iteration;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DocumentPK other = (DocumentPK) obj;
			return iteration == other.iteration && Objects.equals(reference, other.reference)
					&& Objects.equals(version, other.version);
		}

		@Override
		public int hashCode() {
			return Objects.hash(iteration, reference, version);
		}

	}

	@Column
	private String documentAttribute1;
	@Column
	private String documentAttribute2;

	@Id
	private int iteration;
	@Column
	private String lifeCycleState;

	@ManyToOne
	private LifeCycleTemplate lifeCycleTemplate;
	@Id
	private String reference;

	@Column
	private boolean reserved;

	@Column
	private String reservedBy;
	@Id
	private String version;

	@ManyToOne
	private VersionSchema versionSchema;

	public Document() {

	}

	public Document(String reference, String version, int iteration) {
		this.reference = reference;
		this.version = version;
		this.iteration = iteration;
	}

	public String getDocumentAttribute1() {
		return documentAttribute1;
	}

	public String getDocumentAttribute2() {
		return documentAttribute2;
	}

	public int getIteration() {
		return iteration;
	}

	public String getLifeCycleState() {
		return lifeCycleState;
	}

	public LifeCycleTemplate getLifeCycleTemplate() {
		return lifeCycleTemplate;
	}

	public String getReference() {
		return reference;
	}

	public String getReservedBy() {
		return reservedBy;
	}

	public String getVersion() {
		return version;
	}

	public VersionSchema getVersionSchema() {
		return versionSchema;
	}

	public boolean isReserved() {
		return reserved;
	}

	public void setDocumentAttribute1(String documentAttribute1) {
		this.documentAttribute1 = documentAttribute1;
	}

	public void setDocumentAttribute2(String documentAttribute2) {
		this.documentAttribute2 = documentAttribute2;
	}

	public void setLifeCycleState(String lifeCycleState) {
		this.lifeCycleState = lifeCycleState;
	}

	public void setLifeCycleTemplate(LifeCycleTemplate lifeCycleTemplate) {
		this.lifeCycleTemplate = lifeCycleTemplate;
	}

	public void setReserved(boolean reserved) {
		this.reserved = reserved;
	}

	public void setReservedBy(String reservedBy) {
		this.reservedBy = reservedBy;
	}

	public void setVersionSchema(VersionSchema versionSchema) {
		this.versionSchema = versionSchema;
	}

}
