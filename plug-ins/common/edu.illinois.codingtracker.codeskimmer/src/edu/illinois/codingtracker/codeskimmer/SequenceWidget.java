package edu.illinois.codingtracker.codeskimmer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.textchanges.TextChangeOperation;

/**
 * 
 * @author Connor Simmons
 *
 */
public class SequenceWidget extends OperationViewWidget {

	private static final Color BACKGROUND_COLOR = new Color(null, 255, 255, 255);
	private static final Color HIGHLIGHT_COLOR = new Color(null, 225, 100, 100);
	private static final Color SELECT_COLOR = new Color(null, 180, 180, 0);
	private static final Color ADD_TEXT_COLOR = new Color(null, 0, 225, 0);
	private static final Color DELETE_TEXT_COLOR = new Color(null, 130, 0, 0);
	private static final Color REPLACE_TEXT_COLOR = new Color(null, 0, 0, 225);
	private static final Color OTHER_OPERATION_COLOR = new Color(null, 0, 225,
			225);
	private static final Color FADE_FILTER = new Color(null, 190, 190, 190);

	private static final int CELL_WIDTH = 1;
	private static final int CELL_MAX_HEIGHT = 50;
	private static final int CELL_MIN_HEIGHT = 6;
	private static final int MARGIN_WIDTH = 3;
	private static final int NUM_ROWS = 4;

	private Image keyImage;
	private Canvas keyCanvas;

	private Image timelineImage;
	private int maxTextChange;
	private int minTextChange;

	private Canvas timelineCanvas;
	private ScrollBar timelineHBar;
	private ScrollBar timelineVBar;
	private Point timelineOrigin;

	private int currentColumnHighlight;
	private int currentColumnSelection;

	public SequenceWidget(Composite parent, int style) {
		super(parent, style);

		this.setLayout(new GridLayout(2, false));
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.BEGINNING;
		gridData.heightHint = CELL_MAX_HEIGHT * NUM_ROWS + 30;
		this.setLayoutData(gridData);

		createTimelineKeyCanvas();
		createTimelineCanvas();
	}

