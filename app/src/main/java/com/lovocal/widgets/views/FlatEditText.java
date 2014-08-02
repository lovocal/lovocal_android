package com.lovocal.widgets.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.EditText;

import com.lovocal.R;
import com.lovocal.widgets.Attributes;
import com.lovocal.widgets.FlatUI;

/**
 * User: eluleci
 * Date: 24.10.2013
 * Time: 21:09
 */
public class FlatEditText extends EditText implements Attributes.AttributeChangeListener {

    private Attributes attributes;

    private int style = 0;

    private boolean hasOwnTextColor;
    private boolean hasOwnHintColor;

    public FlatEditText(Context context) {
        super(context);
        init(null);
    }

    public FlatEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public FlatEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        if (attributes == null)
            attributes = new Attributes(this);

        if (attrs != null) {

            // getting android default tags for textColor and textColorHint
            hasOwnTextColor = attrs.getAttributeValue(FlatUI.androidStyleNameSpace, "textColor") != null;
            hasOwnHintColor = attrs.getAttributeValue(FlatUI.androidStyleNameSpace, "textColorHint") != null;

            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FlatEditText);

            // getting common attributes
            int customTheme = a.getResourceId(R.styleable.FlatEditText_theme_flat, Attributes.DEFAULT_THEME);
            attributes.setThemeSilent(customTheme, getResources());

            attributes.setFontFamily(a.getString(R.styleable.FlatEditText_fontFamily));
            attributes.setFontWeight(a.getString(R.styleable.FlatEditText_fontWeight));
            attributes.setFontExtension(a.getString(R.styleable.FlatEditText_fontExtension));

            attributes.setTextAppearance(a.getInt(R.styleable.FlatEditText_textAppearance, Attributes.DEFAULT_EDITTEXT_THEME));
            attributes.setRadius(a.getDimensionPixelSize(R.styleable.FlatEditText_cornerRadius, Attributes.DEFAULT_RADIUS_PX));
            attributes.setBorderWidth(a.getDimensionPixelSize(R.styleable.FlatEditText_borderWidth, Attributes.DEFAULT_BORDER_WIDTH_PX));

            // getting view specific attributes
            style = a.getInt(R.styleable.FlatEditText_fieldStyle, 0);

            a.recycle();
        }

        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setCornerRadius(attributes.getRadius());

        if (style == 0) {             // flat
            if (!hasOwnTextColor) setTextColor(attributes.getColor(3));
            backgroundDrawable.setColor(attributes.getColor(2));
            backgroundDrawable.setStroke(0, attributes.getColor(2));

        } else if (style == 1) {      // box
            if (!hasOwnTextColor) setTextColor(attributes.getColor(2));
            backgroundDrawable.setColor(Color.WHITE);
            backgroundDrawable.setStroke(attributes.getBorderWidth(), attributes.getColor(2));

        } else if (style == 2) {      // transparent
            if (!hasOwnTextColor) setTextColor(attributes.getColor(1));
            backgroundDrawable.setColor(Color.TRANSPARENT);
            backgroundDrawable.setStroke(attributes.getBorderWidth(), attributes.getColor(2));
        }

        setBackgroundDrawable(backgroundDrawable);

        if (!hasOwnHintColor) setHintTextColor(attributes.getColor(3));

        if (attributes.getTextAppearance() == 1) setTextColor(attributes.getColor(0));
        else if (attributes.getTextAppearance() == 2) setTextColor(attributes.getColor(3));

        // check for IDE preview render
        if(!this.isInEditMode()) {
            Typeface typeface = FlatUI.getFont(getContext(), attributes);
            if (typeface != null) setTypeface(typeface);
        }
    }

    public Attributes getAttributes() {
        return attributes;
    }

    @Override
    public void onThemeChange() {
        init(null);
    }
}
