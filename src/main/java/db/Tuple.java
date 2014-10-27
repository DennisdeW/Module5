package db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Dennis (Copied from Data & Informatie project - di07)
 *
 */
public final class Tuple {

	private final Object[] items;
	private final int size;

	/**
	 * Creates a new <code>Tuple</code> from the given items.
	 * 
	 * @param items
	 *            Any array of any <code>Object</code>s
	 */
	public Tuple(Object... items) {
		this.items = items;
		size = items.length;
	}

	/**
	 * Boxes the names of primitive types
	 * 
	 * @param i
	 *            The name of an arbitrary class
	 * @return The boxed variant of the supplied name if it exists, or the name
	 *         itself otherwise
	 */
	private static String box(String i) {
		switch (i) {
		case "int":
			return "Integer";
		case "boolean":
			return "Boolean";
		case "char":
			return "Character";
		case "long":
			return "Long";
		case "short":
			return "Short";
		case "byte":
			return "Byte";
		case "float":
			return "Float";
		case "double":
			return "Double";
		default:
			return i;
		}
	}

	/**
	 * Returns the amount of items in this tuple.
	 * 
	 * @return The amount of items in this tuple.
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Returns the <code>index</code>'th item in this tuple, if it exists.
	 * 
	 * @param index
	 *            The index of the desired item.
	 * @return The item at the given index.
	 */
	public Object getItem(int index) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException();
		return items[index];
	}

	/**
	 * Gets all items in his <code>Tuple</code>.
	 * 
	 * @return All items in this <code>Tuple</code>, in order.
	 */
	public Object[] getItems() {
		return items;
	}

	/**
	 * Gets the specified range of items from this <code>Tuple</code>, if the
	 * range is contained in the <code>Tuple</code>.
	 * 
	 * @param start
	 *            The low (inclusive) end of the desired range.
	 * @param end
	 *            The high (inclusive) end of the desired range.
	 * @return The items in this <code>Tuple</code> from <code>start</code> to
	 *         <code>end</code>.
	 */
	public Object[] getRange(int start, int end) {
		end++;
		if (start < 0 || end >= size || start >= end)
			throw new IndexOutOfBoundsException();
		return Arrays.copyOfRange(items, start, end);
	}

	/**
	 * Determines if this <code>Tuple</code> contains elements which are exactly
	 * the same as the supplied <code>Class</code>es.
	 * 
	 * @param classes
	 *            An arbitrary amount of <code>Class</code>es to test this tuple
	 *            against
	 * @return <code>true</code> if for 0 <= i < <code>this.getSize()</code>,
	 *         the i-th element of the <code>Tuple</code> is of type
	 *         <code>classes[i]</code>.
	 */
	public boolean isOfTypes(Class<?>... classes) {
		if (classes.length != size)
			return false;
		for (int i = 0; i < size; i++) {
			if (items[i] == null)
				continue;
			String inTuple = box(items[i].getClass().toString()
					.replaceAll("java.lang.", "").replaceAll("class", "")
					.replaceAll(" ", ""));
			String inClass = box(classes[i].toString()
					.replaceAll("java.lang.", "").replaceAll("class", "")
					.replaceAll(" ", ""));
			if (!inTuple.equalsIgnoreCase(inClass)
					&& !(items[i].getClass().isAssignableFrom(classes[i]) || classes[i]
							.isAssignableFrom(items[i].getClass())))
				return false;
		}
		return true;
	}

	/**
	 * Returns the types of items in this <code>Tuple</code> in the form
	 * [class1, class2, ...]
	 */
	public String getTypes() {
		String res = "[";
		for (Object o : items)
			res += o.getClass().getName() + ", ";
		res = res.substring(0, res.length() - 2);
		res += "]";
		return res;
	}

	@Override
	public boolean equals(Object x) {
		if (!(x instanceof Tuple))
			return false;

		Tuple t = (Tuple) x;

		if (t.size != size)
			return false;

		for (int i = 0; i < size; i++) {
			if (!items[i].equals(t.getItem(i)))
				return false;
		}

		return true;
	}

	/**
	 * Represents this <code>Tuple</code> in the form [Object1, Object2, ...
	 * ObjectI]
	 */
	@Override
	public String toString() {
		String res = "[";
		for (Object o : items)
			res += o == null ? "null, " : (o.toString() + ", ");
		res = res.substring(0, res.length() - 2) + "]";
		return res;
	}

	/**
	 * Generates an array of <code>Tuple</code>s from the given
	 * <code>ResultSet</code>.<br>
	 * Also calls <code>DatabaseMaker.clean(ResultSet)</code>.
	 * 
	 * @param in
	 *            An untouched <code>ResultSet</code> (such that
	 *            <code>in.isBeforeFirst()</code>)
	 * @return An array containing <code>Tuples</code> formed from the rows of
	 *         the supplied <code>ResultSet</code>.
	 * @throws SQLException
	 *             If navigation through the <code>ResultSet</code> fails.
	 */
	public static Tuple[] fromResultSet(ResultSet in) throws SQLException {
		List<Tuple> res = new ArrayList<Tuple>();
		int colCount = in.getMetaData().getColumnCount();
		boolean empty = !in.next();
		while (!in.isAfterLast() && !empty) {
			Object[] objects = new Object[colCount];
			for (int i = 0; i < colCount; i++)
				objects[i] = in.getObject(i + 1);
			res.add(new Tuple(objects));
			in.next();
		}
		DatabaseManager.clean(in);
		return res.toArray(new Tuple[] {});
	}

	/**
	 * Makes a shallow copy of a <code>Tuple</code>. <br>
	 * The tuple is a new instance, but any objects contained in the original
	 * are the same.
	 * 
	 * @param original
	 *            Any <code>Tuple</code>
	 * @return A copy of <code>original</code>
	 */
	public static Tuple copy(Tuple original) {
		Object[] items = new Object[original.size];
		for (int i = 0; i < original.size; i++)
			items[i] = original.items[i];
		return new Tuple(items);
	}
}