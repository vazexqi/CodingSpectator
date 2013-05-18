package edu.illinois.codingtracker.codeskimmer;

import java.util.ArrayList;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import edu.illinois.codingtracker.operations.UserOperation;

/**
 * 
 * @author Connor Simmons
 *
 */
public abstract class OperationViewWidget extends Composite {

	protected ArrayList<UserOperation> operations;
	protected ArrayList<Filter> filters;

	public OperationViewWidget(Composite parent, int style) {
		super(parent, style);
	}

	public void setUserOperations(ArrayList<UserOperation> userOperations) {
		this.operations = userOperations;
		redraw();
	}

	public void setFilters(ArrayList<Filter> filters) {
		this.filters = filters;
		redraw();
	}

	public void setMouseClickListener(MouseListener listener) {
	}

	public void handleMouseClickEvent(MouseEvent e) {
	}
	
	public void clearSelection() {
	}

	public abstract void redraw();
	public abstract void setCurrentOperation(UserOperation op);
	
	/*
	 * Some helper functions
	 */
	protected Color combineColors(Color c1, Color c2) {
		int r = c1.getRed() + c2.getRed();
		int g = c1.getGreen() + c2.getGreen();
		int b = c1.getBlue() + c2.getBlue();

		double scale = 255. / Math.max(Math.max(r, g), b);
		r = (int) (r * scale);
		g = (int) (g * scale);
		b = (int) (b * scale);

		return new Color(null, r, g, b);
	}
}
