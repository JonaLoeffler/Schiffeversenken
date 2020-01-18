package com.jona.schiffeversenken;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.jona.utility.Util;

public class GameBoard extends ViewGroup {

    // Ship names
    public static final String NAME_AIRCRAFT_CARRIER = "Aircraft Carrier";
    public static final String NAME_BATTLESHIP = "Battleship";
    public static final String NAME_CRUISER = "Cruiser";
    public static final String NAME_DESTROYER = "Destroyer";
    private static final String TAG = "Gameboard";
    /**
     * Player's Ships grid, stores ships of player
     */
    private final int[][] playersShipsGrid = new int[10][10];
    /**
     * Player's states grid, stores tile states
     */
    private final int[][] playersStatesGrid = new int[10][10];
    private int childWidth;
    private int childHeight;
    private int parentWidth;
    private int parentHeight;
    private Rect mTmpChildRect = new Rect();

    public GameBoard(Context context) {
        super(context);
    }

    public GameBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GameBoard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * creates and adds a statetile to the viewgroup
     */
    public void addStateTile(int position, int state) {
        StateTile tile = new StateTile(getContext(), position, state);
        this.addView(tile);
        tile.confirmChangesInLayout();

        playersStatesGrid[Util.getX(position)][Util.getY(position)] = state;
    }

