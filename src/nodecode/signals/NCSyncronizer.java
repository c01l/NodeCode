package nodecode.signals;

import java.util.ArrayList;

import nodes.NodeInterface;
import nodes.signals.SignalInputInterface;
import nodes.signals.SignalOutputInterface;
import nodes.signals.SignalSyncronizer;

public class NCSyncronizer extends SignalSyncronizer {

	private SyncronizerDrawer drawer;
	private SignalSyncronizer real = null;

	public NCSyncronizer(SignalSyncronizer s) {
		super(0, 0);
		this.real = s;

		this.drawer = new SyncronizerDrawer(this);
	}

	public NCSyncronizer(int ins, int outs) {
		super(ins, outs);

		this.drawer = new SyncronizerDrawer(this);
	}

	public SyncronizerDrawer getDrawerInstance() {
		return this.drawer;
	}

	public boolean hasInterface(NodeInterface n) {
		SignalSyncronizer target = this;
		if (this.real != null) {
			target = this.real;
		}

		if (n instanceof SignalInputInterface) {
			for (int i = 0; i < target.getInputSize(); ++i) {
				if (target.getInput(i) == n) {
					return true;
				}
			}
		}
		if (n instanceof SignalOutputInterface) {
			for (int i = 0; i < target.getOutputSize(); ++i) {
				if (target.getOutput(i) == n) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public SignalInputInterface getInput(int i) {
		if (this.real == null)
			return super.getInput(i);
		return this.real.getInput(i);
	}

	@Override
	public SignalOutputInterface getOutput(int i) {
		if (this.real == null)
			return super.getOutput(i);
		return this.real.getOutput(i);
	}

	@Override
	public int getInputSize() {
		if (this.real == null)
			return super.getInputSize();
		return this.real.getInputSize();
	}

	@Override
	public int getOutputSize() {
		if (this.real == null)
			return super.getOutputSize();
		return this.real.getOutputSize();
	}

	@Override
	public ArrayList<SignalInputInterface> getInputs() {
		if (this.real == null)
			return super.getInputs();
		return this.real.getInputs();
	}

	@Override
	public ArrayList<SignalOutputInterface> getOutputs() {
		if (this.real == null)
			return super.getOutputs();
		return this.real.getOutputs();
	}

	@Override
	public void destroy() {
		super.destroy();
		if (this.real != null)
			this.real.destroy();
	}

	@Override
	public boolean equals(Object obj) {
		if (this.real == null)
			return super.equals(obj);
		return this.real.equals(obj);
	}

	@Override
	public int hashCode() {
		if (this.real == null)
			return super.hashCode();
		return this.real.hashCode();
	}

}
