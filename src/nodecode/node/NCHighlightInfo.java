package nodecode.node;

public class NCHighlightInfo<T> implements Highlightable {

	private T i;

	private boolean isHighlighted = false;

	public NCHighlightInfo(T i) {
		this.i = i;
	}

	@Override
	public void setHighlighted(boolean isHighlighted) {
		this.isHighlighted = isHighlighted;
	}

	@Override
	public boolean isHighlighted() {
		return this.isHighlighted;
	}

	public T getReal() {
		return this.i;
	}

}