    public boolean isTileUsed(int positionID) {
        if (playersStatesGrid[Util.getX(positionID)][Util.getY(positionID)] != 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * changes the ships attributes to the given values
     */
    private Ship changeShipAttributes(int newPosition, int newOrientation, Ship oldShip) {
        Ship newShip = oldShip;
        eraseFieldsInArray(oldShip);
        newShip.setPosition(newPosition);
        newShip.setOrientation(newOrientation);
        newShip.confirmChangesInLayout();
        markFieldsInArray(newShip);
        return newShip;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    /**
     * deletes the entries of the given ship
     */
    private void eraseFieldsInArray(Ship ship) {

        try {
            for (int i = 0; i < playersShipsGrid.length; i++) {
                for (int j = 0; j < playersShipsGrid[i].length; j++) {
                    if (playersShipsGrid[i][j] == ship.getType()) {
                        playersShipsGrid[i][j] = 0;
                    }
                }
            }
        } catch (NullPointerException e) {
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new GameBoard.LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    /**
     * tests if a ship is on given tile
     */
    public boolean isShipHit(int posID) {
        int x = Util.getX(posID);
        int y = Util.getY(posID);

        if (playersShipsGrid[x][y] != 0)
            return true;
        else
            return false;
    }

    /**
     * checks if a ship can be placed at that position
     */
    private boolean isShipPositionLegal(int newPosition, int newOrientation, Ship oldShip) {
        int x = Util.getX(newPosition);
        int y = Util.getY(newPosition);

        // pr�fung ob schiff rechts aus spielfeld herausragt
        if (x + oldShip.getShipLength() > 10 && newOrientation == LayoutParams.ORIENTATION_EAST) {
            Log.d(TAG, "Too far East");
            return false;
        }

        // pr�fung ob schiff oben aus spielfeld herausragt
        if ((y + 1) - oldShip.getShipLength() < 0 && newOrientation == LayoutParams.ORIENTATION_NORTH) {
            Log.d(TAG, "Too far North");
            return false;
        }

        // pr�fung ob andere schiffe im weg sind
        try {
            for (int i = 0; i < oldShip.getShipLength(); i++) {
                if (newOrientation == LayoutParams.ORIENTATION_EAST) {
                    if (playersShipsGrid[x + i][y] != 0 && playersShipsGrid[x + i][y] != oldShip.getType()) {
                        Log.d(TAG, "Tile occupied");
                        return false;
                    }
                }
                if (newOrientation == LayoutParams.ORIENTATION_NORTH) {
                    if (playersShipsGrid[x][y - i] != 0 && playersShipsGrid[x][y - i] != oldShip.getType()) {
                        Log.d(TAG, "Tile occupied");
                        return false;
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "Out of bounds");
            return false;
        }

        return true;
    }

    /**
     * marks the fields occupied by the given ship in the grid
     */
    private void markFieldsInArray(Ship ship) {

        int x = Util.getX(ship.getPosition());
        int y = Util.getY(ship.getPosition());

        for (int i = 0; i < ship.getShipLength(); i++) {
            if (ship.getOrientation() == LayoutParams.ORIENTATION_EAST) {
                playersShipsGrid[x + i][y] = ship.getType();
            }
            if (ship.getOrientation() == LayoutParams.ORIENTATION_NORTH) {
                playersShipsGrid[x][y - i] = ship.getType();
            }
        }
        Util.printArray(playersShipsGrid);
    }

    /**
     * lays out children
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int count = getChildCount();
        int tileNumber = 0;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() != GONE) {
                measureChildWithMargins(child, parentWidth, childWidth, parentHeight, childHeight);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();

                int xPos = Util.getX(lp.position);
                int yPos = Util.getY(lp.position);




                switch (lp.type) {
                    case LayoutParams.LAYOUT_TYPE_TILE:

                        int x = Util.getX(tileNumber);
                        int y = Util.getY(tileNumber);

                        mTmpChildRect.left = x * childWidth;
                        mTmpChildRect.top = y * childHeight;
                        mTmpChildRect.right = (x + 1) * childWidth;
                        mTmpChildRect.bottom = (y + 1) * childHeight;

                        child.layout(mTmpChildRect.left, mTmpChildRect.top, mTmpChildRect.right, mTmpChildRect.bottom);
                        tileNumber++;
                        break;
                    case LayoutParams.LAYOUT_TYPE_AIRCRAFTCARRIER:
                        if (lp.orientation == LayoutParams.ORIENTATION_NORTH) {
                            mTmpChildRect.left = childWidth * xPos;
                            mTmpChildRect.top = childHeight * (yPos - 4);
                            mTmpChildRect.right = childWidth * (xPos + 1);
                            mTmpChildRect.bottom = childHeight * (yPos + 1);
                        }
                        if (lp.orientation == LayoutParams.ORIENTATION_EAST) {
                            mTmpChildRect.left = childWidth * xPos;
                            mTmpChildRect.top = childHeight * yPos;
                            mTmpChildRect.right = childWidth * (xPos + 5);
                            mTmpChildRect.bottom = childHeight * (yPos + 1);
                        }
                        child.layout(mTmpChildRect.left, mTmpChildRect.top, mTmpChildRect.right, mTmpChildRect.bottom);

                        break;
                    case LayoutParams.LAYOUT_TYPE_BATTLESHIP:
                        if (lp.orientation == LayoutParams.ORIENTATION_NORTH) {
                            mTmpChildRect.left = childWidth * xPos;
                            mTmpChildRect.top = childHeight * (yPos - 3);
                            mTmpChildRect.right = childWidth * (xPos + 1);
                            mTmpChildRect.bottom = childHeight * (yPos + 1);
                        }
                        if (lp.orientation == LayoutParams.ORIENTATION_EAST) {
                            mTmpChildRect.left = childWidth * xPos;
                            mTmpChildRect.top = childHeight * yPos;
                            mTmpChildRect.right = childWidth * (xPos + 4);
                            mTmpChildRect.bottom = childHeight * (yPos + 1);
                        }

                        child.layout(mTmpChildRect.left, mTmpChildRect.top, mTmpChildRect.right, mTmpChildRect.bottom);
                        break;

                    case LayoutParams.LAYOUT_TYPE_CRUISER_1:
                    case LayoutParams.LAYOUT_TYPE_CRUISER_2:
                        if (lp.orientation == LayoutParams.ORIENTATION_NORTH) {
                            mTmpChildRect.left = childWidth * xPos;
                            mTmpChildRect.top = childHeight * (yPos - 2);
                            mTmpChildRect.right = childWidth * (xPos + 1);
                            mTmpChildRect.bottom = childHeight * (yPos + 1);
                        }
                        if (lp.orientation == LayoutParams.ORIENTATION_EAST) {
                            mTmpChildRect.left = childWidth * xPos;
                            mTmpChildRect.top = childHeight * yPos;
                            mTmpChildRect.right = childWidth * (xPos + 3);
                            mTmpChildRect.bottom = childHeight * (yPos + 1);
                        }

                        child.layout(mTmpChildRect.left, mTmpChildRect.top, mTmpChildRect.right, mTmpChildRect.bottom);
                        break;
                    case LayoutParams.LAYOUT_TYPE_DESTROYER:
                        if (lp.orientation == LayoutParams.ORIENTATION_NORTH) {
                            mTmpChildRect.left = childWidth * xPos;
                            mTmpChildRect.top = childHeight * (yPos - 1);
                            mTmpChildRect.right = childWidth * (xPos + 1);
                            mTmpChildRect.bottom = childHeight * (yPos + 1);
                        }
                        if (lp.orientation == LayoutParams.ORIENTATION_EAST) {
                            mTmpChildRect.left = childWidth * xPos;
                            mTmpChildRect.top = childHeight * yPos;
                            mTmpChildRect.right = childWidth * (xPos + 2);
                            mTmpChildRect.bottom = childHeight * (yPos + 1);
                        }
                        child.layout(mTmpChildRect.left, mTmpChildRect.top, mTmpChildRect.right, mTmpChildRect.bottom);
                        break;
                    case LayoutParams.LAYOUT_TYPE_STATETILE:
                        int x2 = Util.getX(lp.position);
                        int y2 = Util.getY(lp.position);

                        mTmpChildRect.left = x2 * childWidth;
                        mTmpChildRect.top = y2 * childHeight;
                        mTmpChildRect.right = (x2 + 1) * childWidth;
                        mTmpChildRect.bottom = (y2 + 1) * childHeight;

                        child.layout(mTmpChildRect.left, mTmpChildRect.top, mTmpChildRect.right, mTmpChildRect.bottom);
                        break;

                    default:
                        Log.e(TAG, "Something bad has happened");
                        break;
                }
            }
        }
    }

    /**
     * determines width and height of a single tile
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        parentWidth = MeasureSpec.getSize(widthMeasureSpec);

        childHeight = parentHeight / 10;
        childWidth = parentWidth / 10;

    }

    /**
     * checks if position is legal, if yes applies changes, if no discards
     * everything
     */
    public Ship placeShipInGrid(int newPosition, int newOrientation, Ship oldShip) {

        if (isShipPositionLegal(newPosition, newOrientation, oldShip)) {
            Log.d(TAG, "Selected Position is legal");
            return changeShipAttributes(newPosition, newOrientation, oldShip);
        } else {
            Log.d(TAG, "Position illegal");
            return null;
        }
    }

    /**
     * prevent Layout from scrolling
     */
    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    /**
     * Layout params fuer child views von gameboard types: tile, ship, statetile
     */
    public static class LayoutParams extends MarginLayoutParams implements Constants {

        public static final int LAYOUT_TYPE_STATETILE = 1;
        public static final int LAYOUT_TYPE_TILE = 0;
        public static final int LAYOUT_TYPE_AIRCRAFTCARRIER = -1;
        public static final int LAYOUT_TYPE_BATTLESHIP = -2;
        public static final int LAYOUT_TYPE_CRUISER_1 = -3;
        public static final int LAYOUT_TYPE_CRUISER_2 = -4;
        public static final int LAYOUT_TYPE_DESTROYER = -5;

        public static final int POSITION_LEGAL = 3;
        public static final int POSITION_ILLEGAL = 4;

        public static final int ORIENTATION_EAST = 0;
        public static final int ORIENTATION_NORTH = 1;

        public int position = 0;
        public int type = LAYOUT_TYPE_TILE;
        public int orientation = ORIENTATION_EAST;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.com_jona_schiffeversenken_Ship);
            position = a.getInt(R.styleable.com_jona_schiffeversenken_Ship_position, position);
            type = a.getInt(R.styleable.com_jona_schiffeversenken_Ship_layout_type, type);
            orientation = a.getInt(R.styleable.com_jona_schiffeversenken_Ship_ship_orientation, orientation);

            a.recycle();
        }

        public LayoutParams(int position, int type, int orientation) {
            super(1, 1);
            this.type = type;
            if (position > 0)
                this.position = position;
            if (orientation > 0)
                this.orientation = orientation;
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public void setOrientation(int orientation) {
            this.orientation = orientation;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public void setType(int type) {
            this.type = type;
        }
    }

}
