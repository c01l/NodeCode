package gui.utils;

public interface ListEntryDrawerCreator<T> {
	ListEntryDrawer<T> create(T x);
}
