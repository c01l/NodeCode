package gui.utils;

public abstract class ListEntryDrawer<T> {

	protected T elem;

	public ListEntryDrawer(T elem) {
		this.elem = elem;
	}

	@Override
	public abstract String toString();

	public T getReal() {
		return this.elem;
	}
	
}
