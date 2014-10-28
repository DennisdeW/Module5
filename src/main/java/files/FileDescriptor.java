package files;

import db.Tuple;

public class FileDescriptor {

	private String identifier;
	private int owner;
	private long size;

	public FileDescriptor(String identifier, int owner, long size) {
		this.setIdentifier(identifier);
		this.setOwner(owner);
		this.setSize(size);
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public int getOwner() {
		return owner;
	}

	public void setOwner(int owner) {
		this.owner = owner;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public boolean equals(Object o) {
		if (o instanceof FileDescriptor) {
			FileDescriptor other = (FileDescriptor) o;
			return other.identifier.equals(identifier);
		}
		return false;
	}

	public String toString() {
		return "FD:[" + identifier + ", " + owner + ", " + size + "]";
	}

	public int hashCode() {
		return identifier.hashCode();
	}

	public static FileDescriptor forTuple(Tuple tup) {
		if (!tup.isOfTypes(byte[].class, Integer.class, Long.class))
			throw new IllegalArgumentException(
					"Invalid Tuple passed to FileDescriptor.forTuple()!");
		String id = new String((byte[]) tup.getItem(0));
		int owner = (int) tup.getItem(1);
		
		long size = -1L;
		Object o = tup.getItem(2);
		if (o instanceof Long)
			size = ((Long) o).longValue();
		else if (o instanceof Integer)
			size = (long) ((Integer) o).intValue();
		else if (o instanceof Short)
			size = (long) ((Short) o).shortValue();
		else if (o instanceof Byte)
			size = (long) ((Byte) o).byteValue();
		
		return new FileDescriptor(id, owner, size);
	}
}
