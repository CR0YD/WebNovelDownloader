
public class List<Type> {

	private Element first;
	private int length;

	public List() {

	}

	public void add(Type content) {
		if (first == null) {
			first = new Element(content);
			length++;
			return;
		}
		Element current = first;
		while (current.getNext() != null) {
			current = current.getNext();
		}
		current.setNext(new Element(content));
		current.getNext().setPrevious(current);
		length++;
	}

	public void add(Type content, int idx) {
		Element current = first;
		if (idx == 0) {
			first = new Element(content);
			first.setNext(current);
			first.getNext().setPrevious(first);
			length++;
			return;
		}
		try {
			for (int i = 0; i < idx; i++) {
				current = current.getNext();
			}
			if (current == null) {
				add(content);
				return;
			}
			current.getPrevious().setNext(new Element(content));
			current.getPrevious().getNext().setPrevious(current.getPrevious());
			current.getPrevious().getNext().setNext(current);
			current.setPrevious(current.getPrevious().getNext());
			length++;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Type get(int idx) {
		Element current;
		current = first;
		try {
			for (int i = 0; i < idx; i++) {
				current = current.getNext();
			}
			return current.getContent();
		} catch (Exception e) {
			return null;
		}
	}

	public int length() {
		return length;
	}

	private class Element {
		private Element next, previous;
		private final Type CONTENT;

		public Element(Type content) {
			CONTENT = content;
		}

		public void setNext(Element next) {
			this.next = next;
		}

		public Element getNext() {
			return next;
		}

		public void setPrevious(Element previous) {
			this.previous = previous;
		}

		public Element getPrevious() {
			return previous;
		}

		public Type getContent() {
			return CONTENT;
		}
	}

}
