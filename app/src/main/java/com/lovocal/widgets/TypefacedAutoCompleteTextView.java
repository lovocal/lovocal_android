

package com.lovocal.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

import com.lovocal.R;


/**
 *  Custom AutoCompleteTextView to apply a font
 */
public class TypefacedAutoCompleteTextView extends AutoCompleteTextView {

    public TypefacedAutoCompleteTextView(final Context context, final AttributeSet attrs) {

        super(context, attrs);

        if (attrs != null) {
            // Get Custom Attribute Name and value
            final TypedArray styledAttrs = context
                            .obtainStyledAttributes(attrs, R.styleable.TypefacedAutoCompleteTextView);
            final int typefaceCode = styledAttrs
                            .getInt(R.styleable.TypefacedAutoCompleteTextView_fontStyle, -1);
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

    public TypefacedAutoCompleteTextView(final Context context) {
        super(context);
    }
}