	private void createTimelineKeyCanvas() {
		drawKeyImage();

		keyCanvas = new Canvas(this, SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE);
		keyCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				gc.drawImage(keyImage, 0, 0);
			}
		});

		GridData gridData = new GridData();
		gridData.verticalAlignment = SWT.FILL;
		gridData.horizontalAlignment = SWT.BEGINNING;
		keyCanvas.setLayoutData(gridData);
	}

	private void drawKeyImage() {
		if (keyImage != null) {
			keyImage.dispose();
		}

		int width = CodeSkimmerConstants.SEQUENCE_VIEW_KEY_WIDTH;
		int height = CodeSkimmerConstants.WIDGET_HEIGHT;
		keyImage = new Image(getDisplay(), width, height);

		GC gc = new GC(keyImage);

		gc.setBackground(getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		gc.fillRectangle(0, 0, width, height);

		Font font = new Font(getDisplay(), "Arial", 10, SWT.BOLD);
		gc.setFont(font);
		gc.drawText("Added\nText", 0, 0, true);
		gc.drawText("Replaced\nText", 0, CELL_MAX_HEIGHT, true);
		gc.drawText("Deleted\nText", 0, 2 * CELL_MAX_HEIGHT, true);
		gc.drawText("Other\nOperation", 0, 3 * CELL_MAX_HEIGHT, true);

		font.dispose();
		gc.dispose();
	}

	private void createTimelineCanvas() {
		drawTimelineImage();
		timelineOrigin = new Point(0, 0);

		timelineCanvas = new Canvas(this, SWT.NO_BACKGROUND
				| SWT.NO_REDRAW_RESIZE | SWT.V_SCROLL | SWT.H_SCROLL);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.heightHint = CodeSkimmerConstants.WIDGET_HEIGHT;
		timelineCanvas.setLayoutData(gridData);

		timelineHBar = timelineCanvas.getHorizontalBar();
		timelineHBar.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				int hSelection = timelineHBar.getSelection();
				int destX = -hSelection - timelineOrigin.x;
				Rectangle rect = timelineImage.getBounds();
				timelineCanvas.scroll(destX, 0, 0, 0, rect.width, rect.height,
						false);
				timelineOrigin.x = -hSelection;
			}
		});
		timelineVBar = timelineCanvas.getVerticalBar();
		timelineVBar.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				int vSelection = timelineVBar.getSelection();
				int destY = -vSelection - timelineOrigin.y;
				Rectangle rect = timelineImage.getBounds();
				timelineCanvas.scroll(0, destY, 0, 0, rect.width, rect.height,
						false);
				timelineOrigin.y = -vSelection;
			}
		});
		timelineCanvas.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event e) {
				GC gc = e.gc;
				gc.drawImage(timelineImage, timelineOrigin.x, timelineOrigin.y);
				Rectangle rect = timelineImage.getBounds();
				Rectangle client = timelineCanvas.getClientArea();
				int marginWidth = client.width - rect.width;
				if (marginWidth > 0) {
					gc.fillRectangle(rect.width, 0, marginWidth, client.height);
				}
				int marginHeight = client.height - rect.height;
				if (marginHeight > 0) {
					gc.fillRectangle(0, rect.height, client.width, marginHeight);
				}
			}
		});

		resizeTimelineCanvasScroll();
	}

	private void drawTimelineImage() {
		if (timelineImage != null) {
			timelineImage.dispose();
		}

		int width = 1, height = 1;
		if (operations != null) {
			width = Math.max(1,
					operations.size() * CELL_WIDTH + (operations.size() - 1)
							* MARGIN_WIDTH);
			height = Math.max(1, NUM_ROWS * CELL_MAX_HEIGHT);
		}

		timelineImage = new Image(getDisplay(), width, height);
		GC gc = new GC(timelineImage);
		gc.setBackground(getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		gc.fillRectangle(0, 0, width, height);

		if (operations != null) {
			// determine scale for bar height
			maxTextChange = Integer.MIN_VALUE;
			minTextChange = Integer.MAX_VALUE;
			for (int i = 0; i < operations.size(); i++) {
				UserOperation op = operations.get(i);
				if (op instanceof TextChangeOperation) {
					TextChangeOperation tOp = (TextChangeOperation) op;
					int charsChanged = Math.max(tOp.getReplacedText().length(),
							tOp.getNewText().length());
					if (charsChanged < minTextChange) {
						minTextChange = charsChanged;
					}
					if (charsChanged > maxTextChange) {
						maxTextChange = charsChanged;
					}
				}
			}

			for (int i = 0; i < operations.size(); i++) {
				UserOperation op = operations.get(i);

				// draw operation
				drawOperation(gc, op, i, false, false);
			}
		}

		gc.dispose();
	}

	private void drawOperation(GC gc, UserOperation op, int x_offset,
			boolean isHighlighted, boolean isSelected) {
		int x = x_offset * (CELL_WIDTH + MARGIN_WIDTH);
		int y = (CELL_MAX_HEIGHT) / 2;

		// get operation color
		Color operationColor;
		if (isAddTextOperation(op)) {
			operationColor = ADD_TEXT_COLOR;
		} else if (isReplaceTextOperation(op)) {
			operationColor = REPLACE_TEXT_COLOR;
			y += CELL_MAX_HEIGHT;
		} else if (isDeleteTextOperation(op)) {
			operationColor = DELETE_TEXT_COLOR;
			y += 2 * CELL_MAX_HEIGHT;
		} else {
			operationColor = OTHER_OPERATION_COLOR;
			y += 3 * CELL_MAX_HEIGHT;
		}
		int cell_height = getOperationBarHeight(op);
		y -= cell_height / 2;

		// apply any filters
		if (filters != null && filters.size() > 0
				&& !Filter.checkFiltersForMatch(filters, op)) {
			operationColor = this.applyFadeFilter(operationColor);
		}

		// draw clear bar and get operationColor
		if (!isHighlighted && !isSelected) {
			gc.setForeground(BACKGROUND_COLOR);
		} else if (isSelected) {
			gc.setForeground(SELECT_COLOR);
			operationColor = combineColors(operationColor, SELECT_COLOR);
		} else {
			gc.setForeground(HIGHLIGHT_COLOR);
			operationColor = combineColors(operationColor, HIGHLIGHT_COLOR);
		}
		gc.drawRectangle(x_offset * (CELL_WIDTH + MARGIN_WIDTH), 0, CELL_WIDTH,
				NUM_ROWS * CELL_MAX_HEIGHT);

		gc.setForeground(operationColor);
		gc.drawRectangle(x, y, CELL_WIDTH, cell_height);

		// play around with colors for highlight and make sure it is disposed
		if (isHighlighted || isSelected) {
			operationColor.dispose();
		}
	}

	private void drawHighlightedOperation(int columnSelection) {
		if (operations != null && operations.size() > 0) {
			GC gc = new GC(timelineImage);

			// first clear old highlight
			if (currentColumnHighlight >= 0
					&& currentColumnHighlight < operations.size()) {
				drawOperation(gc, operations.get(currentColumnHighlight),
						currentColumnHighlight, false,
						currentColumnSelection == currentColumnHighlight);
			}

			if (columnSelection >= 0 && columnSelection < operations.size()) {
				// draw new highlight
				currentColumnHighlight = columnSelection;
				drawOperation(gc, operations.get(currentColumnHighlight),
						currentColumnHighlight, true, false);
			}

			gc.dispose();
		}
	}

	@Override
	public void clearSelection() {
		currentColumnHighlight = -1;
		redraw();
	}

	public void setCurrentOperation(UserOperation op) {
		if (operations != null && operations.size() > 0
				&& timelineImage != null && op != null) {

			drawHighlightedOperation(operations.indexOf(op));

			// do not need to re-create the whole image so just call this
			scrollOperationIntoView(op);
		}
	}

	private void drawSelectedOperation(int columnSelection) {
		if (operations != null && operations.size() > 0) {
			GC gc = new GC(timelineImage);

			// clear old selection
			drawOperation(gc, operations.get(currentColumnSelection),
					currentColumnSelection,
					currentColumnSelection == currentColumnHighlight, false);

			// draw selection
			currentColumnSelection = columnSelection;
			drawOperation(gc, operations.get(currentColumnSelection),
					currentColumnSelection, false, true);

			gc.dispose();
		}
	}

	public void selectOperation(UserOperation op) {
		if (operations != null && timelineImage != null && op != null) {

			drawSelectedOperation(operations.indexOf(op));

			// do not need to re-create the whole image so just call this
			timelineCanvas.redraw();
		}
	}

	private void resizeTimelineCanvasScroll() {
		Rectangle rect = timelineImage.getBounds();
		Rectangle client = timelineCanvas.getClientArea();
		timelineHBar.setMaximum(rect.width);
		timelineVBar.setMaximum(rect.height);
		timelineHBar.setThumb(Math.min(rect.width, client.width));
		timelineVBar.setThumb(Math.min(rect.height, client.height));
		timelineHBar.setIncrement(CELL_WIDTH + MARGIN_WIDTH);
		timelineHBar.setPageIncrement((CELL_WIDTH + MARGIN_WIDTH) * 20);

		updateTimlineCanvasPosition();
	}

	private void updateTimlineCanvasPosition() {
		int hSelection = timelineHBar.getSelection();
		int vSelection = timelineVBar.getSelection();
		timelineOrigin.x = -Math.max(0, hSelection);
		timelineOrigin.y = -Math.max(0, vSelection);
		timelineCanvas.redraw();
	}

	private void scrollOperationIntoView(UserOperation operation) {
		int x = operations.indexOf(operation) * (CELL_WIDTH + MARGIN_WIDTH);
		int y = 0;

		Rectangle bounds = new Rectangle(x, y, CELL_WIDTH + MARGIN_WIDTH,
				CELL_MAX_HEIGHT * NUM_ROWS);
		Rectangle area = timelineCanvas.getClientArea();

		if (bounds.x < timelineHBar.getSelection()) {
			timelineHBar.setSelection(Math.max(0, bounds.x));
		} else if ((bounds.x + bounds.width) > (area.width + timelineHBar
				.getSelection())) {
			timelineHBar.setSelection((bounds.x + bounds.width) - area.width);
		}

		updateTimlineCanvasPosition();
	}

	public void setMouseClickListener(MouseListener listener) {
		// ensure only 1 active mouse listener
		try {
			timelineCanvas.removeMouseListener(listener);
		} catch (Exception e) {

		}
		timelineCanvas.addMouseListener(listener);
	}

	public void executeTimelineSelectionMouseAction(MouseEvent arg0) {
		// get mouse x location on timeline and find nearest operation
		int x_pos = arg0.x - timelineOrigin.x;
		int opIndex = (int) (x_pos + ((CELL_WIDTH + MARGIN_WIDTH) / 2))
				/ (CELL_WIDTH + MARGIN_WIDTH);
		if (operations != null && opIndex < operations.size() && opIndex > 0) {
			selectOperation(operations.get(opIndex));
		}
	}

	public void redraw() {
		drawTimelineImage();
		drawHighlightedOperation(currentColumnHighlight);
		drawSelectedOperation(currentColumnSelection);
		resizeTimelineCanvasScroll();
	}

	public UserOperation getHighlightedOperation() {
		if (operations != null && currentColumnHighlight < operations.size()) {
			return operations.get(currentColumnHighlight);
		}
		return null;
	}

	public UserOperation getSelectedOperation() {
		if (operations != null && currentColumnSelection < operations.size()) {
			return operations.get(currentColumnSelection);
		}
		return null;
	}

	private boolean isAddTextOperation(UserOperation operation) {
		if (operation instanceof TextChangeOperation) {
			TextChangeOperation tOp = (TextChangeOperation) operation;
			if (tOp.getReplacedText().length() == 0
					&& tOp.getNewText().length() > 0) {
				return true;
			}
		}
		return false;
	}

	private boolean isReplaceTextOperation(UserOperation operation) {
		if (operation instanceof TextChangeOperation) {
			TextChangeOperation tOp = (TextChangeOperation) operation;
			if (tOp.getReplacedText().length() > 0
					&& tOp.getNewText().length() > 0) {
				return true;
			}
		}
		return false;
	}

	private boolean isDeleteTextOperation(UserOperation operation) {
		if (operation instanceof TextChangeOperation) {
			TextChangeOperation tOp = (TextChangeOperation) operation;
			if (tOp.getReplacedText().length() > 0
					&& tOp.getNewText().length() == 0) {
				return true;
			}
		}
		return false;
	}

	private Color applyFadeFilter(Color c) {
		int r = Math.max(c.getRed(), FADE_FILTER.getRed());
		int g = Math.max(c.getGreen(), FADE_FILTER.getGreen());
		int b = Math.max(c.getBlue(), FADE_FILTER.getBlue());

		return new Color(null, r, g, b);
	}

	private int getOperationBarHeight(UserOperation operation) {
		if (operation instanceof TextChangeOperation) {
			TextChangeOperation tOp = (TextChangeOperation) operation;
			int charsChanged = Math.max(tOp.getReplacedText().length(), tOp
					.getNewText().length());

			double x = Math.sqrt(charsChanged);
			double a = Math.sqrt(minTextChange);
			double b = Math.sqrt(maxTextChange);

			double opScale = ((double) x - a) / (b - a);
			int step2 = (int) (opScale * ((double) CELL_MAX_HEIGHT - CELL_MIN_HEIGHT));

			return CELL_MIN_HEIGHT + step2;
		}
		return (CELL_MAX_HEIGHT + CELL_MIN_HEIGHT) / 2;
	}

	@Override
	public void handleMouseClickEvent(MouseEvent e) {
		executeTimelineSelectionMouseAction(e);
	}
}
