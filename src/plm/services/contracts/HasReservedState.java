package plm.services.contracts;

public interface HasReservedState {
	String getReservedBy();

	boolean isReserved();

	void setReservedBy(String userId);
}
