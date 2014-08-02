

package com.lovocal.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.CheckBox;

import com.lovocal.R;


/**
 *Custom CheckBox to apply a font
 */
public class TypefacedCheckBox extends CheckBox {

    public TypefacedCheckBox(final Context context, final AttributeSet attrs) {

        super(context, attrs);

        if (attrs != null) {
            // Get Custom Attribute Name and value
            final TypedArray styledAttrs = context
                            .obtainStyledAttributes(attrs, R.styleable.TypefacedCheckBox);
            final int typefaceCode = styledAttrs
                            .getInt(R.styleable.TypefacedCheckBox_fontStyle, -1);
            styledAttrs.recycle();

            // Typeface.createFromAsset doesn't work in the layout editor.
            // Skipping...
            if (isInEditMode()) {
                return;
            }

            final Typeface typeface = TypefaceCache
                            .get(context.getAssets(), typefaceCode);
            setTypeface(typeface);
        }
    }

    public TypefacedCheckBox(final Context context) {
        super(context);
    }
}
