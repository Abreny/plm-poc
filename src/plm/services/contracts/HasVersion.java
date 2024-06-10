package plm.services.contracts;

import plm.model.VersionSchema;

public interface HasVersion {
	String getVersion();

	VersionSchema getVersionSchema();

	void setVersionSchema(VersionSchema versionSchema);
}
