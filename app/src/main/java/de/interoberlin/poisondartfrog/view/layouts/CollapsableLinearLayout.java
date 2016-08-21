package de.interoberlin.poisondartfrog.view.layouts;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import de.interoberlin.poisondartfrog.R;

public class CollapsableLinearLayout extends LinearLayout implements ICollapsable {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = CollapsableLinearLayout.class.getSimpleName();

    private boolean collapsed;
    private int collapseTime;
    private int expandTime;

    private final boolean DEFAULT_COLLAPSED = false;
    private final int DEFAULT_COLLAPSE_TIME = 0;
    private final int DEFAULT_EXPAND_TIME = 0;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public CollapsableLinearLayout(Context context) {
        super(context);

        this.collapsed = DEFAULT_COLLAPSED;
        this.collapseTime = DEFAULT_COLLAPSE_TIME;
        this.expandTime = DEFAULT_EXPAND_TIME;
    }

    public CollapsableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.collapsed = DEFAULT_COLLAPSED;
        this.collapseTime = DEFAULT_COLLAPSE_TIME;
        this.expandTime = DEFAULT_EXPAND_TIME;

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CollapsableLinearLayout,
                0, 0);

        try {
            this.collapsed = a.getBoolean(R.styleable.CollapsableLinearLayout_collapsed, DEFAULT_COLLAPSED);
            this.collapseTime = a.getInt(R.styleable.CollapsableLinearLayout_collapseTime, DEFAULT_COLLAPSE_TIME);
            this.expandTime = a.getInt(R.styleable.CollapsableLinearLayout_expandTime, DEFAULT_EXPAND_TIME);
        } finally {
            a.recycle();
        }
    }

    /*
    public CollapsableLinearLayout(Context context, int collapseTime, int expandTime) {
        super(context);

        this.collapseTime = collapseTime;
        this.expandTime = expandTime;
    }
    */

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="extended" desc="Methods">

    @Override
    public void collapseVertically() {
        final int initialHeight = getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    setVisibility(View.GONE);
                } else {
                    getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration(collapseTime);
        startAnimation(a);
    }

    @Override
    public void expandVertically() {
        measure(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = getMeasuredHeight();

        getLayoutParams().height = 0;
        setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                getLayoutParams().height = interpolatedTime == 1
                        ? GridLayout.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration(expandTime);

        startAnimation(a);
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">

    private boolean isCollapsed() {
        return collapsed;
    }

    // </editor-fold>
}
