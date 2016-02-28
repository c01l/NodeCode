package nodecode.node;

public class NCHighlightInfo<T> {

	private T i;

	private boolean isHighlighted = false;

	public NCHighlightInfo(T i) {
		this.i = i;
	}

	public void setHighlighted(boolean isHighlighted) {
		this.isHighlighted = isHighlighted;
	}

	public boolean isHighlighted() {
		return this.isHighlighted;
	}

	public T getReal() {
		return this.i;
	}

}
